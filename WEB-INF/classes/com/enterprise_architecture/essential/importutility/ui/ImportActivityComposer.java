/**
 * Copyright (c)2009-2017 Enterprise Architecture Solutions Ltd.
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
 * 07.03.2017	JWC Added the XML capability
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

//import java.io.File;
//import java.io.FileInputStream;
import java.util.ArrayList;
//import java.util.Date;
import java.util.Iterator;
import java.util.List;

//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.datatype.XMLGregorianCalendar;

import java.util.logging.Level;
import java.util.logging.Logger;


//import org.zkoss.poi.hslf.model.TextBox;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Execution;
//import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
//import org.zkoss.zk.ui.Session;
//import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.event.EventListener;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.global.SourceRepository;
import com.enterprise_architecture.essential.importutility.utils.Log;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * import activities
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @author Jonathan Carter <jonathan.carter@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 * @version 2.0 - 07.03.2017
 *
 */
public class ImportActivityComposer extends EssentialImportInterface {
	
	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private int currentMode;
	private int currentImpIndex;
	private ImportActivity currentImpAct;
	private ImportUtilityDataManager dataManager;
	
	public Button okBtn;
	public Button cancelBtn;
	public Textbox impActNameTxtBox;
	public Textbox impActDescTxtBox;
	public Textbox spreadsheetTxtBox;
	public Listbox impActTypeListBox;
	public Listbox sourceRepListBox;
	public Checkbox logImportsCkBox;
	public Window impActWindow;
	
	// 07.03.2017 JWC
	public Textbox xslFileTxtBox;
	public Textbox xslRootNameTxtBox;
	// end of 07.03.2017 JWC
	
	private Listbox impActList;
	
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();


	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		this.impActList = (Listbox) Path.getComponent("//appHome/appHomeWin/impActListbox");		
		
		ImportActivity impAct = (ImportActivity) desktop.getAttribute("currentImpAct");
		
		//initialise the list of Source Repositories
		List<String> sourceRepNameList = new ArrayList<String>();
        
		//add the list of source repositories
		List<SourceRepository> sourceReps = this.getImportUtilityData().getSourceRepositories();
		Iterator<SourceRepository> srcRepIter = sourceReps.iterator();
		while(srcRepIter.hasNext()) {
			SourceRepository srcRep = srcRepIter.next();
			sourceRepNameList.add(srcRep.getSourceRepositoryName());
		}
		if(sourceRepNameList.size() == 0) {
			sourceRepNameList.add(0, "<No Source Repositories Available>");
		} else {
			sourceRepNameList.add(0, "<Select Source Repository>");
		}
		ListModelList srcRepModel = new ListModelList(sourceRepNameList);	
		sourceRepListBox.setModel(srcRepModel);
		
		
		
