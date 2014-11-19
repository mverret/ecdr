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
package net.di2e.ecdr.plugin.relevance;

import ddf.catalog.data.Result;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.filter.FilterDelegate;
import ddf.catalog.filter.impl.PropertyNameImpl;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.operation.impl.QueryResponseImpl;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortOrder;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RelevancePluginTest {

    /**
     * Tests out that the scores of results are changed after the plugin is run.
     * @throws Exception
     */
    @Test
    public void testReindexScores() throws Exception {
        //TODO mock up a full query response to test
    }

    /**
     * Verifies that the plugin is skipped when the query has temporal sorting
     * @throws Exception
     */
    @Test
    public void testTemporalSort() throws Exception {
        Filter filter = mock(Filter.class);
        QueryImpl query = new QueryImpl(filter);
        query.setSortBy( new SortByImpl( new PropertyNameImpl( Result.TEMPORAL ), SortOrder.ASCENDING ) );
        QueryRequestImpl queryRequest = new QueryRequestImpl(query);
        QueryResponseImpl queryResponse = new QueryResponseImpl(queryRequest);
        FilterAdapter filterAdapter = mock(FilterAdapter.class);
        RelevancePlugin plugin = new RelevancePlugin( filterAdapter );
        plugin.process( queryResponse );
        // check that filteradapter was NOT called
        verify(filterAdapter, times(0)).adapt( any( Filter.class ), any( FilterDelegate.class ) );
    }

    /**
     * Verifies that the plugin is skipped when the query has distance sorting
     * @throws Exception
     */
    @Test
    public void testSpatialSort() throws Exception {
        Filter filter = mock(Filter.class);
        QueryImpl query = new QueryImpl(filter);
        query.setSortBy( new SortByImpl( new PropertyNameImpl( Result.DISTANCE ), SortOrder.ASCENDING ) );
        QueryRequestImpl queryRequest = new QueryRequestImpl(query);
        QueryResponseImpl queryResponse = new QueryResponseImpl(queryRequest);
        FilterAdapter filterAdapter = mock(FilterAdapter.class);
        RelevancePlugin plugin = new RelevancePlugin( filterAdapter );
        plugin.process( queryResponse );
        // check that filteradapter was NOT called
        verify(filterAdapter, times(0)).adapt( any( Filter.class ), any( FilterDelegate.class ) );
    }



}
