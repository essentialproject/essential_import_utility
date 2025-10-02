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
 * 28.06.2011	JP	1st coding.
 * 09.03.2017	JWC Added XML import handling
 * 
 */
package com.enterprise_architecture.essential.importutility.data.importspec.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.zkoss.zk.ui.WebApp;

import com.enterprise_architecture.essential.importutility.data.common.*;
import com.enterprise_architecture.essential.importutility.data.common.script.*;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.SpreadsheetImportSpec;
import com.enterprise_architecture.essential.importutility.data.importspec.WorksheetImportSpec;
import com.enterprise_architecture.essential.importutility.utils.EssentialServletContext;

/**
 * This class is responsible for managing the marshalling and unmarshalling of data supporting
 * the definition of Import Specifications and the generation of Essential Import Scripts
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @author Jonathan Carter
 * @version 1.0 - 29.06.2011
 * @version 2.0 - 23.03.2015
 * @version 3.0 - 09.03.2017
 *
 */
public class ImportSpecDataManager {
	
	private static final String IMPORT_SPEC_SCHEMA_VERSION = "1.0";
	
	public static final String IMPORT_MATCHMODE_BYNAME = "ByName";
	public static final String IMPORT_MATCHMODE_BYID = "ByID";
	public static final String IMPORT_MATCHMODE_BYINTID = "ByInternalID";
	public static final String IMPORT_MATCHMODE_NONE = "NotApplicable";
	public static final String IMPORT_MATCHMODE_NEW = "New";
	public static final String IMPORT_MATCHMODE_DUP = "DUP";
	
	public static final String INTEGER_PRIMITIVE_TYPE = "Integer";
	public static final String FLOAT_PRIMITIVE_TYPE = "Float";
	public static final String BOOLEAN_PRIMITIVE_TYPE = "Boolean";
	
	public static final int SPEC_ELEMENT_LEVEL = 0;
	public static final int ROOT_ELEMENT_LEVEL = 1;
	public static final int INSTANCE_ELEMENT_LEVEL = 2;
	public static final int SLOT_ELEMENT_LEVEL = 3;
	public static final int VALUE_ELEMENT_LEVEL = 4;
	
	public static final String UNDEFINED_VALUE = "<UNDEFINED>";
	
	public static final String NAME_SLOT_NAME = "name";
	public static final String RELATION_NAME_SLOT_NAME = "relation_name";
	public static final String GRAPH_RELATION_NAME_SLOT_NAME = ":relation_name";
	public static final String EXTID_SLOT_NAME = "ext_id";
	
	public static String SLOT_NAME_TOKEN ="SLOTNAME";
	public static String SLOT_VALUE_TOKEN ="SLOTVALUE";
	public static String CLASS_NAME_TOKEN ="CLASSNAME";
	public static String VARIABLE_NAME_TOKEN ="INSTVAR";
	public static String INTERNAL_ID_TOKEN ="INTREF";
	public static String INSTANCE_ID_TOKEN ="EXTREF";
	public static String REPOSITORY_NAME_TOKEN ="REPNAME";
	public static String INSTANCE_NAME_TOKEN ="INSTNAME";
	public static String MATCHING_STRING_TOKEN ="MATCHSTRING";
	public static String INSTANCE_SLOT_VARIABLE_TOKEN ="INSTSLOTVAR";
	public static String PRIMITIVE_SLOT_TYPE_TOKEN ="SLOTTYPE";
	public static String TIMESTAMP_TOKEN ="TIMESTAMP";
	public static String WORKSHEET_SEQUENCE_NO ="SEQUENCENO";
	public static String WORKSHEET_NAME ="SHEETNAME";
	public static String WORKSHEET_INDEX_NO ="SHEETINDEX";
	public static String WORKSHEET_FIRST_ROW ="FIRST";
	public static String WORKSHEET_LAST_ROW ="LAST";
	
	public static String SCRIPT_TERMINATOR_TEMPLATE_NAME = "Script Section Terminator";
	
	
	
	
	//private String contextDirPath;
	private ServletContext servletContext;
	
