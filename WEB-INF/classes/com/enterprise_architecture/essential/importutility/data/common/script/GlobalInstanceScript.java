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
 * 23.08.2018	JWC Upgrade to ZK 8.5
 * 
 */
package com.enterprise_architecture.essential.importutility.data.common.script;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//import org.zkoss.zss.ui.Spreadsheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SlotImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.WorksheetImportSpecScript;

/**
 * This class extends the DerivedInstanceType class to provide the behaviours required to
 * generate the Essential Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class GlobalInstanceScript extends DerivedInstanceType implements ImportSpecData {

	public static String TEMPLATE_NAME = "Global Instance";
	
	public GlobalInstanceScript() {
		super();
	}
	
	public GlobalInstanceScript(DerivedInstanceType aDerivedInstance) {
		super();
		this.className = aDerivedInstance.getClassName();
		this.variableName = aDerivedInstance.getVariableName();
		this.matchingMode = aDerivedInstance.getMatchingMode();
		this.derivedExtID = new DerivedValueScript(aDerivedInstance.getDerivedExtID());
		this.derivedName = new DerivedValueScript(aDerivedInstance.getDerivedName());
	}
	
	
	public static HashMap<String, String> getScriptTokens() {
		HashMap<String, String> myTokens = new HashMap<String, String>();
		myTokens.put("Instance Variable Name", ImportSpecDataManager.VARIABLE_NAME_TOKEN);
		myTokens.put("Class Name", ImportSpecDataManager.CLASS_NAME_TOKEN);
		myTokens.put("Instance ID", ImportSpecDataManager.INSTANCE_ID_TOKEN);
	//	myTokens.put("Internal ID", ImportSpecDataManager.INTERNAL_ID_TOKEN);
		myTokens.put("Instance Name", ImportSpecDataManager.INSTANCE_NAME_TOKEN);
		myTokens.put("Repository Name", ImportSpecDataManager.REPOSITORY_NAME_TOKEN);
		myTokens.put("Name Matching String", ImportSpecDataManager.MATCHING_STRING_TOKEN);
		return myTokens;
	}
	
	
	/**
	 * @return GlobalInstanceScript a copy of this GlobalInstanceScript
	 */
	public GlobalInstanceScript getCopy() {
		return new GlobalInstanceScript(this);
	}
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		if((this.getClassName() == null) || (this.getDerivedExtID() == null)  || (this.getVariableName() == null)) {
			invalidData.add(this);
		}
		DerivedValueScript extID = (DerivedValueScript) this.getDerivedExtID();
		extID.validate(invalidData);
		
		DerivedValueScript name = (DerivedValueScript) this.getDerivedName();
		name.validate(invalidData);
		
		Iterator slots = this.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().iterator();
		while(slots.hasNext()) {
			ImportSpecData slot = (ImportSpecData) slots.next();
			slot.validate(invalidData);
		}
		
		return invalidData;
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String myString = "Global Instance: ";
		if(this.getVariableName() != null) {
			myString = myString + this.getVariableName();
		} else {
			myString = myString + "<UNDEFINED>";
		}
		if(this.getClassName() != null) {
			myString = myString  + " (" + this.getClassName() + ")";
		} else {
			myString = myString + " (CLASS UNDEFINED)";
		}
		return myString;
	}


	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#getElementLevel()
	 */
	@Override
	public int getElementLevel() {
		// TODO Auto-generated method stub
		return ImportSpecDataManager.INSTANCE_ELEMENT_LEVEL;
	}
	
	
	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		if((this.getDerivedName() == null) || (this.getClassName() == null) || (this.getDerivedExtID() == null)  || (this.getVariableName() == null)) {
			return false;
		}
		
		DerivedValueScript extID = (DerivedValueScript) this.getDerivedExtID();
		if(!extID.isValid()) {
			return false;
		}
		
		DerivedValueScript name = (DerivedValueScript) this.getDerivedName();
		if(!name.isValid()) {
			return false;
		}
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	/*@Override
	public String printImportScript(String srcRepositoryName, Spreadsheet spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, String currentRow) {
		if(!this.isValid()) {
			return null;
		} else {
			
			//get the template for this element type
			HashMap<String, String> modeTemplates = scriptTemplateMap.get(TEMPLATE_NAME);
			String importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME);
			
			//update the template with this instance's name
			ImportSpecData myDerivedName = (ImportSpecData) this.derivedName;
			String derivedNameString = myDerivedName.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, currentRow);
			if(derivedNameString == null) {
				return "";
			}
			importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_NAME_TOKEN, derivedNameString);			
			
			//update the template with this instance's name
			ImportSpecData myDerivedID = (ImportSpecData) this.derivedExtID;
			String derivedIDString = myDerivedID.printImportScript(spreadsheet, scriptTemplateMap, currentRow);
			if(derivedIDString == null) {
				return "";
			} 
			
			//By default, use the name of the instance as the id
			importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedNameString);
			
			//update the template with this instance's variable name
			importScriptString = importScriptString.replace(ImportSpecDataManager.VARIABLE_NAME_TOKEN, this.variableName);

			//update the template with this instance's class name
			importScriptString = importScriptString.replace(ImportSpecDataManager.CLASS_NAME_TOKEN, this.className);
			
			//update the template with this instance's name, if matching mode is by name
			importScriptString = importScriptString.replace(ImportSpecDataManager.MATCHING_STRING_TOKEN, derivedNameString);
			
			//update the template with the name of the source repository
			importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);
			
			
			//add the scripts for the slot import specs for this instance
			Iterator slotIter = (Iterator) this.instanceSlotOrSimpleSlotOrDerivedSimpleSlot.iterator();
			SlotImportSpecData aSlot;
			String slotScript;
			while(slotIter.hasNext()) {
				aSlot = (SlotImportSpecData) slotIter.next();
				slotScript = aSlot.printImportScript(spreadsheet, scriptTemplateMap, currentRow);
				if(slotScript != null) {
					importScriptString = importScriptString + slotScript;
				}
			}  
			
			//return the import script string
			return importScriptString;
		}

	}
	*/
	
	
	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	@Override
	public String printImportScript(String srcRepositoryName, Workbook spreadsheet, Sheet worksheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int rowIndex, ImportSpecScriptListener theMessageListener) {
		if(!this.isValid()) {
			return null;
		} else {
			
			//get the template for this element type
			HashMap<String, String> modeTemplates = scriptTemplateMap.get(TEMPLATE_NAME);
			String importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME);
			
			//update the template with this instance's name
			ImportSpecData myDerivedName = (ImportSpecData) this.derivedName;
			String derivedNameString = myDerivedName.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, rowIndex, theMessageListener);
			if(derivedNameString == null) {
				return "";
			}
			importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_NAME_TOKEN, derivedNameString);			
			
		/*	//update the template with this instance's name
			ImportSpecData myDerivedID = (ImportSpecData) this.derivedExtID;
			String derivedIDString = myDerivedID.printImportScript(spreadsheet, scriptTemplateMap, currentRow);
			if(derivedIDString == null) {
				return "";
			} */
			
			//By default, use the name of the instance as the id
			importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedNameString);
			
			//update the template with this instance's variable name
			importScriptString = importScriptString.replace(ImportSpecDataManager.VARIABLE_NAME_TOKEN, this.variableName);

			//update the template with this instance's class name
			importScriptString = importScriptString.replace(ImportSpecDataManager.CLASS_NAME_TOKEN, this.className);
			
			//update the template with this instance's name, if matching mode is by name
			importScriptString = importScriptString.replace(ImportSpecDataManager.MATCHING_STRING_TOKEN, derivedNameString);
			
			//update the template with the name of the source repository
			importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);
			
			
		/*	//add the scripts for the slot import specs for this instance
			Iterator slotIter = (Iterator) this.instanceSlotOrSimpleSlotOrDerivedSimpleSlot.iterator();
			SlotImportSpecData aSlot;
			String slotScript;
			while(slotIter.hasNext()) {
				aSlot = (SlotImportSpecData) slotIter.next();
				slotScript = aSlot.printImportScript(spreadsheet, scriptTemplateMap, currentRow);
				if(slotScript != null) {
					importScriptString = importScriptString + slotScript;
				}
			}  */
			
			//return the import script string
			return importScriptString;
		}

	}
}
