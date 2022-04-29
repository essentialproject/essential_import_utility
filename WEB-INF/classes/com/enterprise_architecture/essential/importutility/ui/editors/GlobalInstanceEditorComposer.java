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
 * 23.06.2018	JWC Add new argument to ListItemRenderer.render()
 * 
 */
package com.enterprise_architecture.essential.importutility.ui.editors;

//import java.math.BigInteger;
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

import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.common.script.*;
//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
//import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
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
public class GlobalInstanceEditorComposer extends EssentialImportInterface {

	private ImportSpecDataManager importSpecDataManager;
	
	public Div globInstEditorDiv;
	public Listbox globalInstListBox;
	
	private GlobalInstanceScript currentGlobalInstance;
	private List<DerivedInstanceType> itsGlobInstList;
	private int oldSelectedIndex = 0;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		IdSpace window = globInstEditorDiv.getSpaceOwner();
        final Borderlayout borderLayout = (Borderlayout) window.getFellow("editExcelBorderLayout");
        
        final West westpanel = borderLayout.getWest();
        final Tree importspectree = (Tree) westpanel.getFellow("importspectree");
		final TreeNode currentTreeNode = (TreeNode) desktop.getSession().getAttribute("currentTreeNode");
		
    	
		//get the data managers
		this.importSpecDataManager = this.getImportSpecDataManager();
		ImportUtilityDataManager importAppDataManager = this.getImportUtilityDataManager();
		
		
		//get the current Global Instance
        Object currentData = desktop.getSession().getAttribute("currentTreeData");
        if(currentData == null)
        {
        	// Catch all in case the currentData is null
        	currentData = currentTreeNode.getData();
        }
        if(!currentData.getClass().getName().endsWith("GlobalInstanceScript")) {
        	importspectree.setModel(importspectree.getModel());
        	return;
        }
		this.currentGlobalInstance = (GlobalInstanceScript) currentData;
		
		
		List<DerivedInstanceType> globInstances = importAppDataManager.getImportUtilityData().getGlobalInstances();
		DerivedInstanceType blankInstance = importSpecDataManager.newDerivedInstanceType();
		if (globInstances.size() > 0) {
			blankInstance.setVariableName("- Select Global Instance -");
		} else {
			blankInstance.setVariableName("<No Global Instances Defined>");
		}
		itsGlobInstList = new ArrayList<DerivedInstanceType>();
		itsGlobInstList.add(blankInstance);
		itsGlobInstList.addAll(globInstances);
        ListModelList model = new ListModelList(itsGlobInstList);
        
        globalInstListBox.setModel(model);
        
        globalInstListBox.addEventListener("onSelect", new EventListener(){
            public void onEvent(Event event) throws Exception {
            	//refresh the import spec tree
            	int selectionIndex = globalInstListBox.getSelectedIndex();
            	if(selectionIndex > 0) {
	            	ListModel listModel = globalInstListBox.getModel();
	            	DerivedInstanceType selectedInstance = (DerivedInstanceType) listModel.getElementAt(selectionIndex);
	            	copyGlobalInstanceValues(selectedInstance);
	            	oldSelectedIndex = selectionIndex;
	            	
	            	//refresh the import spec tree
	            	importspectree.setModel(importspectree.getModel());
	            	
	            	//save the import spec
	            	importSpecDataManager.saveSpreadsheetImportSpecData();
	            	
            	} else {
            		Messagebox.show("A global variable must be selected", "Set Global Variable Error", Messagebox.OK, Messagebox.ERROR);
            		globalInstListBox.setSelectedIndex(oldSelectedIndex);
            	}
            }
        });
        