	private String importSpecFilePath;
	protected SpreadsheetImportSpecScript spreadsheetImportSpec;
	
	
	
	/*public ImportSpecDataManager(String contextPath, ImportActivity impAct) {
		super();
		this.contextDirPath = contextPath;
		this.openImportSpecForActivity(impAct);
	}*/
	
	public ImportSpecDataManager(WebApp aWebApp, ImportActivity impAct) {
		super();		
		//this.servletContext = (ServletContext) aWebApp.getNativeContext();
		EssentialServletContext aContext = new EssentialServletContext((ServletContext) aWebApp.getNativeContext());
		this.servletContext = aContext;
		this.openImportSpecForActivity(impAct);
	}
	
	
	/*public ImportSpecDataManager(String contextPath) {
		super();
		this.contextDirPath = contextPath;
	}*/
	
	
	public ImportSpecDataManager(ServletContext ctxt) {
		super();
		//this.servletContext = ctxt;
		EssentialServletContext aContext = new EssentialServletContext(ctxt);
		this.servletContext = aContext;
		 
	}
	
	public ImportSpecDataManager(ServletContext ctxt, ImportActivity impAct) {
		super();
		//this.servletContext = ctxt;
		EssentialServletContext aContext = new EssentialServletContext(ctxt);
		this.servletContext = aContext;
		this.openImportSpecForActivity(impAct);
	}
	
	public ImportSpecDataManager(WebApp aWebApp) {
		super();
		//this.servletContext = (ServletContext) aWebApp.getNativeContext();
		EssentialServletContext aContext = new EssentialServletContext((ServletContext) aWebApp.getNativeContext());
		this.servletContext = aContext;
		
	}
	
