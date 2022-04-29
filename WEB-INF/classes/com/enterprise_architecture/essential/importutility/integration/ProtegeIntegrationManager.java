/**
 * Copyright (c)2009-2019 Enterprise Architecture Solutions Ltd.
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
 * 28.06.2011	JP	1st coding.
 * 15.05.2014	JWC Added controls for the Protege logging levels
 * 09.03.2017	JWC Added executeXMLImport()
 * 12.11.2019	JWC Add new EIP Mode functionality
 * 
 */
package com.enterprise_architecture.essential.importutility.integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;

import com.enterprise_architecture.essential.base.api.ApiResponse;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.utils.EssentialServletContext;
import com.enterprise_architecture.essential.integration.core.IntegrationEngine;
import com.enterprise_architecture.essential.integration.core.IntegrationEngineClient;
import com.enterprise_architecture.essential.metamodel.EssentialMetaClass;
import com.enterprise_architecture.essential.metamodel.EssentialMetaClassSlot;
import com.enterprise_architecture.essential.metamodel.EssentialMetaModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.util.ArchiveManager;

/**
 * This class is responsible for managing access to Protege repositories (projects)
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @author Jonathan W. Carter <jonathan.carter@e-asolutions.com>
 * @version 1.0 - 26.07.2011
 * @version 2.0 - 10.03.2017
 *
 */
public class ProtegeIntegrationManager implements IntegrationEngineClient {

	private static final String REPOSITORIES_RESOURCE = "/api/repositories/";

	private static final String METAMODEL_REQUEST_PART = "/metamodel";

	private static String ARCHIVE_COMMENT = "Archived by the Essential Import Utility";
	
	private static String PROTEGE_SYSTEM_LOGGER = "protege.system";
	
	/**
	 * Get a reference to the logger.
	 */
	//private static Logger iuLog = Log.getSystemLogger();
	
	/**
	 * Use SL4J - as per the other components of the Essential Suite
	 */
	private static final Logger itsLog = LoggerFactory.getLogger(ProtegeIntegrationManager.class);

	private String includesPath;
	//private String contextPath;
	private ServletContext servletContext;
	
	private static String INCLUDES_FOLDER_NAME = "includes";
	private List projectLoadErrors;
	private IntegrationEngine itsEngine;
	private int integrationReturnCode = IntegrationEngine.SUCCESS_SC;

	private boolean isEipMode = false;
	protected static String IU_MODE_PARAM_NAME = "iu_mode";
	protected static String EIP_IU_MODE = "eip";
	
	// 10.03.2017 JWC - String to capture the generated import script
	protected String itsImportScript;
	
	private Desktop itsDesktop;
	
	
	/**
	 * @return the itsImportScript
	 */
	public String getItsImportScript() {
		return itsImportScript;
	}


	/**
	 * @param itsImportScript the itsImportScript to set
	 */
	public void setItsImportScript(String itsImportScript) {
		this.itsImportScript = itsImportScript;
	}

	public static String PROTEGE_SYSTEM_LOG_PARAM = "protege_system_log";
	public static String PROTEGE_FILE_HANDLER_PARAM = "protege_file_handler";
	public static String PROTEGE_CONSOLE_HANDLER_PARAM = "protege_console_handler";

	/**
	 * @return the integrationReturnCode
	 */
	public int getIntegrationReturnCode() {
		return integrationReturnCode;
	}


	public ProtegeIntegrationManager() {
		super();
		this.projectLoadErrors = new ArrayList();
		
	}
	
	
	/*public ProtegeIntegrationManager(String aContextPath) {
		super();
		this.projectLoadErrors = new ArrayList();
		//this.contextPath = aContextPath;
		//includesPath = this.contextPath + INCLUDES_FOLDER_NAME;
	}*/
	
