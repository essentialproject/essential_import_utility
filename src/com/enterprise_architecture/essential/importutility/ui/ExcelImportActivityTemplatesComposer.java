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
 * 26.06.2017	JWC Add capability to upload ZIP-ed Import Spec file
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

//import java.util.Iterator;
//import java.util.List;


import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ExcelImportTemplate;
//import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * target environments for Essential imports
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class ExcelImportActivityTemplatesComposer extends EssentialImportInterface {
	
	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private int currentMode;
	private int currentTemplateIndex;
	private ExcelImportTemplate currentTemplate;
	//private ImportUtilityDataManager dataManager;
	
	public Textbox impActTempNameTxtBox;
	public Textbox impActTempDescTxtBox;
	public Textbox importSpecTxtBox;
	public Textbox spreadsheetTxtBox;
	
	public Window excelImpActTemplateWindow;
	
	private Listbox impActTemplateList;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		
		//set the Target for uploading of documents
		// Executions.getCurrent().getDesktop().setAttribute("org.zkoss.zul.Fileupload.target", excelImpActTemplateWindow);
		
		this.impActTemplateList = (Listbox) Path.getComponent("//appHome/appHomeWin/excelImpTempListBox");		
		
		ExcelImportTemplate impTemp = (ExcelImportTemplate) desktop.getAttribute("currentExcelImpTemp");
		if(impTemp != null) {
			//given an Import Template, set the current mode for the composer to EDIT
			this.currentMode = ExcelImportActivityTemplatesComposer.EDIT_MODE;
			excelImpActTemplateWindow.setTitle("Edit Excel Import Activity Template");
			
			this.currentTemplateIndex = impActTemplateList.getSelectedIndex();
			this.currentTemplate = (ExcelImportTemplate) impActTemplateList.getListModel().getElementAt(currentTemplateIndex);
			this.impActTempNameTxtBox.setValue(this.currentTemplate.getExcelImportTemplateName());
			this.impActTempDescTxtBox.setValue(this.currentTemplate.getExcelImportTemplateDescription());
			this.importSpecTxtBox.setValue(this.currentTemplate.getExcelImportTemplateImportSpecFilename());
			this.spreadsheetTxtBox.setValue(this.currentTemplate.getExcelImportTemplateExcelFilename());
			
			desktop.removeAttribute("currentExcelImpTemp");
		} else {
			//in the absence of an import Template, set the current mode for the composer to CREATE
			this.currentMode = ExcelImportActivityTemplatesComposer.CREATE_MODE;
			excelImpActTemplateWindow.setTitle("Create Excel Import Activity Template");
		}

	}
	
	
	
	public void onClick$okBtn() {
		try {
			
			
		//	Retrieve the object that manages the application data
			ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
			
			//if all input constraints are met, update or create the Import Activity Template
			if (impActTempNameTxtBox.isValid() && importSpecTxtBox.isValid() && spreadsheetTxtBox.isValid()) {
				
				ListModelList listModel = (ListModelList) impActTemplateList.getListModel();
				
				//If the dialog is in EDIT MODE, update the details of the current Import Activity Template
				if(this.currentMode == ExcelImportActivityTemplatesComposer.EDIT_MODE) {
							
					this.currentTemplate.setExcelImportTemplateName(impActTempNameTxtBox.getValue());
					this.currentTemplate.setExcelImportTemplateDescription(impActTempDescTxtBox.getValue());
					// this.currentTemplate.setExcelImportTemplateExcelFilename(spreadsheetTxtBox.getValue());
					// this.currentTemplate.setExcelImportTemplateImportSpecFilename(importSpecTxtBox.getValue());
					
					//rename the root directory of the Import Activity Template, if necessary
					dataManager.renameImportActivityTemplateRootDir(this.currentTemplate);
					
					
					//update the files for the Import Activity Template
					Media ssFile = (Media) excelImpActTemplateWindow.getAttribute("uploadedSpreadsheet");
					if (ssFile != null) {
						dataManager.updateExcelImpActTemplateSpreadsheetFile(this.currentTemplate, ssFile);
						excelImpActTemplateWindow.removeAttribute("uploadedSpreadsheet");
					}
					
					Media impSpecFile = (Media) excelImpActTemplateWindow.getAttribute("uploadedImportSpec");
					if ((impSpecFile != null) && (!impSpecFile.isBinary())) {
						dataManager.updateExcelImpActTemplateImportSpecFile(this.currentTemplate, impSpecFile.getName(), impSpecFile.getStringData());
						excelImpActTemplateWindow.removeAttribute("uploadedImportSpec");
					}
					
					// Add ability to upload ZIP-ed import specification
					if((impSpecFile != null) && (impSpecFile.isBinary()) && (impSpecFile.getName().endsWith(".zip")))
					{
						dataManager.updateExcelImpActTemplateImportSpecFile(this.currentTemplate, impSpecFile.getName(), impSpecFile.getStreamData());
						excelImpActTemplateWindow.removeAttribute("uploadedImportSpec");
					}
					
					listModel.remove(this.currentTemplateIndex);
					listModel.add(this.currentTemplateIndex, this.currentTemplate);
					this.impActTemplateList.setSelectedIndex(this.currentTemplateIndex);
				}
				
				
				//If the dialog is in CREATE MODE, add the new Import Activity Template to the config data
				if(this.currentMode == ExcelImportActivityTemplatesComposer.CREATE_MODE) {
					ExcelImportTemplate impTemplate = dataManager.newExcelImportActivityTemplate();
					impTemplate.setExcelImportTemplateName(impActTempNameTxtBox.getValue());
					impTemplate.setExcelImportTemplateDescription(impActTempDescTxtBox.getValue());
				//	impTemplate.setExcelImportTemplateExcelFilename(spreadsheetTxtBox.getValue());
				//	impTemplate.setExcelImportTemplateImportSpecFilename(importSpecTxtBox.getValue());
					
					Media ssFile = (Media) excelImpActTemplateWindow.getAttribute("uploadedSpreadsheet");
					if (ssFile != null) {
						dataManager.storeExcelImpActTemplateSpreadsheetFile(impTemplate, ssFile);
						excelImpActTemplateWindow.removeAttribute("uploadedSpreadsheet");
					}
					
					Media impSpecFile = (Media) excelImpActTemplateWindow.getAttribute("uploadedImportSpec");
					if ((impSpecFile != null) && (!impSpecFile.isBinary())) {
						dataManager.storeExcelImpActTemplateImportSpecFile(impTemplate, impSpecFile);
						excelImpActTemplateWindow.removeAttribute("uploadedImportSpec");
					}
					
					
					listModel.add(impTemplate);
					this.impActTemplateList.setSelectedIndex(listModel.indexOf(impTemplate));
					
					EssentialImportUtility configData = dataManager.getImportUtilityData();
					configData.getExcelImportTemplates().add(impTemplate);
				}
				
				//save the config data and close the dialog
				dataManager.saveAppData();
				excelImpActTemplateWindow.detach();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	public void onClick$cancelBtn() {
		try {
		//	this.popupDebugWindow(exportlabel.getValue());
			excelImpActTemplateWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onUpload$excelImpActTemplateWindow(Event anEvent) {
		try {
			System.out.println("File Uploaded");
			this.popupDebugWindow(anEvent.getName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void processMedia(Media[] media) {
        if (media != null) {
        	System.out.println("File Uploaded");
			this.popupDebugWindow(new Integer(media.length).toString());
        }
    }

}
