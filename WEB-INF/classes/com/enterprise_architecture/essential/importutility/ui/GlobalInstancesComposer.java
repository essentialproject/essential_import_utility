/**
 * Copyright (c)2009-2014 Enterprise Architecture Solutions Ltd.
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
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * instances that are re-used across Essential imports,
 * e.g. specific taxonomy terms
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class GlobalInstancesComposer extends EssentialImportInterface {

	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private int currentMode;
	private int currentInstanceIndex;
	private DerivedInstanceType currentInstance;
	private ImportUtilityDataManager dataManager;
	
	public Textbox varNameTxtBox;
	public Combobox classNameTxtBox;
	public Textbox instanceNameTxtBox;
	
	public Window globalInstanceWindow;
	
	private Listbox globalInstList;
	private List<String> classNameList;
	private String currentClassName;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		this.globalInstList = (Listbox) Path.getComponent("//appHome/appHomeWin/globalInstListBox");		
		classNameTxtBox.setModel(ListModels.toListSubModel(new ListModelList(this.getClassNameList()))); 
		
		DerivedInstanceType globInstance = (DerivedInstanceType) desktop.getAttribute("currentGlobalInstance");
		if(globInstance != null) {
			//given a Global Instance, set the current mode for the composer to EDIT
			this.currentMode = GlobalInstancesComposer.EDIT_MODE;
			globalInstanceWindow.setTitle("Edit Global Instance");
			
			this.currentInstanceIndex = globalInstList.getSelectedIndex();
			this.currentInstance = (DerivedInstanceType) globalInstList.getListModel().getElementAt(currentInstanceIndex);
			this.varNameTxtBox.setValue(this.currentInstance.getVariableName());
			this.currentClassName = this.currentInstance.getClassName();
			this.classNameTxtBox.setValue(currentClassName);
			
			//Retrieve the derived name for the instance (this will always be a single fixed text string
			DerivedValue instanceName = this.currentInstance.getDerivedName();
			if (instanceName.getDerivedValueStringOrDerivedValueRef().size() > 0) {
				DerivedValueString instanceNameString = (DerivedValueString) instanceName.getDerivedValueStringOrDerivedValueRef().get(0);
				this.instanceNameTxtBox.setValue(instanceNameString.getContent());
			}
			
			desktop.removeAttribute("currentGlobalInstance");
		} else {
			//in the absence of a Global Instance, set the current mode for the composer to CREATE
			this.currentMode = GlobalInstancesComposer.CREATE_MODE;
			currentClassName = "";
			globalInstanceWindow.setTitle("Create Global Instance");

		}

	}
	
	
	
	public void onClick$okBtn() {
		
		if(!this.getClassNameList().contains(this.currentClassName)) {
			//there are no value segments defined
			String errorMessage = "A valid class name must be provided";
			String errorTitle = "Invalid Class Name";
			this.displayError(errorMessage, errorTitle);
			return;
		}
		
		try {
		//	Retrieve the object that manages the application data
			ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
			
			//if all input constraints are met, update or create the Target Environment
			if (varNameTxtBox.isValid() && classNameTxtBox.isValid() && instanceNameTxtBox.isValid()) {
				
				ListModelList listModel = (ListModelList) globalInstList.getListModel();
				
				//If the dialog is in EDIT MODE, update the details of the current Target Environment
				if(this.currentMode == GlobalInstancesComposer.EDIT_MODE) {
							
					//set the variable name of the Global Instance
					this.currentInstance.setVariableName(varNameTxtBox.getValue());
					
					//set the Essential class name of the Global Instance
					this.currentInstance.setClassName(classNameTxtBox.getValue());
					
					//set the Essential instance name for the Global Instance
					DerivedValueString valueString = dataManager.newDerivedValueString();
					valueString.setContent(instanceNameTxtBox.getValue());
					DerivedValue nameValue = dataManager.newDerivedValue();
					nameValue.getDerivedValueStringOrDerivedValueRef().add(valueString);
					this.currentInstance.setDerivedName(nameValue);
					
					//Update the list of Global Instances
					listModel.remove(this.currentInstanceIndex);
					listModel.add(this.currentInstanceIndex, this.currentInstance);
					this.globalInstList.setSelectedIndex(this.currentInstanceIndex);
				}
				
				
				//If the dialog is in CREATE MODE, add the new Global Instance to the config data
				if(this.currentMode == GlobalInstancesComposer.CREATE_MODE) {
					//Create a new Global Instance of DerivedInstanceType 
					DerivedInstanceType derivedInstance = dataManager.newDerivedInstanceType();
					
					//Set the variable name for the Global Instance
					derivedInstance.setVariableName(varNameTxtBox.getValue());
					
					//Set the Essential class name for the new Global Instance
					derivedInstance.setClassName(classNameTxtBox.getValue());
					
					//set the Essential instance name for the new Global Instance
					DerivedValueString valueString = dataManager.newDerivedValueString();
					valueString.setContent(instanceNameTxtBox.getValue()); 
					DerivedValue nameValue = dataManager.newDerivedValue();
					nameValue.getDerivedValueStringOrDerivedValueRef().add(valueString);
					derivedInstance.setDerivedName(nameValue); 
									
					//set the external id for the new Global Instance
					valueString = dataManager.newDerivedValueString();
					valueString.setContent(instanceNameTxtBox.getValue()); 
					DerivedValue idValue = dataManager.newDerivedValue();
					idValue.getDerivedValueStringOrDerivedValueRef().add(valueString);
					derivedInstance.setDerivedExtID(idValue); 
				
					listModel.add(derivedInstance);
					this.globalInstList.setSelectedIndex(listModel.indexOf(derivedInstance));
					
					EssentialImportUtility configData = dataManager.getImportUtilityData();
					configData.getGlobalInstances().add(derivedInstance);
					
				}
				
				//save the config data and close the dialog
				dataManager.saveAppData();
				globalInstanceWindow.detach();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$cancelBtn() {
		try {
		//	close the Global Instances dialog without saving
			globalInstanceWindow.detach();
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
	
	
	public void onChange$classNameTxtBox() {
		String className = classNameTxtBox.getValue();
		if(this.classNameList.contains(className)) {
			this.currentClassName = className;
			System.out.println("Class Name Updated: " + this.currentClassName);
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
	
	
}
