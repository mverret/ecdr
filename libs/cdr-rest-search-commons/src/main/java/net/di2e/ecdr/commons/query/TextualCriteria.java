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
package net.di2e.ecdr.commons.query;


public class TextualCriteria {

    private String keywords;
    private boolean isCaseSensitive;
    private boolean fuzzy;

    public TextualCriteria( String keywords, boolean isCaseSensitive, boolean fuzzy ) {
        if ( keywords == null || keywords.trim().isEmpty() ) {
            throw new IllegalArgumentException( "Null or empty String is not valid for keywords[" + keywords + "]" );
        }
        this.keywords = keywords;
        this.isCaseSensitive = isCaseSensitive;
        this.fuzzy = fuzzy;
    }

    public String getKeywords() {
        return keywords;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public boolean isFuzzy() {
        return fuzzy;
    }

    public void setCaseSensitive( boolean caseSensitive ) {
        this.isCaseSensitive = caseSensitive;
    }

}
