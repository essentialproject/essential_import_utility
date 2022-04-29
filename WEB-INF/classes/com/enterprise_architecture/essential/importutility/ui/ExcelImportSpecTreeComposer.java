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
 * 06.02.2019	JWC	Troubleshooting various click event issues 
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Executions;
//import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
//import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.common.script.DeleteAllInstanceSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DeleteDerivedInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DeleteInstanceSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DeleteSimpleInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedSimpleSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.GlobalInstanceScript;
import com.enterprise_architecture.essential.importutility.data.common.script.InstanceSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.PrimitiveSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.RemoveAllInstanceSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.RemoveInstanceSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.SimpleSlotScript;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;
import com.enterprise_architecture.essential.importutility.data.importspec.script.*;
import com.enterprise_architecture.essential.importutility.ui.treemodel.*;

/**
 * This class is the UI controller for the UI Components used to edit an Excel import specification
 * tree structure
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 * @author Jonathan Carter <jonathan.carter@e-asolutions.com>
 * @version 2.0 - 06.02.2019
 *
 */
public class ExcelImportSpecTreeComposer extends EssentialImportInterface {
	Tree importspectree;
	East eastpanel;
	//Spreadsheet spreadsheet;
	Combobox sheets;
	
	private ImportSpecDataManager impSpecDataManager;
	private ImportUtilityDataManager appDataManager;
	private SpreadsheetImportSpecScript importSpec;
	private ExcelImportSpecTreeNode treeRootNode;
	
	public Menuitem addGlobInstBtn;
	public Menuitem addWorksheetBtn;
	public Menuitem addDerivedInstBtn;
	public Menuitem addSimpleInstBtn;
	public Menuitem addDeleteDerivedInstBtn;
	public Menuitem addDeleteSimpleInstBtn;
	public Menuitem addSimpleSlotBtn;
	public Menuitem addDerivedSimpleSlotBtn;
	public Menuitem addInstanceSlotBtn;
	public Menuitem addPrimitiveSlotBtn;
	public Menuitem addRemoveInstanceSlotBtn;
	public Menuitem addDeleteInstanceSlotBtn;
	public Menuitem addRemoveAllInstanceSlotBtn;
	public Menuitem addDeleteAllInstanceSlotBtn;
	
	public Menuitem copyBtn;
	public Menuitem pasteBtn;
	public Menuitem moveUpBtn;
	public Menuitem moveDownBtn;
	
	
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		this.impSpecDataManager = null;
		this.appDataManager = null;
		this.importSpec = null;
		this.treeRootNode = null;
		
