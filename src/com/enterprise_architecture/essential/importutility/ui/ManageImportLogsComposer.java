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
 * 10.03.2017	JWC Added capability to dynamically switch button labels if XML / Excel mode
 * 25.04.2017	JWC Added new button to download a DUP of an XML import activity
 * 23.06.2018	JWC Add new argument to ListItemRenderer.render()
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivityLog;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.global.PromoteRepositoryException;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.data.user.UserDataManager;
import com.enterprise_architecture.essential.importutility.utils.Log;


/**
 * This class manages the modal window for testing Excel Imports against a locally cached Essential repositories
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @author Jonathan W. Carter <jonathan.carter@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 * @version 2.0 - 10.03.2017
 * @version 2.1 - 25.04.2017
 *
 */
public class ManageImportLogsComposer extends EssentialImportInterface {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Window manageImportLogsWindow;
	
	public Label impActNameLbl;
	public Label impActDescLbl;
	public Label impActLastModLbl;
	public Label impActLastTestLbl;
	public Label impActLastRunLbl;
	
	public Label targetEnvNameLbl;
	
	public Button downloadSSBtn;
	public Button downloadImpSpecBtn;
	public Button downloadImpScriptBtn;
	public Button downloadMessagesBtn;
	public Button downloadRepBtn;
	public Button deleteBtn;
	
	public Button promoteToLiveRepBtn;
	public Button downloadXMLDUP;
//	public Button publishToLiveViewerBtn;
//	public Button openLiveViewerBtn;
	
	public Listbox importLogsListBox;

	
	private ImportUtilityDataManager dataManager;
	private UserDataManager userManager;
	
	private static String ENABLED_LABEL_STYLE="font-weight:bold;color:black;";
	private static String DISABLED_LABEL_STYLE="font-weight:bold;color:lightgrey;";
	private ImportActivity currentImpAct;
	
