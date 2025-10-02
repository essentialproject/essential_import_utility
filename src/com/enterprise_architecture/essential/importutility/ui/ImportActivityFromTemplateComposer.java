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
//import java.io.FileInputStream;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;

//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.datatype.XMLGregorianCalendar;

//import org.zkoss.poi.hslf.model.TextBox;
import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Execution;
//import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
//import org.zkoss.zk.ui.Session;
//import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.event.EventListener;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;
//import org.zkoss.zul.event.ListDataEvent;

import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ExcelImportTemplate;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * import activities based on an existing template
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class ImportActivityFromTemplateComposer extends EssentialImportInterface {
	
	//private static final int CREATE_MODE = 1;
	//private static final int EDIT_MODE = 2;
	//private int currentMode;
	//private int currentImpIndex;
	//private ImportActivity currentImpAct;
	//private ImportUtilityDataManager dataManager;
	
	public Button okBtn;
	public Button cancelBtn;
	public Textbox impActNameTxtBox;
	public Textbox impActDescTxtBox;
	public Listbox impActTempListBox;
	public Window impActWindow;
	
	private Listbox impActList;

	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		EssentialImportUtility importAppData = this.getImportUtilityData();
		impActTempListBox.setModel(new ListModelList(importAppData.getExcelImportTemplates()));
		
		impActTempListBox.setItemRenderer(new ListitemRenderer() {
		    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
		         
		        final ExcelImportTemplate impActTemp = (ExcelImportTemplate) data;
		        listItem.setValue(impActTemp);
		         
		        new Listcell(impActTemp.getExcelImportTemplateName()).setParent(listItem);
		    }
		   }
		); 
		
		this.impActList = (Listbox) Path.getComponent("//appHome/appHomeWin/impActListbox");		
		impActWindow.setTitle("Create Import Activity");
			
		//initialise the combo box of import types
		impActTempListBox.setSelectedIndex(0);

	}
	
	
	public void onClick$okBtn() {
		try {
		//	Window newImportDialog = (Window) Executions.createComponents("/new_import_activity.zul", null, null);
			ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
			if (impActNameTxtBox.isValid()) {
				
				//Create a new Import Activity
				ListModelList listModel = (ListModelList) impActList.getListModel();
					
				//create the new Import Activity and set the attributes from the UI fields
				ImportActivity impAct = dataManager.newImportActivity();
				impAct.setImportActivityName(impActNameTxtBox.getValue());
				impAct.setImportActivityDescription(this.impActDescTxtBox.getValue());
				impAct.setImportActivityType("Excel");
				impAct.setImportActivityModifiedDate(this.getNowAsGregorian());
				
				
				//create the root directory for this import activity
				this.getImportUtilityDataManager().initImportActivity(impAct);
				
				//copy across the files from the Import Activity Template
				int selectedTemplateIndex = impActTempListBox.getSelectedIndex();
				ListModelList templateListModel = (ListModelList) impActTempListBox.getListModel();
				ExcelImportTemplate impTemp = (ExcelImportTemplate) templateListModel.getElementAt(selectedTemplateIndex);
				dataManager.createImportActivityFilesFromTemplate(impAct, impTemp);
				
				//add the new Import Activity to the list in the main window
				listModel.add(impAct);
				this.impActList.setSelectedIndex(listModel.indexOf(impAct));
				
				//add the new Import Activity to the config data set
				EssentialImportUtility configData = dataManager.getImportUtilityData();
				configData.getImportActivities().add(impAct);
				
				
				//save the config data
				dataManager.saveAppData();
				//this.popupDebugWindow(comp.getId());
				impActWindow.detach();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$cancelBtn() {
		try {
			impActWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	

}
