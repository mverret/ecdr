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
package net.di2e.ecdr.commons.xml.osd;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SyndicationRight.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SyndicationRight">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="open"/>
 *     &lt;enumeration value="limited"/>
 *     &lt;enumeration value="private"/>
 *     &lt;enumeration value="closed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SyndicationRight")
@XmlEnum
public enum SyndicationRight {

    @XmlEnumValue("open")
    OPEN("open"),
    @XmlEnumValue("limited")
    LIMITED("limited"),
    @XmlEnumValue("private")
    PRIVATE("private"),
    @XmlEnumValue("closed")
    CLOSED("closed");
    private final String value;

    SyndicationRight(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SyndicationRight fromValue(String v) {
        for (SyndicationRight c: SyndicationRight.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
