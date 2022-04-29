/**
 * Copyright (c)2014 Enterprise Architecture Solutions Ltd.
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
 * 18.06.2014	JWC	1st coding.
 */
package com.enterprise_architecture.essential.importutility.data.importspec.script;

/**
 * Interface that provides the import specification script worker classes with a mechanism to send
 * detailed message strings to any registered listener
 * 
 * @author Jonathan Carter
 *
 */
public interface ImportSpecScriptListener 
{
	/**
	 * Receive a string message from an import specification script builder worker class, e.g. to 
	 * report back to the GUI.
	 * @param theMessage the message from the import specification script building class
	 */
	public void receiveImportSpecScriptMessage(String theMessage);
	
	/**
	 * Constant string for the worksheet not found message.
	 */
	String WORKSHEET_NOT_FOUND_MESSAGE = "WARNING: Source spreadsheet does not contain worksheet: ";
	
}
