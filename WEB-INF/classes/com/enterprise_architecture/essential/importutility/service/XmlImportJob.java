/**
 	* Copyright (c)2017 Enterprise Architecture Solutions ltd.
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
	* Runnable class to execute each XML import job.
	* 09.03.2017	JWC	Version 1.0 started 
 */
package com.enterprise_architecture.essential.importutility.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivityLog;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.data.user.UserDataManager;
import com.enterprise_architecture.essential.importutility.integration.EssentialViewerPublisher;
import com.enterprise_architecture.essential.importutility.integration.ProjectLoadException;
import com.enterprise_architecture.essential.importutility.integration.ProtegeIntegrationManager;
import com.enterprise_architecture.essential.importutility.service.importcallback.ImportCallback;
import com.enterprise_architecture.essential.importutility.service.importcallback.ImportJobType;
import com.enterprise_architecture.essential.importutility.service.importcallback.PublishType;
import com.enterprise_architecture.essential.importutility.service.importcallback.StatusType;
import com.enterprise_architecture.essential.importutility.ui.EssentialImportInterface;
import com.enterprise_architecture.essential.importutility.utils.Log;
import com.enterprise_architecture.essential.integration.core.IntegrationEngine;
import com.enterprise_architecture.essential.integration.core.IntegrationEngineClient;

import edu.stanford.smi.protege.model.Project;

/**
 * Class to manage an XML import task. Implements the runnable interface to allow it to be queued by 
 * a thread Executor object. Once the object has been initialised, the job is executed via the #run method
 *
 * @author Jonathan W. Carter
 *
 */
public class XmlImportJob extends EssentialImportInterface implements Runnable,	IntegrationEngineClient 
{
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();

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
	 * The flag to indicate whether the import repository should be published to Viewer
	 */
	private boolean isPublishAfterImport = true;
	
	/**
	 * The ServletContext for the ImportService
	 */
	private ServletContext servletContext = null;
	
	/**
	 * The User ID for using the Import Utility
	 */
	private String userID = "";
	
	/**
	 * The password for the user
	 */
	private String password = "";
		
	/**
	 * Member to hold the code return by the Protege Integration Manager
	 */
	private int importReturnCode = 0;
	
		
	/**
	 * Member to manage the import activity that is being used by the Job
	 */
	private ImportActivity itsImportActivity = null;
	
