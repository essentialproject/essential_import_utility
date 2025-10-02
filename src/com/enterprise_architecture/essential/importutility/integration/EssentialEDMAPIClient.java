/**
 * 
 * Copyright (c)2019 Enterprise Architecture Solutions ltd.
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
 * Class to manage requests to Essential EDM APIs
 * 
 * 18.11.2019	JWC	Version 1.0 started 
 */
package com.enterprise_architecture.essential.importutility.integration;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;

import com.enterprise_architecture.essential.base.api.ApiErrorMessage;
import com.enterprise_architecture.essential.base.api.ApiResponse;
import com.enterprise_architecture.essential.base.api.ApiUtils;
import com.enterprise_architecture.essential.http.client.EssentialHttpClient;
import com.enterprise_architecture.essential.importutility.ui.EssentialImportInterface;
import com.enterprise_architecture.essential.report.security.ViewerSecurityManager;


/**
 * Make requests to Essential EDM APIs (via the internal infrastructure)
 * 
 * @author Jonathan Carter
 *
 */
public class EssentialEDMAPIClient 
{
	
	/**
	 * Property specifying the EDM service endpoint
	 */
	private static final String EDM_SERVICE_REQUEST_PREFIX = "edmServicePrefix";
	
	/**
	 * Request header name in which the API key must be supplied
	 */
	private static final String API_KEY_PARAM = "x-api-key";
	
	/**
	 * Request Parameter name for Authorization
	 */
	private static final String AUTHORIZATION_PARAM = "Authorization";
	
	
	/**
	 * Get a reference to the logger.
	 */
	private static final Logger iuLog = LoggerFactory.getLogger(EssentialEDMAPIClient.class);
	
	/**
	 * ServletContext that holds parameters etc. for making requests to EDM
	 */
	private ServletContext itsServletContext = null;
	
	/**
	 * Properties for the authentication
	 * Also includes details of the API server
	 * 
	 */
	private String itsEipApiKey;

	
	/**
	 * Default constructor
	 */
	public EssentialEDMAPIClient(ServletContext theServletContext) 
	{
		itsServletContext = theServletContext;
		
		Properties anAuthnServerProperties = EssentialImportInterface.getAllApplicationPropertiesFromFile(EssentialImportInterface.AUTHN_SERVER_PROPERTIES_FILE, theServletContext);
		itsEipApiKey = anAuthnServerProperties.getProperty(EssentialImportInterface.EIP_API_KEY_PROPERTY);
		if (itsEipApiKey == null || itsEipApiKey.trim().isEmpty()) {
			iuLog.error("No EIP API Key defined. Make sure to set the "+EssentialImportInterface.EIP_API_KEY_PROPERTY+" property in the property file "+EssentialImportInterface.AUTHN_SERVER_PROPERTIES_FILE);
		}
	}
		
	/**
	 * Make a GET request API call to the Essential EDM API at the specified URL
	 * 
	 * 
	 * @param theURL the relative URL to the API Platform but must be the internal URL after the hostname
	 * @return a String response, e.g. a JSON string
	 */
	public ApiResponse getEssentialEDMAPI(String theURL)
	{
		ApiResponse aResponse;
		HttpServletRequest anOriginalRequest = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		
		// Make an HTTP Client request using the specified URL
		
		// Need:		
		// Authorization Bearer token - Get a fresh bearer token from session attribute
		String aFreshBearerToken = (String) anOriginalRequest.getSession(false).getAttribute(ViewerSecurityManager.SESSION_ATTR_BEARER_TOKEN);
		
		// If the bearer token isn't valid - e.g. refresh token has expired return 403
		if(aFreshBearerToken == null)
		{
			//Return 403 code 10 - in ApiResponse class buildErrorResponseAndLogUserOut			
			aResponse = ApiUtils.buildJsonErrorResponse(HttpServletResponse.SC_FORBIDDEN, ApiErrorMessage.ErrorCode.BEARER_TOKEN_EXPIRED, "Bearer token and refresh token expired. Please login");
			return aResponse;
		}
		
		String anAuthZ = "Bearer " + aFreshBearerToken;
		
		HttpRequest aRequest = new BasicHttpRequest("GET", theURL);		
		aRequest.setHeader(AUTHORIZATION_PARAM, anAuthZ);
		aRequest.setHeader(API_KEY_PARAM, itsEipApiKey);
		aRequest.setHeader("Content-Type", "application/json");
		
		iuLog.debug("Set request headers to: API key: {}", itsEipApiKey);
		iuLog.debug("Authorization: {}", anAuthZ);
		
		// Headers are set, make the request to the URL:
		String aURI = itsServletContext.getInitParameter(EDM_SERVICE_REQUEST_PREFIX) + theURL;		
		
		// Make the HTTP request:
		EssentialHttpClient anHttpClient = new EssentialHttpClient();
		aResponse = anHttpClient.doGet(aRequest, aURI);
		
		// Return the response in aResponse
		return aResponse;
	}
	
}
