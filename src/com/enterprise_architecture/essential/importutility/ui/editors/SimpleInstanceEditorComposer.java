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
 * 
 */
package com.enterprise_architecture.essential.importutility.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedSimpleSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.InstanceSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.PrimitiveSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.SimpleSlotScript;
//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
//import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
//import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDatas;
import com.enterprise_architecture.essential.importutility.ui.EssentialImportInterface;
//import com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedSimpleSlotTreeNode;
//import com.enterprise_architecture.essential.importutility.ui.treemodel.InstanceSlotTreeNode;
//import com.enterprise_architecture.essential.importutility.ui.treemodel.PrimitiveSlotTreeNode;
//import com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleSlotTreeNode;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * instances that are re-used across Essential imports,
 * e.g. specific taxonomy terms
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class SimpleInstanceEditorComposer extends EssentialImportInterface {

	private SimpleInstanceTypeScript currentInstance;
	private ImportSpecDataManager dataManager;
	private Tree importspectree;
	private List<String> classNameList;
	private String currentClassName;
	private String currentVarName;
	
	public Div simpleInstEditorDiv;
	public Textbox varNameTxtBox;
	public Combobox classNameTxtBox;
	public Textbox instanceIDTxtBox;
	public Textbox instanceNameTxtBox;
	public Listbox matchingModeListBox;
	
	private List<String> matchingModes;
	private int oldSelectedIndex = 0;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println(">>>>>> SimpleInstanceEditorComposer.doAfterCompose(). Start");
		super.doAfterCompose(comp);
		
		//System.out.println(">>>>>> SimpleInstanceEditorComposer.doAfterCompose(). Done super()");
		IdSpace window = simpleInstEditorDiv.getSpaceOwner();
        final Borderlayout borderLayout = (Borderlayout) window.getFellow("editExcelBorderLayout");
        
        final West westpanel = borderLayout.getWest();
        this.importspectree = (Tree) westpanel.getFellow("importspectree");
        
		//get the current Simple Instance
        Object currentData = desktop.getSession().getAttribute("currentTreeData");
        if(currentData == null)
        {
        	// Catch all in case the currentData is null
        	System.out.println(">>>> SimpleInstanceEditorComposer.doAfterCompose(). currentData = NULL");
        	final TreeNode currentTreeNode = (TreeNode) desktop.getSession().getAttribute("currentTreeNode");
        	currentData = currentTreeNode.getData();
        	System.out.println(">>>> SimpleInstanceEditorComposer.doAfterCompose(). currentData now = " + currentData.toString());        	
        }
        if(!currentData.getClass().getName().endsWith("SimpleInstanceTypeScript")) {        	
        	importspectree.setModel(importspectree.getModel());
        	return;
        }
		this.currentInstance = (SimpleInstanceTypeScript) currentData;
		
		//System.out.println(">>>>>>> SimpleInstanceEditorComposer - CurrentInstance = " + currentData.toString());
		
		//get the data manager for import specs
		dataManager = this.getImportSpecDataManager();
		
		classNameTxtBox.setModel(ListModels.toListSubModel(new ListModelList(getClassNameList()))); 
		
		matchingModes = new ArrayList<String>();
		matchingModes.add("<Select Mode>");
		matchingModes.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME);
		matchingModes.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYID);
		matchingModes.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYINTID);
		matchingModes.add(ImportSpecDataManager.IMPORT_MATCHMODE_NEW);
        ListModelList model = new ListModelList(matchingModes);
        
        matchingModeListBox.setModel(model);
        
        matchingModeListBox.addEventListener("onSelect", new EventListener(){
            public void onEvent(Event event) throws Exception {
            	//refresh the import spec tree
            	int selectionIndex = matchingModeListBox.getSelectedIndex();
            	if(selectionIndex > 0) {
	            	ListModel listModel = matchingModeListBox.getModel();
	            	String newMatchingMode = (String) listModel.getElementAt(selectionIndex);
	            	currentInstance.setMatchingMode(newMatchingMode);
	            	oldSelectedIndex = selectionIndex;
	            	
	            	//save the import spec data
	            	dataManager.saveSpreadsheetImportSpecData();
	            	
	            	//refresh the import spec tree
	            	importspectree.setModel(importspectree.getModel());
	        	} else {
	        		Messagebox.show("An instance matching mode must be selected", "Set Matching Mode Error", Messagebox.OK, Messagebox.ERROR);
	        		matchingModeListBox.setSelectedIndex(oldSelectedIndex);
	        	}
            }
        });
       
		
		if(this.currentInstance != null) {
			//given an existing Simple Instance, set the values of the page's widgets
			
			//set the variable name text box
			this.currentVarName = this.currentInstance.getVariableName();
			this.varNameTxtBox.setValue(currentVarName);
			
			//set the class name text box
			String className = this.currentInstance.getClassName();
			this.classNameTxtBox.setValue(className);
			this.currentClassName = className;
			
			//set the instance ID column reference text box
			this.instanceIDTxtBox.setValue(this.currentInstance.getExtIDRef());
			
			//set the instance name column reference text box
			this.instanceNameTxtBox.setValue(this.currentInstance.getNameRef());	
			
			//pre-select the matching mode of the current Simple Instance
			String currentMatchingMode = this.currentInstance.getMatchingMode();
	        String aMode;
	        Iterator modesIter = matchingModes.iterator();
			if(currentMatchingMode != null) {
				while(modesIter.hasNext()) {
					aMode = (String) modesIter.next();
					if((aMode != null) && (aMode.equals(currentMatchingMode))) {
						int modeIndex = matchingModes.indexOf(aMode);
						matchingModeListBox.setSelectedIndex(modeIndex);	
						oldSelectedIndex = modeIndex;
						break;
					}
				}
			}
			
			//remove the data attribute from the session
			desktop.getSession().removeAttribute("currentTreeData");
		} 
		
	}
	
	
	public void onChange$varNameTxtBox() {
		try {
			//set the variable name of the current instance
			String newVarName = varNameTxtBox.getValue();
			if(newVarName != null && newVarName.length() > 0) {
				
				if(!ImportSpecDatas.containsSpecialCharacters(newVarName)) {
					this.currentInstance.setVariableName(newVarName);	
					this.currentVarName = newVarName;
					
					//save the import spec data
					dataManager.saveSpreadsheetImportSpecData();
					 
					importspectree.setModel(importspectree.getModel());
				} else {
					//reset the variable name and notify the user of the error
					varNameTxtBox.setValue(this.currentVarName);
					try {
						Messagebox.show("The variable name cannot contain spaces or special characters","Invalid variable Name", Messagebox.OK, Messagebox.ERROR);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/*public void onChange$varNameTxtBox() {
		try {
			//set the variable name of the current instance
			String newVarName = varNameTxtBox.getValue();
			if(newVarName != null && newVarName.length() > 0) {
				this.currentInstance.setVariableName(newVarName);	
				
				//save the import spec data
				dataManager.saveSpreadsheetImportSpecData();
				 
				importspectree.setModel(importspectree.getModel());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	} */
	
	public void onChange$classNameTxtBox() {
		try {
			//set the Essential class name for the current instance
			String className = classNameTxtBox.getValue();
			
			if(this.classNameList.contains(className)) {
				this.currentClassName = className;
				this.currentInstance.setClassName(className);	
				
				this.updateChildInstanceParentClasses();
				
				//save the import spec data
				dataManager.saveSpreadsheetImportSpecData();
				
				//refresh the import spec tree
				importspectree.setModel(importspectree.getModel());
			} 
			else {
				//reset the class name and notify the user of the error
				classNameTxtBox.setValue(currentClassName);
				try {
					Messagebox.show("The defined class is not a valid Essential class","Invalid Class Name", Messagebox.OK, Messagebox.ERROR);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void onChange$instanceIDTxtBox() {
		/*try {
			//set the ID for the current instance
			String newIDRef = instanceIDTxtBox.getValue();
			if(newIDRef != null && newIDRef.length() > 0) {
				this.currentInstance.setExtIDRef(newIDRef);	
				
				//save the import spec data
				dataManager.saveSpreadsheetImportSpecData();
				
				importspectree.setModel(importspectree.getModel());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}*/
		try {
			//set the ID for the current instance
			String newIDRef = instanceIDTxtBox.getValue();
			if(newIDRef == null || newIDRef.length() == 0) {
				this.currentInstance.setExtIDRef("");		
			} else {
				this.currentInstance.setExtIDRef(newIDRef);	
			}
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();
			importspectree.setModel(importspectree.getModel());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void onChange$instanceNameTxtBox() {
		try {
			/*//set the ID for the current instance
			String newNameRef = instanceNameTxtBox.getValue();
			if(newNameRef != null && newNameRef.length() > 0) {
				this.currentInstance.setNameRef(newNameRef);	
				
				//save the import spec data
				dataManager.saveSpreadsheetImportSpecData();
				
				importspectree.setModel(importspectree.getModel());
			}*/
			
			//set the ID for the current instance
			String newNameRef = instanceNameTxtBox.getValue();
			if(newNameRef == null || newNameRef.length() == 0) {
				this.currentInstance.setNameRef("");		
			} else {
				this.currentInstance.setNameRef(newNameRef);
			}
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();		
			importspectree.setModel(importspectree.getModel());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private List<String> getClassNameList() { //return all items
		if(classNameList == null) {
			this.classNameList = this.getEssentialClasses();
		}
		return this.classNameList;
	}
	
	
	private void updateChildInstanceParentClasses() {
		Iterator slotsIter = currentInstance.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().iterator();
		
		while(slotsIter.hasNext()) {
			Object aSlot =  slotsIter.next(); 
			if(aSlot.getClass().getName().endsWith("DerivedSimpleSlotScript")) {
				DerivedSimpleSlotScript derSimpleSlot = (DerivedSimpleSlotScript) aSlot;
				derSimpleSlot.setParentClassName(currentClassName);
			} else if(aSlot.getClass().getName().endsWith("InstanceSlotScript")) {
				InstanceSlotScript instanceSlot = (InstanceSlotScript) aSlot;
				instanceSlot.setParentClassName(currentClassName);
			} else if(aSlot.getClass().getName().endsWith("SimpleSlotScript")) {
				SimpleSlotScript simpleSlot = (SimpleSlotScript) aSlot;
				simpleSlot.setParentClassName(currentClassName);
			} else if(aSlot.getClass().getName().endsWith("PrimitiveSlotScript")) {
				PrimitiveSlotScript primSlot = (PrimitiveSlotScript) aSlot;
				primSlot.setParentClassName(currentClassName);
			}
		}		
	}
	
	
}
