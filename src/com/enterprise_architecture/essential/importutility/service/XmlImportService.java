/**
	* Copyright (c)2017 Enterprise Architecture Solutions ltd.
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
	* Servlet to provide REST service interface.
	* 09.03.2017	JWC	Version 1.0 started
 */
package com.enterprise_architecture.essential.importutility.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.enterprise_architecture.essential.importutility.utils.Log;


/**
 * 
 * Servlet that provides a REST Service interface to the Import Utility, enabling
 * XML Import Activities to be invoked automatically.
 * <p>Import Activities, Import Specifications (the semantic mapping of source data to the Essential Meta Model, defined via XSL transforms) and Target
 * Environments must all be configured in the Import Utility web-based, graphical user interface. The service interface provides
 * a REST-based HTTP approach for invoking imports by selecting the Import Activity and Target Environment in a request that includes
 * the source data to import.</p>
 * <p>Supported source data formats:
 * <ul>
 * <li>XML documents</li>
 * </ul>
 * 
 * </p>
 * <p>An import activity may take several minutes to complete. Therefore, the service supports a long-running process
 * by accepting a callback URL to which final success / failure messages are sent. On request, the parameters are evaluated 
 * and any immediate problems are reported (e.g. invalid import activity). The service responds with an HTTP 201 (Accepted) message
 * if all the request parameters are found to be valid and the request has been accepted for processing. The service receives the following parameters on the request /service:
 * <ul>
 * <li>activity - the name of the import activity to use</li>
 * <li>environment - the name of the target environment in which to import</li>
 * <li>uid - the user ID for the client requestor. This must be a valid user account within the import utility</li>
 * <li>pwd - the password for the user account.</li>
 * <li>publish - a boolean flag (yes / no) specifying whether the Import Utility should publish the resulting repository to the target Essential Viewer environment when the import has completed successfully.</li>
 * <li>source - the source XML document to import</li>
 * <li>response - (optional) a URL on the consumer side providing a callback for success / failure messages</li>
 * </ul>
 * 
 * @author Jonathan W. Carter
 * @version 1.0
 *
 */
public class XmlImportService extends HttpServlet 
{
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();

	/**
	 * Application pararmeter maintaining the singleton for the Import Service controller
	 * 
	 */
	public static final String CONTROLLER_SINGLETON_PARAM = "controllerXML";
	
	/**
	 * Default name for the XML document
	 * 
	 */
	public static final String DEFAULT_XML_DOC_NAME = "xmlDoc1.xml";
	
	/**
	 * Success message
	 */
	public static final String IMPORT_ACCEPTED_MESSAGE = "{\"status\" : 202, \"message\" : \"Import request accepted. Results will be POSTed to the callback URL that you specified\"}"; 
	
	/**
	 * Error message returned when attempts are made to invoke the service using the GET HTTP method.
	 */
	public static final String GET_METHOD_NOT_ALLOWED_MESSAGE = "GET Method is not available on the Essential Import Service. POST must be used to send source content and associated parameters";
	
	/**
	 * 400 series "bad request" messages
	 */
	public static final String INVALID_USER_DETAILS = "403: Invalid user name or password for the selected Import Activity";
	
	/**
	 * 400 series "bad request" messages
	 */
	public static final String INVALID_IMPORT_ACTIVITY = "404-1: Invalid Import Activity specified. Import Activities must be configured via the Import Utility GUI and specified to the service by name.";
	
	/**
	 * 400 series "bad request" messages
	 */
	public static final String INVALID_TARGET_ENVIRONMENT = "404-2: Invalid Target Environment specified. Target Environments must be configured via the Import Utility GUI and specified to the service by name";
		
	/**
	 * 400 series "bad request" messages
	 */
	public static final String INVALID_SOURCE_CONTENT = "415-1: Invalid source content supplied. Source content has not been supplied in one of the supported formats.";

	
	/**
	 * 500 series "internal server error" messages
	 */
	public static final String WRITING_FILE_ERROR_MESSAGE = "500-1: Error encountered while writing content to queue. See logs for more details";
	
