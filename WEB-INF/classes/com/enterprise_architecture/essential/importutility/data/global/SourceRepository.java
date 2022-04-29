//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.08 at 02:47:23 PM BST 
//
/**
 * Updated 21.01.2019 JWC	Add some processing to the source name to avoid newlines etc.
 * 
 */

package com.enterprise_architecture.essential.importutility.data.global;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SourceRepository complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SourceRepository">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}SourceRepositoryName"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}SourceRepositoryDescription"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SourceRepository", propOrder = {
    "sourceRepositoryName",
    "sourceRepositoryDescription"
})
public class SourceRepository {

    @XmlElement(name = "SourceRepositoryName", required = true)
    protected String sourceRepositoryName;
    @XmlElement(name = "SourceRepositoryDescription", required = true)
    protected String sourceRepositoryDescription;
    @XmlAttribute
    protected String id;

    /**
     * Gets the value of the sourceRepositoryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceRepositoryName() {
        return sourceRepositoryName.replace("\n", "").replace("\r", "");
    }

    /**
     * Sets the value of the sourceRepositoryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceRepositoryName(String value) {
        this.sourceRepositoryName = value.replace("\n", "").replace("\r", "");
    }

    /**
     * Gets the value of the sourceRepositoryDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceRepositoryDescription() {
        return sourceRepositoryDescription;
    }

    /**
     * Sets the value of the sourceRepositoryDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceRepositoryDescription(String value) {
        this.sourceRepositoryDescription = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}