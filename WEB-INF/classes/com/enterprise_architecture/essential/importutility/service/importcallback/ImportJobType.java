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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Defines the results of an import job
 * 
 * <p>Java class for import_jobType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="import_jobType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://www.enterprise-architecture.com/essential/importutility/service/importcallback}statusType"/>
 *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="import_activity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="target_environment" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="source_content" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="batch_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "import_jobType", propOrder = {
    "status",
    "timestamp",
    "importActivity",
    "targetEnvironment",
    "sourceContent",
    "batchId"
})
public class ImportJobType {

    @XmlElement(required = true)
    protected StatusType status;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestamp;
    @XmlElement(name = "import_activity", required = true)
    protected String importActivity;
    @XmlElement(name = "target_environment", required = true)
    protected String targetEnvironment;
    @XmlElement(name = "source_content", required = true)
    protected String sourceContent;
    @XmlElement(name = "batch_id")
    protected String batchId;

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

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimestamp(XMLGregorianCalendar value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the importActivity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImportActivity() {
        return importActivity;
    }

    /**
     * Sets the value of the importActivity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImportActivity(String value) {
        this.importActivity = value;
    }

    /**
     * Gets the value of the targetEnvironment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetEnvironment() {
        return targetEnvironment;
    }

    /**
     * Sets the value of the targetEnvironment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetEnvironment(String value) {
        this.targetEnvironment = value;
    }

    /**
     * Gets the value of the sourceContent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceContent() {
        return sourceContent;
    }

    /**
     * Sets the value of the sourceContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceContent(String value) {
        this.sourceContent = value;
    }

    /**
     * Gets the value of the batchId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the value of the batchId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatchId(String value) {
        this.batchId = value;
    }

}
