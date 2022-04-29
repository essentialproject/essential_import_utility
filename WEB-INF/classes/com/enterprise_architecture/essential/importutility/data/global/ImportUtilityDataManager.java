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
 * 07.05.2014	JWC	Additional development
 * 28.11.2016	JWC	New variant of the generateDUPImportScript for DUP service interface
 * 08.12.2016	JWC New log initialization method for EIP users calling the DUP service
 * 10.03.2017	JWC New Support for the XML Import Service 
 * 25.05.2018	JWC New parameter to improve DUP logging
 * 23.08.2018	JWC Upgrade to ZK 8.5 - removed the ZK Spreadsheet code
 * 
 */
package com.enterprise_architecture.essential.importutility.data.global;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
//import org.zkoss.zss.ui.Spreadsheet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileWriter;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.enterprise_architecture.essential.importutility.data.common.DerivedInstanceType;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;
import com.enterprise_architecture.essential.importutility.data.importspec.script.SpreadsheetImportSpecScript;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.integration.DUPGenerator;
import com.enterprise_architecture.essential.importutility.integration.FileCompressionManager;
import com.enterprise_architecture.essential.importutility.integration.ProtegeProjectFilenameFilter;
import com.enterprise_architecture.essential.importutility.utils.Log;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * This class is responsible for managing the marshalling and unmarshalling of global data related
 * to the application
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @author Jonathan W. Carter <jonathan.carter@e-asolutions.com>
 * @version 1.0 - 29.06.2011<br/>
 * @version 2.0 - 16.05.2014<br/>
 * @version 3.0 - 10.03.2017<br/>
 *
 */
public class ImportUtilityDataManager {
	
	/**
	 * Name used for the application level attribute containing the Import Utility Data Manager
	 */
	public static final String IMPORT_UTILITY_DATA_MANAGER_NAME = "appDataManager";
	
	private static final String CONFIG_FOLDER = "config";
	private static final String CONFIG_FILE_NAME = "config.xml";
	private static final String SYSTEM_CONFIG_FILE_NAME = "system_config.xml";
	
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();
	
	public static final String IMPORT_TEMPLATES_ROOT_PATH = "import_templates";
	public static final String IMPORT_ACTIVITIES_ROOT_PATH = "import_activities";
	public static final String REPOSITORY_CACHE_ROOT_PATH = "repository_cache";
	
	public static final String VALUE_REF_SYMBOL = "[REF]";
	
	public static final String IMPORT_ACTIVITY_PENDINGSTATUS = "Pending";
	public static final String IMPORT_ACTIVITY_TESTEDSTATUS = "Tested";
	public static final String IMPORT_ACTIVITY_EXECUTEDEDSTATUS = "Executed";
	
	public static final String IMPORT_ACTIVITY_LOG_UPLOADEDSTATUS = "Repository Uploaded";
	public static final String IMPORT_ACTIVITY_LOG_EXECUTEDEDSTATUS = "Import Executed";
	public static final String IMPORT_ACTIVITY_LOG_PUBLISHEDSTATUS = "Repository Published";
	
	public static final String IMPORT_ENV_DEPLOYMENT_LOCAL = "LOCAL";
	public static final String IMPORT_ENV_DEPLOYMENT_SERVER = "SERVER";
	
	
	public static final String IMPORT_ENV_DEV_ROLE = "DEV";
	public static final String IMPORT_ENV_QA_ROLE = "QA";
	public static final String IMPORT_ENV_PROD_ROLE = "LIVE";
	
	public static final String IMPORT_UTILITY_VERSION = "1.0";
	
	
	public static final int IMPORT_SCRIPT_CHUNK_SIZE = 50;
	
	private String configFilePath;
	private String systemConfigFilePath;


	//private String contextDirPath;
	private ServletContext servletContext;
	
	private HashMap<String, HashMap<String, DerivedValue>> valueTemplateMap;
	private HashMap<String, HashMap<String, String>> scriptTemplateMap;
	
	private ImportActivity currentImpAct;
	private ImportActivityLog currentImpActLog;
	//private Spreadsheet currentSpreadsheet;
	private String currentImportScript = "";


	protected EssentialImportUtility importUtilityData;
	protected EssentialImportUtility systemData;


	
	public ImportUtilityDataManager(ServletContext aServletContext) {
		super();
		
		//set the context path for the application
		this.servletContext = aServletContext;
		
		//initialise the folders that are used to hold files related to the application
		this.initAppFolders();
		
		
		//load the system configuration data for the application
		this.systemConfigFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.CONFIG_FOLDER + File.separator + ImportUtilityDataManager.SYSTEM_CONFIG_FILE_NAME);
		try {
			JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.global") ;
			Unmarshaller   unmarshaller = context.createUnmarshaller() ;
			systemData = (EssentialImportUtility)unmarshaller.unmarshal(new FileInputStream(systemConfigFilePath));
			
			this.initImportScriptTemplateHashmap(this.systemData.getImportScriptTemplates());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		//load the user configuration data for the application
		this.configFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.CONFIG_FOLDER + File.separator + ImportUtilityDataManager.CONFIG_FILE_NAME);
		if(new File(configFilePath).exists()) {
			try {
				JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.global") ;
				Unmarshaller   unmarshaller = context.createUnmarshaller() ;
				importUtilityData = (EssentialImportUtility)unmarshaller.unmarshal(new FileInputStream(configFilePath));
				this.initValueTemplateHashmap(this.importUtilityData.getGlobalValueTemplates());
		//		this.initImportScriptTemplateHashmap(this.systemData.getImportScriptTemplates());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			this.importUtilityData = this.newUserConfig();
			this.saveAppData();   
		}
		
	}
	
	
	/**
	 * @return the systemData
	 */
	public EssentialImportUtility getSystemData() {
		return systemData;
	}
	
	
	/**
	 * @return the importUtilityData
	 */
	public EssentialImportUtility getImportUtilityData() {
		return importUtilityData;
	}
	
	
	/**
	 * @return the configFilePath
	 */
	public String getConfigFilePath() {
		return configFilePath;
	}

	
	
	/**
	 * @return List<ImportEnvironment>, the list of ImportEnvironments with deployment role of 'Live'
	 */
	public List<ImportEnvironment> getLiveEnvironments() {
		List<ImportEnvironment> targetEnvs = this.importUtilityData.getImportEnvironments();
		Iterator<ImportEnvironment> envIter = targetEnvs.iterator();
		
		List<ImportEnvironment> liveEnvs = new ArrayList<ImportEnvironment>();
		ImportEnvironment anEnv;
		while(envIter.hasNext()) {
			anEnv = envIter.next();
			if(anEnv.getImportEnvironmentRole().equals("LIVE")) {
				liveEnvs.add(anEnv);
			}
		}
		return liveEnvs;
	}
	
	
	/**
	 * @return the scriptTemplateMap
	 */
	public HashMap<String, HashMap<String, String>> getScriptTemplateMap() {
		return scriptTemplateMap;
	}
	
	/**
	 * @return void
	 * 
	 * initialise the Hashmap of DerivedValue Templates
	 */
	private void initValueTemplateHashmap(List<GlobalValueTemplate> globalTemplates) {
		valueTemplateMap = new HashMap<String, HashMap<String, DerivedValue>>();
		Iterator<GlobalValueTemplate> templatesIter = globalTemplates.iterator();
		GlobalValueTemplate aTemplate;
		HashMap<String, DerivedValue> valuesHashmap;
		String className, slotName;
		DerivedValue aValue;
		while(templatesIter.hasNext()) {
			aTemplate = templatesIter.next();
			className = aTemplate.getGlobalValueTemplateClass();
			slotName = aTemplate.getGlobalValueTemplateSlot();
			aValue = aTemplate.getGlobalValueTemplateValue();
			if(valueTemplateMap.containsKey(className)) {
				valuesHashmap = valueTemplateMap.get(className);
				valuesHashmap.put(slotName, aValue);
			} else {
				valuesHashmap = new HashMap<String, DerivedValue>();
				valuesHashmap.put(slotName, aValue);
				valueTemplateMap.put(className, valuesHashmap);			
			}
		}
	}
	
	
	
	/**
	 * @return void
	 * 
	 * Add a GlobalValueTemplate to the Hashmap of DerivedValue Templates
	 */
	public void addGlobalValueTemplate(GlobalValueTemplate aTemplate) {
		HashMap<String, DerivedValue> valuesHashmap;
		String className, slotName;
		DerivedValue aValue;
		
		className = aTemplate.getGlobalValueTemplateClass();
		slotName = aTemplate.getGlobalValueTemplateSlot();
		aValue = aTemplate.getGlobalValueTemplateValue();
		if(className != null && slotName!=null && aValue!=null) {
			//add to the list of Global Value Templates
			this.importUtilityData.getGlobalValueTemplates().add(aTemplate);
			
			//add to the Hashmap of Global Value Templates
			if(valueTemplateMap.containsKey(className)) {
				valuesHashmap = valueTemplateMap.get(className);
				valuesHashmap.put(slotName, aValue);
			} else {
				valuesHashmap = new HashMap<String, DerivedValue>();
				valuesHashmap.put(slotName, aValue);
				valueTemplateMap.put(className, valuesHashmap);			
			}
		}
	}
	
	
	
	/**
	 * @return void
	 * 
	 * Remove a GlobalValueTemplate from the list and Hashmap
	 */
	public void removeGlobalValueTemplate(GlobalValueTemplate aTemplate) {
		HashMap<String, DerivedValue> valuesHashmap;
		String className, slotName;
		DerivedValue aValue;
		
		className = aTemplate.getGlobalValueTemplateClass();
		slotName = aTemplate.getGlobalValueTemplateSlot();
		aValue = aTemplate.getGlobalValueTemplateValue();
		if(className != null && slotName!=null && aValue!=null) {
			//add to the list of Global Value Templates
			this.importUtilityData.getGlobalValueTemplates().remove(aTemplate);
			
			//remove the Derived Value from the Hashmap of Global Value Templates
			if(valueTemplateMap.containsKey(className)) {
				valuesHashmap = valueTemplateMap.get(className);
				valuesHashmap.remove(slotName);
			} 
		}
	}
	
	
	
	/**
	 * @return void
	 * 
	 * initialise the Hashmap of Import Script Templates
	 */
	private void initImportScriptTemplateHashmap(List<ImportScriptTemplate> scriptTemplates) {
		this.scriptTemplateMap = new HashMap<String, HashMap<String, String>>();
		Iterator<ImportScriptTemplate> templatesIter = scriptTemplates.iterator();
		ImportScriptTemplate aTemplate;
		String elementName, script,mode;
		HashMap<String, String> modeMap;
		while(templatesIter.hasNext()) {
			aTemplate = templatesIter.next();
			elementName = aTemplate.getImportScriptTemplateClassName();
			script = aTemplate.getImportScriptTemplateString();
			mode = aTemplate.getImportScriptTemplateMode();
			if(scriptTemplateMap.containsKey(elementName)) {
				modeMap = scriptTemplateMap.get(elementName);
				modeMap.put(mode, script);
			} else {
				modeMap = new HashMap<String, String>();
				modeMap.put(mode, script);
				scriptTemplateMap.put(elementName, modeMap);			
			}
		}
	}
	
	
	/**
	 * @return a Hashmap of Import Script Templates
	 * 
	 */
	public HashMap<String, HashMap<String, String>> getImportScriptTemplateMap() {
		List<ImportScriptTemplate> scriptTemplates = this.getImportUtilityData().getImportScriptTemplates();
		HashMap<String, HashMap<String, String>> latestTemplateMap = new HashMap<String, HashMap<String, String>>();
		Iterator<ImportScriptTemplate> templatesIter = scriptTemplates.iterator();
		ImportScriptTemplate aTemplate;
		String elementName, script,mode;
		HashMap<String, String> modeMap;
		while(templatesIter.hasNext()) {
			aTemplate = templatesIter.next();
			elementName = aTemplate.getImportScriptTemplateClassName();
			script = aTemplate.getImportScriptTemplateString();
			mode = aTemplate.getImportScriptTemplateMode();
			if(latestTemplateMap.containsKey(elementName)) {
				modeMap = latestTemplateMap.get(elementName);
				modeMap.put(mode, script);
			} else {
				modeMap = new HashMap<String, String>();
				modeMap.put(mode, script);
				latestTemplateMap.put(elementName, modeMap);			
			}
		}
		return latestTemplateMap;
	}
	
	
	
	/**
	 * @return void
	 * 
	 * Add a ImportScriptTemplate to the Hashmap of Import Script Templates
	 */
	public void addImportScriptTemplate(ImportScriptTemplate aTemplate) {
		String elementName, script,mode;
		HashMap<String, String> modeMap;
		elementName = aTemplate.getImportScriptTemplateClassName();
		script = aTemplate.getImportScriptTemplateString();
		mode = aTemplate.getImportScriptTemplateMode();
		if(elementName != null && script!= null && mode != null) {
			//add to the list of Global Value Templates
			this.systemData.getImportScriptTemplates().add(aTemplate);
			
			//add to the Hashmap of Import Script Templates
			if(this.scriptTemplateMap.containsKey(elementName)) {
				modeMap = scriptTemplateMap.get(elementName);
				modeMap.put(elementName, script);
			} else {
				modeMap = new HashMap<String, String>();
				modeMap.put(mode, script);
				scriptTemplateMap.put(elementName, modeMap);			
			}
		}
	}
	
	
	/**
	 * @return void
	 * 
	 * Remove a ImportScriptTemplate from the list and Hashmap
	 */
	public void removeImportScriptTemplate(ImportScriptTemplate aTemplate) {
		String elementName, script,mode;
		HashMap<String, String> modeMap;
		
		elementName = aTemplate.getImportScriptTemplateClassName();
		script = aTemplate.getImportScriptTemplateString();
		mode = aTemplate.getImportScriptTemplateMode();
		if(elementName != null && script!=null && mode!=null) {
			//remove the Import Script Templates
			this.systemData.getImportScriptTemplates().remove(aTemplate);
			
			//remove the Import Script Template from the Hashmap
			if(scriptTemplateMap.containsKey(elementName)) {
				modeMap = scriptTemplateMap.get(elementName);
				modeMap.remove(elementName);
			} 
		}
	}
	