	public ProtegeIntegrationManager(WebApp aWebApp, Desktop theDesktop) {
		super();
		this.projectLoadErrors = new ArrayList();
		//this.servletContext = (ServletContext) aWebApp.getNativeContext();		
		EssentialServletContext aContext = new EssentialServletContext((ServletContext) aWebApp.getNativeContext());
		this.servletContext = aContext;		
		includesPath = this.servletContext.getRealPath(INCLUDES_FOLDER_NAME);
		isEipMode = servletContext.getInitParameter(IU_MODE_PARAM_NAME).equals(EIP_IU_MODE);
		itsDesktop = theDesktop;
		itsLog.debug("In constructor...");
		initLoggingProperties();		
	}
	
	
	public ProtegeIntegrationManager(ServletContext ctxt) {
		super();
		this.projectLoadErrors = new ArrayList();
		//this.servletContext = ctxt;
		EssentialServletContext aContext = new EssentialServletContext(ctxt);
		this.servletContext = aContext;		
		
		includesPath = this.servletContext.getRealPath(INCLUDES_FOLDER_NAME);
		isEipMode = servletContext.getInitParameter(IU_MODE_PARAM_NAME).equals(EIP_IU_MODE);
		initLoggingProperties();
	}

	public Project executeImport(ImportEnvironment targetEnvironment, String repositoryName, String importScript, IntegrationEngineClient aClient)  throws ProjectLoadException {
		String projectFilePath = this.servletContext.getRealPath(targetEnvironment.getImportEnvironmentRepositoryPath());
		this.projectLoadErrors = new ArrayList();
		
		Project aProject = this.getProjectForImportEnvironment(targetEnvironment);
		
		if(aProject != null) {
			// Create a new IntegrationEngine.
               IntegrationEngine anEngine = new IntegrationEngine(aClient, aProject.getKnowledgeBase(), null);
               
               anEngine.setItsIntegrationScriptSource(importScript);
               anEngine.setItsIncludesDir(this.includesPath);
               anEngine.setItsExternalRepositoryName(repositoryName);
               anEngine.setItsExternalRepositoryDesc("");
               anEngine.addParameter("version_number", "");             
               this.integrationReturnCode = anEngine.executeWithoutTransform();
               
              // aProject.save(projectLoadErrors);
               return aProject;
		} else {
			this.projectLoadErrors.add(0, "Error loading target repository");
			return null;
		}
	}
	
	/**
	 * Execute the import of an XML document by performing the transform defined by the Import Script
	 * @param targetEnvironment the target environment in the Import Utility to which this import will be performed
	 * @param repositoryName the name of this 'external repository' - logged by the external integration instances
	 * @param theImportTransform the XSL file describing the transform
	 * @param theXMLData the content that is to be imported
	 * @param theIncludePath the include path for the execution of the scripts
	 * @param aClient
	 * @return
	 * @throws ProjectLoadException
	 */
	public Project executeXMLImport(ImportEnvironment targetEnvironment, 
									String repositoryName, 
									String theImportTransform,
									String theXMLData,
									String theIncludePath,
									IntegrationEngineClient aClient)  throws ProjectLoadException 
	{
		//String projectFilePath = this.servletContext.getRealPath(targetEnvironment.getImportEnvironmentRepositoryPath());
		this.projectLoadErrors = new ArrayList();
		
		Project aProject = this.getProjectForImportEnvironment(targetEnvironment);
		
		if(aProject != null) 
		{
			itsLog.debug("Got Protege project: {}",targetEnvironment.getImportEnvironmentName());
			itsLog.debug("Transform: {}", theImportTransform);
			itsLog.debug("XML source: {}", theXMLData);
			itsLog.debug("Include path: {}", theIncludePath);
			
			// Create a new IntegrationEngine.
			IntegrationEngine anEngine = new IntegrationEngine(aClient, aProject.getKnowledgeBase(), null);
			anEngine.setItsTransform(theImportTransform);
			anEngine.setItsSourceData(theXMLData);
			anEngine.setItsIncludesDir(theIncludePath);
			anEngine.setItsExternalRepositoryName(repositoryName);
			anEngine.setItsExternalRepositoryDesc("");
			anEngine.addParameter("version_number", "");
			this.integrationReturnCode = anEngine.execute();
			
			// Record the script that was generated by the Integration Engine
			itsImportScript = anEngine.getItsIntegrationScript();
			
			itsLog.info("Integration executed. Return code: {}", integrationReturnCode);
			   
			// aProject.save(projectLoadErrors);
			return aProject;
		} 
		else 
		{
			this.projectLoadErrors.add(0, "Error loading target repository");
			return null;
		}
	}
	
