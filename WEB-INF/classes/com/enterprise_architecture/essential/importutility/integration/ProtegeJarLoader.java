/**
 * Copyright (c)2009-2014 Enterprise Architecture Solutions Ltd.
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
 * 31.03.2012	JP	1st coding.
 * 
 */
package com.enterprise_architecture.essential.importutility.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * This class is responsible for dynamically loading the protege jar file
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 31.03.2012
 *
 */
public class ProtegeJarLoader {
	private static String PROTEGE_JAR_FOLDER_NAME = "/dist/";
	private static String PROTEGE_PROPERTIES_FILEPATH = "/config/protege.properties";
	private static String PROTEGE_JAR_PROPERTY_NAME = "protege.jar.filename";
	
	
	public static boolean loadProtegeJar(String contextPath) {
		
		// create and load default properties
		try {
		String propertiesFullPath = contextPath + PROTEGE_PROPERTIES_FILEPATH;
		Properties protegeProps = new Properties();
		FileInputStream in = new FileInputStream(propertiesFullPath);
		protegeProps.load(in);
		in.close();
		
		String protegeJarFilename = protegeProps.getProperty(PROTEGE_JAR_PROPERTY_NAME);
		String jarFileFullPath = contextPath + PROTEGE_JAR_FOLDER_NAME + protegeJarFilename;
		
		// Create a File object based on the path the given protege jar file
		File file = new File(jarFileFullPath);
		
		// Convert File to a URL
		URI uri = file.toURI();
	    URL url = uri.toURL();       
	    URL[] urls = new URL[]{url};
	    
	    URLClassLoader protegeJarLoader = new URLClassLoader(urls);
	    protegeJarLoader.loadClass("edu.stanford.smi.protege.model.Cls");
	    protegeJarLoader.loadClass("edu.stanford.smi.protege.model.KnowledgeBase");
	    protegeJarLoader.loadClass("edu.stanford.smi.protege.model.Project");
	    protegeJarLoader.loadClass("edu.stanford.smi.protege.model.Slot");
	    protegeJarLoader.loadClass("edu.stanford.smi.protege.server.RemoteProjectManager");
	    protegeJarLoader.loadClass("edu.stanford.smi.protege.storage.clips.ClipsFilesExportProjectPlugin");
	    protegeJarLoader.loadClass("edu.stanford.smi.protege.util.ArchiveManager");
		
		} 
		catch(IOException e) {
			System.out.println("Error loading Protege properties file");
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			System.out.println("Error loading Protege classes from jar file");
			e.printStackTrace();
		}
		
		return true;
	}

}
