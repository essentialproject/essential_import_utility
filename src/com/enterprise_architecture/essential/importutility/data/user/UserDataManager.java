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
 * 02.09.2011	JP	1st coding.
 * 
 */
package com.enterprise_architecture.essential.importutility.data.user;

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

import org.zkoss.zk.ui.Desktop;

//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
//import com.enterprise_architecture.essential.importutility.data.global.SourceRepository;
import com.enterprise_architecture.essential.importutility.utils.EssentialServletContext;


/**
 * This class is a data manager for managing user and role data
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 03.09.2011
 *
 */
public class UserDataManager {

	private static final String USER_FILE_PATH = "/config/essential_import_users.xml";
	private static final String KEY_USER_MANAGER = UserDataManager.class.getName()+"_MANAGER";
	private static final String KEY_USER_DATA = UserDataManager.class.getName()+"_DATA";
	private static final String KEY_USER_MAP = UserDataManager.class.getName()+"_MAP";
	
	private EssentialImportUserData itsUserData;
	private HashMap<String, User> itsUserMap;
	private String itsUserFilePath;
	
	public static final int DUPLICATE_EMAIL = -1;
	public static final int USER_NOT_FOUND = -2;
	public static final int USER_VALID = 1;
	
	private UserDataManager(String contextPath){
		super();
		itsUserFilePath = contextPath + USER_FILE_PATH;
		
		try {
			JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.user") ;
			Unmarshaller   unmarshaller = context.createUnmarshaller() ;
			itsUserData = (EssentialImportUserData) unmarshaller.unmarshal(new FileInputStream(itsUserFilePath));
			this.initUserHashMap();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
    }
	
	
	private void initUserHashMap() {
		HashMap<String, User> userMap = new HashMap<String, User>();
		Iterator<User> userIter = itsUserData.getUser().iterator();
		while(userIter.hasNext()) {
			User aUser = userIter.next();
			userMap.put(aUser.getEmail(), aUser);
		}
		itsUserMap = userMap;
	}
	
	
	public static UserDataManager getInstance(Desktop desktop, String contextPath){
		//ServletContext servletContext = (ServletContext) desktop.getWebApp().getNativeContext();
		EssentialServletContext aContext = new EssentialServletContext((ServletContext) desktop.getWebApp().getNativeContext());
		
		return UserDataManager.getUserManager(aContext, contextPath);
    }
	
	
	public static UserDataManager getInstance(ServletContext servletContext){		
		String contextPath = servletContext.getRealPath("/");
		return UserDataManager.getUserManager(servletContext, contextPath);
    }
	
	
	private static synchronized UserDataManager getUserManager(ServletContext servletContext, String contextPath) {
		UserDataManager aUserManager = (UserDataManager) servletContext.getAttribute(KEY_USER_MANAGER);		
		
		if(aUserManager == null) {
			aUserManager = new UserDataManager(contextPath);
			servletContext.setAttribute(UserDataManager.KEY_USER_MANAGER, aUserManager);
		}
		
		return aUserManager;
	}
	
	
	public User getUser(String email, String password) {
		User aUser = itsUserMap.get(email);
		if(aUser != null) {
			if(aUser.getPassword().equals(password)) {
				return aUser;
			}
		}
		return null;
	}
	
	
	public User getUser(String email) {
		User aUser = itsUserMap.get(email);
		if(aUser != null) {
			return aUser;
		}
		return null;
	}
	
	
	public int addUser(User aUser) {
		int userValidity = this.userIsValid(aUser);
		if(userValidity == UserDataManager.USER_VALID) {
			itsUserData.getUser().add(aUser);
			itsUserMap.put(aUser.getEmail(), aUser);
		}
		return userValidity;
	}	
	
/*	public int updateUser(User aUser) {
		User currentUser = this.itsUserMap.get(aUser.getEmail());
		if(currentUser == null) {
			return UserDataManager.USER_NOT_FOUND;
		}
		currentUser.setFirstname(aUser.getFirstname());
		currentUser.setSurname(aUser.getSurname());
		currentUser.setPassword(aUser.getPassword());
		currentUser.setRole(aUser.getRole());
		this.saveUserData();
		return UserDataManager.USER_VALID;
	}	 */
	
	
	public int deleteUser(User aUser) {
		User currentUser = this.itsUserMap.get(aUser.getEmail());
		if(currentUser == null) {
			return UserDataManager.USER_NOT_FOUND;
		}
		this.itsUserMap.remove(aUser.getEmail());
		if(this.itsUserData.getUser().remove(currentUser)) {	
			return UserDataManager.USER_VALID;
		} else {
			return UserDataManager.USER_NOT_FOUND;
		}
	}	
	
	
	private int userIsValid(User aUser) {
		if(itsUserMap.containsKey(aUser.getEmail())) {
			return UserDataManager.DUPLICATE_EMAIL;
		}
		return UserDataManager.USER_VALID;
	}
	
	
	public synchronized void saveUserData() {
		try {
			JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.user") ;
			Marshaller   marshaller = context.createMarshaller();
		//	marshaller.marshal(this.itsUserData, new FileOutputStream("/Users/jasonpowell/Documents/eclipse_2011_workspace/essential_import_users.xml")) ; 
			marshaller.marshal(this.itsUserData, new FileOutputStream(this.itsUserFilePath)) ; 
			System.out.println("Saving User Data to File Path: " + this.itsUserFilePath);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void initUsersAndRoles() {
		Role aRole = this.newRole();
		aRole.setRoleName("Administrator");
		aRole.setRoleDescription("An administrator of the Essential Import Utility");
		this.itsUserData.getRole().add(aRole);
		
		aRole = this.newRole();
		aRole.setRoleName("Content Author");
		aRole.setRoleDescription("An author of EA content to be imported using the Essential Import Utility");
		this.itsUserData.getRole().add(aRole);
		
		User aUser = this.newUser();
		aUser.setFirstname("The");
		aUser.setSurname("Administrator");
		aUser.setEmail("admin@admin.com");
		aUser.setRole("Administrator");
		aUser.setPassword("admin");
		this.itsUserData.getUser().add(aUser);
		
		this.saveUserData();
		
	}
	
	
	public Role newRole() {
		com.enterprise_architecture.essential.importutility.data.user.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.user.ObjectFactory();
		Role aRole = factory.createRole();
		return aRole;
	}
	
	public User newUser() {
		com.enterprise_architecture.essential.importutility.data.user.ObjectFactory   factory= new com.enterprise_architecture.essential.importutility.data.user.ObjectFactory();
		User aUser = factory.createUser();
		return aUser;
	}
	
	
	public List<User> getUsers() {
		if(this.itsUserData != null) {
			return itsUserData.getUser();
		} else {
			return new ArrayList<User>();
		}
	}
	
	
	public List<Role> getRoles() {
		if(this.itsUserData != null) {
			return itsUserData.getRole();
		} else {
			return new ArrayList<Role>();
		}
	}

	/**
	 * Re-initialise the user configuration, e.g. after a previous configuration is migrated into this one
	 */
	public void reInitialiseFromConfig()
	{
		try {
			JAXBContext  context = JAXBContext.newInstance("com.enterprise_architecture.essential.importutility.data.user") ;
			Unmarshaller   unmarshaller = context.createUnmarshaller() ;
			itsUserData = (EssentialImportUserData) unmarshaller.unmarshal(new FileInputStream(itsUserFilePath));
			this.initUserHashMap();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
