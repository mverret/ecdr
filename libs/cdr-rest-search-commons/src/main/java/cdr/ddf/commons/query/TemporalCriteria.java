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
package cdr.ddf.commons.query;

import java.util.Date;

public class TemporalCriteria {

    private Date startDate;
    private Date endDate;
    private String dateType;

    public TemporalCriteria( Date startDate, Date endDate, String dateType ) {
        if ( dateType == null ) {
            throw new IllegalArgumentException( "Null is not valid for dateType" );
        }
        if ( startDate == null && endDate == null ) {
            throw new IllegalArgumentException( "startDate and endDate parameters cannot both be null" );
        }
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateType = dateType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getDateType() {
        return dateType;
    }

}
