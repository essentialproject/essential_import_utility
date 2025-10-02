/**
    * Copyright (c)2014 Enterprise Architecture Solutions ltd.
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
	* Class to send callback response to clients of the service.
	* 15.04.2014	JWC	Version 1.0 started
 */
package com.enterprise_architecture.essential.importutility.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


import com.enterprise_architecture.essential.importutility.service.importcallback.ImportCallback;

/**
 * Class to manage the callback requests to clients of the service.
 * Marshalls an ImportCallback object into XML and transmits it to a specified URL via 
 * HTTP POST.
 * @author Jonathan Carter
 * @see ImportCallback ImportCallback
 *
 */
public class ImportResponse 
{
	
	/**
	 * Schema location included in the response XML header.
	 */
	public static final String XSD_LOCATION = "service/import_callback.xsd";
	
	/**
	 * HTTP POST parameter containing the callback message
	 */
	public static final String CALLBACK_MESSAGE_PARAM = "callback_message";
	
	/**
	 * Content type of the response call - XML in UTF-8
	 */
	private static final String CONTENT_TYPE = "text/xml; charset=utf-8";
	
	/**
	 * MIME / Content type for the callback payload
	 */
	private static final String MIME_TYPE = "application/x-www-form-urlencoded";
		
	/**
	 * Default value for the encoding of the response. Can be overridden by the Default Character
	 * Encoding context parameter in web.xml
	 */
	private static String XML_ENCODING = "UTF-8";
		
	/**
	 * Default constructor
	 */
	public ImportResponse()
	{
		
	}
	
	/**
	 * Send the details of the execution of an import to the original requestor client via the specified URL.
	 * Returns an HTTP Status code to indicate the results of the callback operation.
	 * @param theImportResults the details of the execution of an import activity
	 * @param theResponseURL the client URL that is waiting to receive the callback
	 * @return an HTTP Status code indicating the results of the callback request. A return code of 0 indicates an
	 * error encountered by this object whislt attempting to make the callback request.
	 */
	public synchronized int sendImportResponse(ImportCallback theImportResults, String theResponseURL)
	{
		int aStatusCode = 0;
				
		// Open an HTTP POST on the response URL
		DefaultHttpClient anHTTPClient = new DefaultHttpClient();
		HttpPost aPost = new HttpPost(theResponseURL);
			
		// Marshall the object into the XML String
		try
		{
			JAXBContext aJaxBContext = JAXBContext.newInstance(ImportCallback.class);
			Marshaller aMarshaller = aJaxBContext.createMarshaller();
			
			// output pretty printed
			aMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			aMarshaller.setProperty(Marshaller.JAXB_ENCODING, XML_ENCODING);
			aMarshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, XSD_LOCATION);
			
			// Load the XML into the response
			StringWriter aWriter = new StringWriter();
			aMarshaller.marshal(theImportResults, aWriter);
			
			List <NameValuePair> aNameValuePairList = new ArrayList <NameValuePair>();
	        aNameValuePairList.add(new BasicNameValuePair(CALLBACK_MESSAGE_PARAM, aWriter.toString()));
	        UrlEncodedFormEntity anXMLEntity = new UrlEncodedFormEntity(aNameValuePairList, XML_ENCODING);
			anXMLEntity.setContentEncoding(XML_ENCODING);
			anXMLEntity.setContentType(MIME_TYPE);
			aPost.setEntity(anXMLEntity);
			
			// Make request and process the response
			HttpResponse aResponse = anHTTPClient.execute(aPost);
			HttpEntity aResponseEntity = aResponse.getEntity();
			aStatusCode = aResponse.getStatusLine().getStatusCode();
			if(aResponseEntity != null)
			{
				aResponseEntity.consumeContent();
			}
		}
		catch (JAXBException aJaxbEx)
		{
			System.err.println("Import Service Callback Response: Error when producing XML message from ImportCallback object.");
			System.err.println("Message: " + aJaxbEx.getLocalizedMessage());
			aJaxbEx.printStackTrace();
		}		
		catch (IllegalArgumentException anIllegalArgEx)
		{
			System.err.println("Import Service Callback Response: Error marshalling the ImportCallback details into XML message.");
			System.err.println("Message: " + anIllegalArgEx.getLocalizedMessage());
			anIllegalArgEx.printStackTrace();
		}
		catch(UnsupportedEncodingException anEncodingEx)
		{
			System.err.println("Import Service Callback Response: Unsupported content type set to POST request when trying to use:" + CONTENT_TYPE);
			System.err.println("Message: " + anEncodingEx.getLocalizedMessage());
			anEncodingEx.printStackTrace();
		}
		catch(ClientProtocolException aProtocolEx)
		{
			System.err.println("Import Service Callback Response: Error encountered when making HTTP POST request for callback");
			System.err.println("Message: " + aProtocolEx.getLocalizedMessage());
			aProtocolEx.printStackTrace();
		}
		catch(IOException anIOEx)
		{
			System.err.println("Import Service Callback Response: Error encountered when making HTTP POST request for callback");
			System.err.println("Message: " + anIOEx.getLocalizedMessage());
			anIOEx.printStackTrace();
		}
		return aStatusCode;
	}
}
