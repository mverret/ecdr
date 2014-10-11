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
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.QueryResponseTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import org.openexi.proc.common.AlignmentType;
import org.openexi.proc.common.EXIOptionsException;
import org.openexi.proc.common.GrammarOptions;
import org.openexi.proc.grammars.GrammarCache;
import org.openexi.sax.Transmogrifier;
import org.openexi.sax.TransmogrifierException;

public class ExiTransformer implements QueryResponseTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExiTransformer.class);

    private static final String XML_FORMAT_KEY = "XML_FORMAT";

    private CatalogFramework framework;

    public ExiTransformer (CatalogFramework framework) {
        this.framework = framework;
    }

    @Override
    public BinaryContent transform(SourceResponse upstreamResponse, Map<String, Serializable> arguments) throws CatalogTransformerException {
        InputStream xmlStream = null;

        String xmlType = null;

        if (arguments.get(XML_FORMAT_KEY) != null) {
            xmlType = arguments.get(XML_FORMAT_KEY).toString();
        }

        if (StringUtils.isNotBlank(xmlType)) {
            try {
                // convert to XML using input transformer
                BinaryContent xmlContent = framework.transform(upstreamResponse, xmlType, arguments);
                xmlStream = xmlContent.getInputStream();

                // convert to EXI data
                byte[] exiBytes = encodeAsExi(xmlStream);

                return new BinaryContentImpl(new ByteArrayInputStream(exiBytes), xmlContent.getMimeType());
            } finally {
                IOUtils.closeQuietly(xmlStream);
            }
        } else {
            throw new CatalogTransformerException("No xml format defined in the arguments. Add format string to arguments with key " + XML_FORMAT_KEY);
        }
    }

    private byte[] encodeAsExi(InputStream xmlStream) throws CatalogTransformerException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            Transmogrifier trans = new Transmogrifier();
            trans.setAlignmentType(AlignmentType.bitPacked);
            GrammarCache grammarCache = new GrammarCache(null, GrammarOptions.DEFAULT_OPTIONS);
            trans.setGrammarCache(grammarCache);
            trans.setOutputStream(baos);
            LOGGER.debug("Performing EXI encoding.");
            trans.encode(new InputSource(xmlStream));
            LOGGER.debug("EXI encoding complete.");
            return baos.toByteArray();
        } catch (EXIOptionsException exp) {
            throw new CatalogTransformerException("Could not transform into EXI encoding due to incorrect options.", exp);
        } catch (TransmogrifierException te) {
            throw new CatalogTransformerException("Could not transform into EXI encoding due to error during encoding", te);
        } catch (IOException ioe) {
            throw new CatalogTransformerException("Could not transform into EXI encoding due to error during encoding (IO)", ioe);
        } finally {
            IOUtils.closeQuietly(baos);
        }
    }
}
