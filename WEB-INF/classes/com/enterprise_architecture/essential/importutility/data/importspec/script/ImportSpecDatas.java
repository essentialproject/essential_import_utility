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
 * 12.05.2014	JP / JWC 	Re-implemented using Apache POI
 * 23.08.2018	JWC	Removed use of ZSS library
 * 
 */
package com.enterprise_architecture.essential.importutility.data.importspec.script;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import com.enterprise_architecture.essential.importutility.utils.UTF8ToAscii;


//import org.zkoss.poi.ss.usermodel.Cell;
//import org.zkoss.poi.ss.usermodel.RichTextString;
//import org.zkoss.zss.model.Range;
//import org.zkoss.zss.model.Ranges;
//import org.zkoss.zss.model.Worksheet;
//import org.zkoss.zss.ui.Spreadsheet;
//import org.zkoss.zss.ui.impl.Utils;


/**
 * This is an abstract class providing utility methods for generating import scripts
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 25.07.2011
 *
 */
public abstract class ImportSpecDatas {

	
	/*public static int getRowIndexFromTitle(Spreadsheet aSS, String rowTitle) {
		int rowIndex = 0;
		while(rowIndex  < 10000) {
			if (aSS.getRowtitle(rowIndex).equals(rowTitle)) {
				return rowIndex ;
			}
			rowIndex++;
		}
		return -1;
	}
	*/
	
	/*public static int getColumnIndexFromTitle(Spreadsheet aSS, String columnTitle) {
		int columnIndex = 0;
		while(columnIndex  <10000) {
			if (aSS.getColumntitle(columnIndex).equals(columnTitle)) {
				return columnIndex ;
			}
			columnIndex++;
		}
		return -1;
	}
	*/
	
	
	/*public static String getCellValue (Spreadsheet aSS, String rowTitle , String colTitle) {
		int rowIndex = new Integer(rowTitle).intValue() - 1;
		
		
		int col = ImportSpecDatas.getColumnIndexFromTitle(aSS, colTitle);
		int row = ImportSpecDatas.getRowIndexFromTitle(aSS, rowTitle);
		if(col >= 0 && row >= 0) {
			Worksheet worksheet = aSS.getSelectedSheet();
			Cell aCell = Utils.getCell(worksheet, row, col);
			Range currentRange = Ranges.range(worksheet, row, col);
			RichTextString cellText = currentRange.getText();
			if(cellText != null) {
				String cellValue = cellText.getString();
			// String cellValue = Utils.getCellText(worksheet, aCell);
				String cleanCellValue = ImportSpecDatas.removeSpecialCharacters(cellValue);
				return cleanCellValue;
			} else {
				return null;
			}
		}
		return null;
	}
	*/
	
	
	public static String getCellValue (Sheet aWorksheet, int rowIndex , String colTitle) {
		
		int aRowIndex = rowIndex - 1;
		Row thisRow = aWorksheet.getRow(aRowIndex);
		int columnIndex = CellReference.convertColStringToIndex(colTitle);
		
		if((thisRow != null) && (columnIndex >= 0)) {
			org.apache.poi.ss.usermodel.Cell theCell = thisRow.getCell(columnIndex);
			
			//System.out.println("Reading " + aWorksheet.getSheetName() + ":" + colTitle + aRowIndex + " from row " + Integer.valueOf(rowIndex).toString());
			// Handle the fact that the cell could be NULL
			if(theCell == null)
			{
				return null;
			}
			else
			{
				// Use the formatter to read any type of cell and get the text from it
				DataFormatter aFormatter = new DataFormatter();				
				String cellValue = "";
				
				// Handle formula cells using an evaluator - JWC 12.05.2014
				if(theCell.getCellType() == CellType.FORMULA)
				{
					FormulaEvaluator anEvaluator = aWorksheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
					cellValue = aFormatter.formatCellValue(theCell, anEvaluator);
				}
				else
				{
					cellValue = aFormatter.formatCellValue(theCell);
				}
				String cleanCellValue = ImportSpecDatas.removeSpecialCharacters(cellValue);				
				return cleanCellValue;
			}
		} else {
			return null;
		}
	}

	
	public static String removeSpecialCharacters(String inputString) {
        // String pattern = "[^A-Za-z 0-9 .,?'!@#$%^&*()-_=+;:<>|}{`~]*/\\\"\n\r";
        
        /*String pattern = "[^a-zA-Z0-9 .,?'!@#��$%^&*()-_=+;:<>|}{`~���������]";
       
        String strippedString = inputString.replaceAll(pattern, "");
        
		return strippedString;*/
		//String unicodeString =  UTF8ToAscii.unicodeEscape(inputString);
		//System.out.println(unicodeString);
		/*try{
		byte[] encodedString = inputString.getBytes("UTF-8");
		String stringToRender = Arrays.toString(encodedString);
		System.out.println(stringToRender);
		return stringToRender;
		}
		catch(Exception e) {
			e.printStackTrace();
			return "";
		}*/
		
		// 17.09.2014 JWC - perform the trim first to remove leading and trailing spaces
		String aTrimmedString = inputString.trim();
		String singleQuoteEscape = aTrimmedString.replace("'", "\'");
		String newLineEscape = singleQuoteEscape.replace("\n", "\\n");
		String tabEscape = newLineEscape.replace("\t", "\\t");
		
		// 03.10.2019 JWC - escape the " character properly
		String doubleQuoteEscape = tabEscape.replace("\"", "\\u0022");
		String carriageReturnEscape = doubleQuoteEscape.replace("\r", "\\n");
		
		// 04.10.2019 JWC - Handle double quotes
		String aDoubleBlackSlashEsc = carriageReturnEscape.replace("\\\\", "\\u005c\\u005c");
		
		// 03.10.2019 JWC - Escape the \backslash character		
		// Handle special cases where \N is incorrectly recognised as Unicode character
		String aCapNEscape = aDoubleBlackSlashEsc.replace("\\N", "\\u005cN");
		
		String unicodeString =  UTF8ToAscii.unicodeEscape(aCapNEscape);
		return unicodeString;
	}
	
	public static String removeNonAlpha(String inputString) {
        String pattern = "[^a-zA-Z}]";     
        String strippedString = inputString.replaceAll(pattern, "");

		return strippedString;
	}
	
	public static String removeNonNumeric(String inputString) {    
        String pattern = "[^0-9.,}]";
       
        String strippedString = inputString.replaceAll(pattern, "");
		return strippedString;
	}
	
	
	public static boolean containsSpecialCharacters(String inputString) {
		Pattern p = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(inputString);
		return m.find();
	}
	
	public static boolean isEmptyValue(String inputString) {
		return (inputString == null) || (inputString.length() == 0);
	}
	
	
}
