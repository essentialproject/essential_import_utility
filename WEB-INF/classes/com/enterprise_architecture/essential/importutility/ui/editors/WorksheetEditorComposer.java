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
 * 23.08.2018	JWC Upgrade to ZK 8.5, remove ZSS
 * 23.08.2018	JWC Removed the Spreadsheet preview
 * 
 */
package com.enterprise_architecture.essential.importutility.ui.editors;

//import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
//import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;





import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
//import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
//import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
//import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
//import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
//import com.enterprise_architecture.essential.importutility.data.common.script.SimpleSlotScript;
//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
//import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.ui.EssentialImportInterface;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * instances that are re-used across Essential imports,
 * e.g. specific taxonomy terms
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class WorksheetEditorComposer extends EssentialImportInterface {

	private WorksheetImportSpec currentWorksheet;
	private ImportSpecDataManager dataManager;
	
	public Div wsEditorDiv;
	public Listbox worksheetNameList;
	public Textbox wsDescTxtBox;
	public Intbox wsFirstRowTxtBox;
	public Intbox wsLastRowTxtBox;
	
	private Tree importspectree;
	
	private List<String> sheetNames;
	private int oldSelectedIndex = 0;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
//		System.out.println("WorksheetEditorComposer.doAfterCompose(): comp = " + comp.toString());
		IdSpace window = wsEditorDiv.getSpaceOwner();
        final Borderlayout borderLayout = (Borderlayout) window.getFellow("editExcelBorderLayout");
        
        //South southpanel = borderLayout.getSouth();
        //final Spreadsheet spreadsheet = (Spreadsheet) southpanel.getFellow("spreadsheet");
        //final Combobox sheets = (Combobox) southpanel.getFellow("sheets");
        final East eastpanel = borderLayout.getEast();
        
        final West westpanel = borderLayout.getWest();
        importspectree = (Tree) westpanel.getFellow("importspectree");
		final TreeNode currentTreeNode = (TreeNode) desktop.getSession().getAttribute("currentTreeNode");
		
//		if(currentTreeNode == null)
//        {
//        	System.out.println("WorksheetEditorComposer.doAfterCompose(): currentTreeNode = NULL ");
//        }
//		else
//		{
//			System.out.println(">>>> CurrentTreeNode = " + currentTreeNode.toString());
//			System.out.println(">>>> CurrentTreeNode data = " + currentTreeNode.getData().toString());
//		}
		
		//get the current Worksheet
        Object currentData = desktop.getSession().getAttribute("currentTreeData");
        //System.out.println("WorksheetEditorComposer.doAfterCompose(): currentData = " + currentData.toString()); 
        if(currentData == null)
        {
        	// Catch all in case the currentData is null
        	currentData = currentTreeNode.getData();
        }
        if(currentData != null)
        {
        	//System.out.println("WorksheetEditorComposer.doAfterCompose(): currentData = " + currentData.toString());
        	//System.out.println("WorksheetEditorComposer.doAfterCompose().114: currentData Class Name = " + currentData.getClass().getName());
	        if(!currentData.getClass().getName().endsWith("WorksheetImportSpecScript")) {
	        	importspectree.setModel(importspectree.getModel());
	        	return;
	        }	        		
        }

        
//        if(currentData == null)
//        {
//        	System.out.println("WorksheetEditorComposer.doAfterCompose(): currentData = NULL ");
//        	System.out.println(">>>>> Resetting to currentTreeNode.getData()");
//        	currentData = currentTreeNode.getData();
//        }
        
        this.currentWorksheet = (WorksheetImportSpec) currentData;
        
        // Trace
//        if(currentWorksheet != null){
//        	System.out.println("WorksheetEditorComposer.doAfterCompose(): currentWorksheet = " + currentWorksheet.getName());
//        }
//        else
//        {
//        	System.out.println("WorksheetEditorComposer.doAfterCompose(): currentWorksheet = NULL");        	
//        }
        
		// START OF LOADING OF SPREADSHEET PREVIEW
    	//Set the list of worksheet names
		//Workbook spreadsheet = WorkbookFactory.create(new File(ssFilePath));
		// 23.08.2018 JWC - Migrate to Apache POI
		//spreadsheet = 
		final Workbook spreadsheet = (Workbook) desktop.getSession().getAttribute("currentWorkbook"); 
		
		//System.out.println("WorksheetEditorComposer.doAfterCompose(): 156. Got Workbook; spreadsheet: " + spreadsheet.getNameAt(0));
		
		sheetNames = new ArrayList<String>();
        //int sheetSize = spreadsheet.getBook().getNumberOfSheets();
		int sheetSize = spreadsheet.getNumberOfSheets();
        for (int i = 0; i < sheetSize; i++){
            	sheetNames.add(spreadsheet.getSheetName(i));
        }      
        if(sheetNames.size() == 0) {
        	sheetNames.add(0, "<No Worksheets Available>");
        } else {
        	sheetNames.add(0, "<Select Worksheet>");
        }
        //System.out.println("WorksheetEditorComposer.doAfterCompose(): 169. Got Sheet names");
        
        // 23.08.2018 JWC - Move away from deprecated List Model class.
        // Check that the type specified is correct for ListModelList
        ListModelList<String> model = new ListModelList<String>(sheetNames, true);        
        //BindingListModelList model = new BindingListModelList(sheetNames, true);
        
      //  final Listbox worksheetNameList = (Listbox) Path.getComponent("/editExcelImportSpecWin/worksheetNameList");
        worksheetNameList.setModel(model);
        
        //System.out.println("WorksheetEditorComposer.doAfterCompose(): 160. Set the worksheetNameList model");
        
        worksheetNameList.addEventListener("onSelect", new EventListener(){
            public void onEvent(Event event) throws Exception {
            	
            	int worksheetIndex = worksheetNameList.getSelectedIndex();
            	//System.out.println("WorksheetEditorComposer.onSelect Event: index " + worksheetIndex);
            	if(worksheetIndex > 0) {
	            	String wsName = (String) worksheetNameList.getSelectedItem().getValue();
	            	//spreadsheet.setSelectedSheet(wsName);
	            	
	            	// 29.08.2018 JWC Not required
	            	//set the combo box associated with the spreadsheet pane
	            	//sheets.setSelectedIndex(worksheetNameList.getSelectedIndex() - 1);
	            		    			
	            	//set the name of the current worksheet spec
	            	currentWorksheet.setName(wsName);
	    			oldSelectedIndex = worksheetIndex;
	    			
	    			//save the config data and close the dialog
	    			dataManager.saveSpreadsheetImportSpecData();
	    			
	    			importspectree.setModel(importspectree.getModel());
            	} else {
            		Messagebox.show("A worksheet must be selected", "Select Worksheet Error", Messagebox.OK, Messagebox.ERROR);
            		worksheetNameList.setSelectedIndex(oldSelectedIndex);
            	}

            }
        });
        
        
        worksheetNameList.addEventListener("onAfterRender", new EventListener(){
            public void onEvent(Event event) throws Exception {
            	//System.out.println("WorksheetEditorComposer.onAfterRender Event: " + event.getTarget());
            	if(worksheetNameList.getSelectedItem() != null && worksheetNameList.getSelectedIndex() > 0) {
            		//System.out.println("WorksheetEditorComposer.onAfterRender Event in loop: wsName = " );
	            	String wsName = (String) worksheetNameList.getSelectedItem().getValue();
	            	//System.out.println("WorksheetEditorComposer.onAfterRender Event in loop: wsName = " + wsName);
	            	currentWorksheet.setName(wsName);
	            	//spreadsheet.setSelectedSheet(wsName);
	            		            	
	            	// 29.08.2018 JWC Not required
	            	//set the combo box associated with the spreadsheet pane
	            	//sheets.setSelectedIndex(worksheetNameList.getSelectedIndex() - 1);	
            	}            	
            	//System.out.println("WorksheetEditorComposer.onAfterRender(): 225. Event= " + event.getTarget().toString());            	
            }
        });
        
        // END OF LOAD AND CONFIG OF SPREADSHEET PREVIEW
        //System.out.println("WorksheetEditorComposer.doAfterCompose(): 206. Setting the data Manager");
		
		//get the data manager for import specs
		dataManager = this.getImportSpecDataManager();
		
		if(this.currentWorksheet != null) {
			//System.out.println("WorksheetEditorComposer.doAfterCompose(): 212. currentWorksheet != null");
			//given a Worksheet Spec, set the values of the page's widgets
			
			//pre-select the name of the worksheet in the worksheet list
			String wsName = this.currentWorksheet.getName();
			if(wsName != null) {
			//	List names = this.worksheetNameList.getItems();
				Iterator nameIter = sheetNames.iterator();
				String currentName;
				//Listitem currentListItem;
				while(nameIter.hasNext()) {
				//	currentListItem = (Listitem) nameIter.next();
				//	String currentName = (String) currentListItem.getLabel();
					currentName = (String) nameIter.next();
					if((currentName != null) && (currentName.equals(wsName))) {
						int nameIndex = sheetNames.indexOf(currentName);
						worksheetNameList.setSelectedIndex(nameIndex);		
						oldSelectedIndex = nameIndex;
						break;
					}
				}
			}
			
			//set the description text box
			this.wsDescTxtBox.setValue(this.currentWorksheet.getWorksheetDescription());
			
			//set the first row integer box
			BigInteger firstRow = this.currentWorksheet.getFirstRow();
			if(firstRow != null) {
				Integer firstRowInt = new Integer(firstRow.toString());
				this.wsFirstRowTxtBox.setValue(firstRowInt);
			}
			
			//set the first row integer box
			BigInteger lastRow = this.currentWorksheet.getLastRow();
			if(lastRow != null) {
				Integer lastRowInt = new Integer(lastRow.toString());
				this.wsLastRowTxtBox.setValue(lastRowInt);
			}
			//System.out.println("WorksheetEditorComposer.doAfterCompose(): 251. About to remove currentTreeData sesssion variable");
			//remove the worksheet attribute from the session			
			desktop.getSession().removeAttribute("currentTreeData");
		} 
			
		// JWC remove the worksheet attribute from the session
		/*desktop.getSession().removeAttribute("currentTreeData");
		if(desktop.getSession().getAttribute("currentTreeData") != null)
		{
			System.out.println("WorksheetEditorComposer.doAfterCompose(): 264. About to remove currentTreeData sesssion variable");
			desktop.getSession().removeAttribute("currentTreeData");
		}*/
	}
	
	
	
	public void onChange$wsDescTxtBox() {
		try {
			//set the description of the current worksheet
			String newDesc = wsDescTxtBox.getValue();
			if(newDesc != null && newDesc.length() > 0) {
				this.currentWorksheet.setWorksheetDescription(newDesc);	
				
				//save the config data and close the dialog
				dataManager.saveSpreadsheetImportSpecData();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onChange$wsFirstRowTxtBox() {
		try {
			//set the first row of the current worksheet
			int newRow = wsFirstRowTxtBox.getValue();
			if(newRow > 0) {
				this.currentWorksheet.setFirstRow(BigInteger.valueOf(newRow));	
				
				//save the config data and close the dialog
				dataManager.saveSpreadsheetImportSpecData();
				
				importspectree.setModel(importspectree.getModel());
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onChange$wsLastRowTxtBox() {
		try {
			//set the last row of the current worksheet
			int newRow = wsLastRowTxtBox.getValue();
			if(newRow > 0) {
				this.currentWorksheet.setLastRow(BigInteger.valueOf(newRow));	
				
				//save the config data and close the dialog
				dataManager.saveSpreadsheetImportSpecData();
				
				importspectree.setModel(importspectree.getModel());
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
}
