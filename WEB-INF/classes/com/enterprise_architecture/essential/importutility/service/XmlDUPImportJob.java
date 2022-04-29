/**
 * Copyright (c)2017 Enterprise Architecture Solutions ltd.
 * This file is part of Essential Architecture Manager, the 
 * Essential Architecture Meta Model and The Essential Project
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
 * Runnable class to create a DUP for each XML import job.
 * 
 * 23.03.2017	JWC	Version 1.0 started
 */
package com.enterprise_architecture.essential.importutility.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivityLog;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.integration.DUPGenerator;
import com.enterprise_architecture.essential.importutility.service.importcallback.ImportCallback;
import com.enterprise_architecture.essential.importutility.service.importcallback.ImportJobType;
import com.enterprise_architecture.essential.importutility.service.importcallback.PublishType;
import com.enterprise_architecture.essential.importutility.service.importcallback.StatusType;
import com.enterprise_architecture.essential.importutility.ui.EssentialImportInterface;
import com.enterprise_architecture.essential.importutility.utils.Log;
import com.enterprise_architecture.essential.integration.core.IntegrationEngine;
import com.enterprise_architecture.essential.integration.core.IntegrationEngineClient;


/**
 * Import Job that is responsible for managing the generation of a DUP from the specified XML Import Activity. Once 
 * the DUP has been created, it is then sent to the Essential Data Management application via a REST service 
 * request for execution on the specified tenant and repository 
 * @author Jonathan W. Carter
 *
 */
public class XmlDUPImportJob extends EssentialImportInterface implements Runnable, IntegrationEngineClient 
{
	
	/**
	 * Endpoint for the login service on EDM
	 */
	public final static String LOGIN_SERVICE = "/login";
	
	/**
	 * Prefix of the DUP service request URL
	 */
	public final static String DUP_SERVICE_PREFIX = "/api/repositories/";
	
	/**
	 * Remainder of the static components of the DUP service URL
	 */
	public final static String DUP_SERVICE = "/essential-update/data-update-pack";
	

	/**
	 * Servlet configuration parameter that defines the EDM service endpoint prefix
	 */
	public static final String EDM_SERVICE_REQUEST_PREFIX = "edmServicePrefix";

	/**
	 * Define the name of the request parameter that contains the DUP file to send to service request
	 */
	public static final String DUP_FILE_PARAMETER_NAME = "file";
	
	/**
	 * Define the description to be used in the external repository definition for the XML DUP
	 */
	public static final String XML_DUP_DESCRIPTION = "Essential Import Utility: XML DUP";

	/**
	 * HTTP Response code returned by EDM on successful login
	 */
	public static final int EDM_LOGIN_SUCCESS = 302;
	
	/**
	 * HTTP Response code returned by the class when the user has authenticated to EDM but is not
	 * authorized to perform the requested operation
	 */
	public final static int EDM_SERVER_FORBIDDEN = 401;
		
	/**
	 * HTTP Response code returned by this class when login to EDM fails
	 */
	public final static int EDM_LOGIN_FAIL = 403;
	
	/**
	 * HTTP Response code returned by this class when request to EDM fails
	 */
	public final static int EDM_SERVER_SCRIPT_POST_FAILED = 500;
	
	/**
	 * HTTP Response code returned by this class when attempt to connect to EDM fails
	 */
	public final static int EDM_SERVER_HOST_CONNECT_FAILED = 503;
	
	/**
	 * The name of the import activity to run
	 */
	private String impActName = "";
	
	/**
	 * The name of the target environment in which to import
	 */
	private String targetEnvName = "";
	
	/**
	 * The name of the source data file to import
	 */
	private String sourceDataFileName = "";
	
	/**
	 * The name of the callback response URL
	 */
	private String responseURL = "";
			
	/**
	 * The ServletContext for the ImportService
	 */
	private ServletContext servletContext = null;
		
	/**
	 * Member to hold the code return by the Protege Integration Manager
	 */
	private int importReturnCode = 0;
	
