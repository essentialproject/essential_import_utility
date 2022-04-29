/**
 * Copyright (c)2009-2015 Enterprise Architecture Solutions Ltd.
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
 * 02.09.2011	JP	1st coding.
 * 20.07.2015	JWC Added support for Storm Path directory
 * 
 */
package com.enterprise_architecture.essential.importutility.data.user;

//import javax.servlet.http.HttpSession;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import com.enterprise_architecture.essential.report.security.ViewerSecurityManager;


/**
 * This class is a manager for controlling user login credentials
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @author Jonathan Carter <jonathan.carter@e-asolutions.com>
 * @version 1.0 - 03.09.2011,
 * @version 1.2 - 20.07.2015
 *
 */
public class UserCredentialManager {
	 
    private static final String KEY_USER_MODEL = UserCredentialManager.class.getName()+"_MODEL";
    
    public static final int LOGIN_SUCCESS = 1;
    public static final int LOGIN_FAILED = -1;
    public static final String ADMIN_ROLE = "Administrator";
    
    private UserDataManager userDM;
    private User user;
    
     
    private UserCredentialManager(Desktop desktop, String contextPath){
    	userDM = UserDataManager.getInstance(desktop, contextPath);
    	
    }
    
     
    public static UserCredentialManager getInstance(Desktop desktop, String contextPath){    	
        return getInstance(Sessions.getCurrent(), desktop, contextPath);
    }
    
    
    /**
     *
     * @return
     */
    public static UserCredentialManager getInstance(Session zkSession, Desktop desktop, String contextPath){
     //   HttpSession httpSession = (HttpSession) zkSession.getNativeSession();
         System.out.println("GETTING USER MANAGER FROM SESSION: " + zkSession);
     //   Session session = Sessions.getCurrent();
        synchronized(zkSession){
            UserCredentialManager userModel = (UserCredentialManager) zkSession.getAttribute(KEY_USER_MODEL);
            if(userModel==null){
                zkSession.setAttribute(UserCredentialManager.KEY_USER_MODEL, userModel = new UserCredentialManager(desktop, contextPath));
            }
            return userModel;
        }
    }
    
    
    public boolean isAuthenticated(Desktop theDesktop) 
    {
    	boolean isAuthN = false;
    	
    	// Check for local session
    	if(user != null)
    	{
    		isAuthN = true;
    	}
    	else
    	{
    		// Look for single-sign-on Cookie
    		ServletContext aContext = (ServletContext)theDesktop.getWebApp().getServletContext();
        	HttpServletRequest request = (HttpServletRequest)theDesktop.getExecution().getNativeRequest();
        	HttpServletResponse response = (HttpServletResponse)theDesktop.getExecution().getNativeResponse();
        	ViewerSecurityManager aSecurityMgr = new ViewerSecurityManager(aContext);
        	
        	String anAccount = aSecurityMgr.authenticateUserBySession(request, response);
        	if(anAccount != null)
        	{
        		user = new User();
    			
    			// User authorized. Build local User object accordingly
    			user.setEmail(getAccountEmail(anAccount));
    			user.setFirstname(getAccountFirstname(anAccount));
    			user.setRole(ADMIN_ROLE);
    			user.setSurname(getAccountLastname(anAccount));

        		isAuthN = true;
        	}
    	}
    	return isAuthN;
    }
    
    public int login(String email, String password) {
    	if(this.userDM != null) {
    		this.user = this.userDM.getUser(email, password);
    		if(this.user != null) {
        		return UserCredentialManager.LOGIN_SUCCESS;
    		}
    	}
    	return UserCredentialManager.LOGIN_FAILED;
    }
    
    
    public boolean userIsAdministrator() {
    	if(this.user != null) {
    		return user.getRole().equals(UserCredentialManager.ADMIN_ROLE);
    	} else {
    		return false;
    	}
    }
    
    public User getCurrentUser() {
    	return this.user;
    }
    
    public void logoutCurrentUser(Desktop theDesktop) {
    	if(this.user != null) {
    		this.user = null;
    	}
    	
    	/**
    	 * Remove any cross-application session
    	 * note - we're invalidating the ZK session which wraps the JSession.
    	 * If we invalidate the JSession and don't invalidate the ZK session we get some 
    	 * horrible null pointers when we try and do any page redirects after logout.
    	 */
		theDesktop.getSession().invalidate();
    }

