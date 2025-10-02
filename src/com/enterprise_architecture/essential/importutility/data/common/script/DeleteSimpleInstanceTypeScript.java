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
 * 23.08.2018	JWC	Upgrade to ZK 8.5
 * 
 */
package com.enterprise_architecture.essential.importutility.data.common.script;

import java.util.HashMap;
import java.util.List;

//import org.zkoss.zss.ui.Spreadsheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;

import com.enterprise_architecture.essential.importutility.data.common.DeleteSimpleInstanceType;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDatas;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;

/**
 * This class extends the SimpleInstanceType class to provide the behaviours required to
 * generate the Essengtial Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011</br>
 * @version 2.0 - 23.08.2018
 *
 */
public class DeleteSimpleInstanceTypeScript extends DeleteSimpleInstanceType implements ImportSpecData {

	public static String TEMPLATE_NAME = "Delete Simple Instance";
	
	public DeleteSimpleInstanceTypeScript() {
		super();
	}
	
	public DeleteSimpleInstanceTypeScript(DeleteSimpleInstanceType simpleInstance) {
		super();
		
		this.className = simpleInstance.getClassName();
		this.extIDRef = simpleInstance.getExtIDRef();
		this.matchingMode = simpleInstance.getMatchingMode();
		this.nameRef = simpleInstance.getNameRef();
		this.sequenceNo = simpleInstance.getSequenceNo();

	}
	
	
	public static HashMap<String, String> getScriptTokens() {
		HashMap<String, String> myTokens = new HashMap<String, String>();
		myTokens.put("Class Name", ImportSpecDataManager.CLASS_NAME_TOKEN);
		myTokens.put("Instance ID", ImportSpecDataManager.INSTANCE_ID_TOKEN);
		myTokens.put("Internal ID", ImportSpecDataManager.INTERNAL_ID_TOKEN);
		myTokens.put("Instance Name", ImportSpecDataManager.INSTANCE_NAME_TOKEN);
		myTokens.put("Repository Name", ImportSpecDataManager.REPOSITORY_NAME_TOKEN);
		myTokens.put("Name Matching String", ImportSpecDataManager.MATCHING_STRING_TOKEN);
		return myTokens;
	}
	
	
	//Get a copy of this DerivedInstanceTypeScript
	public DeleteSimpleInstanceTypeScript getCopy() {
		return new DeleteSimpleInstanceTypeScript(this);
	}
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		if((this.getClassName() == null) || (this.getExtIDRef() == null) || (this.getNameRef() == null)) {
			invalidData.add(this);
		}
		
		return invalidData;
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String myString = "Delete Simple Instance: ";
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
		
		if(ImportSpecDatas.isEmptyValue(this.getClassName())) {
			return false;
		}
		
		if(ImportSpecDatas.isEmptyValue(this.getMatchingMode())) {
			return false;
		}
		
		if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_NEW)) {
			if(ImportSpecDatas.isEmptyValue(this.getClassName()) || ImportSpecDatas.isEmptyValue(this.getNameRef()) || ImportSpecDatas.isEmptyValue(this.getMatchingMode())) {
				return false;
			}
		} else if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME)) {
			if((ImportSpecDatas.isEmptyValue(this.getClassName())) || (ImportSpecDatas.isEmptyValue(this.getNameRef())) || (ImportSpecDatas.isEmptyValue(this.getMatchingMode())) ) {
				return false;
			}
		} else {
			if(ImportSpecDatas.isEmptyValue(this.getClassName()) || ImportSpecDatas.isEmptyValue(this.getExtIDRef()) || ImportSpecDatas.isEmptyValue(this.getNameRef()) || ImportSpecDatas.isEmptyValue(this.getMatchingMode())) {
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
					// If we're matching by name, clear the internal ID parameter for the call in the script
					if(ImportSpecDatas.isEmptyValue(idCellContent)) 
					{
						String cleanNameCellContent = ImportSpecDatas.removeSpecialCharacters(nameCellContent);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, cleanNameCellContent);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, "");
					} 
					else 
					{
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
							importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, cleanIdCellContent);
						} else if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYINTID)) {
							importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, cleanIdCellContent);
							importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, cleanIdCellContent);
						}
					}	
				}
			}

			//update the template with this instance's class name
			importScriptString = importScriptString.replace(ImportSpecDataManager.CLASS_NAME_TOKEN, this.className);
			
			
			//update the template with this instance's name, if matching mode is by name
			importScriptString = importScriptString.replace(ImportSpecDataManager.MATCHING_STRING_TOKEN, nameCellContent);
			
			//update the template with the name of the source repository
			importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);

			
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
