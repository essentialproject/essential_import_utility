/**
	* Copyright (c)2016 Enterprise Architecture Solutions ltd.
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
	* Servlet to provide REST service interface that invokes DUPs.
	* 28.11.2016	JWC	Version 1.0 started
	* 07.12.2016	JWC Ready for first testing.
	* 19.12.2016	JWC	Version 1.0 finished
 */
package com.enterprise_architecture.essential.importutility.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivityLog;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SpreadsheetImportSpecScript;
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
 * Import Job that is responsible for managing the generation of a DUP from the specified 
 * import activity. Once the DUP has been created, it is then sent to the Essential Data Management
 * application via a REST service request for execution on the specified tenant and repository.
 * @author Jonathan Carter
 *
 */
//public class DUPImportJob extends ImportJob 
public class DUPImportJob extends EssentialImportInterface implements Runnable, IntegrationEngineClient
{
	/**
	 * The name of the import activity to run
	 */
	public static final String BATCH_MAP_NAME = "BATCH_MAP";
	
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
	 * HTTP Response code returned by EDM on successful login
	 */
	public static final int EDM_LOGIN_SUCCESS = 302;
	
	/**
	 * HTTP Response code returned by this class when login to EDM fails
	 */
	public final static int EDM_LOGIN_FAIL = 403;
	
	/**
	 * HTTP Response code returned by this class when request to EDM fails
	 */
	public final static int EDM_SERVER_SCRIPT_POST_FAILED = 500;
	
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
	 * The name of the batchID
	 */
	private String batchID = "";
		
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
	 * @return the batchID
	 */
	public String getBatchID() {
		return batchID;
	}

