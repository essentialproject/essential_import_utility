/**
    * Copyright (c)2014 Enterprise Architecture Solutions ltd.
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
	* Class to provide a CSV to Excel adapter.
	* 07.04.2014	JWC	Version 1.0 started
 */
package com.enterprise_architecture.essential.importutility.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Convert a CSV document into an Excel .XLSX file with a single worksheet corresponding to the CSV file.
 * @author Jonathan W. Carter
 *
 */
public class CSVToExcelConverter 
{
	/**
	 * Construct a new instance of the converter from the specified InputStream, 
	 */
	public CSVToExcelConverter()
	{

	}
	
	/**
	 * Convert a CSV file in the specified InputStream into an Excel XLSX document with a single worksheet
	 * that is named by the specified sheet name.
	 * @param theCSV a stream to the CSV to be converted
	 * @param theSheetName the name of the worksheet in the workbook for the CSV
	 * @return a new Excel XSLX format workbook
	 */
	public XSSFWorkbook convert(InputStream theCSV, String theSheetName)
	{
		XSSFWorkbook aNewWorkbook = new XSSFWorkbook();
		
		String aCSVRow;
		try
		{
			// Parse the CSV, line by line
			InputStreamReader aStreamReader = new InputStreamReader(theCSV);
			BufferedReader aCSVReader = new BufferedReader(aStreamReader);
				
			// Create a new workbook for the CSV, and a new worksheet
			XSSFSheet aNewSheet = aNewWorkbook.createSheet(theSheetName);
			int aRowIndex = 0;
			while((aCSVRow = aCSVReader.readLine()) != null)
			{
				// Create a row per line
				XSSFRow aRow = aNewSheet.createRow(aRowIndex);
				String aCellArray[] = aCSVRow.split(",");
				
				// Cell by cell, tokenized by the ','				
				for(int aColumnIndex = 0; aColumnIndex < aCellArray.length; aColumnIndex++)
				{
					XSSFCell aCell = aRow.createCell(aColumnIndex);
					aCell.setCellValue(aCellArray[aColumnIndex]);				
				}
				aRowIndex++;
			}
		}
		catch(IOException anIOex)
		{
			System.err.println("Essential CSVToExcelConverter error reading source CSV content");
			anIOex.printStackTrace(System.err);
		}
		
		// return new workook
		return aNewWorkbook;
	}

	/**
	 * Convert a CSV file in the specified String into an Excel XLSX document with a single worksheet
	 * that is named by the specified sheet name.
	 * @param theCSV a string holding the CSV to be converted
	 * @param theSheetName the name of the worksheet in the workbook for the CSV
	 * @return a new Excel XSLX format workbook
	 */
	public XSSFWorkbook convert(String theCSV, String theSheetName)
	{
		return convert(IOUtils.toInputStream(theCSV), theSheetName);
	}

}
