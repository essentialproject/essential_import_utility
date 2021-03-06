//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.08 at 02:47:23 PM BST 
//
// Edited 25.04.2017 JWC to add new attributes: logXMLDUPPath


package com.enterprise_architecture.essential.importutility.data.global;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ImportActivityLog complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ImportActivityLog">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogUser"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogFolderPath"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogSourceRepositoryPath"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogOutputLogPath"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogImportSpecPath"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogImportScriptPath"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogUpdatedRepositoryPath"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogTargetEnvName"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogTargetEnvType"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/config}LogXMLDUPPath"/>
 *       &lt;/sequence>
 *       &lt;attribute name="logCreationTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="logLastUpdatedTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="logActivityStatus" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="logActivityType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImportActivityLog", propOrder = {
    "logUser",
    "logFolderPath",
    "logSourceRepositoryPath",
    "logOutputLogPath",
    "logImportSpecPath",
    "logImportScriptPath",
    "logUpdatedRepositoryPath",
    "logTargetEnvName",
    "logTargetEnvType",
    "logXMLDUPPath"
})
public class ImportActivityLog {

    @XmlElement(name = "LogUser", required = true)
    protected String logUser;
    @XmlElement(name = "LogFolderPath", required = true)
    protected String logFolderPath;
    @XmlElement(name = "LogSourceRepositoryPath", required = true)
    protected String logSourceRepositoryPath;
    @XmlElement(name = "LogOutputLogPath", required = true)
    protected String logOutputLogPath;
    @XmlElement(name = "LogImportSpecPath", required = true)
    protected String logImportSpecPath;
    @XmlElement(name = "LogImportScriptPath", required = true)
    protected String logImportScriptPath;
    @XmlElement(name = "LogUpdatedRepositoryPath", required = true)
    protected String logUpdatedRepositoryPath;
    @XmlElement(name = "LogTargetEnvName", required = true)
    protected String logTargetEnvName;
    @XmlElement(name = "LogTargetEnvType", required = true)
    protected String logTargetEnvType;
    @XmlElement(name = "LogXMLDUPPath", required = true)
    protected String logXMLDUPPath;
	@XmlAttribute
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar logCreationTime;
    @XmlAttribute
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar logLastUpdatedTime;
    @XmlAttribute
    protected String logActivityStatus;
    @XmlAttribute
    protected String logActivityType;

    /**
     * Gets the value of the logUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogUser() {
        return logUser;
    }

    /**
     * Sets the value of the logUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogUser(String value) {
        this.logUser = value;
    }

    /**
     * Gets the value of the logFolderPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogFolderPath() {
        return logFolderPath;
    }

    /**
     * Sets the value of the logFolderPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogFolderPath(String value) {
        this.logFolderPath = value;
    }

    /**
     * Gets the value of the logSourceRepositoryPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogSourceRepositoryPath() {
        return logSourceRepositoryPath;
    }

    /**
     * Sets the value of the logSourceRepositoryPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogSourceRepositoryPath(String value) {
        this.logSourceRepositoryPath = value;
    }

    /**
     * Gets the value of the logOutputLogPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogOutputLogPath() {
        return logOutputLogPath;
    }

    /**
     * Sets the value of the logOutputLogPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogOutputLogPath(String value) {
        this.logOutputLogPath = value;
    }

    /**
     * Gets the value of the logImportSpecPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogImportSpecPath() {
        return logImportSpecPath;
    }

    /**
     * Sets the value of the logImportSpecPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogImportSpecPath(String value) {
        this.logImportSpecPath = value;
    }

    /**
     * Gets the value of the logImportScriptPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogImportScriptPath() {
        return logImportScriptPath;
    }

    /**
     * Sets the value of the logImportScriptPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogImportScriptPath(String value) {
        this.logImportScriptPath = value;
    }

    /**
     * Gets the value of the logUpdatedRepositoryPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogUpdatedRepositoryPath() {
        return logUpdatedRepositoryPath;
    }

    /**
     * Sets the value of the logUpdatedRepositoryPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogUpdatedRepositoryPath(String value) {
        this.logUpdatedRepositoryPath = value;
    }

    /**
     * Gets the value of the logTargetEnvName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogTargetEnvName() {
        return logTargetEnvName;
    }

    /**
     * Sets the value of the logTargetEnvName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogTargetEnvName(String value) {
        this.logTargetEnvName = value;
    }

    /**
     * Gets the value of the logTargetEnvType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogTargetEnvType() {
        return logTargetEnvType;
    }

    /**
     * Sets the value of the logTargetEnvType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogTargetEnvType(String value) {
        this.logTargetEnvType = value;
    }

    /**
 	 * @return the logXMLDUPPath
 	 */
 	public String getLogXMLDUPPath() {
 		return logXMLDUPPath;
 	}

 	/**
 	 * @param logXMLDUPPath the logXMLDUPPath to set
 	 */
 	public void setLogXMLDUPPath(String logXMLDUPPath) {
 		this.logXMLDUPPath = logXMLDUPPath;
 	}

    /**
     * Gets the value of the logCreationTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLogCreationTime() {
        return logCreationTime;
    }

    /**
     * Sets the value of the logCreationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLogCreationTime(XMLGregorianCalendar value) {
        this.logCreationTime = value;
    }

    /**
     * Gets the value of the logLastUpdatedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLogLastUpdatedTime() {
        return logLastUpdatedTime;
    }

    /**
     * Sets the value of the logLastUpdatedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLogLastUpdatedTime(XMLGregorianCalendar value) {
        this.logLastUpdatedTime = value;
    }

    /**
     * Gets the value of the logActivityStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogActivityStatus() {
        return logActivityStatus;
    }

    /**
     * Sets the value of the logActivityStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogActivityStatus(String value) {
        this.logActivityStatus = value;
    }

    /**
     * Gets the value of the logActivityType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogActivityType() {
        return logActivityType;
    }

    /**
     * Sets the value of the logActivityType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogActivityType(String value) {
        this.logActivityType = value;
    }

}