	/**
	 * @param batchID the batchID to set
	 */
	public void setBatchID(String batchID) {
		this.batchID = batchID;
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
	 * Default constructor
	 */
	public DUPImportJob() 
	{
		// All member variable initialised when defined.
	}
	
	/**
	 * Test the received parameters and store any references to the worker objects, e.g. target environments,
	 * import activities, etc.
	 * @return integer code indicating success or one of the error codes defined by ImportServiceImpl.
	 * @see ImportServiceImpl ImportServiceImpl
	 */
	public int build()
	{
		int aStatusCode = ImportServiceImpl.JOB_ACCEPTED;
		
		// Get the EDM request prefix from the web.xml
		itsEDMRequestPrefix = servletContext.getInitParameter(EDM_SERVICE_REQUEST_PREFIX);
		
		//Get the application level Import Utility Data Manager
		ImportUtilityDataManager appDataManager = this.getImportUtilityDataManager(this.servletContext);
		
		// If we have found the data manager, do builds and checks
		if(appDataManager != null) 
		{			
			
			//Get the Import Activity for the job
			this.itsImportActivity = appDataManager.getImportActivity(this.impActName);
			if(this.itsImportActivity == null) {
				System.out.println("JOB: INVALID IMPORT ACTIVITY");
				this.setBatchHasFailed();
				return ImportServiceImpl.INVALID_IMPORT_ACTIVITY;
			}
			
			//Update the Import Activity using the source data file
			File sourceSpreadsheetFile = new File(this.sourceDataFileName);
			if(sourceSpreadsheetFile.exists()) {
				try {
					Media spreadsheetFileMedia = new AMedia(sourceSpreadsheetFile, null, null);
					appDataManager.updateExcelImpActivitySpreadsheetFile(this.itsImportActivity, spreadsheetFileMedia);
					appDataManager.saveAppData();
				}
				catch(FileNotFoundException e) {
					System.out.println("JOB: INVALID_SOURCE_CONTENT");
					this.setBatchHasFailed();
					return ImportServiceImpl.INVALID_SOURCE_CONTENT;
				}
				catch(IOException e) {
					System.out.println("JOB: INVALID_SOURCE_CONTENT");
					this.setBatchHasFailed();
					return ImportServiceImpl.INVALID_SOURCE_CONTENT;
				}
			} else {
				System.out.println("JOB: INVALID_SOURCE_CONTENT");
				this.setBatchHasFailed();
				return ImportServiceImpl.INVALID_SOURCE_CONTENT;
			}
			
			
			
		} else {
			this.setBatchHasFailed();
			aStatusCode = ImportServiceImpl.JOB_REFUSED;
		}
		
		
		return aStatusCode;
	}
	
	/**
	 * When the parent thread executes, this method is invoked.
	 * Start the DUP Import Job. Process the supplied source content to create a DUP.
	 * When the DUP has been created, invoke the EDM application to process the DUP
	 */
	@Override
	public void run()
	{
		System.out.println("STARTING DUP JOB");
		
		String anImportStatus = "success";
		String anImportMessage = "";		
		
		if(this.batchHasFailed(this.batchID)) {
			anImportStatus = "exception";
			anImportMessage = "Previous batch job failed";
			
			this.constructCallbackMessage(anImportStatus, anImportMessage, "", "");
			return;
		}
		
		//Get the Import Utility Data Manager singleton
		ImportUtilityDataManager appDataManager = this.getImportUtilityDataManager(this.servletContext);
		
		//Create a new Import Spec Data Manager
		ImportSpecDataManager impSpecDataManager = new ImportSpecDataManager(this.servletContext, this.itsImportActivity);
		
		//Retrieve the import spec for the Import Activity from the Import Spec Data Manager
		SpreadsheetImportSpecScript importSpec = impSpecDataManager.getSpreadsheetImportSpecScriptData();
		
		//Create a new Import Activity Log		
		String anImportType = "DUP: Excel";
		ImportActivityLog log = appDataManager.initAutoImportActivityLog(itsImportActivity, anImportType, targetEnvName, itsUser);
		
		//Create the Import Script and save it to the log
		String importScript = "";
		try 
		{			
			importScript = appDataManager.generateDUPImportScript(importSpec, itsImportActivity, log, itsMessageListener);
		} 
		catch (InvalidFormatException e) 
		{
			//set the Import Status to "exception" with an appropriate message and then send an appropriate response
			this.setBatchHasFailed();
			anImportStatus = "exception";
			anImportMessage = "Incorrect source data format";
			
			this.constructCallbackMessage(anImportStatus, anImportMessage, "", "");
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			//set the Import Status to "exception" with an appropriate message and then send an appropriate response
			this.setBatchHasFailed();
			anImportStatus = "exception";
			anImportMessage = "Failed to read source data";
			
			this.constructCallbackMessage(anImportStatus, anImportMessage, "", "");
			e.printStackTrace();
		}
		
		// Execute the import of the DUP
		importReturnCode = this.executeDUP(appDataManager, log, importScript);
		switch(importReturnCode)
		{
			case IntegrationEngine.SCRIPT_EXCEPTION_SC :
				this.setBatchHasFailed();
				anImportStatus = "exception";
				anImportMessage = "Import script exception";
				break;
			case IntegrationEngine.NO_EXTERNAL_REPOSITORY_NAME_SC :
				this.setBatchHasFailed();
				anImportStatus = "exception";
				anImportMessage = "No Source Repository defined";
				break;
			case IntegrationEngine.NO_SOURCE_DATA_SC :
				this.setBatchHasFailed();
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				break;
			case IntegrationEngine.NO_SOURCE_INTEGRATION_SC :
				this.setBatchHasFailed();
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				break;
			case IntegrationEngine.NO_TRANSFORM_SC :
				this.setBatchHasFailed();
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				break;
			case IntegrationEngine.NO_INCLUDES_SC :
				this.setBatchHasFailed();
				anImportStatus = "exception";
				anImportMessage = "Internal Error - Included functions not found";	
				break;		
			case EDM_LOGIN_FAIL :
				this.setBatchHasFailed();
				anImportStatus = "exception";
				anImportMessage = "EDM Service Request Error - failed to login in to EDM with supplied credentials";
				break;
			case EDM_SERVER_SCRIPT_POST_FAILED :
				this.setBatchHasFailed();
				anImportStatus = "exception";
				anImportMessage = "EDM Service Request Error - posted import pack failed, see EDM server environment for errors";
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
	
	@Override
	public void updateProgress(String theMessages, int returnCode) {
		// TODO Auto-generated method stub
		//this.importMessages = this.importMessages + messages;
		messages = messages + theMessages;
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
		String dupPackagePath = dupGenerator.generateDUP(itsImportActivity, importScript, servletContext.getRealPath(""));
		if(dupPackagePath != null) 
		{
			// We have a DUP package.
			// Invoke the service on EDM to have the DUP executed.
			// Authenticate using the user SSO token
			
			
			String anApiUrl = itsEDMRequestPrefix + DUP_SERVICE_PREFIX + targetEnvName + DUP_SERVICE;
			HttpPost anAPIPost = new HttpPost(anApiUrl);
			// Open an InputStream to the path specified to the DUP and stream it in using an InputStreamEntity
			MultipartEntity aMultiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			aMultiPartEntity.addPart(DUP_FILE_PARAMETER_NAME, new FileBody(new File(dupPackagePath)));
			//aMultiPartEntity.addTextBody(DUP_FILE_PARAMETER_NAME, theScript, ContentType.MULTIPART_FORM_DATA);
			anAPIPost.setEntity(aMultiPartEntity);
			aStatus = DUPImportJob.doImportHttpRequest(theAppDataManager, anAPIPost, itsEipApiKey, itsTenantUserSsoToken, this.getNowAsGregorian(), itsImportActivity);
		}
		return aStatus;
	}

	/**
	 * TODO move this to a Utils class
	 * 
	 * Perform the HTTP request to EDM to perform the DUP Import
	 * @param theAppDataManager
	 * @param theHttpRequest
	 * @param theEipApiKey
	 * @param theUserId
	 * @param theNowAsGregorian
	 * @param theImportActivity
	 * @return status
	 */
	public static int doImportHttpRequest(ImportUtilityDataManager theAppDataManager, HttpEntityEnclosingRequestBase theHttpRequest, String theEipApiKey, String theBearerToken, XMLGregorianCalendar theNowAsGregorian, ImportActivity theImportActivity) {
		int aStatus = IntegrationEngine.SUCCESS_SC;
		try {
			CloseableHttpClient anHttpclient = HttpClients.createDefault();
			theHttpRequest.addHeader("Authorization", "Bearer "+theBearerToken);
			theHttpRequest.addHeader("x-api-key", theEipApiKey);
			CloseableHttpResponse anHttpResponse = anHttpclient.execute(theHttpRequest);
			try {
				HttpEntity anEntity = anHttpResponse.getEntity();
				StatusLine aResponseStatus = anHttpResponse.getStatusLine();
				
				if (aResponseStatus.getStatusCode() == HttpStatus.SC_OK) {
					/*
					 * Read the response Map
					 * EDM returns: new ResponseEntity<Map<String, String>>(response, HttpStatus.OK)
					 * Response is the initial progress log of the submitted job, which is a String delimited by <br/>
					 * We're ignoring this Job information but could be used to call back the server to get progress updates.
					 * 
					 * 
					 * Branch of EDM = Release 0.1 public preview
					 * Success - the DUP has been received and processed by the EDM application
					 * Change get the response entity and do something sensible.
					 * Build return object which contains int response code and Map of response strings.
					 * RESPONSE_Data = "data";
					 * Parameters:
					 * body the entity body 
					 * statusCode the status code
					 */
					String aPostResponse = EntityUtils.toString(anEntity);
					EntityUtils.consume(anEntity);
					iuLog.log(Level.INFO, aPostResponse);
											
					// Log the creation of this DUP.
					//set the date of last tested
					theImportActivity.setImportActivityTestedDate(theNowAsGregorian);
					
					//this.impActLastTestLbl.setStyle(EssentialImportInterface.SET_DATE_LABEL_FORMAT);
					//this.impActLastTestLbl.setValue(this.formatXMLCalendar(itsImportActivity.getImportActivityTestedDate()));
					theAppDataManager.saveAppData();
					
					// Report success
					aStatus = IntegrationEngine.SUCCESS_SC;
				} else {
					// Read any error messages
					String aPostResult = EntityUtils.toString(anEntity);
					aStatus = EDM_SERVER_SCRIPT_POST_FAILED;
					
					// Log error message
					iuLog.log(Level.SEVERE, "Error encountered posting DUP to EDM service: " + aPostResult);
				}
			} finally {
				try {
					anHttpResponse.close();
					anHttpclient.close();
				} catch(Exception e) {
					// Log that we failed to clear the settings but ignore error
					iuLog.log(Level.WARNING, "Error encountered closing the Status Response and HTTPClient: " + e.getMessage());
				}
			}
		} catch(Exception e) {
			// Log error message
			iuLog.log(Level.SEVERE, "Exception caught: error encountered posting DUP to EDM service: " + e.getMessage());
			aStatus = IntegrationEngine.SCRIPT_EXCEPTION_SC;
		}
		return aStatus;
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
			aJobDetails.setBatchId(this.batchID);
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
	 * @param aBatchId the batchId of a given job
	 */
	private boolean batchHasFailed(String aBatchId) {
		HashMap<String, String> batchMap = this.getBatchMap();
		return batchMap.containsKey(aBatchId);
	}
	
	
	/**
	 * Adds the BatchId of the current job to the list of failed batches
	 */
	private void setBatchHasFailed() {
		if(this.batchID != null && this.batchID.length() > 0) {
			HashMap<String, String> batchMap = this.getBatchMap();
			batchMap.put(batchID, batchID);
		}
	}
	
	
	/**
	 * Return the HashMap containing failed batches
	 * @param aBatchId the batchId of a given job
	 */
	private HashMap<String, String> getBatchMap() {
		HashMap<String, String> batchMap;
		batchMap = (HashMap<String, String>) this.servletContext.getAttribute(BATCH_MAP_NAME);
		if(batchMap == null) {
			batchMap = new HashMap<String, String>();
			this.servletContext.setAttribute(BATCH_MAP_NAME, batchMap);
		}
		return batchMap;
	}

}
