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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
//import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedInstanceTypeScript;
//import com.enterprise_architecture.essential.importutility.data.common.script.DerivedSimpleSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueScript;
//import com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript;
//import com.enterprise_architecture.essential.importutility.data.common.script.SimpleSlotScript;
//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
//import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDatas;
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
public class DerivedInstanceEditorComposer extends EssentialImportInterface {

	private DerivedInstanceTypeScript currentInstance;
	private ImportSpecDataManager dataManager;
	private ImportUtilityDataManager appDataManager;
	private Tree importspectree;
	private List<String> classNameList;
	private String currentClassName;
	private String currentVarName;
	
	public Div derivedInstanceDiv;
	public Textbox varNameTxtBox;
	public Combobox classNameTxtBox;
	public Listbox matchingModeListBox;
	
	public Textbox idFixedTxtBox;
	public Textbox idColumnTxtBox;
	public Textbox idPreDelimTxtBox;
	public Textbox idPostDelimTxtBox;
	public Listbox idSegmentsListBox;
	public Label idFormatLabel;
	
	public Textbox nameFixedTxtBox;
	public Textbox nameColumnTxtBox;
	public Textbox namePreDelimTxtBox;
	public Textbox namePostDelimTxtBox;
	public Listbox nameSegmentsListBox;
	public Label nameFormatLabel;
	
	private List<String> matchingModes;
	private int oldSelectedIndex = 0;
	
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		//System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 110 comp = " + comp.getClass());
		IdSpace window = derivedInstanceDiv.getSpaceOwner();
        final Borderlayout borderLayout = (Borderlayout) window.getFellow("editExcelBorderLayout");
        
        final West westpanel = borderLayout.getWest();
        this.importspectree = (Tree) westpanel.getFellow("importspectree");
        
        //reset the height of the spreadsheet panel to make room for the editor
        //South southpanel = borderLayout.getSouth();
        //southpanel.setHeight("100px");
        
		//get the current Derived Instance
        Object currentData = desktop.getSession().getAttribute("currentTreeData");
        if(currentData == null)
        {
        	// Catch all in case the currentData is null
        	final TreeNode currentTreeNode = (TreeNode) desktop.getSession().getAttribute("currentTreeNode");
        	currentData = currentTreeNode.getData();
        }
        if(!currentData.getClass().getName().endsWith("DerivedInstanceTypeScript")) {        	
        	importspectree.setModel(importspectree.getModel());
        	        
        	return;
        }        
		this.currentInstance = (DerivedInstanceTypeScript) currentData;		
		
		//get the data manager for import specs
		dataManager = this.getImportSpecDataManager();
		
		//System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 140. DataManager Spec file = " + dataManager.getImportSpecFilePath()); 
		
		//get the application data manager
		appDataManager = this.getImportUtilityDataManager();
		
		//System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 145. App Data Mgr config file = " + appDataManager.getConfigFilePath());
		
		//initialise the list of Essential class names
		classNameTxtBox.setModel(ListModels.toListSubModel(new ListModelList(getClassNameList()))); 
		
		//System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 150");
		
