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
import java.util.List;

//import org.zkoss.zss.ui.Spreadsheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.enterprise_architecture.essential.importutility.data.common.DeleteDerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDatas;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;

/**
 * This class extends the DerivedInstanceType class to provide the behaviours required to
 * generate the Essengtial Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class DeleteDerivedInstanceTypeScript extends DeleteDerivedInstanceType implements ImportSpecData {
	
	public static String TEMPLATE_NAME = "Delete Derived Instance";
	
	public DeleteDerivedInstanceTypeScript() {
		super();
	}
	
	public DeleteDerivedInstanceTypeScript(DeleteDerivedInstanceType aDeletDerivedInstance) {
		super();
		this.className = aDeletDerivedInstance.getClassName();
		this.matchingMode = aDeletDerivedInstance.getMatchingMode();
		this.derivedExtID = new DerivedValueScript(aDeletDerivedInstance.getDerivedExtID());
		this.derivedName = new DerivedValueScript(aDeletDerivedInstance.getDerivedName());
		
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
	public DeleteDerivedInstanceTypeScript getCopy() {
		return new DeleteDerivedInstanceTypeScript(this);
	}
	
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		if((this.getClassName() == null) || (this.getDerivedExtID() == null)) {
			invalidData.add(this);
		}
		DerivedValueScript extID = (DerivedValueScript) this.getDerivedExtID();
		extID.validate(invalidData);
		
		DerivedValueScript name = (DerivedValueScript) this.getDerivedName();
		name.validate(invalidData);
		
		return invalidData;
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String myString = "Delete Derived Instance: ";
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
		
		if(ImportSpecDatas.isEmptyValue(this.getClassName())) {
			return false;
		}
		
		if(ImportSpecDatas.isEmptyValue(this.getMatchingMode())) {
			return false;
		}
		
		DerivedValueScript name = (DerivedValueScript) this.getDerivedName();
		if(!name.isValid()) {
			return false;
		}
		
		if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYID) || this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYINTID)) {
			DerivedValueScript extID = (DerivedValueScript) this.getDerivedExtID();
			if(!extID.isValid()) {
				return false;
			}
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
			String importScriptString = modeTemplates.get(this.matchingMode);
			
			//update the template with this instance's name
			ImportSpecData myDerivedName = (ImportSpecData) this.derivedName;
			String derivedNameString = myDerivedName.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, currentRow);
			if(ImportSpecDatas.isEmptyValue(derivedNameString)) {
				return "";
			}
			importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_NAME_TOKEN, derivedNameString);			
			
			//update the template with this instance's id
			if(!(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_NEW))) {
				ImportSpecData myDerivedID = (ImportSpecData) this.derivedExtID;
				String derivedIDString = myDerivedID.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, currentRow);
				if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME)) {
					if(ImportSpecDatas.isEmptyValue(derivedIDString)) {
						//update the template with the instance name as the external reference for the instance
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedNameString);
						
						//set the internal (Protege) id for the instance as the name
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, derivedNameString);
					} else {
						//update the template with the defined external id as the external reference for the instance
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedIDString);
						
						//set the internal (Protege) id for the instance
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, derivedIDString);
					}
				} else {
					if(ImportSpecDatas.isEmptyValue(derivedIDString)) {
						return "";
					}
					if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYID)) {
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedIDString);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, derivedIDString);
					} else if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYINTID)) {
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedIDString);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, derivedIDString);
					}				
				}
			
			}

			//update the template with this instance's class name
			importScriptString = importScriptString.replace(ImportSpecDataManager.CLASS_NAME_TOKEN, this.className);
			
			//update the template with this instance's name, if matching mode is by name
			importScriptString = importScriptString.replace(ImportSpecDataManager.MATCHING_STRING_TOKEN, derivedNameString);
			
			//update the template with the name of the source repository
			importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);

			
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
			String importScriptString = modeTemplates.get(this.matchingMode);
			
			//update the template with this instance's name
			ImportSpecData myDerivedName = (ImportSpecData) this.derivedName;
			String derivedNameString = myDerivedName.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, rowIndex, theMessageListener);
			if(ImportSpecDatas.isEmptyValue(derivedNameString)) {
				return "";
			}
			importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_NAME_TOKEN, derivedNameString);			
			
			//update the template with this instance's id
			if(!(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_NEW))) {
				ImportSpecData myDerivedID = (ImportSpecData) this.derivedExtID;
				String derivedIDString = myDerivedID.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, rowIndex, theMessageListener);
				if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME)) 
				{
					// If we're matching by name, clear the internal ID parameter for the call in the script
					if(ImportSpecDatas.isEmptyValue(derivedIDString)) 
					{
						//update the template with the instance name as the external reference for the instance
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedNameString);
						
						// clear the internal (Protege) id for the instance as the name
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, "");
					} 
					else 
					{
						//update the template with the defined external id as the external reference for the instance
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedIDString);
						
						// clear the internal (Protege) id for the instance as the name
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, "");
					}
				} 
				else {
					if(ImportSpecDatas.isEmptyValue(derivedIDString)) {
						return "";
					}
					if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYID)) {
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedIDString);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, derivedIDString);
					} else if(this.getMatchingMode().equals(ImportSpecDataManager.IMPORT_MATCHMODE_BYINTID)) {
						importScriptString = importScriptString.replace(ImportSpecDataManager.INSTANCE_ID_TOKEN, derivedIDString);
						importScriptString = importScriptString.replace(ImportSpecDataManager.INTERNAL_ID_TOKEN, derivedIDString);
					}				
				}
			
			}

			//update the template with this instance's class name
			importScriptString = importScriptString.replace(ImportSpecDataManager.CLASS_NAME_TOKEN, this.className);
			
			//update the template with this instance's name, if matching mode is by name
			importScriptString = importScriptString.replace(ImportSpecDataManager.MATCHING_STRING_TOKEN, derivedNameString);
			
			//update the template with the name of the source repository
			importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);
			
			
			//return the import script string
			return importScriptString;
		}

	}
}