	/**
	 * Member to manage the import activity that is being used by the Job
	 */
	private ImportActivity itsImportActivity = null;
	
	
	/**
	 * Member to manage the import user that is being used by the Job
	 */
	private String itsUser = null;
	
	/**
	 * The User SSO Token in the specified tenant. This account should 
	 * have access to the target repository and be able to run DUPs
	 */
	private String itsTenantUserSsoToken = "";
	
	/**
	 * The EIP API Key
	 */
	private String itsEipApiKey = "";
	
	/**
	 * The prefix of the service request to the EDM application, including the 'http:' part
	 * 
	 */
	private String itsEDMRequestPrefix = "";
	
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();

	/**
	 * Default constructor
	 */
	public XmlDUPImportJob() 
	{
		// All member variable initialised when defined.
	}	
	
	/**
	 * @return the impActName
	 */
	public String getImpActName() {
		return impActName;
	}

	/**
	 * @param impActName the impActName to set
	 */
	public void setImpActName(String impActName) {
		this.impActName = impActName;
	}

	/**
	 * @return the targetEnvName
	 */
	public String getTargetEnvName() {
		return targetEnvName;
	}

	/**
	 * @param targetEnvName the targetEnvName to set
	 */
	public void setTargetEnvName(String targetEnvName) {
		this.targetEnvName = targetEnvName;
	}

	/**
	 * @return the sourceDataFileName
	 */
	public String getSourceDataFileName() {
		return sourceDataFileName;
	}

	/**
	 * @param sourceDataFileName the sourceDataFileName to set
	 */
	public void setSourceDataFileName(String sourceDataFileName) {
		this.sourceDataFileName = sourceDataFileName;
	}

	/**
	 * @return the responseURL
	 */
	public String getResponseURL() {
		return responseURL;
	}

	/**
	 * @param responseURL the responseURL to set
	 */
	public void setResponseURL(String responseURL) {
		this.responseURL = responseURL;
	}

	/**
	 * @return the servletContext
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * @param servletContext the servletContext to set
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * @return the importReturnCode
	 */
	public int getImportReturnCode() {
		return importReturnCode;
	}

	/**
	 * @param importReturnCode the importReturnCode to set
	 */
	public void setImportReturnCode(int importReturnCode) {
		this.importReturnCode = importReturnCode;
	}

	/**
	 * @return the itsImportActivity
	 */
	public ImportActivity getItsImportActivity() {
		return itsImportActivity;
	}

	/**
	 * @param itsImportActivity the itsImportActivity to set
	 */
	public void setItsImportActivity(ImportActivity itsImportActivity) {
		this.itsImportActivity = itsImportActivity;
	}

	/**
	 * @return the itsUser
	 */
	public String getItsUser() {
		return itsUser;
	}

	/**
	 * @param itsUser the itsUser to set
	 */
	public void setItsUser(String itsUser) {
		this.itsUser = itsUser;
	}

	/**
	 * @return the itsTenantUserSsoToken
	 */
	public String getItsTenantUserSsoToken() {
		return itsTenantUserSsoToken;
	}

	/**
	 * @param itsTenantUserSsoToken the itsTenantUserSsoToken to set
	 */
	public void setItsTenantUserSsoToken(String itsTenantUserSsoToken) {
		this.itsTenantUserSsoToken = itsTenantUserSsoToken;
	}

	/**
	 * @return the itsEipApiKey
	 */
	public String getItsEipApiKey() {
		return itsEipApiKey;
	}

	/**
	 * @param itsEipApiKey the itsEipApiKey to set
	 */
	public void setItsEipApiKey(String itsEipApiKey) {
		this.itsEipApiKey = itsEipApiKey;
	}
	
