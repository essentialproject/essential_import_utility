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
 * 13.03.2017	JWC	Extended to add support for XML imports
 * 23.06.2018	JWC Add new argument to ListItemRenderer.render()
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
//import org.zkoss.zk.ui.Page;
//import org.zkoss.zk.ui.Path;
//import org.zkoss.zk.ui.Session;
//import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.metainfo.ComponentInfo;
//import org.zkoss.zk.ui.util.Clients;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprise_architecture.essential.importutility.utils.Log;

/**
 * This class is the UI controller for the page used to manage the list of
 * Essential import activities
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @author Jonathan W. Carter <jonathan.carter@e-asolutions.com>
 * @version 1.0 - 28.06.2011<br/>
 * @version 2.0 - 13.03.2017<br/>
 *
 */
public class ManageImportActivityListComposer extends EssentialImportInterface {
	
	public Listbox impActListbox;
	public Window appHomeWin;
	public Window loadingDialog;
	
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();

	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		if(!UserCredentialManager.getInstance(desktop, this.getContextPath()).isAuthenticated(desktop)){
			if (isEIPMode()) {
				execution.sendRedirect("redirectToAuthnServer.zul");
			} else {
				execution.sendRedirect("index.zul");
			}
		    return;
		} 
		
		Component importSettingsTab = appHomeWin.getFellow("importSettingsTabHeader");
		Component systemSettingsTab = appHomeWin.getFellow("systemSettingsTabHeader");
		if(!UserCredentialManager.getInstance(desktop, this.getContextPath()).userIsAdministrator()) {
			importSettingsTab.setVisible(false);
			systemSettingsTab.setVisible(false);
		} else {
			importSettingsTab.setVisible(true);
			systemSettingsTab.setVisible(true);
		}
		
		if(loadingDialog == null) {
			loadingDialog = (Window) Executions.createComponents("/loading_dialog.zul", null, null);
		}
		
		EssentialImportUtility importApp = this.getImportUtilityData();
		
		impActListbox.setModel(new ListModelList(importApp.getImportActivities()));
		
