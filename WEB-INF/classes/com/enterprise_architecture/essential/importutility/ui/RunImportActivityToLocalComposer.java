/**
 * Copyright (c)2009-2018 Enterprise Architecture Solutions Ltd.
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
 * 23.08.2018	JWC Upgrade to ZK 8.5, remove ZSS
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.File;
//import java.util.HashMap;
//import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SpreadsheetImportSpecScript;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprise_architecture.essential.importutility.integration.ProtegeIntegrationManager;
import com.enterprise_architecture.essential.integration.core.IntegrationEngineClient;

/**
 * This class manages the modal window for running Excel Imports against local Essential repositories
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class RunImportActivityToLocalComposer extends EssentialImportInterface implements IntegrationEngineClient {
	
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
	public Label runImportLabel;
	public Button runImportBtn;
	public Button downloadImportScriptBtn;
	public Label downloadRepLabel;
	public Button downloadRepBtn;
	public Label publishRepLabel;
	public Button publishRepBtn;
	public Textbox messagesTxtBox;
	public Button downloadMessagesBtn;
	
	private ImportUtilityDataManager dataManager;
	private ImportSpecDataManager impSpecDataManager;
	private ImportEnvironment currentTargetEnv;
	
	private static String ENABLED_LABEL_STYLE="font-weight:bold;color:black;";
	private static String DISABLED_LABEL_STYLE="font-weight:bold;color:lightgrey;";
	private ImportActivity currentImpAct;
	
	private String messages = "";
	private int itsProgress = 0;
	
	private EventQueue importEventQueue;


	
	public void doAfterCompose(Component comp) {
		try {
			super.doAfterCompose(comp);
			
			dataManager = this.getImportUtilityDataManager();
			this.currentImpAct = (ImportActivity) desktop.getSession().getAttribute("currentImpAct");
			this.currentTargetEnv = (ImportEnvironment) desktop.getSession().getAttribute("currentTargetEnv");
			
			if(currentTargetEnv == null) {
				this.displayError("No Target Environment Selected", "Invalid Target Environment");
				desktop.getSession().removeAttribute("currentTargetEnv");
				return;
				//runLocalImportWindow.detach();
			}
			
			
			if(currentImpAct != null) {
				dataManager = this.getImportUtilityDataManager();
				impSpecDataManager = this.getImportSpecDataManager();

				desktop.getSession().removeAttribute("currentTargetEnv");
			}
			else {
				this.displayError("No Import Activity Selected", "Invalid Import Activity");
				return;
				//runLocalImportWindow.detach();
			}
			
			//set the Import Activity labels
			this.impActNameLbl.setValue(this.currentImpAct.getImportActivityName());
			this.impActDescLbl.setValue(this.currentImpAct.getImportActivityDescription());
			
			//Display the modified date for the Import Activity
	        XMLGregorianCalendar impActModDate = this.currentImpAct.getImportActivityModifiedDate();
	        if(impActModDate != null) {
	        	this.impActLastModLbl.setValue(impActModDate.toString());
	        } else {
	        	this.impActLastModLbl.setValue("-");
	        }
	        
	        
	      //Display the last tested date for the Import Activity
	        XMLGregorianCalendar impActTestedDate = this.currentImpAct.getImportActivityTestedDate();
	        if(impActTestedDate != null) {
	        	this.impActLastTestLbl.setValue(impActTestedDate.toString());
	        } else {
	        	this.impActLastTestLbl.setStyle("font-style:italic");
	        	this.impActLastTestLbl.setValue("Never");
	        }
	        
	      //Display the last executed date for the Import Activity
	        XMLGregorianCalendar impActRunDate = this.currentImpAct.getImportActivityToLiveDate();
	        if(impActRunDate != null) {
	        	this.impActLastTestLbl.setValue(impActRunDate.toString());
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
			itsProgress = progressPercentage;
			messages = messages + message;
			importEventQueue.publish(new Event("updateExecuteImport"));
		}
		
	}
	
	
	
	public void onClick$runImportBtn() {
		messages = messages + "\nGenerating import script...\n";
		this.messagesTxtBox.setValue(messages);	

		final String sourceRepName = this.currentImpAct.getImportActivitySourceRepository();
		SpreadsheetImportSpecScript importSpec = this.impSpecDataManager.getSpreadsheetImportSpecScriptData();
		
		// Commented out 17.09.2014 JWC as causing compile fail
		//final String importScript = dataManager.generateImportScript(importSpec, this.currentImpAct);
		// Replaced with empty string 17.09.2014
		final String importScript = "";
		
		if(importScript != null && importScript.length() > 0) {
			messages = messages + "\nImport script generated.\n\nExecuting Import...\n\n";
			this.messagesTxtBox.setValue(messages);
			
		//	System.out.println(importScript);
			
			final IntegrationEngineClient integrationListenener = this;
			final EventQueue eq = EventQueues.lookup("executeImportEvents"); //create a queue
			importEventQueue = eq;
	        //subscribe async listener to handle long operation
	        eq.subscribe(new EventListener() {
	        	
		          public void onEvent(Event evt) {
		            if ("startExecuteImport".equals(evt.getName())) {
		            	try {
		            		//save the zip file to the local cache
		     
		            		ProtegeIntegrationManager protegeManager =  getProtegeManager();
		        			protegeManager.executeImport(currentTargetEnv, sourceRepName, importScript, integrationListenener);   			
		        			dataManager.compressLocalImpEnvProject(currentTargetEnv);
		        			
		        			dataManager.saveAppData();
		            	}
		            	catch(Exception e) {
		            	//	loadingDialog.detach();
		            		e.printStackTrace();
		            	}
		            	eq.publish(new Event("endExecuteImport")); //notify it is done
		            }
		          }
	        }, true); //asynchronous
	        
	        
	      //subscribe an asynchronous listener for import execution updates
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("updateExecuteImport".equals(evt.getName())) {
	            //	Events.sendEvent("onMessageReceived", runLocalImportWindow, null);
	            }
	          }
	        }, true); //asynchronous
	      
	        //subscribe a normal listener to show the result 
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("endExecuteImport".equals(evt.getName())) {
	            	Events.sendEvent("onEndExecuteImport", runLocalImportWindow, null);
	            	
	            	importEventQueue = null;
	            	EventQueues.remove("executeImportEvents");
	            }
	          }
	        }); //synchronous
	      
	        eq.publish(new Event("startExecuteImport")); //kick off the long operation
			
		}
	}
	
	
	
	
	public void onEndExecuteImport$runLocalImportWindow() {
		messages = messages + "\n\nImport executed successfully.";
		this.messagesTxtBox.setValue(messages);
		
		//save the messages output
		dataManager.saveImportMessageLog(messages);
		
		
		
		downloadRepLabel.setStyle(ENABLED_LABEL_STYLE);
		downloadImportScriptBtn.setDisabled(false);
		downloadRepBtn.setDisabled(false);
		publishRepLabel.setStyle(ENABLED_LABEL_STYLE);
		publishRepBtn.setDisabled(false);
		downloadMessagesBtn.setStyle(ENABLED_LABEL_STYLE);
		downloadMessagesBtn.setDisabled(false);
	}
	
	
	public void onClick$downloadRepBtn() {
		String zipFilePath = dataManager.getUpdatedProjectZipPath();
		if(zipFilePath != null) {
			try {
				Filedownload.save(new File(zipFilePath), "zip");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void onClick$downloadImportScriptBtn() {
		String impScriptFilePath = dataManager.getCurrentImportScriptPath();
		if(impScriptFilePath != null) {
			try {
				Filedownload.save(new File(impScriptFilePath), "txt");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onClick$downloadMessagesBtn() {
		String messageLogFilePath = dataManager.getCurrentMessageLogPath();
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
		runLocalImportWindow.removeAttribute("uploadedProject");
		runLocalImportWindow.detach();
	}
	
	
	public void onClose$runLocalImportWindow() {
		//clear up any attributes and then close the dialog
		runLocalImportWindow.removeAttribute("uploadedProject");
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
		            		User currentUser = UserCredentialManager.getInstance(desktop, getContextPath()).getCurrentUser();
		            		if(currentUser == null) {
		            			displayError("You must be logged in to run an import", "Login Error");
		            			if (isEIPMode()) {
		            				execution.sendRedirect("redirectToAuthnServer.zul");
		            			} else {
		            				execution.sendRedirect("index.zul");
		            			}
		            		    return;
		            		}
		        			dataManager.updateLocalImpEnvProjectZip(currentImpAct, currentTargetEnv, currentUser, projectFile);		        			
		        	//		dataManager.decompressLocalImpEnvProjectZip(currentTargetEnv, projectFile.getName());
		
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

	
	public void onMessageReceived$runLocalImportWindow(Event e) {
		this.messagesTxtBox.setValue(messages);
	}
	

}
