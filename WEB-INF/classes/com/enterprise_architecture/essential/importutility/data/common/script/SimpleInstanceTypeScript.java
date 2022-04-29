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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//import org.zkoss.zss.ui.Spreadsheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;

import com.enterprise_architecture.essential.importutility.data.common.DeleteAllInstanceSlot;
import com.enterprise_architecture.essential.importutility.data.common.DeleteInstanceSlot;
import com.enterprise_architecture.essential.importutility.data.common.DerivedSimpleSlot;
import com.enterprise_architecture.essential.importutility.data.common.InstanceSlot;
import com.enterprise_architecture.essential.importutility.data.common.PrimitiveSlot;
import com.enterprise_architecture.essential.importutility.data.common.RemoveAllInstanceSlot;
import com.enterprise_architecture.essential.importutility.data.common.RemoveInstanceSlot;
import com.enterprise_architecture.essential.importutility.data.common.SimpleInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.SimpleSlot;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDatas;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SlotImportSpecData;

/**
 * This class extends the SimpleInstanceType class to provide the behaviours required to
 * generate the Essengtial Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class SimpleInstanceTypeScript extends SimpleInstanceType implements ImportSpecData {

	public static String TEMPLATE_NAME = "Simple Instance";
	
	public SimpleInstanceTypeScript() {
		super();
	}
	
	public SimpleInstanceTypeScript(SimpleInstanceType simpleInstance) {
		super();
		
		this.className = simpleInstance.getClassName();
		this.extIDRef = simpleInstance.getExtIDRef();
		this.matchingMode = simpleInstance.getMatchingMode();
		this.nameRef = simpleInstance.getNameRef();
		this.sequenceNo = simpleInstance.getSequenceNo();
		this.variableName = simpleInstance.getVariableName();
		
		this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot = new ArrayList();
		if(simpleInstance.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot() != null) {
			Iterator slotIter = simpleInstance.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().iterator();
			while(slotIter.hasNext()) {
				Object slotObject = slotIter.next();
				String slotClassName = slotObject.getClass().getName();
				if(slotClassName.endsWith(".InstanceSlot") || slotClassName.endsWith(".InstanceSlotScript")) {
					InstanceSlot aSlot = (InstanceSlot) slotObject;
					InstanceSlotScript mySlot = new InstanceSlotScript(aSlot);
					mySlot.setParentClassName(this.className);
					mySlot.setParentInstanceVariable(this.variableName);
					this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.add(mySlot);
				} 
				else if(slotClassName.endsWith(".PrimitiveSlot") || slotClassName.endsWith(".PrimitiveSlotScript")) {
					PrimitiveSlot aSlot = (PrimitiveSlot) slotObject;
					PrimitiveSlotScript mySlot = new PrimitiveSlotScript(aSlot);
					mySlot.setParentClassName(this.className);
					mySlot.setParentInstanceVariable(this.variableName);
					this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.add(mySlot);
				} 
				else if(slotClassName.endsWith(".SimpleSlot") || slotClassName.endsWith(".SimpleSlotScript")) {
					SimpleSlot aSlot = (SimpleSlot) slotObject;
					SimpleSlotScript mySlot = new SimpleSlotScript(aSlot);
					mySlot.setParentClassName(this.className);
					mySlot.setParentInstanceVariable(this.variableName);
					this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.add(mySlot);
				} 
				else if(slotClassName.endsWith(".DerivedSimpleSlot") || slotClassName.endsWith(".DerivedSimpleSlotScript")) {
					DerivedSimpleSlot aSlot = (DerivedSimpleSlot) slotObject;
					DerivedSimpleSlotScript mySlot = new DerivedSimpleSlotScript(aSlot);
					mySlot.setParentClassName(this.className);
					mySlot.setParentInstanceVariable(this.variableName);
					this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.add(mySlot);
				} 
				else if(slotClassName.endsWith(".DeleteInstanceSlot") || slotClassName.endsWith(".DeleteInstanceSlotScript")) {
					DeleteInstanceSlot aSlot = (DeleteInstanceSlot) slotObject;
					DeleteInstanceSlotScript mySlot = new DeleteInstanceSlotScript(aSlot);
					mySlot.setParentClassName(this.className);
					mySlot.setParentInstanceVariable(this.variableName);
					this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.add(mySlot);
				} 
				else if(slotClassName.endsWith(".RemoveInstanceSlot") || slotClassName.endsWith(".RemoveInstanceSlotScript")) {
					RemoveInstanceSlot aSlot = (RemoveInstanceSlot) slotObject;
					RemoveInstanceSlotScript mySlot = new RemoveInstanceSlotScript(aSlot);
					mySlot.setParentClassName(this.className);
					mySlot.setParentInstanceVariable(this.variableName);
					this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.add(mySlot);
				} 
				else if(slotClassName.endsWith(".DeleteAllInstanceSlot") || slotClassName.endsWith(".DeleteAllInstanceSlotScript")) {
					DeleteAllInstanceSlot aSlot = (DeleteAllInstanceSlot) slotObject;
					DeleteAllInstanceSlotScript mySlot = new DeleteAllInstanceSlotScript(aSlot);
					mySlot.setParentClassName(this.className);
					mySlot.setParentInstanceVariable(this.variableName);
					this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.add(mySlot);
				} 
				else if(slotClassName.endsWith(".RemoveAllInstanceSlot") || slotClassName.endsWith(".RemoveAllInstanceSlotScript")) {
					RemoveAllInstanceSlot aSlot = (RemoveAllInstanceSlot) slotObject;
					RemoveAllInstanceSlotScript mySlot = new RemoveAllInstanceSlotScript(aSlot);
					mySlot.setParentClassName(this.className);
					mySlot.setParentInstanceVariable(this.variableName);
					this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.add(mySlot);
				} else {
					System.out.println("FAILED TO IDENTIFY SLOT NODE CLASS: " + slotClassName);
				}
				
				
			}
		}
		

	}
	
	
	public static HashMap<String, String> getScriptTokens() {
		HashMap<String, String> myTokens = new HashMap<String, String>();
		myTokens.put("Instance Variable Name", ImportSpecDataManager.VARIABLE_NAME_TOKEN);
		myTokens.put("Class Name", ImportSpecDataManager.CLASS_NAME_TOKEN);
		myTokens.put("Instance ID", ImportSpecDataManager.INSTANCE_ID_TOKEN);
		myTokens.put("Internal ID", ImportSpecDataManager.INTERNAL_ID_TOKEN);
		myTokens.put("Instance Name", ImportSpecDataManager.INSTANCE_NAME_TOKEN);
		myTokens.put("Repository Name", ImportSpecDataManager.REPOSITORY_NAME_TOKEN);
		myTokens.put("Name Matching String", ImportSpecDataManager.MATCHING_STRING_TOKEN);
		return myTokens;
	}
	
	
	//Get a copy of this DerivedInstanceTypeScript
	public SimpleInstanceTypeScript getCopy() {
		return new SimpleInstanceTypeScript(this);
	}
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		if((this.getClassName() == null) || (this.getExtIDRef() == null) || (this.getNameRef() == null) || (this.getVariableName() == null)) {
			invalidData.add(this);
		}
		
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
		String myString = "Simple Instance: ";
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
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.DerivedInstanceType#getElementLevel()
	 */
	@Override
	public void setVariableName(String value) {
        this.variableName = value;
        Iterator slotIter = getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().iterator();
		while(slotIter.hasNext()) {
			Object slotObject = slotIter.next();
			SlotImportSpecData slotImportSpec = (SlotImportSpecData) slotObject;
			slotImportSpec.setParentInstanceVariable(this.variableName);
		}
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
		
		if(ImportSpecDatas.isEmptyValue(this.getClassName())) {
			return false;
		}
		
		if(ImportSpecDatas.isEmptyValue(this.getMatchingMode())) {
			return false;
		}
		
		if(ImportSpecDatas.isEmptyValue(this.getVariableName())) {
			return false;
		}
		
		if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_NEW)) {
			if(ImportSpecDatas.isEmptyValue(this.getClassName()) || ImportSpecDatas.isEmptyValue(this.getNameRef()) || ImportSpecDatas.isEmptyValue(this.getVariableName()) || ImportSpecDatas.isEmptyValue(this.getMatchingMode())) {
				return false;
			}
		} else if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME)) {
			if((ImportSpecDatas.isEmptyValue(this.getClassName())) || (ImportSpecDatas.isEmptyValue(this.getNameRef())) || (ImportSpecDatas.isEmptyValue(this.getVariableName())) || (ImportSpecDatas.isEmptyValue(this.getMatchingMode())) ) {
				return false;
			}
		} else {
			if(ImportSpecDatas.isEmptyValue(this.getClassName()) || ImportSpecDatas.isEmptyValue(this.getExtIDRef()) || ImportSpecDatas.isEmptyValue(this.getNameRef()) || ImportSpecDatas.isEmptyValue(this.getVariableName()) || ImportSpecDatas.isEmptyValue(this.getMatchingMode())) {
				return false;
			}
		}
		return true;
	}
	
	
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
			String importScriptString = modeTemplates.get(this.matchingMode);
			
			//update the template with this instance's name
			String nameCellContent = ImportSpecDatas.getCellValue(worksheet, rowIndex, this.nameRef);
			
			
			if(ImportSpecDatas.isEmptyValue(nameCellContent)) {
				return "";
			} else {
				String cleanNameCellContent = ImportSpecDatas.removeSpecialCharacters(nameCellContent);
				importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_NAME_TOKEN, cleanNameCellContent);
			}	
			
			//update the template with this instance's id (internal or external)
			if(!(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_NEW))) {
				String idCellContent = ImportSpecDatas.getCellValue(worksheet, rowIndex, this.extIDRef);
				if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME)) 
				{
					// In match by name, ensure the internal ID value is cleared
					if(ImportSpecDatas.isEmptyValue(idCellContent)) 
					{
						String cleanNameCellContent = ImportSpecDatas.removeSpecialCharacters(nameCellContent);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, cleanNameCellContent);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, "");
					} else {
						String cleanIdCellContent = ImportSpecDatas.removeSpecialCharacters(idCellContent);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, cleanIdCellContent);						
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, "");
					}
				} 
				else {	
					if(ImportSpecDatas.isEmptyValue(idCellContent)) {
						return "";
					} else {
						String cleanIdCellContent = ImportSpecDatas.removeSpecialCharacters(idCellContent);
						if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYID)) {
							importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, cleanIdCellContent);
							importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, "");
						} else if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYINTID)) {
							importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, "");
							importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, cleanIdCellContent);
						}
					}	
				}
			}
			
			//update the template with this instance's variable name
			importScriptString = importScriptString.replace(ImportSpecDataManager.VARIABLE_NAME_TOKEN, this.variableName);

			//update the template with this instance's class name
			importScriptString = importScriptString.replace(ImportSpecDataManager.CLASS_NAME_TOKEN, this.className);
			
			
			//update the template with this instance's name, if matching mode is by name
			importScriptString = importScriptString.replace(ImportSpecDataManager.MATCHING_STRING_TOKEN, nameCellContent);
			
			//update the template with the name of the source repository
			importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);

			
			//add the scripts for the slot import specs for this instance
			Iterator slotIter = (Iterator) this.instanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot.iterator();
			SlotImportSpecData aSlot;
			String slotScript;
			while(slotIter.hasNext()) {
				aSlot = (SlotImportSpecData) slotIter.next();
				slotScript = aSlot.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, rowIndex, theMessageListener);
				if(slotScript != null) {
					importScriptString = importScriptString + slotScript;
				}
			}
			
			//return the import script string
			return importScriptString;
		}

	}

	/*@Override
	public String printImportScript(String srcRepositoryName,
			Spreadsheet spreadsheet,
			HashMap<String, HashMap<String, String>> scriptTemplateMap,
			String rowTitle) {
		// TODO Auto-generated method stub
		return null;
	}
	
	*/
	

}