		matchingModes = new ArrayList<String>();
		matchingModes.add("<Select Mode>");
		matchingModes.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME);
		matchingModes.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYID);
		matchingModes.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYINTID);
		matchingModes.add(ImportSpecDataManager.IMPORT_MATCHMODE_NEW);
        ListModelList model = new ListModelList(matchingModes);
        
        matchingModeListBox.setModel(model);
        
        //System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 156");
        
        matchingModeListBox.addEventListener("onSelect", new EventListener(){
            public void onEvent(Event event) throws Exception {
            	//refresh the import spec tree
            	int selectionIndex = matchingModeListBox.getSelectedIndex();
            	if(selectionIndex > 0) {
	            	ListModel listModel = matchingModeListBox.getModel();
	            	String newMatchingMode = (String) listModel.getElementAt(selectionIndex);
	            	currentInstance.setMatchingMode(newMatchingMode);
	            	oldSelectedIndex = selectionIndex;
	            	
	            	//save the import spec
	            	dataManager.saveSpreadsheetImportSpecData();
	            	
	            	//refresh the import spec tree
	            	importspectree.setModel(importspectree.getModel());
	        	} else {
	        		Messagebox.show("An instance matching mode must be selected", "Set Matching Mode Error", Messagebox.OK, Messagebox.ERROR);
	        		matchingModeListBox.setSelectedIndex(oldSelectedIndex);
	        	}
            }
        });
        //System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 185");
		
		if(this.currentInstance != null) {
			//given an existing Derived Instance, set the values of the page's widgets
			//System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 189");
			//set the variable name text box
			this.currentVarName = this.currentInstance.getVariableName();
			this.varNameTxtBox.setValue(currentVarName);
			
			//set the class name text box
			String className = this.currentInstance.getClassName();
			this.classNameTxtBox.setValue(className);
			this.currentClassName = className;
			
			
			//set up the list of value segments for the External ID
			DerivedValueScript extIDValue = (DerivedValueScript) this.currentInstance.getDerivedExtID();
			this.updateDerivedValueList(idSegmentsListBox, extIDValue);
			
			//set up the list of value segments for the External ID
			DerivedValueScript nameValue = (DerivedValueScript) this.currentInstance.getDerivedName();
			this.updateDerivedValueList(nameSegmentsListBox, nameValue);
			
			
			//pre-select the matching mode of the current Derived Instance
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
			//System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 226");
			desktop.getSession().removeAttribute("currentTreeData");
		}
		Events.sendEvent("onClick", comp, null);
		//System.out.println("DerivedInstanceEditorComposer.doAfterCompose(): 229");
		
	}
	
	
	public void onChange$varNameTxtBox() {
		try {
			//System.out.println("DerivedInstanceEditorComposer.onChange$varNameTxtBox(): 244");
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
	
	
	
	public void onChange$classNameTxtBox() {
		try {
			//System.out.println("DerivedInstanceEditorComposer.onChange$classNameTxtBox(): 278");
			//set the slot name of the current Slot in the import spec tree
			String className = classNameTxtBox.getValue();
			
			if(this.classNameList.contains(className)) {
				this.currentClassName = className;
				this.currentInstance.setClassName(className);	
				this.initIDValueSegmentList(className);
				
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
	
	
	public void onClick$addIDTxtBtn() {
		//System.out.println("DerivedInstanceEditorComposer.onChange$addIDTxtBox(): 311");
		String fixedText = idFixedTxtBox.getValue();
		if(fixedText.length() > 0) {
			DerivedValue currentID = this.currentInstance.getDerivedExtID();	
			this.addTextSegment(fixedText, idSegmentsListBox, currentID);
			
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();
		}
	}
	
	
	public void onClick$addIDRefBtn() {
			DerivedValue currentID = this.currentInstance.getDerivedExtID();
			this.addRefSegment(idSegmentsListBox, currentID);
			
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();
	}
	
	
	public void onClick$idSegmentUpBtn() {
		DerivedValue currentID = this.currentInstance.getDerivedExtID();
		this.moveSegmentUp(idSegmentsListBox, currentID);
		
		//save the import spec data
		dataManager.saveSpreadsheetImportSpecData();
	}
	
	
	public void onClick$idSegmentDownBtn() {
		DerivedValue currentID = this.currentInstance.getDerivedExtID();
		this.moveSegmentDown(idSegmentsListBox, currentID);
		
		//save the import spec data
		dataManager.saveSpreadsheetImportSpecData();
	}
	
	
	public void onClick$delIDSegBtn() {
		DerivedValue currentID = this.currentInstance.getDerivedExtID();
		this.deleteSegment(idSegmentsListBox, currentID);
		
		//save the import spec data
		dataManager.saveSpreadsheetImportSpecData();
	}
	
	
	
	public void onClick$addNameTxtBtn() {
		String fixedText = nameFixedTxtBox.getValue();
		if(fixedText.length() > 0) {
			DerivedValue currentName = this.currentInstance.getDerivedName();	
			this.addTextSegment(fixedText, nameSegmentsListBox, currentName);
			
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();
		}
	}
	
	
	public void onClick$addNameRefBtn() {
		DerivedValue currentName = this.currentInstance.getDerivedName();	
		this.addRefSegment(nameSegmentsListBox, currentName);
		
		//save the import spec data
		dataManager.saveSpreadsheetImportSpecData();
	}
	
	
	public void onClick$nameSegmentUpBtn() {
		DerivedValue currentName = this.currentInstance.getDerivedName();	
		this.moveSegmentUp(nameSegmentsListBox, currentName);
		
		//save the import spec data
		dataManager.saveSpreadsheetImportSpecData();
	}
	
	
	public void onClick$nameSegmentDownBtn() {
		DerivedValue currentName = this.currentInstance.getDerivedName();	
		this.moveSegmentDown(nameSegmentsListBox, currentName);
		
		//save the import spec data
		dataManager.saveSpreadsheetImportSpecData();
	}
	
	
	public void onClick$delNameSegBtn() {
		DerivedValue currentName = this.currentInstance.getDerivedName();	
		this.deleteSegment(nameSegmentsListBox, currentName);
		
		//save the import spec data
		dataManager.saveSpreadsheetImportSpecData();
	}
	
	
	
	
	
	
	
	private void updateDerivedValueList(Listbox aListbox, DerivedValue derivedValue) {
			DerivedValue thisDerivedValue;
			if(derivedValue == null) {
				thisDerivedValue = this.dataManager.newDerivedValue();
		//		this.currentSlot.setDerivedSlotValue(thisDerivedValue);
			} else {
				thisDerivedValue = derivedValue;
			}
			aListbox.setModel(new ListModelList(thisDerivedValue.getDerivedValueStringOrDerivedValueRef()));
			
			aListbox.setItemRenderer(new ListitemRenderer() {
			    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
			         
			    	if(data.getClass().getName() =="com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueRefScript") {
			    		final DerivedValueRef ref = (DerivedValueRef) data;
				        listItem.setValue(ref);
				        String refLabel = "REF: " + ref.getValue();
				        if(ref.getPreDelimiter() != null && ref.getPreDelimiter().length() > 0) {
				        	refLabel = refLabel + " [pre " + ref.getPreDelimiter() + "]";
				        }
				        if(ref.getPostDelimiter() != null && ref.getPostDelimiter().length() > 0) {
				        	refLabel = refLabel + " [post " + ref.getPostDelimiter() + "]";
				        }
				        Listcell newListCell = new Listcell(refLabel);
				        newListCell.setParent(listItem);
				        newListCell.setStyle("font-weight:bold;font-color:red;");
				        
			    	}
			    	
			    	if(data.getClass().getName() =="com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueStringScript") {
			    		final DerivedValueString fixedString = (DerivedValueString) data;
				        listItem.setValue(fixedString);
				        new Listcell(fixedString.getContent()).setParent(listItem);
			    	}
			        
			    }
			   }
			); 
			importspectree.setModel(importspectree.getModel());
	}
		
		
		public void onClick$setIDRefBtn() {
			try {
				String columnRef = this.idColumnTxtBox.getValue();
				
				if(columnRef != null && columnRef.length() > 0) {
				
					//Update the list of segments for the id
					int selectedIndex = idSegmentsListBox.getSelectedIndex();
					
					if(selectedIndex >= 0) {
						ListModelList listModel = (ListModelList) idSegmentsListBox.getListModel();
						Object segment = listModel.get(selectedIndex);
						if(segment.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueRefScript")) {
							DerivedValueRef valueRef = (DerivedValueRef) segment;
							valueRef.setValue(columnRef);
							
							//set the pre-delimiter if one is provided
							String idPreDelim = this.idPreDelimTxtBox.getValue();
							valueRef.setPreDelimiter(idPreDelim);

							//set the post-delimiter if one is provided
							String idPostDelim = this.idPostDelimTxtBox.getValue();
							valueRef.setPostDelimiter(idPostDelim);
							
							//save the import spec data
							dataManager.saveSpreadsheetImportSpecData();
							
							this.idSegmentsListBox.setModel(idSegmentsListBox.getModel());
							importspectree.setModel(importspectree.getModel());
						// this.segmentsListBox.setSelectedIndex(listModel.indexOf(derivedValRef));
						} else {
							Messagebox.show("A reference value must be selected in the list", "Set Reference Error", Messagebox.OK, Messagebox.ERROR);
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		public void onClick$setNameRefBtn() {
			try {
				String columnRef = this.nameColumnTxtBox.getValue();
				
				if(columnRef != null && columnRef.length() > 0) {
				
					//Update the list of segments for the instance name
					int selectedIndex = nameSegmentsListBox.getSelectedIndex();
					
					if(selectedIndex >= 0) {
						ListModelList listModel = (ListModelList) nameSegmentsListBox.getListModel();
						Object segment = listModel.get(selectedIndex);
						if(segment.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueRefScript")) {
							DerivedValueRef valueRef = (DerivedValueRef) segment;
							valueRef.setValue(columnRef);
							
							//set the pre-delimiter if one is provided
							String namePreDelim = this.namePreDelimTxtBox.getValue();
							valueRef.setPreDelimiter(namePreDelim);
							
							//set the post-delimiter if one is provided
							String namePostDelim = this.namePostDelimTxtBox.getValue();
							valueRef.setPostDelimiter(namePostDelim);
							
							//save the import spec data
							dataManager.saveSpreadsheetImportSpecData();
							
							this.nameSegmentsListBox.setModel(nameSegmentsListBox.getModel());
							importspectree.setModel(importspectree.getModel());
						// this.segmentsListBox.setSelectedIndex(listModel.indexOf(derivedValRef));
						} else {
							Messagebox.show("A reference value must be selected in the list", "Set Reference Error", Messagebox.OK, Messagebox.ERROR);
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		
		public void onClick$createNameTemplateBtn() {
			try {
				String className =  classNameTxtBox.getValue(); 
				if(className != null  && className.length() > 0) {
					DerivedValue valueTemplate = this.appDataManager.getValueTemplateForSlot(className, ImportSpecDataManager.NAME_SLOT_NAME);
					
					// JWC - 27.09.2016 Handle templates for Relation and Graph_Relation classes
					if(valueTemplate == null)
					{
						// Test for relation name based value template
						valueTemplate = this.appDataManager.getValueTemplateForSlot(className, ImportSpecDataManager.RELATION_NAME_SLOT_NAME);
						
						if (valueTemplate == null)
						{
							// If it's still null, check for graph_relation, :relation_name
							valueTemplate = this.appDataManager.getValueTemplateForSlot(className, ImportSpecDataManager.GRAPH_RELATION_NAME_SLOT_NAME);
						}
					}
					
					if(valueTemplate != null) {
						//notify that a template already exists for this slot
						Messagebox.show("A global template aready exists for name of this class", "Create Name Template Error", Messagebox.OK, Messagebox.ERROR);
					} else {
						//open the modal dialog to create a new template
						createNewNameValueTemplate(className);
					}
				} else {
					//notify that a slot name must be defined to create a template
					Messagebox.show("A class name must be defined to create a template", "Create Name Template Error", Messagebox.OK, Messagebox.ERROR);
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		
		public void onClick$createIDTemplateBtn() {
			try {
				String className =  classNameTxtBox.getValue(); 
				if(className != null  && className.length() > 0) {
					DerivedValue valueTemplate = this.appDataManager.getValueTemplateForSlot(className, ImportSpecDataManager.EXTID_SLOT_NAME);
					if(valueTemplate != null) {
						//notify that a template already exists for this slot
						Messagebox.show("A global template aready exists for the external ID of this class", "Create ID Template Error", Messagebox.OK, Messagebox.ERROR);
					} else {
						//open the modal dialog to create a new template
						createNewIDValueTemplate(className);
					}
				} else {
					//notify that a slot name must be defined to create a template
					Messagebox.show("A class name must be defined to create a template", "Create ID Template Error", Messagebox.OK, Messagebox.ERROR);
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		public void onAfterRender$nameSegmentsListBox() {
			//System.out.println("DerivedInstanceEditorComposer.onAfterRender$nameSegmentsListBox. 606");
			String valueString = derivedValuetoString(this.currentInstance.getDerivedName());
			//System.out.println("DerivedInstanceEditorComposer.onAfterRender$nameSegmentsListBox. 608. value: " + valueString);
			nameFormatLabel.setValue(valueString);
		}
		
		public void onAfterRender$idSegmentsListBox() {
			//System.out.println("DerivedInstanceEditorComposer.onAfterRender$idSegmentsListBox: 615");
			String valueString = derivedValuetoString(this.currentInstance.getDerivedExtID());
			idFormatLabel.setValue(valueString);
		}
		
		
		public void initIDValueSegmentList(String className) {
			//System.out.println("DerivedInstanceEditorComposer.initIDValueSegmentList: 622");
			//first retrieve the value segments for the instance id
			if(className != null) {
				final String idSlotName = ImportSpecDataManager.EXTID_SLOT_NAME;
				final DerivedValue valueTemplate = this.appDataManager.getValueTemplateForSlot(className, idSlotName);
				final String newClassName = className;
				
				if(valueTemplate != null) {
					DerivedValueScript newValue = dataManager.getDerivedValueFromTemplate(valueTemplate);
					this.currentInstance.setDerivedExtID(newValue);
					this.updateDerivedValueList(idSegmentsListBox, newValue);
					idColumnTxtBox.setValue("");
					importspectree.setModel(importspectree.getModel());
					initNameValueSegmentList(newClassName);
				} else {
					try {
						Messagebox.show(("Do you want to create a global instance id template for this class\"?" ), "Create ID Template?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
								new EventListener() {
									public void onEvent(Event evt) { 
											switch (((Integer)evt.getData()).intValue()) {
												case Messagebox.YES: 
													createNewIDValueTemplate(newClassName);
													initNameValueSegmentList(newClassName);
													break;
													
												case Messagebox.NO:
													if(currentInstance.getDerivedExtID() == null) {
														DerivedValueScript newValue = dataManager.newDerivedValue();
														currentInstance.setDerivedExtID(newValue);
														clearIDValueSegments();
													}
													initNameValueSegmentList(newClassName);
													break;
											}
									}
								}
						);
						
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			importspectree.setModel(importspectree.getModel());
		}
		
		
		
		public void initNameValueSegmentList(String className) {
			//System.out.println("DerivedInstanceEditorComposer.initNameValueSegmentList: 672");
			//first retrieve the value segments for the instance name
			if(className != null) {
				final String nameSlotName = ImportSpecDataManager.NAME_SLOT_NAME;
				final String relationSlotName = ImportSpecDataManager.RELATION_NAME_SLOT_NAME;
				final String graphRelationSlotName = ImportSpecDataManager.GRAPH_RELATION_NAME_SLOT_NAME;
				
				final String newClassName = className;
				DerivedValue valueTemplate = this.appDataManager.getValueTemplateForSlot(className, nameSlotName);
				
				// JWC 27.09.2016
				if(valueTemplate == null)
				{
					// try the relation name
					valueTemplate = this.appDataManager.getValueTemplateForSlot(className, relationSlotName);
					
					// if it's still NULL, try the graph relation name
					if(valueTemplate == null)
					{
						valueTemplate = this.appDataManager.getValueTemplateForSlot(className, graphRelationSlotName);
					}
				}
				
				// END - JWC 27.09.2016
				
				if(valueTemplate != null) {
					DerivedValueScript newValue = dataManager.getDerivedValueFromTemplate(valueTemplate);
					this.currentInstance.setDerivedName(newValue);
					this.updateDerivedValueList(nameSegmentsListBox, newValue);
					nameColumnTxtBox.setValue("");
					importspectree.setModel(importspectree.getModel());
				} else {
					try {
						Messagebox.show(("Do you want to create a global name template for this class\"?" ), "Create Name Template?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
								new EventListener() {
									public void onEvent(Event evt) { 
											switch (((Integer)evt.getData()).intValue()) {
												case Messagebox.YES: 
													createNewNameValueTemplate(newClassName);
													break;
													
												case Messagebox.NO:
													if(currentInstance.getDerivedName() == null) {
														DerivedValueScript newValue = dataManager.newDerivedValue();
														currentInstance.setDerivedName(newValue);
														clearNameValueSegments();
													}
													
													//save the import spec data
													dataManager.saveSpreadsheetImportSpecData();
													break;
											}
									}
								}
						);
						
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			importspectree.setModel(importspectree.getModel());
		}

		
		
		
		//method to clear the value segments for the derived name
		private void clearNameValueSegments() {
			List<String> blankStringList = new ArrayList<String>();
			nameSegmentsListBox.setModel(new ListModelList(blankStringList));
		//	nameFixedTxtBox.setValue("<NO TEMPLATE DEFINED>");
			nameFixedTxtBox.setValue("");
			nameFormatLabel.setValue("");
			importspectree.setModel(importspectree.getModel());
		}
		
		
		//method to clear the value segments for the derived id
		private void clearIDValueSegments() {
			List<String> blankStringList = new ArrayList<String>();
			idSegmentsListBox.setModel(new ListModelList(blankStringList));
		//	idFixedTxtBox.setValue("<NO TEMPLATE DEFINED>");
			idFixedTxtBox.setValue("");
			idFormatLabel.setValue("");
			importspectree.setModel(importspectree.getModel());
		}
		
		
		//Utility operation to create a flat String from a DerivedValue instance
		protected String derivedValuetoString(DerivedValue derivedVal) {
			String listString = "";
			if(derivedVal != null) {
		        Iterator segmentIter = derivedVal.getDerivedValueStringOrDerivedValueRef().iterator();
		        DerivedValueRef aRef;
		        DerivedValueString aString;
		        while(segmentIter.hasNext()) {
		        	Object segment = segmentIter.next();
		        	if(segment.getClass().getName() == "com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueRefScript") {
		        		aRef = (DerivedValueRef) segment;
		        		listString = listString + "[" + aRef.getValue() + "]";
		        	}
		        	if(segment.getClass().getName() == "com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueStringScript") {
		        		aString = (DerivedValueString) segment;
		        		listString = listString + aString.getContent();
		        	}
		        }
			}
	        return listString;
	  
		}
		
		
		private void createNewNameValueTemplate(String className) {
			String nameSlot;
			List<String> classSlotList = this.getSlotsForEssentialClass(className);
			if(classSlotList.contains(ImportSpecDataManager.NAME_SLOT_NAME)) {
				nameSlot = ImportSpecDataManager.NAME_SLOT_NAME;
			} else if(classSlotList.contains(ImportSpecDataManager.RELATION_NAME_SLOT_NAME)) {
				nameSlot = ImportSpecDataManager.RELATION_NAME_SLOT_NAME;
			} else if(classSlotList.contains(ImportSpecDataManager.GRAPH_RELATION_NAME_SLOT_NAME)) {
				nameSlot = ImportSpecDataManager.GRAPH_RELATION_NAME_SLOT_NAME;
			} else {
				return;
			}
			
			final String newSlotName = nameSlot;
			final String newClassName = className;
			desktop.getSession().setAttribute("currentClassName", newClassName);
			desktop.getSession().setAttribute("currentSlotName", newSlotName);
			
			final Window newValTempDialog = (Window) Executions.createComponents("/value_template_dialog.zul", null, null);
			newValTempDialog.addEventListener("onClose", new EventListener(){
	            public void onEvent(Event event) throws Exception {
	            	//set up the new derived value
	            	DerivedValue newValueTemplate = appDataManager.getValueTemplateForSlot(newClassName, newSlotName);
					if(newValueTemplate != null) {
						DerivedValueScript newValue = dataManager.getDerivedValueFromTemplate(newValueTemplate);
						currentInstance.setDerivedName(newValue);
						updateDerivedValueList(nameSegmentsListBox, newValue);
						nameColumnTxtBox.setValue("");
						
						//save the import spec data
						dataManager.saveSpreadsheetImportSpecData();
						
						importspectree.setModel(importspectree.getModel());
					} else {
						DerivedValueScript newValue = dataManager.newDerivedValue();
						currentInstance.setDerivedName(newValue);
						clearNameValueSegments();
						
						//save the import spec data
						dataManager.saveSpreadsheetImportSpecData();
					}
					newValTempDialog.removeEventListener("onClose", this);
	            }
	        });
			try {
				newValTempDialog.doModal();
			}
			catch(Exception anException) {
				anException.printStackTrace();
			}			
		}
		
		
		
		private void createNewIDValueTemplate(String className) {
			final String newSlotName = ImportSpecDataManager.EXTID_SLOT_NAME;
			final String newClassName = className;
			desktop.getSession().setAttribute("currentClassName", newClassName);
			desktop.getSession().setAttribute("currentSlotName", newSlotName);
			
			final Window newValTempDialog = (Window) Executions.createComponents("/value_template_dialog.zul", null, null);
			newValTempDialog.addEventListener("onClose", new EventListener(){
	            public void onEvent(Event event) throws Exception {
	            	//set up the new derived value
	            	DerivedValue newValueTemplate = appDataManager.getValueTemplateForSlot(newClassName, newSlotName);
					if(newValueTemplate != null) {
						DerivedValueScript newValue = dataManager.getDerivedValueFromTemplate(newValueTemplate);
						currentInstance.setDerivedExtID(newValue);
						updateDerivedValueList(idSegmentsListBox, newValue);
						idColumnTxtBox.setValue("");
						
						//save the import spec data
						dataManager.saveSpreadsheetImportSpecData();
						
						importspectree.setModel(importspectree.getModel());
					} else {
						DerivedValueScript newValue = dataManager.newDerivedValue();
						currentInstance.setDerivedExtID(newValue);
						clearIDValueSegments();
						
						//save the import spec data
						dataManager.saveSpreadsheetImportSpecData();
					}
					newValTempDialog.removeEventListener("onClose", this);
	            }
	        });
			try {
				newValTempDialog.doModal();
			}
			catch(Exception anException) {
				anException.printStackTrace();
			}			
		}
		
		
		
		private List<String> getClassNameList() { //return all items
			if(classNameList == null) {
				this.classNameList = this.getEssentialClasses();
			}
			return this.classNameList;
		}
		
		
		
		private DerivedValueRef createValueRef() {
			DerivedValueRef newRef;
			newRef = dataManager.newDerivedValueRef();
			newRef.setValue(ImportSpecDataManager.UNDEFINED_VALUE);
			return newRef;
		}
		
		
		private DerivedValueString createValueString(String text) {
			DerivedValueString textString;
			textString = dataManager.newDerivedValueString();
			textString.setContent(text);
			return textString;
		}
		
		
		
		private void addTextSegment(String fixedText, Listbox targetListBox, DerivedValue targetValue) {
			try {
				if(fixedText.length() > 0) {
					DerivedValueString derivedValString = this.createValueString(fixedText);
					
					//Update the list of segments for the template
					ListModelList listModel = (ListModelList) targetListBox.getListModel();
					listModel.add(derivedValString);
					if(targetValue != null) {
						targetValue.getDerivedValueStringOrDerivedValueRef().add(derivedValString);
						targetListBox.setSelectedIndex(listModel.indexOf(derivedValString));
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		private void addRefSegment(Listbox targetListBox, DerivedValue targetValue) {
			try {
				DerivedValueRef derivedValRef = this.createValueRef();
				
				//Update the list of segments for the template
				ListModelList listModel = (ListModelList) targetListBox.getListModel();
				listModel.add(derivedValRef);
				if(targetValue != null) {
					targetValue.getDerivedValueStringOrDerivedValueRef().add(derivedValRef);
					targetListBox.setSelectedIndex(listModel.indexOf(derivedValRef));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		private void moveSegmentUp(Listbox targetListBox, DerivedValue targetValue) {
			try {
				//Get the listmodel for the segments list box
				ListModelList listModel = (ListModelList) targetListBox.getListModel();
				
				//Get the index of the currently selected item
				int selectedIndex = targetListBox.getSelectedIndex();
				
				//Provided the currently selected item is not the first, move it up in the list
				if (selectedIndex > 0) {
					Object selectedItem = listModel.get(selectedIndex);
					
					//change the order in the Derived Value and the list box
					if(targetValue != null) {
						targetValue.getDerivedValueStringOrDerivedValueRef().remove(selectedIndex);
						listModel.remove(selectedIndex);
						
						if(selectedItem.getClass().getName().endsWith("DerivedValueRefScript")) {
							targetValue.getDerivedValueStringOrDerivedValueRef().add(selectedIndex - 1, (DerivedValueRef) selectedItem);
							listModel.add(selectedIndex - 1, (DerivedValueRef) selectedItem);
						} else {
							targetValue.getDerivedValueStringOrDerivedValueRef().add(selectedIndex - 1, (DerivedValueString) selectedItem);
							listModel.add(selectedIndex - 1, (DerivedValueString) selectedItem);
						}
						
						//set the currently selected item
						targetListBox.setSelectedIndex(selectedIndex -1);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		public void moveSegmentDown(Listbox targetListBox, DerivedValue targetValue) {
			try {
				//Get the listmodel for the segments list box
				ListModelList listModel = (ListModelList) targetListBox.getListModel();
				
				//Get the index of the currently selected item
				int selectedIndex = targetListBox.getSelectedIndex();
				int listSize = listModel.getSize();
				
				//Provided the currently selected item is not the last, move it down in the list
				if ((listSize > 1) && (selectedIndex < listSize - 1)) {
					Object selectedItem = listModel.get(selectedIndex);
					
					//change the order in the Derived Value and the list box
					if(targetValue != null) {
						targetValue.getDerivedValueStringOrDerivedValueRef().remove(selectedIndex);
						listModel.remove(selectedIndex);
						
						if(selectedItem.getClass().getName().endsWith("DerivedValueRefScript")) {
							targetValue.getDerivedValueStringOrDerivedValueRef().add(selectedIndex + 1, (DerivedValueRef) selectedItem);
							listModel.add(selectedIndex + 1, (DerivedValueRef) selectedItem);
						} else {
							targetValue.getDerivedValueStringOrDerivedValueRef().add(selectedIndex + 1, (DerivedValueString) selectedItem);
							listModel.add(selectedIndex + 1, (DerivedValueString) selectedItem);
						}
						
						//set the currently selected item
						targetListBox.setSelectedIndex(selectedIndex + 1);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		public void deleteSegment(Listbox targetListBox, DerivedValue targetValue) {
			try {
				//delete the currently selected segment
				int selectedIndex = targetListBox.getSelectedIndex();
				if(selectedIndex >= 0) {
					ListModelList listModel = (ListModelList) targetListBox.getListModel();
					Object currentSelection = listModel.getElementAt(selectedIndex);
					if(targetValue != null) {
						targetValue.getDerivedValueStringOrDerivedValueRef().remove(selectedIndex);
						listModel.remove(currentSelection);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
	
	
	
	
}
