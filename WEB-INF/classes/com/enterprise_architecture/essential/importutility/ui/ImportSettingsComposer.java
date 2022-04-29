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
 * 23.06.2018	JWC Add new argument to ListItemRenderer.render() 
 * 12.11.2019	JWC EIP Mode, read Repos from EDM via API
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
//import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
//import org.zkoss.zk.ui.Path;
//import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.api.security.repository.Repository;
import com.enterprise_architecture.essential.api.security.repository.TenantRepositories;
import com.enterprise_architecture.essential.base.api.ApiResponse;
import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.GlobalValueTemplate;
//import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.global.SourceRepository;
//import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprise_architecture.essential.importutility.integration.EssentialAPIClient;
import com.enterprise_architecture.essential.importutility.utils.Log;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is the UI controller for the page used to manage global settings
 * that apply for all import activities
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class ImportSettingsComposer extends EssentialImportInterface {
	
	private static final String ESSENTIAL_SYSTEM_REPOSITORY_URI = "/repository-report";

	private static final int SC_OK = 200;
	
	public Tabpanel importSettingsTab;
	public Listbox targetEnvListBox;
	public Listbox valueTemplateListBox;
	public Listbox globalInstListBox;
	public Listbox sourceRepListBox;
	public Window refreshRefRepDialog;
	public Button newRepoBtn;
	public Button editEnvBtn;
	public Button delEnvBtn;
	
	private EssentialImportUtility importAppData;
	private ImportUtilityDataManager dataManager;
	
	private static Logger iuLog = Log.getSystemLogger();
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);

		
	/*	if(!UserCredentialManager.getInstance(desktop, this.getContextPath()).userIsAdministrator()) {
			importSettingsTab.setVisible(false);
		} else {
			importSettingsTab.setVisible(true);
		} */
		
		
		this.dataManager = this.getImportUtilityDataManager();
		importAppData = this.getImportUtilityData();
		
		//Set up the Source Repositories List REMOVED UNTIL REQUIREMENTS VALIDATED
		sourceRepListBox.setModel(new ListModelList(importAppData.getSourceRepositories()));
		sourceRepListBox.setItemRenderer(new ListitemRenderer() {
		    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
		         
		        final SourceRepository sourceRep = (SourceRepository) data;
		        listItem.setValue(sourceRep);
		         
		        new Listcell(sourceRep.getSourceRepositoryName()).setParent(listItem);
		        new Listcell(sourceRep.getSourceRepositoryDescription()).setParent(listItem);

		    }
		   }
		); 
		if(sourceRepListBox.getListModel().getSize() > 0) {
			sourceRepListBox.setSelectedIndex(0);
		}  
		
		final boolean eipMode = this.isEIPMode();		
		//Set up the Target Environments List
		if(!eipMode)
		{
			final List<ImportEnvironment> impEnvList = importAppData.getImportEnvironments();
			targetEnvListBox.setModel(new ListModelList(impEnvList));

			targetEnvListBox.setItemRenderer(new ListitemRenderer() {
				public void render(Listitem listItem, Object data, int theIndex) throws Exception {
		         
			        final ImportEnvironment impEnv = (ImportEnvironment) data;
			        listItem.setValue(impEnv);
			        Checkbox refCheckbox = new Checkbox();
			        if(impEnv.isUsedAsReference() != null) {
			        	refCheckbox.setChecked(impEnv.isUsedAsReference());
			        }
			        
			        refCheckbox.addEventListener("onCheck", new EventListener(){
			            public void onEvent(Event event) throws Exception {
			            	//update the isUsedAsReference attributes of the other Target Environments
			            	impEnv.setUsedAsReference(true);
			            	int impEnvIndex = impEnvList.indexOf(impEnv);
			            	if(impEnvIndex >= 0) {
			            	/*	getImportUtilityDataManager().updateReferenceImportEnvironments(impEnvIndex);
			            		getImportUtilityDataManager().saveAppData();
			            		refreshEssentialClassMap(); */
			            		refreshReferenceRepository(impEnvIndex);
			            	}           	
			            }
			        });
			        Listcell checkboxListCell = new Listcell();
			        checkboxListCell.setParent(listItem);
			        checkboxListCell.appendChild(refCheckbox);
			        new Listcell(impEnv.getImportEnvironmentName()).setParent(listItem);
			        
			        //Factor in the usage mode of the import utility
			        if(!eipMode) {
			        	new Listcell(impEnv.getImportEnvironmentRole()).setParent(listItem);
			        }
			        
			        //Factor in the usage mode of the import utility
			        if(!eipMode) {
			        	new Listcell(impEnv.getImportEnvironmentDeploymentType()).setParent(listItem);
			        }
			        
			        new Listcell(impEnv.getImportEnvironmentDescription()).setParent(listItem);
			        
			        if(!eipMode) {
			        	new Listcell(impEnv.getImportEnvironmentViewerURL()).setParent(listItem);
			        }
			    }
			   }
			);
		}
		// New EIP-mode behaviour: Get the repository list from the EDM via an API call
		else
		{
			// Prepare the UI - using extended ImportEnvironment class, EipImportEnvironment
			final List<ImportEnvironment> impEnvList = importAppData.getImportEnvironments();
			targetEnvListBox.setModel(new ListModelList<ImportEnvironment>(impEnvList));
			
			// If there is no selected reference repository, select 1st
			//if(targetEnvListBox.getModel().getSize() == 0)
			//{
			//	System.out.println("No reference repositories in saved content");
			//}			
			
			// If there is a selected reference repository, use that one
			//System.out.println("Requesting set of reference repositories from EDM");
			
			// Get the list of possible repositories from an API Request
			EssentialAPIClient anAPIClient = new EssentialAPIClient(desktop.getWebApp().getServletContext());
			ApiResponse aResponse = anAPIClient.getEssentialAPI(ESSENTIAL_SYSTEM_REPOSITORY_URI);
			
			//System.out.println("Got response status code from Repositories API: " + aResponse.getStatusCode());
			//System.out.println("Got JSON response from Repositories API: " + aResponse.getJsonResponse().toString());
			
			// Parse response - if successful read the JSON returned
			if(aResponse.getStatusCode() == SC_OK)
			{
				System.out.println("Processing received JSON");
				String aRepositoriesJson = aResponse.getJsonResponse();
				ObjectMapper aJsonMapper = new ObjectMapper();
				
				// Save current selection
				//System.out.println("Finding current selection...");
				Iterator<ImportEnvironment> anImpEnvIt = impEnvList.iterator();
				String aSelectEnvId = "";
				while(anImpEnvIt.hasNext())
				{
					ImportEnvironment anEnv = anImpEnvIt.next();
					if(anEnv != null)
					{						
						//System.out.println("Found reference repository: " + anEnv.getImportEnvironmentName() + " reference: " + anEnv.isUsedAsReference());
						if(anEnv.isUsedAsReference() == Boolean.TRUE)
						{
							aSelectEnvId = anEnv.getImportEnvironmentId();
						}
					}
				}
				
				try
				{
					//System.out.println("Clearing impEnvList...");
					impEnvList.clear();
					//System.out.println("Reset the list in readiness");
					TenantRepositories aTenantRepos = aJsonMapper.readValue(aRepositoriesJson, TenantRepositories.class);
					Iterator<Repository> aRepoIt = aTenantRepos.getRepositories().iterator();
					while(aRepoIt.hasNext())
					{
						// Get the repo information 
						Repository aRepo = aRepoIt.next();
						
						// Create a new EipImportEnvironment from the Repo information
						ImportEnvironment anEipImportEnv = new ImportEnvironment();
						anEipImportEnv.setImportEnvironmentId(aRepo.getId());
						anEipImportEnv.setImportEnvironmentName(aRepo.getName());
						anEipImportEnv.setImportEnvironmentDescription(aRepo.getName() + " Repository");
						
						// Check to see if this was the previously selected reference repo
						if(anEipImportEnv.getImportEnvironmentId().equals(aSelectEnvId))
						{
							anEipImportEnv.setUsedAsReference(true);
						}
						// Got the info, so load the UI components
						impEnvList.add(anEipImportEnv);						
					}						
				}
				catch (Exception anEx)
				{
					iuLog.log(Level.ALL, "Exception while requesting list of target repositories: " + anEx.getMessage());
				}
				
				//System.out.println("impEnvList size is now: " + impEnvList.size());				
				targetEnvListBox.setModel(new ListModelList<ImportEnvironment>(impEnvList));
								
				// Set up the event listeners etc.
				targetEnvListBox.setItemRenderer(new ListitemRenderer() 
				{					
					public void render(Listitem listItem, Object data, int theIndex) throws Exception 
					{
			         
				        final ImportEnvironment impEnv = (ImportEnvironment) data;
				        listItem.setValue(impEnv);
				        Checkbox refCheckbox = new Checkbox();
				        if(impEnv.isUsedAsReference() != null) 
				        {
				        	refCheckbox.setChecked(impEnv.isUsedAsReference());
				        }
				        
				        refCheckbox.addEventListener("onCheck", new EventListener()
				        {
				        	// Update the model etc. when a repository is selected...
				            public void onEvent(Event event) throws Exception 
				            {
				            	//update the isUsedAsReference attributes of the other Target Environments
				            	//System.out.println(">>> onEvent(). Selecting: " + impEnv.getImportEnvironmentId() + " : " + impEnv.getImportEnvironmentName());
				            	impEnv.setUsedAsReference(true);
				            	int impEnvIndex = impEnvList.indexOf(impEnv);
				            	if(impEnvIndex >= 0) 
				            	{				            	
				            		refreshReferenceRepository(impEnvIndex);
				            	}           	
				            }
				        });
				        
				        Listcell checkboxListCell = new Listcell();
				        checkboxListCell.setParent(listItem);
				        checkboxListCell.appendChild(refCheckbox);
				        new Listcell(impEnv.getImportEnvironmentName()).setParent(listItem);
				        
				        new Listcell(impEnv.getImportEnvironmentDescription()).setParent(listItem);
					}
				});
			}
			else
			{
				if(aResponse.getStatusCode() == HttpServletResponse.SC_FORBIDDEN)
				{
					// If we got a forbidden (403), force a re-login					
					Executions.getCurrent().getSession().invalidate();
					execution.getSession().invalidate();
					Executions.sendRedirect("redirectToAuthnServer.zul");
				}
				// Raise errors if not successful
				iuLog.log(Level.SEVERE, "Request to access list of repositories for tenant refused. Return code: " + aResponse.getStatusCode());
				iuLog.log(Level.SEVERE, "Error message (JSON): " + aResponse.getJsonResponse());
			}
		}
		
		if(targetEnvListBox.getListModel().getSize() > 0) {
			targetEnvListBox.setSelectedIndex(0);
		}
		
		// If in EIP mode, hide the New Edit and Delete buttons
		if(eipMode)
		{
			// Hide the buttons as the reference repositories are pulled automatically
			this.newRepoBtn.setVisible(false);
			this.editEnvBtn.setVisible(false);
			this.delEnvBtn.setVisible(false);			
		}		
		
		//Set up the Global Value Templates List
		valueTemplateListBox.setModel(new ListModelList(importAppData.getGlobalValueTemplates()));	
		valueTemplateListBox.setItemRenderer(new ListitemRenderer() {
		    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
		         
		        final GlobalValueTemplate valTemp = (GlobalValueTemplate) data;
		        listItem.setValue(valTemp);
		         
		        new Listcell(valTemp.getGlobalValueTemplateClass()).setParent(listItem);
		        new Listcell(valTemp.getGlobalValueTemplateSlot()).setParent(listItem);
		        
		        DerivedValue valueTempDef = valTemp.getGlobalValueTemplateValue();
		        String valTempDef = derivedValuetoString(valueTempDef);
		        
		        new Listcell(valTempDef).setParent(listItem);

		    }
		   }
		); 
		if(valueTemplateListBox.getListModel().getSize() > 0) {
			valueTemplateListBox.setSelectedIndex(0);
		}
		
		
		//Set up the Global Instances List
		globalInstListBox.setModel(new ListModelList(importAppData.getGlobalInstances()));	
		globalInstListBox.setItemRenderer(new ListitemRenderer() {
		    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
		         
		        final DerivedInstanceType globInst = (DerivedInstanceType) data;
		        listItem.setValue(globInst);
		         
		        new Listcell(globInst.getVariableName()).setParent(listItem);
		        new Listcell(globInst.getClassName()).setParent(listItem);
		        
		        DerivedValue globInstName = globInst.getDerivedName();
		        if(globInstName != null) {
		        	String nameString = derivedValuetoString(globInstName);   
		        	new Listcell(nameString).setParent(listItem);
		        }

		    }
		   }
		); 
		if(globalInstListBox.getListModel().getSize() > 0) {
			globalInstListBox.setSelectedIndex(0);
		}
		
	}
	
	
	public void onSelectFirstRefEnv$targetEnvListBox() {
		try {
			if(targetEnvListBox.getListModel().getSize() == 1) {
				refreshReferenceRepository(0);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$newRepBtn() {
		try {
			Window neRepDialog = (Window) Executions.createComponents("/source_repository_dialog.zul", null, null);
			neRepDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$editRepBtn() {
		try {
			int selectedSrcRepIndex = sourceRepListBox.getSelectedIndex();
			
			if(selectedSrcRepIndex >=0) {
				EssentialImportUtility importData = this.getImportUtilityData();
				SourceRepository currentSrcRep = importData.getSourceRepositories().get(selectedSrcRepIndex);
				desktop.getSession().setAttribute("currentSrcRep", currentSrcRep);
				Window editRepDialog = (Window) Executions.createComponents("/source_repository_dialog.zul", null, null);
				editRepDialog.doModal();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$delRepBtn() {
		try {
			final int selectedSrcRepIndex = sourceRepListBox.getSelectedIndex();
			final EssentialImportUtility importData = this.getImportUtilityData();
			final ListModelList listModel = (ListModelList) sourceRepListBox.getListModel();
			final SourceRepository currentSrcRep = (SourceRepository) listModel.getElementAt(selectedSrcRepIndex);
			
			
			if(selectedSrcRepIndex >=0) {
			
				Messagebox.show(("Are you sure that you want to permanently delete the selected Source Repository, \"" + currentSrcRep.getSourceRepositoryName() + "\"?" ), "Delete Source Repository?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											listModel.remove(currentSrcRep);
											importData.getSourceRepositories().remove(selectedSrcRepIndex);
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
	
	
	public void onClick$newRepoBtn() {
		try {
			Window newEnvDialog = (Window) Executions.createComponents("/eip_repo_dialog.zul", null, null);
			newEnvDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$newLiveEnvBtn() {
		try {
			Window newEnvDialog = (Window) Executions.createComponents("/target_env_dialog.zul", null, null);
			newEnvDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$newDevQAEnvBtn() {
		try {
			Window newEnvDialog = (Window) Executions.createComponents("/test_env_dialog.zul", null, null);
			newEnvDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$editEnvBtn() {
		try {
			int selectedImpEnvIndex = targetEnvListBox.getSelectedIndex();
			
			if(selectedImpEnvIndex >=0) {
				EssentialImportUtility importData = this.getImportUtilityData();
				ImportEnvironment currentImpEnv = importData.getImportEnvironments().get(selectedImpEnvIndex);
				desktop.setAttribute("currentImpEnv", currentImpEnv);
				String editWindowPath;
				if(currentImpEnv.getImportEnvironmentRole().equals("LIVE")) {
					if(!this.isEIPMode()) {
						editWindowPath = "/target_env_dialog.zul";
					} else {
						editWindowPath = "/eip_repo_dialog.zul";
					}
				} else {
					editWindowPath = "/test_env_dialog.zul";
				}
				Window editEnvDialog = (Window) Executions.createComponents(editWindowPath, null, null);
				editEnvDialog.doModal();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$delEnvBtn() {
		try {
			final int selectedTargetEnvIndex = targetEnvListBox.getSelectedIndex();
			final EssentialImportUtility importData = this.getImportUtilityData();
			final ListModelList listModel = (ListModelList) targetEnvListBox.getListModel();
			final ImportEnvironment currentImpEnv = (ImportEnvironment) listModel.getElementAt(selectedTargetEnvIndex);
			
			
			
			if(checkLastLiveTargetEnv(currentImpEnv)) {
				if(!this.isEIPMode()) {
					this.displayError("At least one live environment must be defined", "Delete Target Environment Error");
				} else {
					this.displayError("At least one reference repository must be defined", "Delete Reference Repository Error");
				}
				return;
			}
			
			
			ImportEnvironment refEnv = dataManager.getReferenceImportEnvironment();
			if(refEnv != null && currentImpEnv.getId() == refEnv.getId()) {
				this.displayError("You must select an alternative reference environment before deleting", "Delete Reference Environment Error");
				return;
			}
			
			
			if(selectedTargetEnvIndex >=0) {
				
				Messagebox.show(("Are you sure that you want to permanently delete the selected Target Environment, \"" + currentImpEnv.getImportEnvironmentName() + "\"?" ), "Delete Target Environment?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											listModel.remove(currentImpEnv);
											dataManager.deleteImportEnvironmentAtIndex(selectedTargetEnvIndex);
											// importData.getImportEnvironments().remove(selectedTargetEnvIndex);
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
	
	
	
	public void onClick$newValTempBtn() {
		try {
			Window newValTempDialog = (Window) Executions.createComponents("/value_template_dialog.zul", null, null);
			newValTempDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$editValTempBtn() {
		try {
			int selectedValTempIndex = valueTemplateListBox.getSelectedIndex();
			
			if(selectedValTempIndex >=0) {
				EssentialImportUtility importData = this.getImportUtilityData();
				GlobalValueTemplate currentValTemp = importData.getGlobalValueTemplates().get(selectedValTempIndex);
				desktop.setAttribute("currentValueTemp", currentValTemp);
				Window editValTempDialog = (Window) Executions.createComponents("/value_template_dialog.zul", null, null);
				editValTempDialog.doModal();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$delValTempBtn() {
		try {
			final int selectedValTempIndex = valueTemplateListBox.getSelectedIndex();
			
			if(selectedValTempIndex >=0) {
				final ListModelList listModel = (ListModelList) valueTemplateListBox.getListModel();
				final GlobalValueTemplate currentValTemp = (GlobalValueTemplate) listModel.getElementAt(selectedValTempIndex);
				Messagebox.show(("Are you sure that you want to permanently delete the selected Global Value Template, \"" + currentValTemp.getGlobalValueTemplateClass() + ", " + currentValTemp.getGlobalValueTemplateSlot() + "\"?" ), "Delete Global Vaue Template?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											listModel.remove(currentValTemp);
											getImportUtilityDataManager().removeGlobalValueTemplate(currentValTemp);
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
	
	
	public void onClick$newGlobInstBtn() {
		try {
			Window newGlobInstDialog = (Window) Executions.createComponents("/global_instance_dialog.zul", null, null);
			newGlobInstDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$editGlobInstBtn() {
		try {
			int selectedGlobInstIndex = globalInstListBox.getSelectedIndex();
			
			if(selectedGlobInstIndex >=0) {
				EssentialImportUtility importData = this.getImportUtilityData();
				DerivedInstanceType currentGlobInst = importData.getGlobalInstances().get(selectedGlobInstIndex);
				desktop.setAttribute("currentGlobalInstance", currentGlobInst);
				Window editGlobInstDialog = (Window) Executions.createComponents("/global_instance_dialog.zul", null, null);
				editGlobInstDialog.doModal();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$delGlobInstBtn() {
		try {
			final int selectedGlobInstIndex = globalInstListBox.getSelectedIndex();
			
			if(selectedGlobInstIndex >=0) {
				final EssentialImportUtility importData = this.getImportUtilityData();
				final ListModelList listModel = (ListModelList) globalInstListBox.getListModel();
				final DerivedInstanceType currentGlobInst = importData.getGlobalInstances().get(selectedGlobInstIndex);
				Messagebox.show(("Are you sure that you want to permanently delete the selected Global Instance, \"" + currentGlobInst.getVariableName() + "\"?" ), "Delete Target Environment?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											listModel.remove(currentGlobInst);
											importData.getGlobalInstances().remove(selectedGlobInstIndex);
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
	
	
	public void onEndInit$importSettingsTab() {
		refreshRefRepDialog.detach();
	}
	
	public void refreshReferenceRepository(int impEnvIndex) {

		final EventQueue eq = EventQueues.lookup("initImportUtilityDataEvents"); //create a queue
        final int envIndex = impEnvIndex;
		//System.out.println(">> In refreshReferenceRepository(). Index = " + impEnvIndex);
		targetEnvListBox.setModel(targetEnvListBox.getModel());
		
		if(isEIPMode())
		{
			getImportUtilityDataManager().updateReferenceEipImportEnvironments(envIndex);
		}
		else
		{
			getImportUtilityDataManager().updateReferenceImportEnvironments(envIndex);
		}
		
		//System.out.println("Saving the new selection in saveAppData()");
		
		getImportUtilityDataManager().saveAppData();		
		
		//System.out.println("Saved.");
		
		try {
			refreshRefRepDialog = (Window) Executions.createComponents("/refreshing_refrep_dialog.zul", importSettingsTab, null);
    		refreshRefRepDialog.doModal();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
		//System.out.println("Subscribing to async events...");
		
        //subscribe async listener to handle long operation
        eq.subscribe(new EventListener() {
        	
	          public void onEvent(Event evt) {
	            if ("initImportUtilityData".equals(evt.getName())) {
	            	try {
	            		refreshEssentialClassMap();
	            	}
	            	catch(Exception e) {
	            	//	loadingDialog.detach();
	            		e.printStackTrace();
	            	}
	            	eq.publish(new Event("endImportUtilityData")); //notify it is done
	            }
	          }
        }, true); //asynchronous
      
        //subscribe a normal listener to show the result to the browser
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("endImportUtilityData".equals(evt.getName())) {
            	//System.out.println("REFERENCE REPOSITORY REFRESHED");
            	Events.sendEvent("onEndInit", importSettingsTab, null);
            	EventQueues.remove("initImportUtilityDataEvents");
            }
          }
        }); //synchronous
      
        eq.publish(new Event("initImportUtilityData")); //kick off the long operation
	}
	
	private boolean checkLastLiveTargetEnv(ImportEnvironment selectedEnv) {
		int total = 0;
		Iterator<ImportEnvironment> envIter = this.getImportUtilityData().getImportEnvironments().iterator();
		ImportEnvironment anEnv, lastLiveEnv = null;
		while(envIter.hasNext()) {
			anEnv = envIter.next();
			if(anEnv.getImportEnvironmentRole().equals(ImportUtilityDataManager.IMPORT_ENV_PROD_ROLE)) {
				total++;
				lastLiveEnv = anEnv;
			}
		}
		if(lastLiveEnv == null) {
			return true;
		}
		return (total == 1 && selectedEnv.getId().equals(lastLiveEnv.getId()));
	}

}
