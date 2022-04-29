/**
 * Copyright (c)2006-2020 Enterprise Architecture Solutions Ltd.
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
 * 31.08.2011	JP	1st coding.
 * 10.01.2020	JWC	Ensure the Viewer XML snapshot is written in UTF-8
 */
package com.enterprise_architecture.essential.importutility.integration;

import java.io.StringWriter;

import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;

import java.net.*;
import java.nio.charset.StandardCharsets;

import com.enterprise_architecture.essential.widgets.HttpReportServiceClient;
import com.enterprise_architecture.essential.widgets.ImageAutoLayoutPanel;
import com.enterprise_architecture.essential.xml.XMLRenderer;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;

/**
 * EssentialViewerPublisher is used to export the Knowledgebase containing the ontology for a Protege Project so that it
 * can be sent to the Essential reporting service.<br>
 * @author Jason Powell
 * @version 1.0
 * @since v1.0
 * @see com.enterprise_architecture.essential.report.EasReportService
 *
 */
public class EssentialViewerPublisher {
	public static final String REPORT_SERVICE_NAME = "/reportService";
	public static final String READY_STATUS_MESSAGE = "Ready to create and send repository snapshot";
	public static final String GETTING_KB_XML_MESSAGE = "Rendering repository...";
	public static final String SENDING_TO_REPORT_SERVICE = "Sending repository snapshot...";
	public static final String WORKING_MESSAGE = "...";
	public static final String SUCCESS_MESSAGE = "Success. Repository snapshot generated and sent";
	public static final String FAILED_GENERATION_MESSAGE = "Repository rendering failed";
	public static final String SUCCESSFUL_GENERATION = "Repository generated successfully";
	public static final String FAILED_SEND = "Failed to send snapshot to the Report Service";
	public static final String PROGRESS_STRING = "Sending...";
	public static final String PROGRESS_COMPLETE = "Finished";
	public static final String NO_SERVER_MESSAGE = "No response from the Report Service at this URL.";
	public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Essential Viewer ReportService encountered an internal error while receiving your repository snapshot. Contact your system administrator and check Essential Viewer server logs for errors, e.g. memory exceptions.";
	public static final String BAD_URL_MESSAGE = "Essential Viewer ReportService could not be found at this URL.";
	public static final String BAD_PASSWORD_MESSAGE = "User name and password required; invalid user name and password supplied";
	public static final String SERVER_ERROR_MESSAGE = "Server error. The URL appears to OK but sending the repository snapshot could not be completed. Contact your system administrator and check Essential Viewer server logs for errors, e.g. memory exceptions.";
	public static final String BAD_REQUEST_MESSAGE = "Client error. An exception occurred while sending graph images to the report service. Check the Protege console logs for more details.";
	
	public static final int PUBLISH_SUCCESS = 210;
	public static final int FAILED_GENERATION = 220;
	public static final int PUBLISH_FAIL = 230;
	public static final int NO_SERVER = 0;
	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final int BAD_URL = 404;
	public static final int BAD_PASSWORD = 401;
	public static final int BAD_REQUEST = 400;

	
	// The XML for the report
	private String itsURL = "";
	private String itsReportXML;
	private String itsUID;
	private String itsPassword;
	private boolean itIsFinished;
	private boolean itIsSuccess;
	private int itsReturnCode;
	
	private boolean itIsTaskComplete = false;
	private Project itsProject;
	private KnowledgeBase itsKB = null;
	
	// 19.11.2009 JWC Get layout choice history
	private boolean isMultiUser = false;
	private static final String IMAGES_URL = "Images";
	private String itsImageURL = "";
	private String itsImageURLSuffix = "";
	private String itsAutoLayout = null;
	
	
	public EssentialViewerPublisher(Project aProject, ImportEnvironment importEnv) {
		// Create a service request client
		itsProject = aProject;
		itsKB = aProject.getKnowledgeBase();
		
		itsURL = importEnv.getImportEnvironmentViewerURL() + EssentialViewerPublisher.REPORT_SERVICE_NAME;
		itsUID = importEnv.getViewerUsername();
		itsPassword = importEnv.getViewerPassword();
		
		itIsFinished = false;
		itIsSuccess = false;
		itsReturnCode = 0;
		
		// 19.11.2009 JWC
		itsAutoLayout = ImageAutoLayoutPanel.LAYOUT_RIGHT;
		itsImageURL = IMAGES_URL;
		itsImageURLSuffix = "";
	}
	
	
	public static boolean viewerExists(String aURL){
	    try {
	      String URLName = aURL + EssentialViewerPublisher.REPORT_SERVICE_NAME;
	      HttpURLConnection.setFollowRedirects(false);
	      // note : you may also need
	      //        HttpURLConnection.setInstanceFollowRedirects(false)
	      HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");
	   //   System.out.println("URL RESPONSE CODE: " + con.getResponseCode());
	      return ((con.getResponseCode() == HttpURLConnection.HTTP_OK) || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP);
	    }
	    catch (Exception e) {
	       e.printStackTrace();
	       return false;
	    }
	  }
	
	
	public void publishRepository() {
		if(!this.generateReport()) {
			System.out.println(FAILED_GENERATION_MESSAGE);
		}
	}
	
	
	/**
	 * Generate the XML for the report.
	 * @return true if the XML was generated successfully, false otherwise.
	 */
	public boolean generateReport()
	{
		boolean isSuccess = false;
		
		// Use the XMLRenderer class to get the repository in XML
		// Create a Writer for XMLRenderer to use.
		StringWriter anXMLString = new StringWriter();
			
		try
		{
			
			// Get the XML representation
			// Render the Instances in the KnowledgeBase as XML
			XMLRenderer anXMLRender = new XMLRenderer(itsKB, anXMLString);	
			
			// render the XML
			anXMLRender.render();
			
			// Check for errors and read the XML.			
			// Make sure to encode the String correctly as UTF-8
			byte[] aRenderedXML = anXMLString.toString().getBytes(StandardCharsets.UTF_8);						
			itsReportXML = new String(aRenderedXML, StandardCharsets.UTF_8);
						
			if(itsReportXML != null)
			{
				isSuccess = true;
			}
			else
			{
				isSuccess = false;
			}
		}
		catch(Exception ex)
		{
			System.out.println("Caught the exception:\n" + ex.toString());
			ex.printStackTrace(System.out);
			isSuccess = false;
		}
		
		return isSuccess;
	}
	
	
	public int sendReportXML() {
		HttpReportServiceClient aService = new HttpReportServiceClient();
		aService.setItsURL(itsURL);
		aService.setItsReportXML(itsReportXML);
		aService.setItsUID(itsUID);
		aService.setItsPassword(itsPassword);
		boolean isASuccess = aService.sendReportXML();
		
		// 19.11.2009 JWC - Send the images now.
		/*if(isASuccess)
		{
			aService.setItsImagesURL(itsImageURL);
			aService.setItsKBRef(itsKB);
			aService.setItsAutoLayout(itsAutoLayout);				
			isASuccess = aService.sendImages(); 
		}*/
		
		itIsSuccess = isASuccess;
		itsReturnCode = aService.getItsReturnCode();
		return itsReturnCode;
	}

}