	/**
	 * 500 series "internal server error" messages
	 */	
	public static final String UPLOAD_ERROR_MESSAGE = "500-2: Error encountered while parsing the uploaded source content. See logs for more details";
	
	/**
	 * 501 message to indicate the service is intentionally not available in EIP mode 
	 */
	public static final String SERVICE_UNAVAILABLE_MESSAGE = "501-1: The requested service is not available in this edition of the Essential Import Utility.";
	
	/**
	 * 500 series "internal server error" messages
	 */		
	public static final String IMPORT_REFUSED_MESSAGE = "503-1: Request to import source content refused. See logs for more details and try again later.";
	
	/**
	 * Service request parameter defining the import activity to use
	 */
	public static final String ACTIVITY_PARAM = "activity";
	
	/**
	 * Service request parameter defining the target environment to use
	 */
	public static final String ENVIRONMENT_PARAM = "environment";
	
	/**
	 * Service request parameter defining the user id
	 */
	public static final String USER_PARAM = "uid";
	
	/**
	 * Service request parameter defining the password of user
	 */
	public static final String PASSWORD_PARAM = "pwd";
	
	/**
	 * Service request parameter defining whether to publish to Essential Viewer on import success
	 */
	public static final String PUBLISH_PARAM = "publish";
	
	/**
	 * Service request parameter defining the source content to import
	 */
	public static final String SOURCE_PARAM = "source";
	
	/**
	 * Service request parameter defining the callback response URL to use
	 */
	public static final String RESPONSE_PARAM = "response";
		
	
	/**
	 * Servlet parameter defining the location to use for temporary storage during
	 * file upload
	 */
	public static final String UPLOAD_TEMP_FOLDER_PARAM = "upload_temp_location";
	
	/**
	 * Servlet parameter defining the location in which source content files are queued
	 * 
	 */
	public static final String SOURCE_FOLDER_PARAM = "source_content_location";
	
	/**
	 * Default filename suffix for XML files saved in the job queue
	 */
	public static final String DEFAULT_QUEUE_FILE_SUFFIX = ".xml";
	
	/**
	 * File size upper limit (before using temp folder) to use.
	 *
	 */
	public static final long UPLOAD_LIMIT_SIZE = 536870912L;
	
	/**
	 * Memory threshold for uploading content
	 *
	 */
	public static final int MEMORY_THRESHOLD_SIZE = 102400;
	
	/**
	 * Content type for XML documents
	 * .XML
	 */
	public final static String XML_TEXT_CONTENT_TYPE = "text/xml";
	
	/**
	 * Content type for XML documents
	 * .XML
	 */
	public final static String XML_APP_CONTENT_TYPE = "application/xml";
	
	
	/**
	 * Prefix for source content files, where none is specifed by the post message
	 */
	protected String itsSourceFilePrefix = "importSource";
	
	/**
	 * Web.xml parameter name for the mode in which the Import Utility is running
	 */
	protected static String IU_MODE_PARAM_NAME = "iu_mode";
	
	/**
	 * Setting denoting running in EIP (cloud) mode
	 */
	protected static String EIP_IU_MODE = "eip";
	
	/**
	 * Setting denoting running in local installation mode
	 */
	protected static String LOCAL_IU_MODE = "local";

	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Content type for posted form data
	 */
	private final static String MULTIPART_FORM_CONTENT_TYPE = "multipart/form-data";
	
	/**
	 * Content type for a Zip archive
	 */
	private final static String ZIPFILE_CONTENT_TYPE = "application/zip";
	
	/**
	 * Alternative pattern to find zip file content type for browsers that don't use standard type
	 */
	private final static String ALT_ZIP_CONTENT_TYPE = "zip";
	
	/**
	 * Finally, another alternative to check for ZIP in POSTed multipart/form-data payload
	 */
	private final static String ASSUME_ZIP_CONTENT_TYPE = "octet";
		
	/**
	 * File suffix for the XML format
	 */
	private final static String XML_FILE_SUFFIX = ".xml";
	
	/**
	 * Maintain the Servlet Context for this instance of the Essential Import Service
	 */
	private ServletContext itsServletContext = null;
	
