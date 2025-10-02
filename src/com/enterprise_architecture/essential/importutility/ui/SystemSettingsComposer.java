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
 * 20.06.2014	JWC Added the save / migrate configuration
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.File;
import java.util.List;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ExcelImportTemplate;
import com.enterprise_architecture.essential.importutility.data.global.ImportScriptTemplate;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.data.user.UserDataManager;

/**
 * This class is the UI controller for the page used to manage system settings
 * that are used by the import utility a cross import activities
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class SystemSettingsComposer extends EssentialImportInterface {
	
	public Listbox usersListBox;
	public Listbox impScriptTempListBox;
	public Listbox excelImpTempListBox;
	
	public Window appHomeWin;
	
	private ImportUtilityDataManager appDataManager;
	private UserDataManager userDataManager;
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		//get the data managers for the application
		EssentialImportUtility importAppData = this.getImportUtilityData();
		EssentialImportUtility importSystemData = this.getSystemData();
		appDataManager = this.getImportUtilityDataManager();
		userDataManager = this.getUserDataManager();
		
		
		
		//Set up the list of Users
		if(!this.isEIPMode()) {
			usersListBox.setModel(new ListModelList(userDataManager.getUsers()));
			
			usersListBox.setItemRenderer(new ListitemRenderer() {
			    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
			         
			        final User aUser = (User) data;
			        listItem.setValue(aUser);
			         
			        new Listcell(aUser.getFirstname()).setParent(listItem);
			        new Listcell(aUser.getSurname()).setParent(listItem);
			        new Listcell(aUser.getRole()).setParent(listItem);
			    }
			   }
			); 
			
			if(usersListBox.getListModel().getSize() > 0) {
				usersListBox.setSelectedIndex(0);
			}
		
		
		//Set up the list of Jython Import Script Templates
		impScriptTempListBox.setModel(new ListModelList(this.getSystemData().getImportScriptTemplates()));
		
		impScriptTempListBox.setItemRenderer(new ListitemRenderer() {
		    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
		         
		        final ImportScriptTemplate impScriptTemp = (ImportScriptTemplate) data;
		        listItem.setValue(impScriptTemp);
		         
		        new Listcell(impScriptTemp.getImportScriptTemplateClassName()).setParent(listItem);
		        new Listcell(impScriptTemp.getImportScriptTemplateMode()).setParent(listItem);
		        new Listcell(impScriptTemp.getImportScriptTemplateString()).setParent(listItem);
		    }
		   }
		); 
		
		if(impScriptTempListBox.getListModel().getSize() > 0) {
			impScriptTempListBox.setSelectedIndex(0);
		}
		
		
		//Set up the list of Excel Import Activity Templates
			excelImpTempListBox.setModel(new ListModelList(importAppData.getExcelImportTemplates()));
			
			excelImpTempListBox.setItemRenderer(new ListitemRenderer() {
			    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
			         
			        final ExcelImportTemplate impActivityTemp = (ExcelImportTemplate) data;
			        listItem.setValue(impActivityTemp);
			         
			        new Listcell(impActivityTemp.getExcelImportTemplateName()).setParent(listItem);
			        new Listcell(impActivityTemp.getExcelImportTemplateDescription()).setParent(listItem);
			        new Listcell(impActivityTemp.getExcelImportTemplateExcelFilename()).setParent(listItem);
			        new Listcell(impActivityTemp.getExcelImportTemplateImportSpecFilename()).setParent(listItem);
			    }
			   }
			); 
			
			if(excelImpTempListBox.getListModel().getSize() > 0) {
				excelImpTempListBox.setSelectedIndex(0);
			}
		}
	}
	
	
	
	public void onClick$delUserBtn() {
		if(this.usersListBox.getItemCount() <= 1) {
			this.displayError("At least one User must be defined", "Delete User Error");
			return;
		}
		try {
			final int selectedUserIndex = usersListBox.getSelectedIndex();
			
			if(selectedUserIndex >=0) {
				final ListModelList listModel = (ListModelList) usersListBox.getListModel();
				final User currentUser = (User) listModel.getElementAt(selectedUserIndex);
				Messagebox.show(("Are you sure that you want to permanently delete the selected User, \"" + currentUser.getFirstname() + " " + currentUser.getSurname() + "\"?" ), "Delete User?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											listModel.remove(currentUser);
											userDataManager.deleteUser(currentUser);
											userDataManager.saveUserData();
											break;
										case Messagebox.NO: break;
									}
							}
						}
				);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$newUserBtn() {
		try {
			Window userDialog = (Window) Executions.createComponents("/user_dialog.zul", null, null);
			userDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$editUserBtn() {
		try {
			int selectedUserIndex = this.usersListBox.getSelectedIndex();
			
			if(selectedUserIndex >=0) {
				List<User> users = userDataManager.getUsers();
				User currentUser = users.get(selectedUserIndex);
				desktop.getSession().setAttribute("currentUserForEditing", currentUser);
				Window userDialog = (Window) Executions.createComponents("/user_dialog.zul", null, null);
				userDialog.doModal();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public void onClick$delImpScriptBtn() {
		try {
			final int selectedImpScriptTemplateIndex = impScriptTempListBox.getSelectedIndex();
			
			if(selectedImpScriptTemplateIndex >=0) {
				final ListModelList listModel = (ListModelList) impScriptTempListBox.getListModel();
				final ImportScriptTemplate currentImpScriptTemp = (ImportScriptTemplate) listModel.getElementAt(selectedImpScriptTemplateIndex);
				Messagebox.show(("Are you sure that you want to permanently delete the selected Jythin Script Template for \"" + currentImpScriptTemp.getImportScriptTemplateClassName() + "\"?" ), "Delete Jython Script Template?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											listModel.remove(currentImpScriptTemp);
											getImportUtilityDataManager().removeImportScriptTemplate(currentImpScriptTemp);
											saveData();
											break;
										case Messagebox.NO: break;
									}
							}
						}
				);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$newImpScriptBtn() {
		try {
			Window newImportScriptTemplateDialog = (Window) Executions.createComponents("/import_script_template_dialog.zul", null, null);
			newImportScriptTemplateDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$editImpScriptBtn() {
		try {
			int selectedImpScriptTempIndex = impScriptTempListBox.getSelectedIndex();
			
			if(selectedImpScriptTempIndex >=0) {
				EssentialImportUtility importSystemData = this.getSystemData();
				ImportScriptTemplate currentImpScriptTemp = importSystemData.getImportScriptTemplates().get(selectedImpScriptTempIndex);
				desktop.getSession().setAttribute("currentImportScriptTemplate", currentImpScriptTemp);
				Window newImportScriptTempDialog = (Window) Executions.createComponents("/import_script_template_dialog.zul", null, null);
				newImportScriptTempDialog.doModal();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void onClick$delExcelImpTempBtn() {
		try {
			final int selectedImpActTemplateIndex = excelImpTempListBox.getSelectedIndex();
			
			if(selectedImpActTemplateIndex >=0) {
				final ListModelList listModel = (ListModelList) excelImpTempListBox.getListModel();
				final ExcelImportTemplate currentImpActTemp = (ExcelImportTemplate) listModel.getElementAt(selectedImpActTemplateIndex);
				Messagebox.show(("Are you sure that you want to permanently delete the selected Import Activity Template, \"" + currentImpActTemp.getExcelImportTemplateName() + "\"?" ), "Delete Import Activity Template?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											listModel.remove(currentImpActTemp);
											getImportUtilityDataManager().deleteExcelImportActivityTemplateAtIndex(selectedImpActTemplateIndex);
											saveData();
											break;
										case Messagebox.NO: break;
									}
							}
						}
				);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$newExcelImpTempBtn() {
		try {
			Window newImportActTemplateDialog = (Window) Executions.createComponents("/import_activity_template_dialog.zul", null, null);
			newImportActTemplateDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$editExcelImpTempBtn() {
		try {
			int selectedImpActTempIndex = excelImpTempListBox.getSelectedIndex();
			
			if(selectedImpActTempIndex >=0) {
				EssentialImportUtility importData = this.getImportUtilityData();
				ExcelImportTemplate currentImpActTemp = importData.getExcelImportTemplates().get(selectedImpActTempIndex);
				desktop.setAttribute("currentExcelImpTemp", currentImpActTemp);
				Window newImportActTempDialog = (Window) Executions.createComponents("/import_activity_template_dialog.zul", null, null);
				newImportActTempDialog.doModal();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Export an archive containing the relevant, local configuration
	 * 
	 */
	public void onClick$backupConfigBtn() 
	{
		try 
		{
			String configFilePath = this.getImportUtilityDataManager().saveConfigPackage();
			
			if(configFilePath != null) 
			{
				try 
				{
					Filedownload.save(new File(configFilePath), "zip");
				}
				catch(Exception e) 
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Import the uploaded package and reset the environment with the new configuration
	 */
	public void onConfigPackageUpload$appHomeWin()
	{
		Media configPackage = (Media) appHomeWin.getAttribute("uploadedConfig");
		boolean isAppReInitialised = appDataManager.deployConfigPackage(configPackage.getStreamData());
		
		if(isAppReInitialised)
		{

			// Reset the reference meta model from the imported configuration
			refreshEssentialClassMap();
			
			// Refresh the user configuration
			userDataManager.reInitialiseFromConfig();
			if(!isEIPMode())
			{
				// If we're not in EIP mode, refresh the user directory
				usersListBox.setModel(new ListModelList(userDataManager.getUsers()));
			}
			
			// Refresh the Import Settings / Source Repositories
			Listbox sourceRepsList = (Listbox) Path.getComponent("//appHome/appHomeWin/sourceRepListBox");
			sourceRepsList.setModel(new ListModelList(appDataManager.getImportUtilityData().getSourceRepositories()));
			
			// Refresh the Import Settings / Target Environments
			Listbox targetEnvList = (Listbox) Path.getComponent("//appHome/appHomeWin/targetEnvListBox");
			targetEnvList.setModel(new ListModelList(appDataManager.getImportUtilityData().getImportEnvironments()));
			
			// Refresh the Import Settings / Value Templates
			Listbox valueTemplateList = (Listbox) Path.getComponent("//appHome/appHomeWin/valueTemplateListBox");
			valueTemplateList.setModel(new ListModelList(appDataManager.getImportUtilityData().getGlobalValueTemplates()));
			
			// Refresh the Import Settings / Global Instances
			Listbox globalInstancesList = (Listbox) Path.getComponent("//appHome/appHomeWin/globalInstListBox");
			globalInstancesList.setModel(new ListModelList(appDataManager.getImportUtilityData().getGlobalInstances()));
			
			// Refresh the Manage Imports / Import Activities
			Listbox importActivityList = (Listbox) Path.getComponent("//appHome/appHomeWin/impActListbox");
			importActivityList.setModel(new ListModelList(appDataManager.getImportUtilityData().getImportActivities()));
				
			appHomeWin.invalidate();
		}
	}
	
	/*
	
	public void onChange$refPathTxtBox() {
		try {
			//get the data manager for the application
			EssentialImportUtility importAppData = this.getImportUtilityData();
			ImportEnvironment refEnv = importAppData.getReferenceEnvironment();
			String newRefEnvPath = refPathTxtBox.getValue();
			if(newRefEnvPath != null && newRefEnvPath.length() > 0) {
				refEnv.setImportEnvironmentRepositoryPath(refPathTxtBox.getValue());
				this.saveData();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	} */
	
	
	
	/*
	public void onClick$testRefEnvBtn() {
		try {
		//	System.out.println("Test Reference Button Pressed Again");
		//	this.getImportUtilityDataManager().initEssentialClassMap();
			
			ProtegeIntegrationManager protegeManager = new ProtegeIntegrationManager();
			ImportEnvironment currentRefEnv = this.getImportUtilityData().getReferenceEnvironment();
			
			List errorList = protegeManager.testProtegeConnection(currentRefEnv);
			if(errorList.size() == 0) {
				Messagebox.show("Project Tested Successfully", "Success", Messagebox.OK, Messagebox.INFORMATION);
			} else {
				Messagebox.show("Errors accessing Protege project", "Test Failed", Messagebox.OK, Messagebox.ERROR);
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

	}  */
	
}
