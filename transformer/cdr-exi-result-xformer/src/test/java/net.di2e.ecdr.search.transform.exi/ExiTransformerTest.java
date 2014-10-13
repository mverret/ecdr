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
package net.di2e.ecdr.search.transform.exi;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.data.impl.BinaryContentImpl;
import ddf.catalog.operation.SourceResponse;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openexi.proc.common.GrammarOptions;
import org.openexi.proc.grammars.GrammarCache;
import org.openexi.sax.EXIReader;
import org.xml.sax.InputSource;

import javax.activation.MimeType;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExiTransformerTest {

    private static final String ATOM_MIME = "application/atom+xml";

    private static final String TEST_FILE = "/atom-example.xml";

    @BeforeClass
    public static void setUp() {
        XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
        XMLUnit.setTransformerFactory("org.apache.xalan.processor.TransformerFactoryImpl");
    }


    @Test
    public void testTransformer() throws Exception {

        CatalogFramework framework = mock(CatalogFramework.class);
        InputStream xmlStream = getClass().getResourceAsStream(TEST_FILE);
        BinaryContent content = new BinaryContentImpl(xmlStream, new MimeType(ATOM_MIME));
        when(framework.transform(any(SourceResponse.class), any(String.class), any(Map.class))).thenReturn(content);
        ExiTransformer transformer = new ExiTransformer(framework);
        SourceResponse response = mock(SourceResponse.class);
        HashMap<String, Serializable> arguments = new HashMap<String, Serializable>();
        arguments.put("XML_FORMAT", "atom");

        BinaryContent exiContent = transformer.transform(response, arguments);

        StringWriter stringWriter = new StringWriter();

        GrammarCache grammarCache;

        SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        TransformerHandler transformerHandler = saxTransformerFactory.newTransformerHandler();

        EXIReader reader = new EXIReader();

        grammarCache = new GrammarCache(null, GrammarOptions.DEFAULT_OPTIONS);

        reader.setGrammarCache(grammarCache);

        transformerHandler.setResult(new StreamResult(stringWriter));

        reader.setContentHandler(transformerHandler);

        reader.parse(new InputSource(exiContent.getInputStream()));
        XMLUnit.setNormalize(true);
        XMLUnit.setNormalizeWhitespace(true);
        Diff diff = XMLUnit.compareXML(IOUtils.toString(getClass().getResourceAsStream(TEST_FILE)), stringWriter.getBuffer().toString());
        assertTrue("The XML input file (" + TEST_FILE + ") did not match the EXI-decoded output", diff.similar());
    }

}