		if(impAct != null) {
			//given an ImportActivity, set the current mode for the composer to EDIT
			this.currentMode = ImportActivityComposer.EDIT_MODE;
			impActWindow.setTitle("Edit Import Activity");
			
			this.currentImpIndex = impActList.getSelectedIndex();
			this.currentImpAct = (ImportActivity) impActList.getListModel().getElementAt(currentImpIndex);
			this.impActNameTxtBox.setValue(this.currentImpAct.getImportActivityName());
			this.impActDescTxtBox.setValue(this.currentImpAct.getImportActivityDescription());			
			this.spreadsheetTxtBox.setValue(this.currentImpAct.getSpreadsheetFilename());
			this.xslFileTxtBox.setValue(this.currentImpAct.getXmlTransformFilename());			
			this.xslRootNameTxtBox.setValue(this.currentImpAct.getRootXslFilename());
			
			//pre-select the Import Activity Type
			String impType = this.currentImpAct.getImportActivityType();
			List types = this.impActTypeListBox.getItems();
			
			Iterator typeIter = types.iterator();
			Listitem currentListItem;
			while(typeIter.hasNext()) {
				currentListItem = (Listitem) typeIter.next();
				String currentType = (String) currentListItem.getLabel();
				if((currentListItem.getLabel() != null) && (currentType.equals(impType))) {
					int typeIndex = impActTypeListBox.getIndexOfItem(currentListItem);
					impActTypeListBox.setSelectedIndex(typeIndex);						
				}
			}
			
			
			//pre-select the Source Repository Name
			String srcRepName = this.currentImpAct.getImportActivitySourceRepository();
			
			Iterator<String> repIter = sourceRepNameList.iterator();
			String aSrcRepName;
			while(repIter.hasNext()) {
				aSrcRepName = (String) repIter.next();
				if((aSrcRepName != null) && (aSrcRepName.equals(srcRepName))) {
					int repIndex = sourceRepNameList.indexOf(aSrcRepName);
					sourceRepListBox.setSelectedIndex(repIndex);						
				}
			}
			
			if(impAct.isLogImports() && !(this.isEIPMode())) {
				logImportsCkBox.setChecked(true);			
			}
			
			desktop.removeAttribute("currentImpAct");
		} else {
			//in the absence of an ImportActivity, set the current mode for the composer to CREATE
			this.currentMode = ImportActivityComposer.CREATE_MODE;
			impActWindow.setTitle("Create Import Activity");
			
			//initialise the combo box of import types
			impActTypeListBox.setSelectedIndex(0);
		}

	}
	
	
	public void onClick$okBtn() {
		try {
		//	Window newImportDialog = (Window) Executions.createComponents("/new_import_activity.zul", null, null);
			ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
			if (impActNameTxtBox.isValid() && sourceRepListBox.getSelectedIndex() > 0) {
				
				//Update the details of the current Import Activity
				ListModelList listModel = (ListModelList) impActList.getListModel();
				
				if(this.currentMode == ImportActivityComposer.EDIT_MODE) {
							
					//update the attributes of the current Import Activity
					this.currentImpAct.setImportActivityName(impActNameTxtBox.getValue());
					this.currentImpAct.setImportActivityDescription(this.impActDescTxtBox.getValue());
					this.currentImpAct.setImportActivityType(impActTypeListBox.getSelectedItem().getLabel());
					this.currentImpAct.setImportActivitySourceRepository(sourceRepListBox.getSelectedItem().getLabel());
					this.currentImpAct.setImportActivityModifiedDate(this.getNowAsGregorian());
					this.currentImpAct.setRootXslFilename(xslRootNameTxtBox.getValue());
					
					if(!this.isEIPMode()) {
						this.currentImpAct.setLogImports(logImportsCkBox.isChecked());
					} else {
						this.currentImpAct.setLogImports(false);
					}
					
					//update the root directory for this import activity
					this.getImportUtilityDataManager().renameImportActivityRootDir(this.currentImpAct);
					
					//if required, update the spreadsheet associated with the Import Activity
					Media ssFile = (Media) impActWindow.getAttribute("uploadedSpreadsheet");
					if (ssFile != null && (ssFile.isBinary())) {
						dataManager.updateExcelImpActivitySpreadsheetFile(this.currentImpAct, ssFile);
						impActWindow.removeAttribute("uploadedSpreadsheet");
					}
					
					// Check for an uploaded XSL File
					Media xslFile = (Media) impActWindow.getAttribute("uploadedXSL");
					if(xslFile != null)
					{
						dataManager.updateXMLImpActivityImportSpecContent(this.currentImpAct, xslFile);
						impActWindow.removeAttribute("uploadedXSL");
					}
					
					listModel.remove(currentImpIndex);
					listModel.add(currentImpIndex, this.currentImpAct);
					this.impActList.setSelectedIndex(currentImpIndex);
				}
				
				
				//If the dialog is in CREATE MODE, add the new Import Activity to the config data
				if(this.currentMode == ImportActivityComposer.CREATE_MODE) {
					
					//create the new Import Activity and set the attributes from the UI fields
					ImportActivity impAct = dataManager.newImportActivity();
					impAct.setImportActivityName(impActNameTxtBox.getValue());
					impAct.setImportActivityDescription(this.impActDescTxtBox.getValue());
					impAct.setImportActivityType(impActTypeListBox.getSelectedItem().getLabel());
					impAct.setImportActivitySourceRepository(sourceRepListBox.getSelectedItem().getLabel());
					impAct.setImportActivityModifiedDate(this.getNowAsGregorian());
					impAct.setRootXslFilename(xslRootNameTxtBox.getValue());
					
					if(!this.isEIPMode()) {
						impAct.setLogImports(logImportsCkBox.isChecked());
					} else {
						impAct.setLogImports(false);
					}
					
					//create the root directory for this import activity
					this.getImportUtilityDataManager().initImportActivity(impAct);
					
					Media ssFile = (Media) impActWindow.getAttribute("uploadedSpreadsheet");
					if ((ssFile != null) && (ssFile.isBinary())) {
						dataManager.updateExcelImpActivitySpreadsheetFile(impAct, ssFile);
						impActWindow.removeAttribute("uploadedSpreadsheet");
					}
					
					// Check for an uploaded XSL File
					Media xslFile = (Media) impActWindow.getAttribute("uploadedXSL");
					if(xslFile != null)
					{
						dataManager.updateXMLImpActivityImportSpecContent(impAct, xslFile);
						impActWindow.removeAttribute("uploadedXSL");
					}
					
					//add the new Import Activity to the list in the main window
					listModel.add(impAct);
					this.impActList.setSelectedIndex(listModel.indexOf(impAct));
					
					//add the new Import Activity to the config data set
					EssentialImportUtility configData = dataManager.getImportUtilityData();
					configData.getImportActivities().add(impAct);
					
				}
				
				//save the config data
				dataManager.saveAppData();
				//this.popupDebugWindow(comp.getId());
				impActWindow.detach();
			}
		}
		catch (Exception e) {
			iuLog.log(Level.SEVERE, "Exception receiving UI OK event", e);
			//e.printStackTrace();
		}

	}
	
	
	public void onClick$cancelBtn() {
		try {
			impActWindow.detach();
		}
		catch (Exception e) {
			iuLog.log(Level.SEVERE, "Exception receiving UI CANCEL event", e);
			//e.printStackTrace();
		}

	}
	
	

}