	/**
	 * Test the received parameters and store any references to the worker objects, e.g. target environments,
	 * import activities, etc.
	 * @return integer code indicating success or one of the error codes defined by XmlImportServiceImpl.
	 * @see XmlImportServiceImpl XmlImportServiceImpl
	 */
	public int build()
	{
		int aStatusCode = XmlImportServiceImpl.JOB_ACCEPTED;
		
		// Get the EDM request prefix from the web.xml
		itsEDMRequestPrefix = servletContext.getInitParameter(EDM_SERVICE_REQUEST_PREFIX);
				
		//Get the application level Import Utility Data Manager
		ImportUtilityDataManager appDataManager = this.getImportUtilityDataManager(this.servletContext);
				
		// If we have found the data manager, do builds and checks
		if(appDataManager != null) 
		{
			//Get the Import Activity for the job
			this.itsImportActivity = appDataManager.getImportActivity(this.impActName);
			if(this.itsImportActivity == null) 
			{
				iuLog.log(Level.INFO, "JOB: INVALID IMPORT ACTIVITY");			
				return ImportServiceImpl.INVALID_IMPORT_ACTIVITY;
			}
			
			//Update the Import Activity using the source data file
			File aSourceXML = new File(this.sourceDataFileName);
			if(aSourceXML.exists()) 
			{
				try 
				{
					Media xmlFileMedia = new AMedia(aSourceXML, null, null);
					appDataManager.updateExcelImpActivitySpreadsheetFile(this.itsImportActivity, xmlFileMedia);
					appDataManager.saveAppData();
				}
				catch(FileNotFoundException e) 
				{
					iuLog.log(Level.INFO, "JOB: INVALID_SOURCE_CONTENT", e);
					return XmlImportServiceImpl.INVALID_SOURCE_CONTENT;
				}
				catch(IOException e) 
				{
					iuLog.log(Level.INFO, "JOB: INVALID_SOURCE_CONTENT", e);				
					return XmlImportServiceImpl.INVALID_SOURCE_CONTENT;
				}
			} 
			else 
			{
				iuLog.log(Level.INFO, "JOB: INVALID_SOURCE_CONTENT");				
				return XmlImportServiceImpl.INVALID_SOURCE_CONTENT;
			}			
		} 
		else 
		{		
			aStatusCode = XmlImportServiceImpl.JOB_REFUSED;
		}
				
		return aStatusCode;
	}

	
	
