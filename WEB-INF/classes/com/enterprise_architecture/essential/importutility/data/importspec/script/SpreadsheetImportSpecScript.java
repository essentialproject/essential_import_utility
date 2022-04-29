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
 * 23.08.2018	JWC Migrate to ZK 8.5, replace ZSS with Apache POI
 * 
 */
package com.enterprise_architecture.essential.importutility.data.importspec.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//import org.zkoss.zss.ui.Spreadsheet;

import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueRefScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueStringScript;
import com.enterprise_architecture.essential.importutility.data.importspec.SpreadsheetImportSpec;
import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * This class extends the SpreadsheetImportSpec class to provide the behaviours required to
 * generate the Essengtial Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class SpreadsheetImportSpecScript extends SpreadsheetImportSpec implements ImportSpecData {

	public static String TEMPLATE_NAME = "Script Prefix";
	
	public WorksheetImportSpecScript currentWorksheet;
	private int currentWorksheetIndex = -1;
	private int rowsCompleted = 0;
	private int totalRows = 0;
	private String currentStatusString = "";
	private int percentageComplete;
	

	public SpreadsheetImportSpecScript() {
		super();
	}
	
	public SpreadsheetImportSpecScript(SpreadsheetImportSpec importSpec) {
		super();
		this.importSchemaVersion = importSpec.getImportSchemaVersion();
		this.globalInstances = new GlobalInstancesScript(importSpec.getGlobalInstances());
		
		this.worksheetImportSpec = new ArrayList<WorksheetImportSpec>();
		if(importSpec.getWorksheetImportSpec() != null) {
			Iterator<WorksheetImportSpec> worksheetIter = importSpec.getWorksheetImportSpec().iterator();
			while(worksheetIter.hasNext()) {
				WorksheetImportSpec worksheet = worksheetIter.next();
				WorksheetImportSpecScript myWorksheet = new WorksheetImportSpecScript(worksheet);
				this.worksheetImportSpec.add(myWorksheet);
			}
		}
	}
	
	public static HashMap<String, String> getScriptTokens() {
		HashMap<String, String> myTokens = new HashMap<String, String>();
		myTokens.put("Script Generation Timestamp", ImportSpecDataManager.TIMESTAMP_TOKEN);
		myTokens.put("Repository Name", ImportSpecDataManager.REPOSITORY_NAME_TOKEN);
		return myTokens;
	}
	
	

	/**
	 * @return SpreadsheetImportSpecScript a copy of this SpreadsheetImportSpecScript
	 */
	public SpreadsheetImportSpecScript getCopy() {
		return new SpreadsheetImportSpecScript(this);
	}
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		GlobalInstancesScript globInstances = (GlobalInstancesScript) this.globalInstances;
		globInstances.validate(invalidData);
		
		Iterator worksheetIter = this.getWorksheetImportSpec().iterator();
		while(worksheetIter.hasNext()) {
			WorksheetImportSpecScript wkImportSpec = (WorksheetImportSpecScript) worksheetIter.next();
			wkImportSpec.validate(invalidData);
		}
		return invalidData;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#getElementLevel()
	 */
	@Override
	public int getElementLevel() {
		// TODO Auto-generated method stub
		return ImportSpecDataManager.SPEC_ELEMENT_LEVEL;
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
		String importScriptString = "";
		HashMap<String, String> modeTemplates = scriptTemplateMap.get(SpreadsheetImportSpecScript.TEMPLATE_NAME);
		
		if(modeTemplates != null) {
			//add the prefix script for the import spec
			importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			
			if(importScriptString != null) {
				
				//set the appropriate values
				String currentTime = new Date().toString();
				importScriptString = importScriptString.replace(ImportSpecDataManager.TIMESTAMP_TOKEN, currentTime);
				
				//update the template with the name of the source repository
				importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);
				
				//add the script for the global instances
				GlobalInstancesScript globInstances = (GlobalInstancesScript) this.globalInstances;
				importScriptString = importScriptString + globInstances.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, null);
				
				
				//add the script for the worksheets
				Iterator<WorksheetImportSpec> worksheetIter = this.getWorksheetImportSpec().iterator();
				
				while(worksheetIter.hasNext()) {
						WorksheetImportSpecScript wkImportSpec = (WorksheetImportSpecScript) worksheetIter.next();
						
						importScriptString = importScriptString + wkImportSpec.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, null);
						System.out.println("GENERATED WORKSHEET SCRIPT:" + importScriptString);
				}
				
			}
		} 
		
		return importScriptString;
	}
	
	*/
	
	
	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	@Override
	public String printImportScript(String srcRepositoryName, Workbook spreadsheet, Sheet worksheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int rowIndex, ImportSpecScriptListener theMessageListener) {
		String importScriptString = "";
		HashMap<String, String> modeTemplates = scriptTemplateMap.get(SpreadsheetImportSpecScript.TEMPLATE_NAME);
		
		if(modeTemplates != null) {
			//add the prefix script for the import spec
			importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			
			if(importScriptString != null) {
				
				//set the appropriate values
				String currentTime = new Date().toString();
				importScriptString = importScriptString.replace(ImportSpecDataManager.TIMESTAMP_TOKEN, currentTime);
				
				//update the template with the name of the source repository
				importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);
				
				//add the script for the global instances
				GlobalInstancesScript globInstances = (GlobalInstancesScript) this.globalInstances;
				importScriptString = importScriptString + globInstances.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, 0, theMessageListener);
				
				
				//add the script for the worksheets
				Iterator<WorksheetImportSpec> worksheetIter = this.getWorksheetImportSpec().iterator();
				
				while(worksheetIter.hasNext()) {
						WorksheetImportSpecScript wkImportSpec = (WorksheetImportSpecScript) worksheetIter.next();
						
						importScriptString = importScriptString + wkImportSpec.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, 0, theMessageListener);
						//System.out.println("GENERATED WORKSHEET SCRIPT:" + importScriptString);
				}
				
			}
		} 
		
		return importScriptString;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	public String printDUPImportScript(String srcRepositoryName, Workbook spreadsheet, Sheet worksheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int rowIndex, ImportSpecScriptListener theMessageListener) {
		String importScriptString = "";
		HashMap<String, String> modeTemplates = scriptTemplateMap.get(SpreadsheetImportSpecScript.TEMPLATE_NAME);
		
		if(modeTemplates != null) {
			//add the prefix script for the import spec
			importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_DUP);
			
			if(importScriptString != null) {
				
				//set the appropriate values
				String currentTime = new Date().toString();
				importScriptString = importScriptString.replace(ImportSpecDataManager.TIMESTAMP_TOKEN, currentTime);
				
				//update the template with the name of the source repository
				importScriptString = importScriptString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);
				
				//add the script for the global instances
				GlobalInstancesScript globInstances = (GlobalInstancesScript) this.globalInstances;
				importScriptString = importScriptString + globInstances.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, 0, theMessageListener);
				
				
				//add the script for the worksheets
				Iterator<WorksheetImportSpec> worksheetIter = this.getWorksheetImportSpec().iterator();
				
				while(worksheetIter.hasNext()) {
						
						WorksheetImportSpecScript wkImportSpec = (WorksheetImportSpecScript) worksheetIter.next();
						theMessageListener.receiveImportSpecScriptMessage("Processing Worksheet: " + wkImportSpec.getName());
						
						importScriptString = importScriptString + wkImportSpec.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, 0, theMessageListener);
						theMessageListener.receiveImportSpecScriptMessage("Completed Worksheet: " + wkImportSpec.getName());
						//System.out.println("GENERATED WORKSHEET SCRIPT:" + importScriptString);
				}
				
			}
		} 
		
		return importScriptString;
	}
	
	
	/*
	public String printNextImportScriptChunk(String srcRepositoryName, Spreadsheet spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int chunkSize) {
		//initialise the import script chunk string
		String importScriptChunkString = "";

		if(this.getWorksheetImportSpec().size() <= 0) {
			this.rowsCompleted = 0;
			return importScriptChunkString;
		}
		
		//If this is the first worksheet, initialise it and print its header script
		WorksheetImportSpecScript wkImportSpec;
		if(this.currentWorksheetIndex < 0) {
			this.currentWorksheetIndex = 0;
			wkImportSpec = (WorksheetImportSpecScript) this.getWorksheetImportSpec().get(currentWorksheetIndex);
			wkImportSpec.initWorksheetImportScriptPrint();
			this.currentStatusString = "Begin generating import script for " + wkImportSpec.getName() + " worksheet...";
		//	importScriptChunkString = importScriptChunkString + wkImportSpec.printWorksheetHeaderImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap);
		} else {
			wkImportSpec = (WorksheetImportSpecScript) this.getWorksheetImportSpec().get(currentWorksheetIndex);
			this.currentStatusString = "Generating import script for " + wkImportSpec.getName() + " worksheet...";
		}		

			
		//get the next chunk of import script for the current Worksheet

		importScriptChunkString = importScriptChunkString + wkImportSpec.printNextImportScriptChunk(srcRepositoryName, spreadsheet, scriptTemplateMap, chunkSize);
		int lastRowsCompleted = wkImportSpec.getLastRowsCompleted();
		this.rowsCompleted = rowsCompleted + lastRowsCompleted;
		this.currentStatusString = "Generating import script for " + wkImportSpec.getName() + " worksheet...\n";
		
	//	System.out.println("COMPLETED ROWS SO FAR: " + rowsCompleted);
		
		if(wkImportSpec.worksheetImportScriptComplete()) {
			this.currentStatusString = "Import script for " + wkImportSpec.getName() + " worksheet complete.";
			this.currentWorksheetIndex++;
			if(currentWorksheetIndex < this.getWorksheetImportSpec().size()) {
				wkImportSpec = (WorksheetImportSpecScript) this.getWorksheetImportSpec().get(currentWorksheetIndex);
				wkImportSpec.initWorksheetImportScriptPrint();
			}
		}
		
		return importScriptChunkString;
			
	}
	*/
	
	
	public String printNextImportScriptChunk(String srcRepositoryName, Workbook spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int chunkSize, ImportSpecScriptListener theMessageListener) {
		//initialise the import script chunk string
		String importScriptChunkString = "";

		if(this.getWorksheetImportSpec().size() <= 0) {
			this.rowsCompleted = 0;
			return importScriptChunkString;
		}
		
		//If this is the first worksheet, initialise it and print its header script
		WorksheetImportSpecScript wkImportSpec;
		if(this.currentWorksheetIndex < 0) {
			this.currentWorksheetIndex = 0;
			wkImportSpec = (WorksheetImportSpecScript) this.getWorksheetImportSpec().get(currentWorksheetIndex);
			wkImportSpec.initWorksheetImportScriptPrint();
			this.currentStatusString = "Begin generating import script for " + wkImportSpec.getName() + " worksheet...";
		//	importScriptChunkString = importScriptChunkString + wkImportSpec.printWorksheetHeaderImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap);
		} else {
			wkImportSpec = (WorksheetImportSpecScript) this.getWorksheetImportSpec().get(currentWorksheetIndex);
			this.currentStatusString = "Generating import script for " + wkImportSpec.getName() + " worksheet...";
		}		

			
		//get the next chunk of import script for the current Worksheet

		importScriptChunkString = importScriptChunkString + wkImportSpec.printNextImportScriptChunk(srcRepositoryName, spreadsheet, scriptTemplateMap, chunkSize, theMessageListener);
		int lastRowsCompleted = wkImportSpec.getLastRowsCompleted();
		this.rowsCompleted = rowsCompleted + lastRowsCompleted;
		this.currentStatusString = "Generating import script for " + wkImportSpec.getName() + " worksheet...\n";
		
	//	System.out.println("COMPLETED ROWS SO FAR: " + rowsCompleted);
		
		if(wkImportSpec.worksheetImportScriptComplete()) {
			this.currentStatusString = "Import script for " + wkImportSpec.getName() + " worksheet complete.";
			this.currentWorksheetIndex++;
			if(currentWorksheetIndex < this.getWorksheetImportSpec().size()) {
				wkImportSpec = (WorksheetImportSpecScript) this.getWorksheetImportSpec().get(currentWorksheetIndex);
				wkImportSpec.initWorksheetImportScriptPrint();
			}
		}
		
		return importScriptChunkString;
			
	}
	
	
	
	/*public String printImportScriptHeader(String srcRepositoryName, Spreadsheet spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap) {
		String importScriptHeaderString = "";
		HashMap<String, String> modeTemplates = scriptTemplateMap.get(SpreadsheetImportSpecScript.TEMPLATE_NAME);
		
		if(modeTemplates != null) {
			//add the prefix script for the import spec
			importScriptHeaderString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			
			if(importScriptHeaderString != null) {
				//set the appropriate values
				String currentTime = new Date().toString();
				importScriptHeaderString = importScriptHeaderString.replace(ImportSpecDataManager.TIMESTAMP_TOKEN, currentTime);
				
				//update the template with the name of the source repository
				importScriptHeaderString = importScriptHeaderString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);
				
				//add the script for the global instances
				GlobalInstancesScript globInstances = (GlobalInstancesScript) this.globalInstances;
				importScriptHeaderString = importScriptHeaderString + globInstances.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, null);
				
			}
			return importScriptHeaderString;
		} else {
			System.err.println("ERROR: Failed to load Import Script Template token map.");
			return null;
		}
		
		
	}
	
	*/
	
	public String printImportScriptHeader(String srcRepositoryName, Workbook spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, ImportSpecScriptListener theMessageListener) {
		String importScriptHeaderString = "";
		HashMap<String, String> modeTemplates = scriptTemplateMap.get(SpreadsheetImportSpecScript.TEMPLATE_NAME);
		
		if(modeTemplates != null) {
			//add the prefix script for the import spec
			importScriptHeaderString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			
			if(importScriptHeaderString != null) {
				//set the appropriate values
				String currentTime = new Date().toString();
				importScriptHeaderString = importScriptHeaderString.replace(ImportSpecDataManager.TIMESTAMP_TOKEN, currentTime);
				
				//update the template with the name of the source repository
				importScriptHeaderString = importScriptHeaderString.replace(ImportSpecDataManager.REPOSITORY_NAME_TOKEN, srcRepositoryName);
				
				//add the script for the global instances
				GlobalInstancesScript globInstances = (GlobalInstancesScript) this.globalInstances;
				importScriptHeaderString = importScriptHeaderString + globInstances.printImportScript(srcRepositoryName, spreadsheet, null, scriptTemplateMap, 0, theMessageListener);
				
			}
			return importScriptHeaderString;
		} else {
			System.err.println("ERROR: Failed to load Import Script Template token map.");
			return null;
		}
		
		
	}
	
	
	public int getTotalImportRows() {
		if(this.totalRows == 0) {
			if(this.getWorksheetImportSpec() != null) {
				int totalRows = 0;
				Iterator<WorksheetImportSpec> worksheetIter = this.getWorksheetImportSpec().iterator();
				WorksheetImportSpecScript aWorksheet;
				while(worksheetIter.hasNext()) {
					aWorksheet = (WorksheetImportSpecScript) worksheetIter.next();
					int worksheetRows = aWorksheet.getTotalRows();
					if(worksheetRows >= 0) {
						totalRows = totalRows + worksheetRows;
					} else {
						return 0;
					}
				}
				this.totalRows = totalRows;
			} else {
				return 0;
			}
		}
		return this.totalRows;
	}
	
	
	public int getMaxRows() {
		if(this.getWorksheetImportSpec() != null) {
			int maxRows = 0;
			Iterator<WorksheetImportSpec> worksheetIter = this.getWorksheetImportSpec().iterator();
			WorksheetImportSpecScript aWorksheet;
			while(worksheetIter.hasNext()) {
				aWorksheet = (WorksheetImportSpecScript) worksheetIter.next();
				int worksheetRows = aWorksheet.getTotalRows();
				if(worksheetRows > maxRows) {
					maxRows = worksheetRows;
				}
			}
			return maxRows;
		} else {
			return 0;
		}
	}
	
	
	public boolean importScriptComplete() {
		if(this.getWorksheetImportSpec() == null || this.getWorksheetImportSpec().size() == 0) {
			return true;
		} 
		
		return (this.currentWorksheetIndex >= this.getWorksheetImportSpec().size());
	}
	
	
	public void initImportScriptPrint() {
		this.currentWorksheetIndex = -1;
		this.totalRows = this.getTotalImportRows();
		this.currentStatusString = "Importing " + this.totalRows + " rows";
		this.rowsCompleted = 0;
		this.percentageComplete = 0;
	}
	
	public int getPercentageComplete() {
		// System.out.println("INSIDE SPREADSHEET, ROWS COMPLETED: " + this.rowsCompleted + " OUT OF " + this.totalRows);
		if(rowsCompleted <= 0) {
			return 0;
		} else {
			float percentage = this.rowsCompleted / this.totalRows * 100;
			return Math.round(percentage);
		}
	}
	
	
	/**
	 * @return the currentStatusString
	 */
	public String getCurrentStatusString() {
		return currentStatusString;
	}
	
	
	/**
	 * @return the rowsCompleted
	 */
	public int getRowsCompleted() {
		return rowsCompleted;
	}

}
