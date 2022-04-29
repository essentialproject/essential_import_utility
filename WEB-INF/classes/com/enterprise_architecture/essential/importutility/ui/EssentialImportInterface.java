 /**
  * Copyright (c)2009-2021 Enterprise Architecture Solutions Ltd.
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
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.IOException;
//import java.rmi.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Messagebox;

import com.enterprise_architecture.essential.importutility.data.common.DerivedValue;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef;
import com.enterprise_architecture.essential.importutility.data.common.DerivedValueString;
import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecScriptListener;
import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprise_architecture.essential.importutility.data.user.UserDataManager;
import com.enterprise_architecture.essential.importutility.integration.ProjectLoadException;
import com.enterprise_architecture.essential.importutility.integration.ProtegeIntegrationManager;
import com.enterprise_architecture.essential.importutility.utils.EssentialServletContext;
import com.enterprise_architecture.essential.importutility.utils.Log;
import com.enterprise_architecture.essential.report.security.ViewerSecurityManager;
import com.enterprisearchitecture.oauth.OauthTokenConsumerUtils;
import com.enterprisearchitecture.oauth.exception.OauthRefreshTokenInvalidException;

public abstract class EssentialImportInterface extends GenericForwardComposer implements ImportSpecScriptListener {

	public static final String AUTHN_SERVER_PROPERTIES_FILE = "/WEB-INF/security/.authn-server/authn-server.properties";
	public static final String AUTHN_SERVER_BASE_URL_PROPERTY = "loginService.base.url";
	public static final String AUTHN_SERVER_OAUTH_TOKEN_URL_PROPERTY = "loginService.oauth.token.url";
	public static final String AUTHN_SERVER_API_KEY_PROPERTY = "loginService.apiKey";
	public static final String API_SERVER_BASE_URL_PROPERTY = "eip.api.platform.base.url";
	public static final String EIP_API_KEY_PROPERTY = "eip.apiKey";
	public static final String DEFAULT_TENANT_NAME_PARAM = "eip.api.platform.tenant.default";
   
	protected static String SET_DATE_LABEL_FORMAT = "";
	protected static String EMPTY_DATE_LABEL_FORMAT = "";
	protected static String IU_MODE_PARAM_NAME = "iu_mode";
	protected static String EIP_IU_MODE = "eip";
	protected static String LOCAL_IU_MODE = "local";

	protected String itsLoginServerBaseUrl;
	protected String itsLoginServerOauthTokenUrl;
	protected String itsLoginServerApiKey;
	
	/**
	 * Get a reference to the logger.
	 */
	private static Logger iuLog = Log.getSystemLogger();
	
	/**
	 * Use SL4J Logger, too!
	 */	
	private static final org.slf4j.Logger itsLog = org.slf4j.LoggerFactory.getLogger(EssentialImportInterface.class);
	
	/**
	 * A string managing all the messages that have been generated during operation, ready to show
	 * on the GUI
	 */
	protected String messages = "";
	
	
	/**
	 * Maintain a handle to the listener that will be passed to the import spec script builders
	 * because we can't use 'this' in the EventHandlers...
	 */
	protected ImportSpecScriptListener itsMessageListener = this;
	
	/**
	 * This is called before the doAfterCompose() of each Composer that inherits from this class
	 * 
	 * This gives us a convenient place to refresh the bearer token if it has expired 
	 * and if the bearer token cannot be refreshed it will log the user out.
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		if (desktop != null && this.isEIPMode()) {
			// load AuthN server properties for the Composer before we do anything
			ServletContext aContext = (ServletContext) desktop.getWebApp().getServletContext();
			loadAuthnServerProperties(aContext);
			
			HttpServletRequest aRequest = (HttpServletRequest) desktop.getExecution().getNativeRequest();
			HttpServletResponse aResponse = (HttpServletResponse) desktop.getExecution().getNativeResponse();
			
			//System.out.println(">>>>> doAfterCompose() super, url: "+aRequest.getRequestURI());
			
			/**
			 * Convert the bearer token to a long-life bearer token and add it to a session attribute 
			 * and continue processing the filter chain.
			 * We add it to the session attribute to allow processes further down the request chain to have access to
			 * a long-life token they can call other services with.
			 * 
			 * Else if we were not able to refresh the bearer token, invalidate the session and log the user out.
			 * 
			 * note - exclude the redirectToAuthnServer page as we have already logged off by then
			 */
			if (!aRequest.getRequestURI().endsWith("/redirectToAuthnServer.zul")) {
				try {
					String aFreshBearerToken = OauthTokenConsumerUtils.getFreshBearerToken(aRequest, aResponse, itsLoginServerBaseUrl+itsLoginServerOauthTokenUrl, itsLoginServerApiKey);
					
					if (ViewerSecurityManager.doesBearerTokenBelongToCurrentUser(aRequest, aFreshBearerToken)) {
						String aLongLifeBearerToken = OauthTokenConsumerUtils.produceLongLifeBearerToken(aFreshBearerToken);
						aRequest.getSession().setAttribute(ViewerSecurityManager.SESSION_ATTR_BEARER_TOKEN, aLongLifeBearerToken);
					} else {
						/**
						 * We have a valid bearer token but its for a different user than the one in session.
						 * Invalidate the session for the current user and create a new session for the new user,
						 * but don't remove the OAuth tokens, they are still valid.
						 */
						iuLog.log(Level.INFO, "OAuth token presented is not for the user currently logged in. Invalidating user session and recreating for new user.");
						UserCredentialManager uCM = UserCredentialManager.getInstance(desktop, this.getContextPath());
						uCM.logoutCurrentUser(desktop);
						Executions.sendRedirect("home_eip.zul");
					}
				} catch (OauthRefreshTokenInvalidException e) {
					/**
					 * Normal case - bearer and refresh tokens have expired, log the user out
					 */
					iuLog.log(Level.INFO, "Logging user out. Reason: "+e.getMessage());
					UserCredentialManager uCM = UserCredentialManager.getInstance(desktop, this.getContextPath());
					uCM.logoutCurrentUser(desktop);
					OauthTokenConsumerUtils.removeAllOauthTokenCookies(aResponse);
					Executions.sendRedirect("redirectToAuthnServer.zul");
				} catch (Exception e) {
					/**
					 * There is some problem refreshing the bearer token so we should log the full trace and log the user out
					 */
					iuLog.log(Level.INFO, "Error refreshing the bearer token, logging user out. Reason: "+e.getMessage(), e);
					UserCredentialManager uCM = UserCredentialManager.getInstance(desktop, this.getContextPath());
					uCM.logoutCurrentUser(desktop);
					OauthTokenConsumerUtils.removeAllOauthTokenCookies(aResponse);
					Executions.sendRedirect("redirectToAuthnServer.zul");
				}
			}
		}
		
	}
	
	public void initImportUtilityAppData() {
		this.getImportUtilityDataManager();		
		itsLog.debug("Initialising - getting class map");
		if(desktop.getWebApp().getAttribute("essentialClassMap") == null) {
			itsLog.debug("No class map - calling initEssentialClassMap");
			this.initEssentialClassMap();
		}
	}
	
	/**
	 * Receive a message from an Import Specification Script building class
	 * and add it to the message that will be displayed on the GUI
	 */
	public void receiveImportSpecScriptMessage(String theMessage)
	{
		messages = messages + "\n" + theMessage;
	}
	
	protected ImportUtilityDataManager getImportUtilityDataManager(ServletContext servletContext) {
		ImportUtilityDataManager appDataManager = (ImportUtilityDataManager) servletContext.getAttribute("appDataManager");
		if(appDataManager == null) {
			appDataManager = new ImportUtilityDataManager(servletContext);
			servletContext.setAttribute("appDataManager", appDataManager);
		} 
		
		return appDataManager;
	}
	
	
	protected ImportUtilityDataManager getImportUtilityDataManager() {
		//ServletContext servletContext = (ServletContext) desktop.getWebApp().getNativeContext();
		EssentialServletContext servletContext = new EssentialServletContext(desktop.getWebApp().getServletContext());
		
		ImportUtilityDataManager appDataManager = (ImportUtilityDataManager) servletContext.getAttribute("appDataManager");
		if(appDataManager == null) {
			appDataManager = new ImportUtilityDataManager(servletContext);
			servletContext.setAttribute("appDataManager", appDataManager);
		} 
		
		return appDataManager;
	}
	
	
	protected String getContextPath() 
	{
		EssentialServletContext servletContext = new EssentialServletContext(desktop.getWebApp().getServletContext());
		return servletContext.getRealPath("");		
	}
	
	
	protected boolean isEIPMode() 
	{
		return this.getInitParameter(EssentialImportInterface.IU_MODE_PARAM_NAME).equals(EssentialImportInterface.EIP_IU_MODE);		
	}
	
	protected String getInitParameter(String paramName) 
	{
		EssentialServletContext servletContext = new EssentialServletContext(desktop.getWebApp().getServletContext());
		return servletContext.getInitParameter(paramName);		
	}
	
	
	protected ImportSpecDataManager getImportSpecDataManager(ImportActivity impAct) {
		//System.out.println("CURRENT IMPORT ACTIVITY: " + impAct.getImportActivityName());
		iuLog.log(Level.INFO, "CURRENT IMPORT ACTIVITY: " + impAct.getImportActivityName());
		ImportSpecDataManager impSpecDataManager = (ImportSpecDataManager) desktop.getSession().getAttribute("impSpecDataManager");
		if(impSpecDataManager == null) {
			//String configPath = desktop.getWebApp().getRealPath("");
			//impSpecDataManager = new ImportSpecDataManager(configPath, impAct);
			impSpecDataManager = new ImportSpecDataManager(desktop.getWebApp(), impAct);
			desktop.getSession().setAttribute("impSpecDataManager", impSpecDataManager);
		}
		else {
			impSpecDataManager.openImportSpecForActivity(impAct);
		} 
		return impSpecDataManager;
	}
	
	
	protected ImportSpecDataManager getImportSpecDataManager() {
		
		ImportSpecDataManager impSpecDataManager = (ImportSpecDataManager) desktop.getSession().getAttribute("impSpecDataManager");
		if(impSpecDataManager == null) {
			//String configPath = desktop.getWebApp().getRealPath("");
			//impSpecDataManager = new ImportSpecDataManager(configPath);
			impSpecDataManager = new ImportSpecDataManager(desktop.getWebApp());
			desktop.getSession().setAttribute("impSpecDataManager", impSpecDataManager);
		} 
		
		return impSpecDataManager;
	}
	
	
	
	protected EssentialImportUtility getImportUtilityData() {
		return this.getImportUtilityDataManager().getImportUtilityData();
	}
	
	protected EssentialImportUtility getSystemData() {
		return this.getImportUtilityDataManager().getSystemData();
	}
	
	
	protected UserDataManager getUserDataManager(ServletContext servletContext) {
		return UserDataManager.getInstance(servletContext);
	}
	
	
	protected UserDataManager getUserDataManager() {
		return UserDataManager.getInstance(desktop, getContextPath());
	}
	
	protected void saveData() {
		ImportUtilityDataManager appDataManager = this.getImportUtilityDataManager();
		appDataManager.saveAppData();
		appDataManager.saveSystemData();
	}
	
	
	protected XMLGregorianCalendar getNowAsGregorian() {
		GregorianCalendar gc = new GregorianCalendar();
		try {
			DatatypeFactory dtf = DatatypeFactory.newInstance();
			XMLGregorianCalendar xgc = dtf.newXMLGregorianCalendar(gc);
			return xgc;
		}
		catch(Exception e) {
			iuLog.log(Level.WARNING, "Exception getting time now as Gregorian format", e);
			//e.printStackTrace();
			return null;
		}
	}
	
	
	protected void popupDebugWindow(String message) {
		try {
			Messagebox.show(message, "DEBUG",Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
					new EventListener() {
						public void onEvent(Event evt) { 
								switch (((Integer)evt.getData()).intValue()) {
									case Messagebox.YES: break;
									case Messagebox.NO: break;
								}
						}
					}
			);
		}
		catch(Exception e) {
			iuLog.log(Level.WARNING, "Exception popping up debug window", e);
			//e.printStackTrace();
		}
	}
	
	//Utility operation to create a flat String from a list of Objects
	protected String getStringFromObjectList(List<Object> objectList) {
		String listString = "";
        Iterator objectIter = objectList.iterator();
        while(objectIter.hasNext()) {
        	String segment = objectIter.next().toString();
        	listString = listString + segment;
        }
        return listString;
	}
	
	
	//Utility operation to create a flat String from a DerivedValue instance
	protected String derivedValuetoString(DerivedValue derivedVal) {
		String listString = "";
        Iterator segmentIter = derivedVal.getDerivedValueStringOrDerivedValueRef().iterator();
        DerivedValueRef aRef;
        DerivedValueString aString;
        while(segmentIter.hasNext()) {
        	Object segment = segmentIter.next();
        	if(segment.getClass().getName() == "com.enterprise_architecture.essential.importutility.data.common.DerivedValueRef") {
        		aRef = (DerivedValueRef) segment;
        		listString = listString + aRef.getValue();
        	}
        	if(segment.getClass().getName() == "com.enterprise_architecture.essential.importutility.data.common.DerivedValueString") {
        		aString = (DerivedValueString) segment;
        		listString = listString + aString.getContent();
        	}
        }
        return listString;
	}
	
	
	/**
	 * @return the list of Essential class names
	 */
	public List<String> getEssentialClasses() {
		HashMap<String, List<String>> essentialClassMap = this.getEssentialClassMap();
		if(essentialClassMap == null) {
			return new ArrayList<String>();
		}
		Set<String> classNameSet = essentialClassMap.keySet();
		List<String> classNameList = new ArrayList<String>(classNameSet);
		Collections.sort(classNameList);
		return classNameList;
	}
	
	
	/**
	 * @return the list of Essential class names
	 */
	public List<String> getSlotsForEssentialClass(String className) {
		HashMap<String, List<String>> essentialClassMap = this.getEssentialClassMap();
		if(essentialClassMap == null) {
			return new ArrayList<String>();
		}
		return essentialClassMap.get(className);
	}
	
	
	private HashMap<String, List<String>> getEssentialClassMap() {
		HashMap<String, List<String>> essentialClassMap = (HashMap<String, List<String>>) desktop.getWebApp().getAttribute("essentialClassMap");
		if(essentialClassMap == null) {
			essentialClassMap = this.initEssentialClassMap();
		}
		return essentialClassMap;
	}
	
	protected ProtegeIntegrationManager getProtegeManager() {
		return new ProtegeIntegrationManager(desktop.getWebApp(), desktop);
	}
	
	
	protected ProtegeIntegrationManager getProtegeManager(ServletContext servletContext) {
		return new ProtegeIntegrationManager(servletContext);
	}
	
	
	
	/**
	 * initialise the map of Essential class names and slots
	 */
	private HashMap<String, List<String>> initEssentialClassMap() 
	{		
		itsLog.debug("In initialiseClassMap()");
		ImportEnvironment refEnv = this.getImportUtilityDataManager().getReferenceImportEnvironment();				
		if(refEnv != null) 
		{
			if(isEIPMode())
			{
				itsLog.debug("Creating an ImportEnvironment instance...");
				ImportEnvironment anEIPEnv = new ImportEnvironment();
				itsLog.debug("Created ImportEnvironment...");
				anEIPEnv.setImportEnvironmentId(refEnv.getImportEnvironmentId());
				anEIPEnv.setImportEnvironmentName(refEnv.getImportEnvironmentName());
				anEIPEnv.setImportEnvironmentDescription(refEnv.getImportEnvironmentDescription());
				itsLog.debug("Copied the refEnv details into the new object. Name: {}. Desc: {}", anEIPEnv.getImportEnvironmentName(), anEIPEnv.getImportEnvironmentDescription());
				// Want to preserve the original refEnv at this point, not reset it
				//refEnv = anEIPEnv;
				itsLog.debug("Set the refEnv to the EipImportEnvironment object");
			}
			
			try {
				ProtegeIntegrationManager piManager = this.getProtegeManager();
				HashMap<String, List<String>> essentialClassMap =  piManager.getEssentialClassMap(refEnv);
				desktop.getWebApp().setAttribute("essentialClassMap", essentialClassMap);
				return essentialClassMap;
			} catch (ProjectLoadException e) {				
        		displayError("Error loading Protege Project. Check target environment settings", "Project Load Error");
        		iuLog.log(Level.WARNING, "Error loading Protege Project", e);
        		return null;
        	}
		}  else {
			return null;
		}
	}
	
	
	/**
	 * initialise the map of Essential class names and slots
	 */
	public boolean refreshEssentialClassMap() {
		desktop.getWebApp().removeAttribute("essentialClassMap");
		if(this.initEssentialClassMap() != null) {
			return true;
		} else {
			return false;
		}
		
	}
	
	
	protected void displayError(String errorMessage, String errorTitle) {
		try {
			Messagebox.show(errorMessage, errorTitle, Messagebox.OK, Messagebox.ERROR);
		}
		catch(Exception e) {
			iuLog.log(Level.WARNING, "Exception trying to display error", e);
			//e.printStackTrace();
		}
	}
	
	
	protected String getLabel(String labelKey) {
		return Labels.getLabel(labelKey);
	}
	
	
	protected String formatXMLCalendar(XMLGregorianCalendar xmlDate) {
		SimpleDateFormat formatter = new SimpleDateFormat ("EEE, d MMM yyyy HH:mm zzz");
		Date testedDate = xmlDate.toGregorianCalendar().getTime();
		String formattedDate = formatter.format(testedDate);
		return formattedDate;
	}
	
	public static Properties getAllApplicationPropertiesFromFile(String thePropertiesFile, ServletContext theServletContext) {
		Properties aPropertyList = new Properties();
		try {
			aPropertyList.load(theServletContext.getResourceAsStream(thePropertiesFile));
		} catch(IOException anIOEx) {
			iuLog.severe("Could not load application properties file: "+thePropertiesFile+", reason: "+anIOEx.getMessage());
		}
		return aPropertyList;
	}

	private void loadAuthnServerProperties(ServletContext theServletContext) {
		Properties anAuthnServerProperties = getAllApplicationPropertiesFromFile(AUTHN_SERVER_PROPERTIES_FILE, theServletContext);
		itsLoginServerBaseUrl = anAuthnServerProperties.getProperty(AUTHN_SERVER_BASE_URL_PROPERTY);
		if (itsLoginServerBaseUrl == null || itsLoginServerBaseUrl.trim().isEmpty()) {
			iuLog.severe("No login server base url defined. Make sure to set the "+AUTHN_SERVER_BASE_URL_PROPERTY+" property in the property file "+AUTHN_SERVER_PROPERTIES_FILE);
		}
		itsLoginServerOauthTokenUrl = anAuthnServerProperties.getProperty(AUTHN_SERVER_OAUTH_TOKEN_URL_PROPERTY);
		if (itsLoginServerOauthTokenUrl == null || itsLoginServerOauthTokenUrl.trim().isEmpty()) {
			iuLog.severe("No login server OAuth Token url defined. Make sure to set the "+AUTHN_SERVER_OAUTH_TOKEN_URL_PROPERTY+" property in the property file "+AUTHN_SERVER_PROPERTIES_FILE);
		}
		itsLoginServerApiKey = anAuthnServerProperties.getProperty(AUTHN_SERVER_API_KEY_PROPERTY);
		if (itsLoginServerApiKey == null || itsLoginServerApiKey.trim().isEmpty()) {
			iuLog.severe("No login server API key defined. Make sure to set the "+AUTHN_SERVER_API_KEY_PROPERTY+" property in the property file "+AUTHN_SERVER_PROPERTIES_FILE);
		}
	}
	
	
}
