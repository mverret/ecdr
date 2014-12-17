/**
 * Copyright (c) Cohesive Integrations, LLC
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
package net.di2e.ecdr.libs.result.relevance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.di2e.ecdr.commons.filter.StrictFilterDelegate;
import net.di2e.ecdr.commons.filter.config.FilterConfig;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.opengis.filter.sort.SortBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Result;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.operation.Query;
import ddf.catalog.source.UnsupportedQueryException;

/**
 * Normalizes the Relevance of a result set by looking at the contextual criteria, then doing a local calculation of
 * relevance based on the localized result set
 */
public class RelevanceNormalizer {

    public static final String RELEVANCE_TIMER = "RELEVANCE TIMER:";

    private static final Logger LOGGER = LoggerFactory.getLogger( RelevanceNormalizer.class );
    private static final String METADATA_FIELD = "metadata";
    private static final String ID_FIELD = "id";
    private static final String PHRASE_KEY = "q";

    private FilterAdapter filterAdapter;

    public RelevanceNormalizer( FilterAdapter filterAdapter ) {
        this.filterAdapter = filterAdapter;
    }

    /**
     * Normalize the relevance score for the results in the query response based on the contextual query criteria
     * 
     * @param queryResponse
     * @return
     */
    public List<Result> normalize( List<Result> results, Query originalQuery ) {

        SortBy sortBy = originalQuery.getSortBy();
        // We want to do relevance sort if no sort order was specfied or if Relevance sort was specified
        if ( sortBy == null || sortBy.getPropertyName() == null || sortBy.getPropertyName().getPropertyName() == null || Result.RELEVANCE.equals( sortBy.getPropertyName().getPropertyName() ) ) {

            String searchPhrase = getSearchPhrase( originalQuery );
            if ( StringUtils.isNotBlank( searchPhrase ) ) {
                LOGGER.debug( "Query contained search phrase and will be sorted by relevance, performing re-indexing to normalize relevance." );
                Directory directory = null;
                DirectoryReader iReader = null;
                Map<String, Result> docMap = new HashMap<>();
                List<Result> updatedResults = new ArrayList<>();
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                try {
                    Analyzer analyzer = new StandardAnalyzer();

                    // create memory-stored index
                    directory = new RAMDirectory();

                    IndexWriterConfig config = new IndexWriterConfig( Version.LATEST, analyzer );
                    IndexWriter iWriter = new IndexWriter( directory, config );

                    // loop through all of the results and add them to the index
                    for ( Result curResult : results ) {
                        Document doc = new Document();
                        String text = TextParser.parseTextFrom( curResult.getMetacard().getMetadata() );
                        String id = curResult.getMetacard().getId();
                        doc.add( new Field( METADATA_FIELD, text, TextField.TYPE_STORED ) );
                        doc.add( new Field( ID_FIELD, id, TextField.TYPE_STORED ) );
                        iWriter.addDocument( doc );
                        docMap.put( id, curResult );
                    }

                    IOUtils.closeQuietly( iWriter );
                    LOGGER.debug( "{} Document indexing finished in {} seconds.", RELEVANCE_TIMER, (double) stopWatch.getTime() / 1000.0 );
                    // Now search the index:
                    iReader = DirectoryReader.open( directory );
                    IndexSearcher iSearcher = new IndexSearcher( iReader );
                    // Parse a simple query that searches for "text":
                    QueryParser parser = new QueryParser( METADATA_FIELD, analyzer );
                    org.apache.lucene.search.Query query = parser.parse( searchPhrase );
                    ScoreDoc[] hits = iSearcher.search( query, null, docMap.size() ).scoreDocs;
                    LOGGER.debug( "Got back {} results", hits.length );

                    // loop through the indexed search results and update the scores in the original query results
                    for ( ScoreDoc curHit : hits ) {
                        Document doc = iSearcher.doc( curHit.doc );
                        Result result = docMap.get( doc.getField( ID_FIELD ).stringValue() );
                        updatedResults.add( updateResult( result, curHit.score ) );
                        LOGGER.debug( "Relevance for result {} was changed FROM {} TO {}", result.getMetacard().getId(), result.getRelevanceScore(), curHit.score );
                    }
                    // create new query response
                    return updatedResults;

                } catch ( IOException | ParseException e ) {
                    LOGGER.warn( "Received an exception while trying to perform re-indexing, sending original queryResponse on.", e );
                } finally {
                    IOUtils.closeQuietly( iReader );
                    IOUtils.closeQuietly( directory );
                    stopWatch.stop();
                    LOGGER.debug( "{} Total relevance process took {} seconds.", RELEVANCE_TIMER, (double) stopWatch.getTime() / 1000.0 );
                }
            } else {
                LOGGER.debug( "Query is not sorted based on relevance with contextual criteria. Skipping relevance normalization." );
            }
        } else {
            LOGGER.debug( "Query is not sorted based on relevance with contextual criteria. Skipping relevance normalization." );
        }
        return results;
    }

    /**
     * Pull out the string-based search phrase from a query.
     *
     * @param query
     *            Query that possibly contains a search phrase.
     * @return Search phrase or null if no search phrase was found.
     */
    protected String getSearchPhrase( ddf.catalog.operation.Query query ) {
        try {
            Map<String, String> filterParameters = filterAdapter.adapt( query, new StrictFilterDelegate( false, 50000.00, new FilterConfig() ) );
            if ( filterParameters.containsKey( PHRASE_KEY ) ) {
                // Add the ~ to make it a fuzzy term search
                return filterParameters.get( PHRASE_KEY ) + "~";
            }
        } catch ( UnsupportedQueryException uqe ) {
            LOGGER.debug( "Query did not contain any contextual criteria (search phrases), cannot perform re-relevance on this query." );
        }

        return null;
    }

    /**
     * Creates a new result with an updated score.
     *
     * @param origResult
     *            Original result that contains an older score.
     * @param newScore
     *            New score to update the result with.
     * @return Result with updated score.
     */
    protected Result updateResult( Result origResult, float newScore ) {
        ResultImpl result = new ResultImpl( origResult.getMetacard() );
        result.setRelevanceScore( (double) newScore );
        result.setDistanceInMeters( origResult.getDistanceInMeters() );
        return result;
    }

}
