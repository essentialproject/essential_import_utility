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
 * 23.06.2018	JWC Add new argument to ListItemRenderer.render()
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
//import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
//import com.enterprise_architecture.essential.importutility.data.global.SourceRepository;
import com.enterprise_architecture.essential.importutility.data.user.Role;
import com.enterprise_architecture.essential.importutility.data.user.User;
import com.enterprise_architecture.essential.importutility.data.user.UserDataManager;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * source repositories for Essential imports
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class UserComposer extends EssentialImportInterface {
	
	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private int currentMode;
	private int currentUserIndex;
	private User currentUser;
	private UserDataManager userDataManager;
	
	public Button okBtn;
	public Button cancelBtn;
	public Textbox firstNameTxtBox;
	public Textbox surnameTxtBox;
	public Textbox emailTxtBox;
	public Textbox passwordTxtBox;
	public Textbox confirmPasswordTxtBox;
	
	public Listbox userRoleListBox;

	public Window userWindow;
	
	private Listbox userList;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		this.userDataManager = this.getUserDataManager();
		
		//set up the list of user roles
		List roleList = this.userDataManager.getRoles();
		ListModelList roleModel = new ListModelList(roleList);
		roleModel.add(0, "- Select User Role -");
		userRoleListBox.setModel(roleModel);
		userRoleListBox.setItemRenderer(new ListitemRenderer() {
        	public void render(Listitem anItem, Object data, int theIndex) {
        		if(!data.getClass().getName().endsWith("String")) {
	        		Role aRole = (Role) data;
	        		String roleName = aRole.getRoleName();
	        		anItem.setLabel(roleName);
	        		anItem.setValue(roleName);
        		} else {
        			String emptyRow = (String) data;
        			anItem.setLabel(emptyRow);
	        		anItem.setValue(emptyRow);
        		}
        	}
        });		
		
		this.userList = (Listbox) Path.getComponent("//appHome/appHomeWin/usersListBox");		
		
		User aUser = (User) desktop.getSession().getAttribute("currentUserForEditing");
		if(aUser != null) {
			//given a User, set the current mode for the composer to EDIT
			this.currentMode = UserComposer.EDIT_MODE;
			userWindow.setTitle("Edit User");
			
			this.currentUserIndex = userList.getSelectedIndex();
			this.currentUser = (User) userList.getListModel().getElementAt(currentUserIndex);
			this.firstNameTxtBox.setValue(this.currentUser.getFirstname());
			this.surnameTxtBox.setValue(this.currentUser.getSurname());
			this.emailTxtBox.setValue(this.currentUser.getEmail());
			this.emailTxtBox.setDisabled(true);
			this.passwordTxtBox.setValue(this.currentUser.getPassword());
			
			//pre- select the appropriate user role
			String userRole = this.currentUser.getRole();
			List<Role> roles = userDataManager.getRoles();
			
			Iterator<Role> rolesIter = roles.iterator();
			Role aRole;
			while(rolesIter.hasNext()) {
				aRole = rolesIter.next();
				if((aRole.getRoleName().equals(userRole))) {
					int roleIndex = roles.indexOf(aRole);
					userRoleListBox.setSelectedIndex(roleIndex + 1);						
				}
			}
			
			desktop.getSession().removeAttribute("currentUserForEditing");
		} else {
			//in the absence of a User, set the current mode for the composer to CREATE
			this.currentMode = UserComposer.CREATE_MODE;
			userWindow.setTitle("Create User");
		}

	}
	
	
	
	public void onClick$okBtn() {
		try {
			
			if(!firstNameTxtBox.isValid()) {
				this.displayError("A valid first name must be provided", "Invalid First Name");
				return;
			}
			
			
			if(!surnameTxtBox.isValid()) {
				this.displayError("A valid surname must be provided", "Invalid Surname");
				return;
			}
			
			
			if(!emailTxtBox.isValid()) {
				this.displayError("A valid email address must be provided", "Invalid Email");
				return;
			}
			
			
			if(!passwordTxtBox.isValid()) {
				this.displayError("A valid password must be provided", "Invalid Password");
				return;
			}
			
			if(!confirmPasswordTxtBox.isValid()) {
				this.displayError("The password confirmation must be provided", "Missing Password Confirmation");
				return;
			}
			
			
			if(passwordTxtBox.getValue() != null && !passwordTxtBox.getValue().equals(confirmPasswordTxtBox.getValue())) {
				this.displayError("The passwords do not match", "Password Mismatch");
				return;
			}
			
			
			if(userRoleListBox.getSelectedIndex() <= 0) {
				this.displayError("A user role must be selected", "Invalid User Role");
				return;
			}
			
			//if all input constraints are met, update or create the User
				
			ListModelList listModel = (ListModelList) userList.getListModel();
			
			//If the dialog is in EDIT MODE, update the details of the current User
			if(this.currentMode == UserComposer.EDIT_MODE) {
				this.setUserDetails(currentUser);

				//refresh the list of Users
				listModel.remove(this.currentUserIndex);
				listModel.add(this.currentUserIndex, this.currentUser);
				this.userList.setSelectedIndex(this.currentUserIndex);
			}
			
			
			//If the dialog is in CREATE MODE, add the new User
			if(this.currentMode == UserComposer.CREATE_MODE) {
				User newUser = userDataManager.newUser();
				this.setUserDetails(newUser);
				
				int updateResult = userDataManager.addUser(newUser);
				if(updateResult == UserDataManager.DUPLICATE_EMAIL) {
					this.displayError("A user with the given email address already exists", "Duplicate Email");
					return;
				}
				
				
				listModel.add(newUser);
				this.userList.setSelectedIndex(listModel.indexOf(newUser));
			}
			
			//save the data and close the dialog
			userDataManager.saveUserData();
			userWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	public void onClick$cancelBtn() {
		try {
			userWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	private void setUserDetails(User aUser) {
		aUser.setFirstname(this.firstNameTxtBox.getValue());
		aUser.setSurname(this.surnameTxtBox.getValue());
		aUser.setEmail(this.emailTxtBox.getValue());
		aUser.setPassword(this.passwordTxtBox.getValue());
		
		String selectedRoleName = (String) this.userRoleListBox.getSelectedItem().getValue();
		aUser.setRole(selectedRoleName);
	}

}