	public void saveProject(Project aProject) 
	{
		//System.out.println("PROJECT FRAME COUNT BEFORE SAVE: " + aProject.getFrameCounts().getDirectSimpleInstanceCount());
		try
		{
			aProject.save(this.projectLoadErrors);
		}
		catch(Exception ex)
		{
			// report to console
			itsLog.error("EXCEPTION in saveProject(), saving Project repository: {}", ex.toString());
						
		}
	}
	
	public void endImport() {
		itsEngine = null;
	}
	
	
	public synchronized Project initChunkedImport(Project aProject, ImportEnvironment targetEnvironment, String repositoryName, String importScript, IntegrationEngineClient aClient) {
		this.projectLoadErrors = new ArrayList();
		
		if(aProject != null) {
			// Create a new IntegrationEngine.
				//System.out.println("PROJECT FRAME COUNT BEFORE IMPORT: " + aProject.getFrameCounts().getDirectSimpleInstanceCount());
               this.itsEngine = new IntegrationEngine(aClient, aProject.getKnowledgeBase(), null);
               
               itsEngine.setItsIntegrationScriptSource(importScript);
               itsEngine.setItsIncludesDir(this.includesPath);
               itsEngine.setItsExternalRepositoryName(repositoryName);
               itsEngine.setItsExternalRepositoryDesc("");
               itsEngine.addParameter("version_number", "");
               itsEngine.executeWithoutTransform();
               
               //System.out.println("IMPORT JOB SCRIPT: " + itsEngine.getItsScriptJob().getItsScript());
               
               return aProject;
		} else {
			this.projectLoadErrors.add(0, "Error loading target repository");
			return aProject;
		}
	}
	
	
	public synchronized Project executeChunkedImport(Project aProject, ImportEnvironment targetEnvironment, String repositoryName, String importScript, IntegrationEngineClient aClient) {
	//	this.projectLoadErrors = new ArrayList();
		
	/*	if(this.itsEngine == null) {
			this.itsEngine = new IntegrationEngine(aClient, aProject.getKnowledgeBase(), null);
			 itsEngine.setItsIntegrationScriptSource(importScript);
             itsEngine.setItsIncludesDir(this.includesPath);
             itsEngine.setItsExternalRepositoryName(repositoryName);
             itsEngine.setItsExternalRepositoryDesc("");
             itsEngine.addParameter("version_number", "");
		}  */
		
		if(aProject != null) {
			 //  System.out.println("IMPORTING CHUNK: " + importScript);
			   itsEngine.setItsIntegrationScriptSource(importScript);			   
               int importResult = itsEngine.executeWithoutTransform();
          //     System.out.println("IMPORT JOB SCRIPT: " + itsEngine.getItsScriptJob().getItsScript());
               return aProject;
		} else {
			this.projectLoadErrors.add(0, "Error loading target repository");
			return null;
		}
	}
	
	
	public boolean testProtegeConnection(ImportEnvironment targetEnvironment)  throws ProjectLoadException {
		this.getProjectForImportEnvironment(targetEnvironment);
		return (this.projectLoadErrors.size() == 0);
	}
	
	
	public HashMap<String, List<String>> getEssentialClassMap(ImportEnvironment anEnvironment)  throws ProjectLoadException {
		HashMap<String, List<String>> classMap = new HashMap<String, List<String>>();
		
		if(isEipMode)
		{
			itsLog.debug("In EIP Mode. Loading Essential Meta Model from the specified environment: {}", anEnvironment.getImportEnvironmentName());			
			// EIP Mode, so get the class map from from the EDM
			// We know we're in EIP mode, so the ImportEnvironment parameter will be an EipImportEnvironment instance
			ImportEnvironment anEipEnvironment = anEnvironment;
			classMap = getClassMapFromEDM(anEipEnvironment);
		}
		else
		{
			// Open source mode, so have a chat with Protege...
		
			String projectFilePath = this.servletContext.getRealPath(anEnvironment.getImportEnvironmentRepositoryPath());
		//	System.out.println("Opening reference project at: " + projectFilePath);
			
			Project aProject = this.getProjectForImportEnvironment(anEnvironment);
			
			if(aProject != null) {
			
				// 1. Get hold of the KnowledgeBase from the Project, 'aProject'
				KnowledgeBase aKb = aProject.getKnowledgeBase();
		
				// 2. Get the 'root' class
				Cls anEAClass = aKb.getCls("EA_Class");
		
				// 3. Get the set of concrete subclasses, this should get everything, because it's not DIRECTSubclasses
				Collection aConcreteClsList = anEAClass.getConcreteSubclasses();
		
				// If you need to get the relationship classes (although, I'd tend to think these are most likely derived)
				// Call the method on the other 2 base classes and append the results to the 'aConcreteClsList' Collection
		
				Cls anEARelation = aKb.getCls("EA_Relation");
				aConcreteClsList.addAll(anEARelation.getConcreteSubclasses());
		
				Cls anEAGraphRelation = aKb.getCls(":EA_Graph_Relation");
				aConcreteClsList.addAll(anEAGraphRelation.getConcreteSubclasses());
		
				// And you're done. The 'aConcreteClsList' contains the list of relevant Cls objects.
				// For each Cls in the list, get its name as follows:
		
				Iterator classIter = aConcreteClsList.iterator();
				Cls aCls;
				while(classIter.hasNext()) {
					aCls = (Cls) classIter.next();
					List<String> slotNameList = new ArrayList<String>();
					String aClassName = aCls.getBrowserText();
					Collection<Slot> slots = aCls.getVisibleTemplateSlots();
				//	System.out.println("Slots Found for " + aClassName + ": " + slots.size());
					Iterator<Slot> slotsIter = slots.iterator();
					Slot aSlot;
					while(slotsIter.hasNext()) {
						aSlot = slotsIter.next();
					//	System.out.println("Slot for " + aClassName + ": " + aSlot.getBrowserText());
						slotNameList.add(aSlot.getBrowserText());
					}
					//add an artificial slot name to allow definition of external ids
					slotNameList.add(ImportSpecDataManager.EXTID_SLOT_NAME);
					
					//sort the list alphabetically
					Collections.sort(slotNameList);
					
					//add the list of slot names to the Hashmap
					classMap.put(aClassName, slotNameList);
					aProject = null;
				}
			}
		}
		return classMap;
		
	} 
	
	
	public Project archiveServerProtegeProject(ImportEnvironment source, String archiveProjectPath)  throws ProjectLoadException {
		Project aProject = this.getProjectForImportEnvironment(source);
		try {
			EIUClipsFilesExportProjectPlugin anExporter = new EIUClipsFilesExportProjectPlugin();	
		
		//	String targetProjectFilePath = target.getImportEnvironmentRepositoryPath();
			
			StringBuffer sb = new StringBuffer(archiveProjectPath);
			int prefixIndex = sb.indexOf(".pprj");
			int repDirIndex = sb.lastIndexOf(File.separator);
			
			String dirPrefix = sb.substring(0, repDirIndex);
			
			String filenamePrefix = sb.substring(repDirIndex + 1, prefixIndex);
			String targetClassesFilePath = filenamePrefix + ".pont";
			String targetInstancesFilePath = filenamePrefix + ".pins";
			
			String targetClassesFileFullPath =  dirPrefix + File.separator + targetClassesFilePath;
			String targetInstancesFileFullPath =  dirPrefix + File.separator + targetInstancesFilePath;
			
			//System.out.println("NEW CLASSES PATH: " + targetClassesFileFullPath);
			itsLog.info("NEW CLASSES PATH: {}", targetClassesFileFullPath);
			//System.out.println("NEW INSTANCES PATH: " + targetInstancesFileFullPath);
			itsLog.info("NEW INSTANCES PATH: {}", targetInstancesFileFullPath);
			
			File targetClassesFile = new File(targetClassesFileFullPath);
			targetClassesFile.setWritable(true);
			targetClassesFile.createNewFile();
			
			File targetInstancesFile = new File(targetInstancesFileFullPath);
			targetInstancesFile.setWritable(true);
			targetInstancesFile.createNewFile();
		
			// String targetProjectFilePath = archiveProjectPath; //this.contextPath + targetProjectFilePath;		
			
			if(aProject != null) {
				if(anExporter.canExportToNewFormat(aProject)) {
					anExporter.setNewProjectPath(archiveProjectPath);
					anExporter.setFiles(targetClassesFilePath, targetInstancesFilePath);
					anExporter.exportProjectToNewFormat(aProject);
					anExporter.setFiles(targetClassesFileFullPath, targetInstancesFileFullPath);
					anExporter.exportProject(aProject);
				}		
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		} 
		
		return aProject;
			
	}
	
	
	public void copyProtegeProject(ImportEnvironment source, ImportEnvironment target)  throws ProjectLoadException {
		Project aProject = this.getProjectForImportEnvironment(source);
		try {
			EIUClipsFilesExportProjectPlugin anExporter = new EIUClipsFilesExportProjectPlugin();	
		
			String targetProjectFilePath = target.getImportEnvironmentRepositoryPath();
			
			StringBuffer sb = new StringBuffer(targetProjectFilePath);
			int prefixIndex = sb.indexOf(".pprj");
			int repDirIndex = sb.lastIndexOf(File.separator);
			
			String dirPrefix = sb.substring(0, repDirIndex);
			
			String filenamePrefix = sb.substring(repDirIndex + 1, prefixIndex);
			String targetClassesFilePath = filenamePrefix + ".pont";
			String targetInstancesFilePath = filenamePrefix + ".pins";
			
			String targetClassesFileFullPath =  this.servletContext.getRealPath(dirPrefix + File.separator + targetClassesFilePath);
			String targetInstancesFileFullPath =  this.servletContext.getRealPath(dirPrefix + File.separator + targetInstancesFilePath);
			
//			System.out.println("NEW CLASSES PATH: " + targetClassesFilePath);
//			System.out.println("NEW INSTANCES PATH: " + targetInstancesFilePath);
			
			File targetClassesFile = new File(targetClassesFileFullPath);
			targetClassesFile.setWritable(true);
			targetClassesFile.createNewFile();
			
			File targetInstancesFile = new File(targetInstancesFileFullPath);
			targetInstancesFile.setWritable(true);
			targetInstancesFile.createNewFile();
		
			targetProjectFilePath = this.servletContext.getRealPath(targetProjectFilePath);		
			
			if(aProject != null) {
				if(anExporter.canExportToNewFormat(aProject)) {
					anExporter.setNewProjectPath(targetProjectFilePath);
					anExporter.setFiles(targetClassesFilePath, targetInstancesFilePath);
					anExporter.exportProjectToNewFormat(aProject);
					anExporter.setFiles(targetClassesFileFullPath, targetInstancesFileFullPath);
					anExporter.exportProject(aProject);
				}		
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			aProject.dispose();
		}
			
	}
	
	
	public Project getProjectForImportEnvironment(ImportEnvironment anEnv)  throws ProjectLoadException {
		Project aProject;
		if(anEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
			//get the project from a local, file-based source repository
			if(anEnv.getImportEnvironmentRepositoryPath() != null) {
				aProject = this.getLocalProject(anEnv);
			} else {
				this.projectLoadErrors.add("No path defined for local repository file");
				return null;
			}
		} else {
			//get the project from a remote, server-based source repository
			aProject = this.getServerProject(anEnv);
		}
		return aProject;
	}
	
	//archive the given project
	public void archiveProject(Project aProject) {
		ArchiveManager anArchiveMan = ArchiveManager.getArchiveManager();
    	anArchiveMan.archive(aProject, ARCHIVE_COMMENT);
	}
	
	
	private Project getLocalProject(ImportEnvironment anEnv) {
		this.projectLoadErrors = new ArrayList();
		String projectFilePath = this.servletContext.getRealPath(anEnv.getImportEnvironmentRepositoryPath());
		Project aProject = Project.loadProjectFromFile(projectFilePath, projectLoadErrors);
		if(projectLoadErrors.size() > 0) {
			return null;
		} else {
			return aProject;
		}
	}
	
	
	
	private Project getServerProject(ImportEnvironment anEnv) throws ProjectLoadException {
		try {
			RemoteProjectManager rpm = RemoteProjectManager.getInstance();
			String hostName = anEnv.getHostname();
			String username = anEnv.getUsername();
			String password = anEnv.getPassword();
			String projectName = anEnv.getProjectName();
			Project aProject = rpm.getProject(hostName, username, password, projectName, true);
			if(aProject == null) {
				this.projectLoadErrors.add("Error loading remote server-based repository");
			}
		return aProject;
		} catch(Exception e) {
			throw new ProjectLoadException();
		}
	}
	
	
	/**
	 * @return the projectLoadErrors
	 */
	public List getProjectLoadErrors() {
		return projectLoadErrors;
	}
	

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.integration.core.IntegrationEngineClient#updateProgress(java.lang.String, int)
	 */
	@Override
	public void updateProgress(String arg0, int arg1) {
		// TODO Auto-generated method stub
		//System.out.println("IMPORT PROGRESS:" + arg0);
		itsLog.debug("IMPORT PROGRESS: {}", arg0);
	}

	/**
	 * Initialise the logging properties for the Protege engine.
	 * Normally, Protege picks these up from the file defined by the System property
	 * <pre>java.util.logging.config.file</pre>
	 * Using 3 servlet context parameters, set the logging level for:
	 * <ul>
	 * <li>protege_system_log - the Protege system logger, protege.system</li>
	 * <li>protege_file_handler - the file log handler</li>
	 * <li>protege_console_handler - the console log handler</li>
	 * </ul>
	 * These have values defined by java.util.logging.Level
	 * @see java.util.logging.Level
	 */
	protected void initLoggingProperties()
	{
		// 15.04.2014 JWC Set the protege.dir System property to get Protege to load logging.properties in 
				
		java.util.logging.Logger aProtegeSystemLogger = java.util.logging.Logger.getLogger(PROTEGE_SYSTEM_LOGGER);
		String aSystemLevel = servletContext.getInitParameter(PROTEGE_SYSTEM_LOG_PARAM);
		String aFileLevel = servletContext.getInitParameter(PROTEGE_FILE_HANDLER_PARAM);
		String aConsoleLevel = servletContext.getInitParameter(PROTEGE_CONSOLE_HANDLER_PARAM);
				
		if(aSystemLevel != null)
		{
			aProtegeSystemLogger.setLevel(Level.parse(aSystemLevel));
			
			if(aFileLevel != null)
			{
				FileHandler aFileLog;
				try 
				{
					aFileLog = new FileHandler();
					aFileLog.setLevel(Level.parse(aFileLevel));
					aProtegeSystemLogger.addHandler(aFileLog);
				} 
				catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(aConsoleLevel != null)
			{
				ConsoleHandler aConsoleLog = new ConsoleHandler();
				aConsoleLog.setLevel(Level.parse(aConsoleLevel));
				aProtegeSystemLogger.addHandler(aConsoleLog);
			}
		}
	}
	
	/**
	 * Make a request to the EDM meta model API to get a JSON structure that contains the definitions
	 * of all the classes and their slots. Note that currently, the slot types are provided by the JSON 
	 * but for compatibility are ignored by this method, by design.
	 * 
	 * @param theEnvironment
	 * @return a HashMap of the Essential Meta Model classes, each with a List of slot names associated
	 * 
	 */
	private HashMap<String, List<String>> getClassMapFromEDM(ImportEnvironment theEnvironment)
	{
		HashMap<String, List<String>> classMap = new HashMap<String, List<String>>();
		
		// Make a request to EDM to get the meta model
		itsLog.debug("Requesting Reference Meta Model: {}", theEnvironment.getImportEnvironmentName());
		itsLog.debug("Repository ID: {}", theEnvironment.getImportEnvironmentId());
		//System.out.println("ProtegeIntegrationManager.getClassMapFromEDM(). theEnvironment: " + theEnvironment.getImportEnvironmentId());
		//System.out.println("ProtegeIntegrationManager.getClassMapFromEDM(). the environment name: " + theEnvironment.getImportEnvironmentName());
		
		String anEssentialMetaModelJson = "";
		
		// Make the service request and get the JSON response here...
		String aRequestURL = REPOSITORIES_RESOURCE + theEnvironment.getImportEnvironmentId() + METAMODEL_REQUEST_PART;		
		itsLog.debug("Requesting meta model information on URL: {}", aRequestURL);
		//System.out.println("Requesting meta model information on URL: : " + aRequestURL);
		
		if(theEnvironment.getImportEnvironmentId() == null || theEnvironment.getImportEnvironmentId().isEmpty())
		{
			// If we've not defined the Environment, return empty class list
			return classMap;
		}
		
		// Call separate method to do the call etc.		
		ApiResponse aResponse = null;
		EssentialEDMAPIClient anEDMAPIClient = new EssentialEDMAPIClient(servletContext);
		try {
			Executions.activate(itsDesktop);
		} catch (DesktopUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//HttpServletRequest aRequest = (HttpServletRequest)Executions.getCurrent().getNativeRequest();
		//aResponse = anEDMAPIClient.getEssentialEDMAPI(aRequestURL, aRequest);
		aResponse = anEDMAPIClient.getEssentialEDMAPI(aRequestURL);
		Executions.deactivate(itsDesktop);
		// Process the response
		if(aResponse.getStatusCode() == HttpServletResponse.SC_OK)
		{
			anEssentialMetaModelJson = aResponse.getJsonResponse();
		}
		else
		{
			// Handle case where error response is received
			// - return null and log error, so it will try to get the class map again if reload the app
			itsLog.error("Could not get class map from EDM: {}", aResponse.getJsonResponse());
			return null;
		}
		//System.out.println("Reading received content.... ");
		// Parse the response into a Java POJO
		ObjectMapper aMapper = new ObjectMapper();
		try
		{
			EssentialMetaModel anEssentialMetaModel = aMapper.readValue(anEssentialMetaModelJson, EssentialMetaModel.class);
					
			// Read the POJO into the HashMap before we return it
			Iterator<EssentialMetaClass> aClassIterator = anEssentialMetaModel.getMetaModel().iterator();
			while(aClassIterator.hasNext())
			{
				EssentialMetaClass aCls = aClassIterator.next();
				List<String> aSlotNameList = new ArrayList<String>();
				
				// Get the name
				String aClassName = aCls.getMetaClass();
				
				// Get the slots
				Iterator<EssentialMetaClassSlot> aSlotListIt = aCls.getSlots().iterator();
				while(aSlotListIt.hasNext())
				{
					EssentialMetaClassSlot aSlot = aSlotListIt.next();
					aSlotNameList.add(aSlot.getName());
				}
				
				// add an artificial slot name to allow definition of external ids
				aSlotNameList.add(ImportSpecDataManager.EXTID_SLOT_NAME);
				
				// Sort the list 
				Collections.sort(aSlotNameList);
				
				// Add the list of slots to the HashMap entry
				classMap.put(aClassName, aSlotNameList);
			}
		}
		catch(Exception anException)
		{
			itsLog.debug("Requested Essential Meta Model for reference failed: {}", anException.getMessage());
		}
		//System.out.println("Got class map with " + classMap.size() + " elements");
		return classMap;
	}
}
