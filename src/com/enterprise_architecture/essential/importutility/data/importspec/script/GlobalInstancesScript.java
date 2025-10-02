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
 * 23.08.2018	JWC	Upgrade to ZK 8.5 - remove ZSS
 * 
 */
package com.enterprise_architecture.essential.importutility.data.importspec.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//import org.zkoss.zss.ui.Spreadsheet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.GlobalInstanceScript;
import com.enterprise_architecture.essential.importutility.data.importspec.GlobalInstances;

/**
 * This class extends the GlobalInstances class to provide the behaviours required to
 * generate the Essengtial Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class GlobalInstancesScript extends GlobalInstances  implements ImportSpecData {

	public static String TEMPLATE_NAME = "Global Instance Set";
	
	public GlobalInstancesScript() {
		super();
	}
	
	public GlobalInstancesScript(GlobalInstances globInstances) {
		super();
		this.derivedInstance = new ArrayList<DerivedInstanceType>();
		if(globInstances.getDerivedInstance() != null) {
			Iterator<DerivedInstanceType> instanceIter = globInstances.getDerivedInstance().iterator();
			while(instanceIter.hasNext()) {
				DerivedInstanceType aDerivedInstance = instanceIter.next();
				GlobalInstanceScript myGlobalInstance = new GlobalInstanceScript(aDerivedInstance);
				this.derivedInstance.add(myGlobalInstance);
			}
		}
	}
	
	
	public static HashMap<String, String> getScriptTokens() {
		HashMap<String, String> myTokens = new HashMap<String, String>();
		return myTokens;
	}
	
	
	/**
	 * @return GlobalInstancesScript a copy of this GlobalInstancesScript
	 */
	public GlobalInstancesScript getCopy() {
		return new GlobalInstancesScript(this);
	}
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		Iterator<DerivedInstanceType> derivedInstanceIter = this.getDerivedInstance().iterator();
		while(derivedInstanceIter.hasNext()) {
			DerivedInstanceTypeScript derivedInstanceSpec = (DerivedInstanceTypeScript) derivedInstanceIter.next();
			derivedInstanceSpec.validate(invalidData);
		}
		return invalidData;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String myString = "Global Instances";
		return myString;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#getElementLevel()
	 */
	@Override
	public int getElementLevel() {
		// TODO Auto-generated method stub
		return ImportSpecDataManager.ROOT_ELEMENT_LEVEL;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
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
			HashMap<String, String> modeTemplates = scriptTemplateMap.get(GlobalInstancesScript.TEMPLATE_NAME);
			
			//add the script for the import spec
			String importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			
			//add the scripts for the instance import specs for this worksheet
			Iterator instanceIter = (Iterator) this.derivedInstance.iterator();
			ImportSpecData anInstance;
			while(instanceIter.hasNext()) {
				anInstance = (ImportSpecData) instanceIter.next();
				importScriptString = importScriptString + anInstance.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, currentRow);
			}
			
			HashMap<String, String> terminatorTemplates = scriptTemplateMap.get(ImportSpecDataManager.SCRIPT_TERMINATOR_TEMPLATE_NAME);
			String terminatorString = terminatorTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			importScriptString = importScriptString + terminatorString;
			
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
			HashMap<String, String> modeTemplates = scriptTemplateMap.get(GlobalInstancesScript.TEMPLATE_NAME);
			
			//add the script for the import spec
			String importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			
			//add the scripts for the instance import specs for this worksheet
			Iterator instanceIter = (Iterator) this.derivedInstance.iterator();
			ImportSpecData anInstance;
			while(instanceIter.hasNext()) {
				anInstance = (ImportSpecData) instanceIter.next();
				importScriptString = importScriptString + anInstance.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, rowIndex, theMessageListener);
			}
			
			HashMap<String, String> terminatorTemplates = scriptTemplateMap.get(ImportSpecDataManager.SCRIPT_TERMINATOR_TEMPLATE_NAME);
			String terminatorString = terminatorTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			importScriptString = importScriptString + terminatorString;
			
			return importScriptString;
		}
	}
	
	
	
}