	/**
	 * Relative location for the temporary folder used to receive source uploads
	 * 
	 */
	private String itsUploadTempFolder = "service/tmp";
	
	/**
	 * Relative location for the source files to be imported
	 * 
	 */
	private String itsSourceContentFolder = "service/source";
	
	/**
	 * Initialise the service on start up
	 */
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		
		itsServletContext = getServletContext();
		itsUploadTempFolder = itsServletContext.getInitParameter(UPLOAD_TEMP_FOLDER_PARAM);
		itsSourceContentFolder = itsServletContext.getInitParameter(SOURCE_FOLDER_PARAM);
	}
	

	/**
	 * Gracefully shutdown the service, releasing the Import Controller singleton
	 */
	public void destroy()
	{
		// Get the import controller and tell it to shutdown
		XmlImportServiceImpl aController = getImportController(false);
		if(aController != null)
		{
			aController.shutdown();
		}
		
		super.destroy();
	}
	
	/**
	 * The service does not support the GET request. No redirect is performed - only an error response.
	 */
	protected void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException 
	{
		// return an error message? Unsupported method?
		// Does it make sense for any resources to be 'got'?
		theResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, GET_METHOD_NOT_ALLOWED_MESSAGE);
	}
	
	/**
	 * Receive requests to the service.
	 * <p>An import activity may take several minutes to complete. Therefore, the service supports a long-running process
	 * by accepting a callback URL to which final success / failure messages are sent. On request, the parameters are evaluated 
	 * and any immediate problems are reported (e.g. invalid import activity). The service responds with an HTTP 201 (Accepted) message
	 * if all the request parameters are found to be valid and the request has been accepted for processing. The service receives the following parameters on the request /service:
	 * <ul>
	 * <li>activity - the name of the import activity to use</li>
	 * <li>environment - the name of the target environment in which to import</li>
	 * <li>uid - the user ID for the client requestor. This must be a valid user account within the import utility</li>
	 * <li>pwd - the password for the user account.</li>
	 * <li>publish - a boolean flag (yes / no) specifying whether the Import Utility should publish the resulting repository to the target Essential Viewer environment when the import has completed successfully.</li>
	 * <li>source - the source XML file (.xml)</li>
	 * <li>docname - (optional) the name of the worksheet to use when receiving XML. Default value is 'xmlDoc1'	 
	 * <li>response - (optional) a URL on the consumer side providing a callback for success / failure messages</li>
	 * </ul>
	 */
	protected void doPost(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
	{
		String aResponseMsg = "";
		int aResponseCode = HttpServletResponse.SC_ACCEPTED;
		boolean isSuccess = false;		
		
		// Ensure that the service is NOT available in EIP mode
		String anIUMode = itsServletContext.getInitParameter(IU_MODE_PARAM_NAME);
		if(anIUMode.equals(EIP_IU_MODE))
		{
			// Service is not available in EIP mode
			aResponseMsg = SERVICE_UNAVAILABLE_MESSAGE;
			aResponseCode = HttpServletResponse.SC_NOT_IMPLEMENTED;
		}
		else // Service available as we're not in EIP mode
		{
		
			// Receive the parameters
			String anActivity = theRequest.getParameter(ACTIVITY_PARAM);
			String aTargetEnv = theRequest.getParameter(ENVIRONMENT_PARAM);
			String aUser = theRequest.getParameter(USER_PARAM);
			String aPwd = theRequest.getParameter(PASSWORD_PARAM);
			String aPublishFlag = theRequest.getParameter(PUBLISH_PARAM);
			String aResponse = theRequest.getParameter(RESPONSE_PARAM);
			
			String aSourceXML = theRequest.getParameter(SOURCE_PARAM);
			String aSourceFilename = "";
			
			// Check content type and work out what the source is
			String aContentType = theRequest.getContentType();
			
			// Keep the source content so that we can make sure we have all params before processing
			FileItem aSourceItem = null;
			
			if(aContentType.contains(MULTIPART_FORM_CONTENT_TYPE))
			{
				// Process the multi-part data			
				DiskFileItemFactory aDiskFactory = new DiskFileItemFactory();
				// Use the upload directory as the temporary file store for large images
				String aRepositoryPath = getServletContext().getRealPath(itsUploadTempFolder);
				aDiskFactory.setRepository(new File(aRepositoryPath));
				
				// Set in-memory threshold to 100KB
				aDiskFactory.setSizeThreshold(MEMORY_THRESHOLD_SIZE);
				
				// Set up the uploader
				ServletFileUpload anUpload = new ServletFileUpload(aDiskFactory);
				
				// Set the max size just under the Tomcat default limit of 2MB
				anUpload.setSizeMax(UPLOAD_LIMIT_SIZE);
				
				// Process the upload
				synchronized(theRequest)
				{
					try
					{
						// Test the content type and handle accordingly
						List<FileItem> aContentList = anUpload.parseRequest(theRequest);
						Iterator<FileItem> aContentListIt = aContentList.iterator();
						while(aContentListIt.hasNext())
						{
							FileItem anItem = aContentListIt.next();
							String aParamName = anItem.getFieldName();
						
							// Get the parameters						
							if(aParamName.equals(ACTIVITY_PARAM))
							{
								anActivity = anItem.getString();
								anItem.delete();
							}
							else if(aParamName.equals(ENVIRONMENT_PARAM))
							{
								aTargetEnv = anItem.getString();
								anItem.delete();
							}
							else if(aParamName.equals(USER_PARAM))
							{
								aUser = anItem.getString();
								anItem.delete();
							}
							else if(aParamName.equals(PASSWORD_PARAM))
							{
								aPwd = anItem.getString();
								anItem.delete();
							}
							else if(aParamName.equals(PUBLISH_PARAM))
							{
								aPublishFlag = anItem.getString();
								anItem.delete();
							}
							else if(aParamName.equals(RESPONSE_PARAM))
							{
								aResponse = anItem.getString();
								anItem.delete();
							}							
							
							// Read the file
							else if(aParamName.equals(SOURCE_PARAM))
							{
								aSourceItem = anItem;
								
							}
						}
					}
					catch(FileUploadException aFileUploadEx)
					{
						iuLog.log(Level.WARNING, "XML Import Service: Exception encountered while parsing request to receive uploaded source content.", aFileUploadEx);
						isSuccess = false;
						aResponseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; 
						aResponseMsg = UPLOAD_ERROR_MESSAGE;
					}
					catch(Exception aWritingEx)
					{
						iuLog.log(Level.WARNING, "XML Import Service: Exception encountered while writing received XML file.", aWritingEx);
						isSuccess = false;
						aResponseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; 
						aResponseMsg = WRITING_FILE_ERROR_MESSAGE;
					}
					
				}
				
				/// PROCESS aSOURCEITEM
				// Test content type
				String aFileContentType = aSourceItem.getContentType();
				String aFileName = getLocalFileName(aSourceItem.getName());
				
				// Report the content type to aid with trouble shooting due to browser variation
				iuLog.log(Level.INFO, "XMLImportService: Received File: Content Type = " + aFileContentType);
				
				// Look for ZIP content type - handle less standard browsers
				if(aFileContentType.contains(ZIPFILE_CONTENT_TYPE) ||
						aFileContentType.contains(ALT_ZIP_CONTENT_TYPE) ||
						aFileContentType.contains(ASSUME_ZIP_CONTENT_TYPE))
				{
					// Handle a ZIP file
					ZipInputStream aZipInStream = new ZipInputStream(aSourceItem.getInputStream());
					
					// Stream the ZipInStream into a String.
					// Get the ZIP file XML content
					aSourceFilename = saveSourceContent(aZipInStream, aFileName);
					if(aSourceFilename.length() > 0)
						isSuccess = true;
					aZipInStream.close();									
				}
				
				// If .XML, then save the source to the temp location							
				else if(aFileContentType.contains(XML_APP_CONTENT_TYPE) || aFileContentType.contains(XML_TEXT_CONTENT_TYPE))
				{
					// Handle uncompressed XLS or XLSX
					aSourceFilename = saveSourceContent(aSourceItem, aFileName);
					if(aSourceFilename.length() > 0)
						isSuccess = true;
				}
				else
				{
					isSuccess = false;
				}
											
				aSourceItem.delete();
	
			}
			else
			{
				// Assume default content type
				// Got the parameters already, directly from request params
				// Convert and queue the XML
				aSourceFilename = saveSourceContent(aSourceXML, getLocalFileName(DEFAULT_XML_DOC_NAME));
				if(aSourceFilename.length() > 0)
					isSuccess = true;
			}
			
			// Source Content in uniform format and saved to queue.
			int aReturnCode = 0;
			if(isSuccess)
			{
				// Make request to the Service Implementation.
				boolean isPublishAfterImport = aPublishFlag.equals("yes") ? true : false;
				XmlImportServiceImpl aController = getImportController();
				aReturnCode = aController.executeImportJob(anActivity, 
														   aTargetEnv, 
														   aSourceFilename, 
														   itsServletContext, 
														   aResponse, 
														   aUser, 
														   aPwd,  
														   isPublishAfterImport);
			}	
			// TEST HARNESS: Call the callback service
			/*ImportCallback aTestHarnessResults = buildTestResponse();
			ImportResponse aCallback = new ImportResponse();
			int aCallbackStatus = aCallback.sendImportResponse(aTestHarnessResults, aResponse);
			*/
			// TEST HARNESS END
			
			
			// Return status code - 202 for success
			if(isSuccess && (aReturnCode == ImportServiceImpl.JOB_ACCEPTED))
			{
				aResponseCode = HttpServletResponse.SC_ACCEPTED;
				aResponseMsg = IMPORT_ACCEPTED_MESSAGE;
			}
			
			else
			{
				switch(aReturnCode)
				{
					case ImportServiceImpl.INVALID_IMPORT_ACTIVITY :
						aResponseCode = HttpServletResponse.SC_NOT_FOUND;
						aResponseMsg = INVALID_IMPORT_ACTIVITY;
						break;
					case ImportServiceImpl.INVALID_SOURCE_CONTENT :
						aResponseCode = HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
						aResponseMsg = INVALID_SOURCE_CONTENT;
						break;
					case ImportServiceImpl.INVALID_TARGET_ENVIRONMENT :
						aResponseCode = HttpServletResponse.SC_NOT_FOUND;
						aResponseMsg = INVALID_TARGET_ENVIRONMENT;
						break;
					case ImportServiceImpl.INVALID_USER :
						aResponseCode = HttpServletResponse.SC_FORBIDDEN;
						aResponseMsg = INVALID_USER_DETAILS;
						break;					
					case ImportServiceImpl.JOB_NULL :
						aResponseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
						aResponseMsg = IMPORT_REFUSED_MESSAGE;
						break;
					case ImportServiceImpl.JOB_REFUSED :
						aResponseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
						aResponseMsg = IMPORT_REFUSED_MESSAGE;
						break;
					default:
						aResponseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
						aResponseMsg = WRITING_FILE_ERROR_MESSAGE;
						break;
						
				}
				
				String aResponseContent = "{\"status\" : " + Integer.toString(aResponseCode);
				aResponseContent = aResponseContent.concat(", \"message\" : \"" + aResponseMsg);
				aResponseContent = aResponseContent.concat("\"}");
				theResponse.setContentType("application/json");
				theResponse.setCharacterEncoding("UTF-8");					
				theResponse.sendError(aResponseCode, aResponseContent);			
			}
		}
		
		theResponse.setStatus(aResponseCode);
		theResponse.setContentType("application/json");
		theResponse.setCharacterEncoding("UTF-8");		
		theResponse.getWriter().print(aResponseMsg);
		theResponse.getWriter().flush();
	}
	
	/**
	 * Find or build a filename for the received source content. If one is supplied use
	 * that as the main part of the filename, otherwise create one using the default prefix.
	 * 
	 * @param theFilename the name that was supplied.
	 * @return the filename that will be stored, including a timestamp-based suffix
	 * to ensure that filename do not clash between requests
	 */
	protected String getLocalFileName(String theFilename)
	{		
		String aLocalName = "";
		String aSuffix = "";
		if((theFilename != null) && (theFilename.length() > 0))
		{
			int aUnixIndex = theFilename.lastIndexOf("/");
			int aWinIndex = theFilename.lastIndexOf("\\");
			int aSuffixIndex = theFilename.lastIndexOf(".");
			if(aSuffixIndex > 0)
			{
				aSuffix = theFilename.substring(aSuffixIndex);	
			}
					
			if(aUnixIndex >= 0)
			{
				aLocalName = theFilename.substring(aUnixIndex + 1, aSuffixIndex);
			}
			else if(aWinIndex >= 0)
			{
				aLocalName = theFilename.substring(aWinIndex + 1, aSuffixIndex);
			}
			else
			{
				aLocalName = theFilename.substring(0, aSuffixIndex);
			}
		}
		else
		{
			aLocalName = itsSourceFilePrefix;
		}
		
		// Add timestamp to ensure unique.
		Date aTimeNow = new Date();
		String aTimeStamp = String.valueOf(aTimeNow.getTime());
		aLocalName = aLocalName.concat(aTimeStamp);
		aLocalName = aLocalName.concat(aSuffix);
		return aLocalName;
	}
	
	/**
	 * Save the specified source content document in the input folder
	 * @return the name of the file that was saved, empty string on error
	 */
	protected synchronized String saveSourceContent(ZipInputStream theZippedInStream, String theFilename)
	{
		boolean isSuccess = false;
		String aSourceFilename = "";
		
		// Stream the posted file and uncompress to file
		try
		{
			ZipEntry aZipEntry = theZippedInStream.getNextEntry();
			String anEntryName = aZipEntry.getName();
			if(anEntryName.endsWith(XML_FILE_SUFFIX))
			{
				String aLocalName = getLocalFileName(anEntryName);
				String aRealFile = getJobQueueFilename(aLocalName, "");
				FileOutputStream anOutFileStream = new FileOutputStream(aRealFile);
				
				IOUtils.copyLarge(theZippedInStream, anOutFileStream);
				aSourceFilename = aRealFile;
			}			
			isSuccess = true;
		}
		catch(IOException anXMLFileEx)
		{
			// Catch and report IO Exception reading update script file
			iuLog.log(Level.WARNING, "XML Import Service: IO Exception while reading received Zip archive. Details:", anXMLFileEx);			
			isSuccess = false;
		}
		catch(NullPointerException anNPE)
		{
			iuLog.log(Level.WARNING, "XML Import Service: Exception reading received Zip archive. Details:", anNPE);			
			isSuccess = false;
		}
		catch(Exception anZipFileEx)
		{
			// Catch and report any other Exception reading update script file
			iuLog.log(Level.WARNING, "XML Import Service: Exception reading received Zip archive. Details:", anZipFileEx);	
			isSuccess = false;
		}
		
		if(!isSuccess)
		{
			aSourceFilename = "";
		}
		return aSourceFilename;
	}
	
	/**
	 * Save a received XML document - to the processing queue folder
	 * @param theUncompressedItem the XML document to process
	 * @param theFilename the name of the file to use
	 * @return the name of the file that was saved, empty string on error.
	 */
	protected synchronized String saveSourceContent(FileItem theUncompressedItem, String theFilename)
	{
		boolean isSuccess = false;
		String aSourceFilename = "";
		try
		{
			String aRealFile = getJobQueueFilename(theFilename, "");
			
			File aFile = new File(aRealFile);
			theUncompressedItem.write(aFile);
			aSourceFilename = aRealFile;
			isSuccess = true;			
		}
		catch (Exception ioEx)
		{
			iuLog.log(Level.SEVERE, "Exception writing received XML source file", ioEx);			
			isSuccess = false;
		}		
		
		if(!isSuccess)
		{
			aSourceFilename = "";
		}
		return aSourceFilename;
	}
	
	/**
	 * Save a received source content in XML format, saving it to the queue folder
	 * @param theUncompressedXML the XML file in uncompressed format in a plain String.
	 * @param theFilename the filename to be used to store the converted XML in the queue.
	 * @return the name of the file that was saved or empty string on error
	 */
	protected synchronized String saveSourceContent(String theUncompressedXML, String theFilename)
	{
		boolean isSuccess = false;
		String aSourceFilename = "";
		
		// Open a file to save to.
		try
		{			
			String aRealFile = getJobQueueFilename(theFilename);		
			File aFile = new File(aRealFile);
			FileOutputStream aFileOutStream = new FileOutputStream(aFile);
			
			StringReader aStringReader = new StringReader(theUncompressedXML);
			IOUtils.copy(aStringReader, aFileOutStream);
			aSourceFilename = aRealFile;
			isSuccess = true;
		}
		catch (Exception ioEx)
		{
			iuLog.log(Level.SEVERE, "Exception writing XML source file", ioEx);
			isSuccess = false;
		}		
		
		if(!isSuccess)
		{
			aSourceFilename = "";
		}
		return aSourceFilename;
	}
	
	
	/**
	 * Build and return a filename in the queue folder that should be used to queue a source file
	 * for import
	 * @param theFilename the local name of the file to queue - typically that specified by the client request 
	 * @param theFileSuffix the file suffix to use, defaults to ".xml" 
	 * @return the real path to the file in the queue
	 * @see ImportService#DEFAULT_QUEUE_FILE_SUFFIX DEFAULT_QUEUE_FILE_SUFFIX
	 */
	protected String getJobQueueFilename(String theFilename, String theFileSuffix)
	{
		String aQueueFolder = itsSourceContentFolder.concat(File.separator);
		String aQueueFile = itsServletContext.getRealPath(aQueueFolder.concat(theFilename) + theFileSuffix);
		return aQueueFile;
	}
	
	/**
	 * Build and return a filename in the queue folder that should be used to queue a source file
	 * for import
	 * @param theFilename the local name of the file to queue - typically that specified by the client request
	 * @return the real path to the file in the queue with the default file suffix, defined by DEFAULT_QUEUE_FILE_SUFFIX
	 * @see ImportService#DEFAULT_QUEUE_FILE_SUFFIX DEFAULT_QUEUE_FILE_SUFFIX
	 */
	protected String getJobQueueFilename(String theFilename)
	{
		int aSuffixIndex = theFilename.lastIndexOf(".");
		String aSuffix = theFilename.substring(aSuffixIndex);
		if(aSuffix.contains(XML_FILE_SUFFIX))
		{
			return getJobQueueFilename(theFilename, "");
		}
		else
		{
			return getJobQueueFilename(theFilename, DEFAULT_QUEUE_FILE_SUFFIX);
		}
	}
	
	/**
	 * Find the Singleton Controller object - creating a new one if one is not already available. Newly created Singletons
	 * are held in the Application variable 'controller'
	 * @return a reference to the ImportServiceImpl Controller Singleton object that manages all requests and performs
	 * the import processes.
	 */
	protected XmlImportServiceImpl getImportController()
	{
		// First, check to see if the Controller is already in the ServletContext.
		String aSingleton = itsServletContext.getInitParameter(CONTROLLER_SINGLETON_PARAM);
		XmlImportServiceImpl aController = (XmlImportServiceImpl)itsServletContext.getAttribute(aSingleton);
		
		// If we get a null back, then the Singleton has not been created. Create it
		if(aController == null)
		{
			aController = new XmlImportServiceImpl();
			
			// Keep the resulting controller as a Singleton via a Servlet attribute
			itsServletContext.setAttribute(aSingleton, aController);
		}
		return aController;
	}
	
	/**
	 * Get a reference to the Service controller but depending on the value of theIsCreate 
	 * parameter, the method will not create the controller if it is not already available.
	 * @param theIsNoCreate true to force the creation of the controller false to return the controller or NULL 
	 * if it has not been created
	 */
	protected XmlImportServiceImpl getImportController(boolean theIsCreate)
	{
		if(theIsCreate)
		{
			return getImportController();
		}
		else
		{
			// First, check to see if the Controller is already in the ServletContext.
			String aSingleton = itsServletContext.getInitParameter(CONTROLLER_SINGLETON_PARAM);
			XmlImportServiceImpl aController = (XmlImportServiceImpl)itsServletContext.getAttribute(aSingleton);
			return aController;
		}
	}
	
}
