/**
 * Copyright (c)2019 Enterprise Architecture Solutions ltd  
 * All rights reserved
 * Redistribution and use in source and binary forms, with or without modification, is not permitted.
 * 
 */
package com.enterprise_architecture.essential.api.security.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO to represent a Default Classification body
 * 
 * @author Jonathan Carter
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultClassificationBody 
{
	private Classification editClassification;
	private Classification readClassification;
	
	/**
	 * @return the editClassification
	 */
	public Classification getEditClassification() {
		return editClassification;
	}
	/**
	 * @param editClassification the editClassification to set
	 */
	public void setEditClassification(Classification editClassification) {
		this.editClassification = editClassification;
	}
	/**
	 * @return the readClassification
	 */
	public Classification getReadClassification() {
		return readClassification;
	}
	/**
	 * @param readClassification the readClassification to set
	 */
	public void setReadClassification(Classification readClassification) {
		this.readClassification = readClassification;
	}
	
	
}
