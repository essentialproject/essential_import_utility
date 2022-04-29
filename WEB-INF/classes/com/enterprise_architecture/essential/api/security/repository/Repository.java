/**
 * Copyright (c)2019 Enterprise Architecture Solutions ltd  
 * All rights reserved
 * Redistribution and use in source and binary forms, with or without modification, is not permitted.
 * 
 */
package com.enterprise_architecture.essential.api.security.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO to manage a the information about a repository
 * 
 * @author Jonathan Carter
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository 
{
	private String id;
	private String name;
	//private DefaultClassificationBody defaultClassification;
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
//	/**
//	 * @return the defaultClassification
//	 */
//	public DefaultClassificationBody getDefaultClassification() {
//		return defaultClassification;
//	}
//	/**
//	 * @param defaultClassification the defaultClassification to set
//	 */
//	public void setDefaultClassification(DefaultClassificationBody defaultClassification) {
//		this.defaultClassification = defaultClassification;
//	}
//	
	
}
