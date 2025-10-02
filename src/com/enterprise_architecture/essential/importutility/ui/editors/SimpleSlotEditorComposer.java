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
//import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
//import org.zkoss.zk.ui.Path;
//import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.event.EventListener;
//import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
//import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
//import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
//import com.enterprise_architecture.essential.importutility.data.common.script.DerivedSimpleSlotScript;
//import com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.SimpleSlotScript;
//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
//import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
//import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;
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
public class SimpleSlotEditorComposer extends EssentialImportInterface {

	private SimpleSlotScript currentSlot;
	private ImportSpecDataManager dataManager;
	private Tree importspectree;
	private String currentInstanceClass;
	private int oldSelectedSlotNameIndex = 0;
	
	public Div simpleSlotEditorDiv;
	public Listbox slotNameListBox;
	public Textbox slotValueTxtBox;
	
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		IdSpace window = simpleSlotEditorDiv.getSpaceOwner();
        final Borderlayout borderLayout = (Borderlayout) window.getFellow("editExcelBorderLayout");
        
        final West westpanel = borderLayout.getWest();
        this.importspectree = (Tree) westpanel.getFellow("importspectree");
        
		//get the current Simple Slot
        Object currentData = desktop.getSession().getAttribute("currentTreeData");
        if(currentData == null)
        {
        	// Catch all in case the currentData is null
        	final TreeNode currentTreeNode = (TreeNode) desktop.getSession().getAttribute("currentTreeNode");
        	currentData = currentTreeNode.getData();
        }
        if(!currentData.getClass().getName().endsWith("SimpleSlotScript")) {
        	importspectree.setModel(importspectree.getModel());
        	return;
        }
		this.currentSlot = (SimpleSlotScript) currentData;
		
		//get the data manager for import specs
		dataManager = this.getImportSpecDataManager();
		
		//get the class name of the current parent Instance
		this.currentInstanceClass = currentSlot.getParentClassName();
		
		//set up the list of slot names
		List<String> classSlotList = this.getSlotNames(this.currentInstanceClass);
		List<String> completeSlotList = new ArrayList<String>();
		completeSlotList.add("- Select Simple Slot -");
		completeSlotList.addAll(classSlotList);
		slotNameListBox.setModel(new ListModelList(completeSlotList)); 
		
		
		if(this.currentSlot != null) {
			//given an existing Simple Slot, set the values of the page's widgets
			
			//set the slot name list box
			String slotName = this.currentSlot.getSlotName();
			if(slotName != null && slotName.length() > 0) {
				int slotNameIndex = completeSlotList.indexOf(slotName);
				if(slotNameIndex >= 0) {
					slotNameListBox.setSelectedIndex(slotNameIndex);
					oldSelectedSlotNameIndex = slotNameIndex;
				}
			}
			
			
			//set the slot value text box
			this.slotValueTxtBox.setValue(this.currentSlot.getSlotValueRef());
			
			
			//remove the data attribute from the session
			desktop.getSession().removeAttribute("currentTreeData");
		} 
		
	}
	
	
	
	public void onSelect$slotNameListBox() {
		try {
			int slotNameIndex = slotNameListBox.getSelectedIndex();
			if(slotNameIndex > 0) {
				List<String> slotList = this.getSlotNames(this.currentInstanceClass);
				if(slotList != null) {
					int actualSlotIndex = slotNameIndex - 1;
					String slotName = slotList.get(actualSlotIndex);
					this.currentSlot.setSlotName(slotName);
					oldSelectedSlotNameIndex = slotNameIndex;
					
					
					//save the import spec data
					dataManager.saveSpreadsheetImportSpecData();
					 
					//refresh the import spec tree
					importspectree.setModel(importspectree.getModel());
				}
			} else {
				this.displayError("A valid slot name must be selected", "Slot Selection Error");
				slotNameListBox.setSelectedIndex(oldSelectedSlotNameIndex);
			}
			
			
		/*	int slotNameIndex = slotNameListBox.getSelectedIndex();
			if(slotNameIndex >= 0) {
				List<String> slotList = this.getSlotNames(this.currentInstanceClass);
				if(slotList != null) {
					String slotName = slotList.get(slotNameIndex);
					this.currentSlot.setSlotName(slotName);
					
					//save the import spec data
					dataManager.saveSpreadsheetImportSpecData();
					 
					//refresh the import spec tree
					importspectree.setModel(importspectree.getModel());
				}
			}  */
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	
	
	public void onChange$slotValueTxtBox() {
		try {
			//set the slot value reference for the current slot
			String newSlotValue = slotValueTxtBox.getValue();
			if(newSlotValue != null && newSlotValue.length() > 0) {
				this.currentSlot.setSlotValueRef(newSlotValue);	
				
				//save the import spec data
				dataManager.saveSpreadsheetImportSpecData();
				
				importspectree.setModel(importspectree.getModel());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private List<String> getSlotNames(String className) { //return all items
		if(className != null && className.length() > 0) {
			List<String> slotList = this.getSlotsForEssentialClass(className);
			
			if(slotList == null) {
				slotList = new ArrayList<String>();
			} 
			
			
			//remove the name and external id slot names
			slotList.remove(ImportSpecDataManager.NAME_SLOT_NAME);
			slotList.remove(ImportSpecDataManager.RELATION_NAME_SLOT_NAME);	
			slotList.remove(ImportSpecDataManager.GRAPH_RELATION_NAME_SLOT_NAME);
			slotList.remove(ImportSpecDataManager.EXTID_SLOT_NAME);	
			return slotList;
		}
		return new ArrayList<String>();
	}
	
}
