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
//import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedSimpleSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueScript;
//import com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript;
//import com.enterprise_architecture.essential.importutility.data.common.script.SimpleSlotScript;
//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.GlobalValueTemplate;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
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
public class DerivedSimpleSlotEditorComposer extends EssentialImportInterface {

	private DerivedSimpleSlotScript currentSlot;
	private String currentInstanceClass;
	private ImportSpecDataManager dataManager;
	private ImportUtilityDataManager appDataManager;
	private Tree importspectree;
	private int oldSelectedSlotNameIndex = 0;
	
	public Div derivedSimpleSlotDiv;
	public Listbox slotNameListBox;
	public Textbox valueFixedTxtBox;
	public Textbox valueColumnTxtBox;
	public Textbox valuePreDelimTxtBox;
	public Textbox valuePostDelimTxtBox;
	public Listbox segmentsListBox;
	public Label valueFormatLabel;
	
	
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		IdSpace window = derivedSimpleSlotDiv.getSpaceOwner();
        final Borderlayout borderLayout = (Borderlayout) window.getFellow("editExcelBorderLayout");
        
        final West westpanel = borderLayout.getWest();
        this.importspectree = (Tree) westpanel.getFellow("importspectree");
        
		//get the current Derived Simple Slot
        Object currentData = desktop.getSession().getAttribute("currentTreeData");
        if(currentData == null)
        {
        	// Catch all in case the currentData is null
        	final TreeNode currentTreeNode = (TreeNode) desktop.getSession().getAttribute("currentTreeNode");
        	currentData = currentTreeNode.getData();
        }
        if(!currentData.getClass().getName().endsWith("DerivedSimpleSlotScript")) {
        	importspectree.setModel(importspectree.getModel());
        	return;
        }
		this.currentSlot = (DerivedSimpleSlotScript) currentData;
		
		//get the class name of the current parent Instance
		this.currentInstanceClass = currentSlot.getParentClassName();
		
		//get the data manager for import specs
		dataManager = this.getImportSpecDataManager();
		
		//get the application data manager
		appDataManager = this.getImportUtilityDataManager();
		
		
		//set up the list of slot names
		List<String> classSlotList = this.getSlotNames(this.currentInstanceClass);
		List<String> completeSlotList = new ArrayList<String>();
		completeSlotList.add("- Select Simple Slot -");
		completeSlotList.addAll(classSlotList);

		slotNameListBox.setModel(new ListModelList(completeSlotList)); 
		
