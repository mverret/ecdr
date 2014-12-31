/**
 * Copyright (c) Cohesive Integrations, LLC
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or any later version. 
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 * 
 **/
package net.di2e.ecdr.federation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.di2e.ecdr.commons.util.SearchConstants;
import net.di2e.ecdr.libs.result.relevance.RelevanceNormalizer;

import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.operation.ProcessingDetails;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.impl.ProcessingDetailsImpl;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.catalog.plugin.PostFederatedQueryPlugin;
import ddf.catalog.plugin.PreFederatedQueryPlugin;
import ddf.catalog.source.Source;
import ddf.catalog.util.impl.DistanceResultComparator;
import ddf.catalog.util.impl.RelevanceResultComparator;
import ddf.catalog.util.impl.TemporalResultComparator;

/**
 * This class represents a FederationStrategy based on sorting {@link Metacard}s. The sorting is based on the
 * {@link Query}'s {@link SortBy} propertyName. The possible sorting values are {@link Metacard.EFFECTIVE},
 * {@link Result.TEMPORAL}, {@link Result.DISTANCE}, or {@link Result.RELEVANCE} . The supported ordering includes
 * {@link SortOrder.DESCENDING} and {@link SortOrder.ASCENDING}. For this class to function properly a sort value and
 * sort order must be provided.
 * 
 * @see Metacard
 * @see Query
 * @see SortBy
 */
public class NormalizingSortedFederationStrategy extends AbstractFederationStrategy {

    /**
     * The default comparator for sorting by {@link Result.RELEVANCE}, {@link SortOrder.DESCENDING}
     */
    protected static final Comparator<Result> DEFAULT_COMPARATOR = new RelevanceResultComparator( SortOrder.DESCENDING );

    private static final XLogger LOGGER = new XLogger( LoggerFactory.getLogger( NormalizingSortedFederationStrategy.class ) );

    private boolean normalizeResults = true;

    private RelevanceNormalizer relevanceNormalizer = null;

    /**
     * Instantiates a {@code SortedFederationStrategy} with the provided {@link ExecutorService}.
     * 
     * @param queryExecutorService
     *            the {@link ExecutorService} for queries
     */
    public NormalizingSortedFederationStrategy( ExecutorService queryExecutorService, List<PreFederatedQueryPlugin> preQuery, List<PostFederatedQueryPlugin> postQuery, 
            RelevanceNormalizer normalizer ) {
        super( queryExecutorService, preQuery, postQuery );
        relevanceNormalizer = normalizer;
    }

    @Override
    protected Runnable createMonitor( final ExecutorService pool, final Map<Source, Future<SourceResponse>> futures, final QueryResponseImpl returnResults, final Query query ) {

        return new SortedQueryMonitor( pool, futures, returnResults, query );
    }

    @Override
    public void setNormalizeResults( boolean normalize ) {
        LOGGER.debug( "Setting the normalizeResults flag to {}", normalize );
        this.normalizeResults = normalize;
    }

    private class SortedQueryMonitor implements Runnable {

        private QueryResponseImpl returnResults;

        private Map<Source, Future<SourceResponse>> futures;

        private Query query;

        public SortedQueryMonitor( ExecutorService pool, Map<Source, Future<SourceResponse>> futuress, QueryResponseImpl returnResults, Query query ) {

            this.returnResults = returnResults;
            this.query = query;
            this.futures = futuress;
        }

