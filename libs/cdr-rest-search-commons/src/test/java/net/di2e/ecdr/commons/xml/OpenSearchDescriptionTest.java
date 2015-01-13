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
package net.di2e.ecdr.commons.xml;

import ddf.catalog.CatalogFramework;
import net.di2e.ecdr.commons.xml.fs.Link;
import net.di2e.ecdr.commons.xml.fs.Rel;
import net.di2e.ecdr.commons.xml.fs.SourceDescription;
import net.di2e.ecdr.commons.xml.osd.OpenSearchDescription;
import net.di2e.ecdr.commons.xml.osd.Query;
import net.di2e.ecdr.commons.xml.osd.SyndicationRight;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class OpenSearchDescriptionTest {

    /**
     * Verify that JAXB object can be created and marshalled without exceptions.
     * @throws Exception
     */
    @Test
    public void testCreation() throws Exception {
        OpenSearchDescription osd = new OpenSearchDescription();
        CatalogFramework framework = null;
        osd.setShortName( "ECDR Opensearch" );
        osd.setDescription( "Opensearch endpoint that conforms to the Enterprise CDR specifications." );
        osd.setTags( "ecdr opensearch cdr ddf" );
        osd.setContact( "test@example.org" );
        osd.setDeveloper( "ECDR" );
        Query query = new Query();
        query.setRole( "example" );
        query.setSearchTerms( "test" );
        osd.getQuery().add( query );
        osd.setSyndicationRight( SyndicationRight.OPEN );
        osd.setAdultContent( false );
        osd.getLanguage().add( Locale.US.getLanguage() );
        osd.getInputEncoding().add( StandardCharsets.UTF_8.name() );
        osd.getOutputEncoding().add( StandardCharsets.UTF_8.name() );

        // federation type
        SourceDescription description = new SourceDescription();
        description.setShortName( "site1" );
        description.setDescription( "This is a federated site." );
        Link link = new Link();
        link.setHref( "http://example.com/description.xml" );
        link.setRel( Rel.SELF );
        link.setType( "text/xml" );
        description.setLink( link );

        osd.getAny().add( description );

        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance( OpenSearchDescription.class, SourceDescription.class );
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        marshaller.marshal(osd, writer);
    }


}
