/**
 * Copyright (c)2019 Enterprise Architecture Solutions Ltd.
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
 */
package com.enterprise_architecture.essential.http.client;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enterprise_architecture.essential.base.api.ApiErrorMessage;
import com.enterprise_architecture.essential.base.api.ApiResponse;
import com.enterprise_architecture.essential.base.api.ApiUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Make client HTTP requests to Essential Components
 *  
 * @author Jonathan Carter
 *
 */
public class EssentialHttpClient 
{

	private Logger itsLog = LoggerFactory.getLogger(EssentialHttpClient.class);
	
	/**
	 * Create an instance of the EssentialHttpClient
	 * 
	 */
	public EssentialHttpClient() 
	{
		
	}
	
	/**
	 * Make a GET request to the specified URL using the specified request
	 * 
	 * @param theRequest the HTTP Method to use (GET, POST, etc.), request headers etc to use
	 * @param theURL the full URL at which to make the request 
	 * @return an ApiResponse object describing the result
	 * 
	 */
	public ApiResponse doGet(HttpRequest theRequest, String theURL)
	{
		ApiResponse aResponse;
		CloseableHttpClient anHttpClient = null;
		try
		{
			// Create a new request
			anHttpClient = HttpClients.createDefault();
			HttpGet aRequest = new HttpGet(theURL);
			
			// Load the headers into the request
			HeaderIterator aHeaderIt = theRequest.headerIterator();
			while(aHeaderIt.hasNext())
			{
				Header aHeader = (Header)aHeaderIt.next();
				String aName = aHeader.getName();
				String aValue = aHeader.getValue();
				aRequest.addHeader(aName, aValue);
			}
			
			// Make the request
			aResponse = executeHttpRequest(anHttpClient, aRequest, theURL);
		}
		catch(Exception anEx)
		{
			itsLog.error("Exception caught whilst making HttpClient GET request: {}", anEx);
			aResponse = ApiUtils.buildJsonErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Exception caught: error encountered making API request");
		}
		finally
		{
			if(anHttpClient != null)
			{
				try
				{
					anHttpClient.close();
				}
				catch (Exception anEx)
				{
					itsLog.error("Error encountered closing the Status Ressponse and HttpClient: " + anEx.getMessage());
					aResponse = ApiUtils.buildJsonErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Exception caught: error encountered making API request");
				}
			}
		}
		
		return aResponse;
	}
	
	/**
	 * Make a POST request to the specified URL using the specified request
	 * @param theRequest
	 * @param theURL
	 * @return
	 */
	public ApiResponse doPost(HttpRequest theRequest, String theURL)
	{
		ApiResponse aResponse = ApiUtils.buildJsonErrorResponse(HttpStatus.SC_NOT_IMPLEMENTED, "EssentialHttpClient.doPost(): method not yet implemented");
		
		return aResponse;
	}
	
	/**
	 * Make a generic HTTP request
	 * @param theClient the HttpClient, configured to make a request to a URL
	 * @param theHttpRequest a GET, PUT, POST, PATCH, DELETE request with headers populated
	 * @param theURL the URL that is being requested - using for error reporting in this method
	 * @return a response JSON 
	 */
	private ApiResponse executeHttpRequest(CloseableHttpClient theClient, HttpUriRequest theHttpRequest, String theURL) 
			throws ClientProtocolException, IOException
	{
		ApiResponse aResponse = null;
		
		CloseableHttpResponse anHttpResponse = theClient.execute(theHttpRequest);
		try
		{
			HttpEntity anEntity = anHttpResponse.getEntity();
			StatusLine aResponseStatus = anHttpResponse.getStatusLine();
			
			if(aResponseStatus.getStatusCode() == HttpStatus.SC_OK)
			{
				// Read the response from a success message
				String aResponseJson = EntityUtils.toString(anEntity);
				EntityUtils.consume(anEntity);
				aResponse = parseApiResponse(aResponseJson);
			}
			else
			{
				// Handle an error response
				String aReason = aResponseStatus.getReasonPhrase();
				int aStatusCode = aResponseStatus.getStatusCode();
				
				// Default response if not error response body
				aResponse = ApiUtils.buildJsonErrorResponse(aStatusCode, aReason);
				if(anEntity != null)
				{
					// We've got an error response, so pass this back
					String aResult = EntityUtils.toString(anEntity);
					itsLog.error("Error encountered making request to URL: {}. Status code: {}, reason: {}. message: {}", theURL, aStatusCode, aReason, aResult);
				}
				else
				{
					itsLog.error("Error encountered making request to URL: {}. Status code: {}, reason: {}. No message returned", theURL, aStatusCode, aReason);
				}
			}
		}
		catch(Exception anEx)
		{
			itsLog.error("Exception caught whilst making HttpClient GET request: {}", anEx);
			aResponse = ApiUtils.buildJsonErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Exception caught: error encountered making API request");
		}
		finally
		{
			try
			{
				anHttpResponse.close();
				theClient.close();					
			}
			catch(Exception anEx)
			{
				itsLog.error("Error encountered closing the status response and HttpClient: " + anEx.getMessage());
			}
		}
		
		return aResponse;
	}

	/** 
	 * Parse the JSON to get the correct HTTP Status code from the response from the execution of the script
	 * If no statusCode JSON attribute is in the start of the response message, return 200.
	 * @param theResponseJson
	 * @return anApiResponse
	 */
	private ApiResponse parseApiResponse(String theResponseJson)
	{
		ObjectMapper aMapper = new ObjectMapper();
		try {
			ApiErrorMessage anApiErrorMessage = (ApiErrorMessage) aMapper.readValue(theResponseJson, ApiErrorMessage.class);
			return new ApiResponse(anApiErrorMessage.getStatusCode(), theResponseJson);
		} catch (IOException e) {
			// couldn't parse the JSON, use fallback method below
			String aStringWithWhitespacesRemoved = theResponseJson.replace(" ", "");
			if (itsLog.isWarnEnabled() && aStringWithWhitespacesRemoved.startsWith("{\"statusCode\":")) {
				itsLog.warn("Couldn't parse the JSON response from the Python API even though it looks like an error response as it starts with \"statusCode\". Falling back to string matching. Response JSON: {}"+theResponseJson);
			}
			int aStatusCode;
			if(aStringWithWhitespacesRemoved.startsWith("{\"statusCode\":4"))
				aStatusCode = 400; // catch-all 4xx
			else if(aStringWithWhitespacesRemoved.startsWith("{\"statusCode\":5"))
				aStatusCode = 500; // catch-all 5xx
			else
				aStatusCode = 200; // we make this the default as when the API runs successfully it doesn't return a status code in the response JSON
			return new ApiResponse(aStatusCode, theResponseJson);
		}
	}

}
