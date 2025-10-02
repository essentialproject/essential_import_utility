/**
 * Copyright (c)2009-2018 Enterprise Architecture Solutions Ltd.
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
 * 01.02.2017	JWC Further checks and conditions on redirects to home.zul / home_eip.zul
 * 30.03.2018	JP	Added modal dialog when generating DUPs
 * 08.06.2018	JWC	Found and resolved un-initialised timer for DUP pop-up
 * 23.06.2018	JWC Add new argument to ListItemRenderer.render(), RowRenderer.render()
 * 					Upgrade to ZK 8.5, remove ZSS
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

//import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
//import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zss.model.Worksheet;
//import org.zkoss.zss.ui.Spreadsheet;
//import org.zkoss.zss.ui.impl.Utils;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SpreadsheetImportSpecScript;
import com.enterprise_architecture.essential.importutility.data.importspec.script.WorksheetImportSpecScript;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprise_architecture.essential.importutility.integration.DUPGenerator;
//import com.enterprise_architecture.essential.importutility.integration.ProjectLoadException;

/**
 * This class is the UI controller for the page used to edit, test and run
 * import activities
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class RunExcelImportComposer extends EssentialImportInterface {
	
	public Window runExcelImportWin;
	public Label impActNameLbl;
	public Label impActDescLbl;
	public Label impActLastModLbl;
	public Label impActLastTestLbl;
	public Label impActLastRunLbl;
	public Label ssNameLbl; 
	public Grid worksheetGrid;
	public Listbox testEnvListBox;
	public Listbox liveEnvListBox;
	
	public Button editImportSpecBtn;
	public Button downloadImportSpecBtn;
	public Button uploadImportSpecBtn;
	public Button downloadSSBtn;
	//public Button previewSSBtn;
	public Button closePage;
	
	
	private ImportActivity currentImpAct;
	private SpreadsheetImportSpecScript importSpec;
	private ImportUtilityDataManager appDataManager;
	private ImportSpecDataManager impSpecDataManager;
	
	public boolean importSpecLoaded = true;
	
	private EventQueue fileUploadEventQueue;
	
	//Spreadsheet currentSpreadsheet;
	
	/* Properties related to the DUP Generation modal dialog  */
	public Window dupDialog;
	private EventQueue dupEventQueue;
	private int itsProgress = 0;
	private String dupScript;
	private String dupPackagePath;
	private String timerProgressStatus = "";
	public Label dupProgressLabel;
	public Timer dupStatusTimer;
	private static String ENABLED_LABEL_STYLE="font-weight:bold;color:black;";
	private static String DISABLED_LABEL_STYLE="font-weight:bold;color:lightgrey;";
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);	
		
		if(!UserCredentialManager.getInstance(desktop, this.getContextPath()).isAuthenticated(desktop)){
			if (isEIPMode()) {
				execution.sendRedirect("redirectToAuthnServer.zul");
			} else {
				execution.sendRedirect("index.zul");
			}
		    return;
		} 
		
		if(!UserCredentialManager.getInstance(desktop, this.getContextPath()).userIsAdministrator()) {
			editImportSpecBtn.setVisible(false);
			downloadImportSpecBtn.setVisible(false);
			uploadImportSpecBtn.setVisible(false);
		} else {
			editImportSpecBtn.setVisible(true);
			downloadImportSpecBtn.setVisible(true);
			uploadImportSpecBtn.setVisible(true);
		}
		
		this.currentImpAct = (ImportActivity) desktop.getSession().getAttribute("currentImpAct");
		
		if(this.currentImpAct != null) {
			//given an ImportActivity, set up the data managers
			this.impSpecDataManager = this.getImportSpecDataManager(this.currentImpAct);
			
			Boolean importSpecLoading = (Boolean) runExcelImportWin.getAttribute("importSpecUploading");
			if((importSpecLoading == null) || (!importSpecLoading.booleanValue())) {
				this.importSpec = impSpecDataManager.getSpreadsheetImportSpecScriptData();
			} else {
				System.out.println("Import Spec Not Loaded");
			}
			
			//get the application data manager
			this.appDataManager = this.getImportUtilityDataManager();
			
			//...and set the labels in the window
			this.impActNameLbl.setValue(this.currentImpAct.getImportActivityName());
			this.impActDescLbl.setValue(this.currentImpAct.getImportActivityDescription());
			
			String ssFileName = currentImpAct.getSpreadsheetFilename();
			if(ssFileName != null) {
				this.ssNameLbl.setValue(ssFileName);
			} else {
				this.downloadSSBtn.setDisabled(true);
				this.editImportSpecBtn.setDisabled(true);
				this.downloadImportSpecBtn.setDisabled(true);
				this.uploadImportSpecBtn.setDisabled(true);
				//this.previewSSBtn.setDisabled(true);
			}

			//Display the modified date for the Import Activity
			this.setLastModLabel();
	        
			//Display the last tested date for the Import Activity
	        this.setLastTestedLabel();
	        
			if(!this.isEIPMode()) {
				
		        
		      //Display the last executed date for the Import Activity
		        this.setLastRunLabel();
		        
		        //set up the list of test import environments
		        List testImpEnvs = this.getImportUtilityDataManager().getTestImportEnvironments();
		        testImpEnvs.add(0, "- Select Test Environment -");
		        testEnvListBox.setModel(new ListModelList(testImpEnvs));
		        testEnvListBox.setItemRenderer(new ListitemRenderer() {
				    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
				    	String listLabel = "";
				    	if(data.getClass().getName().endsWith("ImportEnvironment")) {
				    		ImportEnvironment testEnv = (ImportEnvironment) data;
				    		listLabel = testEnv.getImportEnvironmentName();
				    	} else {
				    		listLabel = data.toString();
				    	}
				    	new Listcell(listLabel).setParent(listItem);
				    }
		        }); 
		        testEnvListBox.setSelectedIndex(0);
		        
		      //set up the list of live import environments
		        List liveImpEnvs = this.getImportUtilityDataManager().getLiveImportEnvironments();
		        liveImpEnvs.add(0, "- Select Live Environment -");
		        liveEnvListBox.setModel(new ListModelList(liveImpEnvs));
		        liveEnvListBox.setItemRenderer(new ListitemRenderer() {
				    public void render(Listitem listItem, Object data, int theIndex) throws Exception {
				    	String listLabel = "";
				    	if(data.getClass().getName().endsWith("ImportEnvironment")) {
				    		ImportEnvironment liveEnv = (ImportEnvironment) data;
				    		listLabel = liveEnv.getImportEnvironmentName();
				    	} else {
				    		listLabel = data.toString();
				    	}
				    	new Listcell(listLabel).setParent(listItem);
				    }
		        }); 
		        liveEnvListBox.setSelectedIndex(0);  
			}
	        this.setupWorksheetList();
		}  
		else 
		{
			//Executions.sendRedirect("/home.zul");
			if(!this.isEIPMode()) {
				Executions.sendRedirect("/home.zul");
			} else {
				Executions.sendRedirect("/home_eip.zul");
			}
		}
	}
	
	
	private void setupWorksheetList() {
		//set up the list of Worksheets
		System.out.println("REDRAWING WORKSHEET GRID");
        if(importSpec != null) {
        	worksheetGrid.setModel(new ListModelList(importSpec.getWorksheetImportSpec()));
        }
        worksheetGrid.setRowRenderer(new RowRenderer() {
		    public void render(Row aRow, Object data, int theIndex) throws Exception {
		        //get the worksheet object
		    	final WorksheetImportSpecScript worksheet = (WorksheetImportSpecScript) data;
		    	
		    	//set up the isImported checkbox
		        final Checkbox rowCheckbox = new Checkbox();
		        final Intbox firstRowIntBox = new Intbox();
		        
		    	rowCheckbox.setChecked(worksheet.isImported());		    	
		    	//set up the onCheckedListsener
		    	rowCheckbox.addEventListener("onCheck", new EventListener(){
		            public void onEvent(Event event) throws Exception {
		            	//update the current worksheet
		            	worksheet.setImported(rowCheckbox.isChecked());
		            	impSpecDataManager.saveSpreadsheetImportSpecData();
		            }
		        });
		        aRow.appendChild(rowCheckbox);
		        			        
		        aRow.appendChild(new Label(worksheet.getName()));
		        aRow.appendChild(new Label(worksheet.getWorksheetDescription()));
		       
		        
		        
		        if(worksheet.getFirstRow() != null) {
		        	Integer firstInt = new Integer(worksheet.getFirstRow().intValue());
		        	firstRowIntBox.setValue(firstInt);
		    	}
		      //set up the onCheckedListsener
		        firstRowIntBox.addEventListener("onChange", new EventListener(){
		            public void onEvent(Event event) throws Exception {
		            	//update the current worksheet
		            	if(firstRowIntBox.getValue() != null) {
		            		String firstRowString = firstRowIntBox.getValue().toString();
		            		BigInteger bigFirstInt = new BigInteger(firstRowString);
		            		worksheet.setFirstRow(bigFirstInt);
			            	impSpecDataManager.saveSpreadsheetImportSpecData();
		            	}
		            }
		        });
		        aRow.appendChild(firstRowIntBox);
		        
		        final Intbox lastRowIntBox = new Intbox();
		        if(worksheet.getLastRow() != null) {
		        	Integer lastInt = new Integer(worksheet.getLastRow().intValue());
		        	lastRowIntBox.setValue(lastInt);
		    	} 
		      //set up the onCheckedListsener
		        lastRowIntBox.addEventListener("onChange", new EventListener(){
		            public void onEvent(Event event) throws Exception {
		            	//update the current worksheet
		            	if(lastRowIntBox.getValue() != null) {
		            		String lastRowString = lastRowIntBox.getValue().toString();
		            		BigInteger bigLastInt = new BigInteger(lastRowString);
		            		worksheet.setLastRow(bigLastInt);
			            	impSpecDataManager.saveSpreadsheetImportSpecData();
		            	}
		            }
		        });
		    	aRow.appendChild(lastRowIntBox);
		    }
		   }
		); 
	}
	
	
	
	public void onClick$closePage() {
		try {
		//	Window appHomeWin = (Window) desktop.getSession().getAttribute("appHomeWin");
				//	appHomeWin.setClosable(false);
			runExcelImportWin.detach();
			desktop.getSession().removeAttribute("currentImpAct");
			if(!this.isEIPMode()) {
				Executions.sendRedirect("/home.zul");
			} else {
				Executions.sendRedirect("/home_eip.zul");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 24.08.2018 JWC Import Utility ZK 8.5 upgrade
	// Remove the preview dialog window
	/*public void onClick$previewSSBtn() {
		try {
				Window previewSSDialog = (Window) Executions.createComponents("/preview_spreadsheet_dialog.zul", null, null);
				previewSSDialog.doModal();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	*/
	
	public void onClick$editImportSpecBtn() {
		try {
				runExcelImportWin.detach();
				Executions.sendRedirect("/edit_excel_import.zul");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	//Asynchronous event handler to commence saving of an uploaded import specification
	public void onOK$impActNameLbl() {
		//update the import specification associated with the Import Activity
		final Media impSpecFile = (Media) runExcelImportWin.getAttribute("uploadedImportSpec");
		System.out.println("Import Spec Uploaded: " + impSpecFile.inMemory());
		
		final EventQueue eq = EventQueues.lookup("loadImportSpecEvents"); //create a queue
		fileUploadEventQueue = eq;
		
		//subscribe async listeners to handle long operation
		//subscribe an asynchronous listener for saving the import specification
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("saveImportSpec".equals(evt.getName())) 
            {            	
            	if (impSpecFile != null) {
        			appDataManager.updateExcelImpActivityImportSpecFile(currentImpAct, impSpecFile);
        		}
            	
            	eq.publish(new Event("loadImportSpec")); //notify it is done
            }
          }
        }, true); //asynchronous
        
        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("loadImportSpec".equals(evt.getName())) {
	            	try {
	            		impSpecDataManager.openImportSpecForActivity(currentImpAct);
	            	}
	            	catch(Exception e) {
	            		e.printStackTrace();
	            	}
	            	eq.publish(new Event("cleanUpImportLoad"));
	            }
	          }
      }); //synchronous
        
        //subscribe a normal listener to clean up after the upload
        eq.subscribe(new EventListener() {
          public void onEvent(Event evt) {
            if ("cleanUpImportLoad".equals(evt.getName())) {   
    			runExcelImportWin.removeAttribute("uploadedImportSpec");

    			currentImpAct.setImportActivityModifiedDate(getNowAsGregorian());
    			setLastModLabel();
    			
    			saveData();
    			//refresh the list of worksheets to be imported
    			//importSpec = impSpecDataManager.getSpreadsheetImportSpecScriptData();
    			//worksheetGrid.setModel(new ListModelList(importSpec.getWorksheetImportSpec()));
    			
            	fileUploadEventQueue = null;
            	EventQueues.remove("loadImportSpecEvents");
            	
            	runExcelImportWin.removeAttribute("importSpecUploading");
            	
            	//redraw the worksheet list
            	importSpec = impSpecDataManager.getSpreadsheetImportSpecScriptData();
            	setupWorksheetList();
            	//worksheetGrid.invalidate();
            }
          }
        }); //synchronous
        
        
      eq.publish(new Event("saveImportSpec")); //kick off the long operation
        
		
	}
	
	
	//Event handler for when the last tested time for the import activity has changed
	public void onTested$runExcelImportWin() {
		 this.setLastTestedLabel();
	}
	
	
	
	//Event handler for when the last executed time for the import activity has changed
	public void onExecuted$runExcelImportWin() {
		 this.setLastRunLabel();
	}
	
	
	//Asynchronous event handler to commence saving of an uploaded spreadsheeet
	public void onOK$ssNameLbl() {
		//if changed, update the spreadsheet associated with the Import Activity
		Media ssFile = (Media) runExcelImportWin.getAttribute("uploadedSpreadsheet");
		if ((ssFile != null) && (ssFile.isBinary())) {
			try {
				this.appDataManager.updateExcelImpActivitySpreadsheetFile(this.currentImpAct, ssFile);
				runExcelImportWin.removeAttribute("uploadedSpreadsheet");	
				this.currentImpAct.setImportActivityModifiedDate(this.getNowAsGregorian());
				this.setLastModLabel();
				
				this.downloadSSBtn.setDisabled(false);
				this.editImportSpecBtn.setDisabled(false);
				this.downloadImportSpecBtn.setDisabled(false);
				this.uploadImportSpecBtn.setDisabled(false);
				//this.previewSSBtn.setDisabled(false);
				
				this.saveData();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void onClick$runTestExcelImpBtn() {
		try {
			int targetEnvIndex = testEnvListBox.getSelectedIndex();
		//	System.out.println("SELECTED ENV INDEX: " + targetEnvIndex); 
			if(targetEnvIndex > 0) {

				try {
					ImportEnvironment targetEnv = (ImportEnvironment) testEnvListBox.getModel().getElementAt(targetEnvIndex);
					desktop.getSession().setAttribute("currentTargetEnv", targetEnv);			
					Window testImportDialog = (Window) Executions.createComponents("/test_import_activity.zul", runExcelImportWin, null);
					testImportDialog.setTitle("Test Import");		
					testImportDialog.doModal();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				this.displayError("A test environment must be selected", "Test Import Error");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onMessageReceived$runExcelImportWin(Event e) {
		//this.dupMessagesTxtBx.setValue(messages);
	}
	
	private Window getDupDialog() {
		try {
			if(this.dupDialog == null) {
				Map data = new HashMap();
	            data.put("composer","pkg$.RunExcelImportComposer");
	            //Executions.createComponents("test2.zul",mainwin,data);
				this.dupDialog = (Window) Executions.createComponents("/dup_generator_dialog.zul", this.runExcelImportWin, data);
				dupDialog.doModal();
			}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}   
    	return this.dupDialog;
	}
	
	/*public int getItsProgress()
	{
		return itsProgress;
	}
	

	public void updateProgress(String message, int progressPercentage) {
		// TODO Auto-generated method stub
		if(dupEventQueue != null) {
			// itsProgress = progressPercentage;
			messages = messages + message;
			//System.out.println("PROGRESS: " + message);
			dupEventQueue.publish(new Event("updateGenerateDup"));
		}
		
	}*/
	
	
	
	public void onClick$generateDUPBtn() {
		try {
			//initialise the log for the import activity
			User currentUser = UserCredentialManager.getInstance(desktop, getContextPath()).getCurrentUser();
			if(currentUser == null) {
				this.displayError("You must be logged in to generate a DUP file", "Login Error");
				if (isEIPMode()) {
					execution.sendRedirect("redirectToAuthnServer.zul");
				} else {
					execution.sendRedirect("index.zul");
				}
			    return;
			}
			
			//Retrieve the import spec for the Import Activity from the Import Spec Data Manager
			this.importSpec = impSpecDataManager.getSpreadsheetImportSpecScriptData();
						
			//Create an event queue
			final EventQueue eq = EventQueues.lookup("generateDupEvents"); //create a queue
			dupEventQueue = eq;
			
			//Create the DUP progress dialog
			final Window statusDialog = this.getDupDialog();
			final Label dupProgressLabel = (Label) statusDialog.getFellow("dupProgressLabel");	
			final Progressmeter dupProgressMeter = (Progressmeter) statusDialog.getFellow("dupProgressMeter");
			final Textbox dupMessagesTxtBx = (Textbox) statusDialog.getFellow("dupMessagesTxtBx");
			final RunExcelImportComposer scriptGenerationListener = this;
			
			//Add an event listener to the cancel button on the DUP progress dialog
			final Button cancelDUPBtn = (Button) statusDialog.getFellow("cancelDUPBtn");	
			cancelDUPBtn.addEventListener("onClick", new EventListener(){
	            public void onEvent(Event event) throws Exception {
	            	dupProgressLabel.setValue("Cancelling DUP Generation...");
	            	eq.publish(new Event("abortGenerateDup"));
	            }
	        });
			//Add an event listener to the cancel button on the DUP progress dialog
			final Button downloadDUPBtn = (Button) statusDialog.getFellow("downloadDUPBtn");	
			downloadDUPBtn.addEventListener("onClick", new EventListener(){
	            public void onEvent(Event event) throws Exception {
	            	dupProgressLabel.setValue("Downloading DUP...");
	            	try 
	    			{	
	            		if(dupPackagePath != null) {
	            			Filedownload.save(new File(dupPackagePath), "dup");		
	            			dupProgressLabel.setValue("DUP Download Complete");
	            		}
	    			}
	    			catch(Exception e) 
	    			{
	    				messages = messages + "\n" + e.getLocalizedMessage();
	            		dupMessagesTxtBx.setValue(messages);
	    				//e.printStackTrace();
	    			}  
	            	//eq.publish(new Event("downloadDUP"));	      	
	            }
	        });
			//Add an event listener to the cancel button on the DUP progress dialog
			final Button closeDUPBtn = (Button) statusDialog.getFellow("closeDUPBtn");	
			closeDUPBtn.addEventListener("onClick", new EventListener(){
	            public void onEvent(Event event) throws Exception {
	            	dupStatusTimer.stop();
	            	getDupDialog().detach();
	            	
	            	//tidy up
	            	dupDialog = null;
	            	messages = "";
	            	try {
	            		if(dupPackagePath != null) {
	            			dupPackagePath = null;
	            		}
	            	} catch(Exception e) {
	            		messages = messages + "\n" + e.getLocalizedMessage();
	            		dupMessagesTxtBx.setValue(messages);
	            		e.printStackTrace();
	            	}
	            }
	        });
			
			//Add an event listener to the timer to refresh themessages displayed
			final Timer dupProgressTimer = (Timer) statusDialog.getFellow("dupProgressTimer");	
			dupProgressTimer.addEventListener("onTimer", new EventListener(){
	            public void onEvent(Event event) throws Exception {
	            	messages = messages + " .";
	            	dupMessagesTxtBx.setValue(messages);
	            }
	        });
			
			//subscribe async listeners to handle long operation
			//subscribe an asynchronous listener for import execution updates
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("initGenerateDup".equals(evt.getName())) {
	            	//generate a string containing the import script to be included in the DUP package
	            	try {
	            		dupScript = appDataManager.generateDUPImportScript(importSpec, currentImpAct,scriptGenerationListener);	            		
	            	} catch(IOException ioEx) {
	            		//Display the exception messages and disable/enable appropriate buttons
	            		ioEx.printStackTrace();
	            		messages = messages + "\n" + ioEx.getLocalizedMessage();
	            		if(dupEventQueue != null) {
		            		eq.publish(new Event("dupGenerationError"));
		            	}
	    			}
	    			catch(InvalidFormatException ifEx) {
	    				//Stop the timer and display the exception messages and disable/enable appropriate buttons
	    				ifEx.printStackTrace();
	    				scriptGenerationListener.displayError("Error generating DUP Content - Invalid Format", "DUP Generation");
	    				messages = messages + "\n" + ifEx.getLocalizedMessage();
	            		if(dupEventQueue != null) {
		            		eq.publish(new Event("dupGenerationError"));
		            	}
	    			}
	            	if(dupEventQueue != null) {
	            		eq.publish(new Event("packageDup")); //notify it is done
	            	}
	            }
	          }
	        }, true); //asynchronous
			
			//Create the DUP package
	        eq.subscribe(new EventListener() {
		          public void onEvent(Event evt) {
		            if ("packageDup".equals(evt.getName())) {
		            	try {
		            		if(dupEventQueue != null) { 	
			            		dupProgressLabel.setValue("Packaging DUP...");
			            		dupProgressMeter.setValue(75);
			            		DUPGenerator dupGenerator = new DUPGenerator();
			        			dupPackagePath = dupGenerator.generateDUP(scriptGenerationListener.currentImpAct, dupScript, scriptGenerationListener.getContextPath());
			        			//set the date of last tested
			        			scriptGenerationListener.currentImpAct.setImportActivityTestedDate(scriptGenerationListener.getNowAsGregorian());
								
								scriptGenerationListener.appDataManager.saveAppData();
		            		}
		        			
		            	}
		            	catch(Exception e) {
		            		//Stop the timer and display the exception messages and disable/enable appropriate buttons
		            		e.printStackTrace();
		            		messages = messages + "\n" + e.getLocalizedMessage();
		            		if(dupEventQueue != null) {
			            		eq.publish(new Event("dupGenerationError"));
			            	}
		            	}
		            	if(dupEventQueue != null) {
		            		eq.publish(new Event("dupComplete"));
		            	}
		            }
		          }
	        }); //synchronous
			
	      //subscribe a normal listener to abort the DUP generation 
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("abortGenerateDup".equals(evt.getName())) {
	            	//Events.sendEvent("onAbortGenerateDup", runExcelImportWin, null);
	            	dupProgressLabel.setValue("DUP Generation Aborted");
	        		messages = messages + "\nDUP Generation Cancelled.";
	        		dupMessagesTxtBx.setValue(messages);
	            	
	            	downloadDUPBtn.setDisabled(true);
	            	//downloadDUPBtn.setStyle(DISABLED_LABEL_STYLE);
	            	cancelDUPBtn.setDisabled(true);
	        		//cancelDUPBtn.setStyle(DISABLED_LABEL_STYLE);
	        		closeDUPBtn.setDisabled(false);
	        		//closeDUPBtn.setStyle(ENABLED_LABEL_STYLE);
	            	
	        		dupStatusTimer.stop();
	        		dupProgressTimer.stop();
	            	dupEventQueue = null;
	            	EventQueues.remove("generateDupEvents");
	            }
	          }
	        }); //synchronous
	        
	      //subscribe a normal listener to abort the DUP generation 
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("dupGenerationError".equals(evt.getName())) {
	            	dupMessagesTxtBx.setValue(messages);
            		cancelDUPBtn.setDisabled(true);
	        		//cancelDUPBtn.setStyle(DISABLED_LABEL_STYLE);
            		closeDUPBtn.setDisabled(false);
	        		//closeDUPBtn.setStyle(ENABLED_LABEL_STYLE);
	        		dupStatusTimer.stop();
	        		dupProgressTimer.stop();
	        		
	        		dupEventQueue = null;
	            	EventQueues.remove("generateDupEvents");
	            }
	          }
	        }); //synchronous
	        
	      //On completion of DUP generation, enable the download and close buttons and disable the cancel button
	        eq.subscribe(new EventListener() {
	          public void onEvent(Event evt) {
	            if ("dupComplete".equals(evt.getName())) {
	            	dupProgressLabel.setValue("DUP Complete");
	            	dupMessagesTxtBx.setValue(messages);
	            	dupProgressMeter.setValue(100);
	            	downloadDUPBtn.setDisabled(false);
	            	//downloadDUPBtn.setStyle(ENABLED_LABEL_STYLE);
	            	cancelDUPBtn.setDisabled(true);
	        		//cancelDUPBtn.setStyle(DISABLED_LABEL_STYLE);
	        		closeDUPBtn.setDisabled(false);
	        		//closeDUPBtn.setStyle(ENABLED_LABEL_STYLE);
	        		dupStatusTimer.stop();
	        		dupProgressTimer.stop();
	            	
	        		dupEventQueue = null;
	            	EventQueues.remove("generateDupEvents");
	            }
	          }
	        }); //synchronous
	        
	        
	        try {
				statusDialog.doHighlighted();
			}
			catch(Exception e) {				
				scriptGenerationListener.displayError("Failed to load DUP generation window", "DUP Generation Error");
				dupEventQueue = null;
            	EventQueues.remove("generateDupEvents");
				e.printStackTrace();
			}  
	      
	        //Start the DUP timer and publish the event to start the DUP generation process
	        // Check to make sure it is initialised
	        if(dupStatusTimer == null)
	        {	        	
	        	dupStatusTimer = new Timer();
	        }	        
			dupStatusTimer.start();
			
			dupProgressTimer.start();
			
	        eq.publish(new Event("initGenerateDup")); //kick off the long operation
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	/*public void onClick$generateDUPBtn() {
		try {
			//Retrieve the import spec for the Import Activity from the Import Spec Data Manager
			this.importSpec = impSpecDataManager.getSpreadsheetImportSpecScriptData();
			
			//Create the Spreadsheet in memory from the file system
			Workbook spreadsheet;
			try {
				spreadsheet = appDataManager.getSpreadsheetForImportActivity(currentImpAct);
			} catch (InvalidFormatException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//generate a string containing the import script to be included in the DUP package
			String importScript = "";
			try {
				importScript = appDataManager.generateDUPImportScript(importSpec, this.currentImpAct,this);
			} 
			catch(IOException ioEx) {
				ioEx.printStackTrace();
			}
			catch(InvalidFormatException ifEx) {
				ifEx.printStackTrace();
			}
			
			DUPGenerator dupGenerator = new DUPGenerator();
			String dupPackagePath = dupGenerator.generateDUP(this.currentImpAct, importScript, this.getContextPath());
			if(dupPackagePath != null) 
			{
				try 
				{	
					Filedownload.save(new File(dupPackagePath), "dup");
					
					//set the date of last tested
					this.currentImpAct.setImportActivityTestedDate(this.getNowAsGregorian());
					
					//this.impActLastTestLbl.setStyle(EssentialImportInterface.SET_DATE_LABEL_FORMAT);
					this.impActLastTestLbl.setValue(this.formatXMLCalendar(currentImpAct.getImportActivityTestedDate()));
					this.appDataManager.saveAppData();
				}
				catch(Exception e) 
				{
					e.printStackTrace();
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}*/
	
	
	
	public void onClick$runProdExcelImpBtn() {
		try {
			int targetEnvIndex = liveEnvListBox.getSelectedIndex();
			if(targetEnvIndex > 0) {

				try {
					ImportEnvironment targetEnv = (ImportEnvironment) liveEnvListBox.getModel().getElementAt(targetEnvIndex);
					desktop.getSession().setAttribute("currentTargetEnv", targetEnv);			
					Window liveImportDialog;
					if(targetEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
						liveImportDialog = (Window) Executions.createComponents("/run_import_activity_to_local.zul", runExcelImportWin, null);
					} else {
						liveImportDialog = (Window) Executions.createComponents("/run_import_activity_to_server.zul", runExcelImportWin, null);
					}
					liveImportDialog.setTitle("Live Import");		
					liveImportDialog.doModal();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				this.displayError("A live environment must be selected", "Live Import Error");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	public void onClick$manageActivityLogsBtn() {
				try {			
					Window manageImportLogsDialog = (Window) Executions.createComponents("/manage_import_logs.zul", runExcelImportWin, null);		
					manageImportLogsDialog.doModal();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
	}
	
	
	
	/**
	 * Updated JWC 26.06.2017 - Zip the downloaded import spec.
	 */
	public void onClick$downloadImportSpecBtn() 
	{
		String specFilePath = impSpecDataManager.getImportSpecFilePath();
		if(specFilePath != null) 
		{
			try 
			{
				// Compress the specFile defined by specFilePath
				String aZippedSpecFilePath = appDataManager.getZippedImportSpec(specFilePath);				
				Filedownload.save(new File(aZippedSpecFilePath), "zip");				
			}
			catch(Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void onClick$downloadSSBtn() {
		String ssFilePath = appDataManager.getFullImpActSpreadsheetPath(currentImpAct);
		if(ssFilePath != null) {
			try {
				Filedownload.save(new File(ssFilePath), "excel");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void setLastModLabel() {
		//Display the last executed date for the Import Activity
        XMLGregorianCalendar impActModDate = this.currentImpAct.getImportActivityModifiedDate();
        if(impActModDate != null) {
        	impActLastModLbl.setStyle(EssentialImportInterface.SET_DATE_LABEL_FORMAT);
        	impActLastModLbl.setValue(this.formatXMLCalendar(impActModDate));
        } else {
        	impActLastModLbl.setStyle(EssentialImportInterface.EMPTY_DATE_LABEL_FORMAT);
        	impActLastModLbl.setValue("Never");
        }
	}
	
	
	private void setLastTestedLabel() {
		//Display the last tested date for the Import Activity
        XMLGregorianCalendar impActTestedDate = this.currentImpAct.getImportActivityTestedDate();
        if(impActTestedDate != null) {
        	impActLastTestLbl.setStyle(EssentialImportInterface.SET_DATE_LABEL_FORMAT);
        	impActLastTestLbl.setValue(this.formatXMLCalendar(impActTestedDate));
        } else {
        	impActLastTestLbl.setStyle(EssentialImportInterface.EMPTY_DATE_LABEL_FORMAT);
        	impActLastTestLbl.setValue("Never");
        }
	}
	
	
	private void setLastRunLabel() {
		//Display the last executed date for the Import Activity
        XMLGregorianCalendar impActRunDate = this.currentImpAct.getImportActivityToLiveDate();
        if(impActRunDate != null) {
        	impActLastRunLbl.setStyle(EssentialImportInterface.SET_DATE_LABEL_FORMAT);
        	impActLastRunLbl.setValue(this.formatXMLCalendar(impActRunDate));
        } else {
        	impActLastRunLbl.setStyle(EssentialImportInterface.EMPTY_DATE_LABEL_FORMAT);
        	impActLastRunLbl.setValue("Never");
        }
	}

}
