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
package net.di2e.ecdr.libs.result.relevance;

import org.apache.commons.lang.StringUtils;
import org.codehaus.stax2.XMLInputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

public final class TextParser {

    private static final Logger LOGGER = LoggerFactory.getLogger( TextParser.class );

    private static XMLInputFactory xmlInputFactory = null;

    static {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    TextParser.class.getClassLoader());

            xmlInputFactory = XMLInputFactory2.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                    Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
                    Boolean.FALSE);
            xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
            xmlInputFactory.setProperty( XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    private TextParser() {

    }

    /**
     * Given xml as a string, this method will parse out element text and CDATA text. It separates
     * each by one space character.
     *
     * @param xmlData
     *            XML as a {@code String}
     * @return parsed CDATA and element text
     */
    protected static String parseTextFrom(String xmlData) {

        StringBuilder builder = new StringBuilder();

        XMLStreamReader xmlStreamReader;

        try {
            // xml parser does not handle leading whitespace
            xmlStreamReader = xmlInputFactory
                    .createXMLStreamReader(new StringReader(xmlData));

            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();

                if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {

                    String text = xmlStreamReader.getText();

                    if ( StringUtils.isNotBlank( text )) {
                        builder.append(" " + text.trim());
                    }

                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {

                        String text = xmlStreamReader.getAttributeValue(i);

                        if (StringUtils.isNotBlank(text)) {
                            builder.append(" " + text.trim());
                        }
                    }
                }
            }
        } catch (XMLStreamException e1) {
            LOGGER.warn(
                    "Failure occurred in parsing the xml data (" + xmlData + "). No data has been stored or indexed.",
                    e1);
        }

        return builder.toString();
    }

}
