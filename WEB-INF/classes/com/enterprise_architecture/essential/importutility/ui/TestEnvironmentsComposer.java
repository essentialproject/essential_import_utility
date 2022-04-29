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
 * 23.06.2018	JWC Add new argument to ListItemRenderer.render()
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

//import java.io.File;
//import java.rmi.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.integration.EssentialViewerPublisher;
import com.enterprise_architecture.essential.importutility.integration.ProjectLoadException;
import com.enterprise_architecture.essential.importutility.integration.ProtegeIntegrationManager;
//import com.enterprise_architecture.essential.integration.core.IntegrationEngineClient;

import edu.stanford.smi.protege.model.Project;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * target environments for Essential imports
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class TestEnvironmentsComposer extends EssentialImportInterface {
	
	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private static final String LOCAL_DEPLOYMENT_TYPE = "LOCAL";
	private static final String SERVER_DEPLOYMENT_TYPE = "SERVER";
	
	
	private int currentMode;
	private int currentEnvIndex;
	private ImportEnvironment currentEnv;
	private ImportUtilityDataManager dataManager;
	
	public Button okBtn;
	public Button cancelBtn;
	public Textbox targetEnvNameTxtBox;
	public Textbox targetEnvDescTxtBox;
	public Listbox targetEnvRoleListBox;

	public Listbox liveEnvListBox;
	
	public Textbox viewerPathTxtBox;
	public Textbox viewerUsernameTxtBox;
	public Textbox viewerPasswordTxtBox;
	public Textbox confirmViewerPasswordTxtBox;
	
	public Textbox exportlabel;
	
	public Window testEnvWindow;
	
	private Listbox testEnvList;
	private Media projectFile;
	
	private String oldLiveRepositoryName = "";
	
	private Project currentProject;
	
	private EventQueue importEventQueue;
	
	public Window loadingDialog;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		this.dataManager = this.getImportUtilityDataManager();
		
		this.testEnvList = (Listbox) Path.getComponent("//appHome/appHomeWin/targetEnvListBox");		
		
		//set up the list of potential live environments
		List liveEnvList = this.dataManager.getLiveEnvironments();
		ListModelList liveEnvModel = new ListModelList(liveEnvList);
		liveEnvModel.add(0, "- Select Production Environment -");
		liveEnvListBox.setModel(liveEnvModel);
		liveEnvListBox.setItemRenderer(new ListitemRenderer() {
        	public void render(Listitem anItem, Object data, int theIndex) {
        		if(!data.getClass().getName().endsWith("String")) {
	        		ImportEnvironment anEnv = (ImportEnvironment) data;
	        		String liveEnvName = anEnv.getImportEnvironmentName();
	        		anItem.setLabel(liveEnvName);
	        		anItem.setValue(liveEnvName);
        		} else {
        			String emptyRow = (String) data;
        			anItem.setLabel(emptyRow);
	        		anItem.setValue(emptyRow);
        		}
        	}
        });		
		
		
		ImportEnvironment impEnv = (ImportEnvironment) desktop.getAttribute("currentImpEnv");
		if(impEnv != null) {
			//given a Target Environment, set the current mode for the composer to EDIT
			this.currentMode = TestEnvironmentsComposer.EDIT_MODE;
			testEnvWindow.setTitle("Edit Dev/QA Environment");
			
			//set the general details for the environment
			this.currentEnvIndex = testEnvList.getSelectedIndex();
			this.currentEnv = (ImportEnvironment) testEnvList.getListModel().getElementAt(currentEnvIndex);
			this.targetEnvNameTxtBox.setValue(this.currentEnv.getImportEnvironmentName());
			this.targetEnvDescTxtBox.setValue(this.currentEnv.getImportEnvironmentDescription());
			
			//set the Essential Viewer details
			this.viewerPathTxtBox.setValue(this.currentEnv.getImportEnvironmentViewerURL());
			this.viewerUsernameTxtBox.setValue(this.currentEnv.getViewerUsername());
			this.viewerPasswordTxtBox.setValue(this.currentEnv.getViewerPassword());
			this.confirmViewerPasswordTxtBox.setValue(this.currentEnv.getViewerPassword());
			
			
			//pre- select the appropriate target environment role
			String envRole = this.currentEnv.getImportEnvironmentRole();
			List roles = this.targetEnvRoleListBox.getItems();
			
			Iterator rolesIter = roles.iterator();
			Listitem currentListItem;
			while(rolesIter.hasNext()) {
				currentListItem = (Listitem) rolesIter.next();
				String currentRole = (String) currentListItem.getValue();
				if((currentRole != null) && (currentRole.equals(envRole))) {
					int roleIndex = targetEnvRoleListBox.getIndexOfItem(currentListItem);
					targetEnvRoleListBox.setSelectedIndex(roleIndex);						
				}
			}
			
			
			//pre- select the appropriate source live environment
			this.oldLiveRepositoryName = this.currentEnv.getImportEnvironmentLiveSource();
			Iterator liveEnvsIter = liveEnvModel.iterator();
			Object currentLiveEnvListObject;
			ImportEnvironment liveEnv;
			while(liveEnvsIter.hasNext()) {
				currentLiveEnvListObject = liveEnvsIter.next();
				if(!currentLiveEnvListObject.getClass().getName().endsWith("String")) {
					liveEnv = (ImportEnvironment) currentLiveEnvListObject;
					if((liveEnv != null) && (liveEnv.getImportEnvironmentName().equals(oldLiveRepositoryName))) {
						int liveEnvIndex = liveEnvModel.indexOf(currentLiveEnvListObject);
						liveEnvListBox.setSelectedIndex(liveEnvIndex);						
					}
				}
			}			
			
			
			desktop.removeAttribute("currentImpEnv");
		} else {
			//in the absence of a Target Environment, set the current mode for the composer to CREATE
			this.currentMode = TestEnvironmentsComposer.CREATE_MODE;
			testEnvWindow.setTitle("Create Dev/QA Environment");
			
			this.currentEnv = dataManager.newImportEnvironment();
			
			//pre-select the first in the list of environment roles
			targetEnvRoleListBox.setSelectedIndex(0);
		}

	}
	
	
	
	public void onClick$okBtn() {
		try {
		//	Retrieve the object that manages the application data
			ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
			
			//raise an error if a viewer username is given but the password is empty
			if(!this.viewerUsernameTxtBox.getValue().isEmpty() && this.viewerPasswordTxtBox.getValue().isEmpty()) {
				this.displayError("A viewer password must be provided for the username", "Empty Viewer Password");
				return;
			}
			
			//raise an error if the viewer passwords do not match
			if(!this.viewerPasswordTxtBox.getValue().isEmpty() && !this.confirmViewerPasswordTxtBox.getValue().equals(this.viewerPasswordTxtBox.getValue())) {
				this.displayError("The viewer passwords do not match", "Viewer Password Error");
				return;
			}
			
			
			
			//raise an error if a deployment role has not been selected
			if(this.targetEnvRoleListBox.getSelectedIndex() < 1) {
				this.displayError("A deployment role must be selected", "Undefined Deployment Role");
				return;
			}
			
			
			//raise an error if a related production repository has not been selected
			if(liveEnvListBox.getSelectedIndex() < 1) {
				this.displayError("An associated production repository must be selected", "Undefined Live Repository");
				return;
			}

			
			//if all input constraints are met, update or create the Target Environment
			if ((targetEnvNameTxtBox.isValid()) && (targetEnvRoleListBox.getSelectedIndex() > 0)  && (liveEnvListBox.getSelectedIndex() > 0)) {
				
				ListModelList listModel = (ListModelList) testEnvList.getListModel();
				
				//If the dialog is in EDIT MODE, update the details of the current Target Environment
				if(this.currentMode == TestEnvironmentsComposer.EDIT_MODE) {
							
					if(!this.setEnvironmentDetails(this.currentEnv)) {
						this.displayError("Error saving environment details", "Import Environment Error");
						return;
					}
					
					//save the repository to the local file system
					this.saveCurrentEnv();
					
					
					//update the list of target environments
					listModel.remove(this.currentEnvIndex);
					listModel.add(this.currentEnvIndex, this.currentEnv);
					this.testEnvList.setSelectedIndex(this.currentEnvIndex);
					
					//save the config data and close the dialog
					dataManager.saveAppData();
					
					
					testEnvWindow.detach();
				}
				
				
				//If the dialog is in CREATE MODE, add the new Target Environment to the config data
				if(this.currentMode == TestEnvironmentsComposer.CREATE_MODE) {
					
					if(!this.setEnvironmentDetails(this.currentEnv)) {
						this.displayError("Error saving environment details", "Import Environment Error");
						return;
					}
					
					//save the repository to the local file system
					this.saveCurrentEnv();
					
				}
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	public void onClick$cancelBtn() {
		try {
			testEnvWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	private void refreshEnvFromLocalLive() {
		
		System.out.println("REFRESHING TEST ENV FROM LOCAL LIVE");
		
		final Window statusDialog = this.getLoadingDialog();
		final Label loadingLabel = (Label) getLoadingDialog().getFellow("importDialogLabel");		
		loadingLabel.setValue("Refreshing Test/QA Repository...");
		
		statusDialog.doHighlighted();
		
		int liveEnvIndex = liveEnvListBox.getSelectedIndex();
		ListModelList liveEnvModel = (ListModelList) liveEnvListBox.getModel();
		final ImportEnvironment liveEnv = (ImportEnvironment) liveEnvModel.get(liveEnvIndex);
		dataManager.refreshTestEnvFromLocalLive(currentEnv, liveEnv);
		
		Events.sendEvent("onEndRefresh", testEnvWindow, null);
	}
	
	
	private void refreshEnvFromServerLive() {
		
		System.out.println("REFRESHING TEST ENV FROM SERVER LIVE");
		int liveEnvIndex = liveEnvListBox.getSelectedIndex();
		ListModelList liveEnvModel = (ListModelList) liveEnvListBox.getModel();
		final ImportEnvironment liveEnv = (ImportEnvironment) liveEnvModel.get(liveEnvIndex);
		final ProtegeIntegrationManager protegeManager = getProtegeManager();
		
		final Window statusDialog = this.getLoadingDialog();
		Label loadingLabel = (Label) getLoadingDialog().getFellow("importDialogLabel");		
		loadingLabel.setValue("Refreshing from Live Repository. Please Wait...");
		
		final EventQueue eq = EventQueues.lookup("refreshRepEvents"); //create a queue
		importEventQueue = eq;
		
		//subscribe async listeners to handle long operation
		//subscribe an asynchronous listener for import execution updates
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("initRefreshTestProject".equals(evt.getName())) {
            	dataManager.initRefreshTestEnv(currentEnv, liveEnv);
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
            		protegeManager.copyProtegeProject(liveEnv, currentEnv);  	
            		eq.publish(new Event("endRefreshRepository")); //notify it is done
            	} catch (ProjectLoadException e) {
            		eq.publish(new Event("abortRefreshRepository")); //notify it is done
            	}
            }
          }
        }, true); //asynchronous
        
      //subscribe a normal listener to show the result 
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("endRefreshRepository".equals(evt.getName())) {     
            	Events.sendEvent("onEndRefresh", testEnvWindow, null);
            	importEventQueue = null;
            	EventQueues.remove("refreshRepEvents");
            }
          }
        }); //synchronous
        
        
      //subscribe a normal listener to show the result 
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("abortRefreshRepository".equals(evt.getName())) {     
            	Events.sendEvent("onAbortRefresh", testEnvWindow, null);
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
	
	
	
	public void onEndRefresh$testEnvWindow() {
		this.getLoadingDialog().detach();
		
		ListModelList listModel = (ListModelList) testEnvList.getListModel();
		listModel.add(this.currentEnv);
		this.testEnvList.setSelectedIndex(listModel.indexOf(this.currentEnv));
		
		EssentialImportUtility configData = dataManager.getImportUtilityData();
		configData.getImportEnvironments().add(this.currentEnv);
		
		//save the config data and close the dialog
		dataManager.saveAppData();
			
		testEnvWindow.detach();
	}
	
	
	public void onAbortRefresh$testEnvWindow() {
		this.getLoadingDialog().detach();
		displayError("Error loading Protege Project. Check target environment settings", "Project Load Error");
	}
	
	
	private boolean setEnvironmentDetails(ImportEnvironment anEnv) {
		//set the general details for the environment
		anEnv.setImportEnvironmentName(targetEnvNameTxtBox.getValue());
		anEnv.setImportEnvironmentDescription(targetEnvDescTxtBox.getValue());
		String envRole = (String) targetEnvRoleListBox.getSelectedItem().getValue();
		anEnv.setImportEnvironmentRole(envRole);
		anEnv.setImportEnvironmentDeploymentType(LOCAL_DEPLOYMENT_TYPE);
		
		anEnv.setImportEnvironmentViewerURL(viewerPathTxtBox.getValue());
		anEnv.setViewerPassword(this.viewerPasswordTxtBox.getValue());
		anEnv.setViewerUsername(this.viewerUsernameTxtBox.getValue());
		
		return true;
	}
	
	
	private boolean saveCurrentEnv() {
		//set the related live repository and copy the latest version of the repository
		int liveEnvIndex = liveEnvListBox.getSelectedIndex();
		ListModelList liveEnvModel = (ListModelList) liveEnvListBox.getModel();
		ImportEnvironment liveEnv = (ImportEnvironment) liveEnvModel.get(liveEnvIndex);
		currentEnv.setImportEnvironmentLiveSource(liveEnv.getImportEnvironmentName());	
		
		if(!oldLiveRepositoryName.equals(liveEnv.getImportEnvironmentName())) {		
			if(liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
				this.refreshEnvFromLocalLive();
			} else {
				this.refreshEnvFromServerLive();
			}
							
			//dataManager.refreshTestImportFromLive(anEnv, liveEnv);
		}
		return true;
	}
	
	
	public void onProjectUpload$testEnvWindow() {
		
		//update the project zip file for the local environment
		this.projectFile = (Media) testEnvWindow.getAttribute("uploadedProject");
	}
	
	
	private void saveProject() {
			
		if (projectFile != null && (projectFile.isBinary())) {	
	        final EventQueue eq = EventQueues.lookup("unzipProjectEvents"); //create a queue
	        
	        //subscribe async listener to handle long operation
	        eq.subscribe(new EventListener() {
	        	
		          public void onEvent(Event evt) {
		            if ("startUnzipProject".equals(evt.getName())) {
		            	try {
		            		//save the zip file to the local cache		            		
		        			dataManager.storeLocalImpEnvProjectZip(currentEnv, projectFile);	
		        			
		        			dataManager.saveAppData();
		            	}
		            	catch(Exception e) {
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
	            	EventQueues.remove("unzipProjectEvents");
	            }
	          }
	        }); //synchronous
	      
	        eq.publish(new Event("startUnzipProject")); //kick off the long operation
		}
	}
	
		
	public void onClick$testImpEnvBtn() {
		try {
		//	Create a temporary Import Environment with the required details
			ImportEnvironment tempImpEnv = this.dataManager.newImportEnvironment();
			this.setEnvironmentDetails(tempImpEnv);
			
			ProtegeIntegrationManager protegeManager = getProtegeManager();
			if(protegeManager.testProtegeConnection(tempImpEnv)) {
				Messagebox.show("Connection Details Tested Successfully", "Success", Messagebox.OK, Messagebox.INFORMATION);
			} else {
				List errorList = protegeManager.getProjectLoadErrors();
				Messagebox.show("Errors accessing repository", "Test Failed", Messagebox.OK, Messagebox.ERROR);
				Iterator errorIter = errorList.iterator();
				while(errorIter.hasNext()) {
					System.out.println(errorIter.next().toString());
				}
			} 
		}
		catch (Exception e) {
			e.printStackTrace();
			Clients.clearBusy();
		}

	}
	
	
	public void onClick$testViewerBtn() {
		String viewerURL = viewerPathTxtBox.getValue();
		try {	
				if(EssentialViewerPublisher.viewerExists(viewerURL)) {
					Messagebox.show("Essential Viewer Connection Tested Successfully", "Success", Messagebox.OK, Messagebox.INFORMATION);
				} else {
					Messagebox.show("Unable to connect to Essential Viewer", "Test Failed", Messagebox.OK, Messagebox.ERROR);
				} 
			}
			catch (Exception e) {
				e.printStackTrace();
				Clients.clearBusy();
			}
	}
	
	
	private Window getLoadingDialog() {
		try {
			if(this.loadingDialog == null) {
				this.loadingDialog = (Window) Executions.createComponents("/import_dialog.zul", this.testEnvWindow, null);
				loadingDialog.doModal();
			}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}   
    	return this.loadingDialog;
	}

}