	/**
	 * @return void
	 * 
	 * initialise the Hashmap of DerivedValue Templates
	 */
	public static HashMap<String, HashMap<String, String>> getImportScriptTokensMap() {
		HashMap<String, HashMap<String, String>> importScriptTokensMap = new HashMap<String, HashMap<String, String>>();
		
		//add the Spreadsheet Import Spec Tokens
		importScriptTokensMap.put(SpreadsheetImportSpecScript.TEMPLATE_NAME, SpreadsheetImportSpecScript.getScriptTokens());
		
		//add the Worksheet Import Spec Tokens
		importScriptTokensMap.put(WorksheetImportSpecScript.TEMPLATE_NAME, WorksheetImportSpecScript.getScriptTokens());
		
		//add the Global Instances Import Spec Tokens
		importScriptTokensMap.put(GlobalInstancesScript.TEMPLATE_NAME, GlobalInstancesScript.getScriptTokens());
		
		//add the Global Instance Import Spec Tokens
		importScriptTokensMap.put(GlobalInstanceScript.TEMPLATE_NAME, GlobalInstanceScript.getScriptTokens());
		
		//add the Simple Instance Import Spec Tokens
		importScriptTokensMap.put(SimpleInstanceTypeScript.TEMPLATE_NAME, SimpleInstanceTypeScript.getScriptTokens());
		
		//add the Delete Simple Instance Import Spec Tokens
		importScriptTokensMap.put(DeleteSimpleInstanceTypeScript.TEMPLATE_NAME, DeleteSimpleInstanceTypeScript.getScriptTokens());
		
		//add the Derived Instance Import Spec Tokens
		importScriptTokensMap.put(DerivedInstanceTypeScript.TEMPLATE_NAME, DerivedInstanceTypeScript.getScriptTokens());
		
		//add the Delete Derived Instance Import Spec Tokens
		importScriptTokensMap.put(DeleteDerivedInstanceTypeScript.TEMPLATE_NAME, DeleteDerivedInstanceTypeScript.getScriptTokens());
		
		//add the Simple Slot Import Spec Tokens
		importScriptTokensMap.put(SimpleSlotScript.TEMPLATE_NAME, SimpleSlotScript.getScriptTokens());
		
		//add the Derived Simple Slot Import Spec Tokens
		importScriptTokensMap.put(DerivedSimpleSlotScript.TEMPLATE_NAME, DerivedSimpleSlotScript.getScriptTokens());
		
		//add the Instance Slot Import Spec Tokens
		importScriptTokensMap.put(InstanceSlotScript.TEMPLATE_NAME, InstanceSlotScript.getScriptTokens());
		
		//add the Derived Instance Import Spec Tokens
		importScriptTokensMap.put(PrimitiveSlotScript.TEMPLATE_NAME, PrimitiveSlotScript.getScriptTokens());
		
		//add the Derived Instance Import Spec Tokens
		importScriptTokensMap.put(ImportSpecDataManager.SCRIPT_TERMINATOR_TEMPLATE_NAME, new HashMap<String, String>());
		
		//add the Remove Instance Slot Import Spec Tokens
		importScriptTokensMap.put(RemoveInstanceSlotScript.TEMPLATE_NAME, RemoveInstanceSlotScript.getScriptTokens());
		
		//add the Remove All Instance Slot Import Spec Tokens
		importScriptTokensMap.put(RemoveAllInstanceSlotScript.TEMPLATE_NAME, RemoveAllInstanceSlotScript.getScriptTokens());
		
		//add the Delete Instance Slot Import Spec Tokens
		importScriptTokensMap.put(DeleteInstanceSlotScript.TEMPLATE_NAME, DeleteInstanceSlotScript.getScriptTokens());
		
		//add the Delete All Instance Slot Import Spec Tokens
		importScriptTokensMap.put(DeleteAllInstanceSlotScript.TEMPLATE_NAME, DeleteAllInstanceSlotScript.getScriptTokens());
		
		
		return importScriptTokensMap;
		
	}
	
	
	/**
	 * @return the importSpecFilePath
	 */
	public String getImportSpecFilePath() {
		return importSpecFilePath;
	}
	
	
	public void openImportSpecForActivity(ImportActivity impAct) {
		//if the context path has been set, open the supplied Import Activity
		
		this.spreadsheetImportSpec = null;
		this.importSpecFilePath = null;
		
		if(this.servletContext != null) {
			this.importSpecFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName() + File.separator + impAct.getSpreadsheetImportSpecFilename());
			File importSpecFile = new File(this.importSpecFilePath);
			//System.out.println("IMPORT SPEC FILE PATH: " + importSpecFilePath);
			
			//if the import spec file does not yet exist, create it and cache the instance in a member variable
			if (!importSpecFile.exists()) {
				//System.out.println("CREATING NEW IMPORT SPEC");
				
				//create an empty Spreadsheet Import Spec Script instance
				this.spreadsheetImportSpec = this.newSpreadsheetImportSpec();
				
				//set the schema version for the Import Spec
				spreadsheetImportSpec.setImportSchemaVersion(ImportSpecDataManager.IMPORT_SPEC_SCHEMA_VERSION);
				
				//save the newly created import spec
				this.saveSpreadsheetImportSpecData();
			} else {
				// if the file does exist, then load the import specification object from the appropriate file
				try {
					
					//System.out.println("OPENING EXISTING IMPORT SPEC: " + this.importSpecFilePath);
					JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.importspec") ;
					Unmarshaller   unmarshaller = context.createUnmarshaller() ;
					//unmarshaller.setProperty(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl.FACTORY, new ObjectFactoryScript());
					// Problems finding this package, so define the static FACTORY property explicitly - JWC 20.07.2018
					//unmarshaller.setProperty("com.sun.xml.bind.ObjectFactory", new ObjectFactoryScript());
					//unmarshall the import spec and create a Spreadsheet Import Spec Script instance based on the loaded import spec
					SpreadsheetImportSpec importSpec = (SpreadsheetImportSpec) unmarshaller.unmarshal(new FileInputStream(this.importSpecFilePath));
						this.spreadsheetImportSpec = new SpreadsheetImportSpecScript(importSpec);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	
	
	
	/**
	 * @return void
	 * 
	 * Save the Spreadsheet Import Spec data
	 */
	public void saveSpreadsheetImportSpecData() {
		if (this.importSpecFilePath != null) {
			//System.out.println("SAVING IMPORT SPEC FILE PATH: " + importSpecFilePath);
			try {
				JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.importspec") ;
				Marshaller   marshaller = context.createMarshaller();
				marshaller.marshal(this.spreadsheetImportSpec, new FileOutputStream(this.importSpecFilePath)) ; 
				//System.out.println("Saved Import Spec to: " + this.importSpecFilePath);
				//System.out.println(">>>> Worksheet count: " + this.spreadsheetImportSpec.getWorksheetImportSpec().size());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * @return the spreadsheetImportSpec
	 */
	public SpreadsheetImportSpecScript getSpreadsheetImportSpecScriptData() {
		return spreadsheetImportSpec;
	}
	
	
	/**
	 * @return a new SpreadsheetImportSpec instance and initialise the GlobalInstances attribute	
	 */
	public SpreadsheetImportSpecScript newSpreadsheetImportSpec() {
		com.enterprise_architecture.essential.importutility.data.importspec.script.ObjectFactoryScript importSpecFactory = new com.enterprise_architecture.essential.importutility.data.importspec.script.ObjectFactoryScript();
		
		SpreadsheetImportSpecScript importSpec = (SpreadsheetImportSpecScript) importSpecFactory.createSpreadsheetImportSpec();
		importSpec.setGlobalInstances(this.newGlobalInstances());
		importSpec.setImportSchemaVersion(IMPORT_SPEC_SCHEMA_VERSION);
		return importSpec;
	}
	
	
	/**
	 * @return a new WorksheetImportSpec instance
	 */
	public WorksheetImportSpecScript newWorksheetImportSpec() {
		com.enterprise_architecture.essential.importutility.data.importspec.script.ObjectFactoryScript importSpecFactory = new com.enterprise_architecture.essential.importutility.data.importspec.script.ObjectFactoryScript();
		
		WorksheetImportSpecScript importSpec = (WorksheetImportSpecScript) importSpecFactory.createWorksheetImportSpec();
		importSpec.setImported(true);
		return importSpec;
	}
	
	
	/**
	 * @return a new GlobalInstances instance
	 */
	public GlobalInstancesScript newGlobalInstances() {
		com.enterprise_architecture.essential.importutility.data.importspec.script.ObjectFactoryScript importSpecFactory = new com.enterprise_architecture.essential.importutility.data.importspec.script.ObjectFactoryScript();
		
		GlobalInstancesScript globInstances = (GlobalInstancesScript) importSpecFactory.createGlobalInstances();
		return globInstances;
	}
	
	
	/**
	 * @return a new GlobalInstance instance
	 */
	public GlobalInstanceScript newGlobalInstance() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory = new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		GlobalInstanceScript globInstance = (GlobalInstanceScript) commonFactory.createGlobalInstanceScript();
		
		return globInstance;
	}
	
	
	/**
	 * @return a new DerivedInstanceTypeScript instance
	 */
	public DerivedInstanceTypeScript newDerivedInstanceType() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DerivedInstanceTypeScript derivedInstance = (DerivedInstanceTypeScript) commonFactory.createDerivedInstanceType();
		return derivedInstance;
	}
	
	/**
	 * @return a new SimpleInstanceTypeScript instance
	 */
	public SimpleInstanceTypeScript newSimpleInstanceType() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		SimpleInstanceTypeScript simpleInstance = (SimpleInstanceTypeScript) commonFactory.createSimpleInstanceType();
		return simpleInstance;
	}
	
	
	/**
	 * @return a new DeleteDerivedInstanceTypeScript instance
	 */
	public DeleteDerivedInstanceTypeScript newDeleteDerivedInstanceType() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DeleteDerivedInstanceType delDerivedInstance = (DeleteDerivedInstanceType) commonFactory.createDeleteDerivedInstanceType();
		return new DeleteDerivedInstanceTypeScript(delDerivedInstance);
	}
	
	/**
	 * @return a new DeleteSimpleInstanceTypeScript instance
	 */
	public DeleteSimpleInstanceTypeScript newDeleteSimpleInstanceType() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DeleteSimpleInstanceType delSimpleInstance = (DeleteSimpleInstanceType) commonFactory.createDeleteSimpleInstanceType();
		return new DeleteSimpleInstanceTypeScript(delSimpleInstance);
	}
	
	
	/**
	 * @return a new SimpleSlot instance
	 */
	public SimpleSlotScript newSimpleSlot() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		SimpleSlotScript simpleSlot = (SimpleSlotScript) commonFactory.createSimpleSlot();
		return simpleSlot;
	}
	
	
	/**
	 * @return a new DerivedSimpleSlot instance
	 */
	public DerivedSimpleSlotScript newDerivedSimpleSlot() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DerivedSimpleSlotScript derivedSimpleSlot = (DerivedSimpleSlotScript) commonFactory.createDerivedSimpleSlot();
		return derivedSimpleSlot;
	}
	
	
	/**
	 * @return a new PrimitiveSlot instance
	 */
	public PrimitiveSlotScript newPrimitiveSlot() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		PrimitiveSlotScript primitiveSlot = (PrimitiveSlotScript) commonFactory.createPrimitiveSlot();
		return primitiveSlot;
	}
	
	
	/**
	 * @return a InstanceSlot instance
	 */
	public InstanceSlotScript newInstanceSlot() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		InstanceSlotScript instanceSlot = (InstanceSlotScript) commonFactory.createInstanceSlot();
		return instanceSlot;
	}
	
	/**
	 * @return a RemoveInstanceSlot instance
	 */
	public RemoveInstanceSlotScript newRemoveInstanceSlot() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		RemoveInstanceSlot remInstanceSlot = (RemoveInstanceSlot) commonFactory.createRemoveInstanceSlot();
		return new RemoveInstanceSlotScript(remInstanceSlot);
	}
	
	
	/**
	 * @return a DeleteInstanceSlot instance
	 */
	public DeleteInstanceSlotScript newDeleteInstanceSlot() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DeleteInstanceSlot delInstanceSlot = (DeleteInstanceSlot) commonFactory.createDeleteInstanceSlot();
		return new DeleteInstanceSlotScript(delInstanceSlot);
	}
	
	
	/**
	 * @return a RemoveAllInstanceSlot instance
	 */
	public RemoveAllInstanceSlotScript newRemoveAllInstanceSlot() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		RemoveAllInstanceSlot remAllInstanceSlot = (RemoveAllInstanceSlot) commonFactory.createRemoveAllInstanceSlot();
		return new RemoveAllInstanceSlotScript(remAllInstanceSlot);
	}
	
	
	/**
	 * @return a DeleteAllInstanceSlot instance
	 */
	public DeleteAllInstanceSlotScript newDeleteAllInstanceSlot() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory= new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DeleteAllInstanceSlot delAllInstanceSlot = (DeleteAllInstanceSlot) commonFactory.createDeleteAllInstanceSlot();
		return new DeleteAllInstanceSlotScript(delAllInstanceSlot);
	}
	
	
	
	
	/**
	 * @return a new DerivedValue instance
	 */
	public DerivedValueScript newDerivedValue() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory = new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DerivedValueScript derivedValue = (DerivedValueScript) commonFactory.createDerivedValue();
		return derivedValue;
	}
	
	
	/**
	 * @return a new DerivedValueString instance
	 */
	public DerivedValueStringScript newDerivedValueString() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory = new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DerivedValueStringScript derivedValueString = (DerivedValueStringScript) commonFactory.createDerivedValueString();
		return derivedValueString;
	}
	
	
	/**
	 * @return a new DerivedValueString instance
	 */
	public DerivedValueRefScript newDerivedValueRef() {
		com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript commonFactory = new com.enterprise_architecture.essential.importutility.data.common.script.ObjectFactoryScript();
		
		DerivedValueRefScript derivedValueRef = (DerivedValueRefScript) commonFactory.createDerivedValueRef();
		return derivedValueRef;
	}
	
	/**
	 * @return List<String> a list of Simple Instance variable names
	 */
	public List<String> getSimpleInstanceOlderSiblingVars(SimpleInstanceTypeScript simpleInstance, WorksheetImportSpec worksheet) {
		ArrayList<String> variableList = new ArrayList<String>();
		List worksheetChildren = worksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance();
		Iterator childIter = worksheetChildren.iterator();
		String aVar;
		while(childIter.hasNext()) {
			Object child = childIter.next();
			if(child != simpleInstance) {
				if(child.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript")) {
					SimpleInstanceTypeScript childSimpleInstance = (SimpleInstanceTypeScript) child;
					variableList.add(childSimpleInstance.getVariableName());
				} else if(child.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.DerivedInstanceTypeScript")) {
					DerivedInstanceTypeScript childDerivedInstance = (DerivedInstanceTypeScript) child;
					variableList.add(childDerivedInstance.getVariableName());
				}
			}
			else {
				break;
			}
		}
		return variableList;
	}
	
	
	/**
	 * @return List<String> a list of Derived Instance variable names
	 */
	public List<String> getDerivedInstanceOlderSiblingVars(DerivedInstanceTypeScript derivedInstance, WorksheetImportSpec worksheet) {
		ArrayList<String> variableList = new ArrayList<String>();
		List worksheetChildren = worksheet.getDerivedInstanceOrSimpleInstanceOrDeleteDerivedInstance();
		Iterator childIter = worksheetChildren.iterator();
		String aVar;
		while(childIter.hasNext()) {
			Object child = childIter.next();
			if(child != derivedInstance) {
				if(child.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript")) {
					SimpleInstanceTypeScript childSimpleInstance = (SimpleInstanceTypeScript) child;
					variableList.add(childSimpleInstance.getVariableName());
				} else if(child.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.DerivedInstanceTypeScript")) {
					DerivedInstanceTypeScript childDerivedInstance = (DerivedInstanceTypeScript) child;
					variableList.add(childDerivedInstance.getVariableName());
				}
			}
			else {
				break;
			}
		}
		return variableList;
	}
	
	
	/**
	 * @return DerivedValueScript based on a Global Value Template of a given Class and Slot
	 */
	public DerivedValueScript getDerivedValueFromTemplate(DerivedValue template) {
		List valueSegments = template.getDerivedValueStringOrDerivedValueRef();
		Iterator segmentsIter = valueSegments.iterator();
		DerivedValueScript newValue = this.newDerivedValue();
		while(segmentsIter.hasNext()) {
			Object segment = segmentsIter.next();
			if(segment.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.DerivedValueString")) {
				
				DerivedValueString stringSegment = (DerivedValueString) segment;
				DerivedValueStringScript newSegment = this.newDerivedValueString();
				newSegment.setContent(stringSegment.getContent());
				newValue.getDerivedValueStringOrDerivedValueRef().add(newSegment);
			} else {
				DerivedValueRef valueSegment = this.newDerivedValueRef();
				valueSegment.setValue(ImportSpecDataManager.UNDEFINED_VALUE);
				newValue.getDerivedValueStringOrDerivedValueRef().add(valueSegment);
			}
		}
		return newValue;
	}
	
	/**
	 * Get the XML transform XSL script associated with the Import Specification
	 * If the XML transform is a ZIP rather than a simple XSL, use the RootXSL attribute
	 * to find the correct transform file
	 * @param theImportActivity the import activity that is being used.
	 * @return the full path of the transform XSL file.
	 */
	public String getXMLImportSpecScript(ImportActivity theImportActivity)
	{
		String aTransformXSLFile = "";
		
		// Check whether the Import Activity has been uploaded as a ZIP of content
		// or a simple XSL file
		if(theImportActivity.getXmlTransformFilename().endsWith("zip"))
		{
			// Read the root XSL variable to get the XSL document filename
			aTransformXSLFile = theImportActivity.getRootXslFilename();			
		}
		else
		{
			// Just return the XSL document held in the XSL Transform Filename
			aTransformXSLFile = theImportActivity.getXmlTransformFilename();
		}
		
		// Read the XSL file into anXSLTransformContent and return that.
		String anXSLFileFullPath = servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + theImportActivity.getImportActivityRootDirName() + File.separator + aTransformXSLFile);
		
		return anXSLFileFullPath;
	}
	
}