	/**
	 * When the parent thread executes, this method is invoked.
	 * Start the DUP Import Job. Process the supplied source content to create a DUP.
	 * When the DUP has been created, invoke the EDM application to process the DUP
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() 
	{
		iuLog.log(Level.INFO, "Starting DUP Job");
		String anImportStatus = "success";
		String anImportMessage = "";		
		
		//Get the Import Utility Data Manager singleton
		ImportUtilityDataManager appDataManager = this.getImportUtilityDataManager(this.servletContext);
		
		//Create a new Import Spec Data Manager
		ImportSpecDataManager impSpecDataManager = new ImportSpecDataManager(this.servletContext, this.itsImportActivity);
		
		//Retrieve the import spec for the Import Activity from the Import Spec Data Manager
		String importScript = impSpecDataManager.getXMLImportSpecScript(itsImportActivity);
		
		//Create a new Import Activity Log
		String anImportType = "DUP: XML";
		ImportActivityLog log = appDataManager.initAutoImportActivityLog(itsImportActivity, anImportType, targetEnvName, itsUser);
		
		// Transform the source data with the importSpec XSL to get the Python script.
		// and run it, via the EssentialIntegrationEngine.
		// Set the include path to the location of this activity
		String anIncludeLocalPath = itsImportActivity.getImportActivityRootDirName();
		String anIncludePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + anIncludeLocalPath);
				
		//Create the Import Script and save it to the log
		String anXMLImportScript = "";
		try 
		{			
			anXMLImportScript = executeXMLDUP(importScript, sourceDataFileName);
		} 
		catch (TransformerException aTransformerEx) 
		{
			//set the Import Status to "exception" with an appropriate message and then send an appropriate response
			anImportStatus = "exception";
			anImportMessage = "Exception encountered when transforming source XML to create import script";
			
			this.constructCallbackMessage(anImportStatus, anImportMessage, "", "");
			aTransformerEx.printStackTrace();
		} 		
		
		// Execute the import of the DUP
		importReturnCode = this.executeDUP(appDataManager, log, anXMLImportScript);
		switch(importReturnCode)
		{
			case IntegrationEngine.SCRIPT_EXCEPTION_SC :
				anImportStatus = "exception";
				anImportMessage = "Import script exception";
				break;
			case IntegrationEngine.NO_EXTERNAL_REPOSITORY_NAME_SC :
				anImportStatus = "exception";
				anImportMessage = "No Source Repository defined";
				break;
			case IntegrationEngine.NO_SOURCE_DATA_SC :
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				break;
			case IntegrationEngine.NO_SOURCE_INTEGRATION_SC :
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				break;
			case IntegrationEngine.NO_TRANSFORM_SC :
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				break;
			case IntegrationEngine.NO_INCLUDES_SC :				
				anImportStatus = "exception";
				anImportMessage = "Internal Error - Included functions not found";	
				break;		
			case EDM_LOGIN_FAIL :
				anImportStatus = "exception";
				anImportMessage = "EDM Service Request Error - failed to login in to EDM with supplied credentials";
				break;
			case EDM_SERVER_SCRIPT_POST_FAILED :
				anImportStatus = "exception";
				anImportMessage = "EDM Service Request Error - posted import pack failed, see logs and EDM server environment for errors";
				break;
			case EDM_SERVER_HOST_CONNECT_FAILED :
				anImportStatus = "exception";
				anImportMessage = "EDM Service Request Error - failed to connect to the EDM service";
				break;
			case EDM_SERVER_FORBIDDEN :
				anImportStatus = "exception";
				anImportMessage = "EDM Service Request Error - not authorized to execute the DUP on the specified target repository";
				break;
				
			default:
				anImportStatus = "success";
				anImportMessage = "Import Successful";
				break;			
		}
		
		// Clear the service/source folder as we have logged the received data file
		File aSourceFile = new File(sourceDataFileName);
		iuLog.log(Level.FINE, "XML Import complete. Deleting received source XML: " + sourceDataFileName);
		aSourceFile.delete();
				
		//Send the callback message
		this.constructCallbackMessage(anImportStatus, anImportMessage, "", "");
		// FINISHED RUNNING THE IMPORT
		
	}
	
	/** 
	 * Update progress on the generation of the DUP
	 * @see com.enterprise_architecture.essential.integration.core.IntegrationEngineClient#updateProgress(java.lang.String, int)
	 */
	@Override
	public void updateProgress(String theMessage, int theProgressPercentage) 
	{
		messages = messages + theMessage;

	}	
		
	/**
	 * Construct and send a callback message containing the given Import Status and Message.
	 */
	protected void constructCallbackMessage(String anImportStatus, String anImportMessage, String aPublishStatus, String aPublishMessage) {
		
		ImportCallback anImportResults = new ImportCallback();
		ImportJobType aJobDetails = new ImportJobType();
		PublishType aPublishDetails = new PublishType();
		
		try
		{
			Date aDate = new Date();
			aJobDetails.setImportActivity(this.impActName);
			aJobDetails.setSourceContent(this.sourceDataFileName);
			aJobDetails.setTargetEnvironment(this.targetEnvName);
			GregorianCalendar aCal = new GregorianCalendar();
			aCal.setTime(aDate);
			XMLGregorianCalendar aTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(aCal);
			aJobDetails.setTimestamp(aTimestamp);
			StatusType aJobStatus = new StatusType();
			aJobStatus.setValue(anImportStatus);
			aJobStatus.setMessage(anImportMessage);
			aJobDetails.setStatus(aJobStatus);
			anImportResults.setImportJob(aJobDetails);
			
			StatusType aPubStat = new StatusType();
			aPubStat.setValue(aPublishStatus);
			aPubStat.setMessage(aPublishMessage);
			aPublishDetails.setStatus(aPubStat);
			anImportResults.setPublish(aPublishDetails);
		}
		catch(Exception ex)
		{
			System.err.println("Error building test response");
			ex.printStackTrace(System.err);
		}
		
		sendCallbackMessage(anImportResults);
	}

