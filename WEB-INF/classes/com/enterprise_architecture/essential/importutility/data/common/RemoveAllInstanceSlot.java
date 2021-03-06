//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.08 at 03:53:17 PM BST 
//


package com.enterprise_architecture.essential.importutility.data.common;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/common}SlotName"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/common}ConditionalRef" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="sequenceNo" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "slotName",
    "conditionalRef"
})
@XmlRootElement(name = "RemoveAllInstanceSlot")
public class RemoveAllInstanceSlot {

    @XmlElement(name = "SlotName", required = true)
    protected String slotName;
    @XmlElement(name = "ConditionalRef")
    protected List<String> conditionalRef;
    @XmlAttribute
    protected BigInteger sequenceNo;

    /**
     * Gets the value of the slotName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSlotName() {
        return slotName;
    }

    /**
     * Sets the value of the slotName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSlotName(String value) {
        this.slotName = value;
    }

    /**
     * Gets the value of the conditionalRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conditionalRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConditionalRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getConditionalRef() {
        if (conditionalRef == null) {
            conditionalRef = new ArrayList<String>();
        }
        return this.conditionalRef;
    }

    /**
     * Gets the value of the sequenceNo property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSequenceNo() {
        return sequenceNo;
    }

    /**
     * Sets the value of the sequenceNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSequenceNo(BigInteger value) {
        this.sequenceNo = value;
    }

}