	private ImportActivityLog currentLog;
	private ImportEnvironment targetEnv, sourceEnv;
	boolean sourceEnvIsLocal = true;

	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();

	
	public void doAfterCompose(Component comp) {
		try {
			super.doAfterCompose(comp);
			
			this.dataManager = this.getImportUtilityDataManager();
			this.userManager = this.getUserDataManager();
			
			this.currentImpAct = (ImportActivity) desktop.getSession().getAttribute("currentImpAct");
			
			
			if(currentImpAct == null) {
				this.displayError("No Import Activity Selected", "Invalid Import Activity");
				return;
				//testLocalImportWindow.detach();
			}
			
			//set the Import Activity labels
			this.impActNameLbl.setValue(this.currentImpAct.getImportActivityName());
			this.impActDescLbl.setValue(this.currentImpAct.getImportActivityDescription());
			
			// 10.03.2017 JWC
			// Change the label of Download Spreadsheet if this is an XML activity
			if(this.currentImpAct.getImportActivityType().equals("Automated XML Import"))
			{
				this.downloadSSBtn.setLabel("Download XML...");
				this.downloadImpSpecBtn.setLabel("Download Import Code...");
				this.downloadXMLDUP.setVisible(true);
			}
			else
			{
				this.downloadXMLDUP.setVisible(false);
			}
			
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
	        
	        
	        //set up the list of import logs
	        List importLogs = this.currentImpAct.getImportActivityLogs();
	        ListModelList logListModel = new ListModelList(importLogs);
	        importLogsListBox.setModel(logListModel);
	        importLogsListBox.setItemRenderer(new ListitemRenderer() {
			    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
			    	if(data instanceof ImportActivityLog) {
			    		ImportActivityLog log = (ImportActivityLog) data;
			    		
			    		Listcell typeCell = new Listcell();
			            listItem.appendChild(typeCell);
			            typeCell.appendChild(new Label(log.getLogActivityType()));
			            
			            Listcell targetCell = new Listcell();
			            listItem.appendChild(targetCell);
			            targetCell.appendChild(new Label(log.getLogTargetEnvName()));
			            
			            Listcell userCell = new Listcell();
			            listItem.appendChild(userCell);
			            User aUser = userManager.getUser(log.getLogUser());
			            if(aUser != null) {
			            	userCell.appendChild(new Label(aUser.getFirstname() + " " + aUser.getSurname()));
			            } else {
			            	userCell.appendChild(new Label("-"));
			            }
			            
			            Listcell timeCell = new Listcell();
			            listItem.appendChild(timeCell);
				        XMLGregorianCalendar logDate = log.getLogCreationTime();
				        if(logDate != null) {
				        	timeCell.appendChild(new Label(formatXMLCalendar(logDate)));
				        } else {
				        	timeCell.appendChild(new Label("-"));
				        }

			    	} 
			    }
	        }); 
	        
	        this.disableActionButtons();
	        deleteBtn.setStyle(DISABLED_LABEL_STYLE);
			deleteBtn.setDisabled(true);
	        
			
		} 
		catch(Exception e) {
			iuLog.log(Level.SEVERE, "Exception in doAfterCompose()", e);
			//e.printStackTrace();
		}
	}
	
	
	public void onSelect$importLogsListBox() {
		//enable or disable the appropriate buttons
		int selectedCount = importLogsListBox.getSelectedCount();
		if(selectedCount == 1) {
			this.enableActionButtons();
			this.initPromotionRepSettings();
		} else {
			this.disableActionButtons();
		}
		
		if(selectedCount > 0) {
			deleteBtn.setStyle(ENABLED_LABEL_STYLE);
			deleteBtn.setDisabled(false);
		} else {
			deleteBtn.setStyle(DISABLED_LABEL_STYLE);
			deleteBtn.setDisabled(true);
		}
	}
	
	//Event handler for when the user clicks the Download Spreadsheet button
	public void onClick$downloadSSBtn() {
		//download the spreadsheet associated with the currently selected log
		ListModelList logListModel = (ListModelList) importLogsListBox.getModel();
		Set selectedLogs = logListModel.getSelection();
		if(selectedLogs.size() == 1) {
			Iterator logIter = selectedLogs.iterator();
			ImportActivityLog aLog = (ImportActivityLog) logIter.next();
			String spreadsheetFilePath = aLog.getLogSourceRepositoryPath();
			if(spreadsheetFilePath != null) {
				try {
					Filedownload.save(new File(spreadsheetFilePath), "*");
				}
				catch(Exception e) {
					iuLog.log(Level.SEVERE, "Exception in download spreadsheet button", e);
					//e.printStackTrace();
				}
			}
		}
	}
	
	
	//Event handler for when the user clicks the Download Import Spec button
	public void onClick$downloadImpSpecBtn() {
		//download the import spec associated with the currently selected log
		ListModelList logListModel = (ListModelList) importLogsListBox.getModel();
		Set selectedLogs = logListModel.getSelection();
		if(selectedLogs.size() == 1) {
			Iterator logIter = selectedLogs.iterator();
			ImportActivityLog aLog = (ImportActivityLog) logIter.next();
			String downloadFilePath = aLog.getLogImportSpecPath();
			if(downloadFilePath != null) {
				try {
					Filedownload.save(new File(downloadFilePath), "*");
				}
				catch(Exception e) {
					iuLog.log(Level.SEVERE, "Exception in download Import Spec button", e);
					//e.printStackTrace();
				}
			}
		}
	}
	
	
	
	//Event handler for when the user clicks the Download Import Script button
	public void onClick$downloadImpScriptBtn() {
		//download the import script associated with the currently selected log
		ListModelList logListModel = (ListModelList) importLogsListBox.getModel();
		Set selectedLogs = logListModel.getSelection();
		if(selectedLogs.size() == 1) {
			Iterator logIter = selectedLogs.iterator();
			ImportActivityLog aLog = (ImportActivityLog) logIter.next();
			String downloadFilePath = aLog.getLogImportScriptPath();
			if(downloadFilePath != null) {
				try {
					Filedownload.save(new File(downloadFilePath), "*");
				}
				catch(Exception e) {
					iuLog.log(Level.SEVERE, "Exception in download Import Script button", e);
					//e.printStackTrace();
				}
			}
		}
	}
	
	
	
	//Event handler for when the user clicks the Download Messages button
	public void onClick$downloadMessagesBtn() {
		//download the logged output messages associated with the currently selected log
		ListModelList logListModel = (ListModelList) importLogsListBox.getModel();
		Set selectedLogs = logListModel.getSelection();
		if(selectedLogs.size() == 1) {
			Iterator logIter = selectedLogs.iterator();
			ImportActivityLog aLog = (ImportActivityLog) logIter.next();
			String downloadFilePath = aLog.getLogOutputLogPath();
			if(downloadFilePath != null) {
				try {
					Filedownload.save(new File(downloadFilePath), "*");
				}
				catch(Exception e) {
					iuLog.log(Level.SEVERE, "Exception in dowload messages button", e);
					//e.printStackTrace();
				}
			}
		}
	}
	
	
	//Event handler for when the user clicks the Download Result button
	public void onClick$downloadRepBtn() {
		//download the zipped up resulting repository associated with the currently selected log
		ListModelList logListModel = (ListModelList) importLogsListBox.getModel();
		Set selectedLogs = logListModel.getSelection();
		if(selectedLogs.size() == 1) {
			Iterator logIter = selectedLogs.iterator();
			ImportActivityLog aLog = (ImportActivityLog) logIter.next();
			String downloadFilePath = aLog.getLogUpdatedRepositoryPath();
			if(downloadFilePath != null) {
				try {
					Filedownload.save(new File(downloadFilePath), "*");
				}
				catch(Exception e) {
					iuLog.log(Level.SEVERE, "Exception in dowload resulting repository button", e);
					//e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Handle the button-push for Download DUP for an XML import
	 */
	public void onClick$downloadXMLDUP()
	{
		// Build the DUP file from the selected components
		ListModelList logListModel = (ListModelList) importLogsListBox.getModel();
		Set selectedLogs = logListModel.getSelection();
		if(selectedLogs.size() == 1)
		{
			Iterator logIterator = selectedLogs.iterator();
			ImportActivityLog aLog = (ImportActivityLog) logIterator.next();
			
			// Get the DUP from the resulting logs.			
			// Where is the DUP? - set the location to the download path			
			String downloadFilePath = aLog.getLogXMLDUPPath();
			if(downloadFilePath != null)
			{
				try
				{
					Filedownload.save(new File(downloadFilePath), "*");
				}
				catch(Exception ex)
				{
					iuLog.log(Level.SEVERE, "Exception in download DUP button", ex);
				}
			}
		}
	}
	
	//Event handler for when the user clicks the Delete Selected (Logs) button
	public void onClick$deleteBtn() {
		
		//Confirm that the user wishes to delete the currently selected logs
		try {
		Messagebox.show(("Are you sure that you want to permanently delete the selected Import Logs?" ), "Delete Import Logs?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
				new EventListener() {
					public void onEvent(Event evt) { 
							switch (((Integer)evt.getData()).intValue()) {
								case Messagebox.YES: 
									//delete the currently selected logs
									ListModelList logListModel = (ListModelList) importLogsListBox.getModel();
									Set selectedLogs = logListModel.getSelection();
									if(selectedLogs.size() > 0) {
										Iterator logIter = selectedLogs.iterator();
										while(logIter.hasNext()) {
											ImportActivityLog aLog = (ImportActivityLog) logIter.next();
											int logIndex = currentImpAct.getImportActivityLogs().indexOf(aLog);
											if(logIndex >= 0) {
												dataManager.deleteImportActivityLog(currentImpAct, aLog);
												// logListModel.remove(logIndex);
											}
										}
										importLogsListBox.setModel(new ListModelList(currentImpAct.getImportActivityLogs()));
										
										//disable the buttons if all logs have been deleted
										if(currentImpAct.getImportActivityLogs().size() == 0) {
											disableActionButtons();
											deleteBtn.setStyle(DISABLED_LABEL_STYLE);
											deleteBtn.setDisabled(true);
										}
									}
									break;
								case Messagebox.NO: break;
							}
					}
				}
		);
		} catch (Exception e) {
			iuLog.log(Level.SEVERE, "Exception in delete button callback", e);
			//e.printStackTrace();
		}
		
	}
	
	
	public void onClick$promoteToLiveRepBtn() {
		if(sourceEnvIsLocal && (currentLog != null)) {
			//System.out.println("PROMOTING IMPORT LOG REPOSITORY OUTPUT TO ASSOCIATED LIVE");
			iuLog.log(Level.INFO, "PROMOTING IMPORT LOG REPOSITORY OUTPUT TO ASSOCIATED LIVE");
			
			try {
				String confirmationMessage = "Are you sure that you want to overwrite the " + sourceEnv.getImportEnvironmentName() + " production environment?";
				Messagebox.show((confirmationMessage ), "Promote Test Result to Live?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											try {
												//delete the currently selected logs
												boolean success = dataManager.promoteLogResultToLocalLive(currentLog, sourceEnv);
												
												if(success) {
													dataManager.refreshTestEnvFromLocalLive(targetEnv, sourceEnv);
												} else {
													displayError("Error promoting import log results", "Log Promotion Error");
												}
												promoteToLiveRepBtn.setDisabled(true);
												break;
											} catch(PromoteRepositoryException e) {
												displayError(e.getMessage(),"Log Promotion Error");
												promoteToLiveRepBtn.setDisabled(true);
												break;
											}
											
										case Messagebox.NO: break;
									}
							}
						}
				);
				} catch (Exception e) {
					iuLog.log(Level.SEVERE, "Exception in promote to live button", e);
					//e.printStackTrace();
				}
			
		}
	}
	
	
	private void disableActionButtons() {
		downloadSSBtn.setStyle(DISABLED_LABEL_STYLE);
		downloadSSBtn.setDisabled(true);
		downloadImpSpecBtn.setStyle(DISABLED_LABEL_STYLE);
		downloadImpSpecBtn.setDisabled(true);
		downloadImpScriptBtn.setStyle(DISABLED_LABEL_STYLE);
		downloadImpScriptBtn.setDisabled(true);
		downloadMessagesBtn.setStyle(DISABLED_LABEL_STYLE);
		downloadMessagesBtn.setDisabled(true);
		downloadRepBtn.setStyle(DISABLED_LABEL_STYLE);
		downloadRepBtn.setDisabled(true);
		promoteToLiveRepBtn.setStyle(DISABLED_LABEL_STYLE);
		promoteToLiveRepBtn.setDisabled(true);
		downloadXMLDUP.setStyle(DISABLED_LABEL_STYLE);
		downloadXMLDUP.setDisabled(true);
	}
	
	
	private void enableActionButtons() {
		downloadSSBtn.setStyle(ENABLED_LABEL_STYLE);
		downloadSSBtn.setDisabled(false);
		downloadImpSpecBtn.setStyle(ENABLED_LABEL_STYLE);
		downloadImpSpecBtn.setDisabled(false);
		downloadImpScriptBtn.setStyle(ENABLED_LABEL_STYLE);
		downloadImpScriptBtn.setDisabled(false);
		downloadMessagesBtn.setStyle(ENABLED_LABEL_STYLE);
		downloadMessagesBtn.setDisabled(false);
		downloadRepBtn.setStyle(ENABLED_LABEL_STYLE);
		downloadRepBtn.setDisabled(false);
		downloadXMLDUP.setStyle(ENABLED_LABEL_STYLE);
		downloadXMLDUP.setDisabled(false);
	}
	
	
	public void onClick$closeBtn() {
		//clear up any attributes and then close the dialog
		manageImportLogsWindow.detach();
	}
	
	
	private ImportEnvironment getEnv(String envName) {
		Iterator<ImportEnvironment> envIter = dataManager.getImportUtilityData().getImportEnvironments().iterator();
		ImportEnvironment anEnv;
		while(envIter.hasNext()) {
			anEnv = envIter.next();
			if(anEnv.getImportEnvironmentName().equals(envName)) {
				return anEnv;
			}
		}
		return null;
	}
	
	private void initPromotionRepSettings() {
		ListModelList logListModel = (ListModelList) importLogsListBox.getModel();
		Set selectedLogs = logListModel.getSelection();
		if(selectedLogs.size() == 1) {
			Iterator logIter = selectedLogs.iterator();
			currentLog = (ImportActivityLog) logIter.next();
			String targetEnvName = currentLog.getLogTargetEnvName();
			if(targetEnvName != null) {
				targetEnv = this.getEnv(targetEnvName);
				if(targetEnv != null) {
					String sourceEnvName = targetEnv.getImportEnvironmentLiveSource();
					sourceEnv = this.getEnv(sourceEnvName);
					if(sourceEnv != null) {
						if(sourceEnvIsLocal = sourceEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
							promoteToLiveRepBtn.setStyle(ENABLED_LABEL_STYLE);
							promoteToLiveRepBtn.setDisabled(false);
						}
					} else {
						sourceEnvIsLocal = false;
						promoteToLiveRepBtn.setStyle(DISABLED_LABEL_STYLE);
						promoteToLiveRepBtn.setDisabled(true);
					}				
				} else {
					sourceEnvIsLocal = false;
					promoteToLiveRepBtn.setStyle(DISABLED_LABEL_STYLE);
					promoteToLiveRepBtn.setDisabled(true);
				}
			} else {
				sourceEnvIsLocal = false;
				promoteToLiveRepBtn.setStyle(DISABLED_LABEL_STYLE);
				promoteToLiveRepBtn.setDisabled(true);
			}
		}
	}
	

}
