//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.08 at 03:53:17 PM BST 
//


package com.enterprise_architecture.essential.importutility.data.common;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DerivedValue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DerivedValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/common}DerivedValueString"/>
 *         &lt;element ref="{http://www.enterprise-architecture.org/essential/importutility/common}DerivedValueRef"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DerivedValue", propOrder = {
    "derivedValueStringOrDerivedValueRef"
})
public class DerivedValue {

    @XmlElements({
        @XmlElement(name = "DerivedValueRef", type = DerivedValueRef.class),
        @XmlElement(name = "DerivedValueString", type = DerivedValueString.class)
    })
    protected List<Object> derivedValueStringOrDerivedValueRef;

    /**
     * Gets the value of the derivedValueStringOrDerivedValueRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the derivedValueStringOrDerivedValueRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDerivedValueStringOrDerivedValueRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DerivedValueRef }
     * {@link DerivedValueString }
     * 
     * 
     */
    public List<Object> getDerivedValueStringOrDerivedValueRef() {
        if (derivedValueStringOrDerivedValueRef == null) {
            derivedValueStringOrDerivedValueRef = new ArrayList<Object>();
        }
        return this.derivedValueStringOrDerivedValueRef;
    }

}
