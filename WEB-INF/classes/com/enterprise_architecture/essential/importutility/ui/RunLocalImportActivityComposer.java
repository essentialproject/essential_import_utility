/**
 * Copyright (c)2009-2014 Enterprise Architecture Solutions Ltd.
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
 * 02.05.2014	JWC	Migrated to new data manager methods
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivityLog;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SpreadsheetImportSpecScript;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprise_architecture.essential.importutility.integration.EssentialViewerPublisher;
import com.enterprise_architecture.essential.importutility.integration.ProjectLoadException;
import com.enterprise_architecture.essential.importutility.integration.ProtegeIntegrationManager;
import com.enterprise_architecture.essential.importutility.utils.EssentialServletContext;
import com.enterprise_architecture.essential.integration.core.IntegrationEngineClient;

import edu.stanford.smi.protege.model.Project;

/**
 * This class manages the modal window for testing Excel Imports against a locally cached Essential repositories
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class RunLocalImportActivityComposer extends EssentialImportInterface implements IntegrationEngineClient {
	
	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.integration.core.IntegrationEngineClient#updateProgress(java.lang.String, int)
	 */


	public Window runLocalImportWindow;
	
	public Label impActNameLbl;
	public Label impActDescLbl;
	public Label impActLastModLbl;
	public Label impActLastTestLbl;
	public Label impActLastRunLbl;
	
	public Label targetEnvNameLbl;
	
	public Textbox projectPathTxtBox;
	public Textbox liveSourceTxtBox;
	public Label runImportLabel;
	public Button runImportBtn;
	public Button downloadImportScriptBtn;
	public Label downloadRepLabel;
	public Button downloadRepBtn;
	public Label publishRepLabel;
	public Button publishRepBtn;
	public Button openViewerBtn;
	public Button downloadMessagesBtn;
	public Textbox messagesTxtBox;
	public Timer importStatusTimer;
	
	private ImportUtilityDataManager dataManager;
	private ImportSpecDataManager impSpecDataManager;
	private ImportEnvironment currentTargetEnv;
	private ProtegeIntegrationManager protegeManager;
	private String sourceRepName;
	private SpreadsheetImportSpecScript importSpec;
	private User currentUser;
	
	private static String ENABLED_LABEL_STYLE="font-weight:bold;color:black;";
	private static String DISABLED_LABEL_STYLE="font-weight:bold;color:lightgrey;";
	private ImportActivity currentImpAct;
	
	//private String messages = "";
	private int itsProgress = 0;
	private String timerProgressStatus = "";
	
	private Project currentProject;
	private ImportActivityLog log;
	private Workbook spreadsheet;
	private String currentImportScript = "";
	
	private EventQueue importEventQueue;
	
	public Window loadingDialog;


	
	public void doAfterCompose(Component comp) {
		try {
			super.doAfterCompose(comp);
			
			dataManager = this.getImportUtilityDataManager();
			this.currentImpAct = (ImportActivity) desktop.getSession().getAttribute("currentImpAct");
			this.currentTargetEnv = (ImportEnvironment) desktop.getSession().getAttribute("currentTargetEnv");
			
			if(currentTargetEnv == null) {
				this.displayError("No Target Environment Selected", "Invalid Target Environment");
				desktop.getSession().removeAttribute("currentTargetEnv");
				// JWC 02.05.2014 commented this out to match Test and Server-based live approaches 
				//runLocalImportWindow.detach();
				return;
			}
			
			
			if(currentImpAct != null) 
			{
				// JWC new version 02.05.2014
				dataManager = this.getImportUtilityDataManager();
				//ServletContext context = (ServletContext) desktop.getWebApp().getNativeContext();
				EssentialServletContext context = new EssentialServletContext((ServletContext) desktop.getWebApp().getNativeContext());
				
				impSpecDataManager = new ImportSpecDataManager(context, currentImpAct);
				
				//dataManager = this.getImportUtilityDataManager();
				//impSpecDataManager = this.getImportSpecDataManager();

				desktop.getSession().removeAttribute("currentTargetEnv");
			}
			else {
				this.displayError("No Import Activity Selected", "Invalid Import Activity");
				// JWC 02.05.2014 commented this out to match Test and Server-based live approaches 
				//runLocalImportWindow.detach();
				return;
			}
			
			
			this.currentUser = UserCredentialManager.getInstance(desktop, getContextPath()).getCurrentUser();
    		if(currentUser == null) {
    			displayError("You must be logged in to run an import", "Login Error");
    			runLocalImportWindow.detach();
    			if (isEIPMode()) {
    				execution.sendRedirect("redirectToAuthnServer.zul");
    			} else {
    				execution.sendRedirect("index.zul");
    			}
    		    return;
    		}
			
			//set the Import Activity labels
			this.impActNameLbl.setValue(this.currentImpAct.getImportActivityName());
			this.impActDescLbl.setValue(this.currentImpAct.getImportActivityDescription());
			
			//Display the modified date for the Import Activity
	        XMLGregorianCalendar impActModDate = this.currentImpAct.getImportActivityModifiedDate();
	        if(impActModDate != null) {
	        	this.impActLastModLbl.setValue(formatXMLCalendar(impActModDate));
	        } else {
	        	this.impActLastModLbl.setValue("-");
	        }
	        
	        
	      //Display the last tested date for the Import Activity
	        XMLGregorianCalendar impActTestedDate = this.currentImpAct.getImportActivityTestedDate();
	        if(impActTestedDate != null) {
	        	this.impActLastTestLbl.setValue(formatXMLCalendar(impActTestedDate));
	        } else {
	        	this.impActLastTestLbl.setStyle("font-style:italic");
	        	this.impActLastTestLbl.setValue("Never");
	        }
	        
	      //Display the last executed date for the Import Activity
	        XMLGregorianCalendar impActRunDate = this.currentImpAct.getImportActivityToLiveDate();
	        if(impActRunDate != null) {
	        	this.impActLastRunLbl.setValue(formatXMLCalendar(impActRunDate));
	        } else {
	        	this.impActLastRunLbl.setStyle("font-style:italic");
	        	this.impActLastRunLbl.setValue("Never");
	        }
	        
	        
	      //set the Target Environment name label
	        String targetEnvLabel =  this.currentTargetEnv.getImportEnvironmentName() + " (" + this.currentTargetEnv.getImportEnvironmentRole() + ")";
			this.targetEnvNameLbl.setValue(targetEnvLabel);
			
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public int getItsProgress()
	{
		return itsProgress;
	}
	
	
	@Override
	public void updateProgress(String message, int progressPercentage) {
		// TODO Auto-generated method stub
		if(importEventQueue != null) {
			// itsProgress = progressPercentage;
			messages = messages + message;
			importEventQueue.publish(new Event("updateExecuteImport"));
		}
		
	}
	
	
	/**
	 * New version of this method
	 */
	public void onClick$runImportBtn() {
		//initialise the log for the import activity
		//initialise the log for the import activity
		User currentUser = UserCredentialManager.getInstance(desktop, getContextPath()).getCurrentUser();
		if(currentUser == null) {
			this.displayError("You must be logged in to run an import", "Login Error");
			if (isEIPMode()) {
				execution.sendRedirect("redirectToAuthnServer.zul");
			} else {
				execution.sendRedirect("index.zul");
			}
		    return;
		}
		
		// JWC - 02.05.2014 New version
		//Initialise a log file for the Import		
		this.log = this.dataManager.initAutoImportActivityLog(currentImpAct, currentTargetEnv, currentUser);
		
		//Get the name of the source repository from the Import Activity
		this.sourceRepName = this.currentImpAct.getImportActivitySourceRepository();
		
		//Retrieve the import spec for the Import Activity from the Import Spec Data Manager
		this.importSpec = impSpecDataManager.getSpreadsheetImportSpecScriptData();
		
		//Create a new Protege Manager instance
		this.protegeManager = getProtegeManager();
		
		//Create the Spreadsheet in memory from the file system
		try {
			this.spreadsheet = this.dataManager.getSpreadsheetForImportActivity(currentImpAct);
		} catch (InvalidFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		final Window statusDialog = this.getLoadingDialog();
		final Label loadingLabel = (Label) getLoadingDialog().getFellow("importDialogLabel");		
		
		final IntegrationEngineClient integrationListenener = this;
		
		final EventQueue eq = EventQueues.lookup("executeImportEvents"); //create a queue
		importEventQueue = eq;
		
		
        //subscribe async listeners to handle long operation
		//subscribe an asynchronous listener for import execution updates
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("initImportProject".equals(evt.getName())) {
     
            	try 
            	{
            		timerProgressStatus = "Loading Target Repository. Please Wait..."; 
                	currentProject = protegeManager.getProjectForImportEnvironment(currentTargetEnv);
            	} 
            	catch (ProjectLoadException e) 
            	{
            		displayError("Error loading Protege Project. Check target environment settings", "Project Load Error");
            	} 
            	/*catch (InvalidFormatException e) 
            	{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
            	catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
            	eq.publish(new Event("importProjectLoaded")); //notify it is done
            }
          }
        }, true); //asynchronous
        
        
        
        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("importProjectLoaded".equals(evt.getName())) {
	            	try {
	            		timerProgressStatus = "Target Repository Loaded. Initialising Import..."; 
	            		
	            	//	System.out.println("PROGRESS STATUS: " + timerProgressStatus);		
	        			loadingLabel.setValue(timerProgressStatus);
	        			
	            	}
	            	catch(Exception e) {
	            		e.printStackTrace();
	            	}
	            //	Events.sendEvent("onTimer", runLocalImportWindow, null);
	            	eq.publish(new Event("initExecuteImport"));
	            }
	          }
      }); //synchronous
		
		
		
		
		
		//subscribe an asynchronous listener for import execution updates
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("initExecuteImport".equals(evt.getName())) {
            	String importScript;
				try 
				{
					// New version 02.05.2014 JWC
					importScript = dataManager.initChunkedImportScript(currentImpAct, spreadsheet, log, importSpec, itsMessageListener);
					currentImportScript = currentImportScript + importScript;
					protegeManager.initChunkedImport(currentProject, currentTargetEnv, sourceRepName, importScript, integrationListenener);
	        		timerProgressStatus = "Import Script Initialised...";
	        		
	        		if(importEventQueue != null)
	        		{
		        		if(importSpec.importScriptComplete()) {
		            		eq.publish(new Event("endExecuteImport")); //notify it is done
		            	} else {
		            		eq.publish(new Event("executeNextImportChunk")); //notify to import another chunk
		            	}
	        		}
				} catch (InvalidFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
            }
          }
        }, true); //asynchronous
        
        
        
        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("executeNextImportChunk".equals(evt.getName())) 
	            {
	            	try 
	            	{
	            		// JWC 02.05.2014 New version
	            		if(importEventQueue != null)
	            		{
	            			eq.publish(new Event("updateProgress")); 
	            			//	Events.sendEvent("onTimer", importStatusTimer, null);
	            			if(importSpec.importScriptComplete()) 
	            			{
	            				eq.publish(new Event("endExecuteImport")); //notify it is done
	            			} 
	            			else 
	            			{
		            		eq.publish(new Event("generateImportChunk")); //notify to import another chunk
	            			}
	            		}
	        			
	            	}
	            	catch(Exception e) {
	            	//	loadingDialog.detach();
	            		e.printStackTrace();
	            	}
	       
	            }
	          }
        }); //synchronous
		
		
        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("generateImportChunk".equals(evt.getName())) {
	            	try 
	            	{
	            		// 02.05.2014 JWC new version
	            		String importScript = dataManager.getNextImportScriptChunk(currentImpAct, spreadsheet, log, importSpec, itsMessageListener);
	            		currentImportScript = currentImportScript + importScript;
	            		
	            		//	timerProgressStatus = importSpec.getCurrentStatusString() + " (" + importSpec.getPercentageComplete() + "%)";
	            		eq.publish(new Event("updateProgress")); 
	            		Event nextEvent = new Event("executeImportChunk", null, importScript);     	           
		            	eq.publish(nextEvent); //notify to import another chunk
	            	}
	            	catch(Exception e) {
	            		e.printStackTrace();
	            	}                 	
	            }
	          }
        }); //synchronous
        
        

        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("executeImportChunk".equals(evt.getName())) {
	            	try 
	            	{
	            		// JWC 02.05.2014 new version
	            		String importScript = (String) evt.getData();
	            		currentProject = protegeManager.executeChunkedImport(currentProject, currentTargetEnv, sourceRepName, importScript, integrationListenener);
	            		
	            		if(importEventQueue != null)
	            		{
		            		if(importSpec.importScriptComplete()) 
		            		{
		            			timerProgressStatus = "Finalising Import..."; 
		                    	eq.publish(new Event("updateProgress")); 
			            		eq.publish(new Event("endExecuteImport")); //notify it is done
			            	} 
		            		else 
		            		{
			            		eq.publish(new Event("updateProgress")); 
			            		eq.publish(new Event("executeNextImportChunk")); //notify to import another chunk
			            	}
	            		}
	            	}
	            	catch(Exception e) {
	            		e.printStackTrace();
	            	}
	            }
	          }
        }, true); //synchronous
        
        
      //subscribe a normal listener to show progress
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("updateProgress".equals(evt.getName())) {
            	
            	// JWC 02.05.2014 new version
            	if(importEventQueue != null)
            	{
	            	// itsProgress = importSpec.getRowsCompleted();
	            	itsProgress = Math.round(importSpec.getRowsCompleted() / importSpec.getTotalImportRows() * 100);
	            	timerProgressStatus = importSpec.getCurrentStatusString() + " (" + importSpec.getRowsCompleted() + " of " + importSpec.getTotalImportRows() + " rows completed)";
	            	//	Events.sendEvent("onTimer", importStatusTimer, null);
            	}
            }
          }
        }, true); //asynchronous
        
        
      
        //subscribe a normal listener to show the result 
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("endExecuteImport".equals(evt.getName())) {
            	protegeManager.saveProject(currentProject);
            	Events.sendEvent("onEndExecuteImport", runLocalImportWindow, null);
            	
            	importEventQueue = null;
            	EventQueues.remove("executeImportEvents");
            }
          }
        }); //synchronous
        
        try {
        	timerProgressStatus = "Loading Target Repository..."; 
    	//	System.out.println("PROGRESS STATUS: " + timerProgressStatus);		
			loadingLabel.setValue(timerProgressStatus);
			statusDialog.doHighlighted();
		}
		catch(Exception e) {
			e.printStackTrace();
		}  
      
		this.importStatusTimer.start();
        eq.publish(new Event("initImportProject")); //kick off the long operation
			

	}
	
	
	
	public void startTimer$runLocalImportWindow() {
		try {	
			this.importStatusTimer.start();
		}
		catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	
	
	public void onTimer$importStatusTimer() {
		try {		
			Label loadingLabel = (Label) getLoadingDialog().getFellow("importDialogLabel");	
			loadingLabel.setValue(timerProgressStatus);
			
			Progressmeter progressMeter = (Progressmeter) getLoadingDialog().getFellow("importProgressMeter");	
			progressMeter.setValue(itsProgress);
		}
		catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	
	
	public void onEndExecuteImport$runLocalImportWindow() {
		this.importStatusTimer.stop();
		this.getLoadingDialog().detach();
		messages = messages + "\nImport executed successfully.";
		this.messagesTxtBox.setValue(messages);
		
		//set the date of last live import
		this.currentImpAct.setImportActivityToLiveDate(this.getNowAsGregorian());
		
		this.impActLastRunLbl.setStyle(EssentialImportInterface.SET_DATE_LABEL_FORMAT);
		this.impActLastRunLbl.setValue(this.formatXMLCalendar(currentImpAct.getImportActivityToLiveDate()));
		
		// JWC 02.05.2014 new version
		// compress the repository created after the import
		dataManager.compressLocalImpEnvProject(log, this.currentTargetEnv);
		
		//save the messages output
		dataManager.saveImportMessageLog(log, this.currentImpAct, messages);
		
		//log the recent live import and save the resulting import script
		dataManager.finaliseImport(this.currentImpAct, log, this.currentImportScript);
		
		//save the config data
		dataManager.saveAppData();
		
		Component parentComponent = runLocalImportWindow.getParent();
		Events.sendEvent("onExecuted", parentComponent, null);
		
		
		this.runImportLabel.setStyle(DISABLED_LABEL_STYLE);
		downloadRepLabel.setStyle(ENABLED_LABEL_STYLE);
		downloadImportScriptBtn.setDisabled(false);
		downloadRepBtn.setDisabled(false);
		publishRepLabel.setStyle(ENABLED_LABEL_STYLE);
		publishRepBtn.setDisabled(false);
		downloadMessagesBtn.setStyle(ENABLED_LABEL_STYLE);
		downloadMessagesBtn.setDisabled(false);
	}
	
	
	
	public void onEndUnzip$runLocalImportWindow() {
		//unzip the files
		messages = messages + "\nProject files uploaded successfully.\n";
		Events.sendEvent("onMessageReceived", runLocalImportWindow, null);
		runImportLabel.setStyle(ENABLED_LABEL_STYLE);
		runImportBtn.setDisabled(false);
	}
	
	public void onProjectUpload$runLocalImportWindow() {
		//the file has been uploaded
		System.out.println("PROJECT ZIP FILE UPLOADED");
		
		//if required, update the spreadsheet associated with the Import Activity
		Media projectFile = (Media) runLocalImportWindow.getAttribute("uploadedProject");
		if (projectFile != null && (projectFile.isBinary())) {
			
			
			//save the zip file to the local cache
			this.saveProject(projectFile); 
			
		//	System.out.println("Project File Saved: " + currentTargetEnv.getImportEnvironmentRepositoryPath());
			// runLocalImportWindow.removeAttribute("uploadedProject");
		}
	}
	
	
	private void saveProject(Media thisProjectFile) {
		 /*    if (EventQueues.exists("initImportUtilityDataOp")) {
	          print("It is busy. Please wait");
	          return; //busy
	        }  */
		
			messages = messages + "\nSaving and extracting project zip file...";
    		Events.sendEvent("onMessageReceived", runLocalImportWindow, null);
			
	    	final Media projectFile = thisProjectFile;
	        final EventQueue eq = EventQueues.lookup("unzipProjectEvents"); //create a queue
	        
	        //subscribe async listener to handle long operation
	        eq.subscribe(new EventListener() {
	        	
		          public void onEvent(Event evt) {
		            if ("startUnzipProject".equals(evt.getName())) {
		            	try {
		            		//save the zip file to the local cache		            		
		        			dataManager.updateLocalImpEnvProjectZip(currentImpAct, currentTargetEnv, currentUser, projectFile);		        			
		
		        			dataManager.saveAppData();
		            	}
		            	catch(Exception e) {
		            	//	loadingDialog.detach();
		            		e.printStackTrace();
		            	}
		            	eq.publish(new Event("endUnzipProject")); //notify it is done
		            }
		          }
	        }, true); //asynchronous
	      
	        //subscribe a normal listener to show the result 
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("endUnzipProject".equals(evt.getName())) {
	            	Events.sendEvent("onEndUnzip", runLocalImportWindow, null);
	            	EventQueues.remove("unzipProjectEvents");
	            }
	          }
	        }); //synchronous
	      
	        eq.publish(new Event("startUnzipProject")); //kick off the long operation
		}
	
	
	
	public void onClick$downloadRepBtn() 
	{
		// JWC 02.05.2014 new version
		String zipFilePath = log.getLogUpdatedRepositoryPath();
		
		if(zipFilePath != null) {
			try {
				Filedownload.save(new File(zipFilePath), "zip");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	
	
	public void onClick$downloadImportScriptBtn() 
	{
		// JWC 02.05.2014 new version
		String impScriptFilePath = log.getLogImportScriptPath();
		
		if(impScriptFilePath != null) {
			try {
				Filedownload.save(new File(impScriptFilePath), "txt");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void onClick$publishRepBtn() {
		//create an EssentialViewerPublisher instance to publish the repository to the test environment
		EssentialViewerPublisher publisher = new EssentialViewerPublisher(this.currentProject, this.currentTargetEnv);
		if(publisher.generateReport()) {
			messages = messages + "\n" + EssentialViewerPublisher.SUCCESSFUL_GENERATION;
			this.messagesTxtBox.setValue(messages);
			
			//send the generated XML file to the viewing environment
			publisher.sendReportXML();
			
			//enable the open viewer button
			openViewerBtn.setDisabled(false);
		} else {
			messages = messages + "\n" + EssentialViewerPublisher.FAILED_GENERATION_MESSAGE;
			this.messagesTxtBox.setValue(messages);
		}
	}
	
	
	
	public void onClick$openViewerBtn() {
		//open the viewer in a new browser window
		Executions.getCurrent().sendRedirect(this.currentTargetEnv.getImportEnvironmentViewerURL(), "_blank");
	}
	
	public void onClick$downloadMessagesBtn() 
	{
		// JWC 02.05.2014 new version
		String messageLogFilePath = log.getLogOutputLogPath();
		
		if(messageLogFilePath != null) {
			try {
				Filedownload.save(new File(messageLogFilePath), "txt");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void onClick$closeBtn() {
		//clear up any attributes and then close the dialog
		// JWC 02.05.2014 new version
		this.finaliseImportActivity();
		runLocalImportWindow.detach();
	}
	
	
	public void onClose$runLocalImportWindow() {
		//clear up any attributes and then close the dialog
		runLocalImportWindow.removeAttribute("uploadedProject");
		this.finaliseImportActivity();
	}
	
	/** Private method to tidy up when closing the window
	 * 
	 */
	private void finaliseImportActivity()
	{
		// JWC 02.05.2014
		// Tell the data manager to close down the import activity
		dataManager.closeCurrentImportActivity();
		
		// clear up any attributes and then close the dialog
		dataManager = null;
		impSpecDataManager = null;
		currentTargetEnv = null;
		protegeManager = null;
		sourceRepName = null;
		importSpec = null;
		currentImpAct = null;	
		messages = "";
		itsProgress = 0;
		timerProgressStatus = "";
		currentProject = null;
		importEventQueue = null;
	}

	
	public void onMessageReceived$runLocalImportWindow(Event e) {
		this.messagesTxtBox.setValue(messages);
	}
	
	private Window getLoadingDialog() {
		try {
			if(this.loadingDialog == null) {
				this.loadingDialog = (Window) Executions.createComponents("/import_dialog.zul", this.runLocalImportWindow, null);
				loadingDialog.doModal();
			}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}   
    	return this.loadingDialog;
	}
	

}
