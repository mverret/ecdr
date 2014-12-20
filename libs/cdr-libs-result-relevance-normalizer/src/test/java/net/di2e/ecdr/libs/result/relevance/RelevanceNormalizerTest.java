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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.di2e.ecdr.libs.result.relevance.RelevanceNormalizer;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Result;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.filter.FilterDelegate;
import ddf.catalog.filter.impl.PropertyNameImpl;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.operation.impl.QueryResponseImpl;

public class RelevanceNormalizerTest {

    private static final String SEARCH_KEY = "q";
    private static final String EXAMPLE_PHRASE = "snow";
    private static final String ID_1 = "metacard1";
    private static final String ID_2 = "metacard2";
    private static final String ID_3 = "metacard3";
    private static final String XML_FILE1 = "example1.xml";
    private static final String XML_FILE2 = "example2.xml";
    private static final String XML_FILE3 = "example3.xml";
    private static final double ORIG_SCORE1 = .225;
    private static final double ORIG_SCORE2 = .556;
    private static final double ORIG_SCORE3 = .121;

    private static final Logger LOGGER = LoggerFactory.getLogger( RelevanceNormalizerTest.class );

    /**
     * Tests out that the scores of results are changed after the plugin is run.
     *
     * @throws Exception
     */
    @Test
    public void testReindexScores() throws Exception {
        Result result1 = createResult( ID_1, XML_FILE1, ORIG_SCORE1 );
        Result result2 = createResult( ID_2, XML_FILE2, ORIG_SCORE2 );
        Result result3 = createResult( ID_3, XML_FILE3, ORIG_SCORE3 );
        List<Result> results = Arrays.asList( result1, result2, result3 );
        QueryResponse queryResponse = createResponse( new PropertyNameImpl( Result.RELEVANCE ), SortOrder.DESCENDING, results );
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put( SEARCH_KEY, EXAMPLE_PHRASE );
        FilterAdapter filterAdapter = createAdapter( paramMap );
        RelevanceNormalizer resultNormalizer = new RelevanceNormalizer( filterAdapter );
        List<Result> returnResponse = resultNormalizer.normalize( queryResponse.getResults(), queryResponse.getRequest().getQuery() );
        assertNotSame( queryResponse.getResults(), returnResponse );
        for ( Result curResult : returnResponse ) {
            if ( ID_1.equals( curResult.getMetacard().getId() ) ) {
                compareResults( result1, curResult );
            } else if ( ID_2.equals( curResult.getMetacard().getId() ) ) {
                compareResults( result2, curResult );
            } else if (ID_3.equals( curResult.getMetacard().getId() ) ) {
                compareResults( result3, curResult );
                // should not have matched on search
                assertEquals(Double.valueOf( 0 ), curResult.getRelevanceScore());
            } else {
                fail( "metacard IDs do not match original metacards." );
            }
        }

    }

    /**
     * Verifies that the plugin is skipped when search phrase is not present
     *
     * @throws Exception
     */
    @Test
    public void testNoPhraseSort() throws Exception {
        QueryResponse queryResponse = createResponse( new PropertyNameImpl( Result.RELEVANCE ), SortOrder.DESCENDING, Collections.<Result>emptyList() );
        FilterAdapter filterAdapter = createAdapter( Collections.<String, String>emptyMap() );
        RelevanceNormalizer resultNormalizer = new RelevanceNormalizer( filterAdapter );
        List<Result> returnResponse = resultNormalizer.normalize( queryResponse.getResults(), queryResponse.getRequest().getQuery() );
        assertEquals( queryResponse.getResults(), returnResponse );
    }

