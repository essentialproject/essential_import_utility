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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
//import org.zkoss.zss.ui.Spreadsheet;

import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDatas;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;

/**
 * This class extends the DerivedValue class to provide the behaviours required to
 * generate the Essengtial Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011<br/>
 * @version 2.0 - 23.08.2018
 *
 */
public class DerivedValueRefScript extends DerivedValueRef implements ImportSpecData {

	public DerivedValueRefScript() {
		super();
	}
	
	public DerivedValueRefScript(DerivedValueRef aValueRef) {
		super();
		this.value = aValueRef.getValue();
		this.postDelimiter = aValueRef.getPostDelimiter();
		this.preDelimiter = aValueRef.getPreDelimiter();
	}
	
	
	
	/**
	 * @return DerivedValueRefScript a copy of this DerivedValueRefScript
	 */
	public DerivedValueRefScript getCopy() {
		return new DerivedValueRefScript(this);
	}
	
	
	@Override
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData) {
		// TODO Auto-generated method stub
		if((this.value == null) || (this.value.length() == 0)) {
			invalidData.add(this);
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
		if((this.value == null) || (this.value.length() == 0) || (this.value.equals(ImportSpecDataManager.UNDEFINED_VALUE))) {
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
			//get the contents of the cell referred to by this value
			String cellContent = ImportSpecDatas.getCellValue(spreadsheet, currentRow, this.getValue());
			if(cellContent == null || cellContent.length() == 0) {
				return null;
			}
			
			//extract the content required if a pre-delimeter is set
			if(this.preDelimiter != null  && this.preDelimiter.length() > 0) {
				int delimiterIndex = cellContent.indexOf(this.preDelimiter);
				if(delimiterIndex>=0) {		
					cellContent = cellContent.substring(0, delimiterIndex);
				}
			}
			
			//extract the content required if a post-delimeter is set
			if(this.postDelimiter != null && this.postDelimiter.length() > 0) {
				int delimiterIndex = cellContent.indexOf(this.postDelimiter);
				if(delimiterIndex>=0) {		
					cellContent = cellContent.substring(delimiterIndex + this.preDelimiter.length() + 2);
				}
			}
			
			String cleanCellContent = ImportSpecDatas.removeSpecialCharacters(cellContent);
			
			return cleanCellContent;
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
			//get the contents of the cell referred to by this value
			String cellContent = ImportSpecDatas.getCellValue(worksheet, rowIndex, this.getValue());
			if(cellContent == null || cellContent.length() == 0) {
				return null;
			}
			
			//extract the content required if a pre-delimeter is set
			if(this.preDelimiter != null  && this.preDelimiter.length() > 0) {
				int delimiterIndex = cellContent.indexOf(this.preDelimiter);
				if(delimiterIndex>=0) {		
					cellContent = cellContent.substring(0, delimiterIndex);
				}
			}
			
			//extract the content required if a post-delimeter is set
			if(this.postDelimiter != null && this.postDelimiter.length() > 0) {
				int delimiterIndex = cellContent.indexOf(this.postDelimiter);
				if(delimiterIndex>=0) {		
					cellContent = cellContent.substring(delimiterIndex + this.preDelimiter.length() + 2);
				}
			}
			
			String cleanCellContent = ImportSpecDatas.removeSpecialCharacters(cellContent);
			
			return cleanCellContent;
		}
	}
	
}