	/**
	 * Member to manage the import target environment that is being used by the Job
	 */
	private ImportEnvironment itsTargetEnvironment = null;
	
	
	/**
	 * Member to manage the import user that is being used by the Job
	 */
	private User itsUser = null;
	
	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Test the received parameters and store any references to the worker objects, e.g. target environments,
	 * import activities, etc.
	 * @return integer code indicating success or one of the error codes defined by XmlImportServiceImpl.
	 * @see XmlImportServiceImpl XmlImportServiceImpl
	 */
	public int build()
	{
		int aStatusCode = XmlImportServiceImpl.JOB_ACCEPTED;
		
		//Get the application level Import Utility Data Manager
		ImportUtilityDataManager appDataManager = this.getImportUtilityDataManager(this.servletContext);
		
		// If we have found the data manager, do builds and checks
		if(appDataManager != null) 
		{
			//Get the User for the job
			UserDataManager userManager = this.getUserDataManager(servletContext);
			this.itsUser = userManager.getUser(this.userID, this.password);	
			if(this.itsUser == null) 
			{
				iuLog.log(Level.INFO, "JOB: INVALID USER");				
				return XmlImportServiceImpl.INVALID_USER;
			}
			
			//Get the Target Environment for the job
			this.itsTargetEnvironment = appDataManager.getImportEnvironment(this.targetEnvName);
			if(this.itsTargetEnvironment == null) 
			{
				iuLog.log(Level.INFO, "JOB: INVALID TARGET ENVIRONMENT");
				return XmlImportServiceImpl.INVALID_TARGET_ENVIRONMENT;
			}
					
			//Get the Import Activity for the job
			this.itsImportActivity = appDataManager.getImportActivity(this.impActName);
			if(this.itsImportActivity == null) 
			{
				iuLog.log(Level.INFO, "JOB: INVALID IMPORT ACTIVITY");			
				return XmlImportServiceImpl.INVALID_IMPORT_ACTIVITY;
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
	 * Execute the import task.
	 * @see java.lang.Runnable#run()
	 * 
	 */
	@Override
	public void run() 
	{	
		iuLog.log(Level.INFO, "Starting XML Import Job...");
		
		String anImportStatus = "success";
		String anImportMessage = "";
		
		String aPublishStatus;
		String aPublishMessage = "";
		
		if(this.isPublishAfterImport) 
		{
			aPublishStatus = "success";
		} 
		else 
		{
			aPublishStatus = "not requested";
		}
					
		//Get the Import Utility Data Manager singleton
		ImportUtilityDataManager appDataManager = this.getImportUtilityDataManager(this.servletContext);
		
		//Create a new Import Spec Data Manager
		ImportSpecDataManager impSpecDataManager = new ImportSpecDataManager(this.servletContext, this.itsImportActivity);
		
		//Retrieve the import spec file path for the Import Activity from the Import Spec Data Manager
		String importScript = impSpecDataManager.getXMLImportSpecScript(itsImportActivity);
		
		//Create a new Import Activity Log
		ImportActivityLog log = appDataManager.initAutoImportActivityLog(itsImportActivity, itsTargetEnvironment, itsUser);
		
		//Get the role (i.e. Dev, QA, Prod) of the target environment.
		String targetEnvRole = this.itsTargetEnvironment.getImportEnvironmentRole();
		
		// Transform the source data with the importSpec XSL to get the Python script.
		// and run it, via the EssentialIntegrationEngine.
		// Set the include path to the location of this activity
		String anIncludeLocalPath = itsImportActivity.getImportActivityRootDirName();
		String anIncludePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + anIncludeLocalPath);
				
		//Execute the import against a DEV or QA environment (which is local by definition)
		if(targetEnvRole.equals(ImportUtilityDataManager.IMPORT_ENV_DEV_ROLE) || 
		   targetEnvRole.equals(ImportUtilityDataManager.IMPORT_ENV_QA_ROLE))  
		{
			importReturnCode = this.executeDevQAAutoImport(appDataManager, log, importScript, sourceDataFileName, anIncludePath);
		}
		
		//Execute the import against a local or server-based production environment
		if(targetEnvRole.equals(ImportUtilityDataManager.IMPORT_ENV_PROD_ROLE))  
		{
			importReturnCode = this.executeProdAutoImport(appDataManager, log, importScript, sourceDataFileName, anIncludePath);
		}
		
		switch(importReturnCode)
		{
			case IntegrationEngine.SCRIPT_EXCEPTION_SC :
				anImportStatus = "exception";
				anImportMessage = "Import script exception";
				if(this.isPublishAfterImport) {
					aPublishStatus = "exception";
					aPublishMessage = "Import failed";
				}	
				break;
			case IntegrationEngine.NO_EXTERNAL_REPOSITORY_NAME_SC :		
				anImportStatus = "exception";
				anImportMessage = "No Source Repository defined";
				if(this.isPublishAfterImport) {
					aPublishStatus = "exception";
					aPublishMessage = "Import failed";
				}	
				break;
			case IntegrationEngine.NO_SOURCE_DATA_SC :
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				if(this.isPublishAfterImport) {
					aPublishStatus = "exception";
					aPublishMessage = "Import failed";
				}	
				break;
			case IntegrationEngine.NO_SOURCE_INTEGRATION_SC :
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				if(this.isPublishAfterImport) {
					aPublishStatus = "exception";
					aPublishMessage = "Import failed";
				}	
				break;
			case IntegrationEngine.NO_TRANSFORM_SC :
				anImportStatus = "exception";
				anImportMessage = "Empty Import Script";
				if(this.isPublishAfterImport) {
					aPublishStatus = "exception";
					aPublishMessage = "Import failed";
				}	
				break;
			case IntegrationEngine.NO_INCLUDES_SC :
				anImportStatus = "exception";
				anImportMessage = "Internal Error - Included functions not found";
				if(this.isPublishAfterImport) {
					aPublishStatus = "exception";
					aPublishMessage = "Import failed";
				}	
				break;
			case EssentialViewerPublisher.FAILED_GENERATION :
				anImportStatus = "success";
				anImportMessage = "Import Successful";
				aPublishStatus = "exception";
				aPublishMessage = "Failed to generate XML to publish";
				break;
			case EssentialViewerPublisher.PUBLISH_FAIL :
				anImportStatus = "success";
				anImportMessage = "Import Successful";
				aPublishStatus = "exception";
				aPublishMessage = "XML Generated. Failed to send to viewer";
				break;
			case EssentialViewerPublisher.PUBLISH_SUCCESS :
				anImportStatus = "success";
				anImportMessage = "Import successful";
				aPublishStatus = "success";
				aPublishMessage = "Publish successful";
				break;
			default:
				anImportStatus = "success";
				anImportMessage = "Import Successful";
				break;			
		}
		
		// Clear the service/source folder as we have logged the received XML document
		File aSourceXMLDoc = new File(sourceDataFileName);
		iuLog.log(Level.FINE, "XML Import complete. Deleting received source XML: " + sourceDataFileName);
		aSourceXMLDoc.delete();
		
		//Send the callback message
		this.constructCallbackMessage(anImportStatus, anImportMessage, aPublishStatus, aPublishMessage);
		// FINISHED RUNNING THE IMPORT
		
		
	}
	

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.integration.core.IntegrationEngineClient#updateProgress(java.lang.String, int)
	 */
	@Override
	public void updateProgress(String theMessages, int arg1) 
	{
		messages = messages + theMessages;
	}

	/**
	 * Construct and send a callback message containing the given Import Status and Message.
	 */
	protected void constructCallbackMessage(String anImportStatus, String anImportMessage, String aPublishStatus, String aPublishMessage) 
	{		
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
			String aPublishRequest = this.isPublishAfterImport ? "yes" : "no";
			aPublishDetails.setRequested(aPublishRequest);
			anImportResults.setPublish(aPublishDetails);
		}
		catch(Exception ex)
		{
			iuLog.log(Level.SEVERE, "Error building test response", ex);			
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
	 * Publish a project to the Viewer of the target environment
	 * @param aProject a project to be published 
	 * @return a return code indicating whether the project was successfully published
	 */
	protected int publishAutoImportToViewer(Project aProject) 
	{
		EssentialViewerPublisher publisher = new EssentialViewerPublisher(aProject, this.itsTargetEnvironment);
		if(publisher.generateReport()) 
		{
			//send the generated XML file to the Viewer of the target environment
			int pubishReturnCode = publisher.sendReportXML();
			if(pubishReturnCode == EssentialViewerPublisher.PUBLISH_SUCCESS) 
			{
				messages = messages + "\n" + EssentialViewerPublisher.SUCCESS_MESSAGE;
			} 
			else 
			{
				messages = messages + "\n" + EssentialViewerPublisher.FAILED_SEND;
				pubishReturnCode = EssentialViewerPublisher.PUBLISH_FAIL;
			}		
			return pubishReturnCode;
		} 
		else 
		{
			messages = messages + "\n" + EssentialViewerPublisher.FAILED_GENERATION;
			return EssentialViewerPublisher.FAILED_GENERATION;
		}
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
	 * @return the isPublishAfterImport
	 */
	public boolean isPublishAfterImport() {
		return isPublishAfterImport;
	}

	/**
	 * @param isPublishAfterImport the isPublishAfterImport to set
	 */
	public void setPublishAfterImport(boolean isPublishAfterImport) {
		this.isPublishAfterImport = isPublishAfterImport;
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
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Execute the import task against a Dev or QA Target Environment.
	 * @return integer code indicating success or one of the error codes defined by IntegrationEngine
	 * @see IntegrationEngine IntegrationEngine.
	 */
	private int executeDevQAAutoImport(ImportUtilityDataManager appDataManager, 
									   ImportActivityLog log, 
									   String importScript,
									   String theXMLData,
									   String theIncludePath) 
	{
		int returnCode = IntegrationEngine.SUCCESS_SC;
		Project updatedProject = null;
		
		try 
		{
			//Temporarily cache the original project files
			String cachedProjectFilePath = appDataManager.cacheLocalImpEnvProject(itsTargetEnvironment);
			
			//Execute the Import Script against the target Dev/QA environment and save the resulting project
		    String sourceRepName = this.itsImportActivity.getImportActivitySourceRepository();
			ProtegeIntegrationManager protegeManager = getProtegeManager(this.servletContext);
    		updatedProject = protegeManager.executeXMLImport(this.itsTargetEnvironment, 
					 										 sourceRepName, 
					 										 importScript,
					 										 theXMLData,
					 										 theIncludePath,
					 										 this);
    		returnCode = protegeManager.getIntegrationReturnCode();
    		
    		protegeManager.saveProject(updatedProject);
    		
    		//If requested, publish the resulting project to the viewer of the target environment
    		if(this.isPublishAfterImport) 
    		{
    			returnCode = this.publishAutoImportToViewer(updatedProject);
    		}
    		
    		//Save any messages returned by the Integration Engine to the log
    		appDataManager.saveImportMessageLog(log, this.itsImportActivity, messages);
    		
    		//Save a zipped copy of the project files to the log
    		appDataManager.compressLocalImpEnvProject(log, this.itsTargetEnvironment);
    		
    		//Set the last tested date against the Import Activity
    		this.itsImportActivity.setImportActivityTestedDate(this.getNowAsGregorian());
		
    		//Clean up and finalise the import log
    		appDataManager.finaliseXMLImport(this.itsImportActivity, log, protegeManager.getItsImportScript());
    		
    		//Save the record of the import
    		appDataManager.saveAppData();
    		
    		//Reset the Dev/QA project files to their original state - if it was cached
    		if(cachedProjectFilePath != null)
    		{
    			appDataManager.uncacheLocalImpEnvProject(itsTargetEnvironment, cachedProjectFilePath);
    		}
    		
    		//Save memory by removing the reference to the project
    		// Done in the finally block
    		
		} 
		catch (ProjectLoadException e) 
		{
			//Return an error code if the Project fails to load
			//e.printStackTrace();
			iuLog.log(Level.WARNING, "Project load exception", e);
			return IntegrationEngine.SCRIPT_EXCEPTION_SC;
    	} 
		finally 
		{
    		//Save memory by removing the reference to the project
    		if(updatedProject != null) 
    		{
    			updatedProject.dispose();
    			updatedProject = null;
    		}
    	}
		//Return the code that resulted from the Integration being executed
		return  returnCode;
	}
	
	
	/**
	 * Execute the import task against a local or server-based Target Environment.
	 * @return integer code indicating success or one of the error codes defined by IntegrationEngine
	 * @see IntegrationEngine IntegrationEngine.
	 */
	private int executeProdAutoImport(ImportUtilityDataManager appDataManager, 
									  ImportActivityLog log, 
									  String importScript,
									  String theXMLData,
									  String theIncludePath) 
	{
		int returnCode = IntegrationEngine.SUCCESS_SC;
		Project updatedProject = null;
		try 
		{
			//Execute the Import Script against the target production environment and save the resulting project
		    String sourceRepName = this.itsImportActivity.getImportActivitySourceRepository();
			ProtegeIntegrationManager protegeManager = getProtegeManager(this.servletContext);
    		updatedProject = protegeManager.executeXMLImport(this.itsTargetEnvironment, 
    														 sourceRepName, 
    														 importScript,
    														 theXMLData,
    														 theIncludePath,
    														 this);
    		returnCode = protegeManager.getIntegrationReturnCode();
    		protegeManager.saveProject(updatedProject);	
    		
    		//If requested, publish the resulting project to the viewer of the target environment
    		if(this.isPublishAfterImport) 
    		{
    			returnCode = this.publishAutoImportToViewer(updatedProject);
    		}
    		
    		//Save any messages returned by the Integration Engine to the log
    		appDataManager.saveImportMessageLog(log, this.itsImportActivity, messages);
    		
    		//Save the zipped project files to the log
    		appDataManager.compressLocalImpEnvProject(log, this.itsTargetEnvironment);
    		
    		//Set the last executed date against the Import Activity
    		this.itsImportActivity.setImportActivityToLiveDate(this.getNowAsGregorian());
    		
    		//Clean up and finalise the import log
    		appDataManager.finaliseXMLImport(this.itsImportActivity, log, importScript);
    		
    		//Save the record of the import
    		appDataManager.saveAppData();
    		
		} 
		catch (ProjectLoadException e) 
		{
			//Return an error code if the Project fails to load
			//e.printStackTrace();
			iuLog.log(Level.WARNING, "Project load exception", e);
			return IntegrationEngine.SCRIPT_EXCEPTION_SC;
    	} 
		finally 
		{
    		//Save memory by removing the reference to the project
    		if(updatedProject != null) 
    		{
    			updatedProject.dispose();
    			updatedProject = null;
    		}
    	}
		//Return the code that resulted from the Integration being executed
		return  returnCode;
	}
	

}
