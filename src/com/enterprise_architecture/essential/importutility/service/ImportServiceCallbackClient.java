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
	* Servlet to receive ImportService callback messages
	* 28.04.2014	JWC	Version 1.0 started
 */
package com.enterprise_architecture.essential.importutility.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default callback client for the Import Service. Receives a POST request with an
 * XML document describing the results of executions of the ImportService.
 * On receiving a request, the servlet writes the received XML to standard out stream.
 * <br/>
 * This class can be used to receive callbacks or as an example / point of reference for the development
 * of more specific callback handlers.
 * @author Jonathan Carter
 *
 */
public class ImportServiceCallbackClient extends HttpServlet 
{
	public static final String NO_CALLBACK_XML = "ImportServiceCallbackClient: Request Error. No callback message found.";

	/**
	 * Receive requests to the service, however, the GET method is not supported and requests using GET will be 
	 * rejected.
	 */
	protected void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException 
	{
		// return an error message? Unsupported method?
		// Does it make sense for any resources to be 'got'?
		theResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ImportService.GET_METHOD_NOT_ALLOWED_MESSAGE);
	}
	
	/**
	 * Receive requests to the callback service.
	 * Read the posted XML callback message from a request parameter called callback_message and write it to Standard Out.
	 * @see #ImportResponse.CALLBACK_MESSAGE_PARAM
	 */
	protected void doPost(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
	{
		String aResponseMessage = theRequest.getParameter(ImportResponse.CALLBACK_MESSAGE_PARAM);
		
		if(aResponseMessage != null)
		{
			System.out.println("Callback message received from ImportService:");
			System.out.println(aResponseMessage);
			theResponse.setStatus(HttpServletResponse.SC_OK);
		}
		else
		{
			theResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, NO_CALLBACK_XML);
		}
		
	}
}