		if(this.importSpec == null) {
			//retrieve the current Import Activity from the session
	        ImportActivity currentImpAct = (ImportActivity) desktop.getSession().getAttribute("currentImpAct");
        
	        // 28.08.2018 JWC Add some trace during upgrade
	        //System.out.println("EDITING SPEC FOR IMP ACT: " + currentImpAct.getImportActivityName());
	        
	        
	        //get data manager for Import Specification if we don't already have one
	        if(currentImpAct != null) {
	        	//given an ImportActivity, set up the import spec data manager)
	        	this.appDataManager = this.getImportUtilityDataManager();
				this.impSpecDataManager = this.getImportSpecDataManager();
				this.importSpec = this.impSpecDataManager.getSpreadsheetImportSpecScriptData();
				
				//set up the import specification tree
				treeRootNode = new ExcelImportSpecTreeNode(importSpec, new ArrayList());
				final DefaultTreeModel importSpecTreeModel = new DefaultTreeModel(treeRootNode);
				importspectree.setModel(importSpecTreeModel);
				importspectree.setItemRenderer(new ImportSpecTreeRenderer());
								
				importspectree.addEventListener("onSelect", new EventListener() {
		            @Override
		            public void onEvent(Event event) throws Exception {
		            	
		            	Set selectedNodes = importSpecTreeModel.getSelection();
		            	Iterator selectedNodesIter = selectedNodes.iterator();
		            	if(selectedNodesIter.hasNext()) {
			            	DefaultTreeNode selectedNode = (DefaultTreeNode) selectedNodesIter.next();
			            	desktop.getSession().setAttribute("currentTreeNode", selectedNode);
			            	desktop.getSession().setAttribute("currentTreeData", selectedNode.getData());
			            	
			            	//System.out.println("!!>> Current Tree Node = " + desktop.getSession().getAttribute("currentTreeNode").toString());
			            	//System.out.println("!!>> Current Tree Data = " + desktop.getSession().getAttribute("currentTreeData").toString());
			            	// Update the selections in the tree
			            	sendTreeSelectionEvents(selectedNode);			            
		            	}		            	
		            }
		            
		        });
				
		     }
        }
	}
	
	
	 /**
     * Delete the current Node 
     */
    public void onClick$deleteBtn() {
    	final DefaultTreeNode aTreeNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
//    	if(desktop.getSession().getAttribute("currentTreeData") != null)
//    	{
//    		System.out.println("!!!> Current Tree Data = " + desktop.getSession().getAttribute("currentTreeData").toString());
//    	}
//    	else
//    	{
//    		System.out.println("!!!> Current Tree Data = null");
//        	
//    	}
    	
    	try {
    		if (aTreeNode != null && (!aTreeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.GlobalInstancesTreeNode"))) {
		    	Messagebox.show(("Are you sure that you want to permanently delete the selected element, \"" + aTreeNode.getData().toString() + "\"?" ), "Delete Import Spec element?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
		
												//System.out.println(">>>> Delete Button and AYS=YES...");
												
									    		String nodeClassName = aTreeNode.getClass().getName();
									    		DefaultTreeNode parentTreeNode = (DefaultTreeNode) aTreeNode.getParent();
									    		
									    		//remove a Global Instance from its parent Import Spec
									    		if(nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.GlobalInstanceTreeNode")) {	    		
									    			//System.out.println(">>>> Delete GlobalInstanaceTreeNode...");
													
									    			//remove the current worksheet from the import spec
										        	GlobalInstancesScript parentImportSpec = (GlobalInstancesScript) parentTreeNode.getData();
										        	Object currentGlobInstance = desktop.getSession().getAttribute("currentTreeData");
										        	
										        	//remove the current node from the import spec tree
										        	parentTreeNode.remove(aTreeNode);
										        	
										        	parentImportSpec.getDerivedInstance().remove(currentGlobInstance);									        	
										        											        	
										        	//refresh the import spec tree - no parent to which to switch focus
										    		sendTreeSelectionEvents(null);
										    		
										    		//save the import spec
										    		impSpecDataManager.saveSpreadsheetImportSpecData();
									    		}
									    		
									    		//remove a Worksheet Spec from its parent Import Spec
									    		if(nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.WorksheetTreeNode")) {
									    			//System.out.println(">>>> Delete WorksheetTreeNode...");
													
										    		//remove the current worksheet from the import spec
										        	SpreadsheetImportSpecScript parentImportSpec = (SpreadsheetImportSpecScript) parentTreeNode.getData();
										        	Object currentWorksheet = desktop.getSession().getAttribute("currentTreeData");
										        	//remove the current node from the import spec tree
										        	parentTreeNode.remove(aTreeNode);
										        	
										        	//System.out.println(">>>> Delete WorksheetTreeNode. Before # Worksheets = " + parentImportSpec.getWorksheetImportSpec().size());
//													if(currentWorksheet != null)
//													{
//														System.out.println("*** Current Worksheet is NOT null");
//													}
										        	List<WorksheetImportSpec> aWorksheetList = parentImportSpec.getWorksheetImportSpec();
										        	aWorksheetList.remove(currentWorksheet);
										        	//parentImportSpec.getWorksheetImportSpec().remove(currentWorksheet);
										        	
										        	//System.out.println(">>>> Delete WorksheetTreeNode. After # Worksheets = " + parentImportSpec.getWorksheetImportSpec().size());
										        	
										        	//refresh the import spec tree - no parent to which to switch focus
										        	sendTreeSelectionEvents(null);
										    		
										    		//save the import spec
										    		impSpecDataManager.saveSpreadsheetImportSpecData();
									    		}
									    		
									    		//remove an Instance Spec from its parent Worksheet
									    		if(nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteSimpleInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteDerivedInstanceTreeNode") ) {
									    			//System.out.println(">>>> Delete X-InstanaceTreeNode..." + aTreeNode.getClass());
									    			//System.out.println(">>>> Delete X-InstanaceTreeNode...parent = " + parentTreeNode.getClass());
																						    			
										    		//remove the current instance from the worksheet
										        	WorksheetImportSpecScript parentWorksheet = (WorksheetImportSpecScript) parentTreeNode.getData();
										        	Object currentData = desktop.getSession().getAttribute("currentTreeData");
										        	
										        	//remove the current node from the import spec tree
										        	parentTreeNode.remove(aTreeNode);
										    		
										        	parentWorksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().remove(currentData);
										    		
										        	//refresh the import spec tree
										        	// Move to focus to the parent node in the tree
										        	desktop.getSession().setAttribute("currentTreeNode", parentTreeNode);
									            	desktop.getSession().setAttribute("currentTreeData", parentTreeNode.getData());									            	
										        	sendTreeSelectionEvents(parentTreeNode);
										    		
										    		//save the import spec
										    		impSpecDataManager.saveSpreadsheetImportSpecData();
										    		
									    		}
									    	
										    	//remove a Slot Spec from its parent Instance
												if(nodeClassName.equals(
														"com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleSlotTreeNode")
													|| nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedSimpleSlotTreeNode") 
													|| nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.InstanceSlotTreeNode")
													|| nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.RemoveInstanceSlotTreeNode")
													|| nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteInstanceSlotTreeNode")
													|| nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.RemoveAllInstanceSlotTreeNode")
													|| nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteAllInstanceSlotTreeNode")
													|| nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.PrimitiveSlotTreeNode")) {
										    		
													//System.out.println(">>>> Delete x-SlotInstanaceTreeNode...");
													
										    		//remove the current slot from its parent instance spec
										        	Object parentInstanceSpec = parentTreeNode.getData();
										        	//remove the current node from the import spec tree
										        	parentTreeNode.remove(aTreeNode);
										        	
										        	Object childSlot = desktop.getSession().getAttribute("currentTreeData");
										        	if(parentInstanceSpec.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript")) {
										    			SimpleInstanceTypeScript parentSimpleInstance = (SimpleInstanceTypeScript) parentInstanceSpec;
										    			parentSimpleInstance.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().remove(childSlot);
										    		} else {
										    			DerivedInstanceTypeScript parentDerivedInstance = (DerivedInstanceTypeScript) parentInstanceSpec;
										    			parentDerivedInstance.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().remove(childSlot);
										    		}	
										        	
										        	//refresh the import spec tree
										        	// Move to focus to the parent node in the tree
										        	desktop.getSession().setAttribute("currentTreeNode", parentTreeNode);
									            	desktop.getSession().setAttribute("currentTreeData", parentTreeNode.getData());
									            	sendTreeSelectionEvents(parentTreeNode);
										    		
										    		//save the import spec
										    		impSpecDataManager.saveSpreadsheetImportSpecData();
										    	}	
									    	
											
											break;
										case Messagebox.NO: break;
									}
							}
						}
				);
    		}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    
    
    /**
     * Copy the data of the currently selected Instance Node or Slot Node
     */
    public void onClick$copyBtn() {
    	DefaultTreeNode aTreeNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    
    	if (aTreeNode != null) {
    		String nodeClassName = aTreeNode.getClass().getName();
    		if(nodeClassName.endsWith("InstanceTreeNode") || nodeClassName.endsWith("SlotTreeNode") || nodeClassName.endsWith("WorksheetTreeNode")) {
    			ImportSpecData nodeData = (ImportSpecData) aTreeNode.getData(); 	
    			if(nodeData != null) {
    				importspectree.setAttribute("copyBuffer", nodeData.getCopy());
    			}
    		}
    	}
    
    }
    
    

    /**
     * Copy the data of the currently selected Instance Node or Slot Node
     */
    public void onClick$pasteBtn() {
    	DefaultTreeNode aTreeNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    
    	if (aTreeNode != null) {
    		String nodeClassName = aTreeNode.getClass().getName();
    			Object copyBuffer =  importspectree.getAttribute("copyBuffer");
    			if(copyBuffer != null) {
    				String copyBufferClassName = copyBuffer.getClass().getName();			
    				//paste the 
    				if(copyBufferClassName.endsWith(".SimpleSlotScript")) {   					
    					SimpleSlotScript aSlot = (SimpleSlotScript) copyBuffer;
    					SimpleSlotScript newSlot = new SimpleSlotScript(aSlot);
    					this.addSimpleSlot(newSlot);
    		    	} else if(copyBufferClassName.endsWith(".PrimitiveSlotScript") ) {
    		    		PrimitiveSlotScript aSlot = (PrimitiveSlotScript) copyBuffer;
    		    		PrimitiveSlotScript newSlot = new PrimitiveSlotScript(aSlot);
    					this.addPrimitiveSlot(newSlot);
    		    	} else if(copyBufferClassName.endsWith(".InstanceSlotScript")) {
    		    		InstanceSlotScript aSlot = (InstanceSlotScript) copyBuffer;
    		    		InstanceSlotScript newSlot = new InstanceSlotScript(aSlot);
    					this.addInstanceSlot(newSlot);
    		    	} else if(copyBufferClassName.endsWith(".DerivedSimpleSlotScript")) {
    		    		DerivedSimpleSlotScript aSlot = (DerivedSimpleSlotScript) copyBuffer;
    		    		DerivedSimpleSlotScript newSlot = new DerivedSimpleSlotScript(aSlot);
    					this.addDerivedSimpleSlot(newSlot);
    		    	} else if(copyBufferClassName.endsWith(".RemoveInstanceSlotScript")) {
    		    		RemoveInstanceSlotScript aSlot = (RemoveInstanceSlotScript) copyBuffer;
    		    		RemoveInstanceSlotScript newSlot = new RemoveInstanceSlotScript(aSlot);
    					this.addRemoveInstanceSlot(newSlot);
    		    	} else if(copyBufferClassName.endsWith(".DeleteInstanceSlotScript")) {
    		    		DeleteInstanceSlotScript aSlot = (DeleteInstanceSlotScript) copyBuffer;
    		    		DeleteInstanceSlotScript newSlot = new DeleteInstanceSlotScript(aSlot);
    					this.addDeleteInstanceSlot(newSlot);
    		    	} else if(copyBufferClassName.endsWith(".RemoveAllInstanceSlotScript")) {
    		    		RemoveAllInstanceSlotScript aSlot = (RemoveAllInstanceSlotScript) copyBuffer;
    		    		RemoveAllInstanceSlotScript newSlot = new RemoveAllInstanceSlotScript(aSlot);
    					this.addRemoveAllInstanceSlot(newSlot);
    		    	} else if(copyBufferClassName.endsWith(".DeleteAllInstanceSlotScript")) {
    		    		DeleteAllInstanceSlotScript aSlot = (DeleteAllInstanceSlotScript) copyBuffer;
    		    		DeleteAllInstanceSlotScript newSlot = new DeleteAllInstanceSlotScript(aSlot);
    					this.addDeleteAllInstanceSlot(newSlot);
    		    	} else if(copyBufferClassName.endsWith(".SimpleInstanceTypeScript")) {
    		    		SimpleInstanceTypeScript anInstance = (SimpleInstanceTypeScript) copyBuffer;
    		    		SimpleInstanceTypeScript newInstance = new SimpleInstanceTypeScript(anInstance);
    					this.addSimpleInstance(newInstance);
    		    	} else if(copyBufferClassName.endsWith(".DerivedInstanceTypeScript")) {
    		    		DerivedInstanceTypeScript anInstance = (DerivedInstanceTypeScript) copyBuffer;
    		    		DerivedInstanceTypeScript newInstance = new DerivedInstanceTypeScript(anInstance);
    					this.addDerivedInstance(newInstance);
    		    	} else if(copyBufferClassName.endsWith(".DeleteSimpleInstanceTypeScript")) {
    		    		DeleteSimpleInstanceTypeScript anInstance = (DeleteSimpleInstanceTypeScript) copyBuffer;
    		    		DeleteSimpleInstanceTypeScript newInstance = new DeleteSimpleInstanceTypeScript(anInstance);
    					this.addDeleteSimpleInstance(newInstance);
    		    	} else if(copyBufferClassName.endsWith(".DeleteDerivedInstanceTypeScript")) {
    		    		DeleteDerivedInstanceTypeScript anInstance = (DeleteDerivedInstanceTypeScript) copyBuffer;
    		    		DeleteDerivedInstanceTypeScript newInstance = new DeleteDerivedInstanceTypeScript(anInstance);
    					this.addDeleteDerivedInstance(newInstance);
    		    	} else if(copyBufferClassName.endsWith(".WorksheetImportSpecScript")) {
    		    		WorksheetImportSpecScript aWorksheet = (WorksheetImportSpecScript) copyBuffer;
    		    		WorksheetImportSpecScript newWorksheet = new WorksheetImportSpecScript(aWorksheet);
    					this.addWorksheet(newWorksheet);
    		    	} 
    				
    			//	importspectree.removeAttribute("copyBuffer");
    			}
    		}
    }
    
    
	
	
	/**
     * Move the current Node up a level
     */
    public void onClick$moveUpBtn() {
    	DefaultTreeNode aTreeNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (aTreeNode != null) {
    		String nodeClassName = aTreeNode.getClass().getName();
    		if(nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteSimpleInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteDerivedInstanceTreeNode") ) {
	    		DefaultTreeNode parentTreeNode = (DefaultTreeNode) aTreeNode.getParent();
	    		WorksheetImportSpecScript parentWorksheet = (WorksheetImportSpecScript) parentTreeNode.getData();
	        	Object currentData = desktop.getSession().getAttribute("currentTreeData");
	    		int childIndex = parentTreeNode.getIndex(aTreeNode);
	    		if(childIndex > 0) {
	    			//move the current node up a place
	    			parentTreeNode.remove(aTreeNode);
	    			parentTreeNode.insert(aTreeNode, childIndex - 1);
	    			
	    			//move the current instance import spec up a place
	    			parentWorksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().remove(currentData);
	    			parentWorksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().add(childIndex - 1, currentData);
	    			
	    			//save the import spec
		    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    			
	    		}
	    		
		    	
		    	this.sendTreeSelectionEvents(aTreeNode);
    		}
    	
    	}

    }
    
    
    /**
     * Move the current Node down a level
     */
    public void onClick$moveDownBtn() {
    	DefaultTreeNode aTreeNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (aTreeNode != null) {
    		String nodeClassName = aTreeNode.getClass().getName();
    		if(nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteSimpleInstanceTreeNode") || nodeClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteDerivedInstanceTreeNode")) {
	    		DefaultTreeNode parentTreeNode = (DefaultTreeNode) aTreeNode.getParent();
	    		WorksheetImportSpecScript parentWorksheet = (WorksheetImportSpecScript) parentTreeNode.getData();
	        	Object currentData = desktop.getSession().getAttribute("currentTreeData");
	    		int childIndex = parentTreeNode.getIndex(aTreeNode);
	    		if(childIndex < (parentTreeNode.getChildCount() - 1)) {
	    			//move the current node up a place
	    			parentTreeNode.remove(aTreeNode);
	    			parentTreeNode.insert(aTreeNode, childIndex + 1);
	    			
	    			//move the current instance import spec up a place
	    			parentWorksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().remove(currentData);
	    			parentWorksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().add(childIndex + 1, currentData);
	    			
	    			//save the import spec
		    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    			
	    		}
		    	
		    	this.sendTreeSelectionEvents(aTreeNode);
    		}
    	
    	}

    }
    
    
    /**
     * Adds a new Worksheet element to the import spec
     */
    public void onClick$addWorksheetBtn() {
    	WorksheetImportSpecScript wsImportSpec =  this.impSpecDataManager.newWorksheetImportSpec(); 
    	this.addWorksheet(wsImportSpec);
    }
	
	/**
     * Adds a new Worksheet element to the import spec
     */
    private void addWorksheet(WorksheetImportSpecScript wsImportSpec) {
    	
    	this.importSpec.getWorksheetImportSpec().add(wsImportSpec);
		WorksheetTreeNode aTreeNode = new WorksheetTreeNode(wsImportSpec, new ArrayList() );
		desktop.getSession().setAttribute("currentTreeNode", aTreeNode);
		desktop.getSession().setAttribute("currentTreeData", wsImportSpec);
		treeRootNode.add(aTreeNode);
		
		//save the import spec
		impSpecDataManager.saveSpreadsheetImportSpecData();
		
		this.sendTreeSelectionEvents(aTreeNode);
    }
    
    
    /**
     * Adds a new Global Instance element to the import spec
     */
    public void onClick$addGlobInstBtn() {
    	GlobalInstanceScript globalVar =  this.impSpecDataManager.newGlobalInstance(); 
    	globalVar.setDerivedName(this.impSpecDataManager.newDerivedValue());
    	globalVar.setDerivedExtID(this.impSpecDataManager.newDerivedValue());
    	this.importSpec.getGlobalInstances().getDerivedInstance().add(globalVar);
    	GlobalInstancesTreeNode globInstancesNode = (GlobalInstancesTreeNode) treeRootNode.getChildAt(0);
    	// System.out.println("GLOBAL INSTANCE VAR: " + globInstancesNode);
    	
    	GlobalInstanceTreeNode globInstTreeNode = new GlobalInstanceTreeNode(globalVar);
    	desktop.getSession().setAttribute("currentTreeNode", globInstTreeNode);
    	desktop.getSession().setAttribute("currentTreeData", globalVar);
    	globInstancesNode.add(globInstTreeNode);
    	
    	//save the import spec
		impSpecDataManager.saveSpreadsheetImportSpecData();
    	
    	this.sendTreeSelectionEvents(globInstTreeNode);
    }
    
    
    /**
     * Adds a new SimpleInstance element to the import spec
     */
    public void onClick$addSimpleInstBtn() {
    	SimpleInstanceTypeScript simpleInst =  this.impSpecDataManager.newSimpleInstanceType(); 
    	this.addSimpleInstance(simpleInst);
    }
    
    /**
     * Adds a new SimpleInstance element to the import spec
     */
    private void addSimpleInstance(SimpleInstanceTypeScript simpleInst) {

    	WorksheetTreeNode parentNode = (WorksheetTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
	    	WorksheetImportSpecScript worksheet = (WorksheetImportSpecScript) parentNode.getData();
	    	worksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().add(simpleInst);
	    	SimpleInstanceTreeNode simpleInstTreeNode = new SimpleInstanceTreeNode(simpleInst);
	    	desktop.getSession().setAttribute("currentTreeNode", simpleInstTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", simpleInst);
	    	parentNode.add(simpleInstTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(simpleInstTreeNode);
    	
    	}
    }
    
    
    /**
     * Adds a new DeleteSimpleInstance element to the import spec
     */
    public void onClick$addDeleteSimpleInstBtn() {
    	DeleteSimpleInstanceTypeScript simpleInst =  this.impSpecDataManager.newDeleteSimpleInstanceType(); 
    	this.addDeleteSimpleInstance(simpleInst);
    }
    
    /**
     * Adds a new DeleteSimpleInstance element to the import spec
     */
    private void addDeleteSimpleInstance(DeleteSimpleInstanceTypeScript simpleInst) {

    	WorksheetTreeNode parentNode = (WorksheetTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
	    	WorksheetImportSpecScript worksheet = (WorksheetImportSpecScript) parentNode.getData();
	    	worksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().add(simpleInst);
	    	DeleteSimpleInstanceTreeNode simpleInstTreeNode = new DeleteSimpleInstanceTreeNode(simpleInst);
	    	desktop.getSession().setAttribute("currentTreeNode", simpleInstTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", simpleInst);
	    	parentNode.add(simpleInstTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(simpleInstTreeNode);
    	
    	}
    }
    
    
    
    
    /**
     * Adds a new Derived Instance element to the import spec
     */
    public void onClick$addDerivedInstBtn() {
    	DerivedInstanceTypeScript derivedInst =  this.impSpecDataManager.newDerivedInstanceType(); 
    	this.addDerivedInstance(derivedInst);
    }
    
    
    /**
     * Adds a new Derived Instance element to the import spec
     */
    private void addDerivedInstance(DerivedInstanceTypeScript derivedInst) {
    	WorksheetTreeNode parentNode = (WorksheetTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
	    	WorksheetImportSpecScript worksheet = (WorksheetImportSpecScript) parentNode.getData();
	    	worksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().add(derivedInst);
	    	DerivedInstanceTreeNode derivedInstTreeNode = new DerivedInstanceTreeNode(derivedInst);
	    	desktop.getSession().setAttribute("currentTreeNode", derivedInstTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", derivedInst);
	    	parentNode.add(derivedInstTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(derivedInstTreeNode);
    	}
    }
    
    
    /**
     * Adds a new Delete Derived Instance element to the import spec
     */
    public void onClick$addDeleteDerivedInstBtn() {
    	DeleteDerivedInstanceTypeScript derivedInst =  this.impSpecDataManager.newDeleteDerivedInstanceType(); 
    	this.addDeleteDerivedInstance(derivedInst);
    }
    
    
    /**
     * Adds a new Delete Derived Instance element to the import spec
     */
    private void addDeleteDerivedInstance(DeleteDerivedInstanceTypeScript derivedInst) {
    	WorksheetTreeNode parentNode = (WorksheetTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
	    	WorksheetImportSpecScript worksheet = (WorksheetImportSpecScript) parentNode.getData();
	    	worksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().add(derivedInst);
	    	DeleteDerivedInstanceTreeNode derivedInstTreeNode = new DeleteDerivedInstanceTreeNode(derivedInst);
	    	desktop.getSession().setAttribute("currentTreeNode", derivedInstTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", derivedInst);
	    	parentNode.add(derivedInstTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(derivedInstTreeNode);
    	
    	}
    }
    
    
    /**
     * Adds a new Simple Slot element to the import spec
     */
    public void onClick$addSimpleSlotBtn() {
    	SimpleSlotScript simpleSlot =  this.impSpecDataManager.newSimpleSlot(); 
    	this.addSimpleSlot(simpleSlot);
    }
    
    
    /**
     * Adds a new Simple Slot element to the import spec
     */
    private void addSimpleSlot(SimpleSlotScript simpleSlot) {
    	
    	DefaultTreeNode parentNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
    		if(parentNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {   			
    			SimpleInstanceTreeNode simpleInstTreeNode = (SimpleInstanceTreeNode) parentNode;
    			SimpleInstanceTypeScript simpleInst = (SimpleInstanceTypeScript) simpleInstTreeNode.getData();
    			simpleInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(simpleSlot);
    			simpleSlot.setParentInstanceVariable(simpleInst.getVariableName());
    			simpleSlot.setParentClassName(simpleInst.getClassName());
    		}
    		if(parentNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {    			
    			DerivedInstanceTreeNode derivedInstTreeNode = (DerivedInstanceTreeNode) parentNode;
    			DerivedInstanceTypeScript derivedInst = (DerivedInstanceTypeScript) derivedInstTreeNode.getData();
    			derivedInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(simpleSlot);
    			simpleSlot.setParentInstanceVariable(derivedInst.getVariableName());
    			simpleSlot.setParentClassName(derivedInst.getClassName());
    		}

	    	SimpleSlotTreeNode simpleSlotTreeNode = new SimpleSlotTreeNode(simpleSlot);
	    	desktop.getSession().setAttribute("currentTreeNode", simpleSlotTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", simpleSlot);
	    		    	
	    	try
	    	{
	    		parentNode.add(simpleSlotTreeNode);
	    	}
	    	catch(NullPointerException anNPE)
	    	{
	    		// Continue - absorb the exception
	    	}
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(simpleSlotTreeNode);
    	
    	}
    }
    
    
    /**
     * Adds a new Primitive Slot element to the import spec
     */
    public void onClick$addPrimitiveSlotBtn() {
    	PrimitiveSlotScript primitiveSlot =  this.impSpecDataManager.newPrimitiveSlot(); 
    	this.addPrimitiveSlot(primitiveSlot);
    }
    
    
    
    /**
     * Adds a new Primitive Slot element to the import spec
     */
    private void addPrimitiveSlot(PrimitiveSlotScript primitiveSlot) {

    	DefaultTreeNode parentNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
    		String parentClassName = parentNode.getClass().getName();
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {
    			SimpleInstanceTreeNode simpleInstTreeNode = (SimpleInstanceTreeNode) parentNode;
    			SimpleInstanceTypeScript simpleInst = (SimpleInstanceTypeScript) simpleInstTreeNode.getData();
    			simpleInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(primitiveSlot);
    			primitiveSlot.setParentInstanceVariable(simpleInst.getVariableName());
    			primitiveSlot.setParentClassName(simpleInst.getClassName());
    		}
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {
    			DerivedInstanceTreeNode derivedInstTreeNode = (DerivedInstanceTreeNode) parentNode;
    			DerivedInstanceTypeScript derivedInst = (DerivedInstanceTypeScript) derivedInstTreeNode.getData();
    			derivedInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(primitiveSlot);
    			primitiveSlot.setParentInstanceVariable(derivedInst.getVariableName());
    			primitiveSlot.setParentClassName(derivedInst.getClassName());
    		}

	    	PrimitiveSlotTreeNode primitiveSlotTreeNode = new PrimitiveSlotTreeNode(primitiveSlot);
	    	desktop.getSession().setAttribute("currentTreeNode", primitiveSlotTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", primitiveSlot);
	    	parentNode.add(primitiveSlotTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(primitiveSlotTreeNode);
    	
    	}
    }
    
    
    
    /**
     * Adds a new Instance Slot element to the import spec
     */
    public void onClick$addInstanceSlotBtn() {
    	InstanceSlotScript instanceSlot =  this.impSpecDataManager.newInstanceSlot(); 
    	this.addInstanceSlot(instanceSlot);
    }
    
    
    
    /**
     * Adds a new Instance Slot element to the import spec
     */
    private void addInstanceSlot(InstanceSlotScript instanceSlot) {

    	DefaultTreeNode parentNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
    		String parentClassName = parentNode.getClass().getName();
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {
    			SimpleInstanceTreeNode simpleInstTreeNode = (SimpleInstanceTreeNode) parentNode;
    			SimpleInstanceTypeScript simpleInst = (SimpleInstanceTypeScript) simpleInstTreeNode.getData();
    			simpleInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(simpleInst.getVariableName());
    			instanceSlot.setParentClassName(simpleInst.getClassName());
    		}
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {
    			DerivedInstanceTreeNode derivedInstTreeNode = (DerivedInstanceTreeNode) parentNode;
    			DerivedInstanceTypeScript derivedInst = (DerivedInstanceTypeScript) derivedInstTreeNode.getData();
    			derivedInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(derivedInst.getVariableName());
    			instanceSlot.setParentClassName(derivedInst.getClassName());
    		}

	    	InstanceSlotTreeNode instanceSlotTreeNode = new InstanceSlotTreeNode(instanceSlot);
	    	desktop.getSession().setAttribute("currentTreeNode", instanceSlotTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", instanceSlot);
	    	parentNode.add(instanceSlotTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(instanceSlotTreeNode);
    	
    	}
    }
    
    
    /**
     * Adds a new Remove Instance Slot element to the import spec
     */
    public void onClick$addRemoveInstanceSlotBtn() {
    	RemoveInstanceSlotScript instanceSlot =  this.impSpecDataManager.newRemoveInstanceSlot(); 
    	this.addRemoveInstanceSlot(instanceSlot);
    }
    
    
    
    /**
     * Adds a new Remove Instance Slot element to the import spec
     */
    private void addRemoveInstanceSlot(RemoveInstanceSlotScript instanceSlot) {

    	DefaultTreeNode parentNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
    		String parentClassName = parentNode.getClass().getName();
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {
    			SimpleInstanceTreeNode simpleInstTreeNode = (SimpleInstanceTreeNode) parentNode;
    			SimpleInstanceTypeScript simpleInst = (SimpleInstanceTypeScript) simpleInstTreeNode.getData();
    			simpleInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(simpleInst.getVariableName());
    			instanceSlot.setParentClassName(simpleInst.getClassName());
    		}
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {
    			DerivedInstanceTreeNode derivedInstTreeNode = (DerivedInstanceTreeNode) parentNode;
    			DerivedInstanceTypeScript derivedInst = (DerivedInstanceTypeScript) derivedInstTreeNode.getData();
    			derivedInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(derivedInst.getVariableName());
    			instanceSlot.setParentClassName(derivedInst.getClassName());
    		}

	    	RemoveInstanceSlotTreeNode instanceSlotTreeNode = new RemoveInstanceSlotTreeNode(instanceSlot);
	    	desktop.getSession().setAttribute("currentTreeNode", instanceSlotTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", instanceSlot);
	    	parentNode.add(instanceSlotTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(instanceSlotTreeNode);
    	
    	}
    }
    
    
    /**
     * Adds a new Delete Instance Slot element to the import spec
     */
    public void onClick$addDeleteInstanceSlotBtn() {
    	DeleteInstanceSlotScript instanceSlot =  this.impSpecDataManager.newDeleteInstanceSlot(); 
    	this.addDeleteInstanceSlot(instanceSlot);
    }
    
    
    
    /**
     * Adds a new Delete Instance Slot element to the import spec
     */
    private void addDeleteInstanceSlot(DeleteInstanceSlotScript instanceSlot) {

    	DefaultTreeNode parentNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
    		String parentClassName = parentNode.getClass().getName();
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {
    			SimpleInstanceTreeNode simpleInstTreeNode = (SimpleInstanceTreeNode) parentNode;
    			SimpleInstanceTypeScript simpleInst = (SimpleInstanceTypeScript) simpleInstTreeNode.getData();
    			simpleInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(simpleInst.getVariableName());
    			instanceSlot.setParentClassName(simpleInst.getClassName());
    		}
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {
    			DerivedInstanceTreeNode derivedInstTreeNode = (DerivedInstanceTreeNode) parentNode;
    			DerivedInstanceTypeScript derivedInst = (DerivedInstanceTypeScript) derivedInstTreeNode.getData();
    			derivedInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(derivedInst.getVariableName());
    			instanceSlot.setParentClassName(derivedInst.getClassName());
    		}

	    	DeleteInstanceSlotTreeNode instanceSlotTreeNode = new DeleteInstanceSlotTreeNode(instanceSlot);
	    	desktop.getSession().setAttribute("currentTreeNode", instanceSlotTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", instanceSlot);
	    	parentNode.add(instanceSlotTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(instanceSlotTreeNode);
    	
    	}
    }
    
    
    /**
     * Adds a new Remove All Instance Slot element to the import spec
     */
    public void onClick$addRemoveAllInstanceSlotBtn() {
    	RemoveAllInstanceSlotScript instanceSlot =  this.impSpecDataManager.newRemoveAllInstanceSlot(); 
    	this.addRemoveAllInstanceSlot(instanceSlot);
    }
    
    
    /**
     * Adds a new Remove All Instance Slot element to the import spec
     */
    private void addRemoveAllInstanceSlot(RemoveAllInstanceSlotScript instanceSlot) {

    	DefaultTreeNode parentNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
    		String parentClassName = parentNode.getClass().getName();
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {
    			SimpleInstanceTreeNode simpleInstTreeNode = (SimpleInstanceTreeNode) parentNode;
    			SimpleInstanceTypeScript simpleInst = (SimpleInstanceTypeScript) simpleInstTreeNode.getData();
    			simpleInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(simpleInst.getVariableName());
    			instanceSlot.setParentClassName(simpleInst.getClassName());
    		}
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {
    			DerivedInstanceTreeNode derivedInstTreeNode = (DerivedInstanceTreeNode) parentNode;
    			DerivedInstanceTypeScript derivedInst = (DerivedInstanceTypeScript) derivedInstTreeNode.getData();
    			derivedInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(derivedInst.getVariableName());
    			instanceSlot.setParentClassName(derivedInst.getClassName());
    		}

	    	RemoveAllInstanceSlotTreeNode instanceSlotTreeNode = new RemoveAllInstanceSlotTreeNode(instanceSlot);
	    	desktop.getSession().setAttribute("currentTreeNode", instanceSlotTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", instanceSlot);
	    	parentNode.add(instanceSlotTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(instanceSlotTreeNode);
    	
    	}
    }
    
    
    /**
     * Adds a new Delete Instance Slot element to the import spec
     */
    public void onClick$addDeleteAllInstanceSlotBtn() {
    	DeleteAllInstanceSlotScript instanceSlot =  this.impSpecDataManager.newDeleteAllInstanceSlot(); 
    	this.addDeleteAllInstanceSlot(instanceSlot);
    }
    
    
    
    /**
     * Adds a new Delete Instance Slot element to the import spec
     */
    private void addDeleteAllInstanceSlot(DeleteAllInstanceSlotScript instanceSlot) {

    	DefaultTreeNode parentNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
    		String parentClassName = parentNode.getClass().getName();
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {
    			SimpleInstanceTreeNode simpleInstTreeNode = (SimpleInstanceTreeNode) parentNode;
    			SimpleInstanceTypeScript simpleInst = (SimpleInstanceTypeScript) simpleInstTreeNode.getData();
    			simpleInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(simpleInst.getVariableName());
    			instanceSlot.setParentClassName(simpleInst.getClassName());
    		}
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {
    			DerivedInstanceTreeNode derivedInstTreeNode = (DerivedInstanceTreeNode) parentNode;
    			DerivedInstanceTypeScript derivedInst = (DerivedInstanceTypeScript) derivedInstTreeNode.getData();
    			derivedInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(instanceSlot);
    			instanceSlot.setParentInstanceVariable(derivedInst.getVariableName());
    			instanceSlot.setParentClassName(derivedInst.getClassName());
    		}

	    	DeleteAllInstanceSlotTreeNode instanceSlotTreeNode = new DeleteAllInstanceSlotTreeNode(instanceSlot);
	    	desktop.getSession().setAttribute("currentTreeNode", instanceSlotTreeNode);
	    	desktop.getSession().setAttribute("currentTreeData", instanceSlot);
	    	parentNode.add(instanceSlotTreeNode);
	    	
	    	//save the import spec
    		impSpecDataManager.saveSpreadsheetImportSpecData();
	    	
	    	this.sendTreeSelectionEvents(instanceSlotTreeNode);
    	
    	}
    }
    
    
    
    /**
     * Adds a new Derived Simple Slot element to the import spec
     */
    public void onClick$addDerivedSimpleSlotBtn() {
    	DerivedSimpleSlotScript derivedSimpleSlot =  this.impSpecDataManager.newDerivedSimpleSlot(); 
    	this.addDerivedSimpleSlot(derivedSimpleSlot);
    }
    
    
    /**
     * Adds a new Derived Simple Slot element to the import spec
     */
    private void addDerivedSimpleSlot(DerivedSimpleSlotScript derivedSimpleSlot) {

    	DefaultTreeNode parentNode = (DefaultTreeNode) desktop.getSession().getAttribute("currentTreeNode");
    	
    	if (parentNode != null) {
    		String parentClassName = parentNode.getClass().getName();
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {
    			SimpleInstanceTreeNode simpleInstTreeNode = (SimpleInstanceTreeNode) parentNode;
    			SimpleInstanceTypeScript simpleInst = (SimpleInstanceTypeScript) simpleInstTreeNode.getData();
    			System.out.println("ADDING DERIVED SLOT TO SIMPLE INSTANCE");
    			if(simpleInst.getClassName() != null && simpleInst.getClassName().length() > 0 ) {
    				simpleInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(derivedSimpleSlot);
    				derivedSimpleSlot.setParentClassName(simpleInst.getClassName());
    				derivedSimpleSlot.setParentInstanceVariable(simpleInst.getVariableName());
    				
    				DerivedSimpleSlotTreeNode derivedSimpleSlotTreeNode = new DerivedSimpleSlotTreeNode(derivedSimpleSlot);
    		    	desktop.getSession().setAttribute("currentTreeNode", derivedSimpleSlotTreeNode);
    		    	desktop.getSession().setAttribute("currentTreeData", derivedSimpleSlot);
    		    	parentNode.add(derivedSimpleSlotTreeNode);
    				
    				//save the import spec
    	    		impSpecDataManager.saveSpreadsheetImportSpecData();
    	    		
    	    		this.sendTreeSelectionEvents(derivedSimpleSlotTreeNode);
    			} else {
    				try {
    					Messagebox.show("A class must be defined in the parent Instance element to create a Derived Simple Slot", "Invalid Parent Instance", Messagebox.OK, Messagebox.ERROR);
    					return;
    				}
    				catch(Exception e) {
    					e.printStackTrace();
    				}
    			}
    		}
    		if(parentClassName.equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {
    			DerivedInstanceTreeNode derivedInstTreeNode = (DerivedInstanceTreeNode) parentNode;
    			DerivedInstanceTypeScript derivedInst = (DerivedInstanceTypeScript) derivedInstTreeNode.getData();
    			if(derivedInst.getClassName() != null && derivedInst.getClassName().length() > 0 ) {
    				derivedInst.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().add(derivedSimpleSlot);
    				derivedSimpleSlot.setParentClassName(derivedInst.getClassName());
    				derivedSimpleSlot.setParentInstanceVariable(derivedInst.getVariableName());
    				
    				DerivedSimpleSlotTreeNode derivedSimpleSlotTreeNode = new DerivedSimpleSlotTreeNode(derivedSimpleSlot);
    		    	desktop.getSession().setAttribute("currentTreeNode", derivedSimpleSlotTreeNode);
    		    	desktop.getSession().setAttribute("currentTreeData", derivedSimpleSlot);
    		    	parentNode.add(derivedSimpleSlotTreeNode);
    				
    				//save the import spec
    	    		impSpecDataManager.saveSpreadsheetImportSpecData();
    	    		
    	    		this.sendTreeSelectionEvents(derivedSimpleSlotTreeNode);
    			} else {
    				try {
    					Messagebox.show("A class must be defined in the parent Instance element to create a Derived Simple Slot", "Invalid Parent Instance", Messagebox.OK, Messagebox.ERROR);
    					return;
    				}
    				catch(Exception e) {
    					e.printStackTrace();
    				}
    			}
    		}

    	
    	}
    }
    
    
    
    /**
     * Sends notification events to widgets in the import spec editor
     */
    private void sendTreeSelectionEvents(DefaultTreeNode currentNode) {
    	DefaultTreeModel aTreeModel = (DefaultTreeModel)importspectree.getModel();		
    	if(currentNode != null)
    	{
    		this.setMenuStatus(currentNode);    		
    		if(aTreeModel.isSelected(currentNode) == false)
			{
				aTreeModel.addToSelection(currentNode);	
				eastpanel.getChildren().clear();
			}
			aTreeModel.addOpenObject(currentNode);			
						
    	} else {  
    		importspectree.setModel(aTreeModel);
    		eastpanel.getChildren().clear();
    		
    	}
    }
    
    
    /**
     * Disables and enables the menu buttons, depending upon the currently selected Node
     */
    private void setMenuStatus(DefaultTreeNode currentNode) {
    	String nodeClass = currentNode.getClass().getName();
    	Object copyBuffer = importspectree.getAttribute("copyBuffer");
    	String copyBufferClass = "";
    	if(copyBuffer != null) {
    		copyBufferClass = copyBuffer.getClass().getName();
    	}
    	if(nodeClass.endsWith("SimpleSlotTreeNode") || nodeClass.endsWith("PrimitiveSlotTreeNode") || nodeClass.endsWith("InstanceSlotTreeNode") || nodeClass.endsWith("DerivedSimpleSlotTreeNode")  || nodeClass.endsWith("RemoveInstanceSlotTreeNode") || nodeClass.endsWith("DeleteInstanceSlotTreeNode") || nodeClass.endsWith("RemoveAllInstanceSlotTreeNode") || nodeClass.endsWith("DeleteAllInstanceSlotTreeNode")) {
    		addGlobInstBtn.setDisabled(true);
        	addWorksheetBtn.setDisabled(false);
        	addDerivedInstBtn.setDisabled(true);
        	addSimpleInstBtn.setDisabled(true);
        	addDeleteDerivedInstBtn.setDisabled(true);
        	addDeleteSimpleInstBtn.setDisabled(true);
        	addSimpleSlotBtn.setDisabled(true);
        	addDerivedSimpleSlotBtn.setDisabled(true);
        	addInstanceSlotBtn.setDisabled(true);
        	addRemoveInstanceSlotBtn.setDisabled(true);
        	addDeleteInstanceSlotBtn.setDisabled(true);
        	addRemoveAllInstanceSlotBtn.setDisabled(true);
        	addDeleteAllInstanceSlotBtn.setDisabled(true);
        	addPrimitiveSlotBtn.setDisabled(true);
        	
        	copyBtn.setDisabled(false);
        	if(copyBufferClass.endsWith("WorksheetImportSpecScript")) {
        		pasteBtn.setDisabled(false);
        	} else {
        		pasteBtn.setDisabled(true);
        	}
    	}
    	else if(nodeClass.endsWith("SimpleInstanceTreeNode") || nodeClass.endsWith("DerivedInstanceTreeNode")) {
    		addGlobInstBtn.setDisabled(true);
    		addWorksheetBtn.setDisabled(false);
    		addDerivedInstBtn.setDisabled(true);
        	addSimpleInstBtn.setDisabled(true);
        	addDeleteDerivedInstBtn.setDisabled(true);
        	addDeleteSimpleInstBtn.setDisabled(true);
        	addSimpleSlotBtn.setDisabled(false);
        	addDerivedSimpleSlotBtn.setDisabled(false);
        	addInstanceSlotBtn.setDisabled(false);
        	addPrimitiveSlotBtn.setDisabled(false); 
        	addRemoveInstanceSlotBtn.setDisabled(false);
        	addDeleteInstanceSlotBtn.setDisabled(false);
        	addRemoveAllInstanceSlotBtn.setDisabled(false);
        	addDeleteAllInstanceSlotBtn.setDisabled(false);
        	
        	copyBtn.setDisabled(false);
        	if(copyBufferClass.endsWith("SlotScript") || copyBufferClass.endsWith("WorksheetImportSpecScript")) {
        		pasteBtn.setDisabled(false);
        	} else {
        		pasteBtn.setDisabled(true);
        	}
    	} 
    	else if(nodeClass.endsWith("DeleteSimpleInstanceTreeNode") || nodeClass.endsWith("DeleteDerivedInstanceTreeNode")) {
    		addGlobInstBtn.setDisabled(true);
    		addWorksheetBtn.setDisabled(false);
    		addDerivedInstBtn.setDisabled(true);
        	addSimpleInstBtn.setDisabled(true);
        	addDeleteDerivedInstBtn.setDisabled(true);
        	addDeleteSimpleInstBtn.setDisabled(true);
        	addSimpleSlotBtn.setDisabled(true);
        	addDerivedSimpleSlotBtn.setDisabled(true);
        	addInstanceSlotBtn.setDisabled(true);
        	addPrimitiveSlotBtn.setDisabled(true); 
        	addRemoveInstanceSlotBtn.setDisabled(true);
        	addDeleteInstanceSlotBtn.setDisabled(true);
        	addRemoveAllInstanceSlotBtn.setDisabled(true);
        	addDeleteAllInstanceSlotBtn.setDisabled(true);
        	
        	copyBtn.setDisabled(false);
        	if(copyBufferClass.endsWith("SlotScript") || copyBufferClass.endsWith("WorksheetImportSpecScript")) {
        		pasteBtn.setDisabled(false);
        	} else {
        		pasteBtn.setDisabled(true);
        	}
    	} 
    	else if(nodeClass.endsWith("WorksheetTreeNode")) {
    		addGlobInstBtn.setDisabled(true);
    		addWorksheetBtn.setDisabled(false);
    		addDerivedInstBtn.setDisabled(false);
        	addSimpleInstBtn.setDisabled(false);
        	addDeleteDerivedInstBtn.setDisabled(false);
        	addDeleteSimpleInstBtn.setDisabled(false);
        	addSimpleSlotBtn.setDisabled(true);
        	addDerivedSimpleSlotBtn.setDisabled(true);
        	addInstanceSlotBtn.setDisabled(true);
        	addPrimitiveSlotBtn.setDisabled(true);
        	addRemoveInstanceSlotBtn.setDisabled(true);
        	addDeleteInstanceSlotBtn.setDisabled(true);
        	addRemoveAllInstanceSlotBtn.setDisabled(true);
        	addDeleteAllInstanceSlotBtn.setDisabled(true);
        	copyBtn.setDisabled(false);
        	
        	if(copyBufferClass.endsWith("InstanceTypeScript")  || copyBufferClass.endsWith("WorksheetImportSpecScript")) {
        		pasteBtn.setDisabled(false);
        	} else {
        		pasteBtn.setDisabled(true);
        	}
    	} 
    	else if(nodeClass.endsWith("GlobalInstanceTreeNode")) {
    		addGlobInstBtn.setDisabled(true);
    		addWorksheetBtn.setDisabled(false);
    		addDerivedInstBtn.setDisabled(true);
        	addSimpleInstBtn.setDisabled(true);
        	addDeleteDerivedInstBtn.setDisabled(true);
        	addDeleteSimpleInstBtn.setDisabled(true);
        	addSimpleSlotBtn.setDisabled(true);
        	addDerivedSimpleSlotBtn.setDisabled(true);
        	addInstanceSlotBtn.setDisabled(true);
        	addPrimitiveSlotBtn.setDisabled(true);
        	addRemoveInstanceSlotBtn.setDisabled(true);
        	addDeleteInstanceSlotBtn.setDisabled(true);
        	addRemoveAllInstanceSlotBtn.setDisabled(true);
        	addDeleteAllInstanceSlotBtn.setDisabled(true);
        	copyBtn.setDisabled(true);
        	if(copyBufferClass.endsWith("WorksheetImportSpecScript")) {
        		pasteBtn.setDisabled(false);
        	} else {
        		pasteBtn.setDisabled(true);
        	}
    	} 
    	else if(nodeClass.endsWith("GlobalInstancesTreeNode")) {
    		addGlobInstBtn.setDisabled(false);
    		addWorksheetBtn.setDisabled(false);
    		addDerivedInstBtn.setDisabled(true);
        	addSimpleInstBtn.setDisabled(true);
        	addDeleteDerivedInstBtn.setDisabled(true);
        	addDeleteSimpleInstBtn.setDisabled(true);
        	addSimpleSlotBtn.setDisabled(true);
        	addDerivedSimpleSlotBtn.setDisabled(true);
        	addInstanceSlotBtn.setDisabled(true);
        	addPrimitiveSlotBtn.setDisabled(true);
        	addRemoveInstanceSlotBtn.setDisabled(true);
        	addDeleteInstanceSlotBtn.setDisabled(true);
        	addRemoveAllInstanceSlotBtn.setDisabled(true);
        	addDeleteAllInstanceSlotBtn.setDisabled(true);
        	copyBtn.setDisabled(true);
        	if(copyBufferClass.endsWith("WorksheetImportSpecScript")) {
        		pasteBtn.setDisabled(false);
        	} else {
        		pasteBtn.setDisabled(true);
        	}
    	} 
    }
    
	
}
