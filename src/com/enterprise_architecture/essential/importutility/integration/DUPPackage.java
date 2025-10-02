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
 * 20.06.2014	JWC	1st coding.
 * 13.04.2017	JWC	New package create method for XML DUPs
 */
package com.enterprise_architecture.essential.importutility.integration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.enterprise_architecture.essential.updatepack.Packdefinition;
import org.enterprise_architecture.essential.updatepack.Updatepack;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.utils.Log;

/**
 * Class to manage the creation of Data Update Packs that can be applied to Essential Repositories independently of the Import Utility .
 * Packages are saved as ZIP files that contain all the relevant components
 * @author Jason Powell
 * @author Jonathan Carter
 *
 */
public class DUPPackage 
{
	/**
	 * The import activity for which the DUP is being generated
	 */
	protected ImportActivity importActivity;
	
	
	/**
	 * The name of the directory in which the dup package standard files are located
	 */
	private static final String dupPackageTemplateLocation = "dup_generation";
	
	
	/**
	 * The name of the properties file for DUP generation
	 */
	private static final String dupPropertiesFilename = "dup_generation.properties";
	

	
	/**
	 * The name of manifest file template for the DUP package
	 *//*
	private static final String dupInfoFileTemplateName = "update.info";
	
	*//**
	 * The name of the folder within the DUP package that contains the import script files 
	 *//*
	private static final String dupScriptFolderName = "Scripts";
	
	
	*//**
	 * The default name of the import script file 
	 *//*
	private static final String dupImportScriptFilename = "dup_import_script.py";
	
	*//**
	 * The name of the xml schema file for DUP files 
	 *//*
	private static final String dupSchemaFilename = "updatepack.xsd";
	
	
	*//**
	 * The name of the common functions file for DUP files 
	 */
	//private static final String dupStandardFunctionsFolderName = "includes";
	
	/**
	 * The name of the common functions file for DUP files 
	 */
	private static final String dupStandardFunctionsFilename = "standardFunctions.py";
	
	
	/**
	 * The file extension suffix of the dup file 
	 *//*
	private static final String dupFileExtension = "dup";
	*/
	
	/**
	 * The token used in the manifest file for the Import Activity name 
	 */
	private static final String IMPORTACTIVITY_NAME_TOKEN = "@IMPORT ACTIVITY NAME@";
	
	/**
	 * The token used in the manifest file for the Import Activity description 
	 */
	private static final String IMPORTACTIVITY_DESC_TOKEN = "@IMPORT ACTIVITY DESC@";
	
	/**
	 * The token used in the manifest file for the Import Script file name 
	 */
	private static final String IMPORTACTIVITY_FILENAME_TOKEN = "@IMPORT SCRIPT FILENAME@";
	
	/**
	 * Name of the Update Pack XML package
	 */
	protected final static String XML_UPDATE_PACK_PACKAGE = "org.enterprise_architecture.essential.updatepack";
	
	/**
	 * The name of the directory in which the application is running. All paths are relative to this 
	 */
	protected String itsRootDirectory = "";
	
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();
	
	/**
	 * Constant value used to specify that the generated package file includes a timestamp
	 */
	public static final boolean ADD_TIMESTAMP = true;
	
	/**
	 * Constant value used to specify that the generated package file DOES NOT include a timestamp
	 */
	public static final boolean NO_TIMESTAMP = false;
	
	/**
	 * Name of the folder in which all import activities are located
	 */
	public static final String IMPORT_ACTIVITIES_FOLDER = "import_activities";
	
	/**
	 * Constant value used to specify that the generated package file DOES NOT include a timestamp
	 */
	protected Properties props;
	
	
	/**
	 * Constructor for the configuration packaging class.
	 * @param theRootDirectory the full path to the root directory in which this class 
	 * will operate.
	 */
	public DUPPackage(String theRootDirectory)
	{
		itsRootDirectory = theRootDirectory;
		props = new Properties();
		
	}
	
