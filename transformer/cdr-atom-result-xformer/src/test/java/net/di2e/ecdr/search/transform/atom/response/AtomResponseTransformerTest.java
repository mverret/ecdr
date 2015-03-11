/**
 * Copyright (C) 2014 Cohesive Integrations, LLC (info@cohesiveintegrations.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.di2e.ecdr.search.transform.atom.response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import net.di2e.ecdr.commons.filter.config.FilterConfig;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import ddf.catalog.data.Result;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.SourceResponse;

/**
 * Tests that the atom-based search response transformer returns proper atom content.
 */
public class AtomResponseTransformerTest extends net.di2e.ecdr.search.transform.atom.response.AtomTest {

    @Test
    public void testBoxTransform() throws Exception {
        SourceResponse response = getTransformResponse( SIMPLE_BOX );
        validateWKT( BOX_WKT, response );
    }

    @Test
    public void testLineTransform() throws Exception {
        SourceResponse response = getTransformResponse( SIMPLE_LINE );
        validateWKT( LINE_WKT, response );
    }

    @Test
    public void testPointTransform() throws Exception {
        SourceResponse response = getTransformResponse( SIMPLE_POINT );
        validateWKT( POINT_WKT, response );
    }

    @Test
    public void testPolygonTransform() throws Exception {
        SourceResponse response = getTransformResponse( SIMPLE_POLYGON );
        validateWKT( POLYGON_WKT, response );
    }

    @Test
    public void testGMLBoxTransform() throws Exception {
        SourceResponse response = getTransformResponse( GML_BOX );
        validateWKT( BOX_WKT, response );
    }

    @Test
    public void testGMLPointTransform() throws Exception {
        SourceResponse response = getTransformResponse( GML_POINT );
        validateWKT( POINT_WKT, response );
    }

    @Test
    public void testGMLLineTransform() throws Exception {
        SourceResponse response = getTransformResponse( GML_LINE );
        validateWKT( LINE_WKT, response );
    }

    @Test
    public void testGMLPolygonTransform() throws Exception {
        SourceResponse response = getTransformResponse( GML_POLYGON );
        validateWKT( POLYGON_WKT, response );
    }

    @Test
    public void testMultiPointTransform() throws Exception {
        SourceResponse response = getTransformResponse( MULTIPOINT );
        validateWKT( MULTIPOINT_WKT, response );
    }

    @Test
    public void testMultiLineStringTransform() throws Exception {
        SourceResponse response = getTransformResponse( MULTILINESTRING );
        validateWKT( MULTILINESTRING_WKT, response );
    }

    @Test
    public void testMultiPolygonTransform() throws Exception {
        SourceResponse response = getTransformResponse( MULTIPOLYGON );
        validateWKT( MULTIPOLYGON_WKT, response );
    }

    @Test
    public void testMultiBoxTransform() throws Exception {
        SourceResponse response = getTransformResponse( MULTIBOX );
        validateWKT( MULTIBOX_WKT, response );
    }

    @Test
    public void testGeometryCollectionTransform() throws Exception {
        SourceResponse response = getTransformResponse( GEOMETRYCOLLECTION );
        validateWKT( GEOMETRYCOLLECTION_WKT, response );
    }

    @Test
    public void testInvalidEntry() throws Exception {
        FilterConfig config = mock( FilterConfig.class );
        QueryRequest request = mock( QueryRequest.class );
        AtomResponseTransformer transformer = new AtomResponseTransformer( config );
        String atomXML = IOUtils.toString( getClass().getResourceAsStream( ATOM_INVALID_FILE ) );
        SourceResponse response = transformer.processSearchResponse( IOUtils.toInputStream( atomXML ), request, SITE_NAME );
        assertEquals( 0, response.getHits() );
        assertEquals( 0, response.getResults().size() );
    }

    private SourceResponse getTransformResponse( final String LOCATION_XML ) throws Exception {
        FilterConfig config = mock( FilterConfig.class );
        QueryRequest request = mock( QueryRequest.class );
        AtomResponseTransformer transformer = new AtomResponseTransformer( config );
        String atomXML = IOUtils.toString( getClass().getResourceAsStream( ATOM_TEMPLATE_FILE ) );
        atomXML = StringUtils.replace( atomXML, LOCATION_MARKER, LOCATION_XML );
        return transformer.processSearchResponse( IOUtils.toInputStream( atomXML ), request, SITE_NAME );
    }

    private void validateWKT( String expectedWKT, SourceResponse response ) {
        List<Result> results = response.getResults();
        assertEquals( 1, results.size() );
        Result result = results.iterator().next();
        assertEquals( expectedWKT, result.getMetacard().getLocation() );
    }

}