	/**
	 * Get the index of the given Import Environment in the managed list of Import Environments
	 * @return List<ImportEnvironment>
	 */
	public int getImportEnvironmentIndex(ImportEnvironment anEnv) {
		int impEnvsSize = this.importUtilityData.getImportEnvironments().size();
		if(impEnvsSize > 0) {
			//String envName;
			ImportEnvironment currentEnv;
			for(int i=0;i< impEnvsSize;i++) {
				currentEnv = this.importUtilityData.getImportEnvironments().get(i);
				if(currentEnv.getImportEnvironmentName().equals(anEnv.getImportEnvironmentName())) {
					return i;
				}
			}
			return -1;
		} else {
			return -1;
		}
	}
	
	
	/**
	 * Get a list of Import Environments that are designated as LIVE
	 * @return List<ImportEnvironment>
	 */
	public List<ImportEnvironment> getLiveImportEnvironments() {
		List<ImportEnvironment> liveEnvs = new ArrayList<ImportEnvironment>();
		Iterator<ImportEnvironment> impEnvIter = this.importUtilityData.getImportEnvironments().iterator();
		ImportEnvironment impEnv;
		while(impEnvIter.hasNext()) {
			impEnv = impEnvIter.next();
			if((impEnv.getImportEnvironmentRole() != null) && (impEnv.getImportEnvironmentRole().equals("LIVE"))) {
				liveEnvs.add(impEnv);
			}
		}
		return liveEnvs;
	}
	
	
	/**
	 * Get an Import Environment whose name matches the given string
	 * @return ImportEnvironment
	 */
	public ImportEnvironment getImportEnvironment(String impEnvName) {
		Iterator<ImportEnvironment> impEnvIter = this.importUtilityData.getImportEnvironments().iterator();
		ImportEnvironment impEnv;
		while(impEnvIter.hasNext()) {
			impEnv = impEnvIter.next();
			if(impEnv.getImportEnvironmentName().equals(impEnvName)) {
				return impEnv;
			}
		}
		return null;
	}
	
	
	public EssentialImportUtility newUserConfig() {
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		EssentialImportUtility userConfig = factory.createEssentialImportUtility();
		userConfig.setImportUtilityVersion(IMPORT_UTILITY_VERSION);
		
		return userConfig;
	}
	

	
	public GlobalValueTemplate newGlobalValueTemplate() {
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		GlobalValueTemplate valueTemplate = factory.createGlobalValueTemplate();
		valueTemplate.setGlobalValueTemplateValue(this.newDerivedValue());
		return valueTemplate;
	}
	
	public SourceRepository newSourceRepository() {
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		SourceRepository rep = factory.createSourceRepository();
		rep.setId(UUID.randomUUID().toString());
		return rep;
	}
	
	public ImportEnvironment newImportEnvironment() {
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		ImportEnvironment env = factory.createImportEnvironment();
		
		//set the environment as the reference if this is the only one in the list
		if(this.importUtilityData.getImportEnvironments().size() == 0) {
			env.setUsedAsReference(true);
		} else {
			env.setUsedAsReference(false);
		}
		env.setId(UUID.randomUUID().toString());
		return env;
	}
	
	
	public ImportActivityLog newImportActivityLog() {
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		ImportActivityLog log = factory.createImportActivityLog();
		return log;
	}
	
	
	public ImportActivityLog newImportActivityLog(ImportActivity impAct, ImportEnvironment targetImpAnv) throws Exception {
		//create the new Import Activiy Log
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		ImportActivityLog log = factory.createImportActivityLog();
		
		//set the creation and last updated date of the current activity log
		XMLGregorianCalendar xgc = this.getXMLGregorianTimestamp();
		log.setLogCreationTime(xgc);
		log.setLogLastUpdatedTime(xgc);
		
		//set the type of the import activity log, based on the target environment role (e.g. Test, QA, Live)
		log.setLogActivityType(targetImpAnv.getImportEnvironmentRole());						
		
		//create the folder for the import activity log
		String logFolderPath = this.createImpActLogFolder(impAct.getImportActivityName());
		log.setLogFolderPath(logFolderPath);
		
		return log;
	}
	
	/**
	 * Alternative version that can be used to create a new log for DUPs that are created via service interface.
	 * @param impAct
	 * @param targetImpAct
	 * @return
	 * @throws Exception
	 */
	public ImportActivityLog newImportActivityLog(ImportActivity impAct, String targetImpAct) throws Exception {
		//create the new Import Activiy Log
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		ImportActivityLog log = factory.createImportActivityLog();
		
		//set the creation and last updated date of the current activity log
		XMLGregorianCalendar xgc = this.getXMLGregorianTimestamp();
		log.setLogCreationTime(xgc);
		log.setLogLastUpdatedTime(xgc);
		
		//set the type of the import activity log, based on the target environment role (e.g. Test, QA, Live)
		log.setLogActivityType(targetImpAct);						
		
		//create the folder for the import activity log
		String logFolderPath = this.createImpActLogFolder(impAct.getImportActivityName());
		log.setLogFolderPath(logFolderPath);
		
		return log;
	}
	
	
	public DerivedInstanceType newDerivedInstanceType() {
		com.enterprise_architecture.essential.importutility.data.common.ObjectFactory commonFactory= new com.enterprise_architecture.essential.importutility.data.common.ObjectFactory();
		
		DerivedInstanceType derivedInstance = commonFactory.createDerivedInstanceType();
		return derivedInstance;
	}
	
	
	public DerivedValue newDerivedValue() {
		com.enterprise_architecture.essential.importutility.data.common.ObjectFactory commonFactory= new com.enterprise_architecture.essential.importutility.data.common.ObjectFactory();
		
		DerivedValue derivedValue = commonFactory.createDerivedValue();
		return derivedValue;
	}
	
	
	public DerivedValueRef newDerivedValueRef() {
		com.enterprise_architecture.essential.importutility.data.common.ObjectFactory commonFactory= new com.enterprise_architecture.essential.importutility.data.common.ObjectFactory();
		
		DerivedValueRef derivedValueRef = commonFactory.createDerivedValueRef();
		return derivedValueRef;
	}
	
	
	public DerivedValueString newDerivedValueString() {
		com.enterprise_architecture.essential.importutility.data.common.ObjectFactory commonFactory= new com.enterprise_architecture.essential.importutility.data.common.ObjectFactory();
		
		DerivedValueString derivedValueString = commonFactory.createDerivedValueString();
		return derivedValueString;
	}
	
	
	public DerivedValue copyDerivedValue(DerivedValue derivedValue) {
		com.enterprise_architecture.essential.importutility.data.common.ObjectFactory commonFactory= new com.enterprise_architecture.essential.importutility.data.common.ObjectFactory();
		
		DerivedValue copy = commonFactory.createDerivedValue();
		Iterator valuesIter = derivedValue.getDerivedValueStringOrDerivedValueRef().iterator();
		while(valuesIter.hasNext()) {
			Object valueSegment = valuesIter.next();
			
			if(valueSegment.getClass().getName() =="com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef") {
				DerivedValueRef newValueRef= this.newDerivedValueRef();
				newValueRef.setValue(ImportUtilityDataManager.VALUE_REF_SYMBOL);
				copy.getDerivedValueStringOrDerivedValueRef().add(newValueRef);
			} else {
				DerivedValueString stringCopy = copyDerivedValueString((DerivedValueString) valueSegment);
				copy.getDerivedValueStringOrDerivedValueRef().add(stringCopy);
			}
			
		}
		
		return copy;
	}
	
	
	public DerivedValueString copyDerivedValueString(DerivedValueString derivedValueString) {
		com.enterprise_architecture.essential.importutility.data.common.ObjectFactory commonFactory= new com.enterprise_architecture.essential.importutility.data.common.ObjectFactory();
		
		DerivedValueString copy = commonFactory.createDerivedValueString();
		copy.setContent(derivedValueString.getContent());
		
		return copy;
	}
	
	
	/**
	 * Get an Import Environment whose name matches the given string
	 * @return ImportEnvironment
	 */
	public ImportActivity getImportActivity(String impActName) {
		Iterator<ImportActivity> impActIter = this.importUtilityData.getImportActivities().iterator();
		ImportActivity impAct;
		while(impActIter.hasNext()) {
			impAct = impActIter.next();
			if(impAct.getImportActivityName().equals(impActName)) {
				return impAct;
			}
		}
		return null;
	}
	
	
	public ImportActivity newImportActivity() {
		//Get the factory for this package to create a new Import Activity
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		ImportActivity impAct = factory.createImportActivity();
		impAct.setImportActivityStatus(ImportUtilityDataManager.IMPORT_ACTIVITY_PENDINGSTATUS);
		impAct.setId(UUID.randomUUID().toString());
		return impAct;
	}
	
	
	public ImportScriptTemplate newImportScriptTemplate() {
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		ImportScriptTemplate impScriptTemp = factory.createImportScriptTemplate();
		return impScriptTemp;
	}
	
	
	public ExcelImportTemplate newExcelImportActivityTemplate() {
		com.enterprise_architecture.essential.importutility.data.global.ObjectFactory commonFactory= new com.enterprise_architecture.essential.importutility.data.global.ObjectFactory();
		
		ExcelImportTemplate newTemplate = commonFactory.createExcelImportTemplate();
		return newTemplate;
	}
	
	
	