	/**
	 * Send the results of the import to the callback.
	 * @param theResults the details import results 
	 * @return an HTTP Status code indicating whether the callback message was received
	 */
	protected int sendCallbackMessage(ImportCallback theResults)
	{
		ImportResponse aCallback = new ImportResponse();
		int aCallbackStatus = aCallback.sendImportResponse(theResults, this.responseURL);
		return aCallbackStatus;
	}

	/**
	 * Create the DUP by running the specified Import Activity and once created,
	 * execute this DUP on the specified target environment in the Essential Data
	 * Management application.
	 * @return
	 */
	protected int executeDUP(ImportUtilityDataManager theAppDataManager, ImportActivityLog log, String importScript)
	{
		int aStatus = IntegrationEngine.SUCCESS_SC;
		
		// With the supplied DUP script (importScript), invoke a service on the EDM platform
		// to execute this on the selected target repository, as defined in appDataManager.
		
		DUPGenerator dupGenerator = new DUPGenerator();
		//String dupPackagePath = dupGenerator.generateDUP(itsImportActivity, importScript, servletContext.getRealPath(""));
		String xmlDupPackagePath = dupGenerator.generateXMLDUP(itsImportActivity, importScript, servletContext.getRealPath(""));
		if(xmlDupPackagePath != null) 
		{
			// We have a DUP package.			
			// Invoke the service on EDM to have the DUP executed.
			// Authenticate using the user SSO token
			
			
			String anApiUrl = itsEDMRequestPrefix + DUP_SERVICE_PREFIX + targetEnvName + DUP_SERVICE;
			HttpPost anAPIPost = new HttpPost(anApiUrl);
			// Open an InputStream to the path specified to the DUP and stream it in using an InputStreamEntity
			MultipartEntity aMultiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			aMultiPartEntity.addPart(DUP_FILE_PARAMETER_NAME, new FileBody(new File(xmlDupPackagePath)));
			//aMultiPartEntity.addTextBody(DUP_FILE_PARAMETER_NAME, theScript, ContentType.MULTIPART_FORM_DATA);
			anAPIPost.setEntity(aMultiPartEntity);
			aStatus = DUPImportJob.doImportHttpRequest(theAppDataManager, anAPIPost, itsEipApiKey, itsTenantUserSsoToken, this.getNowAsGregorian(), itsImportActivity);
			
			// Log the DUP that was posted and remove the temporary copy
			theAppDataManager.finaliseXMLImport(itsImportActivity, log, importScript, xmlDupPackagePath);

		}
		return aStatus;
	}
	
	/**
	 * Execute the task to create a DUP from the supplied XML data, using the specified import Script.
	 * @return integer code indicating success or one of the error codes defined by IntegrationEngine
	 * @see IntegrationEngine IntegrationEngine.
	 */
	private String executeXMLDUP(String theImportScript,
							  	 String theXMLData) throws TransformerException
	{
		
		// Run the transform locally. No need to use the IntegrationEngine as it assumes a Protege KB
		String sourceRepName = this.itsImportActivity.getImportActivitySourceRepository();
		String anIntegrationScript = "";
		
		// Create a transformer and do the transformation
		StringWriter anIntScript = new StringWriter();
		StreamResult aResult = new StreamResult(anIntScript);
		TransformerFactory aTransformFact = TransformerFactory.newInstance();
			
		Transformer aTransformer = aTransformFact.newTransformer(new StreamSource(theImportScript));
		aTransformer.setParameter("ExternalRepository", sourceRepName);
		aTransformer.setParameter("ExternalRepositoryDesc", XML_DUP_DESCRIPTION);
		
		aTransformer.transform(new StreamSource(theXMLData), aResult);
		anIntegrationScript = anIntScript.toString();
								
		return anIntegrationScript;
	}
}
