/**
 * Copyright (c)2019 Enterprise Architecture Solutions ltd  
 * All rights reserved
 * Redistribution and use in source and binary forms, with or without modification, is not permitted.
 * 
 */
package com.enterprise_architecture.essential.api.security.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO to represent a Default Classification
 * 
 * @author Jonathan Carter
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultClassification 
{
	private DefaultClassificationBody defaultClassification;

	/**
	 * @return the defaultClassification
	 */
	public DefaultClassificationBody getDefaultClassification() {
		return defaultClassification;
	}

	/**
	 * @param defaultClassification the defaultClassification to set
	 */
	public void setDefaultClassification(
			DefaultClassificationBody defaultClassification) {
		this.defaultClassification = defaultClassification;
	}
	
	
}
