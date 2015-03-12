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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimeType;

import net.di2e.ecdr.commons.constants.BrokerConstants;
import net.di2e.ecdr.commons.constants.SearchConstants;
import net.di2e.ecdr.commons.filter.config.FilterConfig;
import net.di2e.ecdr.search.transform.atom.AtomTransformer;
import net.di2e.ecdr.search.transform.atom.geo.GeoHelper;
import net.di2e.ecdr.search.transform.atom.security.FeedSecurityConfiguration;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.geotools.filter.text.cql2.CQL;
import org.junit.BeforeClass;
import org.junit.Test;

import ddf.action.ActionProvider;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.ProcessingDetails;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.impl.ProcessingDetailsImpl;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.operation.impl.QueryResponseImpl;
import ddf.catalog.source.UnsupportedQueryException;

/**
 * Test out the atom transformer
 */
public class AtomTransformerTest extends net.di2e.ecdr.search.transform.atom.response.AtomTest {

    private static final String RESPONSE_FILE = "/exampleResponse.xml";

    @BeforeClass
    public static void setUp() {
        XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
        XMLUnit.setTransformerFactory("org.apache.xalan.processor.TransformerFactoryImpl");
        XMLUnit.setNormalize(true);
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void testPointTransform() throws Exception {
        // use gml georss (default)
        Entry entry = performTransform(POINT_WKT, true);
        compareXML(GML_POINT, entry.getExtension(GeoHelper.QNAME_WHERE));

        // use simple georss
        entry = performTransform(POINT_WKT, false);
        compareXML(SIMPLE_POINT, entry.getExtension(GeoHelper.QNAME_SIMPLE_POINT));
    }

    @Test
    public void testLineTransform() throws Exception {
        // use gml georss (default)
        Entry entry = performTransform(LINE_WKT, true);
        compareXML(GML_LINE, entry.getExtension(GeoHelper.QNAME_WHERE));

        // use simple georss
        entry = performTransform(LINE_WKT, false);
        compareXML(SIMPLE_LINE, entry.getExtension(GeoHelper.QNAME_SIMPLE_LINE));
    }

    @Test
    public void testBoxTransform() throws Exception {
        // use gml georss (default)
        Entry entry = performTransform(BOX_WKT, true);
        compareXML(GML_BOX, entry.getExtension(GeoHelper.QNAME_WHERE));

        // use simple georss
        entry = performTransform(BOX_WKT, false);
        compareXML(SIMPLE_BOX, entry.getExtension(GeoHelper.QNAME_SIMPLE_BOX));
    }

    @Test
    public void testPolygonTransform() throws Exception {
        // use gml georss (default)
        Entry entry = performTransform(POLYGON_WKT, true);
        compareXML(GML_POLYGON, entry.getExtension(GeoHelper.QNAME_WHERE));

        // use simple georss
        entry = performTransform(POLYGON_WKT, false);
        compareXML(SIMPLE_POLYGON, entry.getExtension(GeoHelper.QNAME_SIMPLE_POLYGON));
    }

    @Test
    public void testResponseTransform() throws Exception {
        String sourceName = "Example";
        String sourceName2 = "Bad Example";
        AtomResponseTransformer responseTransformer = new AtomResponseTransformer( new FilterConfig() );
        QueryRequest request = new QueryRequestImpl( new QueryImpl( CQL.toFilter( "title like 'test'" ) ) );
        SourceResponse sourceResponse = responseTransformer.processSearchResponse( getClass().getResourceAsStream( RESPONSE_FILE ), request, sourceName );
        QueryResponseImpl queryResponse = new QueryResponseImpl( sourceResponse, sourceName );
        Map<String, Serializable> queryProperties = new HashMap<>();
        queryProperties.put( "site-list", new ArrayList<>( Arrays.asList( sourceName, sourceName2 ) ) );
        queryProperties.put( sourceName, getSiteMap() );
        queryProperties.put( sourceName2, getSiteMap() );
        queryResponse.setProperties( queryProperties );
        Set<ProcessingDetails> details = new HashSet<>();
        ProcessingDetailsImpl goodSite = new ProcessingDetailsImpl();
        goodSite.setSourceId( sourceName );
        details.add( goodSite );
        ProcessingDetailsImpl badSite = new ProcessingDetailsImpl();
        badSite.setSourceId( sourceName2 );
        badSite.setException( new UnsupportedQueryException( "Unsupported test query" ) );
        badSite.setWarnings( Arrays.asList( "Example Warning" ) );
        details.add( badSite );
        queryResponse.setProcessingDetails( details );
        AtomTransformer transformer = createTransformer();
        Map<String, Serializable> properties = new HashMap<>();
        properties.put( SearchConstants.STATUS_PARAMETER, Boolean.TRUE );
        properties.put( BrokerConstants.PATH_PARAMETER, "/path" );
        transformer.transform( queryResponse, properties );
    }

    @Test
    public void testResponseTransformNoDetails() throws Exception {
        String sourceName = "Example";
        AtomResponseTransformer responseTransformer = new AtomResponseTransformer( new FilterConfig() );
        QueryRequest request = new QueryRequestImpl( new QueryImpl( CQL.toFilter( "title like 'test'" ) ) );
        SourceResponse sourceResponse = responseTransformer.processSearchResponse( getClass().getResourceAsStream( RESPONSE_FILE ), request, sourceName );
        QueryResponseImpl queryResponse = new QueryResponseImpl( sourceResponse, sourceName );
        Map<String, Serializable> queryProperties = new HashMap<>();
        queryProperties.put( "site-list", new ArrayList<>( Arrays.asList( sourceName ) ) );
        queryProperties.put( sourceName, getSiteMap() );
        queryResponse.setProperties( queryProperties );
        AtomTransformer transformer = createTransformer();
        Map<String, Serializable> properties = new HashMap<>();
        properties.put( SearchConstants.STATUS_PARAMETER, Boolean.TRUE );
        properties.put( BrokerConstants.PATH_PARAMETER, "/path" );
        transformer.transform( queryResponse, properties );
    }

    @Test
    public void testLocalResponseTransform() throws Exception {
        String sourceName = "Example";
        AtomResponseTransformer responseTransformer = new AtomResponseTransformer( new FilterConfig() );
        QueryRequest request = new QueryRequestImpl( new QueryImpl( CQL.toFilter( "title like 'test'" ) ) );
        SourceResponse sourceResponse = responseTransformer.processSearchResponse( getClass().getResourceAsStream( RESPONSE_FILE ), request, sourceName );
        QueryResponseImpl queryResponse = new QueryResponseImpl( sourceResponse, sourceName );
        Map<String, Serializable> queryProperties = new HashMap<>();
        queryProperties.put( sourceName, getSiteMap() );
        queryResponse.setProperties( queryProperties );
        AtomTransformer transformer = createTransformer();
        Map<String, Serializable> properties = new HashMap<>();
        properties.put( SearchConstants.STATUS_PARAMETER, Boolean.TRUE );
        properties.put( BrokerConstants.PATH_PARAMETER, "/path" );
        transformer.transform( queryResponse, properties );
    }

    private Entry performTransform(String locationWKT, boolean useGMLEncoding) throws Exception {

        MetacardImpl metacard = new MetacardImpl();
        metacard.setLocation(locationWKT);
        AtomTransformer transformer = createTransformer();
        transformer.setUseGMLEncoding(useGMLEncoding);
        BinaryContent content = transformer.transform(metacard, new HashMap<String, Serializable>());
        // parse into abdera
        Abdera abdera = new Abdera();
        Document<Entry> doc = abdera.getParser().parse(content.getInputStream());
        return doc.getRoot();

    }

    private AtomTransformer createTransformer() throws Exception {
        ConfigurationWatcherImpl configurationWatcher = new ConfigurationWatcherImpl();
        ActionProvider viewMetacardProvider = mock(ActionProvider.class);
        ActionProvider metadataProvider = mock(ActionProvider.class);
        ActionProvider resourceProvider = mock(ActionProvider.class);
        ActionProvider thumbnailProvider = mock(ActionProvider.class);
        List<FeedSecurityConfiguration> securityConfig = Collections.emptyList();
        MimeType thumbnailMime = new MimeType("image/jpeg");
        MimeType viewMime = new MimeType("text/html");
        return new AtomTransformer( configurationWatcher, viewMetacardProvider, metadataProvider, resourceProvider, thumbnailProvider, thumbnailMime, viewMime, securityConfig );
    }

    private void compareXML(String expectedXML, Element atomElement) throws Exception {
        Diff diff = XMLUnit.compareXML(expectedXML, atomElement.toString());
        assertTrue(diff.similar());
    }

    private HashMap<String, Serializable> getSiteMap() {
        HashMap<String, Serializable> siteMap = new HashMap<String, Serializable>();
        siteMap.put( "elapsed-time", new Long(1000) );
        siteMap.put( "total-hits", new Long(20) );
        siteMap.put( "total-results-returned", 20 );
        return siteMap;
    }

}