	/**
	 * Create an EUP file in a temporary location for the given import script
	 * @param theIsAddTimestamp = TRUE, then a timestamp is added to the package filename.
	 * @return the relative path and name of the created DUP file 
	 */
	public String createDUPPackage(ImportActivity impAct, String importScript, boolean theIsAddTimestamp)
	{	
		InputStream inputStream;
		try { 
			inputStream = getClass().getClassLoader().getResourceAsStream(dupPropertiesFilename);
			props.load(inputStream);
			inputStream.close();
		}
		catch(IOException ioEx) {
			iuLog.log(Level.ALL, "Failed to read DUP generation properties file: " + dupPropertiesFilename);
			iuLog.log(Level.ALL, ioEx.getMessage());
		}	
		
		//Get the required values from the properties file
		String dupInfoFileTemplateName = props.getProperty("dupInfoFileTemplateName");
		String dupImportScriptFilename = props.getProperty("dupImportScriptFilename");
		String dupFileExtension = props.getProperty("dupFileExtension");
		String dupSchemaFilename = props.getProperty("dupSchemaFilename");
		String dupStandardFunctionsFolderName = props.getProperty("dupStandardFunctionsFolderName");
		String dupStandardFunctionsFilename = props.getProperty("dupStandardFunctionsFilename");
		//String dupScriptFolderName = props.getProperty("dupScriptFolderName");
		
		
		//Create the formatted string for the date/time stamp
		SimpleDateFormat aSimpleFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String formattedDate = aSimpleFormat.format(new Date());
		
		//Create the string that represents the DUP manifest file and send the contents to a file
		String manifestTemplatePath = itsRootDirectory + File.separator + dupPackageTemplateLocation + File.separator + dupInfoFileTemplateName;
		String manifestContents = "";
		try {
			File manifestTemplate = new File(manifestTemplatePath);
			manifestContents = FileUtils.readFileToString(manifestTemplate);
		}
		catch(IOException anIOEx) {
			iuLog.log(Level.ALL, "Error encountered loading DUP manifest file template: " + manifestTemplatePath);
			iuLog.log(Level.ALL, anIOEx.getMessage());
		}
		//Replace the appropriate TOKENS in the manifest file template with the required values
		manifestContents = manifestContents.replaceAll(IMPORTACTIVITY_NAME_TOKEN, impAct.getImportActivityName() + " Data Import - " + formattedDate);
		manifestContents = manifestContents.replaceAll(IMPORTACTIVITY_DESC_TOKEN, impAct.getImportActivityDescription());
		manifestContents = manifestContents.replaceAll(IMPORTACTIVITY_FILENAME_TOKEN, dupImportScriptFilename);

		//System.out.println("MANIFEST CONTENTS: " + manifestContents);
		
		InputStream aManifestStream = new ByteArrayInputStream(manifestContents.getBytes(StandardCharsets.UTF_8));
		InputStream anImportScriptStream = new ByteArrayInputStream(importScript.getBytes(StandardCharsets.UTF_8));
		
		//Create the DUP pacjage file
		String aDUPFilename = "";
		String aSuffix = "dup";
		if(theIsAddTimestamp)
		{
			// Create timestamp-based suffix for the filename	
			aSuffix = "_data_update-" + formattedDate;
		}
		
		aDUPFilename = impAct.getImportActivityName().toLowerCase().replaceAll(" ", "_") + aSuffix + "." + dupFileExtension;
		
		String anArchiveBaseName = aDUPFilename;
		String aFullPathPrefix = itsRootDirectory + File.separator;
		String aFullPackagePath = aFullPathPrefix + anArchiveBaseName;	
		
		FileOutputStream aZipFile = null;
		try
		{
			aZipFile = FileUtils.openOutputStream(new File(aFullPackagePath), false);
			ZipOutputStream aZipArchive = new ZipOutputStream(aZipFile);
			
			// Add the components with relative paths, creating
			//DUP XML schema file
			addToArchive(aZipArchive, new File(aFullPathPrefix + dupPackageTemplateLocation + File.separator + dupSchemaFilename), "", false);
			//DUP common functions python file
			addToArchive(aZipArchive, new File(aFullPathPrefix + dupStandardFunctionsFolderName + File.separator + dupStandardFunctionsFilename), "", false);
			//DUP manifest file
			addStreamToArchive(aZipArchive, aManifestStream, dupInfoFileTemplateName);
			//DUP import script file
			addStreamToArchive(aZipArchive, anImportScriptStream, dupImportScriptFilename);

			//close the dup archive
			aZipArchive.close();	
			
		}
		catch(IOException anIOEx)
		{
			iuLog.log(Level.ALL, "Error encountered when creating dup file: " + anArchiveBaseName);
			iuLog.log(Level.ALL, anIOEx.getMessage());
		}
		finally
		{
			IOUtils.closeQuietly(aZipFile);
		}
		
		return anArchiveBaseName;
	}
	
