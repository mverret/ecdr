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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for Url complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="Url">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="template" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="rel" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="indexOffset" type="{http://www.w3.org/2001/XMLSchema}int" default="1" />
 *       &lt;attribute name="pageOffset" type="{http://www.w3.org/2001/XMLSchema}int" default="1" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "Url" )
public class Url {

    @XmlAttribute( name = "template", required = true )
    private String template;
    @XmlAttribute( name = "type", required = true )
    private String type;
    @XmlAttribute( name = "rel" )
    private String rel;
    @XmlAttribute( name = "indexOffset" )
    private Integer indexOffset;
    @XmlAttribute( name = "pageOffset" )
    private Integer pageOffset;

    /**
     * Gets the value of the template property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTemplate( String value ) {
        this.template = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setType( String value ) {
        this.type = value;
    }

    /**
     * Gets the value of the rel property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRel() {
        return rel;
    }

    /**
     * Sets the value of the rel property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRel( String value ) {
        this.rel = value;
    }

    /**
     * Gets the value of the indexOffset property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public int getIndexOffset() {
        if ( indexOffset == null ) {
            return 1;
        } else {
            return indexOffset;
        }
    }

    /**
     * Sets the value of the indexOffset property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setIndexOffset( Integer value ) {
        this.indexOffset = value;
    }

    /**
     * Gets the value of the pageOffset property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public int getPageOffset() {
        if ( pageOffset == null ) {
            return 1;
        } else {
            return pageOffset;
        }
    }

    /**
     * Sets the value of the pageOffset property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setPageOffset( Integer value ) {
        this.pageOffset = value;
    }

}