    /**
	 * Find the ID of the Account from the XML representation of the account details
	 * @param theAccountXML the user data in XML format
	 * @return the ID of the Account
	 */
	protected String getAccountID(String theAccountXML)
	{
		String anAccountID = "";
		org.enterprise_architecture.essential.vieweruserdata.User aUserInfo = new org.enterprise_architecture.essential.vieweruserdata.User();
		
		try
		{
			JAXBContext aContext = JAXBContext.newInstance(ViewerSecurityManager.XML_USER_DATA_PACKAGE);
			Unmarshaller anUnmarshaller = aContext.createUnmarshaller();
			
			// Read the configuration from from the XML in the input stream
			aUserInfo = (org.enterprise_architecture.essential.vieweruserdata.User)anUnmarshaller.unmarshal(IOUtils.toInputStream(theAccountXML, "UTF-8"));
			anAccountID = aUserInfo.getUri();				
		}
		catch (Exception e)
		{
			System.err.println("ViewerSecurityManager Error processing user data XML");
			System.err.println("Message: " + e.getLocalizedMessage());
			e.printStackTrace();
		}		
		
		return anAccountID;
	}
	
	/**
	 * Find the first name of the Account from the XML representation of the account details
	 * @param theAccountXML the user data in XML format
	 * @return the first name of the Account
	 */
	protected String getAccountFirstname(String theAccountXML)
	{
		String anAccountFirstName = "";
		org.enterprise_architecture.essential.vieweruserdata.User aUserInfo = new org.enterprise_architecture.essential.vieweruserdata.User();
		
		try
		{
			JAXBContext aContext = JAXBContext.newInstance(ViewerSecurityManager.XML_USER_DATA_PACKAGE);
			Unmarshaller anUnmarshaller = aContext.createUnmarshaller();
			
			// Read the configuration from from the XML in the input stream
			aUserInfo = (org.enterprise_architecture.essential.vieweruserdata.User)anUnmarshaller.unmarshal(IOUtils.toInputStream(theAccountXML, "UTF-8"));
			anAccountFirstName = aUserInfo.getFirstname();				
		}
		catch (Exception e)
		{
			System.err.println("ViewerSecurityManager Error processing user data XML");
			System.err.println("Message: " + e.getLocalizedMessage());
			e.printStackTrace();
		}		
		
		return anAccountFirstName;
	}
	
	/**
	 * Find the last name of the Account from the XML representation of the account details
	 * @param theAccountXML the user data in XML format
	 * @return the last name of the Account
	 */
	protected String getAccountLastname(String theAccountXML)
	{
		String anAccountLastName = "";
		org.enterprise_architecture.essential.vieweruserdata.User aUserInfo = new org.enterprise_architecture.essential.vieweruserdata.User();
		
		try
		{
			JAXBContext aContext = JAXBContext.newInstance(ViewerSecurityManager.XML_USER_DATA_PACKAGE);
			Unmarshaller anUnmarshaller = aContext.createUnmarshaller();
			
			// Read the configuration from from the XML in the input stream
			aUserInfo = (org.enterprise_architecture.essential.vieweruserdata.User)anUnmarshaller.unmarshal(IOUtils.toInputStream(theAccountXML, "UTF-8"));
			anAccountLastName = aUserInfo.getLastname();				
		}
		catch (Exception e)
		{
			System.err.println("ViewerSecurityManager Error processing user data XML");
			System.err.println("Message: " + e.getLocalizedMessage());
			e.printStackTrace();
		}		
		
		return anAccountLastName;
	}
	
	/**
	 * Find the email address of the Account from the XML representation of the account details
	 * @param theAccountXML the user data in XML format
	 * @return the email address of the Account
	 */
	protected String getAccountEmail(String theAccountXML)
	{
		String anAccountEmail = "";
		org.enterprise_architecture.essential.vieweruserdata.User aUserInfo = new org.enterprise_architecture.essential.vieweruserdata.User();
		
		try
		{
			JAXBContext aContext = JAXBContext.newInstance(ViewerSecurityManager.XML_USER_DATA_PACKAGE);
			Unmarshaller anUnmarshaller = aContext.createUnmarshaller();
			
			// Read the configuration from from the XML in the input stream
			aUserInfo = (org.enterprise_architecture.essential.vieweruserdata.User)anUnmarshaller.unmarshal(IOUtils.toInputStream(theAccountXML, "UTF-8"));
			anAccountEmail = aUserInfo.getEmail();				
		}
		catch (Exception e)
		{
			System.err.println("ViewerSecurityManager Error processing user data XML");
			System.err.println("Message: " + e.getLocalizedMessage());
			e.printStackTrace();
		}		
		
		return anAccountEmail;
	}
}
