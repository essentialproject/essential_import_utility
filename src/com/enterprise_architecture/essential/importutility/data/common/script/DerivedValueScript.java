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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
//import org.zkoss.zss.ui.Spreadsheet;

import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDatas;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;

/**
 * This class extends the DerivedValue class to provide the behaviours required to
 * generate the Essential Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 * @version 2.0 - 23.08.2018
 *
 */
public class DerivedValueScript extends DerivedValue implements ImportSpecData {

	public DerivedValueScript() {
		super();
	}
	
	public DerivedValueScript(DerivedValue aDerivedValue) {
		super();
		if(aDerivedValue != null) {
			this.derivedValueStringOrDerivedValueRef = new ArrayList();
			if(aDerivedValue.getDerivedValueStringOrDerivedValueRef() != null) {
				Iterator segmentsIter = aDerivedValue.getDerivedValueStringOrDerivedValueRef().iterator();
				while(segmentsIter.hasNext()) {
					Object segment = segmentsIter.next();
					String segmentClassName = segment.getClass().getName();
					if(segmentClassName.endsWith(".DerivedValueString") || segmentClassName.endsWith(".DerivedValueStringScript")) {
						DerivedValueString valueString = (DerivedValueString) segment;
						DerivedValueStringScript stringValue = new DerivedValueStringScript(valueString);
						this.derivedValueStringOrDerivedValueRef.add(stringValue);
					} else {
						DerivedValueRef valueRef = (DerivedValueRef) segment;
						DerivedValueRefScript refValue = new DerivedValueRefScript(valueRef);
						this.derivedValueStringOrDerivedValueRef.add(refValue);
					}
					
				}
			}
		}
	}
	
	
	/**
	 * @return DerivedValueScript a copy of this DerivedValueScript
	 */
	public DerivedValueScript getCopy() {
		return new DerivedValueScript(this);
	}
	
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		Iterator segmentsIter = this.getDerivedValueStringOrDerivedValueRef().iterator();
		while(segmentsIter.hasNext()) {
			ImportSpecData segment = (ImportSpecData )segmentsIter.next();
			segment.validate(invalidData);
		}
		return invalidData;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#getElementLevel()
	 */
	@Override
	public int getElementLevel() {
		// TODO Auto-generated method stub
		return ImportSpecDataManager.VALUE_ELEMENT_LEVEL;
	}

	/* (non-Javadoc)
	 * @see com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData#isValid()
	 */
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		if(this.getDerivedValueStringOrDerivedValueRef().size() == 0) {
			return false;
		} else {
			Iterator segmentsIter = this.getDerivedValueStringOrDerivedValueRef().iterator();
			while(segmentsIter.hasNext()) {
				ImportSpecData segment = (ImportSpecData )segmentsIter.next();
				if(!segment.isValid()) {
					return false;
				}
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
			//initialise the import script string
			String importScriptString = "";
			
			//add the scripts for the segments of this derived value
			Iterator segmentIter = (Iterator) this.getDerivedValueStringOrDerivedValueRef().iterator();
			ImportSpecData aSegment;
			String segmentContent;
			while(segmentIter.hasNext()) {
				aSegment = (ImportSpecData) segmentIter.next();
				segmentContent =  aSegment.printImportScript(srcRepositoryName, spreadsheet, scriptTemplateMap, currentRow);
				if(segmentContent == null) {
					return null;
				} else {
					importScriptString = importScriptString + segmentContent;
				}
			}
			return importScriptString;
			//return ImportSpecDatas.removeSpecialCharacters(importScriptString);
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
			//initialise the import script string
			String importScriptString = "";
			
			//add the scripts for the segments of this derived value
			Iterator segmentIter = (Iterator) this.getDerivedValueStringOrDerivedValueRef().iterator();
			ImportSpecData aSegment;
			String segmentContent;
			while(segmentIter.hasNext()) {
				aSegment = (ImportSpecData) segmentIter.next();
				segmentContent =  aSegment.printImportScript(srcRepositoryName, spreadsheet, worksheet, scriptTemplateMap, rowIndex, theMessageListener);
				if(segmentContent == null) {
					return null;
				} else {
					importScriptString = importScriptString + segmentContent;
				}
			}
			return importScriptString;
			//return ImportSpecDatas.removeSpecialCharacters(importScriptString);
		}
	}
}
