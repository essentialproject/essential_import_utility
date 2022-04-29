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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
//import org.zkoss.zss.ui.Spreadsheet;

import com.enterprise_architecture.essential.importutility.data.common.PrimitiveSlot;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDatas;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SlotImportSpecData;

/**
 * This class extends the PrimitiveSlot class to provide the behaviours required to
 * generate the Essengtial Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class PrimitiveSlotScript extends PrimitiveSlot  implements SlotImportSpecData {

	public static String TEMPLATE_NAME = "Primitive Slot";
	private String parentInstanceVar;
	private String parentClassName;
	
	
	public PrimitiveSlotScript() {
		super();
	}
	
	public PrimitiveSlotScript(PrimitiveSlot primSlot) {
		super();
		this.primitiveSlotType = primSlot.getPrimitiveSlotType();
		this.conditionalRef = primSlot.getConditionalRef();
		this.sequenceNo = primSlot.getSequenceNo();
		this.slotName = primSlot.getSlotName();
		this.slotValueRef = primSlot.getSlotValueRef();
	}
	
	
	public static HashMap<String, String> getScriptTokens() {
		HashMap<String, String> myTokens = new HashMap<String, String>();
		myTokens.put("Target Instance Variable", ImportSpecDataManager.VARIABLE_NAME_TOKEN);
		myTokens.put("Slot Name", ImportSpecDataManager.SLOT_NAME_TOKEN);
		myTokens.put("Slot Type", ImportSpecDataManager.PRIMITIVE_SLOT_TYPE_TOKEN);
		myTokens.put("Slot Value", ImportSpecDataManager.SLOT_VALUE_TOKEN);
		return myTokens;
	}
	
	
	
	/**
	 * @return the parentClassName
	 */
	public String getParentClassName() {
		return parentClassName;
	}

	/**
	 * @param parentClassName the parentClassName to set
	 */
	public void setParentClassName(String parentClassName) {
		this.parentClassName = parentClassName;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.SlotImportSpecData#getParentInstanceVariable()
	 */
	@Override
	public String getParentInstanceVariable() {
		// TODO Auto-generated method stub
		return parentInstanceVar;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.SlotImportSpecData#setParentInstanceVariable(java.lang.String)
	 */
	@Override
	public void setParentInstanceVariable(String variableName) {
		// TODO Auto-generated method stub
		parentInstanceVar = variableName;
	}
	
	
	/**
	 * @return PrimitiveSlotScript a copy of this PrimitiveSlotScript
	 */
	public PrimitiveSlotScript getCopy() {
		return new PrimitiveSlotScript(this);
	}
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		if((this.getSlotName() == null) || (this.getPrimitiveSlotType() == null)  || (this.getSlotValueRef() == null)) {
			invalidData.add(this);
		}
		
		return invalidData;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String myString = "Primitive Slot: ";
		if(this.getSlotName() != null) {
			myString = myString + this.getSlotName();
		} else {
			myString = myString + "<UNDEFINED>";
		}
		if(this.getPrimitiveSlotType() != null) {
			myString = myString + " (" + this.getPrimitiveSlotType() + ")";
		}
		return myString;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#getElementLevel()
	 */
	@Override
	public int getElementLevel() {
		// TODO Auto-generated method stub
		return ImportSpecDataManager.SLOT_ELEMENT_LEVEL;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		if((this.getSlotName() == null) || (this.getPrimitiveSlotType() == null)  || (this.getSlotValueRef() == null) || (this.getParentInstanceVariable() == null)) {
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
			//check to make sure that the conditional references have values
			if(this.conditionalRef != null && this.conditionalRef.size() > 0) {
				String aCondRef, aCondValue;
				Iterator<String> conditionalRefIter = this.conditionalRef.iterator();
				while(conditionalRefIter.hasNext()) {
					aCondRef = conditionalRefIter.next();
					aCondValue = ImportSpecDatas.getCellValue(spreadsheet, currentRow, aCondRef);
					if(aCondValue == null || aCondValue.length() == 0) {
						return "";
					}
				}
			}
			
			//get the template for this element type
			HashMap<String, String> modeTemplates = scriptTemplateMap.get(TEMPLATE_NAME);
			String importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			

			//get the contents of the cell referred to by this value
			String cellContent = ImportSpecDatas.getCellValue(spreadsheet, currentRow, this.getSlotValueRef());
			if(cellContent == null || cellContent.length() == 0) {
				return "";
			} else {
				String cleanCellContent;
				if(this.primitiveSlotType.equals(ImportSpecDataManager.BOOLEAN_PRIMITIVE_TYPE)) {
					cleanCellContent = ImportSpecDatas.removeNonAlpha(cellContent);
				}
				else {
					cleanCellContent = ImportSpecDatas.removeNonNumeric(cellContent);
				}

				importScriptString = importScriptString.replace(ImportSpecDataManager.SLOT_VALUE_TOKEN, cleanCellContent);
			}	
			
			//update the template with this primitive slot's name
			importScriptString = importScriptString.replace(ImportSpecDataManager.SLOT_NAME_TOKEN, this.getSlotName());
			
			//update the template with this primitive slot's parent's variable name
			importScriptString = importScriptString.replace(ImportSpecDataManager.VARIABLE_NAME_TOKEN, this.parentInstanceVar);
			
			//update the template with this primitive slot's type
			importScriptString = importScriptString.replace(ImportSpecDataManager.PRIMITIVE_SLOT_TYPE_TOKEN, this.primitiveSlotType);

			
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
			//check to make sure that the conditional references have values
			if(this.conditionalRef != null && this.conditionalRef.size() > 0) {
				String aCondRef, aCondValue;
				Iterator<String> conditionalRefIter = this.conditionalRef.iterator();
				while(conditionalRefIter.hasNext()) {
					aCondRef = conditionalRefIter.next();
					aCondValue = ImportSpecDatas.getCellValue(worksheet, rowIndex, aCondRef);
					if(aCondValue == null || aCondValue.length() == 0) {
						return "";
					}
				}
			}
			
			//get the template for this element type
			HashMap<String, String> modeTemplates = scriptTemplateMap.get(TEMPLATE_NAME);
			String importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			

			//get the contents of the cell referred to by this value
			String cellContent = ImportSpecDatas.getCellValue(worksheet, rowIndex, this.getSlotValueRef());
			if(cellContent == null || cellContent.length() == 0) {
				return "";
			} else {
				String cleanCellContent;
				if(this.primitiveSlotType.equals(ImportSpecDataManager.BOOLEAN_PRIMITIVE_TYPE)) {
					cleanCellContent = ImportSpecDatas.removeNonAlpha(cellContent);
				}
				else {
					cleanCellContent = ImportSpecDatas.removeNonNumeric(cellContent);
				}

				importScriptString = importScriptString.replace(ImportSpecDataManager.SLOT_VALUE_TOKEN, cleanCellContent);
			}	
			
			//update the template with this primitive slot's name
			importScriptString = importScriptString.replace(ImportSpecDataManager.SLOT_NAME_TOKEN, this.getSlotName());
			
			//update the template with this primitive slot's parent's variable name
			importScriptString = importScriptString.replace(ImportSpecDataManager.VARIABLE_NAME_TOKEN, this.parentInstanceVar);
			
			//update the template with this primitive slot's type
			importScriptString = importScriptString.replace(ImportSpecDataManager.PRIMITIVE_SLOT_TYPE_TOKEN, this.primitiveSlotType);

			
			//return the import script string
			return importScriptString;
		}

	}

}
