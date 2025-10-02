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
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
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
import com.enterprise_architecture.essential.importutility.data.global.PromoteRepositoryException;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SpreadsheetImportSpecScript;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprise_architecture.essential.importutility.integration.EssentialViewerPublisher;
import com.enterprise_architecture.essential.importutility.integration.ProjectLoadException;
import com.enterprise_architecture.essential.importutility.integration.ProtegeIntegrationManager;
import com.enterprise_architecture.essential.importutility.utils.EssentialServletContext;
import com.enterprise_architecture.essential.integration.core.IntegrationEngine;
import com.enterprise_architecture.essential.integration.core.IntegrationEngineClient;

import edu.stanford.smi.protege.model.Project;

/**
 * This class manages the modal window for testing Excel Imports against a locally cached Essential repositories
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class TestImportActivityComposer extends EssentialImportInterface implements IntegrationEngineClient {
	
	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.integration.core.IntegrationEngineClient#updateProgress(java.lang.String, int)
	 */


	public Window testLocalImportWindow;
	
	public Label impActNameLbl;
	public Label impActDescLbl;
	public Label impActLastModLbl;
	public Label impActLastTestLbl;
	public Label impActLastRunLbl;
	
	public Label targetEnvNameLbl;
	
	public Label refreshRepLabel;
	public Button refreshTestProjectBtn;
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
	public Button promoteToLiveRepBtn;
	public Button publishToLiveViewerBtn;
	public Button openLiveViewerBtn;
	public Timer importStatusTimer;
	
	private ImportUtilityDataManager dataManager;
	private ImportSpecDataManager impSpecDataManager;
	private ImportEnvironment currentTargetEnv;
	private ProtegeIntegrationManager protegeManager;
	private String sourceRepName;
	private SpreadsheetImportSpecScript importSpec;
	
	private static String ENABLED_LABEL_STYLE="font-weight:bold;color:black;";
	private static String DISABLED_LABEL_STYLE="font-weight:bold;color:lightgrey;";
	private ImportActivity currentImpAct;
	private String cachedLocalRepZipPath;
	
	// JWC 18.06.2014 Remove
	//private String messages = "";
	// JWC 18.06.14 remove end
	
	private int itsProgress = 0;
	private String timerProgressStatus = "";
	private boolean targetEnvLiveIsLocal = true;
	private ImportEnvironment sourceEnv;
	
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
				return;
				//testLocalImportWindow.detach();
			}
			
			
			if(currentImpAct != null) {
				dataManager = this.getImportUtilityDataManager();
				//ServletContext context = (ServletContext) desktop.getWebApp().getNativeContext();
				EssentialServletContext context = new EssentialServletContext((ServletContext) desktop.getWebApp().getNativeContext());
				
				impSpecDataManager = new ImportSpecDataManager(context, currentImpAct);
				
				//impSpecDataManager = this.getImportSpecDataManager();

				desktop.getSession().removeAttribute("currentTargetEnv");
			}
			else {
				this.displayError("No Import Activity Selected", "Invalid Import Activity");
				return;
				//testLocalImportWindow.detach();
			}
			
			this.sourceEnv = this.getLiveSourceEnv();
			if(sourceEnv != null) {
				targetEnvLiveIsLocal = sourceEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL);
				
				if(targetEnvLiveIsLocal) {
					promoteToLiveRepBtn.setVisible(true);
					if((sourceEnv.getImportEnvironmentViewerURL() != null) && (sourceEnv.getImportEnvironmentViewerURL().length() > new String("http://").length())) {
						publishToLiveViewerBtn.setVisible(true);
						openLiveViewerBtn.setVisible(true);
					}
				}

			} else {
				targetEnvLiveIsLocal = false;
			}

			
			/*String liveEnvName = currentTargetEnv.getImportEnvironmentLiveSource();
			Iterator<ImportEnvironment> liveIter = dataManager.getImportUtilityData().getImportEnvironments().iterator();
			ImportEnvironment liveEnv;
			while(liveIter.hasNext()) {
				liveEnv = liveIter.next();
				if(liveEnv.getImportEnvironmentName().equals(liveEnvName)) {
					if(liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_SERVER)) {
						targetEnvLiveIsLocal = false;
						break;
					}
				}
			}*/
			
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
	        	this.impActLastTestLbl.setValue(formatXMLCalendar(impActRunDate));
	        } else {
	        	this.impActLastRunLbl.setStyle("font-style:italic");
	        	this.impActLastRunLbl.setValue("Never");
	        }
	        
	        
	      //set the Target Environment name label
	        String targetEnvLabel =  this.currentTargetEnv.getImportEnvironmentName() + " (" + this.currentTargetEnv.getImportEnvironmentRole() + ")";
			this.targetEnvNameLbl.setValue(targetEnvLabel);
			this.liveSourceTxtBox.setValue(currentTargetEnv.getImportEnvironmentLiveSource());
			
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void onClick$refreshTestProjectBtn() {
		if(this.currentTargetEnv != null) {
			/*try {
				loadingDialog = (Window) Executions.createComponents("/loading_dialog.zul", null, null);
				Label loadingLabel = (Label) loadingDialog.getFellow("loadingDialogLabel");
				loadingLabel.setValue("Refreshing Test Repository...");
	    		loadingDialog.doModal();
	    	}
	    	catch(Exception e) {
	    		e.printStackTrace();
	    	}*/
			messages = messages + "\nRefreshing test repository from live source. Please wait...";
			this.messagesTxtBox.setValue(messages);
			
			this.refreshEnvFromLive();
			
			/*final EventQueue eq = EventQueues.lookup("refreshRepositoryEvents"); //create a queue
			importEventQueue = eq;
	        //subscribe async listener to handle long operation
	        eq.subscribe(new EventListener() {
	        	
		          public void onEvent(Event evt) {
		            if ("startRefreshRepository".equals(evt.getName())) {
		            	try {
		            		//refresh the repository
		            		try {
		            			dataManager.refreshTestImportEnvironment(currentTargetEnv);
		                	}
		                	catch(Exception e) {
		                	//	loadingDialog.detach();
		                		e.printStackTrace();
		                	}
		            	}
		            	catch(Exception e) {
		            		e.printStackTrace();
		            	}
		            	eq.publish(new Event("endRefreshRepository")); //notify it is done
		            }
		          }
	        }, true); //asynchronous
	      
	        //subscribe a normal listener to show the result 
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("endRefreshRepository".equals(evt.getName())) {
	            	Events.sendEvent("onEndRefreshRepository", testLocalImportWindow, null);
	            	
	            	importEventQueue = null;
	            	EventQueues.remove("refreshRepositoryEvents");
	            }
	          }
	        }); //synchronous
	      
	        eq.publish(new Event("startRefreshRepository")); //kick off the long operation
		*/	
		}
	}
	
	private void refreshEnvFromLive() {
		
		if(sourceEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
			this.refreshEnvFromLocalLive(sourceEnv);
		} else {
			this.refreshEnvFromServerLive(sourceEnv);
		}
		
		/*String sourceEnvName = currentTargetEnv.getImportEnvironmentLiveSource();
		if(sourceEnvName == null || sourceEnvName.length() == 0) {
			return;
		}

		Iterator<ImportEnvironment> impEnvIter = dataManager.getImportUtilityData().getImportEnvironments().iterator();
		ImportEnvironment sourceEnv;
		while(impEnvIter.hasNext()) {
			sourceEnv = impEnvIter.next();
			if(sourceEnv.getImportEnvironmentName().equals(sourceEnvName)) {
				if(sourceEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
					this.refreshEnvFromLocalLive(sourceEnv);
				} else {
					this.refreshEnvFromServerLive(sourceEnv);
				}
			}
		}*/
	}
	
	private void refreshEnvFromLocalLive(ImportEnvironment sourceEnv) {
		
		System.out.println("REFRESHING TEST ENV FROM LOCAL LIVE");
		
		Window statusDialog = this.getLoadingDialog();
		Label loadingLabel = (Label) getLoadingDialog().getFellow("importDialogLabel");		
		loadingLabel.setValue("Refreshing Test/QA Repository...");
		statusDialog.doHighlighted();
		
		dataManager.refreshTestEnvFromLocalLive(currentTargetEnv, sourceEnv);
		
		Events.sendEvent("onEndRefreshRepository", testLocalImportWindow, null);
	}
	
	
	private void refreshEnvFromServerLive(ImportEnvironment sourceEnv) {
		
		
		if(sourceEnv != null) {
			final ImportEnvironment liveSourceEnv = sourceEnv;
			final ProtegeIntegrationManager protegeManager = getProtegeManager();
			
			final Window statusDialog = this.getLoadingDialog();
			Label loadingLabel = (Label) getLoadingDialog().getFellow("importDialogLabel");		
			loadingLabel.setValue("Refreshing from Live Repository. Please Wait...");
			
			final EventQueue eq = EventQueues.lookup("refreshRepEvents"); //create a queue
			importEventQueue = eq;
			
			//subscribe async listeners to handle long operation
			//subscribe an asynchronous listener for initialising the refresh
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("initRefreshTestProject".equals(evt.getName())) {
	            	dataManager.initRefreshTestEnv(currentTargetEnv, liveSourceEnv);
	            //	loadingLabel.setValue("Loading Live Repository. Please Wait..."); 
	           // 	currentProject = protegeManager.getProjectForImportEnvironment(liveEnv);
	            	eq.publish(new Event("liveProjectLoaded")); //notify it is done
	            }
	          }
	        }, true); //asynchronous
	        
	        eq.subscribe(new EventListener() {
		          public void onEvent(Event evt) {
		            if ("liveProjectLoaded".equals(evt.getName())) {
		            	try {
		            	//	loadingLabel.setValue("Refreshing Test/QA Repository...");       			
		            	}
		            	catch(Exception e) {
		            		e.printStackTrace();
		            	}
		            //	Events.sendEvent("onTimer", testLocalImportWindow, null);
		            	eq.publish(new Event("RefreshDevQAProject"));
		            }
		          }
	      }); //synchronous
	        
	        
	      //subscribe an asynchronous listener for import execution updates
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("RefreshDevQAProject".equals(evt.getName())) {
	            	try {
	            		protegeManager.copyProtegeProject(liveSourceEnv, currentTargetEnv);  	
	            	} catch (ProjectLoadException e) {
	            		displayError("Error loading Protege Project. Check target environment settings", "Project Load Error");
	            	}
	            	eq.publish(new Event("endRefreshRepository")); //notify it is done
	            }
	          }
	        }, true); //asynchronous
	        
	      //subscribe a normal listener to show the result 
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("endRefreshRepository".equals(evt.getName())) {     
	            	Events.sendEvent("onEndRefreshRepository", testLocalImportWindow, null);
	            	importEventQueue = null;
	            	EventQueues.remove("refreshRepEvents");
	            }
	          }
	        }); //synchronous
	        
	        try {
				statusDialog.doHighlighted();
			}
			catch(Exception e) {
				e.printStackTrace();
			}  
	      
			
	        eq.publish(new Event("initRefreshTestProject")); //kick off the long operation
		}
	}
	
	
	public void onEndRefreshRepository$testLocalImportWindow() {
		loadingDialog.detach();
		this.loadingDialog = null;
		refreshRepLabel.setStyle(DISABLED_LABEL_STYLE);
		refreshTestProjectBtn.setDisabled(true);
		messages = messages + "\nTest repository refreshed successfully.\n\n";
		this.messagesTxtBox.setValue(messages);
	}
	
	
	
	public void onClick$promoteToLiveRepBtn() {
		if(targetEnvLiveIsLocal) {
			System.out.println("PROMOTING TEST REPOSITORY OUTPUT TO LOCAL LIVE");
			
			try {
				String confirmationMessage = "Are you sure that you want to overwrite the " + sourceEnv.getImportEnvironmentName() + " production environment?";
				Messagebox.show((confirmationMessage ), "Promote Test Result to Live?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											//delete the currently selected logs
											try {
												boolean success = dataManager.promoteTestResultToLocalLive(log, sourceEnv);
												
												if(success) {
													dataManager.refreshTestEnvFromLocalLive(currentTargetEnv, sourceEnv);
													publishToLiveViewerBtn.setDisabled(false);
												} else {
													displayError("Error promoting test results", "Test Promotion Error");
												}
												promoteToLiveRepBtn.setDisabled(true);
												break;
											} catch(PromoteRepositoryException e) {
												displayError(e.getMessage(),"Test Promotion Error");
												promoteToLiveRepBtn.setDisabled(true);
												break;
											}
											
										case Messagebox.NO: break;
									}
							}
						}
				);
				} catch (Exception e) {
					e.printStackTrace();
				}
			
		}
	}
	
	
	
	public void onClick$publishToLiveViewerBtn() {
			System.out.println("PUBLISHING TEST REPOSITORY OUTPUT TO LIVE VIEWER");
			
			try {
				String confirmationMessage = "Are you sure that you want to publish the test results to the " + sourceEnv.getImportEnvironmentName() + " production viewing environment?";
				Messagebox.show((confirmationMessage ), "Publish Test Result to Live Viewer?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											//create an EssentialViewerPublisher instance to publish the repository to the test environment
											EssentialViewerPublisher publisher = new EssentialViewerPublisher(currentProject, sourceEnv);
											if(publisher.generateReport()) {
												messages = messages + "\n" + EssentialViewerPublisher.SUCCESSFUL_GENERATION;
												messagesTxtBox.setValue(messages);
												
												//send the generated XML file to the live viewing environment
												publisher.sendReportXML();
												
												//enable the open live viewer button
												openLiveViewerBtn.setDisabled(false);
											} else {
												messages = messages + "\n" + EssentialViewerPublisher.FAILED_GENERATION_MESSAGE;
												messagesTxtBox.setValue(messages);
											}
											
											publishToLiveViewerBtn.setDisabled(true);
											break;
											
										case Messagebox.NO: break;
									}
							}
						}
				);
				} catch (Exception e) {
					e.printStackTrace();
				}
			
	}
	
	
	
	public void onClick$openLiveViewerBtn() {
		System.out.println("OPENING LIVE VIEWER");
		
		//open the live viewer in a new browser window
		Executions.getCurrent().sendRedirect(this.sourceEnv.getImportEnvironmentViewerURL(), "_blank");	
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
			//System.out.println("PROGRESS: " + message);
			importEventQueue.publish(new Event("updateExecuteImport"));
		}
		
	}

	//NEW VERSION
	public void onClick$runImportBtn() {
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
		
		/*
		this.dataManager.initImportActivityLog(currentImpAct, currentTargetEnv, currentUser);

		this.sourceRepName = this.currentImpAct.getImportActivitySourceRepository();
		this.importSpec = this.impSpecDataManager.getSpreadsheetImportSpecScriptData();
		
		this.protegeManager = getProtegeManager();
		*/
		
		
		final IntegrationEngineClient integrationListener = this;
		
		if(!targetEnvLiveIsLocal) {
			this.cachedLocalRepZipPath = dataManager.cacheLocalImpEnvProject(currentTargetEnv);
		}
		
				
		final EventQueue eq = EventQueues.lookup("executeImportEvents"); //create a queue
		importEventQueue = eq;
		
		final Window statusDialog = this.getLoadingDialog();
		final Label loadingLabel = (Label) getLoadingDialog().getFellow("importDialogLabel");	
		Button loadingCancelButton = (Button) getLoadingDialog().getFellow("cancelImportBtn");	
		loadingCancelButton.addEventListener("onClick", new EventListener(){
            public void onEvent(Event event) throws Exception {
            	loadingLabel.setValue("Cancelling Import...");
            	eq.publish(new Event("abortExecuteImport"));
            }
        });
		
	
		
        //subscribe async listeners to handle long operation
		//subscribe an asynchronous listener for import execution updates
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("initImportProject".equals(evt.getName())) {
   
            	try {
            		timerProgressStatus = "Loading Target Repository. Please Wait..."; 
            		currentProject = protegeManager.getProjectForImportEnvironment(currentTargetEnv);
            	} catch (ProjectLoadException e) {
            		displayError("Error loading Protege Project. Check target environment settings", "Project Load Error");
            	} /*catch (InvalidFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
            	if(importEventQueue != null) {
            		eq.publish(new Event("importProjectLoaded")); //notify it is done
            	}
            }
          }
        }, true); //asynchronous
        
        
        
        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("importProjectLoaded".equals(evt.getName())) {
	            	try {
	            		if(importEventQueue != null) {
		            		timerProgressStatus = "Target Repository Loaded. Initialising Import..."; 
		            		
		            	//	System.out.println("PROGRESS STATUS: " + timerProgressStatus);		
		        			loadingLabel.setValue(timerProgressStatus);
	            		}
	        			
	            	}
	            	catch(Exception e) {
	            		e.printStackTrace();
	            	}
	            //	Events.sendEvent("onTimer", testLocalImportWindow, null);
	            	if(importEventQueue != null) {
	            		eq.publish(new Event("initExecuteImport"));
	            	}
	            }
	          }
      }); //synchronous
		
		
		
		
		
		//subscribe an asynchronous listener for import execution updates
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("initExecuteImport".equals(evt.getName())) {
            	String importScript;
				try {
					importScript = dataManager.initChunkedImportScript(currentImpAct, spreadsheet, log, importSpec, itsMessageListener);	
					currentImportScript = currentImportScript + importScript;
					protegeManager.initChunkedImport(currentProject, currentTargetEnv, sourceRepName, importScript, integrationListener);
	        		timerProgressStatus = "Import Script Initialised..."; 
	        		if(importEventQueue != null) {
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
	            if ("executeNextImportChunk".equals(evt.getName())) {
	            	try {
	            		if(importEventQueue != null) {
		            		eq.publish(new Event("updateProgress")); 
		            	//	Events.sendEvent("onTimer", importStatusTimer, null);
			            	if(importSpec.importScriptComplete()) {
			            		eq.publish(new Event("endExecuteImport")); //notify it is done
			            	} else {
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
	            	try { 	
	            		String importScript = dataManager.getNextImportScriptChunk(currentImpAct, spreadsheet, log, importSpec, itsMessageListener);
	            		currentImportScript = currentImportScript + importScript;
	            		//	timerProgressStatus = importSpec.getCurrentStatusString() + " (" + importSpec.getPercentageComplete() + "%)";
	            		if(importEventQueue != null) {
		            		eq.publish(new Event("updateProgress")); 
		            		Event nextEvent = new Event("executeImportChunk", null, importScript);     	           
			            	eq.publish(nextEvent); //notify to import another chunk
	            		}
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
	            	try { 	
	            		if(importEventQueue != null) {
	            			String importScript = (String) evt.getData();
	            			currentProject = protegeManager.executeChunkedImport(currentProject, currentTargetEnv, sourceRepName, importScript, integrationListener);  
	            			if(importEventQueue != null) {
			            		if(importSpec.importScriptComplete()) {
			            			timerProgressStatus = "Finalising Import..."; 
			                    	eq.publish(new Event("updateProgress")); 
				            		eq.publish(new Event("endExecuteImport")); //notify it is done
				            	} else {
				            		eq.publish(new Event("updateProgress")); 
				            		eq.publish(new Event("executeNextImportChunk")); //notify to import another chunk
				            	}
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
            	if(importEventQueue != null) {
	            	// itsProgress = importSpec.getRowsCompleted();
	            	itsProgress = Math.round(importSpec.getRowsCompleted() / importSpec.getTotalImportRows() * 100);
	            	timerProgressStatus = importSpec.getCurrentStatusString() + " (" + importSpec.getRowsCompleted() + " of " + importSpec.getTotalImportRows() + " rows completed)";
	        	//	Events.sendEvent("onTimer", importStatusTimer, null);
            	}
            }
          }
        }, true); //asynchronous
        
      //subscribe a normal listener to abort the import 
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("abortExecuteImport".equals(evt.getName())) {
            	// protegeManager.saveProject(currentProject);
            	System.out.println("	CANCELLING IMPORT");
            	Events.sendEvent("onAbortExecuteImport", testLocalImportWindow, null);
            	
            	importEventQueue = null;
            	EventQueues.remove("executeImportEvents");
            }
          }
        }); //synchronous
        
      
        //subscribe a normal listener to close down the import once completed 
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("endExecuteImport".equals(evt.getName())) 
            {
            	if(importEventQueue != null) 
            	{
            		// catch any save exceptions on resulting project
            		try
            		{
		            	protegeManager.saveProject(currentProject);
		            	Events.sendEvent("onEndExecuteImport", testLocalImportWindow, null);
		            	
		            	importEventQueue = null;
		            	EventQueues.remove("executeImportEvents");
            		}
            		catch(Exception ex)
            		{
            			// report to console
            			System.out.println("EXCEPTION in endExecuteImport event, saving Project repository: " + ex.toString());
            			
            			// add to messages
            			messages = messages + "\n" + ex.getLocalizedMessage();
            		}
            	}
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
	
	
	
	public void startTimer$testLocalImportWindow() {
		try {	
			if(importEventQueue != null) {
				this.importStatusTimer.start();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	
	
	public void onTimer$importStatusTimer() {
		try {	
		//	int percentageComplete = this.importSpec.getPercentageComplete();
		//	System.out.println("TIMER PROGRESS STATUS: " + timerProgressStatus);	
		//	System.out.println("PERCENTAGE PROGRESS STATUS: " + itsProgress);	
			if(importEventQueue != null) {
				Label loadingLabel = (Label) getLoadingDialog().getFellow("importDialogLabel");	
				loadingLabel.setValue(timerProgressStatus);
				
				Progressmeter progressMeter = (Progressmeter) getLoadingDialog().getFellow("importProgressMeter");	
				progressMeter.setValue(Math.min(100, itsProgress));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		} 
	} 
	
	
	public void onAbortExecuteImport$testLocalImportWindow() {
		
		this.importStatusTimer.stop();
		this.getLoadingDialog().detach();
		messages = messages + "\nImport Cancelled.";
		this.messagesTxtBox.setValue(messages);
		
		runImportBtn.setStyle(DISABLED_LABEL_STYLE);
		runImportBtn.setDisabled(false);
		downloadMessagesBtn.setDisabled(false);
		downloadMessagesBtn.setStyle(ENABLED_LABEL_STYLE);
		
		//Save any messages returned by the Integration Engine to the log
		dataManager.saveImportMessageLog(log, this.currentImpAct, messages);
		
		//log the recent execution and save the resulting import script
		dataManager.finaliseImport(this.currentImpAct, log, this.currentImportScript);
		
		//save the config data
		dataManager.saveAppData();

		
		this.impActLastTestLbl.setStyle(DISABLED_LABEL_STYLE);
	}
	
	
	
	public void onEndExecuteImport$testLocalImportWindow() {
		this.importStatusTimer.stop();
		this.getLoadingDialog().detach();
		
		if(this.protegeManager.getIntegrationReturnCode() == IntegrationEngine.SUCCESS_SC) {
			messages = messages + "\nImport executed successfully.";
		} else {
			messages = messages + "\nImport executed with exceptions.";
		}
		this.messagesTxtBox.setValue(messages);
		
		//set the date of last tested
		this.currentImpAct.setImportActivityTestedDate(this.getNowAsGregorian());
		
		this.impActLastTestLbl.setStyle(EssentialImportInterface.SET_DATE_LABEL_FORMAT);
		this.impActLastTestLbl.setValue(this.formatXMLCalendar(currentImpAct.getImportActivityTestedDate()));
		
		//compress the repository created after the import
		dataManager.compressLocalImpEnvProject(log, this.currentTargetEnv);
		
		//refresh the test repository to its former state
		if(!targetEnvLiveIsLocal) {
			dataManager.uncacheLocalImpEnvProject(currentTargetEnv, cachedLocalRepZipPath);
		} else {	
			if(sourceEnv != null) {
				dataManager.refreshTestEnvFromLocalLive(currentTargetEnv, sourceEnv);
			} else {
				System.out.println("SOURCE ENV NOT FOUND");
			}
		}
		
		
		//Save any messages returned by the Integration Engine to the log
		dataManager.saveImportMessageLog(log, this.currentImpAct, messages);
		
		//log the recent execution and save the resulting import script
		dataManager.finaliseImport(this.currentImpAct, log, this.currentImportScript);
		
		//save the config data
		dataManager.saveAppData();
		
		Component parentComponent = testLocalImportWindow.getParent();
		Events.sendEvent("onTested", parentComponent, null);
		
		
		this.runImportLabel.setStyle(DISABLED_LABEL_STYLE);
		this.refreshRepLabel.setStyle(DISABLED_LABEL_STYLE);
		this.refreshTestProjectBtn.setDisabled(true);
		downloadRepLabel.setStyle(ENABLED_LABEL_STYLE);
		downloadImportScriptBtn.setDisabled(false);
		downloadRepBtn.setDisabled(false);
		downloadMessagesBtn.setDisabled(false);
		downloadMessagesBtn.setStyle(ENABLED_LABEL_STYLE);
		publishRepLabel.setStyle(ENABLED_LABEL_STYLE);
		publishRepBtn.setDisabled(false);
		
		promoteToLiveRepBtn.setDisabled(false);
	}
	
	
	
	public void onClick$downloadRepBtn() {
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

	
	
	public void onClick$downloadImportScriptBtn() {
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
	
	
	public void onClick$downloadMessagesBtn() {
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
	
	
	//Event handler to open a window to the Essential Viewer associated with the target environment
	public void onClick$openViewerBtn() {
		//open the viewer in a new browser window
		Executions.getCurrent().sendRedirect(this.currentTargetEnv.getImportEnvironmentViewerURL(), "_blank");
	}
	
	
	public void onClick$closeBtn() {
		//clear up any attributes and then close the dialog
		this.finaliseImportActivity();
		testLocalImportWindow.detach();
	}
	
	
	//event handler for when the window is closed using the 'x' button
	public void onClose$testLocalImportWindow() {
		//clear up any attributes and then close the dialog	
		this.finaliseImportActivity();
	}

	
	//private method to tidy up when closing the window
	private void finaliseImportActivity() {

		//tell the data manager to close down the import activity
		dataManager.closeCurrentImportActivity();
		
		//clear up any attributes and then close the dialog	
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
	
	public void onMessageReceived$testLocalImportWindow(Event e) {
		this.messagesTxtBox.setValue(messages);
	}
	
	private Window getLoadingDialog() {
		try {
			if(this.loadingDialog == null) {
				this.loadingDialog = (Window) Executions.createComponents("/import_dialog.zul", this.testLocalImportWindow, null);
				loadingDialog.doModal();
			}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}   
    	return this.loadingDialog;
	}
	
	
	private ImportEnvironment getLiveSourceEnv() {
		String liveEnvName = currentTargetEnv.getImportEnvironmentLiveSource();
		Iterator<ImportEnvironment> liveIter = dataManager.getImportUtilityData().getImportEnvironments().iterator();
		ImportEnvironment liveEnv;
		while(liveIter.hasNext()) {
			liveEnv = liveIter.next();
			if(liveEnv.getImportEnvironmentName().equals(liveEnvName)) {
				return liveEnv;
			}
		}
		return null;
	}

}