        globalInstListBox.setItemRenderer(new ListitemRenderer() {
        	public void render(Listitem anItem, Object data, int theIndex) {
        		DerivedInstanceType anInst = (DerivedInstanceType) data;
        		String globInstLabel;
        		if(anInst.getClassName() != null) {
        			globInstLabel = anInst.getVariableName() + " (" + anInst.getClassName() + ")";	
        		} else {
        			globInstLabel = anInst.getVariableName();
        		}
        		anItem.setLabel(globInstLabel);
        	}
        });
        
		
		if(this.currentGlobalInstance != null) {
			//given a Global Instance, set the values of the page's widgets
			
			//pre-select the name of the current GlobalInstance in the list
			String currentVarName = this.currentGlobalInstance.getVariableName();
			Listitem globInstListItem;
	        DerivedInstanceType aGlobInst;
	        Iterator globInstIter = itsGlobInstList.iterator();
			if(currentVarName != null) {
				while(globInstIter.hasNext()) {
					aGlobInst = (DerivedInstanceType) globInstIter.next();
					if((aGlobInst != null) && (aGlobInst.getVariableName().equals(currentVarName))) {
						int instIndex = this.itsGlobInstList.indexOf(aGlobInst);
						globalInstListBox.setSelectedIndex(instIndex);		
						this.oldSelectedIndex = instIndex;
						break;
					}
				}
			}
			
			//remove the worksheet attribute from the session
			desktop.getSession().removeAttribute("currentTreeData");
		} 
	}
	
	
	private void copyGlobalInstanceValues(DerivedInstanceType sourceInstance) {
		if (this.currentGlobalInstance != null) {
			this.currentGlobalInstance.setClassName(sourceInstance.getClassName());
			this.currentGlobalInstance.setVariableName(sourceInstance.getVariableName());
			
			//set up the local variables required to update the global instance name and ID
			Object valueSegment;
			DerivedValueString valueString;
			DerivedValueRef valueRef;
			DerivedValueStringScript newValueStringScript;
			DerivedValueRefScript newValueRefScript;
			
			
			//clear the current instance's name
			DerivedValue currentDerivedName = this.currentGlobalInstance.getDerivedName();
			currentDerivedName.getDerivedValueStringOrDerivedValueRef().clear();
			
			//copy the derived name of the source global instance
			DerivedValue sourceNameValue = sourceInstance.getDerivedName();
			List sourceNameValueList = sourceNameValue.getDerivedValueStringOrDerivedValueRef();
			Iterator sourceNameValueIter = sourceNameValueList.iterator();
			
			while(sourceNameValueIter.hasNext()) {
				valueSegment = sourceNameValueIter.next();
				if(valueSegment.getClass().getName() == "com.enterprise_architecture.essential.importutility.data.common.DerivedValueString") {
					valueString = (DerivedValueString) valueSegment;
					newValueStringScript = this.importSpecDataManager.newDerivedValueString();
					newValueStringScript.setContent(valueString.getContent());
					currentDerivedName.getDerivedValueStringOrDerivedValueRef().add(newValueStringScript);
				} else if(valueSegment.getClass().getName() == "com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef") {
					valueRef = (DerivedValueRef) valueSegment;
					newValueRefScript = this.importSpecDataManager.newDerivedValueRef();
					newValueRefScript.setValue(valueRef.getValue());
					currentDerivedName.getDerivedValueStringOrDerivedValueRef().add(newValueRefScript);
				}
			}
			
			//clear the current instance's ID
			DerivedValue currentDerivedID = this.currentGlobalInstance.getDerivedExtID();
			currentDerivedID.getDerivedValueStringOrDerivedValueRef().clear();
			
			
			//copy the derived ID of the source global instance
			DerivedValue sourceIDValue = sourceInstance.getDerivedExtID();
			List sourceIDValueList = sourceIDValue.getDerivedValueStringOrDerivedValueRef();
			Iterator sourceIDValueIter = sourceIDValueList.iterator();
			
			while(sourceIDValueIter.hasNext()) {
				valueSegment = sourceIDValueIter.next();
				if(valueSegment.getClass().getName() == "com.enterprise_architecture.essential.importutility.data.common.DerivedValueString") {
					valueString = (DerivedValueString) valueSegment;
					newValueStringScript = this.importSpecDataManager.newDerivedValueString();
					newValueStringScript.setContent(valueString.getContent());
					currentDerivedID.getDerivedValueStringOrDerivedValueRef().add(newValueStringScript);
				} else if(valueSegment.getClass().getName() == "com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef") {
					valueRef = (DerivedValueRef) valueSegment;
					newValueRefScript = this.importSpecDataManager.newDerivedValueRef();
					newValueRefScript.setValue(valueRef.getValue());
					currentDerivedID.getDerivedValueStringOrDerivedValueRef().add(newValueRefScript);
				}
			}
		}
	}
	
	
}