		final boolean isEIPMode = this.isEIPMode();
		impActListbox.setItemRenderer(new ListitemRenderer() {
		    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
		         
		        final ImportActivity impAct = (ImportActivity) data;
		        listItem.setValue(impAct);
		         
		        new Listcell(impAct.getImportActivityName()).setParent(listItem);
		        new Listcell(impAct.getImportActivityType()).setParent(listItem);
		        new Listcell(impAct.getImportActivityDescription()).setParent(listItem);
		        new Listcell(impAct.getImportActivityStatus()).setParent(listItem);
		        
		        //Display the modified date for the Import Activity
		        XMLGregorianCalendar impActModDate = impAct.getImportActivityModifiedDate();
		        if(impActModDate != null) {
		        	new Listcell(formatXMLCalendar(impActModDate)).setParent(listItem);
		        } else {
		        	new Listcell("-").setParent(listItem);
		        }
		        
		        
		        //Display the 'last tested' date for the Import Activity
		        XMLGregorianCalendar impActTestedDate = impAct.getImportActivityTestedDate();
		        if(impActTestedDate != null) {
		        	new Listcell(formatXMLCalendar(impActTestedDate)).setParent(listItem);
		        } else {
		        	new Listcell("Never").setParent(listItem);
		        }
		        
			    if(!isEIPMode) {    
			      //Display the modified date for the Import Activity
			        XMLGregorianCalendar impActExecDate = impAct.getImportActivityToLiveDate();
			        if(impActExecDate != null) {
			        	new Listcell(formatXMLCalendar(impActExecDate)).setParent(listItem);
			        } else {
			        	new Listcell("Never").setParent(listItem);
			        }
		        }
		    }
		   }
		); 
		
		if(impActListbox.getListModel().getSize() > 0) {
			impActListbox.setSelectedIndex(0);
		}
		
		// System.out.println("Current Utility Version: " + appDataManager.getImportUtilityData().getImportUtilityVersion());
	}
	




	public void onClick$delImpBtn() {
		try {
			final int selectedImpActIndex = impActListbox.getSelectedIndex();
			
			if(selectedImpActIndex >=0) {
				final EssentialImportUtility importData = this.getImportUtilityData();
				final ListModelList listModel = (ListModelList) impActListbox.getListModel();
				final ImportActivity currentImpAct = (ImportActivity) listModel.getElementAt(selectedImpActIndex);
				Messagebox.show(("Are you sure that you want to permanently delete the selected Import Activity, \"" + currentImpAct.getImportActivityName() + "\"?" ), "Delete Import Activity?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											listModel.remove(currentImpAct);
											getImportUtilityDataManager().deleteImportActivityAtIndex(selectedImpActIndex);
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
			iuLog.log(Level.SEVERE, "Exception in delete import", e);
			//e.printStackTrace();
		}

	}
	
	
	public void onClick$newImpBtn() {
		try {
			String windowPath;
			if(this.isEIPMode()) {
				windowPath = "/eip_new_import_activity.zul";
			} else {
				windowPath = "/new_import_activity.zul";
			}
			Window newImportDialog = (Window) Executions.createComponents(windowPath, null, null);
			newImportDialog.doModal();
		}
		catch (Exception e) {
			iuLog.log(Level.SEVERE, "Exception in new import", e);
			//e.printStackTrace();
		}

	}
	
	
	public void onClick$fromExcelTemplateBtn() {
		try {
			if(this.getImportUtilityData().getExcelImportTemplates().size() > 0) {
				Window newImportDialog = (Window) Executions.createComponents("/import_activity_from_template_dialog.zul", null, null);
				newImportDialog.doModal();
			}
		}
		catch (Exception e) {
			iuLog.log(Level.SEVERE, "Exception in new from excel template", e);
			//e.printStackTrace();
		}

	}
	
	
	public void onClick$editImpBtn() {
		try {
			int selectedImpActIndex = impActListbox.getSelectedIndex();
			
			if(selectedImpActIndex >=0) {
				EssentialImportUtility importData = this.getImportUtilityData();
				ImportActivity currentImpAct = importData.getImportActivities().get(selectedImpActIndex);
				desktop.setAttribute("currentImpAct", currentImpAct);
				String windowPath;
				if(this.isEIPMode()) {
					windowPath = "/eip_new_import_activity.zul";
				} else {
					windowPath = "/new_import_activity.zul";
				}
				Window newImportDialog = (Window) Executions.createComponents(windowPath, null, null);
				newImportDialog.doModal();
			}
		}
		catch (Exception e) {
			iuLog.log(Level.SEVERE, "Exception in edit import", e);
			//e.printStackTrace();
		}

	}
	
	
	public void onClick$openImpBtn() {
		try {
			int selectedImpActIndex = impActListbox.getSelectedIndex();
			
			if(selectedImpActIndex >=0) {
				EssentialImportUtility importData = this.getImportUtilityData();
				ImportActivity currentImpAct = importData.getImportActivities().get(selectedImpActIndex);
				desktop.getSession().setAttribute("currentImpAct", currentImpAct);
			//	importActivityHome.setClosable(false);
				
				// 07.03.2017 JWC - Not available in XML Import capability
				//System.out.println("Selected Import Type = " + currentImpAct.getImportActivityType());
				if(currentImpAct.getImportActivityType().equalsIgnoreCase("Excel"))
				{
					appHomeWin.detach();
					if(!this.isEIPMode()) {
						Executions.sendRedirect("/run_excel_import.zul");
					} else {
						Executions.sendRedirect("/eip_run_excel_import.zul");
					}
				}
				else
				{					
					// If this is an XML import, show the logs					
					Window manageImportLogsDialog = (Window) Executions.createComponents("/manage_import_logs.zul", appHomeWin, null);		
					manageImportLogsDialog.doModal();
				}
				// 07.03.2017 JWC - End of new section
				
			//	importActivityHome.doEmbedded();
			}
		}
		catch (Exception e) {
			iuLog.log(Level.SEVERE, "Exception in open import", e);
			//e.printStackTrace();
		}

	}
	
	
	public void onEndInit$appHomeWin() {
		loadingDialog.detach();
	}
	
	public void onCreate$appHomeWin() {
	 /*    if (EventQueues.exists("initImportUtilityDataOp")) {
          print("It is busy. Please wait");
          return; //busy
        }  */
		
		try {
    		loadingDialog.doModal();
    	}
    	catch(Exception e) {
    	//	loadingDialog.detach();
    		iuLog.log(Level.SEVERE, "Exception in create app home window", e);
    		//e.printStackTrace();
    	}
		
      
        final EventQueue eq = EventQueues.lookup("initImportUtilityDataEvents"); //create a queue
      
        //subscribe async listener to handle long operation
        eq.subscribe(new EventListener() {
        	
	          public void onEvent(Event evt) {
	            if ("initImportUtilityData".equals(evt.getName())) {
	            	try {
	            		System.out.println("INITIALISING IMPORT UTILITY APPLICATION");
	            	//	print("Loading");
	            		
	            	//	loadingDialog = (Window) Executions.createComponents("/loading_dialog.zul", appHomeWin, null);
	            	//	loadingDialog.doModal();
	            		initImportUtilityAppData();
	            	}
	            	catch(Exception e) {
	            	//	loadingDialog.detach();
	            		iuLog.log(Level.SEVERE, "Exception in onEvent", e);
	            		//e.printStackTrace();
	            	}
	              // org.zkoss.lang.Threads.sleep(3000); //simulate a long operation
	            //  String result = "success"; //store the result
	            	eq.publish(new Event("endImportUtilityData")); //notify it is done
	            }
	          }
	          
        }, true); //asynchronous
      
        //subscribe a normal listener to show the result to the browser
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("endImportUtilityData".equals(evt.getName())) {
            	System.out.println("IMPORT UTILITY APPLICATION INITIALISED");
            //	loadingDialog.detach();
            	Events.sendEvent("onEndInit", appHomeWin, null);
            	EventQueues.remove("initImportUtilityDataEvents");
            }
          }
        }); //synchronous
      
        eq.publish(new Event("initImportUtilityData")); //kick off the long operation
	}

}