	public boolean initImportActivity(ImportActivity impAct) {
		//create the root directory for this import activity
		String rootDirName = this.getFilenameForName(impAct.getImportActivityName());
		
		//set the file name of the import specification of the Import Activity
		String newImpSpecFileName = rootDirName + "_IMPORTSPEC.xml"; 
		impAct.setSpreadsheetImportSpecFilename(newImpSpecFileName);
		
		if(rootDirName != null) {
			String rootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + rootDirName);
			File rootDir = new File(rootDirPath);
			try {
				if (rootDir.mkdir()) {
					impAct.setImportActivityRootDirName(rootDirName);
					return true;
				} else {
					return false;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	
	
	
	public boolean createImportActivityFilesFromTemplate(ImportActivity impAct, ExcelImportTemplate impTemp) {
		String rootFolderName = impAct.getImportActivityRootDirName();
		String rootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + rootFolderName);
		String rootTemplatePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + impTemp.excelImportTemplateRootDirName);
		String templateSSFilePath = rootTemplatePath + File.separator + impTemp.getExcelImportTemplateExcelFilename();
		String templateImpSpecFilePath = rootTemplatePath + File.separator + impTemp.getExcelImportTemplateImportSpecFilename();
		
		//define the new file name for the spreadsheet
		StringBuffer tempSSFileSuffixBuff = new StringBuffer(impTemp.getExcelImportTemplateExcelFilename());
		int tempSSSuffixIndex = tempSSFileSuffixBuff.lastIndexOf(".");
		String newSSFileName = rootFolderName + tempSSFileSuffixBuff.substring(tempSSSuffixIndex);
		String newSSFilePath = rootDirPath + File.separator + newSSFileName;
		impAct.setSpreadsheetFilename(newSSFileName);
		
		//define the new file name for the import specification
		StringBuffer tempImpSpecFileSuffixBuff = new StringBuffer(impTemp.getExcelImportTemplateImportSpecFilename());
		int tempImpSpecSuffixIndex = tempImpSpecFileSuffixBuff.lastIndexOf(".");
		String newImpSpecFileName = rootFolderName + "_IMPORTSPEC" + tempImpSpecFileSuffixBuff.substring(tempImpSpecSuffixIndex);
		String newImpSpecFilePath = rootDirPath + File.separator + newImpSpecFileName;
		
		//copy across the spreadsheet file
		ImportUtilityDataManager.copyFile(templateSSFilePath, newSSFilePath);
		
		//copy across the import spec file
		ImportUtilityDataManager.copyFile(templateImpSpecFilePath, newImpSpecFilePath);
		
		return true;
	}
	
	
	
	public boolean renameImportActivityRootDir(ImportActivity impAct) {
		//update the root directory for this import activity
		String newRootDirName = this.getFilenameForName(impAct.getImportActivityName());
		
		if(!newRootDirName.equals(impAct.getImportActivityRootDirName())) {
			String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName());
			String newRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + newRootDirName);
			File currentRootDir = new File(currentRootDirPath);
			File newRootDir = new File(newRootDirPath);
			try {
				if (currentRootDir.renameTo(newRootDir)) {
					impAct.setImportActivityRootDirName(newRootDirName);
					return true;
				} else {
					return false;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return true;
		}
	}
	
	
	public boolean deleteImportEnvironmentAtIndex(int impEnvIndex) {
		//delete the Import Environment at the given index
		ImportEnvironment impEnv = this.getImportUtilityData().getImportEnvironments().get(impEnvIndex);
		if(impEnv != null && impEnv.getImportEnvironmentLocalFolderName() != null) {
			String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + impEnv.getImportEnvironmentLocalFolderName());
						
			//System.out.println("DELETE ENV ROOT DIR: " + currentRootDirPath);
			iuLog.log(Level.INFO, "DELETE ENV ROOT DIR: " + currentRootDirPath);
			
			File currentRootDir = new File(currentRootDirPath);
			try {
				this.getImportUtilityData().getImportEnvironments().remove(impEnvIndex);
				this.deleteDir(currentRootDir);
				/*File[] impEnvFiles = currentRootDir.listFiles();
				if(impEnvFiles != null) {
					for(int i=0;i < impEnvFiles.length;i++) {
						impEnvFiles[i].delete();
					}
				}
				currentRootDir.delete();*/
				//if there is only one environment left in the list, set it as the reference environment
				List<ImportEnvironment> impEnvs = this.getImportUtilityData().getImportEnvironments();
				if(impEnvs.size() == 1) {
					ImportEnvironment anEnv = impEnvs.get(0);
					anEnv.setUsedAsReference(true);
				}
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	
	
	public boolean deleteImportActivityAtIndex(int impActIndex) {
		//delete the Import Activity at the given index
		ImportActivity impAct = this.getImportUtilityData().getImportActivities().get(impActIndex);
		if(impAct != null) {
			String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName());
			//System.out.println("DELETE IMP ACT ROOT DIR: " + impAct.getImportActivityRootDirName());
			File currentRootDir = new File(currentRootDirPath);
			try {
				this.getImportUtilityData().getImportActivities().remove(impActIndex);
				this.deleteDir(currentRootDir);
			/*	File[] activityFiles = currentRootDir.listFiles();
				for(int i=0;i < activityFiles.length;i++) {
					activityFiles[i].delete();
				}
				currentRootDir.delete(); */
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	
	public void updateExcelImpActivityImportSpecFile(ImportActivity impAct, Media importSpec) {
		//create the root folder for the Import Activity Template, if required
		String rootDirName = this.createExcelImpActFolder(impAct.getImportActivityName());
		String newFileName = importSpec.getName();
		//write the file to the appropriate root folder
		if (rootDirName != null) 
		{
			String rootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + rootDirName); 
			try 
			{
				//	System.out.println("SET NEW SS FILENAME: " + newFileName);
				if(impAct.getSpreadsheetImportSpecFilename() != null) {
					String oldFilePath = rootDirPath + File.separator + impAct.getSpreadsheetImportSpecFilename();
					File oldFile = new File(oldFilePath);
					if(oldFile.exists()) {
						oldFile.delete();
					}
				}
				
				// If the importSpec is Zipped, unpack it first
				if(newFileName.endsWith(".zip"))
				{
					String anUnCompressedContent = readImportSpecFromZipArchive(importSpec.getStreamData());
					String aNewXMLFilename = newFileName.substring(0, newFileName.lastIndexOf(".zip"));
					aNewXMLFilename = aNewXMLFilename.concat(".xml");
					iuLog.log(Level.INFO, "Import Spec Selected: " + anUnCompressedContent.length());
					String newFilePath = rootDirPath + File.separator + aNewXMLFilename;
							
					// Save the uncompressed import specification
					this.storeFileStringContent(newFilePath, anUnCompressedContent);
					
					// set the file name of the spreadsheet in the Import Activity
					impAct.setSpreadsheetImportSpecFilename(aNewXMLFilename);
				}
				// Otherwise, simply save the supplied media
				else
				{
					/*
					BufferedWriter writer = new BufferedWriter(new FileWriter(newFilePath));
					Files.copy(writer, importSpec.getReaderData());
					*/
					String newFilePath = rootDirPath + File.separator + newFileName;					
					java.io.ByteArrayOutputStream sw = new java.io.ByteArrayOutputStream();
	            	IOUtils.copy(importSpec.getReaderData(), sw, "utf-8");
	            	
	            	//System.out.println("Import Spec Selected: " + sw.toString().length());
	            	iuLog.log(Level.INFO, "Import Spec Selected: " + sw.toString().length());
	            	
					this.storeFileStringContent(newFilePath, sw.toString());
					
					//set the file name of the spreadsheet in the Import Activity
					impAct.setSpreadsheetImportSpecFilename(newFileName);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Update the XML import activity import spec content.
	 * The importSpec can be an XSL file or a ZIP file containing XSL plus any
	 * Python files containing supporting functions.
	 * @param impAct the import activity to which this content is associated
	 * @param importSpec the XSL or ZIP file that implements the import specification
	 */
	public void updateXMLImpActivityImportSpecContent(ImportActivity impAct, Media importSpec) 
	{
		//create the root folder for the Import Activity Template, if required
		String rootDirName = this.createExcelImpActFolder(impAct.getImportActivityName());
		String newFileName = importSpec.getName();
	
		//write the file to the appropriate root folder
		if (rootDirName != null) 
		{
			String rootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + rootDirName); 
			String newFilePath = rootDirPath + File.separator + newFileName;
			try 
			{
				if(impAct.getXmlTransformFilename() != null) 
				{
					// Remove ALL files in the Import Activity as this is XML-mode
					String oldFilePath = rootDirPath + File.separator + impAct.getXmlTransformFilename();
					File oldFile = new File(oldFilePath);
					if(oldFile.exists()) 
					{
						// Remove all files in the folder to ensure clean slate 
						// Iterate over all files in rootDirPath
						WildcardFileFilter aFilesToDelete = new WildcardFileFilter("*");
						Iterator <File>aFileIt = FileUtils.iterateFilesAndDirs(new File(rootDirPath), 
																		  	   aFilesToDelete,
																		  	   aFilesToDelete);
						while(aFileIt.hasNext())
						{
							File aDeleteFile = aFileIt.next();
							if(aDeleteFile.isDirectory())
							{
								// Don't delete the ZIP or the containing folder
								if(!aDeleteFile.getPath().equals(newFilePath) &&
								   !aDeleteFile.getPath().equals(rootDirPath))
								{									
									FileUtils.deleteDirectory(aDeleteFile);
								}
							}
							else
							{
								// Don't delete the ZIP File
								if(!aDeleteFile.getPath().equals(newFilePath))
								{
									aDeleteFile.delete();
								}
							}
						}
						
					}
				}
				
				// If the importSpec file ends in .xsl then just copy the file in
				if(newFileName.endsWith(".xsl"))
				{
					java.io.ByteArrayOutputStream sw = new java.io.ByteArrayOutputStream();
					IOUtils.copy(importSpec.getReaderData(), sw, "utf-8");
            	
					iuLog.log(Level.INFO, "Import Spec Selected: " + sw.toString().length());            	
					this.storeFileStringContent(newFilePath, sw.toString());
				}
				else
				{
					// Else, if it's a Zip, unpack it into the folder for the import activity en-masse
					ZipInputStream aZipArchive = new ZipInputStream(importSpec.getStreamData());
					boolean isSuccess = readFromArchive(aZipArchive, rootDirPath);
					if(aZipArchive != null)
					{
						try
						{
							aZipArchive.close();
							
							// Save the ZIP itself
							iuLog.log(Level.INFO, "Zip Archive for XML Import Selected: " + importSpec.getFormat());
							if (importSpec.isBinary()) 
							{
								Files.copy(new File(newFilePath), importSpec.getStreamData());
							}
							else 
							{
								BufferedWriter writer = new BufferedWriter(new FileWriter(newFilePath));
								Files.copy(writer, importSpec.getReaderData());
							}
						}
						catch(IOException aCloseEx)
						{
							iuLog.log(Level.ALL, "Error encountered when uploading XML ZIP import specification archive, closing file: ");
							iuLog.log(Level.ALL, aCloseEx.getMessage());
						}
					}
					if(isSuccess)
					{						
						iuLog.log(Level.INFO, "Unpacked uploaded XML ZIP import specification archive");
					}
					else
					{
						iuLog.log(Level.INFO, "Failed to upload XML ZIP import specification archive");
					}
				}
				//set the file name of the spreadsheet in the Import Activity
				impAct.setXmlTransformFilename(newFileName);
			}
			catch(Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void updateExcelImpActivitySpreadsheetFile(ImportActivity impAct, Media newContent) throws IOException {
		//create the root folder for the Import Activity Template, if required
		String rootDirName = this.createExcelImpActFolder(impAct.getImportActivityName());
		String newFileName = newContent.getName();
		//write the file to the appropriate root folder
		if (rootDirName != null) {
				String rootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + rootDirName); 
				String newFilePath = rootDirPath + File.separator + newFileName;
				
				//System.out.println("SET NEW SS FILENAME: " + newFileName);
				iuLog.log(Level.INFO, "SET NEW SS FILENAME: " + newFileName);
				
				if(impAct.getSpreadsheetFilename() != null) {
					String oldFilePath = rootDirPath + File.separator + impAct.getSpreadsheetFilename();
					new File(oldFilePath).delete();
				}
				if (newContent.isBinary()) {
					Files.copy(new File(newFilePath), newContent.getStreamData());
				}
				else {
					BufferedWriter writer = new BufferedWriter(new FileWriter(newFilePath));
					Files.copy(writer, newContent.getReaderData());
				}
								
				//set the file name of the spreadsheet in the Import Activity
				impAct.setSpreadsheetFilename(newFileName);
		}
	}
	
	
	
	public boolean renameImportActivityTemplateRootDir(ExcelImportTemplate template) {
		//update the root directory for this import activity
		String newRootDirName = this.getFilenameForName(template.getExcelImportTemplateName());
		
		if(!newRootDirName.equals(template.getExcelImportTemplateRootDirName())) {
			String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + template.getExcelImportTemplateRootDirName());
			String newRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + newRootDirName);
			File currentRootDir = new File(currentRootDirPath);
			File newRootDir = new File(newRootDirPath);
			try {
				if (currentRootDir.renameTo(newRootDir)) {
					template.setExcelImportTemplateRootDirName(newRootDirName);
					return true;
				} else {
					return false;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return true;
		}
	}
	
	
	public void deleteExcelImportActivityTemplateAtIndex(int templateIndex) {
		ExcelImportTemplate template = this.importUtilityData.getExcelImportTemplates().get(templateIndex);
		
		if(template != null) {
			String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + template.getExcelImportTemplateRootDirName());
			File rootDir = new File(currentRootDirPath);
			iuLog.log(Level.INFO, "DELETING FOLDER PATH: " + currentRootDirPath );
			if(rootDir.exists()) {
				try {
					/*	File[] templateFiles = rootDir.listFiles();
						for(int i=0;i < templateFiles.length;i++) {
							templateFiles[i].delete();
						} */
						this.deleteDir(rootDir);
						rootDir.delete();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			} 
			this.importUtilityData.getExcelImportTemplates().remove(templateIndex);
		}
		
	}
	
	
	
	public void storeExcelImpActTemplateSpreadsheetFile(ExcelImportTemplate template, Media content) {
		//create the root folder for the Import Activity Template, if required
		if(template.getExcelImportTemplateRootDirName() == null) {
			template.setExcelImportTemplateRootDirName(this.createExcelImpActFolder(template.getExcelImportTemplateName()));
		}
		String rootDirName = template.getExcelImportTemplateRootDirName();
		
		//write the file to the template's root folder
		if (rootDirName != null) {
			String filePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + rootDirName + File.separator + content.getName());
			
			try {
				if (content.isBinary()) {
					Files.copy(new File(filePath), content.getStreamData());
				}
				else {
					BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
					Files.copy(writer, content.getReaderData());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			template.setExcelImportTemplateExcelFilename(content.getName());
			
			/*if (this.storeFileBinaryContent(filePath, content)) {
				template.setExcelImportTemplateExcelFilename(fileName);
			} */
		}
		
	}
	
	
	public void storeExcelImpActTemplateImportSpecFile(ExcelImportTemplate template, Media importSpecMedia) {
		//create the root folder for the Import Activity Template, if required
		if(template.getExcelImportTemplateRootDirName() == null) {
			template.setExcelImportTemplateRootDirName(this.createExcelImpActFolder(template.getExcelImportTemplateName()));
		}
		String rootDirName = template.getExcelImportTemplateRootDirName();
		String fileName = importSpecMedia.getName();
		String content = importSpecMedia.getStringData();
		
		//write the file to the template's root folder
		if (rootDirName != null) {
			String filePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + rootDirName + File.separator + fileName);
			if (this.storeFileStringContent(filePath, content)) {
				template.setExcelImportTemplateImportSpecFilename(fileName);
			}
		}
		
	}
	
	
	public void updateExcelImpActTemplateSpreadsheetFile(ExcelImportTemplate template, Media newContent) {
		//create the root folder for the Import Activity Template, if required
		String rootDirName = this.createExcelImpActFolder(template.getExcelImportTemplateName());
		
		//write the file to the template's root folder
		if (rootDirName != null) {
			String newFileName = newContent.getName();
			if (template.getExcelImportTemplateExcelFilename() != newFileName) {
				//String filePath = this.contextDirPath + ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + rootDirName + File.separator + newFileName;
				
				String rootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + rootDirName); 
				String oldFilePath = rootDirPath + File.separator + template.getExcelImportTemplateExcelFilename();
				String newFilePath = rootDirPath + File.separator + newFileName;
				
				if(oldFilePath != null) {
					new File(oldFilePath).delete();
				}
				
				try {
					if (newContent.isBinary()) {
						Files.copy(new File(newFilePath), newContent.getStreamData());
					}
					else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(newFilePath));
						Files.copy(writer, newContent.getReaderData());
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				template.setExcelImportTemplateExcelFilename(newFileName);
				
				
		/*		if (this.storeFileBinaryContent(newFilePath, newContent)) {
					try {
						if (new File(oldFilePath).delete()) {
							template.setExcelImportTemplateExcelFilename(newFileName);
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}  */
			}
		}
	}
	
	
	public void updateExcelImpActTemplateImportSpecFile(ExcelImportTemplate template, String newFileName, String newContent) {
		//create the root folder for the Import Activity Template, if required
		String rootDirName = this.createExcelImpActFolder(template.getExcelImportTemplateName());
				
		//write the file to the template's root folder
		if (rootDirName != null) {
			if (template.getExcelImportTemplateExcelFilename() != newFileName) {
				String rootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + rootDirName); 
				String oldFilePath = rootDirPath + File.separator + template.getExcelImportTemplateImportSpecFilename();
				String newFilePath = rootDirPath + File.separator + newFileName;

				if (this.storeFileStringContent(newFilePath, newContent)) {
					try {
						if (new File(oldFilePath).delete()) {
							template.setExcelImportTemplateImportSpecFilename(newFileName);
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	/**
	 * Upload a new version of the Import Specification via a Zipped format
	 * @param theTemplate the import template
	 * @param theNewFilename the newly uploaded Zip file
	 * @param theNewSpec the ZIP file content, ready to be processed
	 */
	public void updateExcelImpActTemplateImportSpecFile(ExcelImportTemplate theTemplate, String theNewFilename, InputStream theNewSpec)
	{
		String aRootDirName = this.createExcelImpActFolder(theTemplate.getExcelImportTemplateName());
		
		// Test for the newFileName being a ZIP file
		if(theNewFilename.endsWith(".zip"))
		{
			// If so, unpack it first into the target location
			String aNewContent = readImportSpecFromZipArchive(theNewSpec);
			
			//write the file to the template's root folder
			if (aRootDirName != null) 
			{
				// Replace the .zip with .xml in the filename
				String aNewXMLFilename = theNewFilename.substring(0, theNewFilename.lastIndexOf(".zip"));
				aNewXMLFilename = aNewXMLFilename.concat(".xml");
				if (theTemplate.getExcelImportTemplateExcelFilename() != aNewXMLFilename) 
				{
					String rootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH + File.separator + aRootDirName); 
					String oldFilePath = rootDirPath + File.separator + theTemplate.getExcelImportTemplateImportSpecFilename();
					String newFilePath = rootDirPath + File.separator + aNewXMLFilename;

					if (this.storeFileStringContent(newFilePath, aNewContent)) 
					{
						try 
						{
							if (new File(oldFilePath).delete()) 
							{
								theTemplate.setExcelImportTemplateImportSpecFilename(aNewXMLFilename);
							}
						}
						catch(Exception e) 
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}
	
	public String createExcelImpActFolder(String activityName) {
		String templateFolderName = this.getFilenameForName(activityName);

		if(templateFolderName != null) {
			String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + templateFolderName);
			File rootDir = new File(currentRootDirPath);
			if(!rootDir.exists()) {
				try {
						rootDir.mkdir();
					//	template.setExcelImportTemplateRootDirName(templateFolderName);
						return templateFolderName;
				}
				catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				return templateFolderName;
			}
		} else {
			return null;
		}
	}
	
	
	public String createImpActLogFolder(String activityName) {
		String templateFolderName = this.getFilenameForName(activityName);

		if(templateFolderName != null) {
			String logFolderName = this.createFileNameFromTimestamp(templateFolderName);
			
			String currentLogDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + templateFolderName + File.separator + logFolderName);
			File currentLogDir = new File(currentLogDirPath);
			if(!currentLogDir.exists()) {
				try {
						currentLogDir.mkdir();
						return currentLogDirPath;
				}
				catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				return currentLogDirPath;
			}
		} else {
			return null;
		}
	}
	
	
	private synchronized boolean storeFileStringContent(String filePath, String content) {
		File ssFile = new File(filePath);
		try {
			FileWriter ssFileWriter = new FileWriter(ssFile);
			ssFileWriter.write(content);
			ssFileWriter.close();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	@SuppressWarnings("unused")
	private synchronized boolean storeFileBinaryContent(String filePath, byte[] content) {
		File ssFile = new File(filePath);
		try {
			FileWriter ssFileWriter = new FileWriter(ssFile);
			
			int c = 0;
			 while (c < content.length) {
				 ssFileWriter.write(content[c]);
				 c++;
			 }

			ssFileWriter.close();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	@SuppressWarnings("unused")
	private boolean storeFileContentAsStream(String filePath, InputStream content) {
		File ssFile = new File(filePath);
		BufferedInputStream bufferedIS = new BufferedInputStream(content);
		try {
			FileWriter ssFileWriter = new FileWriter(ssFile);
			
			while(bufferedIS.available() > 0) {
				ssFileWriter.write(bufferedIS.read());
			}
			ssFileWriter.close();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	
	public String createLocalImportEnvFolder(String importEnvName) {
		String localImpEnvFolderName = this.getFilenameForName(importEnvName);

		if(localImpEnvFolderName != null) {
			String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + localImpEnvFolderName);
			File rootDir = new File(currentRootDirPath);
			if(!rootDir.exists()) {
				try {
						rootDir.mkdir();
						return localImpEnvFolderName;
				}
				catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				return localImpEnvFolderName;
			}
		} else {
			return null;
		}
	}
	
	public ImportActivityLog initImportActivityLog(ImportActivity impAct, ImportEnvironment impEnv, User aUser) {
		//create a new import activity log and add it to the current import activity
		try {
			this.currentImpAct = impAct;
			ImportActivityLog log = this.newImportActivityLog(this.currentImpAct, impEnv);
			log.setLogUser(aUser.getEmail());
			log.setLogTargetEnvName(impEnv.getImportEnvironmentName());
			log.setLogTargetEnvType(impEnv.getImportEnvironmentRole());
			this.currentImpActLog = log;
		    this.currentImpAct.getImportActivityLogs().add(log);
		    
		    return currentImpActLog;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public ImportActivityLog initAutoImportActivityLog(ImportActivity impAct, ImportEnvironment impEnv, User aUser) {
		//create a new import activity log and add it to the current import activity
		try {
			ImportActivityLog log = this.newImportActivityLog(impAct, impEnv);
			log.setLogUser(aUser.getEmail());
			log.setLogTargetEnvName(impEnv.getImportEnvironmentName());
			log.setLogTargetEnvType(impEnv.getImportEnvironmentRole());
		    impAct.getImportActivityLogs().add(log);
		    
		    return log;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ImportActivityLog initAutoImportActivityLog(ImportActivity impAct, String impEnv, User aUser) {
		//create a new import activity log and add it to the current import activity
		try {
			ImportActivityLog log = this.newImportActivityLog(impAct, impEnv);
			log.setLogUser(aUser.getEmail());
			log.setLogTargetEnvName(impEnv);
			log.setLogTargetEnvType("");
		    impAct.getImportActivityLogs().add(log);
		    
		    return log;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Initialise the import activity log for an EIP service request user
	 * @param impAct
	 * @param theImportType
	 * @param impEnv
	 * @param anEIPUser the service request user ID. 
	 * @return the Import Activity Log
	 * @author Jonathan Carter
	 * @since 08.12.2016
	 */
	public ImportActivityLog initAutoImportActivityLog(ImportActivity impAct, String theImportType, String impEnv, String anEIPUser) 
	{
		//create a new import activity log and add it to the current import activity
		try {
			ImportActivityLog log = this.newImportActivityLog(impAct, theImportType);
			log.setLogUser(anEIPUser);
			log.setLogTargetEnvName(impEnv);
			log.setLogTargetEnvType("");
		    impAct.getImportActivityLogs().add(log);
		    
		    return log;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public synchronized boolean updateLocalImpEnvProjectZip(ImportActivity impAct, ImportEnvironment impEnv, User aUser, Media zipFile) {
		//store the current Import Activity
		this.currentImpAct = impAct;
		
		//create the root folder for the Local Import Environment, if required
		String rootDirName = impEnv.getImportEnvironmentLocalFolderName();
		String zipFileName = zipFile.getName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(impEnv.getImportEnvironmentName());
		}
		//write the zip file to the import environments local cache folder
		if (rootDirName != null) {
			String fullCachePath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
			String zipFilePath = fullCachePath + File.separator + zipFileName;
			try 
			{
				// Clear out the repository cache if there is an existing repository in there		
				//empty the cache if it already exists
				File cacheDir = new File(fullCachePath);
				if(cacheDir.exists()) {
					this.deleteDirContents(cacheDir);
				}
				
				if (zipFile.isBinary()) {
					Files.copy(new File(zipFilePath), zipFile.getStreamData());
				}
				else {
					BufferedWriter writer = new BufferedWriter(new FileWriter(zipFilePath));
					Files.copy(writer, zipFile.getReaderData());
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			
			//if the decompression is successful, create an activity log and move the original zip file to its folder
			if (this.decompressLocalImpEnvProjectZip(impEnv, zipFile.getName())) 
			{
				// 07.05.2014 JWC Remove the following 'additional' log entry
				// But if decompressed the ZIP, delete it
				File aZipProjectFile = new File(zipFilePath);
				//System.out.println("TRACE: Deleting uploaded ZIP file");				
				boolean isDeleted = aZipProjectFile.delete();
				iuLog.log(Level.FINE, "ZIP deleted = " + isDeleted);
				//System.out.println("TRACE: ZIP deleted = " + isDeleted);
				
				// We've unzipped the repository, so point the current import activity
				// at the newly uploaded project
				
				return true;
								
			} 
			else 
			{
				return false;
			}

		} else {
				return false;
		}

	}
	
	/*public boolean refreshTestImportEnvironment(ImportEnvironment aTestEnv) {
		if(aTestEnv.getImportEnvironmentRole().equals(IMPORT_ENV_PROD_ROLE)) {
			return false;
		}
		String sourceEnvName = aTestEnv.getImportEnvironmentLiveSource();
		if(sourceEnvName == null || sourceEnvName.length() == 0) {
			return false;
		}

		Iterator<ImportEnvironment> impEnvIter = this.importUtilityData.getImportEnvironments().iterator();
		ImportEnvironment sourceEnv;
		while(impEnvIter.hasNext()) {
			sourceEnv = impEnvIter.next();
			if(sourceEnv.getImportEnvironmentName().equals(sourceEnvName)) {
				return this.refreshTestImportFromLive(aTestEnv, sourceEnv);
			}
		}
		return false;
	}*/
	
	
	public boolean initArchiveLiveEnv(ImportEnvironment liveEnv) {
		
		//create the root folder for the Local Import Environment, if required
		
		/*String rootDirName = testEnv.getImportEnvironmentLocalFolderName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(testEnv.getImportEnvironmentName());
			testEnv.setImportEnvironmentLocalFolderName(rootDirName);
		}*/
		
		
		//delete any files that are already contained in the import environment repository cache folder				
		//String currentRootDirPath = this.contextDirPath + ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + rootDirName;
		String currentRootDirPath = this.currentImpActLog.getLogFolderPath();
		File rootDir = new File(currentRootDirPath);
		if(!rootDir.exists()) {
			// System.out.println("DELETING CONTENTS OF DIR: " + currentRootDirPath);
			return false;
		} 
				

		//Set the repository project filename of the archive
		if(liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_SERVER)) {
			String projectFilename = this.getFilenameForName(liveEnv.getProjectName()) + ".pprj";
			String contextProjectFilePath = currentRootDirPath + File.separator + projectFilename;
			currentImpActLog.setLogUpdatedRepositoryPath(contextProjectFilePath);
		}
		else {			
			return false;
		}
		
		return true;
	}
	
	
	public String compressServerImpEnvArchive() {
		if(this.currentImpActLog == null) {
			return null;
		}

		String fullArchivePath = currentImpActLog.getLogFolderPath();
		//write the zip file to the current import activity log folder
		if (fullArchivePath != null) {
			
			try {
				SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyy'_'HHmm");
				StringBuffer dateSB = new StringBuffer();
				dateFormatter.format(new Date(), dateSB, new FieldPosition(0));
				File rootDir = new File(fullArchivePath);
				String zipFileName;
				String[] projectFilenameList = rootDir.list();
				if(projectFilenameList.length > 0) {
					//derive the zip file name from the project filename prefix
					
					StringBuffer filenameSB = new StringBuffer(currentImpActLog.getLogUpdatedRepositoryPath());
					int suffixIndex = filenameSB.lastIndexOf(".");
					int dirPathIndex = filenameSB.lastIndexOf(File.separator);
					String zipFilePrefix = filenameSB.substring(dirPathIndex + 1, suffixIndex);
					zipFileName = zipFilePrefix + "_" + dateSB.toString() + ".zip";		
					
					//set the path of the zip file
					String zipFilePath = fullArchivePath + File.separator + zipFileName;
					FileCompressionManager compressManager = new FileCompressionManager();
					
					String[] projectPathList = new String[3];
					String fileFullPath, currentFileName;
					int projectFileCount = 0;
					for(int i=0;i < projectFilenameList.length;i++) {
						currentFileName = projectFilenameList[i];
						if(currentFileName.endsWith("pins") || currentFileName.endsWith("pont") || currentFileName.endsWith("pprj")) {
							fileFullPath = fullArchivePath + File.separator + projectFilenameList[i];
							projectPathList[projectFileCount] = fileFullPath;
							projectFileCount++;
						}
					}
					compressManager.compressFiles(zipFilePath, projectPathList, fullArchivePath);
					this.currentImpActLog.setLogUpdatedRepositoryPath(zipFilePath);
					
					//Delete the original project files
					File currentProjectFile;
					for(int i=0;i < projectPathList.length;i++) {
						currentProjectFile = new File(projectPathList[i]);
						currentProjectFile.delete();
					}
					
					return zipFilePath;
				} else {
					return null;
				}		
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
				return null;
		}

	}
	
	public void printEnvPaths(ImportEnvironment env) {
		String repositoryPath = ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + env.getImportEnvironmentLocalFolderName();
		
		System.out.println("Env Path: " + repositoryPath);
	}
	
	public boolean refreshTestEnvFromLocalLive(ImportEnvironment testEnv, ImportEnvironment liveEnv) {
		
		//create the root folder for the Local Import Environment, if required
		String rootDirName = testEnv.getImportEnvironmentLocalFolderName();
		
		String currentRootDirPath;
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(testEnv.getImportEnvironmentName());
			testEnv.setImportEnvironmentLocalFolderName(rootDirName);
			currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
		} else {
			//delete any files that are already contained in the import environment repository cache folder				
			currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
			File rootDir = new File(currentRootDirPath);
			if(rootDir.exists()) {
				// System.out.println("DELETING CONTENTS OF DIR: " + currentRootDirPath);
				this.deleteDirContents(rootDir);
			} 
		}


		//Set the repository project filename of the test project
		if(liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
			String liveProjectPath = liveEnv.getImportEnvironmentRepositoryPath();
			String liveProjectDirName = liveEnv.getImportEnvironmentLocalFolderName();
			String liveProjectDirPath = ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + liveProjectDirName + File.separator;
			if(liveProjectPath != null) {
				int projectFilenameIndex = liveProjectDirPath.length();
				String projectFilename = liveProjectPath.substring(projectFilenameIndex);
				String contextProjectFilePath = ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName + File.separator + projectFilename;
				testEnv.setImportEnvironmentRepositoryPath(contextProjectFilePath);
			} else {
				return false;
			}
		}
		
		
		//export the project associated with the live repository to the test environment's cache folder
		//if the live project is a local project, then just copy the files across
		if(liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
			String liveRootDirName = liveEnv.getImportEnvironmentLocalFolderName();
			if(liveRootDirName == null) {
				return false;
			}
			
			String liveRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + liveRootDirName);
			File liveRootDir = new File(liveRootDirPath);
			/*if(!liveRootDir.exists()) {
				try {
					liveRootDir.createNewFile();
				} 
				catch(IOException e) {
					e.printStackTrace();
				}
			}*/
			
			if (liveRootDir.isDirectory()) {
		        String[] children = liveRootDir.list();
		        for (int i=0; i<children.length; i++) {
		        	String targetFilename = children[i];
		        	String targetFilePath = currentRootDirPath + File.separator + targetFilename;
		        	String sourceFilePath = liveRootDir + File.separator + targetFilename;
		        	try {
			        	File sourceFile = new File(liveRootDir, targetFilename);
			        	File targetFile = new File(currentRootDirPath, targetFilename);
			        	//File targetFile = new File(targetFilePath);
			        	
			        	copyFile(sourceFilePath, targetFilePath);
			        	// this.copy(sourceFile, targetFile);      
		        	}
		        	catch(Exception e) {
		        		e.printStackTrace();
		        		return false;
		        	}
		        }
		    } else {
		    	return false;
		    }
			return true;
		} 
		else {
			return false;
		}
	}
	
	
	public boolean promoteTestResultToLocalLive(ImportActivityLog log, ImportEnvironment liveEnv) throws PromoteRepositoryException {
		if(log == null) {
			return false;
		}
		return this.promoteLogResultToLocalLive(log, liveEnv);
	}
	
	
	public boolean promoteLogResultToLocalLive(ImportActivityLog importLog, ImportEnvironment liveEnv) throws PromoteRepositoryException {
				
		String importLogProjectPath;
		
		//Get the full path to log's zipped up repository
		if(liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
			importLogProjectPath = importLog.getLogUpdatedRepositoryPath();
			if(importLogProjectPath == null) {
				throw(new PromoteRepositoryException("Import Log Project path does not exist"));
				//System.out.println("ERROR: Import Log Project path does not exist");
				//return false;
			}
		} else {
			throw(new PromoteRepositoryException("Target Live Repository is not local"));
		//	System.out.println("ERROR: Target Live Repository is not local");
		//	return false;
		}
		
		//get the root folder for the live Import Environment, if required
		String rootDirName = liveEnv.getImportEnvironmentLocalFolderName();
		
		
		//delete the project files that are contained in the live import environment repository cache folder				
		String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
		File rootDir = new File(currentRootDirPath);
		if(rootDir.exists()) {
			//System.out.println("DELETING CONTENTS OF DIR: " + currentRootDirPath);
			iuLog.log(Level.INFO, "DELETING CONTENTS OF DIR: " + currentRootDirPath);
			
			try {
				FileUtils.cleanDirectory(rootDir);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			
			//this.deleteDirContents(rootDir);
		} else {
			//System.out.println("LIVE DIR NOT FOUND");
			iuLog.log(Level.WARNING, "LIVE DIR NOT FOUND");
		}
		
		
		
		//Copy the log's zip file to the live environment's repository cache folder
		StringBuffer zipFilePathBuffer = new StringBuffer(importLogProjectPath);
		int zipFileNameIndex = zipFilePathBuffer.lastIndexOf(File.separator);
		String zipFileName = zipFilePathBuffer.substring(zipFileNameIndex + 1, importLogProjectPath.length());
		String zipFileCopyFullPath = currentRootDirPath + File.separator + zipFileName;
		
		//System.out.println("PROMOTING ZIP FILE FROM PATH: " + importLogProjectPath);
		//System.out.println("UNZIPPING FILES TO PATH: " + currentRootDirPath);
		iuLog.log(Level.INFO, "PROMOTING ZIP FILE FROM PATH: " + importLogProjectPath);
		iuLog.log(Level.INFO, "UNZIPPING FILES TO PATH: " + currentRootDirPath);
		
		try {
			FileCompressionManager fcp = new FileCompressionManager();
			fcp.decompressZipFileToDir(importLogProjectPath, currentRootDirPath + File.separator);
			return true;
		/*//	File zipFileCopy = new File(zipFileCopyFullPath);
		//	this.copy(new File(importLogProjectPath), zipFileCopy);
			copyFile(importLogProjectPath, zipFileCopyFullPath);
			
			
			//Unzip the new live environment zip file and update the liveEnvironment's repository path
			if(this.decompressLocalImpEnvProjectZip(liveEnv, zipFileName)) {
				//delete the copied zip file
				File zipFileCopy = new File(zipFileCopyFullPath);
				zipFileCopy.delete();
				this.saveAppData();
				return true;
			} else {
				throw(new PromoteRepositoryException("Failed to decompress promoted zip file"));
				//System.out.println("ERROR: Failed to decompress promoted zip file");
				//return false;
			}*/
		} catch (Exception e) {
			throw(new PromoteRepositoryException("Failed to copy promoted repository zip file to live"));
		}
		

	}
	
	
	public boolean initRefreshTestEnv(ImportEnvironment testEnv, ImportEnvironment liveEnv) {
		
		//create the root folder for the Local Import Environment, if required
		String rootDirName = testEnv.getImportEnvironmentLocalFolderName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(testEnv.getImportEnvironmentName());
			testEnv.setImportEnvironmentLocalFolderName(rootDirName);
		}
		
		
		//delete any files that are already contained in the import environment repository cache folder				
		String currentRootDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
		File rootDir = new File(currentRootDirPath);
		if(rootDir.exists()) {
			// System.out.println("DELETING CONTENTS OF DIR: " + currentRootDirPath);
			this.deleteDirContents(rootDir);
		} 
		
		

		//Set the repository project filename of the test project
		if(liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
			String liveProjectPath = liveEnv.getImportEnvironmentRepositoryPath();
			String liveProjectDirName = liveEnv.getImportEnvironmentLocalFolderName();
			String liveProjectDirPath = ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + liveProjectDirName + File.separator;
			if(liveProjectPath != null) {
				int projectFilenameIndex = liveProjectDirPath.length();
				String projectFilename = liveProjectPath.substring(projectFilenameIndex);
				String contextProjectFilePath = ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName + File.separator + projectFilename;
				testEnv.setImportEnvironmentRepositoryPath(contextProjectFilePath);
			} 
		}
		else {			
			String projectFilename = this.getFilenameForName(liveEnv.getProjectName()) + ".pprj";
			String contextProjectFilePath = ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName + File.separator + projectFilename;
			testEnv.setImportEnvironmentRepositoryPath(contextProjectFilePath);
		}
		
		return true;
	}
	
	
	
	/*public boolean refreshTestImportFromLive(ImportEnvironment testEnv, ImportEnvironment liveEnv) {
		
		boolean initComplete = this.initRefreshTestEnv(testEnv, liveEnv);
		
		//export the project associated with the live repository to the test environment's cache folder
		//if the live project is a local project, then just copy the files across
		if(initComplete && liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_LOCAL)) {
			String rootDirName = testEnv.getImportEnvironmentLocalFolderName();
			String currentRootDirPath = this.contextDirPath + ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + rootDirName;
			
			String liveRootDirName = liveEnv.getImportEnvironmentLocalFolderName();
			if(liveRootDirName == null) {
				return false;
			}
			
			String liveRootDirPath = this.contextDirPath + ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + liveRootDirName;
			File liveRootDir = new File(liveRootDirPath);
			
			if (liveRootDir.isDirectory()) {
		        String[] children = liveRootDir.list();
		        for (int i=0; i<children.length; i++) {
		        	String targetFilename = children[i];
		        	String targetFilePath = currentRootDirPath + File.separator + targetFilename;
		     
		        	try {
			        	File sourceFile = new File(liveRootDir, targetFilename);
			        	File targetFile = new File(targetFilePath);
			        	this.copy(sourceFile, targetFile);      
		        	}
		        	catch(IOException e) {
		        		e.printStackTrace();
		        		return false;
		        	}
		        }
		    } else {
		    	return false;
		    }
			return true;
		} 
		else if(initComplete && liveEnv.getImportEnvironmentDeploymentType().equals(ImportUtilityDataManager.IMPORT_ENV_DEPLOYMENT_SERVER)) {
			ProtegeIntegrationManager integrationMgr = new ProtegeIntegrationManager(this.contextDirPath);
			integrationMgr.copyProtegeProject(liveEnv, testEnv);
			return true;
		}
		return false;
	}*/
	
	
	
	
	
	public synchronized boolean storeLocalImpEnvProjectZip(ImportEnvironment impEnv, Media zipFile) {
		//store the current Import Activity
		
		//create the root folder for the Local Import Environment, if required
		String rootDirName = impEnv.getImportEnvironmentLocalFolderName();
		String zipFileName = zipFile.getName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(impEnv.getImportEnvironmentName());
			impEnv.setImportEnvironmentLocalFolderName(rootDirName);
		}
		//write the zip file to the import environments local cache folder
		if (rootDirName != null) {
			String fullCachePath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
			String zipFilePath = fullCachePath + File.separator + zipFileName;
					
			
			try {
				//empty the cache if it already exists
				File cacheDir = new File(fullCachePath);
				if(cacheDir.exists()) {
					this.deleteDirContents(cacheDir);
				}
				
				if (zipFile.isBinary()) 
				{
					Files.copy(new File(zipFilePath), zipFile.getStreamData());
				}
				else {
					BufferedWriter writer = new BufferedWriter(new FileWriter(zipFilePath));
					Files.copy(writer, zipFile.getReaderData());
				}
				
				//if the decompression is successful, delete the original zip file
				if(this.decompressLocalImpEnvProjectZip(impEnv, zipFile.getName())) {
					File sourceFile = new File(zipFilePath);
					
					// 12.05.2014 JWC - Check ZIP is deleted
					//System.out.println("TRACE: Deleting uploaded ZIP file");
					boolean isDeleted = sourceFile.delete();
					iuLog.log(Level.FINE, "ZIP deleted = " + isDeleted);
					//System.out.println("TRACE: ZIP deleted = " + isDeleted);
					/*if(!isDeleted)
					{
						System.out.println("TRACE: Failed delete of: " + zipFilePath);												
						System.out.println("TRACE: can write file: " + sourceFile.canWrite());
						
					}*/
					return true;
				} else {
					return false;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	
	
	public synchronized boolean decompressLocalImpEnvProjectZip(ImportEnvironment impEnv, String zipFileName) {
		//create the root folder for the Local Import Environment, if required
		String rootDirName = impEnv.getImportEnvironmentLocalFolderName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(impEnv.getImportEnvironmentName());
		}
		//write the zip file to the import environments local cache folder
		if (rootDirName != null) {
			String fullCachePath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);	
			
				//decompress the file
				FileCompressionManager compressManager = new FileCompressionManager();
				try {
					compressManager.decompressFiles(fullCachePath);
				} catch(Exception e) {
					e.printStackTrace();
					return false;
				}
				
				String zipFilePath = fullCachePath + File.separator + zipFileName;
				File cacheFolder = new File(fullCachePath);
				ProtegeProjectFilenameFilter projectFileFilter = new ProtegeProjectFilenameFilter();
				File[] projectFile = cacheFolder.listFiles(projectFileFilter);
				if(projectFile.length >0) {
					File pprjFile = projectFile[0];
					String contextProjectFilePath = ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName + File.separator + pprjFile.getName();
					iuLog.log(Level.INFO, "Setting project PPRJ PATH: " + contextProjectFilePath);
					//System.out.println("SETTING PROJECT PPRJ PATH: " + contextProjectFilePath);
					impEnv.setImportEnvironmentRepositoryPath(contextProjectFilePath);
				} else {
					return false;
				}
				
				return true;
		} else {
			return false;
		}
		
	}
	
	
	/**
	 * Gets a spreadsheet for import purposes from the given Import Activity
	 * @return Workbook, the in memory spreadsheet
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public Workbook getSpreadsheetForImportActivity(ImportActivity impAct) throws InvalidFormatException, IOException {
		String ssFilePath = this.getLocalImpActSpreadsheetPath(impAct);
		return WorkbookFactory.create(new File(ssFilePath)); 
	}
	
	
	
	/**
	 * @return String, the import script for automating the execution of an Import Activity
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public String autoGenerateImportScript(SpreadsheetImportSpecScript importSpec, ImportActivity anImpAct, ImportActivityLog log, ImportSpecScriptListener theMessageListener) throws InvalidFormatException, IOException {
		
		
			//System.out.println("AUTO-GENERATING IMPORT SCRIPT");
			iuLog.log(Level.INFO, "AUTO-GENERATING IMPORT SCRIPT");
			
			Workbook aSpreadsheet = this.getSpreadsheetForImportActivity(anImpAct);
			
			String sourceRepName = anImpAct.getImportActivitySourceRepository();
			
			//String importScript = importSpec.printImportScriptHeader(sourceRepName, currentSpreadsheet, this.scriptTemplateMap);

			
			String importScript = importSpec.printImportScript(sourceRepName, aSpreadsheet, null, this.scriptTemplateMap, 0, theMessageListener);
			/*while(!importSpec.importScriptComplete()) {
				importScript = importScript + importSpec.printNextImportScriptChunk(sourceRepName, aSpreadsheet, this.scriptTemplateMap, ImportUtilityDataManager.IMPORT_SCRIPT_CHUNK_SIZE);
			}*/
					
						
			//copy the import spec that was used to the import activity log folder
			String importSpecFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + anImpAct.getImportActivityRootDirName() + File.separator + anImpAct.spreadsheetImportSpecFilename);
			String logFolderPath = log.getLogFolderPath();
			String importSpecCopyPath = logFolderPath + File.separator + anImpAct.spreadsheetImportSpecFilename;
			ImportUtilityDataManager.copyTextFile(importSpecFilePath, importSpecCopyPath);
			
			log.setLogImportSpecPath(importSpecCopyPath);	
			
			//System.out.println("FINISHED CREATING SCRIPT");
			iuLog.log(Level.INFO, "FINISHED CREATING SCRIPT");
			
			//save the resulting import script to the log
			this.saveAutoImportScript(importScript, log, anImpAct);
			
			//return the import script
			return importScript;

	}
	
	
	
	/**
	 * @return String, the import script for the current Import Activity
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	/*public synchronized String initChunkedImportScript(SpreadsheetImportSpecScript importSpec) throws InvalidFormatException, IOException {
		importSpec.initImportScriptPrint();
		currentImportScript = "";
		
		if(this.currentImpActLog != null && this.currentImpAct != null) {
			String ssFilePath = this.getLocalImpActSpreadsheetPath(currentImpAct);
			this.currentSpreadsheet = new Spreadsheet();
			currentSpreadsheet.setSrc(ssFilePath);
			
			String sourceRepName = currentImpAct.getImportActivitySourceRepository();
			
			String importScriptHeader = importSpec.printImportScriptHeader(sourceRepName, this.currentSpreadsheet, this.scriptTemplateMap);
		//	System.out.println("HEADER SCRIPT: " + importScriptHeader);
			currentImportScript = importScriptHeader;
			
			//return the import script
			return importScriptHeader;
		} else {
			return null;
		}
	}
	*/
	
	/**
	 * @return String, the import script for the current Import Activity
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public synchronized String initChunkedImportScript(ImportActivity impAct, Workbook spreadsheet, ImportActivityLog log, SpreadsheetImportSpecScript importSpec, ImportSpecScriptListener theMessageListener) throws InvalidFormatException, IOException {
		importSpec.initImportScriptPrint();
		currentImportScript = "";
		
		if(log != null && impAct != null) {
			String sourceRepName = impAct.getImportActivitySourceRepository();
			
			String importScriptHeader = importSpec.printImportScriptHeader(sourceRepName, spreadsheet, scriptTemplateMap, theMessageListener);

			currentImportScript = importScriptHeader;
			
			//return the import script
			return importScriptHeader;
		} else {
			return null;
		}
	}
	
	
	
	/**
	 * @return String, the next chunk of import script for the current Import Activity
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	/*public synchronized String getNextImportScriptChunk(SpreadsheetImportSpecScript importSpec) throws InvalidFormatException, IOException {
		
		if(this.currentImpActLog != null && this.currentImpAct != null) {
			String sourceRepName = currentImpAct.getImportActivitySourceRepository();
			
			String ssFilePath = this.getLocalImpActSpreadsheetPath(currentImpAct);
			this.currentSpreadsheet = new Spreadsheet();
			currentSpreadsheet.setSrc(ssFilePath);
			//aSpreadsheet = WorkbookFactory.create(new File(ssFilePath)); 		
			
			String nextImportScriptChunk = importSpec.printNextImportScriptChunk(sourceRepName, this.currentSpreadsheet, scriptTemplateMap, ImportUtilityDataManager.IMPORT_SCRIPT_CHUNK_SIZE);
			// System.out.println("IMPORT SCRIPT: " + nextImportScriptChunk);
			currentImportScript = currentImportScript + nextImportScriptChunk;
			
			//return the import script
			return nextImportScriptChunk;
		} else {
			return null;
		}
	}
	*/
	
	/**
	 * @return String, the next chunk of import script for the current Import Activity
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public synchronized String getNextImportScriptChunk(ImportActivity impAct, Workbook spreadsheet, ImportActivityLog log, SpreadsheetImportSpecScript importSpec, ImportSpecScriptListener theMessageListener) throws InvalidFormatException, IOException {
		
		if(log != null && impAct != null) {
			String sourceRepName = impAct.getImportActivitySourceRepository();
	
			
			String nextImportScriptChunk = importSpec.printNextImportScriptChunk(sourceRepName, spreadsheet, scriptTemplateMap, ImportUtilityDataManager.IMPORT_SCRIPT_CHUNK_SIZE, theMessageListener);
			// System.out.println("IMPORT SCRIPT: " + nextImportScriptChunk);
			currentImportScript = currentImportScript + nextImportScriptChunk;
			
			//return the import script
			return nextImportScriptChunk;
		} else {
			return null;
		}
	}
	
	
	public synchronized void finaliseImport() {
		String logFolderPath = this.currentImpActLog.getLogFolderPath();
		
		//copy the import spec that was used to the import activity log folder
		String importSpecFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + this.currentImpAct.getImportActivityRootDirName() + File.separator + this.currentImpAct.spreadsheetImportSpecFilename);
		String importSpecCopyPath = logFolderPath + File.separator + this.currentImpAct.spreadsheetImportSpecFilename;
		ImportUtilityDataManager.copyTextFile(importSpecFilePath, importSpecCopyPath);
		this.currentImpActLog.setLogImportSpecPath(importSpecCopyPath);	
		
		//copy the source spreadsheet that was used to the import activity log folder
		String spreadsheetFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + this.currentImpAct.getImportActivityRootDirName() + File.separator + this.currentImpAct.spreadsheetFilename);
		String spreadsheetCopyPath = logFolderPath + File.separator + this.currentImpAct.spreadsheetFilename;
		ImportUtilityDataManager.copyFile(spreadsheetFilePath, spreadsheetCopyPath);
		this.currentImpActLog.setLogSourceRepositoryPath(spreadsheetCopyPath);
		
		//save the resulting import script
		this.saveImportScript(currentImportScript);
	}
	
	
	public synchronized void finaliseImport(ImportActivity impAct, ImportActivityLog log, String importScript) {
		String logFolderPath = log.getLogFolderPath();
		
		//copy the import spec that was used to the import activity log folder
		String importSpecFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName() + File.separator + impAct.spreadsheetImportSpecFilename);
		String importSpecCopyPath = logFolderPath + File.separator + impAct.spreadsheetImportSpecFilename;
		ImportUtilityDataManager.copyTextFile(importSpecFilePath, importSpecCopyPath);
		log.setLogImportSpecPath(importSpecCopyPath);	
		
		//copy the source spreadsheet that was used to the import activity log folder
		String spreadsheetFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName() + File.separator + impAct.spreadsheetFilename);
		String spreadsheetCopyPath = logFolderPath + File.separator + impAct.spreadsheetFilename;
		ImportUtilityDataManager.copyFile(spreadsheetFilePath, spreadsheetCopyPath);
		log.setLogSourceRepositoryPath(spreadsheetCopyPath);
		
		//save the resulting import script
		this.saveImportScript(impAct, log, importScript);
	}
	
	/**
	 * Finalise an import of an XML source
	 * @param impAct the Import Activity that was used
	 * @param log the log for the run of the import activity
	 * @param theImportScript the content of the import script
	 */
	public synchronized void finaliseXMLImport(ImportActivity impAct, ImportActivityLog log, String theImportScript) 
	{
		String logFolderPath = log.getLogFolderPath();
		
		//copy the import spec that was used to the import activity log folder
		String importSpecFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName() + File.separator + impAct.xmlTransformFilename);
		String importSpecCopyPath = logFolderPath + File.separator + impAct.xmlTransformFilename;
		ImportUtilityDataManager.copyFile(importSpecFilePath, importSpecCopyPath);
		log.setLogImportSpecPath(importSpecCopyPath);	
		
		//copy the source spreadsheet that was used to the import activity log folder
		String spreadsheetFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName() + File.separator + impAct.spreadsheetFilename);
		String spreadsheetCopyPath = logFolderPath + File.separator + impAct.spreadsheetFilename;
		ImportUtilityDataManager.copyFile(spreadsheetFilePath, spreadsheetCopyPath);
		log.setLogSourceRepositoryPath(spreadsheetCopyPath);
		
		//save the resulting import script
		this.saveXMLImportScript(impAct, log, theImportScript);
		
		// Extend to save a DUP as well
		DUPGenerator aDUPGenerator = new DUPGenerator();		
		String aDUPFilePath = aDUPGenerator.generateXMLDUP(impAct, theImportScript, servletContext.getRealPath(""));
		String aDUPCopyPath = logFolderPath + File.separator + FilenameUtils.getName(aDUPFilePath);
		
		// Save it to the logFolderPath and save in the log data store
		ImportUtilityDataManager.copyFile(aDUPFilePath, aDUPCopyPath);
		log.setLogXMLDUPPath(aDUPCopyPath);
		
		// Delete source DUP from filesystem, not that it's in the logs
		FileUtils.deleteQuietly(new File(aDUPFilePath));		
	}
	
	/**
	 * Finalise an import of an XML source
	 * @param impAct the Import Activity that was used
	 * @param log the log for the run of the import activity
	 * @param theImportScript the content of the import script
	 * @param theDUPFilePath the path to the DUP file that was posted to the EIP service.
	 */
	public synchronized void finaliseXMLImport(ImportActivity impAct, ImportActivityLog log, String theImportScript, String theDUPFilePath) 
	{
		String logFolderPath = log.getLogFolderPath();
		
		//copy the import spec that was used to the import activity log folder
		String importSpecFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName() + File.separator + impAct.xmlTransformFilename);
		String importSpecCopyPath = logFolderPath + File.separator + impAct.xmlTransformFilename;
		ImportUtilityDataManager.copyFile(importSpecFilePath, importSpecCopyPath);
		log.setLogImportSpecPath(importSpecCopyPath);	
		
		//copy the source spreadsheet that was used to the import activity log folder
		String spreadsheetFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impAct.getImportActivityRootDirName() + File.separator + impAct.spreadsheetFilename);
		String spreadsheetCopyPath = logFolderPath + File.separator + impAct.spreadsheetFilename;
		ImportUtilityDataManager.copyFile(spreadsheetFilePath, spreadsheetCopyPath);
		log.setLogSourceRepositoryPath(spreadsheetCopyPath);
		
		//save the resulting import script
		this.saveXMLImportScript(impAct, log, theImportScript);
		
		// Extend to save a DUP as well
		String aDUPCopyPath = logFolderPath + File.separator + FilenameUtils.getName(theDUPFilePath);
		
		// Save it to the logFolderPath and save in the log data store
		ImportUtilityDataManager.copyFile(theDUPFilePath, aDUPCopyPath);
		log.setLogXMLDUPPath(aDUPCopyPath);
		
		// Delete source DUP from filesystem, not that it's in the logs
		FileUtils.deleteQuietly(new File(theDUPFilePath));		
	}
	
	public synchronized void saveImportMessageLog(String messageLog) {

		if(this.currentImpAct != null && this.currentImpActLog != null) {
			String messageLogBaseName = this.currentImpAct.getImportActivityRootDirName() + "_MESSAGE_LOG";
			String messageLogFileName = this.createFileNameFromTimestamp(messageLogBaseName) + ".txt";
			
			String logFolderPath = this.currentImpActLog.getLogFolderPath();
			String messageLogFilePath = logFolderPath + File.separator + messageLogFileName;
			try {
				FileWriter messageLogFW = new FileWriter(messageLogFilePath);
				messageLogFW.write(messageLog);
				messageLogFW.close();
				
				this.currentImpActLog.setLogOutputLogPath(messageLogFilePath);
				
				//set the last updated date of the current activity log
				this.currentImpActLog.setLogLastUpdatedTime(this.getXMLGregorianTimestamp());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} 
	}
	
	
	public synchronized void saveImportMessageLog(ImportActivityLog log, ImportActivity impAct, String messageLog) {

		if(log != null && impAct != null) {
			String messageLogBaseName = impAct.getImportActivityRootDirName() + "_MESSAGE_LOG";
			String messageLogFileName = this.createFileNameFromTimestamp(messageLogBaseName) + ".txt";
			
			String logFolderPath = log.getLogFolderPath();
			String messageLogFilePath = logFolderPath + File.separator + messageLogFileName;
			try {
				FileWriter messageLogFW = new FileWriter(messageLogFilePath);
				messageLogFW.write(messageLog);
				messageLogFW.close();
				
				log.setLogOutputLogPath(messageLogFilePath);
				
				//set the last updated date of the current activity log
				log.setLogLastUpdatedTime(this.getXMLGregorianTimestamp());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} 
	}
	
	
	public synchronized void saveImportScript(String importScript) {

		if(this.currentImpAct != null && this.currentImpActLog != null) {
			String impScriptBaseName = this.currentImpAct.getImportActivityRootDirName() + "_IMPORT_SCRIPT";
			String impScriptFileName = this.createFileNameFromTimestamp(impScriptBaseName) + ".txt";
			
			String logFolderPath = this.currentImpActLog.getLogFolderPath();
			String impScriptFilePath = logFolderPath + File.separator + impScriptFileName;
			try {
				FileWriter impScriptFW = new FileWriter(impScriptFilePath);
				impScriptFW.write(importScript);
				impScriptFW.close();
				
				this.currentImpActLog.setLogImportScriptPath(impScriptFilePath);
				
				//set the last updated date of the current activity log
				this.currentImpActLog.setLogLastUpdatedTime(this.getXMLGregorianTimestamp());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} 
	}
	
	
	
	public synchronized void saveImportScript(ImportActivity impAct, ImportActivityLog log, String importScript) {

		if(impAct != null && log != null) {
			String impScriptBaseName = impAct.getImportActivityRootDirName() + "_IMPORT_SCRIPT";
			String impScriptFileName = this.createFileNameFromTimestamp(impScriptBaseName) + ".txt";
			
			String logFolderPath = log.getLogFolderPath();
			String impScriptFilePath = logFolderPath + File.separator + impScriptFileName;
			try {
				FileWriter impScriptFW = new FileWriter(impScriptFilePath);
				impScriptFW.write(importScript);
				impScriptFW.close();
				
				log.setLogImportScriptPath(impScriptFilePath);
				
				//set the last updated date of the current activity log
				log.setLogLastUpdatedTime(this.getXMLGregorianTimestamp());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} 
	}
	
	/**
	 * Save the XML Import Script
	 * @param impAct the import activity that was executed
	 * @param log the log for the import activity
	 * @param theImportScript the integration script that was run on the impAct Import Activity 
	 */
	public synchronized void saveXMLImportScript(ImportActivity impAct, ImportActivityLog log, String theImportScript) 
	{

		if(impAct != null && log != null) 
		{
			String impScriptBaseName = impAct.getImportActivityRootDirName() + "_IMPORT_SCRIPT";
			String impScriptFileName = this.createFileNameFromTimestamp(impScriptBaseName) + ".py";
			
			String logFolderPath = log.getLogFolderPath();
			String impScriptFilePath = logFolderPath + File.separator + impScriptFileName;
			try 
			{				
				File anImportScriptLog = new File(impScriptFilePath);
				FileUtils.writeStringToFile(anImportScriptLog, theImportScript);
				
				log.setLogImportScriptPath(impScriptFilePath);
				
				//set the last updated date of the current activity log
				log.setLogLastUpdatedTime(this.getXMLGregorianTimestamp());
			}
			catch(Exception e) 
			{
				e.printStackTrace();
			}
		} 
	}
	
	public synchronized void saveAutoImportScript(String importScript, ImportActivityLog log, ImportActivity impAct) {

			String impScriptBaseName = impAct.getImportActivityRootDirName() + "_IMPORT_SCRIPT";
			String impScriptFileName = this.createFileNameFromTimestamp(impScriptBaseName) + ".txt";
			
			String logFolderPath = log.getLogFolderPath();
			String impScriptFilePath = logFolderPath + File.separator + impScriptFileName;
			try {
				FileWriter impScriptFW = new FileWriter(impScriptFilePath);
				impScriptFW.write(importScript);
				impScriptFW.close();
				
				log.setLogImportScriptPath(impScriptFilePath);
				
				//set the last updated date of the current activity log
				log.setLogLastUpdatedTime(this.getXMLGregorianTimestamp());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	
	/**
	 * @return String, the import script to be packaged in the DUP
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public String generateDUPImportScript(SpreadsheetImportSpecScript importSpec, ImportActivity anImpAct, ImportSpecScriptListener theMessageListener) throws InvalidFormatException, IOException {
		
		
			iuLog.log(Level.INFO, "GENERATING IMPORT SCRIPT FOR DUP PACKAGE");
			
			Workbook aSpreadsheet = this.getSpreadsheetForImportActivity(anImpAct);
			
			String sourceRepName = anImpAct.getImportActivitySourceRepository();
			
			String importScript = importSpec.printDUPImportScript(sourceRepName, aSpreadsheet, null, this.scriptTemplateMap, 0, theMessageListener);
					
			iuLog.log(Level.INFO, "FINISHED CREATING DUP IMPORT SCRIPT");
			
			//return the import script
			return importScript;

	}
	
	/**
	 * Override, to include the specification of a log file.
	 * @return String, the import script to be packaged in the DUP
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public String generateDUPImportScript(SpreadsheetImportSpecScript importSpec, ImportActivity anImpAct, ImportActivityLog theLog, ImportSpecScriptListener theMessageListener) throws InvalidFormatException, IOException 
	{
		
		// Set up the log for the DUP Import Script generation.
		// copy the import spec that was used to the import activity log folder
		String importSpecFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + anImpAct.getImportActivityRootDirName() + File.separator + anImpAct.spreadsheetImportSpecFilename);
		String logFolderPath = theLog.getLogFolderPath();
		String importSpecCopyPath = logFolderPath + File.separator + anImpAct.spreadsheetImportSpecFilename;
		ImportUtilityDataManager.copyTextFile(importSpecFilePath, importSpecCopyPath);
		
		theLog.setLogImportSpecPath(importSpecCopyPath);	
		
		// With logs set up, generate the DUP script
		return generateDUPImportScript(importSpec, anImpAct, theMessageListener);

	}
	
/*	*//**
	 * 
	 * @param importSpec
	 * @param theImpAct
	 * @param theMessageListener
	 * @return the import script to be packaged as part of the DUP
	 * @throws InvalidFormatException
	 * @throws IOException
	 *//*
	public String generateXMLDUPImportScript(String importSpecFile, ImportActivity theImpAct, ImportSpecScriptListener theMessageListener) throws InvalidFormatException, IOException 
	{
		//iuLog.log(Level.INFO, "GENERATING XML IMPORT SCRIPT FOR DUP PACKAGE");
		
		//Workbook aSpreadsheet = this.getSpreadsheetForImportActivity(anImpAct);
		// Get the source XML to transform
		//String anXMLDoc = this.
		
		//String sourceRepName = theImpAct.getImportActivitySourceRepository();
		
		// Using the specified importSpecFile, generate the Python resulting from applying the XSL file
		// to the XML
		//String importScript = importSpec.printDUPImportScript(sourceRepName, aSpreadsheet, null, this.scriptTemplateMap, 0, theMessageListener);
				
		//iuLog.log(Level.INFO, "FINISHED CREATING XML DUP IMPORT SCRIPT");
		
		//return the import script
		//return anImportScript;

	}

	*//**
	 * Override, to include the specification of a log file
	 * @param theImportSpecXSL
	 * @param theImpAct
	 * @param theLog
	 * @param theMessageListener
	 * @return String, the import script to be packaged in the DUP
	 * @throws InvalidFormatException
	 * @throws IOException
	 *//*
	public String generateXMLDUPImportScript(String theImportSpecXSL, ImportActivity theImpAct, ImportActivityLog theLog, ImportSpecScriptListener theMessageListener) throws InvalidFormatException, IOException
	{
		// Set up the log for the DUP XML Import Script generation
		// Copy the import spec XSL that was used to the import activity log folder
		String anImportSpecFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + theImpAct.getImportActivityRootDirName() + File.separator + theImpAct.xmlTransformFilename);
		String aLogFolderPath = theLog.getLogFolderPath();
		String anImportSpecCopyPath = aLogFolderPath + File.separator + theImpAct.xmlTransformFilename;
		
		theLog.setLogImportSpecPath(anImportSpecCopyPath);
		
		// With the logs set up, generate the DUP script
		return generateXMLDUPImportScript(theImportSpecXSL, theImpAct, theMessageListener);
	}
*/	
	public String cacheLocalImpEnvProject(ImportEnvironment impEnv) {

		//create the root folder for the Local Import Environment, if required
		String rootDirName = impEnv.getImportEnvironmentLocalFolderName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(impEnv.getImportEnvironmentName());
		}
		
		//write the zip file to the current import activity log folder
		if (rootDirName != null) {
			String fullCachePath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
			try {
				File rootDir = new File(fullCachePath);
				String zipFileName;
				String[] projectFilenameList = rootDir.list();
								
				if(projectFilenameList.length > 0) {
					//derive the zip file name from the project filename prefix
					String pprjFileName = "";
					for(int i=0;i<projectFilenameList.length;i++) {
						pprjFileName = projectFilenameList[i];
						if(pprjFileName.endsWith(".pprj")) break;
					}
					int aSuffixIndex = pprjFileName.lastIndexOf(".");
					String zipFilePrefix = pprjFileName.substring(0, aSuffixIndex);
						
					zipFileName = zipFilePrefix + "_TEMP" + ".zip";		
					
					//set the path of the zip file
					String cacheDirPath = fullCachePath + File.separator + "TEMP";
					File aCacheDir = new File(cacheDirPath);
					if(!aCacheDir.exists())
					{
						aCacheDir.mkdir();
					}
					if (aCacheDir.exists()) {
						String zipFilePath = cacheDirPath + File.separator + zipFileName;
							
						FileCompressionManager compressManager = new FileCompressionManager();
						
						String[] projectPathList = new String[projectFilenameList.length];
						String fileFullPath;
						for(int i=0;i < projectFilenameList.length;i++) {
							fileFullPath = fullCachePath + File.separator + projectFilenameList[i];
							projectPathList[i] = fileFullPath;
						}
						compressManager.compressFiles(zipFilePath, projectPathList, fullCachePath);
						return zipFilePath;
					} else {
						System.err.println("Could not create directory for project cache");
						return null;
					}
				} else {
					return null;
				}		
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
				return null;
		}
	}
	
	public synchronized boolean uncacheLocalImpEnvProject(ImportEnvironment impEnv, String zipFilePath) {
		//create the root folder for the Local Import Environment, if required
		String rootDirName = impEnv.getImportEnvironmentLocalFolderName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(impEnv.getImportEnvironmentName());
		}
		//write the zip file to the import environments local cache folder
		if (rootDirName != null) {
			String tempCachePath =  this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName + File.separator + "TEMP");
			String fullCachePath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);	
			
			//decompress the file
			FileCompressionManager compressManager = new FileCompressionManager();
			try {
				compressManager.decompressFilesToDir(tempCachePath, fullCachePath);
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
			
		//	String zipFilePath = fullCachePath + File.separator + zipFileName;
			File cacheFolder = new File(fullCachePath);
			ProtegeProjectFilenameFilter projectFileFilter = new ProtegeProjectFilenameFilter();
			File[] projectFile = cacheFolder.listFiles(projectFileFilter);
			if(projectFile.length >0) {
				File pprjFile = projectFile[0];
				String contextProjectFilePath = ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName + File.separator + pprjFile.getName();
				//System.out.println("SETTING PROJECT PPRJ PATH: " + contextProjectFilePath);
				iuLog.log(Level.INFO, "SETTING PROJECT PPRJ PATH: " + contextProjectFilePath);
				impEnv.setImportEnvironmentRepositoryPath(contextProjectFilePath);
			} else {
				return false;
			}
			
			//delete the original zip file and the TEMP folder
			File zipFile = new File(zipFilePath);
			if(zipFile != null && zipFile.exists())
			{
				zipFile.delete();
			}
			File tempDir = new File(tempCachePath);
			if(tempDir != null && tempDir.exists())
			{
				tempDir.delete();
			}
			return true;
		} else {
			return false;
		}
	}
	
	
	
	
	
	
	
	
	public String compressLocalImpEnvProject(ImportEnvironment impEnv) {
		if(this.currentImpActLog == null) {
			return null;
		}
		//create the root folder for the Local Import Environment, if required
		String rootDirName = impEnv.getImportEnvironmentLocalFolderName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(impEnv.getImportEnvironmentName());
		}
		//write the zip file to the current import activity log folder
		if (rootDirName != null) {
			String fullCachePath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
			try {
				SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyy'_'HHmm");
				StringBuffer dateSB = new StringBuffer();
				dateFormatter.format(new Date(), dateSB, new FieldPosition(0));
				File rootDir = new File(fullCachePath);
				String zipFileName;
				String[] projectFilenameList = rootDir.list();
				if(projectFilenameList.length > 0) {
					//derive the zip file name from the project filename prefix
					String projectFilename = projectFilenameList[0];
					if(projectFilename.indexOf(".") < 0) {
						projectFilename = projectFilenameList[1];
					}
					StringBuffer filenameSB = new StringBuffer(projectFilename);
					int suffixIndex = filenameSB.lastIndexOf(".");
					String zipFilePrefix = filenameSB.substring(0, suffixIndex);
					zipFileName = zipFilePrefix + "_" + dateSB.toString() + ".zip";		
					
					//set the path of the zip file
					String zipFilePath = this.currentImpActLog.getLogFolderPath() + File.separator + zipFileName;
					FileCompressionManager compressManager = new FileCompressionManager();
					
					String[] projectPathList = new String[projectFilenameList.length];
					String fileFullPath;
					for(int i=0;i < projectFilenameList.length;i++) {
						fileFullPath = fullCachePath + File.separator + projectFilenameList[i];
						projectPathList[i] = fileFullPath;
					}
					compressManager.compressFiles(zipFilePath, projectPathList, fullCachePath);
					this.currentImpActLog.setLogUpdatedRepositoryPath(zipFilePath);
					this.currentImpActLog.setLogActivityStatus(IMPORT_ACTIVITY_LOG_EXECUTEDEDSTATUS);
					this.currentImpActLog.setLogLastUpdatedTime(this.getXMLGregorianTimestamp());
					return zipFilePath;
				} else {
					return null;
				}		
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
				return null;
		}

	}
	
	
	
	public String compressLocalImpEnvProject(ImportActivityLog log, ImportEnvironment impEnv) {

		//create the root folder for the Local Import Environment, if required
		String rootDirName = impEnv.getImportEnvironmentLocalFolderName();
		if(rootDirName == null) {
			rootDirName = this.createLocalImportEnvFolder(impEnv.getImportEnvironmentName());
		}
		//write the zip file to the current import activity log folder
		if (rootDirName != null) {
			String fullCachePath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);
			try {
				SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyy'_'HHmm");
				StringBuffer dateSB = new StringBuffer();
				dateFormatter.format(new Date(), dateSB, new FieldPosition(0));
				File rootDir = new File(fullCachePath);
				String zipFileName;
				String[] projectFilenameList = rootDir.list();
				if(projectFilenameList.length > 0) {
					
					String pprjFileName = "";
					for(int i=0;i<projectFilenameList.length;i++) {
						pprjFileName = projectFilenameList[i];
						if(pprjFileName.endsWith(".pprj")) break;
					}
					int aSuffixIndex = pprjFileName.lastIndexOf(".");
					String zipFilePrefix = pprjFileName.substring(0, aSuffixIndex);
					
					
					//derive the zip file name from the project filename prefix
					zipFileName = zipFilePrefix + "_" + dateSB.toString() + ".zip";		
					
					//set the path of the zip file
					String zipFilePath = log.getLogFolderPath() + File.separator + zipFileName;
					FileCompressionManager compressManager = new FileCompressionManager();
					
					String[] projectPathList = new String[projectFilenameList.length];
					String fileFullPath;
					for(int i=0;i < projectFilenameList.length;i++) {
						fileFullPath = fullCachePath + File.separator + projectFilenameList[i];
						projectPathList[i] = fileFullPath;
						//System.out.println("ZIPPING UP FILE: " + fileFullPath);
						iuLog.log(Level.FINE, "ZIPPING UP FILE: " + fileFullPath);
					}
					compressManager.compressFiles(zipFilePath, projectPathList, fullCachePath);
					log.setLogUpdatedRepositoryPath(zipFilePath);
					log.setLogActivityStatus(IMPORT_ACTIVITY_LOG_EXECUTEDEDSTATUS);
					log.setLogLastUpdatedTime(this.getXMLGregorianTimestamp());
					return zipFilePath;
				} else {
					return null;
				}		
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
				return null;
		}

	}
	
	
	//return the full path to the zip file containing the updated repository of the current import activity session
	public String getUpdatedProjectZipPath() {
		if(this.currentImpActLog != null) {
			return this.currentImpActLog.getLogUpdatedRepositoryPath();
		} else {
			return null;
		}
	}
	
	
	//return the full path to the import script generated as part of the current import activity session
	public String getCurrentImportScriptPath() {
		if(this.currentImpActLog != null) {
			return this.currentImpActLog.getLogImportScriptPath();
		} else {
			return null;
		}
	}
	
	
	//return the full path to the message log generated as part of the current import activity session
	public String getCurrentMessageLogPath() {
		if(this.currentImpActLog != null) {
			return this.currentImpActLog.getLogOutputLogPath();
		} else {
			return null;
		}
	}
	
	
	//close the current Import Activity test/run
	public void closeCurrentImportActivity() {
		if(this.currentImpAct != null) {
			if(this.currentImpActLog != null) {
				if(!currentImpAct.isLogImports()) {
					//System.out.println("DELETING THE IMPORT LOG");
					this.deleteImportActivityLog(currentImpAct, currentImpActLog);
				}
				this.currentImpActLog = null;
			}
			this.currentImpAct = null;
		}
	}
	
	
	
	
	//delete the given import activity log 
	public synchronized void deleteImportActivityLog(ImportActivity anImpAct, ImportActivityLog aLog) {
		if((anImpAct != null) && (aLog != null)) {
			anImpAct.getImportActivityLogs().remove(aLog);
			File logDir = new File(aLog.getLogFolderPath());
			if(logDir.exists()) {
				this.deleteDir(logDir);
				//logDir.delete();
				this.saveAppData();
			}
		}
	}
	
	
	
	
	//save the user config data of the Import Utility Application
	public void saveAppData() {
		try {
			JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.global") ;
			Marshaller   marshaller = context.createMarshaller();
			marshaller.marshal(this.importUtilityData, new FileOutputStream(this.configFilePath)) ; 
		//	System.out.println("Saving Data to Config Path: " + this.configFilePath);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//save the system config data of the Import Utility Application
	public void saveSystemData() {
		try {
			JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.global") ;
			Marshaller   marshaller = context.createMarshaller(); 
			marshaller.marshal(this.systemData, new FileOutputStream(this.systemConfigFilePath)) ; 
			
			this.initImportScriptTemplateHashmap(this.systemData.getImportScriptTemplates());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//Utility operation to move a binary file to an alternative location
	@SuppressWarnings("unused")
	private static void moveBinaryFile(String sourceFilePath, String targetFilePath) {
		try {
			FileInputStream fileIS = new FileInputStream(sourceFilePath);
			File targetFile = new File(targetFilePath);
			Files.copy(targetFile, fileIS);
			
			File sourceFile = new File(sourceFilePath);
			fileIS.close();
			sourceFile.delete();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	//Utility operation to move a binary file to an alternative location
	@SuppressWarnings("unused")
	private static void moveTextFile(String sourceFilePath, String targetFilePath) {
		try {
			FileReader fileReader = new FileReader(sourceFilePath);
			FileWriter targetFileWriter = new FileWriter(targetFilePath);
			Files.copy(targetFileWriter, fileReader);
			
			targetFileWriter.close();
			fileReader.close();
			File sourceFile = new File(sourceFilePath);
			sourceFile.delete();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//Utility operation to create an appropriate file name from a name string
	private String getFilenameForName(String aName) {
		String filenameString = aName.replaceAll(" ", "_");
		filenameString = filenameString.toLowerCase();
        return filenameString;
	}
	
	/**
	 * Utility to delete a file as specified by a relative / local
	 * path within a specified import environment. The filename is expanded to a full 
	 * path from the servlet context before
	 * attempting to remove the file.
	 * @param theEnvironment the Import Environment from which to delete the file
	 * @param theFilename the relative path to the file to delete
	 */
	public synchronized void deleteFileFromEnv(ImportEnvironment theEnvironment, String theFilename)
	{
		//create the root folder for the Local Import Environment, if required
		String rootDirName = theEnvironment.getImportEnvironmentLocalFolderName();
		if(rootDirName != null)
		{
			String aFullFolderPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH + File.separator + rootDirName);	
			String aFileToDelete = aFullFolderPath + File.separator + theFilename;
			try
			{
				File aDeleteFile = new File(aFileToDelete);
				if(aDeleteFile.exists())
				{
					boolean isDeleted = aDeleteFile.delete();
					if(!isDeleted)
					{
						System.out.println("ERROR: IMPORT UTILITY DATA MANAGER. Failed to delete file: " + aFileToDelete);
					}
				}
			}
			catch(Exception ex)
			{
				System.out.println("ERROR: IMPORT UTILITY DATA MANAGER. Exception during delete of file: " + aFileToDelete);
				ex.printStackTrace();
			}
		}
		else
		{
			// No path found for the specified environment, so no action taken
			return;
		}
				
	}
	
	//Utility operation to copy a file
	public static void copyFile(String fromFileName, String toFileName) {
		File inputFile = new File(fromFileName);
	    File outputFile = new File(toFileName);
	    	    
	    try 
	    {
	    	// 12.05.2014 JWC / NW re-implement
	    	// Copy the input file to the output file, preserving date attributes
	    	FileUtils.copyFile(inputFile, outputFile, true);
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	
	
	//Utility operation to copy a text file
	public static void copyTextFile(String sourceFilePath, String targetFilePath) {
		try 
		{
			// 12.05.2014 JWC / NW re-implement
			File aSourceFile = new File(sourceFilePath);
			File aTargetFile = new File(targetFilePath);
			
			// Copy the input file to the output file, preserving date attributes	    	
		    FileUtils.copyFile(aSourceFile, aTargetFile, true);		    
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	//Utility operation to create a folder or file name based on the current time
	private String createFileNameFromTimestamp(String baseFileName) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyy'_'HHmmss");
		StringBuffer dateSB = new StringBuffer();
		dateFormatter.format(new Date(), dateSB, new FieldPosition(0));
		String newFileName = baseFileName + "_" + dateSB.toString();
		return newFileName;
	}
	
	

	//Utility operation to create an XML coompatible Grogeorian Calendar timestamp
	private XMLGregorianCalendar getXMLGregorianTimestamp() throws Exception {
		GregorianCalendar gc = new GregorianCalendar();
		DatatypeFactory dtf = DatatypeFactory.newInstance();
		XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar(gc);
		return xgc;
	}
	
	
	
	private void initAppFolders() {
		File appDir;
		String impActsDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH);
		appDir = new File(impActsDirPath);
		try {
			if (!appDir.exists()) {
				appDir.mkdir();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		String impActTempsDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_TEMPLATES_ROOT_PATH);
		appDir = new File(impActTempsDirPath);
		try {
			if (!appDir.exists()) {
				appDir.mkdir();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		String repCacheDirPath = this.servletContext.getRealPath(ImportUtilityDataManager.REPOSITORY_CACHE_ROOT_PATH);
		appDir = new File(repCacheDirPath);
		try {
			if (!appDir.exists()) {
				appDir.mkdir();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getLocalImpActSpreadsheetPath(ImportActivity impAct) {
		String ssFileName = impAct.getSpreadsheetFilename();
		String impActRootFolderName = impAct.getImportActivityRootDirName();
		if (ssFileName != null && impActRootFolderName != null) {
			String localSSFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impActRootFolderName + File.separator + ssFileName); 
			return localSSFilePath;
		} else {
			return null;
		}
		
	}
	
	
	public String getFullImpActSpreadsheetPath(ImportActivity impAct) {
		String ssFileName = impAct.getSpreadsheetFilename();
		String impActRootFolderName = impAct.getImportActivityRootDirName();
		if (ssFileName != null && impActRootFolderName != null) {
			String fullSSFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.IMPORT_ACTIVITIES_ROOT_PATH + File.separator + impActRootFolderName + File.separator + ssFileName); 
			return fullSSFilePath;
		} else {
			return null;
		}
		
	}
	
	
	public DerivedValue getValueTemplateForSlot(String className, String slotName) {
		if(this.valueTemplateMap.containsKey(className)) {
			HashMap<String, DerivedValue> valueHashmap = valueTemplateMap.get(className);
			if(valueHashmap.containsKey(slotName)) {
				return valueHashmap.get(slotName);
			} else {
				return null;
			}
		} else {
			return null;
		}
		
	}
	
	
	public void updateReferenceImportEnvironments(int importEnvIndex) {
		List<ImportEnvironment> impEnvList = this.importUtilityData.getImportEnvironments();
		int impEnvListSize = impEnvList.size();
		if(impEnvListSize > 0) {
			ImportEnvironment notRefImpEnv ;
			for(int i=0;i<impEnvListSize;i++) {
				if(i != importEnvIndex) {
					notRefImpEnv = impEnvList.get(i);
					notRefImpEnv.setUsedAsReference(false);		
				}
			}
		}
	}
	
	/** 
	 * EIP Variant
	 * that reads the Environments from a service request
	 * @return
	 */
	public void updateReferenceEipImportEnvironments(int theImportEnvIndex)
	{
		List<ImportEnvironment> anImportEnvList = this.importUtilityData.getImportEnvironments();	
		int anImportEnvListSize = anImportEnvList.size();
		if(anImportEnvListSize > 0)
		{
			ImportEnvironment aNotReferenceEnv;
			for(int i=0; i< anImportEnvListSize; i++)
			{
				if(i != theImportEnvIndex)
				{
					aNotReferenceEnv = anImportEnvList.get(i);
					aNotReferenceEnv.setUsedAsReference(false);
				}
			}
		}
	}
	
	
	public ImportEnvironment getReferenceImportEnvironment() {
		Iterator<ImportEnvironment> impEnvIter = this.importUtilityData.getImportEnvironments().iterator();
		ImportEnvironment impEnv;
		while(impEnvIter.hasNext()) {
			impEnv = impEnvIter.next();
			if(impEnv.isUsedAsReference() != null && impEnv.isUsedAsReference()) {
				return impEnv;
			}
		}
		return null;
	}
	
	
	public List<ImportEnvironment> getTestImportEnvironments() {
		List<ImportEnvironment> testEnvs = new ArrayList<ImportEnvironment>();
		Iterator<ImportEnvironment> impEnvIter = this.importUtilityData.getImportEnvironments().iterator();
		ImportEnvironment impEnv;
		while(impEnvIter.hasNext()) {
			impEnv = impEnvIter.next();
			if((impEnv.getImportEnvironmentRole() != null) && (!impEnv.getImportEnvironmentRole().equals("LIVE"))) {
				testEnvs.add(impEnv);
			}
		}
		return testEnvs;
	}
	
	
	
	public boolean deleteDirContents(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDirContents(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	        // And now delete the sub-directory itself
	        // 09.03.2017 JWC
	        dir.delete();
	    } else {
	    	dir.delete();
	    }
	    // The directory is now empty so return true
	    return true;
	}
	
	
	public boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDirContents(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }	        
	        dir.delete();
	    } else {	    	
	    	dir.delete();
	    }
	    // The directory is now empty so return true
	    return true;
	}
	
	
	
	@SuppressWarnings("unused")
	private void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    
	    Files.copy(dst, in);

	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	  
	}

	/**
	 * Save a package containing the configuration details ready to be migrated / exported
	 * to another Import Utility
	 * @return the full path to the configuration package archive.
	 */
	public String saveConfigPackage()
	{
		String aConfigPackagePath = "";
		String aRootPath = this.servletContext.getRealPath("");
		ConfigurationPackage aPackage = new ConfigurationPackage(aRootPath);
		aConfigPackagePath = aPackage.createConfigurationPackage(ConfigurationPackage.NO_TIMESTAMP);
		
		String aFullPathToBackup = FilenameUtils.concat(aRootPath, aConfigPackagePath);
		
		return aFullPathToBackup;
	}

	/**
	 * Deploy the configuration contained in the specified package and reset the Import Utility to use the
	 * new configuration
	 * @param theNewConfigPackage the new configuration of users, repositories and import activities to use
	 * @return true on success or false otherwise
	 */
	public boolean deployConfigPackage(InputStream theNewConfigPackage)
	{
		boolean isSuccess = false;
		ConfigurationPackage aPackage = new ConfigurationPackage(this.servletContext.getRealPath(""));
		isSuccess = aPackage.deployConfigurationPackage(theNewConfigPackage);
		
		// Re-initialise everything with the new config.
		reInitialiseConfiguration();
		
		return isSuccess;
	}
	
	protected void reInitialiseConfiguration()
	{
		//initialise the folders that are used to hold files related to the application
		this.initAppFolders();
		
		//load the user configuration data for the application
		this.configFilePath = this.servletContext.getRealPath(ImportUtilityDataManager.CONFIG_FOLDER + File.separator + ImportUtilityDataManager.CONFIG_FILE_NAME);
		if(new File(configFilePath).exists()) {
			try {
				JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.global") ;
				Unmarshaller   unmarshaller = context.createUnmarshaller() ;
				importUtilityData = (EssentialImportUtility)unmarshaller.unmarshal(new FileInputStream(configFilePath));

				this.initValueTemplateHashmap(this.importUtilityData.getGlobalValueTemplates());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			this.importUtilityData = this.newUserConfig();
			this.saveAppData();   
		}	
	}
	
	/**
	 * Read the specified configuration archive and write / deploy the contents to the configuration
	 * @param theSourceArchive the source archive containing a configuration component
	 * @param theRootTargetDirectory the name of the directory in which this archive will be unpacked
	 */
	protected boolean readFromArchive(ZipInputStream theSourceArchive, String theRootTargetDirectory)
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
	  			  	String aTargetFile = theRootTargetDirectory + File.separator + entryFileName;
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
	
	/**
	 * Decompress a Zipped Import Specification XML document into a String
	 * @param theZippedContent the compressed import specification content
	 * @return the decompressed import specification XML document
	 */
	protected String readImportSpecFromZipArchive(InputStream theZippedContent)
	{
		String anImportSpec = null;
		if(theZippedContent != null)
		{
			try
			{
				ZipInputStream aZippedImportSpec = new ZipInputStream(theZippedContent);				
				ZipEntry anEntry = null;
				while((anEntry = aZippedImportSpec.getNextEntry()) != null)
				{
					// Ignore explicit directory entries as openOutputStream will create any required
					if(!anEntry.isDirectory())
					{
		  			  	String entryFileName = anEntry.getName();
		  			  	
		  			  	// Filter out any rogue hidden files in the archive
		  			  	if(entryFileName.endsWith("xml") && !entryFileName.startsWith("__") && !entryFileName.startsWith("."))
		  			  	{
		  			  		ByteArrayOutputStream anXMLContent = new ByteArrayOutputStream();
		  			  		IOUtils.copy(aZippedImportSpec, anXMLContent);
		  			  		anImportSpec = anXMLContent.toString();
		  			  	}
					}
				}
			}
			catch(IOException anIOEx)
			{
				iuLog.log(Level.ALL, "Error encountered when deploying configuration archive ");
				iuLog.log(Level.ALL, anIOEx.getMessage());
			}
		}
		return anImportSpec;
	}
	
	/**
	 * Compress the specified Import Spec file from XML into a ZIP
	 * @param theImportSpecPath the full path to the import spec XML file
	 * @return the full path to the generated ZIP file containing the XML file
	 */
	public String getZippedImportSpec(String theImportSpecPath)
	{
		String aZippedImportSpecPath = theImportSpecPath.substring(0, theImportSpecPath.lastIndexOf(".xml"));
		aZippedImportSpecPath = aZippedImportSpecPath.concat(".zip");
		String aZippedImportSpec = FilenameUtils.getBaseName(theImportSpecPath) + ".xml";
		FileOutputStream aCompressedImportSpec = null;
		ZipOutputStream aZipArchive = null;
		try
		{
			aCompressedImportSpec = FileUtils.openOutputStream(new File(aZippedImportSpecPath), false);
			aZipArchive = new ZipOutputStream(aCompressedImportSpec);
			
			// Add the XML import spec file into the ZIP
			aZipArchive.putNextEntry(new ZipEntry(aZippedImportSpec));
			FileInputStream anXMLFile = FileUtils.openInputStream(new File(theImportSpecPath));
			IOUtils.copy(anXMLFile, aZipArchive);
			aZipArchive.closeEntry();
			aZipArchive.close();						
		}
		catch(IOException anIOEx)
		{
			iuLog.log(Level.ALL, "Error encountered when creating Import Spec download file" + theImportSpecPath);
			iuLog.log(Level.ALL, anIOEx.getMessage());
		}
		finally
		{
			IOUtils.closeQuietly(aZipArchive);
		}
		
		return aZippedImportSpecPath;
		
	}
}
