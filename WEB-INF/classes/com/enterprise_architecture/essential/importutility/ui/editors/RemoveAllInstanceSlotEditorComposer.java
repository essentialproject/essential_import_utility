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

//import java.math.BigInteger;
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
//import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
//import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.common.script.*;
//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
//import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
//import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.WorksheetImportSpecScript;
import com.enterprise_architecture.essential.importutility.ui.EssentialImportInterface;

/**
 * This class is the UI controller for the screen used to select global instances to include
 * in a spreadsheet import specification
 * e.g. specific taxonomy terms
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class RemoveAllInstanceSlotEditorComposer extends EssentialImportInterface {

	private ImportSpecDataManager importSpecDataManager;
	
	public Div removeAllInstanceSlotEditorDiv;
	public Listbox conditionalRefListBox;
	public Listbox slotNameListBox;
	public Textbox condColumnTxtBox;
	private Tree importspectree;
	
	private RemoveAllInstanceSlotScript currentInstanceSlot;
	private List<String> conditionalRefList = new ArrayList<String>();
	private int oldSelectedVariableIndex = 0;
	private int oldSelectedSlotNameIndex = 0;
	private String currentInstanceClass;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		IdSpace window = removeAllInstanceSlotEditorDiv.getSpaceOwner();
        final Borderlayout borderLayout = (Borderlayout) window.getFellow("editExcelBorderLayout");
        
        final West westpanel = borderLayout.getWest();
        importspectree = (Tree) westpanel.getFellow("importspectree");
		final DefaultTreeNode currentTreeNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
		
    	
		//get the data managers
		this.importSpecDataManager = this.getImportSpecDataManager();
		
		
		//get the current instance variable
		Object currentData = desktop.getSession().getAttribute("currentTreeData");
		if(currentData == null)
        {
        	// Catch all in case the currentData is null
        	currentData = currentTreeNode.getData();
        }
        if(!currentData.getClass().getName().endsWith("RemoveAllInstanceSlotScript")) {
        	importspectree.setModel(importspectree.getModel());
        	return;
        }
		this.currentInstanceSlot = (RemoveAllInstanceSlotScript) currentData;
		
		//get the class name of the current parent Instance
		this.currentInstanceClass = currentInstanceSlot.getParentClassName();
		
		
		//set up the list of slot names
		List<String> classSlotList = this.getSlotNames(this.currentInstanceClass);
		List<String> completeSlotList = new ArrayList<String>();
		completeSlotList.add("- Select Instance Slot -");
		completeSlotList.addAll(classSlotList);
		slotNameListBox.setModel(new ListModelList(completeSlotList)); 


		
		//add the list of sibling instance variables
		DefaultTreeNode parentNode = (DefaultTreeNode) currentTreeNode.getParent();
		DefaultTreeNode worksheetNode = (DefaultTreeNode) parentNode.getParent();
		WorksheetImportSpecScript wsImportSpec = (WorksheetImportSpecScript) worksheetNode.getData();
	
		
		if(this.currentInstanceSlot != null) {
			//given an Instance Slot, set the values of the page's widgets
			
			//set the current slot name
			String slotName = this.currentInstanceSlot.getSlotName();
			if(slotName != null && slotName.length() > 0) {
				int slotNameIndex = completeSlotList.indexOf(slotName);
				if(slotNameIndex >= 0) {
					slotNameListBox.setSelectedIndex(slotNameIndex);
					oldSelectedSlotNameIndex = slotNameIndex;
				}
			}
			
			
			
			//initialise the list of conditional references
			conditionalRefList = this.currentInstanceSlot.getConditionalRef();
			
			//remove the worksheet attribute from the session
			desktop.getSession().removeAttribute("currentTreeData");
		} 
		
		//initialise the list of conditional column references
		ListModelList conditionalRefModel = new ListModelList(conditionalRefList);
		conditionalRefListBox.setModel(conditionalRefModel);
	}
	
	
	public void onSelect$slotNameListBox() {
		try {
			int slotNameIndex = slotNameListBox.getSelectedIndex();
			if(slotNameIndex > 0) {
				List<String> slotList = this.getSlotNames(this.currentInstanceClass);
				if(slotList != null) {
					int actualSlotIndex = slotNameIndex - 1;
					String slotName = slotList.get(actualSlotIndex);
					this.currentInstanceSlot.setSlotName(slotName);
					oldSelectedSlotNameIndex = slotNameIndex;
					
					
					//save the import spec data
					importSpecDataManager.saveSpreadsheetImportSpecData();
					 
					//refresh the import spec tree
					importspectree.setModel(importspectree.getModel());
				}
			} else {
				this.displayError("A valid slot name must be selected", "Slot Selection Error");
				slotNameListBox.setSelectedIndex(oldSelectedSlotNameIndex);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	
	public void onClick$addCondRefBtn() {
		try {
			//set the slot name of the current Slot in the import spec tree
			String newRef = condColumnTxtBox.getValue();
			if(newRef != null && newRef.length() > 0 && (!currentInstanceSlot.getConditionalRef().contains(newRef))) {
				this.currentInstanceSlot.getConditionalRef().add(newRef);	
				
				//save the import spec data
            	importSpecDataManager.saveSpreadsheetImportSpecData();
				
				conditionalRefListBox.setModel(new ListModelList(this.currentInstanceSlot.getConditionalRef()));
				condColumnTxtBox.setValue("");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	
	public void onClick$delCondRefBtn() {
		try {
			//delete the currently selected conditional reference
			int selectedIndex = conditionalRefListBox.getSelectedIndex();
			if(selectedIndex >= 0) {
				ListModelList listModel = (ListModelList) conditionalRefListBox.getListModel();
				Object currentSelection = listModel.getElementAt(selectedIndex);
				this.currentInstanceSlot.getConditionalRef().remove(selectedIndex);
				
				//save the import spec data
            	importSpecDataManager.saveSpreadsheetImportSpecData();
				
				listModel.remove(currentSelection);
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
