/**
 * Copyright (c)2014-2016 Enterprise Architecture Solutions Ltd.
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
 * 24.10.2016	JWC Replace all use of File.separator in ZIP files with "/"
 */
package com.enterprise_architecture.essential.importutility.data.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.enterprise_architecture.essential.importutility.utils.Log;

/**
 * Class to manage the packaging of the Essential Import Utility configuration.
 * Packages are saved as ZIP files that contain all the relevant configuration components
 * @author Jonathan Carter
 *
 */
public class ConfigurationPackage 
{
	/**
	 * The name of the directory in which the Import Utility manages its configuration 
	 */
	protected String configurationDir = "config";
	
	/**
	 * The name of the file in which the configuration of the application is managed 
	 */
	protected String configurationFile = "config.xml";
	
	/**
	 * The name of the file in which the user accounts for the application are managed 
	 */
	protected String userConfiguration = "essential_import_users.xml";
	
	/**
	 * The name of the directory in which the target repositories are cached 
	 */
	protected String repositoryCache = "repository_cache";
	
	/**
	 * The name of the directory in which the set up and logs for each import activity are maintained 
	 */
	protected String importActivities = "import_activities";
	
	/**
	 * The name of the directory in which configuration packages are persisted
	 */
	protected String configPackageLocation = "configBackup";
	
	/**
	 * The name of the file in which the configuration package is saved 
	 */
	protected String configPackageFilename = "essentialImportConfig.eic";
	
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
	 * Constant value used to create a file separator or directory within the Zip Archive.
	 */
	public static final String ZIP_FILE_SEPARATOR = "/";
	
	
	/**
	 * Constructor for the configuration packaging class.
	 * @param theRootDirectory the full path to the root directory in which this class 
	 * will operate.
	 */
	public ConfigurationPackage(String theRootDirectory)
	{
		itsRootDirectory = theRootDirectory;
	}
	
	/**
	 * Create a configuration package in the configuration package location for the current
	 * configuration of the Import Utility
	 * @param theIsAddTimestamp = TRUE, then a timestamp is added to the package filename.
	 * @return the relative path and name of the created package 
	 */
	public String createConfigurationPackage(boolean theIsAddTimestamp)
	{	
		String aConfigFilename = "";
		String aSuffix = "";
		if(theIsAddTimestamp)
		{
			// Create timestamp for the filename
			SimpleDateFormat aSimpleFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			aSuffix = "-" + aSimpleFormat.format(new Date());
		}
		
		aConfigFilename = FilenameUtils.getBaseName(configPackageFilename) + aSuffix + "." + FilenameUtils.getExtension(configPackageFilename);
		
		String anArchiveBaseName = configPackageLocation + File.separator + aConfigFilename;
		String aFullPathPrefix = itsRootDirectory + File.separator;
		String aFullPackagePath = aFullPathPrefix + anArchiveBaseName;
		
		FileOutputStream aZipFile = null;
		try
		{
			aZipFile = FileUtils.openOutputStream(new File(aFullPackagePath), false);
			ZipOutputStream aZipArchive = new ZipOutputStream(aZipFile);
			
			// Add the components with relative paths, creating
			// Basic config
			addToArchive(aZipArchive, new File(aFullPathPrefix + configurationDir), "", false);
			addToArchive(aZipArchive, new File(aFullPathPrefix + configurationDir + ZIP_FILE_SEPARATOR + configurationFile), configurationDir, false);
			addToArchive(aZipArchive, new File(aFullPathPrefix + configurationDir + ZIP_FILE_SEPARATOR + userConfiguration), configurationDir, false);
			
			// Repository Cache
			addToArchive(aZipArchive, new File(aFullPathPrefix + repositoryCache), "", true);
			
			// Import Activities
			addToArchive(aZipArchive, new File(aFullPathPrefix + importActivities), "", true); 		
			aZipArchive.close();
			
		}
		catch(IOException anIOEx)
		{
			iuLog.log(Level.ALL, "Error encountered when creating configuration archive file: " + anArchiveBaseName);
			iuLog.log(Level.ALL, anIOEx.getMessage());
		}
		finally
		{
			IOUtils.closeQuietly(aZipFile);
		}
		
		return anArchiveBaseName;
	}
	
	/**
	 * Read and process the specified configuration package, unpacking it and deploying the
	 * included components into the live Import Utility configuration, ready to be used.<br/>
	 * The current configuration is backed up to a configuration package before the deployment commences. 
	 * @param theConfigurationPackage configuration package to use 
	 * @return true on success or false if the package could not be deployed.
	 */
	public boolean deployConfigurationPackage(InputStream theConfigurationPackage)
	{
		boolean isSuccess = false;
		
		// First backup the current configuration
		String aConfigBackupFilename = createConfigurationPackage(ADD_TIMESTAMP);
		iuLog.log(Level.INFO, "Current Import Utility Configuration backed up to: " + aConfigBackupFilename);
		
		// Create a ZipInput Stream from theConfigurationPackage
		ZipInputStream aZipArchive = new ZipInputStream(theConfigurationPackage);
		isSuccess = readFromArchive(aZipArchive);
		if(aZipArchive != null)
		{
			try
			{
				aZipArchive.close();
			}
			catch(IOException aCloseEx)
			{
				iuLog.log(Level.ALL, "Error encountered when deploying configuration archive, closing file: ");
				iuLog.log(Level.ALL, aCloseEx.getMessage());
			}
		}
		
		return isSuccess;
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
			aContentFilename = theParentDirectoryName + ZIP_FILE_SEPARATOR + FilenameUtils.getName(theSourceContent.getName());
		}
		FileInputStream fin = null;
		
		try
		{
			// If theSource is a directory, add it 
			if(theSourceContent.isDirectory())
			{
				theTargetPackage.putNextEntry(new ZipEntry(aContentFilename + ZIP_FILE_SEPARATOR));
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
				theTargetPackage.putNextEntry(new ZipEntry(aContentFilename));
				fin = FileUtils.openInputStream(theSourceContent);
				IOUtils.copy(fin, theTargetPackage);	
				theTargetPackage.closeEntry();				
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