    /**
     * Verifies that the plugin is skipped when the query has temporal sorting
     *
     * @throws Exception
     */
    @Test
    public void testTemporalSort() throws Exception {
        QueryResponse queryResponse = createResponse( new PropertyNameImpl( Result.TEMPORAL ), SortOrder.ASCENDING, Collections.<Result>emptyList() );
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put( SEARCH_KEY, EXAMPLE_PHRASE );
        FilterAdapter filterAdapter = createAdapter( paramMap );
        RelevanceNormalizer resultNormalizer = new RelevanceNormalizer( filterAdapter );
        List<Result> returnResponse = resultNormalizer.normalize( queryResponse.getResults(), queryResponse.getRequest().getQuery() );
        assertEquals( queryResponse.getResults(), returnResponse );
    }

    /**
     * Verifies that the plugin is skipped when the query has distance sorting
     *
     * @throws Exception
     */
    @Test
    public void testSpatialSort() throws Exception {
        QueryResponse queryResponse = createResponse( new PropertyNameImpl( Result.DISTANCE ), SortOrder.ASCENDING, Collections.<Result>emptyList() );
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put( SEARCH_KEY, EXAMPLE_PHRASE );
        FilterAdapter filterAdapter = createAdapter( paramMap );
        RelevanceNormalizer resultNormalizer = new RelevanceNormalizer( filterAdapter );
        List<Result> returnResponse = resultNormalizer.normalize( queryResponse.getResults(), queryResponse.getRequest().getQuery() );
        assertEquals( queryResponse.getResults(), returnResponse );
    }

    /**
     * Verifies that the plugin is skipped when there is no contextual criteria (search phrase).
     *
     * @throws Exception
     */
    @Test
    public void testNoSearchPhrase() throws Exception {
        QueryResponse queryResponse = createResponse( new PropertyNameImpl( Result.DISTANCE ), SortOrder.ASCENDING, Collections.<Result>emptyList() );
        FilterAdapter filterAdapter = createAdapter( Collections.<String, String>emptyMap() );
        RelevanceNormalizer resultNormalizer = new RelevanceNormalizer( filterAdapter );
        List<Result> returnResponse = resultNormalizer.normalize( queryResponse.getResults(), queryResponse.getRequest().getQuery() );
        assertEquals( queryResponse.getResults(), returnResponse );
    }

    /**
     * Creates a response with the given sortproperty and sortorder.
     *
     * @param sortProperty
     * @param sortOrder
     * @param resultList
     * @return
     * @throws Exception
     */
    private QueryResponse createResponse( PropertyName sortProperty, SortOrder sortOrder, List<Result> resultList ) throws Exception {
        Filter filter = mock( Filter.class );
        QueryImpl query = new QueryImpl( filter );
        query.setSortBy( new SortByImpl( sortProperty, sortOrder ) );
        QueryRequestImpl queryRequest = new QueryRequestImpl( query );
        QueryResponseImpl queryResponse = new QueryResponseImpl( queryRequest, resultList, true, resultList.size() );
        LOGGER.debug( "Created response." );
        return queryResponse;
    }

    /**
     * Create an adapter that returns the given map when adapt is called.
     *
     * @param adapterMap
     * @return
     * @throws Exception
     */
    private FilterAdapter createAdapter( Map<String, String> adapterMap ) throws Exception {
        FilterAdapter filterAdapter = mock( FilterAdapter.class );
        when( filterAdapter.adapt( any( Filter.class ), any( FilterDelegate.class ) ) ).thenReturn( adapterMap );
        return filterAdapter;
    }

    private Result createResult( String id, String xmlFile, double origScore ) throws IOException {
        MetacardImpl metacard = new MetacardImpl();
        metacard.setId( id );
        metacard.setMetadata( IOUtils.toString( getClass().getResourceAsStream( "/" + xmlFile ) ) );
        ResultImpl result = new ResultImpl( metacard );
        result.setRelevanceScore( origScore );
        return result;
    }

    /**
     * Compares two results and asserts that only the relevance was changed.
     *
     * @param origResult
     * @param newResult
     */
    private void compareResults( Result origResult, Result newResult ) {
        assertEquals( origResult.getMetacard(), newResult.getMetacard() );
        assertNotSame( origResult.getRelevanceScore(), newResult.getRelevanceScore() );
    }

}
