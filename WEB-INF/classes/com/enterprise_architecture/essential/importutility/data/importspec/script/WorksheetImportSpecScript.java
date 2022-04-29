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
 * 23.08.2018	JWC	Migrate to ZK 8.5 and from ZSS to Apache POI
 * 
 */
package com.enterprise_architecture.essential.importutility.data.importspec.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//import org.zkoss.zss.model.Worksheet;
//import org.zkoss.zss.ui.Spreadsheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;

import com.enterprise_architecture.essential.importutility.data.common.DeleteDerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DeleteSimpleInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.common.SimpleInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.script.DeleteDerivedInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DeleteSimpleInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueRefScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedValueStringScript;
import com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;

/**
 * This class extends the WorksheetImportSpec class to provide the behaviours required to
 * generate the Essengtial Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class WorksheetImportSpecScript extends WorksheetImportSpec implements ImportSpecData {

	public static String TEMPLATE_NAME = "Worksheet";
	private int currentRow = -1;
	private int lastRowsCompleted = 0;
	private boolean scriptCompleted = false;
	
	public WorksheetImportSpecScript() {
		super();
	}
	
	public WorksheetImportSpecScript(WorksheetImportSpec worksheet) {
		super();
		
		this.firstRow = worksheet.getFirstRow();
		this.lastRow = worksheet.getLastRow();
		this.name = worksheet.getName();
		this.worksheetDescription = worksheet.getWorksheetDescription();
		this.sheetNo = worksheet.getSheetNo();
		this.sequenceNo = worksheet.getSequenceNo();
		this.imported = worksheet.isImported();
		
		this.derivedInstanceOrSimpleInstanceOrDeleteDerivedInstance = new ArrayList();
		if(worksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance() != null) {
			Iterator instanceIter = worksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().iterator();
			while(instanceIter.hasNext()) {
				Object instance = instanceIter.next();
				String instanceClassName = instance.getClass().getName();
				if(instanceClassName.endsWith(".SimpleInstanceType") || instanceClassName.endsWith(".SimpleInstanceTypeScript")) {
					SimpleInstanceType simpleInstance = (SimpleInstanceType) instance;
					SimpleInstanceTypeScript instanceScript = new SimpleInstanceTypeScript(simpleInstance);
					this.derivedInstanceOrSimpleInstanceOrDeleteDerivedInstance.add(instanceScript);
				} else if(instanceClassName.endsWith(".DeleteSimpleInstanceType") || instanceClassName.endsWith(".DeleteSimpleInstanceTypeScript")) {
					DeleteSimpleInstanceType delSimpleInstance = (DeleteSimpleInstanceType) instance;
					DeleteSimpleInstanceTypeScript instanceScript = new DeleteSimpleInstanceTypeScript(delSimpleInstance);
					this.derivedInstanceOrSimpleInstanceOrDeleteDerivedInstance.add(instanceScript);
				} else if(instanceClassName.endsWith(".DeleteDerivedInstanceType") || instanceClassName.endsWith(".DeleteDerivedInstanceTypeScript")) {
					DeleteDerivedInstanceType delDerivedInstance = (DeleteDerivedInstanceType) instance;
					DeleteDerivedInstanceTypeScript instanceScript = new DeleteDerivedInstanceTypeScript(delDerivedInstance);
					this.derivedInstanceOrSimpleInstanceOrDeleteDerivedInstance.add(instanceScript);
				} else if(instanceClassName.endsWith(".DerivedInstanceType") || instanceClassName.endsWith(".DerivedInstanceTypeScript")) {
					DerivedInstanceType derivedInstance = (DerivedInstanceType) instance;
					DerivedInstanceTypeScript instanceScript = new DerivedInstanceTypeScript(derivedInstance);
					this.derivedInstanceOrSimpleInstanceOrDeleteDerivedInstance.add(instanceScript);
				} else {
					System.out.println("ERROR: FAILED TO IDENTIFY TYPE OF INSTANCE NODE: " + instanceClassName);
				}
				
			}
		}
	}
	
	public static HashMap<String, String> getScriptTokens() {
		HashMap<String, String> myTokens = new HashMap<String, String>();
		myTokens.put("Worksheet Sequence No", ImportSpecDataManager.WORKSHEET_SEQUENCE_NO);
		myTokens.put("Worksheet Name", ImportSpecDataManager.WORKSHEET_NAME);
		myTokens.put("Worksheet Index No", ImportSpecDataManager.WORKSHEET_INDEX_NO);
		myTokens.put("Worksheet First Row", ImportSpecDataManager.WORKSHEET_FIRST_ROW);
		myTokens.put("Worksheet Last Row", ImportSpecDataManager.WORKSHEET_LAST_ROW);
		return myTokens;
	}
	
	
	/**
	 * @return WorksheetImportSpecScript a copy of this WorksheetImportSpecScript
	 */
	public WorksheetImportSpecScript getCopy() {
		return new WorksheetImportSpecScript(this);
	}
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		Iterator instances = this.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().iterator();
		while(instances.hasNext()) {
			ImportSpecData instance = (ImportSpecData) instances.next();
			instance.validate(invalidData);
		}
		return invalidData;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String myString = "Worksheet: ";
		if(this.getName() != null) {
		 return (myString + this.getName());
		} else {
			return myString + "<UNDEFINED>";
		}
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
		if(this.getName() == null || this.getName().length() == 0) {
			return false;
		}
		if(this.getFirstRow() == null || this.getFirstRow().doubleValue() <= 0) {
			return false;
		}
		if(this.getLastRow() == null || this.getLastRow().doubleValue() <= 0) {
			return false;
		}
		if(this.getLastRow().doubleValue() < this.getFirstRow().doubleValue()) {
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
			
				// return an empty string if this Worksheet is not to be imported
				if(!this.isImported()) {
					return "";
				}
				
				HashMap<String, String> modeTemplates = scriptTemplateMap.get(WorksheetImportSpecScript.TEMPLATE_NAME);
				String importScriptString = "";
		

				//set the name of the worksheet
				importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_NAME, this.getName());
				
			
				//set the index of the worksheet
				spreadsheet.setSelectedSheet(this.getName());
				Worksheet worksheet = spreadsheet.getSelectedSheet();
				int myIndex = spreadsheet.indexOfSheet(worksheet);
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_INDEX_NO, new Integer(myIndex).toString());
				
				//set the first row of the worksheet
				String firstRowString = this.getFirstRow().toString();
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_FIRST_ROW, firstRowString);

			
				//set the last row of the worksheet
				String lastRowString = this.getLastRow().toString();
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_LAST_ROW, lastRowString);
				
				
				HashMap<String, String> terminatorTemplates = scriptTemplateMap.get(ImportSpecDataManager.SCRIPT_TERMINATOR_TEMPLATE_NAME);
				
				String terminatorString = terminatorTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
				for(int row=this.getFirstRow().intValue();row <= this.getLastRow().intValue();row++) {

					//add the scripts for the instance import specs for this worksheet
					Iterator instanceIter = (Iterator) this.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().iterator();
					ImportSpecData anInstance;
					while(instanceIter.hasNext()) {
						anInstance = (ImportSpecData) instanceIter.next();
						importScriptString = importScriptString + anInstance.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, new Integer(row).toString());
						importScriptString = importScriptString + terminatorString;
					}
				}
				
				return importScriptString;
		}
	}
	*/
	
	
	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	@Override
	public String printImportScript(String srcRepositoryName, Workbook spreadsheet, Sheet aSheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int rowIndex, ImportSpecScriptListener theMessageListener) {
		if(!this.isValid()) {
			return null;
		} else {
			
				// return an empty string if this Worksheet is not to be imported
				if(!this.isImported()) {
					return "";
				}
				
				HashMap<String, String> modeTemplates = scriptTemplateMap.get(WorksheetImportSpecScript.TEMPLATE_NAME);
				String importScriptString = "";
		

				//set the name of the worksheet
				importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_NAME, this.getName());
				
			
				//set this to be the current worksheet
				System.out.println("GENERATING THIS WORKSHEET SCRIPT:" + this.getName());
								
				Sheet worksheet = spreadsheet.getSheet(this.getName());
				if(worksheet != null)
				{
					int myIndex = spreadsheet.getSheetIndex(worksheet);
					importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_INDEX_NO, new Integer(myIndex).toString());
					
					//set the first row of the worksheet
					String firstRowString = this.getFirstRow().toString();
					importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_FIRST_ROW, firstRowString);
	
				
					//set the last row of the worksheet
					String lastRowString = this.getLastRow().toString();
					importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_LAST_ROW, lastRowString);
					
					
					HashMap<String, String> terminatorTemplates = scriptTemplateMap.get(ImportSpecDataManager.SCRIPT_TERMINATOR_TEMPLATE_NAME);
					
					String terminatorString = terminatorTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
					for(int row=this.getFirstRow().intValue();row <= this.getLastRow().intValue();row++) {
	
						//add the scripts for the instance import specs for this worksheet
						Iterator instanceIter = (Iterator) this.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().iterator();
						ImportSpecData anInstance;
						while(instanceIter.hasNext()) {
							anInstance = (ImportSpecData) instanceIter.next();
							importScriptString = importScriptString + anInstance.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, row, theMessageListener);
							importScriptString = importScriptString + terminatorString;
						}
					}
				}
				else
				{
					importScriptString = "";
					theMessageListener.receiveImportSpecScriptMessage(ImportSpecScriptListener.WORKSHEET_NOT_FOUND_MESSAGE + this.getName());
				}
				return importScriptString;
		}
	}
	
	
	/*
	public String printChunkedImportScript(String srcRepositoryName, Spreadsheet spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int chunkSize) {
		if(!this.isValid()) {
			return null;
		} else {
				// return an empty string if this Worksheet is not to be imported
				if(!this.isImported()) {
					return "";
				}
				
				String importScriptString = this.printWorksheetHeaderImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap);
				
				importScriptString = importScriptString +  this.printNextImportScriptChunk(srcRepositoryName, spreadsheet, scriptTemplateMap, chunkSize);
				return importScriptString;
		}
	}
	*/
	
	/*
	public String printWorksheetHeaderImportScript(String srcRepositoryName, Spreadsheet spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap) {
		if(!this.isValid()) {
			return null;
		} else {
				// return an empty string if this Worksheet is not to be imported
				if(!this.isImported()) {
					return "";
				}
				
				HashMap<String, String> modeTemplates = scriptTemplateMap.get(WorksheetImportSpecScript.TEMPLATE_NAME);
				String importScriptString = "";
		
				//set the name of the worksheet
				importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_NAME, this.getName());

			
				//set the index of the worksheet
				spreadsheet.setSelectedSheet(this.getName());
				Worksheet worksheet = spreadsheet.getSelectedSheet();
				int myIndex = spreadsheet.indexOfSheet(worksheet);
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_INDEX_NO, new Integer(myIndex).toString());
				
				//set the first row of the worksheet
				String firstRowString = this.getFirstRow().toString();
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_FIRST_ROW, firstRowString);

			
				//set the last row of the worksheet
				String lastRowString = this.getLastRow().toString();
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_LAST_ROW, lastRowString);
			
				return importScriptString;
		}
	}
	*/
	
	/*
	public String printNextImportScriptChunk(String srcRepositoryName, Spreadsheet spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int chunkSize) {
		String importScriptString = "";
		
		// return an empty string if this Worksheet is not to be imported
		if(!this.isImported()) {
			return "";
		}
		
		if(this.currentRow < 0) {
			this.initWorksheetImportScriptPrint();
			importScriptString =  this.printWorksheetHeaderImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap);
			this.currentRow = this.getFirstRow().intValue();
		}
		
		HashMap<String, String> terminatorTemplates = scriptTemplateMap.get(ImportSpecDataManager.SCRIPT_TERMINATOR_TEMPLATE_NAME);
		String terminatorString = terminatorTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
		
		int firstChunkRow = this.currentRow;
		int lastChunkRow;
		if(this.getLastRow().intValue() > (firstChunkRow + chunkSize)) {
			lastChunkRow = firstChunkRow + chunkSize - 1;
			this.lastRowsCompleted = chunkSize;
		} else {
			lastChunkRow = this.getLastRow().intValue();
			this.lastRowsCompleted = lastChunkRow - firstChunkRow + 1;
		}
		
		for(int row=firstChunkRow;row <= lastChunkRow;row++) {

			//add the scripts for the instance import specs for this worksheet
			Iterator instanceIter = (Iterator) this.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().iterator();
			ImportSpecData anInstance;
			while(instanceIter.hasNext()) {
				anInstance = (ImportSpecData) instanceIter.next();
				importScriptString = importScriptString + anInstance.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, new Integer(row).toString());
				importScriptString = importScriptString + terminatorString;
			}
		}
		
		this.currentRow = lastChunkRow + 1;
		
		return importScriptString;
	}
	*/
	
	public int getTotalRows() {
		if(!this.isImported()) {
			return 0;
		}
		
		if(this.getFirstRow() != null && this.getLastRow() != null) {
			return this.getLastRow().intValue() - this.getFirstRow().intValue() + 1;
		} else {
			return 0;
		}
	}
	
	
	
	public String printWorksheetHeaderImportScript(String srcRepositoryName, Workbook spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, ImportSpecScriptListener theMessageListener) {
		if(!this.isValid()) {
			return null;
		} else {
				// return an empty string if this Worksheet is not to be imported
				if(!this.isImported()) {
					return "";
				}
				
				HashMap<String, String> modeTemplates = scriptTemplateMap.get(WorksheetImportSpecScript.TEMPLATE_NAME);
				String importScriptString = "";
		
				//set the name of the worksheet
				importScriptString = modeTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
				importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_NAME, this.getName());

			
				//set the index of the worksheet
				Sheet worksheet = spreadsheet.getSheet(this.getName());
				if (worksheet != null)
				{
					int myIndex = spreadsheet.getSheetIndex(worksheet);
					importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_INDEX_NO, new Integer(myIndex).toString());
					
					//set the first row of the worksheet
					String firstRowString = this.getFirstRow().toString();
					importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_FIRST_ROW, firstRowString);
	
				
					//set the last row of the worksheet
					String lastRowString = this.getLastRow().toString();
					importScriptString = importScriptString.replace(ImportSpecDataManager.WORKSHEET_LAST_ROW, lastRowString);
				}
				else
				{
					importScriptString = "";
					theMessageListener.receiveImportSpecScriptMessage(ImportSpecScriptListener.WORKSHEET_NOT_FOUND_MESSAGE + this.getName());
				}
				return importScriptString;
		}
	}
	
	
	
	public String printNextImportScriptChunk(String srcRepositoryName, Workbook spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int chunkSize, ImportSpecScriptListener theMessageListener) {
		String importScriptString = "";
		
		// return an empty string if this Worksheet is not to be imported
		if(!this.isImported()) {
			return "";
		}
		
		if(this.currentRow < 0) {
			this.initWorksheetImportScriptPrint();
			importScriptString =  this.printWorksheetHeaderImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, theMessageListener);
			this.currentRow = this.getFirstRow().intValue();
		}
		
		Sheet worksheet = spreadsheet.getSheet(this.getName());
		if(worksheet != null)
		{
			HashMap<String, String> terminatorTemplates = scriptTemplateMap.get(ImportSpecDataManager.SCRIPT_TERMINATOR_TEMPLATE_NAME);
			String terminatorString = terminatorTemplates.get(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
			
			int firstChunkRow = this.currentRow;
			int lastChunkRow;
			if(this.getLastRow().intValue() > (firstChunkRow + chunkSize)) {
				lastChunkRow = firstChunkRow + chunkSize - 1;
				this.lastRowsCompleted = chunkSize;
			} else {
				lastChunkRow = this.getLastRow().intValue();
				this.lastRowsCompleted = lastChunkRow - firstChunkRow + 1;
			}
			
			for(int row=firstChunkRow;row <= lastChunkRow;row++) {
	
				//add the scripts for the instance import specs for this worksheet
				Iterator instanceIter = (Iterator) this.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance().iterator();
				ImportSpecData anInstance;
				while(instanceIter.hasNext()) {
					anInstance = (ImportSpecData) instanceIter.next();
					importScriptString = importScriptString + anInstance.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, row, theMessageListener);
					importScriptString = importScriptString + terminatorString;
				}
			}
			this.currentRow = lastChunkRow + 1;
			
		}
		else
		{
			importScriptString = "";
			theMessageListener.receiveImportSpecScriptMessage(ImportSpecScriptListener.WORKSHEET_NOT_FOUND_MESSAGE + this.getName());
			
			// Indicate that the script for this sheet is complete, since we couldn't find it
			this.currentRow = this.getLastRow().intValue() + 1;
		}
		
		return importScriptString;
	}
	
	
	public boolean worksheetImportScriptComplete() {
		// return an empty string if this Worksheet is not to be imported
		if(!this.isImported()) {
			return true;
		} else {
			return (this.currentRow > this.getLastRow().intValue());
		}
	}
	
	public void initWorksheetImportScriptPrint() {
		this.currentRow = -1;
		lastRowsCompleted = 0;
		scriptCompleted = false;
	}
	
	
	public int getLastRowsCompleted() {
		if(!this.isImported()) {
			return 0;
		} else {
			return lastRowsCompleted;
		}
	}
	
	
}
