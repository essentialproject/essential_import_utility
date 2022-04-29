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
 * 28.06.2011	JP	1st coding.
 * 13.05.2014	JWC Updates to fully support Windows zip files
 * 
 */
package com.enterprise_architecture.essential.importutility.integration;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.enterprise_architecture.essential.importutility.utils.Log;

import java.io.File;
import java.util.zip.ZipInputStream;
 
public class FileCompressionManager {
	
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();
	
 
	public void compressFiles(String targetFilePath, String[] sourceFilePaths, String dirPath) {
		String zipFile = targetFilePath;
		 
		 
		try
		{
			//create object of FileOutputStream
			FileOutputStream fout = new FileOutputStream(zipFile);
			
			/*
			* To create a zip file, use
			*
			* ZipOutputStream(OutputStream out)
			* constructor of ZipOutputStream class.
			*/
			
			//create object of ZipOutputStream from FileOutputStream
			ZipOutputStream zout = new ZipOutputStream(fout);
			File sourceFile;
			
			try {
				for(int i=0; i < sourceFilePaths.length; i++) {
					 
						sourceFile = new File( sourceFilePaths[i]);	
						if(sourceFile.isFile()) {
							//System.out.println("Adding " + sourceFilePaths[i]);
							//System.out.println("Zip Entry name " + FilenameUtils.getName(sourceFilePaths[i]));
							
							//create object of FileInputStream for source file
							FileInputStream fin = new FileInputStream(sourceFilePaths[i]);
							 
							/*
							* To begin writing ZipEntry in the zip file, use
							*
							* void putNextEntry(ZipEntry entry)
							* method of ZipOutputStream class.
							*
							* This method begins writing a new Zip entry to
							* the zip file and positions the stream to the start
							* of the entry data.
							*/
							
							// 13.05.2014 JWC Need to use relative path for entry names
							String entryName = FilenameUtils.getName(sourceFilePaths[i]);
							zout.putNextEntry(new ZipEntry(entryName));
							
							// Stream the file into the Zip file
							IOUtils.copy(fin, zout);
							
							// end of 13.05.2014 JWC
							
							zout.closeEntry();
							 
							//close the InputStream
							fin.close();
						}
				 
				}
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
			finally{
				//close the ZipOutputStream
				zout.close();
			}
		 
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	 
	}
	
	

	/**
	 * Decompress uploaded repository zip files from outside the Import Utility
	 * @param sourceDirPath
	 * @throws Exception
	 */
	public void decompressFiles(String sourceDirPath) throws Exception {
	       
	    try {
	      File dir = new File(sourceDirPath);
	      String[] zipFileExtensions = {"zip"};
	      
	      // Read all the files found in the folder
	      // Should just be ZIP files
	      Iterator<File> aFolderContents = FileUtils.iterateFiles(dir, zipFileExtensions, false);
	      while(aFolderContents.hasNext())
	      {
	    	  // 13.05.2014 JWC - Read ZIP file using ZipInputStream
	    	  InputStream aFile = new FileInputStream(aFolderContents.next());
	    	  ZipInputStream aZipFile = new ZipInputStream(aFile);
	    	  FileOutputStream anUnCompressedFile = null;
			  String entryFilePath = "";
	    	  
	    	  try
	    	  {
	    		  ZipEntry aZipEntry;
	    		  while((aZipEntry = aZipFile.getNextEntry()) != null)
	    		  {
	    			  // Just get the filename, dropping all path content from Zip
	    			  String entryFileName = FilenameUtils.getName(aZipEntry.getName());    				  
	    			  
	    			  entryFilePath =  sourceDirPath + File.separator + entryFileName;
	    			  	    			  
			          // Ignore folders and hidden files from decompress
	    			  if(!aZipEntry.isDirectory() && !entryFileName.startsWith("."))
	    			  {	    				  
	    				  anUnCompressedFile = new FileOutputStream(entryFilePath);
	    				  IOUtils.copy(aZipFile, anUnCompressedFile);
	    			  }
	    			  else
	    			  {
	    				  iuLog.info("Ignoring content from uploaded repository: " + aZipEntry.getName());
	    			  }
	    		  }
	    	  }
	    	  catch(Exception ex)
	    	  {
	    		  System.out.println("Error: File Compression Manager decompressing: " + entryFilePath);
	    		  ex.printStackTrace();
	    	  }
	    	  finally
	    	  {
	    		  if(aZipFile != null)
	    		  {
	    			  aZipFile.close();
	    		  }
	    		  if(anUnCompressedFile != null)
	    		  {
	    			  anUnCompressedFile.close();
	    		  }
	    	  }
	    	 
	      }	     
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	}
	
	/**
	 * Decompress the files in the source directory into the target directory
	 * @param sourceDirPath the directory containing the file(s) to be decompressed
	 * @param targetDirPath the directory into which the decompressed files will be written
	 * @throws Exception
	 */
	public void decompressFilesToDir(String sourceDirPath, String targetDirPath) throws Exception {
	    //final int BUFFER = 2048;
	    try 
	    {
	      //BufferedOutputStream dest = null;
	      File dir = new File(sourceDirPath);
	      //File[] children = dir.listFiles();
	      Iterator<File> aSourceContents = FileUtils.iterateFiles(dir, null, false);
	      while(aSourceContents.hasNext())
	      {
	    	  InputStream aFile = new FileInputStream(aSourceContents.next());
	    	  ZipInputStream aZipFile = new ZipInputStream(aFile);
	    	  FileOutputStream anUncompressedFile = null;
	    	  String entryFilePath = "";
	    	  
	    	  try
	    	  {
	    		  ZipEntry aZipEntry;
	    		  while((aZipEntry = aZipFile.getNextEntry()) != null)
	    		  {
	    			  // Write the entry to the target folder
	    			  entryFilePath = targetDirPath + File.separator + aZipEntry.getName();
	    			  anUncompressedFile = new FileOutputStream(entryFilePath);
	    			  IOUtils.copy(aZipFile, anUncompressedFile);		    			  
	    		  }
	    	  }
	    	  catch(Exception ex)
	    	  {
	    		  System.out.println("Error: File Compression Manager decompressing: " + entryFilePath);
	    		  ex.printStackTrace();
	    	  }
	    	  finally
	    	  {
	    		  if(aZipFile != null)
	    		  {
	    			  aZipFile.close();
	    		  }
	    		  if(anUncompressedFile != null)
	    		  {
	    			  anUncompressedFile.close();
	    		  }
	    	  }
	    	 
	      }	     
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	      
	  }
	
	/**
	 * Decompress the specified ZIP file into the target directory
	 * @param sourceFilePath the ZIP file to decompress
	 * @param targetDirPath the target directory into which the uncompressed files will be written
	 * @throws Exception
	 */
	public void decompressZipFileToDir(String sourceFilePath, String targetDirPath) throws Exception 
	{
	    //final int BUFFER = 2048;
	    try 
	    {
	    	
	    	//BufferedOutputStream dest = null;
	    	File zipFile = new File(sourceFilePath);
	    	FileInputStream aFileIn = new FileInputStream(zipFile);
	    	ZipInputStream aZipFile = new ZipInputStream(aFileIn);
	    	FileOutputStream anUncompressedFile = null;
	    	String entryFilePath = "";
	    	
	    	try
	    	{
	    		ZipEntry aZipEntry;
	    		while((aZipEntry = aZipFile.getNextEntry()) != null)
	    		{
	    			// Write the entry to the target folder
	    			entryFilePath = targetDirPath + aZipEntry.getName();
	    			anUncompressedFile = new FileOutputStream(entryFilePath);
	    			IOUtils.copy(aZipFile, anUncompressedFile);
	    		}
	    	}
	    	catch(Exception ex)
	    	{
	    		System.out.println("Error: File Compression Manager decompressing: " + entryFilePath);
				ex.printStackTrace();
			}
			finally
			{
				if(aZipFile != null)
				{
					aZipFile.close();
				}
				if(anUncompressedFile != null)
				{
					anUncompressedFile.close();
				}
			}
     
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    	
	  }
	
	
}
 