		if(this.currentSlot != null) {
			//given an existing Derived Simple Slot, set the values of the page's widgets
			
			//set the current slot name
			String slotName = this.currentSlot.getSlotName();
			if(slotName != null && slotName.length() > 0) {
				int slotNameIndex = completeSlotList.indexOf(slotName);
				if(slotNameIndex >= 0) {
					slotNameListBox.setSelectedIndex(slotNameIndex);
					oldSelectedSlotNameIndex = slotNameIndex;
				}
			}
			
		/*	String slotName = this.currentSlot.getSlotName();
			if(slotName != null && slotName.length() > 0) {
				int slotNameIndex = this.getSlotNames(this.currentInstanceClass).indexOf(slotName);
				if(slotNameIndex >= 0) {
					slotNameListBox.setSelectedIndex(slotNameIndex);
					oldSelectedSlotNameIndex = slotNameIndex;
				}
			} */

			
			//set the slot value text box
			DerivedValueScript value = (DerivedValueScript) currentSlot.getDerivedSlotValue();
			this.setValueTemplateWidgets(value);
			
			
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
					
					this.initValueSegmentList(slotName);
					
					//save the import spec data
					dataManager.saveSpreadsheetImportSpecData();
					 
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
	
	
	
	
	private void setValueTemplateWidgets(DerivedValue derivedValue) {
			DerivedValue thisDerivedValue;
			if(derivedValue == null) {
				thisDerivedValue = this.dataManager.newDerivedValue();
				this.currentSlot.setDerivedSlotValue(thisDerivedValue);
			} else {
				thisDerivedValue = derivedValue;
			}
			segmentsListBox.setModel(new ListModelList(thisDerivedValue.getDerivedValueStringOrDerivedValueRef()));
			
			segmentsListBox.setItemRenderer(new ListitemRenderer() {
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
		
		
		public void onClick$valueRefBtn() {
			try {
				String columnRef = this.valueColumnTxtBox.getValue();
				
				if(columnRef != null && columnRef.length() > 0) {
				
					//Update the list of segments for the template
					int selectedIndex = segmentsListBox.getSelectedIndex();
					
					if(selectedIndex >= 0) {
						ListModelList listModel = (ListModelList) segmentsListBox.getListModel();
						Object segment = listModel.get(selectedIndex);
						if(segment.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueRefScript")) {
							DerivedValueRef valueRef = (DerivedValueRef) segment;
							valueRef.setValue(columnRef);
							
							//set the pre-delimiter if one is provided
							String valuePreDelim = this.valuePreDelimTxtBox.getValue();
							valueRef.setPreDelimiter(valuePreDelim);
							
							//set the post-delimiter if one is provided
							String valuePostDelim = this.valuePostDelimTxtBox.getValue();
							valueRef.setPostDelimiter(valuePostDelim);
							
							//save the import spec data
							dataManager.saveSpreadsheetImportSpecData();
							
							this.segmentsListBox.setModel(segmentsListBox.getModel());
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
		
		
		
		public void onClick$createTemplateBtn() {
			try {
				int slotNameIndex = slotNameListBox.getSelectedIndex();
				if(slotNameIndex > 0) {
					List<String> slotList = this.getSlotNames(this.currentInstanceClass);
					if(slotList != null) {
						String newSlotName = slotList.get(slotNameIndex);
						
						DerivedValue valueTemplate = this.appDataManager.getValueTemplateForSlot(currentInstanceClass, newSlotName);
						if(valueTemplate != null) {
							//notify that a template already exists for this slot
							Messagebox.show("A global template aready exists for this slot", "Create Value Template Error", Messagebox.OK, Messagebox.ERROR);
						} else {
							//open the modal dialog to create a new template
							createNewValueTemplate(newSlotName);
						}
					} else {
						//notify that a slot name must be defined to create a template
						Messagebox.show("A slot name must be selected to create a global template", "Slot Value Template Error", Messagebox.OK, Messagebox.ERROR);
					}
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		
		public void onAfterRender$segmentsListBox() {
			String valueString = derivedValuetoString(this.currentSlot.getDerivedSlotValue());
			valueFormatLabel.setValue(valueString);
		}
		
		
		
		public void onClick$addValueTxtBtn() {
			String fixedText = valueFixedTxtBox.getValue();
			if(fixedText.length() > 0) {
				DerivedValue currentValue = this.currentSlot.getDerivedSlotValue();	
				this.addTextSegment(fixedText, segmentsListBox, currentValue);
				
				//save the import spec data
				dataManager.saveSpreadsheetImportSpecData();
			}
		}
		
		
		public void onClick$addValueRefBtn() {
				DerivedValue currentValue = this.currentSlot.getDerivedSlotValue();
				this.addRefSegment(segmentsListBox, currentValue);
				
				//save the import spec data
				dataManager.saveSpreadsheetImportSpecData();
		}
		
		
		public void onClick$valueSegmentUpBtn() {
			DerivedValue currentValue = this.currentSlot.getDerivedSlotValue();
			this.moveSegmentUp(segmentsListBox, currentValue);
			
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();
		}
		
		
		public void onClick$valueSegmentDownBtn() {
			DerivedValue currentValue = this.currentSlot.getDerivedSlotValue();
			this.moveSegmentDown(segmentsListBox, currentValue);
			
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();
		}
		
		
		public void onClick$delValueSegBtn() {
			DerivedValue currentValue = this.currentSlot.getDerivedSlotValue();
			this.deleteSegment(segmentsListBox, currentValue);
			
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();
		}
		
		
		
		public void initValueSegmentList(String slotName) {
			System.out.println("Initialising Segment Widgets: " + currentInstanceClass);
			if(slotName != null && this.currentInstanceClass != null) {
				final DerivedValue valueTemplate = this.appDataManager.getValueTemplateForSlot(currentInstanceClass, slotName);
				final String newSlotName = slotName;
				if(valueTemplate != null) {
					DerivedValueScript newValue = dataManager.getDerivedValueFromTemplate(valueTemplate);
					this.currentSlot.setDerivedSlotValue(newValue);
					this.setValueTemplateWidgets(newValue);
					valueColumnTxtBox.setValue("");
					
					//save the import spec data
					dataManager.saveSpreadsheetImportSpecData();
					
					importspectree.setModel(importspectree.getModel());
				} else {
					try {
						Messagebox.show(("Do you want to create a global template for the slot, \"" + newSlotName + "\"?" ), "Create Global Value Template?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
								new EventListener() {
									public void onEvent(Event evt) { 
											switch (((Integer)evt.getData()).intValue()) {
												case Messagebox.YES: 
													createNewValueTemplate(newSlotName);
													break;
													
												case Messagebox.NO:
													if(currentSlot.getDerivedSlotValue() == null) {
														DerivedValueScript newValue = dataManager.newDerivedValue();
														currentSlot.setDerivedSlotValue(newValue);
														clearValueSegments();
													}
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
		
		
		//method to clear the value segments for the slot
		private void clearValueSegments() {
			List<String> blankStringList = new ArrayList<String>();
			segmentsListBox.setModel(new ListModelList(blankStringList));
		//	valueColumnTxtBox.setValue("<NO TEMPLATE DEFINED>");
			valueColumnTxtBox.setValue("");
			valueFormatLabel.setValue("");
			
			//save the import spec data
			dataManager.saveSpreadsheetImportSpecData();
			
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
		
		
		private void createNewValueTemplate(String slotName) {
			desktop.getSession().setAttribute("currentClassName", currentInstanceClass);
			desktop.getSession().setAttribute("currentSlotName", slotName);
			final String newSlotName = slotName;
			final Window newValTempDialog = (Window) Executions.createComponents("/value_template_dialog.zul", null, null);
			newValTempDialog.addEventListener("onClose", new EventListener(){
	            public void onEvent(Event event) throws Exception {
	            	//set up the new derived value
	            //	System.out.println("NEW VALUE ADDED");
	            	DerivedValue newValueTemplate = appDataManager.getValueTemplateForSlot(currentInstanceClass, newSlotName);
					if(newValueTemplate != null) {
						DerivedValueScript newValue = dataManager.getDerivedValueFromTemplate(newValueTemplate);
						currentSlot.setDerivedSlotValue(newValue);
						setValueTemplateWidgets(newValue);
						valueColumnTxtBox.setValue("");
						
						//save the import spec data
						dataManager.saveSpreadsheetImportSpecData();
						
						importspectree.setModel(importspectree.getModel());
					} else {
					//	System.out.println("CLEARING THE SLOT VALUE");
						DerivedValueScript newValue = dataManager.newDerivedValue();
						currentSlot.setDerivedSlotValue(newValue);
						clearValueSegments();
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
		
		
	/*	
		private void initSlotList(String className) {
			List<String> slotList = this.getSlotNames(className);
			
			if(slotList == null) {
				slotList = new ArrayList<String>();
			} 
			slotNameListBox.setModel(new ListModelList(slotList)); 
		}
		
		*/
		
		
		
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
