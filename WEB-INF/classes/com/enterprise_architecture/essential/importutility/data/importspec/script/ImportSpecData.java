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
 * 15.07.2011	JP	1st coding.
 * 18.06.2014	JWC	Added listener to printImportScript
 * 23.08.2018	JWC Migration to ZK 8.5
 * 
 */
package com.enterprise_architecture.essential.importutility.data.importspec.script;

import java.util.HashMap;
import java.util.List;

//import org.zkoss.zss.ui.Spreadsheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * This interface defines the behaviour required by the elements contained within an Import
 * Specification
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 15.07.2011<br/>
 * @version 2.0 - May-June 2014
 *
 */
public interface ImportSpecData {
	
	public List<ImportSpecData> validate(List<ImportSpecData> invalidData);
	public int getElementLevel();
	public boolean isValid();
	public Object getCopy();
	//public String printImportScript(String srcRepositoryName, Spreadsheet spreadsheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, String rowTitle);
	public String printImportScript(String srcRepositoryName, Workbook spreadsheet, Sheet worksheet, HashMap<String, HashMap<String, String>> scriptTemplateMap, int rowIndex, ImportSpecScriptListener theMessageListener);
}
