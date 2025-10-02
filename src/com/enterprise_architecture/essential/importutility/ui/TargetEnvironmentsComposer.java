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
 * 23.08.2018	JWC Upgrade to ZK 8.5
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.File;
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
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.integration.EssentialViewerPublisher;
import com.enterprise_architecture.essential.importutility.integration.ProjectLoadException;
import com.enterprise_architecture.essential.importutility.integration.ProtegeIntegrationManager;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * target environments for Essential imports
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class TargetEnvironmentsComposer extends EssentialImportInterface {
	
	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private static final String LOCAL_DEPLOYMENT_TYPE = "LOCAL";
	private static final String SERVER_DEPLOYMENT_TYPE = "SERVER";
	
	
	private int currentMode;
	private int currentEnvIndex;
	private ImportEnvironment currentEnv;
	private ImportUtilityDataManager dataManager;
	private boolean projectUploaded = false;
	
	public Button okBtn;
	public Button cancelBtn;
	public Button uploadLocalProjectBtn;
	public Textbox targetEnvNameTxtBox;
	public Textbox targetEnvDescTxtBox;
	public Listbox targetEnvTypeListBox;
	
	public Textbox projectPathTxtBox;
	
	public Textbox projectNameTxtBox;
	public Textbox serverHostNameTxtBox;
	public Textbox usernameTxtBox;
	public Textbox passwordTxtBox;
	public Textbox confirmPasswordTxtBox;
	
	public Textbox viewerPathTxtBox;
	public Textbox viewerUsernameTxtBox;
	public Textbox viewerPasswordTxtBox;
	public Textbox confirmViewerPasswordTxtBox;
	
	public Textbox exportlabel;
	
	public Window targetEnvWindow;
	
	private Listbox targetEnvList;
	
	private Media projectFile;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		this.dataManager = this.getImportUtilityDataManager();
		
		this.targetEnvList = (Listbox) Path.getComponent("//appHome/appHomeWin/targetEnvListBox");		
		
		ImportEnvironment impEnv = (ImportEnvironment) desktop.getAttribute("currentImpEnv");
		if(impEnv != null) {
			//given a Target Environment, set the current mode for the composer to EDIT
			this.currentMode = TargetEnvironmentsComposer.EDIT_MODE;
			if(this.isEIPMode()) {
				targetEnvWindow.setTitle("Edit Reference Repository");
			}  else {
				targetEnvWindow.setTitle("Edit Production Environment");
			}
			
			//set the general details for the environment
			this.currentEnvIndex = targetEnvList.getSelectedIndex();
			this.currentEnv = (ImportEnvironment) targetEnvList.getListModel().getElementAt(currentEnvIndex);
			this.targetEnvNameTxtBox.setValue(this.currentEnv.getImportEnvironmentName());
			this.targetEnvDescTxtBox.setValue(this.currentEnv.getImportEnvironmentDescription());
			
			//set the local project details
			String projectPath = this.currentEnv.getImportEnvironmentRepositoryPath();
			if(projectPath != null) {
				StringBuffer pathSB = new StringBuffer(projectPath);
				int fileNameStart = pathSB.lastIndexOf(File.separator);
				if(fileNameStart >= 0) {
					this.projectPathTxtBox.setValue(pathSB.substring(fileNameStart + 1, projectPath.length() - 1));
				}
			}
			
			if(!this.isEIPMode()) {
				//set the server-based repository details
				this.projectNameTxtBox.setValue(this.currentEnv.getProjectName());
				this.serverHostNameTxtBox.setValue(this.currentEnv.getHostname());
				this.usernameTxtBox.setValue(this.currentEnv.getUsername());
				this.passwordTxtBox.setValue(this.currentEnv.getPassword());
				this.confirmPasswordTxtBox.setValue(this.currentEnv.getPassword());
				
				//set the Essential Viewer details
				this.viewerPathTxtBox.setValue(this.currentEnv.getImportEnvironmentViewerURL());
				this.viewerUsernameTxtBox.setValue(this.currentEnv.getViewerUsername());
				this.viewerPasswordTxtBox.setValue(this.currentEnv.getViewerPassword());
				this.confirmViewerPasswordTxtBox.setValue(this.currentEnv.getViewerPassword());
					
				
				//pre- select the appropriate target environment type
				String envType = this.currentEnv.getImportEnvironmentDeploymentType();
				List types = this.targetEnvTypeListBox.getItems();
				
				Iterator typesIter = types.iterator();
				Listitem currentTypeItem;
				while(typesIter.hasNext()) {
					currentTypeItem = (Listitem) typesIter.next();
					String currentType = (String) currentTypeItem.getValue();
					if((currentTypeItem.getValue() != null) && (currentType.equals(envType))) {
						int typeIndex = targetEnvTypeListBox.getIndexOfItem(currentTypeItem);
						targetEnvTypeListBox.setSelectedIndex(typeIndex);						
					}
				}
				
				//disable the deployment type listbox as this cannot be changed after an environment has been created
				targetEnvTypeListBox.setDisabled(true);						
			}
			
			desktop.removeAttribute("currentImpEnv");
		} else {
			//in the absence of a Target Environment, set the current mode for the composer to CREATE
			this.currentMode = TargetEnvironmentsComposer.CREATE_MODE;
			if(this.isEIPMode()) {
				targetEnvWindow.setTitle("Create Reference Repository");
			} else {
				targetEnvWindow.setTitle("Create Production Environment");
				//pre-select the first in the list of environment types
				targetEnvTypeListBox.setSelectedIndex(0);
			}
			
			
			this.currentEnv = dataManager.newImportEnvironment();
			
		}

	}
	
	
	public void onBlur$targetEnvNameTxtBox() {
		if((targetEnvNameTxtBox.getValue().length() > 0)) {
			uploadLocalProjectBtn.setDisabled(false);
		} else {
			uploadLocalProjectBtn.setDisabled(true);
		}
	}
	
	
	public void onSelect$targetEnvTypeListBox() {
		if((targetEnvTypeListBox.getSelectedIndex() > 0) && (targetEnvNameTxtBox.getValue().length() > 0)) {
			uploadLocalProjectBtn.setDisabled(false);
		} else {
			uploadLocalProjectBtn.setDisabled(true);
		}
	}
	
	
	
	public void onClick$okBtn() {
		try {
		//	Retrieve the object that manages the application data
			ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
			
			if(!this.isEIPMode()) {
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
				
				
				//raise an error if the deployment types is server, but no project name is given
				String deploymentType = (String) targetEnvTypeListBox.getSelectedItem().getValue();
				if(deploymentType.equals(SERVER_DEPLOYMENT_TYPE) && (this.projectNameTxtBox.getValue().isEmpty())) {
					this.displayError("A project name must be given for a server-based environment", "Missing Project Name Error");
					return;
				}
				
				//raise an error if the deployment type is server, but no host name is given
				if(deploymentType.equals(SERVER_DEPLOYMENT_TYPE) && this.serverHostNameTxtBox.getValue().isEmpty()) {
					this.displayError("A server host name must be given for a server-based environment", "Missing Host Name Error");
					return;
				}
				
				//raise an error if the deployment type is server, but no username is given
				if(deploymentType.equals(SERVER_DEPLOYMENT_TYPE) && this.usernameTxtBox.getValue().isEmpty()) {
					this.displayError("A username must be given for a server-based environment", "Missing Username Error");
					return;
				}
				
				//raise an error if the deployment type is server, but no password is given
				if(deploymentType.equals(SERVER_DEPLOYMENT_TYPE) && this.passwordTxtBox.getValue().isEmpty()) {
					this.displayError("A password must be given for a server-based environment", "Missing Password Error");
					return;
				}
				
				//raise an error if the deployment type is server, but the passwords do not match
				if(deploymentType.equals(SERVER_DEPLOYMENT_TYPE) && !this.passwordTxtBox.getValue().equals(this.confirmPasswordTxtBox.getValue())) {
					this.displayError("The passwords do not match for the server-based environment", "Password Mismatch Error");
					return;
				}
				
				
				//raise an error if the deployment type is local, but no file has been uploaded
				if(deploymentType.equals(LOCAL_DEPLOYMENT_TYPE) && this.projectFile == null) {
					this.displayError("A project zip file must be uploaded  for a local, file-based environment", "Missing Project File Error");
					return;
				}
			}
			
			//if all input constraints are met, update or create the Target Environment
			
			if(!this.isEIPMode()) {
				if ((targetEnvNameTxtBox.isValid()) && (targetEnvTypeListBox.getSelectedIndex() > 0)) {
					
					ListModelList listModel = (ListModelList) targetEnvList.getListModel();
					
					//If the dialog is in EDIT MODE, update the details of the current Target Environment
					if(this.currentMode == TargetEnvironmentsComposer.EDIT_MODE) {
								
						if(!this.setEnvironmentDetails(this.currentEnv)) {
							this.displayError("Error saving environment details", "Import Environment Error");
							return;
						}
						
						
						if(this.currentEnv.getImportEnvironmentDeploymentType().equals("LOCAL") && this.projectUploaded) {
							this.saveProject();
						} else {
							Events.sendEvent("onEndSaveProject", targetEnvWindow, null);
						}
						
					}
					
					
					//If the dialog is in CREATE MODE, add the new Target Environment to the config data
					if(this.currentMode == TargetEnvironmentsComposer.CREATE_MODE) {
						
						if(!this.setEnvironmentDetails(this.currentEnv)) {
							this.displayError("Error saving environment details", "Import Environment Error");
							return;
						}
						
						if(this.currentEnv.getImportEnvironmentDeploymentType().equals("LOCAL") && this.projectUploaded) {
							this.saveProject();
						} else {
							Events.sendEvent("onEndSaveProject", targetEnvWindow, null);
						}
						
					}
	
				}
			} else {
				if ((targetEnvNameTxtBox.isValid())) {
					
					ListModelList listModel = (ListModelList) targetEnvList.getListModel();
					
					//If the dialog is in EDIT MODE, update the details of the current Target Environment
					if(this.currentMode == TargetEnvironmentsComposer.EDIT_MODE) {
								
						if(!this.setEnvironmentDetails(this.currentEnv)) {
							this.displayError("Error saving environment details", "Import Environment Error");
							return;
						}
						
						
						if(this.currentEnv.getImportEnvironmentDeploymentType().equals("LOCAL") && this.projectUploaded) {
							this.saveProject();
						} else {
							Events.sendEvent("onEndSaveProject", targetEnvWindow, null);
						}
						
					}
					
					
					//If the dialog is in CREATE MODE, add the new Target Environment to the config data
					if(this.currentMode == TargetEnvironmentsComposer.CREATE_MODE) {
						
						if(!this.setEnvironmentDetails(this.currentEnv)) {
							this.displayError("Error saving environment details", "Import Environment Error");
							return;
						}
						
						if(this.currentEnv.getImportEnvironmentDeploymentType().equals("LOCAL") && this.projectUploaded) {
							this.saveProject();
						} else {
							Events.sendEvent("onEndSaveProject", targetEnvWindow, null);
						}
						
					}
	
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	public void onClick$cancelBtn() {
		try {
		//	this.popupDebugWindow(exportlabel.getValue());
			targetEnvWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	private boolean setEnvironmentDetails(ImportEnvironment anEnv) {
		//set the general details for the environment
		anEnv.setImportEnvironmentName(targetEnvNameTxtBox.getValue());
		anEnv.setImportEnvironmentDescription(targetEnvDescTxtBox.getValue());
//		String envRole = (String) targetEnvRoleListBox.getSelectedItem().getValue();
		anEnv.setImportEnvironmentRole("LIVE");
		
		if(!this.isEIPMode()) {
			String deploymentType = (String) targetEnvTypeListBox.getSelectedItem().getValue();
			anEnv.setImportEnvironmentDeploymentType(deploymentType);
		} else {
			anEnv.setImportEnvironmentDeploymentType(LOCAL_DEPLOYMENT_TYPE);
		}
		
		
		//set a local repository's details
	/*	if(anEnv.getImportEnvironmentDeploymentType().equals(TargetEnvironmentsComposer.LOCAL_DEPLOYMENT_TYPE)) {
			String localPath = localFilePathTxtBox.getValue();
			if((localPath == null) || (localPath.length() == 0) ) {
				return false;
			} else {
				anEnv.setImportEnvironmentRepositoryPath(localPath);
			}
			
		}   */
		
		
		//set a local repository's details
		if(anEnv.getImportEnvironmentDeploymentType().equals(TargetEnvironmentsComposer.SERVER_DEPLOYMENT_TYPE)) {
			String projectName, hostName, username, password, passwordConfirm;
			projectName = projectNameTxtBox.getValue();
			hostName = serverHostNameTxtBox.getValue();
			username = usernameTxtBox.getValue();
			password = passwordTxtBox.getValue();
			passwordConfirm = confirmPasswordTxtBox.getValue();
			if((projectName.length() == 0) || (hostName.length() == 0) || (username.length() == 0) || (password.length() == 0) || (passwordConfirm.length() == 0) ) {
				return false;
			} else {
				if(!passwordConfirm.equals(password)) {
					return false;
				} else {
					anEnv.setProjectName(projectName);
					anEnv.setHostname(hostName);
					anEnv.setUsername(username);
					anEnv.setPassword(password);
				}
			}	
		}
		
		if(!this.isEIPMode()) {
			anEnv.setImportEnvironmentViewerURL(viewerPathTxtBox.getValue());
			anEnv.setViewerPassword(this.viewerPasswordTxtBox.getValue());
			anEnv.setViewerUsername(this.viewerUsernameTxtBox.getValue());
		}
		
		return true;
	}
	
	
	public void onProjectUpload$targetEnvWindow() {
		
		//update the project zip file for the local environment
		this.projectFile = (Media) targetEnvWindow.getAttribute("uploadedProject");
	
		//ACTION: Test if the zip file is valid
		
		this.projectUploaded = true;
	}
	
	
	private void saveProject() {
		
		if (projectFile != null && (projectFile.isBinary())) {	
			this.okBtn.setDisabled(true);
			
			
	        final EventQueue eq = EventQueues.lookup("unzipProjectEvents"); //create a queue
	        
	        //subscribe async listener to handle long operation
	        eq.subscribe(new EventListener() {
	        	
		          public void onEvent(Event evt) {
		            if ("startUnzipProject".equals(evt.getName())) {
		            	try {
		            		//save the zip file to the local cache		            		
		        			dataManager.storeLocalImpEnvProjectZip(currentEnv, projectFile);
		        			dataManager.saveAppData();
		        			if(currentEnv.isUsedAsReference()) {
		        				refreshEssentialClassMap();
		        			}	 
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
	            	Events.sendEvent("onEndSaveProject", targetEnvWindow, null);
	            }
	          }
	        }); //synchronous
	      
	        eq.publish(new Event("startUnzipProject")); //kick off the long operation
		}
	}
	
	public void onEndSaveProject$targetEnvWindow() {
		ListModelList listModel = (ListModelList) targetEnvList.getListModel();
		

		//If the dialog is in EDIT MODE, finalise the update and close the window
		if(this.currentMode == TargetEnvironmentsComposer.EDIT_MODE) {
			//update the list of target environments
			listModel.remove(this.currentEnvIndex);
			listModel.add(this.currentEnvIndex, this.currentEnv);
			this.targetEnvList.setSelectedIndex(this.currentEnvIndex);
			
			//save the config data and close the dialog
			dataManager.saveAppData();
			
			targetEnvWindow.detach();		
			
		} else {
			listModel.add(this.currentEnv);
			this.targetEnvList.setSelectedIndex(listModel.indexOf(this.currentEnv));
			
			EssentialImportUtility configData = dataManager.getImportUtilityData();
			configData.getImportEnvironments().add(this.currentEnv);
			// configData.setReferenceEnvironment(impEnv);
			
			//save the config data and close the dialog
			dataManager.saveAppData();
			
			targetEnvWindow.detach();	
			
		
			
			//If this is the first and only target environment, then notify the parent window to refresh the reference repository
			if(configData.getImportEnvironments().size() == 1) {
				System.out.println("SETTING THE REFERENCE REP");
				configData.setReferenceEnvironment(currentEnv);
				Events.postEvent("onSelectFirstRefEnv", targetEnvList, null); 
			//	this.refreshEssentialClassMap();
			}
		}
		
		//12.05.2014 JWC - add delayed attempt to remove ZIP
		if(projectFile != null)
		{
			String aZipFilename = projectFile.getName();
			projectFile = null;
			dataManager.deleteFileFromEnv(currentEnv, aZipFilename);
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
		catch (ProjectLoadException e) {
			e.printStackTrace();
			Clients.clearBusy();
		} catch (Exception e) {
			// Generic catch block as a catch-all
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
	
	
	

}