	/**
	 * Create an EUP file for an XML import pack in a temporary location for the given import script
	 * @param theIsAddTimestamp = TRUE, then a timestamp is added to the package filename.
	 * @return the relative path and name of the created DUP file
	 * @param impAct the ImportActivity that will be used
	 * @param importScript the ImportScript that will be added to the pack
	 * @return the filename of the DUP file that has been created.
	 */
	public String createXMLDUPPackage(ImportActivity impAct, String importScript, boolean theIsAddTimestamp)
	{		
		// Read the template stuff in
		InputStream inputStream;
		try { 
			inputStream = getClass().getClassLoader().getResourceAsStream(dupPropertiesFilename);
			props.load(inputStream);
			inputStream.close();
		}
		catch(IOException ioEx) {
			iuLog.log(Level.ALL, "Failed to read DUP generation properties file: " + dupPropertiesFilename);
			iuLog.log(Level.ALL, ioEx.getMessage());
		}	
		
		//Get the required values from the properties file
		String dupInfoFileTemplateName = props.getProperty("dupInfoFileTemplateName");
		String dupImportScriptFilename = props.getProperty("dupImportScriptFilename");
		String dupFileExtension = props.getProperty("dupFileExtension");
		String dupSchemaFilename = props.getProperty("dupSchemaFilename");
		
		//Create the formatted string for the date/time stamp
		SimpleDateFormat aSimpleFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String formattedDate = aSimpleFormat.format(new Date());
		
		//Create the DUP package file
		String aDUPFilename = "";
		String aSuffix = "dup";
		if(theIsAddTimestamp)
		{
			// Create timestamp-based suffix for the filename	
			aSuffix = "_data_update-" + formattedDate;
		}
		
		aDUPFilename = impAct.getImportActivityName().toLowerCase().replaceAll(" ", "_") + aSuffix + "." + dupFileExtension;
		
		String anImportActivityRoot = impAct.getImportActivityRootDirName();
		String aDUPRootDir = IMPORT_ACTIVITIES_FOLDER + File.separator + anImportActivityRoot;
		String anArchiveBaseName = aDUPFilename;
		String aFullPathPrefix = itsRootDirectory + File.separator;
		String aFullPackagePath = aFullPathPrefix + anArchiveBaseName;			
		
		// Create a new ZIP archive
		FileOutputStream aZipFile = null;
		try
		{
			aZipFile = FileUtils.openOutputStream(new File(aFullPackagePath), false);
			ZipOutputStream aZout = new ZipOutputStream(aZipFile);
			File aSourceFile = new File(aFullPathPrefix + aDUPRootDir + File.separator);
			String anActivityLogPrefix = anImportActivityRoot + "_";
			
			// Create the update.info file 				
			// Create an JAXB object for the new update.info file
			Updatepack anUpdatePack = new Updatepack();
			anUpdatePack.setUpdatename(impAct.getImportActivityName());
			anUpdatePack.setUpdatedescription(impAct.getImportActivityDescription());			
						
			// Build list of files to consider adding to DUP package			
			addToArchive(aZout, aSourceFile, "", true, anUpdatePack, anActivityLogPrefix);
			
			// Add the import script
			InputStream anImportScriptStream = new ByteArrayInputStream(importScript.getBytes(StandardCharsets.UTF_8));
			addStreamToArchive(aZout, anImportScriptStream, anUpdatePack, dupImportScriptFilename , dupImportScriptFilename);
			
			// Write the Updatepack out and add it to the root of the Zip file
			String aPackXML = "";
			StringWriter aPackXMLWriter = new StringWriter();
			JAXBContext aContext = JAXBContext.newInstance(XML_UPDATE_PACK_PACKAGE);
			Marshaller aMarshaller = aContext.createMarshaller();
			aMarshaller.marshal(anUpdatePack, aPackXMLWriter);
			aPackXML = aPackXMLWriter.toString(); 
			
			// Add to the Zip
			InputStream aManifestStream = new ByteArrayInputStream(aPackXML.getBytes(StandardCharsets.UTF_8));
			addStreamToArchive(aZout, aManifestStream, dupInfoFileTemplateName);
						
			// Save the generated DUP
			aZout.close();					
		}
		catch(Exception e)
		{
			iuLog.log(Level.SEVERE, "Exception encountered whilst creating new DUP file", e);
		}
		finally
		{
			IOUtils.closeQuietly(aZipFile);
		}
		
		return anArchiveBaseName;
	}
	
