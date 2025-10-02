/**
 * 
 * Copyright (c)2019-2020 Enterprise Architecture Solutions ltd.
 * This file is part of Essential Architecture Manager, 
 * the Essential Architecture Meta Model and The Essential Project.
 *
 * Essential Architecture Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Essential Architecture Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Essential Architecture Manager.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Class to manage requests to Essential APIs
 * 
 * 14.11.2019	JWC	Version 1.0 started 
 * 30.09.2020	JWC Tweak to use default tenant name for certain Docker modes
 */
package com.enterprise_architecture.essential.importutility.integration;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;

import com.enterprise_architecture.essential.base.api.ApiResponse;
import com.enterprise_architecture.essential.base.api.ApiUtils;
import com.enterprise_architecture.essential.http.client.EssentialHttpClient;
import com.enterprise_architecture.essential.importutility.ui.EssentialImportInterface;
import com.enterprise_architecture.essential.report.security.ViewerSecurityManager;


/**
 * Make requests to Essential APIs (via the Gateway)
 * 
 * @author Jonathan Carter
 *
 */
public class EssentialAPIClient 
{
	
	/**
	 * Request Parameter name for Authorization
	 */
	private static final String AUTHORIZATION_PARAM = "Authorization";

	/**
	 * Names of Cookies used to identify the user
	 */
	private static final String TENANT_NAME_COOKIE = "tenant";
	
	/**
	 * Request Parameter name for tenant
	 */
	private static final String TENANT_NAME_PARAM = "x-tenant-name";
			
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = LoggerFactory.getLogger(EssentialAPIClient.class);
	
	/**
	 * Properties for the authentication
	 * Also includes details of the API server
	 * 
	 */
	private String itsApiServerBaseUrl;

	/**
	 * Property defining the default tenant name
	 */
	private String itsDefaultTenantName;

	@SuppressWarnings("unused")
	private ServletContext itsServletContext = null;
	
	
	/**
	 * Default constructor
	 */
	public EssentialAPIClient(ServletContext theServletContext) 
	{
		itsServletContext = theServletContext;

		Properties anAuthnServerProperties = EssentialImportInterface.getAllApplicationPropertiesFromFile(EssentialImportInterface.AUTHN_SERVER_PROPERTIES_FILE, theServletContext);
		itsApiServerBaseUrl = anAuthnServerProperties.getProperty(EssentialImportInterface.API_SERVER_BASE_URL_PROPERTY);
		if (itsApiServerBaseUrl == null || itsApiServerBaseUrl.trim().isEmpty()) {
			iuLog.error("No API server base url defined. Make sure to set the "+EssentialImportInterface.API_SERVER_BASE_URL_PROPERTY+" property in the property file "+EssentialImportInterface.AUTHN_SERVER_PROPERTIES_FILE);
		}
		// Read the default tenant name property. If it is null or empty, this is not a bug, just not Docker-mode
		itsDefaultTenantName = anAuthnServerProperties.getProperty(EssentialImportInterface.DEFAULT_TENANT_NAME_PARAM);
		iuLog.debug("Default tenant name is {}", itsDefaultTenantName);
	}
		
	/**
	 * Make a GET request API call to the Essential API platform at the specified URL
	 * behind the API Gateway.
	 * 
	 * @param theURL the relative URL to the API Platform but must be the internal URL after the hostname
	 * @return a String response, e.g. a JSON string
	 */
	public ApiResponse getEssentialAPI(String theURL)
	{
		ApiResponse aResponse;
		HttpServletRequest anOriginalRequest = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		// Make an HTTP Client request using the specified URL
		
		// Need:
		// x-tenant-name: Get from Cookie "tenant"
		// Authorization Bearer token - Get fresh token from session attribute
		String aTenantName = null;
		String aFreshBearerToken = (String) anOriginalRequest.getSession(false).getAttribute(ViewerSecurityManager.SESSION_ATTR_BEARER_TOKEN);
		
		// TODO Read the aTenantName tenant name from a look up using the context of the bearer token, instead of this "work-around" that uses the Cookie
		Cookie[] aCookieList = anOriginalRequest.getCookies();
		Cookie aCookie = null;
		boolean isTenantFound = false;
		if(aCookieList != null && aCookieList.length > 0)
		{
			for (int i = 0; i < aCookieList.length; i++)
			{
				aCookie = aCookieList[i];
				String aCookieName = aCookie.getName();
				if (aCookieName.equals(TENANT_NAME_COOKIE))
				{
					aTenantName = aCookie.getValue();
					isTenantFound = true;					
				}
				
				// Check that we have all the cookies that we need
				if(isTenantFound)
				{
					// If we have both, break the loop
					break;
				}
			}			
			iuLog.debug("Got tenant name: {}", aTenantName);
		}
		
		// If aTenantName has not been set, check to see if there's a default Docker value set and use that
		if(aTenantName == null || aTenantName.isEmpty())
		{
			aTenantName = itsDefaultTenantName;
			iuLog.debug("Using default tenant name of: {}", aTenantName);
		}

		if (aTenantName == null || aFreshBearerToken == null) {
			// Got no user credentials, so return error
			aResponse = ApiUtils.buildJsonErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "No authentication credentials supplied");
			iuLog.error("Could not get bearer token or tenant name from the request");
			return aResponse;
		}
				
		// We have credentials and can make a client request to the API
		String anAuthZ = "Bearer " + aFreshBearerToken;
		HttpRequest aRequest = new BasicHttpRequest("GET", theURL);
		aRequest.setHeader(TENANT_NAME_PARAM, aTenantName);
		aRequest.setHeader(AUTHORIZATION_PARAM, anAuthZ);
		aRequest.setHeader("Content-Type", "application/json");
		
		iuLog.debug("Set request headers to: Tenant name: {}", aTenantName);
		iuLog.debug("Authorization: ", anAuthZ);
		
		// Headers are set, make the request to the URL:
		String aURI = itsApiServerBaseUrl + theURL;
				
		// Make the HTTP request:
		EssentialHttpClient anHttpClient = new EssentialHttpClient();
		aResponse = anHttpClient.doGet(aRequest, aURI);
				
		// Return the response in aResponse
		return aResponse;
	}
	
}
