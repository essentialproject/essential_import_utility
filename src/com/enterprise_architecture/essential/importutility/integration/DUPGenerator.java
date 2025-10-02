/**
 * Copyright (c)2009-2017 Enterprise Architecture Solutions Ltd.
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
 * 18.012.2014	JP	1st coding.
 * 19.04.2017	JWC	Added XML-based EUP generation capability
 * 
 */
package com.enterprise_architecture.essential.importutility.integration;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.utils.Log;

 
public class DUPGenerator {
	
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();
	
 
	/**
	 * Generate an EUP file based on the given import script
	 * @param importScript
	 * @param contextPath
	 */
	public String generateDUP(ImportActivity impAct, String importScript, String contextPath) {
 
		String aFullPathToDUPPackage = "";
		try
		{
			String aDUPPackagePath = "";
			DUPPackage aPackage = new DUPPackage(contextPath);
			aDUPPackagePath = aPackage.createDUPPackage(impAct, importScript, DUPPackage.ADD_TIMESTAMP);	
			aFullPathToDUPPackage = FilenameUtils.concat(contextPath, aDUPPackagePath);
		 
		}
		catch(Exception e) {
			iuLog.log(Level.SEVERE, "Exception in generateDUP()", e);
			e.printStackTrace();
		}
		return aFullPathToDUPPackage;
	 
	}
	
	/**
	 * Generate an XML-based DUP file based on the given import script
	 * @param importScript
	 * @param contextPath
	 */
	public String generateXMLDUP(ImportActivity impAct, String importScript, String contextPath) 
	{
 
		String aFullPathToDUPPackage = "";
		try
		{
			String aDUPPackagePath = "";
			DUPPackage aPackage = new DUPPackage(contextPath);
			aDUPPackagePath = aPackage.createXMLDUPPackage(impAct, importScript, DUPPackage.ADD_TIMESTAMP);	
			aFullPathToDUPPackage = FilenameUtils.concat(contextPath, aDUPPackagePath);
		 
		}
		catch(Exception e) {
			iuLog.log(Level.SEVERE, "Exception in generateDUP()", e);
			e.printStackTrace();
		}
		return aFullPathToDUPPackage;
	 
	}
}
 