	/**
	 * Add the source content to the target archive package. If the source content is a folder, this
	 * in addition to its children are added to the package.
	 * @param theTargetPackage the target archive in which to add the source file or directory
	 * @param theSourceContent the file or directory to add to the archive
	 * @param theIsAddDirContents = true, any contained files and subdirectories are also added.
	 */
	protected void addToArchive(ZipOutputStream theTargetPackage, File theSourceContent, String theParentDirectoryName, boolean theIsAddDirContents)
	{
		String aContentFilename = "";
		if(theParentDirectoryName.isEmpty())
		{
			aContentFilename = FilenameUtils.getName(theSourceContent.getName());
		}
		else
		{
			aContentFilename = theParentDirectoryName + File.separator + FilenameUtils.getName(theSourceContent.getName());
		}
		FileInputStream fin = null;
		
		try
		{
			// If theSource is a directory, add it 
			if(theSourceContent.isDirectory())
			{
				theTargetPackage.putNextEntry(new ZipEntry(aContentFilename + File.separator));
				theTargetPackage.closeEntry();
				
				if(theIsAddDirContents)
				{
					// Get all the subcomponents
					File[] aSourceFileList = theSourceContent.listFiles();
					for(int i = 0; i < aSourceFileList.length; i++)
					{
						File aNextFile = aSourceFileList[i];
						if(aNextFile != null)
						{
							addToArchive(theTargetPackage, aNextFile, aContentFilename, theIsAddDirContents);
						}
					}
																	
				}
			}
			else
			{
				// theSourceContent is just a file, so add that				
				fin = FileUtils.openInputStream(theSourceContent);
				addStreamToArchive(theTargetPackage, fin, aContentFilename);				
			}
		}
		catch(IOException anIOEx)
		{
			iuLog.log(Level.ALL, "Error encountered when creating configuration archive, adding file: " + theSourceContent.getName());
			iuLog.log(Level.ALL, anIOEx.getMessage());
		}
		finally
		{
			if(fin != null)
			{
				try
				{
					fin.close();
				}
				catch(IOException aCloseEx)
				{
					iuLog.log(Level.ALL, "Error encountered when creating configuration archive, closing file: " + theSourceContent.getName());
					iuLog.log(Level.ALL, aCloseEx.getMessage());
				}
			}
		}
	}

	private void addStreamToArchive(ZipOutputStream theTargetPackage, InputStream fin, String aContentFilename) throws IOException {
		theTargetPackage.putNextEntry(new ZipEntry(aContentFilename));
		IOUtils.copy(fin, theTargetPackage);	
		theTargetPackage.closeEntry();
	}
	
	/**
	 * Override of the addToArchive() method that takes the current update.info object of the DUP and updates this 
	 * as the archive of the DUP itself is built.
	 * @param theTargetPackage the ZIP file that will contain the DUP
	 * @param theSourceContent the File from which the archiver will start
	 * @param theParentDirectoryName the 'root' directory in which to start archiving 
	 * @param theIsAddDirContents if set to true, recurses into sub-directories of the parent and its child directories
	 * @param theUpdatePack the DUP update.info object that will be added to as each file is added to the archive
	 * @param theActivityRootDirName the name of the import activity 'root' directory.
	 */
	protected void addToArchive(ZipOutputStream theTargetPackage, File theSourceContent, String theParentDirectoryName, boolean theIsAddDirContents, Updatepack theUpdatePack, String theActivityRootDirName)
	{		
		String aContentFilename = "";
		if(theParentDirectoryName.isEmpty() || theSourceContent.isDirectory())
		{
			aContentFilename = FilenameUtils.getName(theSourceContent.getName());
		}
		else
		{
			aContentFilename = theParentDirectoryName + File.separator + FilenameUtils.getName(theSourceContent.getName());
		}
		FileInputStream fin = null;
		
		try
		{			
			// If theSource is a directory, add it but avoid all log folders
			if(theSourceContent.isDirectory() && !(theSourceContent.getName().startsWith(theActivityRootDirName)))
			{
				// Only add the source folder if it is NOT the root of the import activity, i.e. theParentDirectoryName = ""
				if(!theParentDirectoryName.isEmpty())
				{					
					theTargetPackage.putNextEntry(new ZipEntry(aContentFilename + File.separator));
				}
				else
				{
					// If we're at the root, clear the aContentFilename
					aContentFilename = "";
				}
				theTargetPackage.closeEntry();
				
				if(theIsAddDirContents)
				{
					// Get all the subcomponents
					File[] aSourceFileList = theSourceContent.listFiles();
					for(int i = 0; i < aSourceFileList.length; i++)
					{
						File aNextFile = aSourceFileList[i];
						if(aNextFile != null)
						{
							if(aNextFile.isDirectory())
							{								
								addToArchive(theTargetPackage, aNextFile, aNextFile.getName(), theIsAddDirContents, theUpdatePack, theActivityRootDirName);								
							}
							else
							{
								addToArchive(theTargetPackage, aNextFile, aContentFilename, theIsAddDirContents, theUpdatePack, theActivityRootDirName);
							}
						}
					}
																	
				}
			}
			else
			{
				String aSourceFilename = FilenameUtils.getName(theSourceContent.getName());
				// If it is a Python script file, add it to the current Updatepack
				// and to our DUP zip file
				if(aContentFilename.endsWith(".py"))
				{
					// theSourceContent is just a file, so add that				
					fin = FileUtils.openInputStream(theSourceContent);
					addStreamToArchive(theTargetPackage, fin, theUpdatePack, aSourceFilename, aContentFilename);
				}
			}
		}
		catch(IOException anIOEx)
		{
			iuLog.log(Level.ALL, "Error encountered when creating configuration archive, adding file: " + theSourceContent.getName());
			iuLog.log(Level.ALL, anIOEx.getMessage());
		}
		finally
		{
			if(fin != null)
			{
				try
				{
					fin.close();
				}
				catch(IOException aCloseEx)
				{
					iuLog.log(Level.ALL, "Error encountered when creating configuration archive, closing file: " + theSourceContent.getName());
					iuLog.log(Level.ALL, aCloseEx.getMessage());
				}
			}
		}
	}

