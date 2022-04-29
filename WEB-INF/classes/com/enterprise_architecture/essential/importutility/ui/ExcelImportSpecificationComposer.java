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
 * 23.08.2018	JWC Upgrade to ZK 8.5, remove ZSS
 * 06.02.2019	JWC Bug fix for the issue with editor panel
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.File;
import java.util.ArrayList;
//import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
//import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.event.EventListener;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.importspec.script.*;
import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprise_architecture.essential.importutility.ui.treemodel.*;

/**
 * This class is the UI controller for the page used to edit Excel import specifications
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class ExcelImportSpecificationComposer extends EssentialImportInterface {
	Tree importspectree;
	East eastpanel;
	Workbook spreadsheet;
	Combobox sheets;
	Window editExcelImportSpecWin;
	
	private ImportSpecDataManager impSpecDataManager;
	private SpreadsheetImportSpecScript importSpec;
	private ExcelImportSpecTreeNode treeRootNode;
	
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		if(!UserCredentialManager.getInstance(desktop, this.getContextPath()).isAuthenticated(desktop)){
			if (isEIPMode()) {
				execution.sendRedirect("redirectToAuthnServer.zul");
			} else {
				execution.sendRedirect("index.zul");
			}
		    return;
		} 
		
	//	if(this.importSpec == null) {
			//retrieve the current Import Activity from the session
	        ImportActivity currentImpAct = (ImportActivity) desktop.getSession().getAttribute("currentImpAct");
        
	        //get data manager for Import Specification if we don't already have one
	        if(currentImpAct != null) {
	        	System.out.println("OPENING IMPORT SPEC FOR ACTIVITY: " + currentImpAct.getImportActivityName());
	        	
	        	//given an ImportActivity, set up the import spec data manager)
				this.impSpecDataManager = this.getImportSpecDataManager();
				this.importSpec = this.impSpecDataManager.getSpreadsheetImportSpecScriptData();
				
				
				//set up the import specification tree
			/*	This is commented out from before migration to ZK 8.5
			 * treeRootNode = new ExcelImportSpecTreeNode(importSpec, new ArrayList());
				TreeModel importSpecTreeModel = new DefaultTreeModel(treeRootNode);
				importspectree.setModel(importSpecTreeModel);
				importspectree.setItemRenderer(new ImportSpecTreeRenderer());  */
	        
				//get the path of the import activity's spreadsheet and associate it with the spreadsheet UI component
	        	String ssFilePath = this.getImportUtilityDataManager().getLocalImpActSpreadsheetPath(currentImpAct);
	        	if(ssFilePath != null) 
	        	{
	        		// 23.08.2018 JWC - Migrate to Apache POI
	        		spreadsheet = WorkbookFactory.create(new File(ssFilePath));
	        		//spreadsheet.setSrc(ssFilePath);
	        	}
	        			        
		        List<String> sheetNames = new ArrayList<String>();
		        int sheetSize = 0;
		        if(spreadsheet != null)
		        {
		        	sheetSize = spreadsheet.getNumberOfSheets();
		        }
		        
		        for (int i = 0; i < sheetSize; i++){
		                sheetNames.add(spreadsheet.getSheetName(i));
		        }
		        
		        // 23.08.2018 JWC - Move away from deprecated List Model class.
		        // Check that the type specified is correct for ListModelList
		        //BindingListModelList model = new BindingListModelList(sheetNames, true);
		        /*ListModelList<String> model = new ListModelList<String>(sheetNames, true);
		        System.out.println("Model is: " + model.get(0));
		        if (sheets == null){
		        	System.out.println("Sheets Comboxbox is null!");
		        }
		        else{		        		       
		        	sheets.setModel(model);
		        	System.out.println("Sheets component is: " + sheets.getName());
		        }
		        */
		        // Add a session variable for the selected workbook
		        //System.out.println("ExcelImportSpecificationComposer.doAfterCompose(). currentWorkbook = " + spreadsheet.getSheetName(0));
		        desktop.getSession().setAttribute("currentWorkbook", spreadsheet);
		     } else {
				if (isEIPMode()) {
					execution.sendRedirect("redirectToAuthnServer.zul");
				} else {
					execution.sendRedirect("index.zul");
				}
			}
	        
	    //    System.out.println("IMP SPEC EDITOR REFRESHED - TOTAL WORKSHEETS: " + this.importSpec.getWorksheetImportSpec().size());
     //   }
	}
	
	 /**
     * Sets selected sheet
     * @param event
     */
    public void onSelect$sheets(Event event) 
    {
        //spreadsheet.setSelectedSheet(sheets.getText());
    	spreadsheet.setActiveSheet(sheets.getSelectedIndex());
    }
    
    
    
    /**
     * clode the editor
     * @param event
     */
    public void onClick$closeBtn() {
    	this.removeSettings();
		System.out.println("CLOSING EDIT EXCEL IMPORT SPEC WINDOW");
    	//Executions.sendRedirect("/run_excel_import.zul");
		if(!this.isEIPMode()) {
			Executions.sendRedirect("/run_excel_import.zul");
		} else {
			Executions.sendRedirect("/eip_run_excel_import.zul");
		}
    }

    

	public void onSelect$importspectree() {
		try {
			Treeitem selectedItem = importspectree.getSelectedItem();
			
			// Removed this code as part of the v2.0 release
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	} 
	
	
	/**
     * Sets selected sheet
     * @param event
     */
 /*   public void onClick$addWorksheetBtn() {
    	WorksheetImportSpecScript wsImportSpec =  this.impSpecData)Manager.newWorksheetImportSpec(); 
    	this.importSpec.getWorksheetImportSpec().add(wsImportSpec);
		WorksheetTreeNode aTreeNode = new WorksheetTreeNode(wsImportSpec, new ArrayList() );
		treeRootNode.add(aTreeNode);
		this.importspectree.invalidate(); 
		System.out.println("ADD WS CLICKED - TOTAL WORKSHEETS: " + this.importSpec.getWorksheetImportSpec().size());
    } */
    
    public void onClose$editExcelImportSpecWin() {
    	this.removeSettings();
		System.out.println("CLOSING EDIT EXCEL IMPORT SPEC WINDOW");
    }
    
    
    private void removeSettings() {
    	this.impSpecDataManager = null;
    	this.importSpec = null;
    	this.treeRootNode = null;
    	// Remove the Workbook from the system parameter
    	desktop.getSession().removeAttribute("currentWorkbook");
    }
	
}
