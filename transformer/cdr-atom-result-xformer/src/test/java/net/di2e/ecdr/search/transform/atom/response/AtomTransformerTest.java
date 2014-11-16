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
package net.di2e.ecdr.search.transform.atom.response;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.HashMap;

import javax.activation.MimeType;

import net.di2e.ecdr.search.transform.atom.AtomTransformer;
import net.di2e.ecdr.search.transform.atom.geo.GeoHelper;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.codice.ddf.configuration.impl.ConfigurationWatcherImpl;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;

import ddf.action.ActionProvider;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.impl.MetacardImpl;

/**
 * Test out the atom transformer
 */
public class AtomTransformerTest extends net.di2e.ecdr.search.transform.atom.response.AtomTest {

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

    private Entry performTransform(String locationWKT, boolean useGMLEncoding) throws Exception {
        ConfigurationWatcherImpl configurationWatcher = new ConfigurationWatcherImpl();
        ActionProvider viewMetacardProvider = mock(ActionProvider.class);
        ActionProvider resourceProvider = mock(ActionProvider.class);
        ActionProvider thumbnailProvider = mock(ActionProvider.class);
        MimeType thumbnailMime = new MimeType();
        MimeType viewMime = new MimeType();
        AtomTransformer transformer = new AtomTransformer( null, configurationWatcher, viewMetacardProvider, resourceProvider, thumbnailProvider, thumbnailMime, viewMime );
        MetacardImpl metacard = new MetacardImpl();
        metacard.setLocation(locationWKT);
        transformer.setUseGMLEncoding(useGMLEncoding);
        BinaryContent content = transformer.transform(metacard, new HashMap<String, Serializable>());
        // parse into abdera
        Abdera abdera = new Abdera();
        Document<Entry> doc = abdera.getParser().parse(content.getInputStream());
        return doc.getRoot();

    }

    private void compareXML(String expectedXML, Element atomElement) throws Exception {
        Diff diff = XMLUnit.compareXML(expectedXML, atomElement.toString());
        assertTrue(diff.similar());
    }

}