	private void addStreamToArchive(ZipOutputStream theTargetPackage, InputStream theStream, Updatepack theUpdatePack, String aSourceFilename, String aContentFilename) throws IOException {
		theTargetPackage.putNextEntry(new ZipEntry(aContentFilename));

		Packdefinition aNewPack = new Packdefinition();
		aNewPack.setName(aSourceFilename);
		aNewPack.setDescription("");
		
		// Give standardFunctions.py a special sequence number
		if(aSourceFilename.contains(dupStandardFunctionsFilename))
		{
			aNewPack.setSequence(0);
		}
		else
		{
			aNewPack.setSequence(theUpdatePack.getPack().size() + 1);
		}
		aNewPack.setChunktoken("###-CHUNK-###");
		aNewPack.setFilename(aContentFilename);
		theUpdatePack.getPack().add(aNewPack);

		// Write the stream into the DUP
		IOUtils.copy(theStream, theTargetPackage);
		theTargetPackage.closeEntry();
	}
	
	/**
	 * Read the specified configuration archive and write / deploy the contents to the configuration
	 * @param theSourceArchive the source archive containing a configuration component
	 */
	protected boolean readFromArchive(ZipInputStream theSourceArchive)
	{
		boolean isSuccess = false;
		FileOutputStream aDeployedFile = null;
		try
		{
			ZipEntry aZipEntry = null;
			while((aZipEntry = theSourceArchive.getNextEntry()) != null)
			{
				// Ignore explicit directory entries as openOutputStream will create any required
				if(!aZipEntry.isDirectory())
				{
	  			  	String entryFileName = aZipEntry.getName();
	  			  	String aTargetFile = itsRootDirectory + File.separator + entryFileName;
	  			  	File aConfigFile = new File(aTargetFile);
	  			  	
	  			  	// Filter out any rogue hidden files in the archive
	  			  	if(!aConfigFile.isHidden())
	  			  	{
	  			  		aDeployedFile = FileUtils.openOutputStream(aConfigFile, false);
	  			  		IOUtils.copy(theSourceArchive, aDeployedFile);
	  			  		aDeployedFile.close();
	  			  	}
				}
			}
			isSuccess = true;
		}
		catch(IOException anIOEx)
		{
			iuLog.log(Level.ALL, "Error encountered when deploying configuration archive ");
			iuLog.log(Level.ALL, anIOEx.getMessage());
		}
		finally
		{
			if(aDeployedFile != null)
			{
				try
				{
					aDeployedFile.close();
				}
				catch(IOException aCloseEx)
				{
					iuLog.log(Level.ALL, "Error encountered when deploying configuration archive, closing target file: ");
					iuLog.log(Level.ALL, aCloseEx.getMessage());
				}
			}
		}
		return isSuccess;
	}
	
}
