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
	* 22.03.2017	JWC	Version 1.0 started
	*  
 */
package com.enterprise_architecture.essential.importutility.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.servlet.ServletContext;

/**
 * Class to implement the XML DUP service. Using the controller pattern, validates the request parameters
 * where possible, creates a task for the requested import and sends it to the thread Executor to have it 
 * executed.
 * 
 * @author Jonathan Carter
 */
public class XmlDUPImportServiceImpl 
{
	/**
	 * Return code for successfully accepted new import job
	 */
	public static final int JOB_ACCEPTED = 200;
	
	/**
	 * Return code indicating that the import job could not be accepted as the thread pool is fully committed.
	 */
	public static final int JOB_REFUSED = 500;
	
	/**
	 * Return code indicating that the import job could not be accepted as it was null
	 */
	public static final int JOB_NULL = 510;
		
	/**
	 * Return code indicating that an invalid import activity was requested
	 */
	public static final int INVALID_IMPORT_ACTIVITY = 400;
	
	/**
	 * Return code indicating that an invalid target environment was requested
	 */
	public static final int INVALID_TARGET_ENVIRONMENT = 410;
	
	/**
	 * Return code indicating that an invalid source content file was requested
	 */
	public static final int INVALID_SOURCE_CONTENT = 420;
	
	/**
	 * Return code indicating that an invalid user ID was requested
	 */
	public static final int INVALID_USER = 430;
	
	/**
	 * ExecutorService managing a controlled threadpool and queue of tasks.
	 */
	private ExecutorService itsExecutor = null;
		
	/**
	 * Maximum number of threads in the thread pool 
	 */
	private static final int MAX_THREAD_POOL_SIZE = 1;
	

	/**
	 * Default constructor which initializes the thread pool.
	 */
	public XmlDUPImportServiceImpl() 
	{
		itsExecutor = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
	}

	/**
	 * Submit a new Import Job to the controller.
	 * Jobs are queued until a thread in which to complete the job becomes available.
	 * 
	 * @param impActName
	 * @param targetEnvName
	 * @param sourceFileName
	 * @param context
	 * @param responseURL
	 * @param IUUsername
	 * @param IUPassword
	 * @param theTenantUserSsoToken
	 * @return a status code defined by the public static values.
	 */
	public int executeImportJob(String impActName,
								String targetEnvName,
								String sourceFileName,
								ServletContext context,
								String theEipApiKey,
								String responseURL,
								String theTenantUserSsoToken)
	{
		int aReturnCode = JOB_ACCEPTED;
		
		// Validate the parameters
		
		// Create a new ImportJob.
		XmlDUPImportJob anImportJob = new XmlDUPImportJob();
		anImportJob.setImpActName(impActName);
		anImportJob.setTargetEnvName(targetEnvName);
		anImportJob.setSourceDataFileName(sourceFileName);		
		anImportJob.setResponseURL(responseURL);
		anImportJob.setServletContext(context);
		anImportJob.setItsTenantUserSsoToken(theTenantUserSsoToken);
		anImportJob.setItsEipApiKey(theEipApiKey);
		
		// Test that the parameters are valid
		aReturnCode = anImportJob.build();
		if(aReturnCode == JOB_ACCEPTED)
		{
			// Add it to the queue.
			try
			{
				itsExecutor.submit(anImportJob); 
			}
			catch(RejectedExecutionException rejectedEx)
			{
				aReturnCode = JOB_REFUSED;
			}
			catch(NullPointerException npe)
			{
				aReturnCode = JOB_NULL;
			}
		}
		
		return aReturnCode;
	}
	
	/**
	 * Terminate the service implementation and release the threadpool
	 */
	protected void shutdown()
	{
		if(itsExecutor != null)
		{
			itsExecutor.shutdown();
		}
	}
}