        @SuppressWarnings( { "rawtypes", "unchecked" } )
        @Override
        public void run() {
            String methodName = "run";
            LOGGER.entry( methodName );

            SortBy sortBy = query.getSortBy();
            // Prepare the Comparators that we will use
            Comparator<Result> coreComparator = DEFAULT_COMPARATOR;

            if ( sortBy != null && sortBy.getPropertyName() != null ) {
                PropertyName sortingProp = sortBy.getPropertyName();
                String sortType = sortingProp.getPropertyName();
                SortOrder sortOrder = (sortBy.getSortOrder() == null) ? SortOrder.DESCENDING : sortBy.getSortOrder();
                LOGGER.debug( "Sorting by type: " + sortType );
                LOGGER.debug( "Sorting by Order: " + sortBy.getSortOrder() );

                if ( Result.TEMPORAL.equals( sortType ) ) {
                    coreComparator = new TemporalResultComparator( sortOrder );
                } else if ( Result.DISTANCE.equals( sortType ) ) {
                    coreComparator = new DistanceResultComparator( sortOrder );
                } else if ( Result.RELEVANCE.equals( sortType ) ) {
                    coreComparator = new RelevanceResultComparator( sortOrder );
                } else {
                    // ECDR-67 should improve on this, but right now we will just default to
                    // temporal sort to handle created, effective, modified, etc.
                    coreComparator = new TemporalResultComparator( sortOrder, sortType );
                }
            }

            List<Result> resultList = new ArrayList<Result>();
            long totalHits = 0;
            Set<ProcessingDetails> processingDetails = returnResults.getProcessingDetails();

            long deadline = System.currentTimeMillis() + query.getTimeoutMillis();

            Map<String, Serializable> returnProperties = returnResults.getProperties();
            for ( final Entry<Source, Future<SourceResponse>> entry : futures.entrySet() ) {
                Source site = entry.getKey();
                SourceResponse sourceResponse = null;
                try {
                    sourceResponse = query.getTimeoutMillis() < 1 ? entry.getValue().get() : entry.getValue().get( getTimeRemaining( deadline ), TimeUnit.MILLISECONDS );
                } catch ( InterruptedException e ) {
                    LOGGER.warn( "Couldn't get results from completed federated query on site with ShortName " + site.getId(), e );
                    processingDetails.add( new ProcessingDetailsImpl( site.getId(), e ) );
                } catch ( ExecutionException e ) {
                    LOGGER.warn( "Couldn't get results from completed federated query on site " + site.getId(), e );
                    if ( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "Adding exception to response." );
                    }
                    processingDetails.add( new ProcessingDetailsImpl( site.getId(), e ) );
                } catch ( TimeoutException e ) {
                    LOGGER.warn( "search timed out: " + new Date() + " on site " + site.getId() );
                    processingDetails.add( new ProcessingDetailsImpl( site.getId(), e ) );
                }
                if ( sourceResponse != null ) {
                    List<Result> sourceResults = sourceResponse.getResults();
                    resultList.addAll( sourceResults );
                    long sourceHits = sourceResponse.getHits();

                    totalHits += sourceHits;
                    Map<String, Serializable> newSourceProperties = new HashMap<String, Serializable>();
                    newSourceProperties.put( SearchConstants.TOTAL_HITS, sourceHits );
                    newSourceProperties.put( SearchConstants.TOTAL_RESULTS_RETURNED, sourceResults.size() );

                    Map<String, Serializable> originalSourceProperties = sourceResponse.getProperties();
                    if ( originalSourceProperties != null ) {
                        Serializable object = originalSourceProperties.get( SearchConstants.ELAPSED_TIME );
                        if ( object != null && object instanceof Long ) {
                            newSourceProperties.put( SearchConstants.ELAPSED_TIME, (Long) object );
                            originalSourceProperties.remove( SearchConstants.ELAPSED_TIME );
                            LOGGER.debug( "Setting the elapsedTime responseProperty to {} for source {}", object, site.getId() );
                        }
                        returnProperties.putAll( originalSourceProperties );
                    }
                    returnProperties.put( site.getId(), (Serializable) newSourceProperties );
                    LOGGER.debug( "Setting the query responseProperties for site {}", site.getId() );

                    // Add a List of siteIds so endpoints know what sites got queried
                    Serializable siteListObject = returnProperties.get( SearchConstants.SITE_LIST );
                    if ( siteListObject != null && siteListObject instanceof List<?> ) {
                        ((List) siteListObject).add( site.getId() );
                    } else {
                        siteListObject = new ArrayList<String>();
                        ((List) siteListObject).add( site.getId() );
                        returnProperties.put( SearchConstants.SITE_LIST, (Serializable) siteListObject );
                    }

                }
            }
            LOGGER.debug( "all sites finished returning results: " + resultList.size() );
            if ( normalizeResults ) {
                resultList = relevanceNormalizer.normalize( resultList, query );
            }
            LOGGER.debug( "Sorting the results by {}", coreComparator );
            Collections.sort( resultList, coreComparator );
            returnResults.setHits( totalHits );
            int maxResults = query.getPageSize() > 0 ? query.getPageSize() : Integer.MAX_VALUE;

            returnResults.addResults( resultList.size() > maxResults ? resultList.subList( 0, maxResults ) : resultList, true );
        }

        private long getTimeRemaining( long deadline ) {
            long timeleft;
            if ( System.currentTimeMillis() > deadline ) {
                timeleft = 0;
            } else {
                timeleft = deadline - System.currentTimeMillis();
            }
            return timeleft;
        }

    }
}
