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
package net.di2e.ecdr.commons.query.rest;

import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.source.UnsupportedQueryException;
import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.query.rest.parsers.BrokerQueryParser;

import net.di2e.ecdr.commons.sort.SortTypeConfiguration;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class CDRQueryImplCQLTest {

    private static final String FIELD_CQL = "created <> '2014-01-03'";
    private static final String LIKE_CQL = "title like 'test'";
    private static final String BOOLEAN_CQL = "title = 'test' and author = 'example'";
    private static final String PARENTHESIS_CQL = "country = 'England' and (city = 'London' and created before 2014-05-05T00:00:00) or (city = 'Cambridge' and created between '2014-01-03' and '2014-03-03')";
    private static final String BAD_CQL = "BAD!!!CQL";

    /**
     * Tests that a filter is created that targets a specific field
     *
     * @throws UnsupportedQueryException
     */
    @Test
    public void testField() throws UnsupportedQueryException {
        testQuery( FIELD_CQL );
    }

    /**
     * Tests that a filter is created for a targeted 'like' query
     *
     * @throws UnsupportedQueryException
     */
    @Test
    public void testLike() throws UnsupportedQueryException {
        testQuery( LIKE_CQL );
    }

    /**
     * Tests that a filter is created with a boolean predicate.
     *
     * @throws UnsupportedQueryException
     */
    @Test
    public void testBoolean() throws UnsupportedQueryException {
        testQuery( BOOLEAN_CQL );
    }

    /**
     * Tests that a filter is created with a boolean predicate.
     *
     * @throws UnsupportedQueryException
     */
    @Test(expected = UnsupportedQueryException.class)
    public void testBad() throws UnsupportedQueryException {
        testQuery( BAD_CQL );
    }

    /**
     * Tests that a filter is created from a complex statement that has parenthesis.
     *
     * @throws UnsupportedQueryException
     */
    @Test
    public void testParenthesis() throws UnsupportedQueryException {
        testQuery( PARENTHESIS_CQL );
    }

    private void testQuery( String CQL ) throws UnsupportedQueryException {
        FilterBuilder filterBuilder = mock( FilterBuilder.class );
        BrokerQueryParser parser = new BrokerQueryParser( Collections.<SortTypeConfiguration>emptyList() );
        MultivaluedMap<String, String> queryParameters = new MetadataMap<>();
        queryParameters.add( SearchConstants.QUERYLANGUAGE_PARAMETER, SearchConstants.CDR_CQL_QUERY_LANGUAGE );
        queryParameters.add( SearchConstants.KEYWORD_PARAMETER, CQL );
        new CDRQueryImpl( filterBuilder, queryParameters, parser, true, "test" );
    }

}
