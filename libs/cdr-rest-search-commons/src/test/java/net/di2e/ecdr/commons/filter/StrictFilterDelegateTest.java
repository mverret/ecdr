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
package net.di2e.ecdr.commons.filter;

import ddf.catalog.filter.proxy.adapter.GeotoolsFilterAdapterImpl;
import ddf.catalog.operation.impl.QueryImpl;
import net.di2e.ecdr.commons.filter.config.FilterConfig;
import net.di2e.ecdr.commons.util.SearchConstants;
import org.geotools.filter.text.cql2.CQL;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StrictFilterDelegateTest {

    @Test
    public void testFilterDelegate() throws Exception {
        GeotoolsFilterAdapterImpl filterAdapter = new GeotoolsFilterAdapterImpl();
        QueryImpl query = new QueryImpl( CQL.toFilter("q like 'test' AND (created before 2014-05-05T00:00:00 AND created after 2014-06-05T00:00:00) OR created after 2014-07-05T00:00:00") );
        Map<String, String> filterParameters = filterAdapter.adapt( query, new StrictFilterDelegate( false, 50000.00, new FilterConfig() ) );
        assertEquals("test", filterParameters.get( SearchConstants.KEYWORD_PARAMETER ));
    }

}
