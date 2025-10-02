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
 * 16.05.2014	JWC first coding
 *  
 */
package com.enterprise_architecture.essential.importutility.utils;

import java.util.logging.Logger;

/**
 * Class to manage the configuration of logging using java.util.Logger
 * @author Jonathan Carter
 * @version 1
 *
 */
public class Log 
{
	/**
	 * Name of the system logger for the Import Utility
	 */
	public static String ESSENTIAL_IMPORT_LOGGER_NAME = "essentialimport.system";
	
	/**
	 * Handle to the Logger for the Import Utility
	 */
	private static Logger itsLogger;
	
	/**
	 * Use the static functions to get to the Logger
	 */
	private Log()
	{
		
	}
	
	/**
	 * Get a reference to the Essential Import Utility logger.
	 * If it has not been created, create it.
	 * @return a reference to the Logger for the Import Utility
	 */
	public static Logger getSystemLogger()
	{
		if(itsLogger == null)
		{
			itsLogger = Logger.getLogger(ESSENTIAL_IMPORT_LOGGER_NAME);
		}
		return itsLogger;
	}
}
