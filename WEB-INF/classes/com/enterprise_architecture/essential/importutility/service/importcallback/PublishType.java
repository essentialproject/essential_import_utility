//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.15 at 12:57:14 PM BST 
//


package com.enterprise_architecture.essential.importutility.service.importcallback;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Defines the results of a publish task
 * 
 * <p>Java class for publishType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="publishType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requested" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="status" type="{http://www.enterprise-architecture.com/essential/importutility/service/importcallback}statusType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "publishType", propOrder = {
    "requested",
    "status"
})
public class PublishType {

    @XmlElement(required = true)
    protected String requested;
    @XmlElement(required = true)
    protected StatusType status;

    /**
     * Gets the value of the requested property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequested() {
        return requested;
    }

    /**
     * Sets the value of the requested property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequested(String value) {
        this.requested = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link StatusType }
     *     
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusType }
     *     
     */
    public void setStatus(StatusType value) {
        this.status = value;
    }

}
