/**
 * Copyright (c)2019 Enterprise Architecture Solutions ltd  
 * All rights reserved
 * Redistribution and use in source and binary forms, with or without modification, is not permitted.
 * 
 */
package com.enterprise_architecture.essential.api.security.repository;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO to manage information about a tenant
 * 
 * @author Jonathan Carter
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantRepositories 
{
	private String tenantId = "";
	private String tenantName = "";
	private List<Repository> repositories;
	
	/**
	 * @return the tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}
	/**
	 * @param tenantId the tenantId to set
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	/**
	 * @return the tenantName
	 */
	public String getTenantName() {
		return tenantName;
	}
	/**
	 * @param tenantName the tenantName to set
	 */
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	/**
	 * @return the repositories
	 */
	public List<Repository> getRepositories() {
		return repositories;
	}
	/**
	 * @param repositories the repositories to set
	 */
	public void setRepositories(List<Repository> repositories) {
		this.repositories = repositories;
	}
	
}
