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

import java.util.ArrayList;
//import java.util.Iterator;
import java.util.List;

//import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.GlobalValueTemplate;
//import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * common templates for values that are re-used across Essential imports,
 * e.g. a common naming convention for instances of a given relationship class
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class GlobalValueTemplatesComposer extends EssentialImportInterface {
	
	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private int currentMode;
	private int currentValTempIndex;
	private GlobalValueTemplate currentValTemp;
	private DerivedValue currentDerivedValue;
	private ImportUtilityDataManager dataManager;
	private List<String> classNameList;
	private String currentClassName;
	
	public Button okBtn;
	public Button cancelBtn;
	public Combobox classNameTxtBox;
	public Listbox slotNameListBox;
	public Textbox fixedTxtBox;
	public Listbox segmentsListBox;
	public Label templateLabel;
	
	
	public Window valueTemplateWindow;
	
	private Listbox valueTemplateList;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
//		Retrieve the object that manages the application data
		this.dataManager = this.getImportUtilityDataManager();
		
		this.valueTemplateList = (Listbox) Path.getComponent("//appHome/appHomeWin/valueTemplateListBox");		
		classNameTxtBox.setModel(ListModels.toListSubModel(new ListModelList(getClassNameList()))); 
		
		
		GlobalValueTemplate valTemp = (GlobalValueTemplate) desktop.getAttribute("currentValueTemp");
		if(valTemp != null) {
			//given a Value Template, set the current mode for the composer to EDIT
			this.currentMode = GlobalValueTemplatesComposer.EDIT_MODE;
			valueTemplateWindow.setTitle("Edit Global Value Template");
			
			this.currentValTempIndex = valueTemplateList.getSelectedIndex();
			this.currentValTemp = (GlobalValueTemplate) valueTemplateList.getListModel().getElementAt(currentValTempIndex);
			String className = this.currentValTemp.getGlobalValueTemplateClass();
			this.classNameTxtBox.setValue(className);
			this.currentClassName = className;
			if(className != null && className.length() > 0) {
				this.updateSlotList(className);
			}

			//set the current slot name
			String slotName = this.currentValTemp.getGlobalValueTemplateSlot();
			if(slotName != null && slotName.length() > 0) {
				int slotNameIndex = this.getSlotNames(className).indexOf(slotName);
				if(slotNameIndex >= 0) {
					slotNameListBox.setSelectedIndex(slotNameIndex);
				}
			}
		//	this.slotNameListBox.setValue(this.currentValTemp.getGlobalValueTemplateSlot());
			
			// this.currentDerivedValue = currentValTemp.getGlobalValueTemplateValue();
			this.currentDerivedValue = dataManager.copyDerivedValue(valTemp.getGlobalValueTemplateValue());
			this.setValueTemplateWidgets(this.currentDerivedValue);
			
			desktop.removeAttribute("currentValueTemp");
		} else {
			//in the absence of a Global Value Template, set the current mode for the composer to CREATE
			this.currentMode = GlobalValueTemplatesComposer.CREATE_MODE;
			valueTemplateWindow.setTitle("Create Global Value Template");
			String aClassName = (String) desktop.getSession().getAttribute("currentClassName");
			if(aClassName != null) {
				this.classNameTxtBox.setValue(aClassName);
				currentClassName = aClassName;
				desktop.getSession().removeAttribute("currentClassName");
				this.updateSlotList(aClassName);
				
				String aSlotName = (String) desktop.getSession().getAttribute("currentSlotName");
				if(aSlotName != null) {
					int slotNameIndex = this.getSlotNames(aClassName).indexOf(aSlotName);
					if(slotNameIndex >= 0) {
						slotNameListBox.setSelectedIndex(slotNameIndex);
					}
					desktop.getSession().removeAttribute("currentSlotName");
				}
			} else {
				currentClassName = "";
			}
			
			
			this.currentDerivedValue = dataManager.newDerivedValue();
			this.setValueTemplateWidgets(this.currentDerivedValue);
		}

	}
	
	
	
	public void onClick$okBtn() {
		try {
			
			if(segmentsListBox.getModel().getSize() == 0) {
				//there are no value segments defined
				String errorMessage = "A value template must be defined";
				String errorTitle = "Undefined Value Template";
				this.displayError(errorMessage, errorTitle);
				return;
			}
			
			//if all input constraints are met, update or create the Global Value Template
			if (this.classNameList.contains(this.currentClassName)) {
				
				
				//If the dialog is in EDIT MODE, update the details of the current Global Value Template
				if(this.currentMode == GlobalValueTemplatesComposer.EDIT_MODE) {
					//set the slot name for the value template
					if(!this.setValueTemplateSlot(this.currentValTemp)) {
						return;
					}
					
					
					//set the class name for the value template
					this.currentValTemp.setGlobalValueTemplateClass(classNameTxtBox.getValue());

					
					this.currentValTemp.setGlobalValueTemplateValue(this.currentDerivedValue);
					
					if(valueTemplateList != null) {
						ListModelList listModel = (ListModelList) valueTemplateList.getListModel();
						listModel.remove(this.currentValTempIndex);
						listModel.add(this.currentValTempIndex, this.currentValTemp);
						this.valueTemplateList.setSelectedIndex(this.currentValTempIndex);
					}
				}
				
				
				//If the dialog is in CREATE MODE, add the new Global Value Template to the config data
				if(this.currentMode == GlobalValueTemplatesComposer.CREATE_MODE) {
					GlobalValueTemplate valTemp = dataManager.newGlobalValueTemplate();
					valTemp.setGlobalValueTemplateClass(classNameTxtBox.getValue());
					
					//set the slot name for the value template
					if(!this.setValueTemplateSlot(valTemp)) {
						//there are no slots defined for the provided class 
						String errorMessage = "A slot must be selected";
						String errorTitle = "Invalid Slot";
						this.displayError(errorMessage, errorTitle);
						return;
					}
					
					valTemp.setGlobalValueTemplateValue(this.currentDerivedValue);
					
					if(valueTemplateList != null) {
						ListModelList listModel = (ListModelList) valueTemplateList.getListModel();
						listModel.add(valTemp);
						this.valueTemplateList.setSelectedIndex(listModel.indexOf(valTemp));
					}
					
					EssentialImportUtility configData = dataManager.getImportUtilityData();
					dataManager.addGlobalValueTemplate(valTemp);
					
				}
				
				//save the config data and close the dialog
				dataManager.saveAppData();
				Events.postEvent("onClose", valueTemplateWindow, null);
				valueTemplateWindow.detach();
			} else {
				//there are no value segments defined
				String errorMessage = "A valid class name must be provided";
				String errorTitle = "Invalid Class Name";
				this.displayError(errorMessage, errorTitle);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	private boolean setValueTemplateSlot(GlobalValueTemplate aValTemplate) {
		int slotNameIndex = slotNameListBox.getSelectedIndex();
		if(slotNameIndex > 0) {
			List<String> slotList = this.getSlotNames(currentClassName);
			if(slotList != null) {
				String slotName = slotList.get(slotNameIndex);
				aValTemplate.setGlobalValueTemplateSlot(slotName);
			}
			else {
				//there are no slots defined for the provided class 
				String errorMessage = "The defined class is not a valid Essential class";
				String errorTitle = "Invalid Class Name";
				this.displayError(errorMessage, errorTitle);
				return false;
			}
		}
		else {
			//a slot has not been selected
			return false;
		}		
		return true;
	}
	
	
	public void onClick$addTxtBtn() {
		try {
			String fixedText = fixedTxtBox.getValue();
			if(fixedText.length() > 0) {
				DerivedValueString derivedValString = this.createValueString(fixedText);
				
				//Update the list of segments for the template
				ListModelList listModel = (ListModelList) segmentsListBox.getListModel();
				listModel.add(derivedValString);
				this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().add(derivedValString);
				this.segmentsListBox.setSelectedIndex(listModel.indexOf(derivedValString));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$addRefBtn() {
		try {
			DerivedValueRef derivedValRef = this.createValueRef();
			
			//Update the list of segments for the template
			ListModelList listModel = (ListModelList) segmentsListBox.getListModel();
			listModel.add(derivedValRef);
			this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().add(derivedValRef);
			this.segmentsListBox.setSelectedIndex(listModel.indexOf(derivedValRef));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$segmentUpBtn() {
		try {
			//Get the listmodel for the segments list box
			ListModelList listModel = (ListModelList) segmentsListBox.getListModel();
			
			//Get the index of the currently selected item
			int selectedIndex = segmentsListBox.getSelectedIndex();
			
			//Provided the currently selected item is not the first, move it up in the list
			if (selectedIndex > 0) {
				Object selectedItem = listModel.get(selectedIndex);
				
				//change the order in the Derived Value and the list box
				this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().remove(selectedIndex);
				listModel.remove(selectedIndex);
				
				if(selectedItem.getClass().getName() =="com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef") {
					this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().add(selectedIndex - 1, (DerivedValueRef) selectedItem);
					listModel.add(selectedIndex - 1, (DerivedValueRef) selectedItem);
				} else {
					this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().add(selectedIndex - 1, (DerivedValueString) selectedItem);
					listModel.add(selectedIndex - 1, (DerivedValueString) selectedItem);
				}
				
				//set the currently selected item
				this.segmentsListBox.setSelectedIndex(selectedIndex -1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$segmentDownBtn() {
		try {
			//Get the listmodel for the segments list box
			ListModelList listModel = (ListModelList) segmentsListBox.getListModel();
			
			//Get the index of the currently selected item
			int selectedIndex = segmentsListBox.getSelectedIndex();
			int listSize = listModel.getSize();
			
			//Provided the currently selected item is not the last, move it down in the list
			if ((listSize > 1) && (selectedIndex < listSize - 1)) {
				Object selectedItem = listModel.get(selectedIndex);
				
				//change the order in the Derived Value and the list box
				this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().remove(selectedIndex);
				listModel.remove(selectedIndex);
				
				if(selectedItem.getClass().getName() =="com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef") {
					this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().add(selectedIndex + 1, (DerivedValueRef) selectedItem);
					listModel.add(selectedIndex + 1, (DerivedValueRef) selectedItem);
				} else {
					this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().add(selectedIndex + 1, (DerivedValueString) selectedItem);
					listModel.add(selectedIndex + 1, (DerivedValueString) selectedItem);
				}
				
				//set the currently selected item
				this.segmentsListBox.setSelectedIndex(selectedIndex + 1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void onClick$delSegBtn() {
		try {
			//delete the currently selected segment
			int selectedIndex = segmentsListBox.getSelectedIndex();
			if(selectedIndex >= 0) {
				ListModelList listModel = (ListModelList) segmentsListBox.getListModel();
				Object currentSelection = listModel.getElementAt(selectedIndex);
				this.currentDerivedValue.getDerivedValueStringOrDerivedValueRef().remove(selectedIndex);
				listModel.remove(currentSelection);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	public void onClick$cancelBtn() {
		try {
			Events.postEvent("onClose", valueTemplateWindow, null);
			valueTemplateWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onAfterRender$segmentsListBox() {
		templateLabel.setValue(derivedValuetoString(this.currentDerivedValue));
	}
	
	
	
	private void setValueTemplateWidgets(DerivedValue valueTemplate) {
		
		segmentsListBox.setModel(new ListModelList(valueTemplate.getDerivedValueStringOrDerivedValueRef()));
		
		segmentsListBox.setItemRenderer(new ListitemRenderer() {
		    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
		         
		    	if(data.getClass().getName() =="com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef") {
		    		final DerivedValueRef ref = (DerivedValueRef) data;
			        listItem.setValue(ref);
			        new Listcell(ref.getValue()).setParent(listItem);
		    	}
		    	
		    	if(data.getClass().getName() =="com.enterprise_architecture.essential.importutility.data.common.DerivedValueString") {
		    		final DerivedValueString fixedString = (DerivedValueString) data;
			        listItem.setValue(fixedString);
			        new Listcell(fixedString.getContent()).setParent(listItem);
		    	}
		        
		    }
		   }
		); 
	}
	
	
	public void onChange$classNameTxtBox() {
		String className = classNameTxtBox.getValue();
		if(this.classNameList.contains(className)) {
			this.currentClassName = className;
			this.updateSlotList(currentClassName);
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
	
	private void updateSlotList(String className) {
		List<String> slotList = this.getSlotNames(className);
		
		if(slotList == null) {
			slotList = new ArrayList<String>();
		} 
		
		slotList.add(0, "- Select Slot -");
		slotNameListBox.setModel(new ListModelList(slotList)); 
	}
	
	
	private List<String> getClassNameList() { //return all items
		if(classNameList == null) {
			this.classNameList = this.getEssentialClasses();
		}
		return this.classNameList;
	}
	
	
	private List<String> getSlotNames(String className) { //return all items
		if(className.length() > 0) {
			List<String> slotList = this.getSlotsForEssentialClass(className);
			//System.out.println("Getting Slot List for: " + className);
			return slotList;
		}
		return null;
	}
	
	
	private DerivedValueRef createValueRef() {
		DerivedValueRef newRef;
		newRef = dataManager.newDerivedValueRef();
		newRef.setValue("[REF]");
		return newRef;
	}
	
	
	private DerivedValueString createValueString(String text) {
		DerivedValueString textString;
		textString = dataManager.newDerivedValueString();
		textString.setContent(text);
		return textString;
	}

}
