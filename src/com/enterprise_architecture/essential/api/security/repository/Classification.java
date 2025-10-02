/**
 * Copyright (c)2019 Enterprise Architecture Solutions ltd  
 * All rights reserved
 * Redistribution and use in source and binary forms, with or without modification, is not permitted.
 * 
 */
package com.enterprise_architecture.essential.api.security.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO to manage a Classification.
 * 
 * @author Jonathan Carter
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Classification 
{
	private String classificationSchemeId;
	private String classificationSchemeName;
	private IdNamePair defaultClassification;
	
	/**
	 * @return the classificationSchemeId
	 */
	public String getClassificationSchemeId() {
		return classificationSchemeId;
	}
	/**
	 * @param classificationSchemeId the classificationSchemeId to set
	 */
	public void setClassificationSchemeId(String classificationSchemeId) {
		this.classificationSchemeId = classificationSchemeId;
	}
	/**
	 * @return the classificationSchemeName
	 */
	public String getClassificationSchemeName() {
		return classificationSchemeName;
	}
	/**
	 * @param classificationSchemeName the classificationSchemeName to set
	 */
	public void setClassificationSchemeName(String classificationSchemeName) {
		this.classificationSchemeName = classificationSchemeName;
	}
	/**
	 * @return the defaultClassification
	 */
	public IdNamePair getDefaultClassification() {
		return defaultClassification;
	}
	/**
	 * @param defaultClassification the defaultClassification to set
	 */
	public void setDefaultClassification(IdNamePair defaultClassification) {
		this.defaultClassification = defaultClassification;
	}
	
	

}
