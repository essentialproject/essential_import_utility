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
 * 20.07.2015	JWC	Extended to work with EIP
 * 19.04.2016	JWC Added cookie to pick up selected tenant (from Viewer, EDM, etc.)
 * 23.01.2017	JWC Added to remove the SAML login option when not in EIP mode
 * 11.06.2018	JWC	Handle switch to the new Login App for EIP mode
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.net.URLDecoder;
import java.net.URLEncoder;
//import java.util.Iterator;
//import java.util.List;



//import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;

//import org.zkoss.zk.ui.Path;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
//import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
//import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
//import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
//import com.enterprise_architecture.essential.importutility.data.global.SourceRepository;
import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
//import com.enterprise_architecture.essential.importutility.data.user.UserDataManager;
//import com.enterprise_architecture.essential.importutility.utils.EssentialServletContext;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * source repositories for Essential imports
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class LoginComposer extends EssentialImportInterface {
	private static final long serialVersionUID = -4474202530368905870L;
	private static final String USER_COOKIE_NAME = "username";
	private static final String TENANT_COOKIE_NAME = "tenant";
	
	public static final int SECONDS_PER_YEAR = 60*60*24*365;	
	
	public Textbox emailTxtBox;
	public Textbox passwordTxtBox;
	
	// Textbox for the tenant name
	public Textbox tenantTxtBox;
	public Label tenantTxtBoxLabel;
	
	public Checkbox rememberUserCkBox;
	public Label rememberMeLabel;
	
	// Button for SAML login
	public Button samlLogin;

	public Window loginWindow;
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		// 11.06.2018 JWC - not matter what, if in EIP mode, redirect to home_eip.zul
		if(this.isEIPMode()) {
			execution.sendRedirect("home_eip.zul");
		}
		// end of 11.06.2018 JWC
		
		if(UserCredentialManager.getInstance(desktop, this.getContextPath()).isAuthenticated(desktop)){
			if(this.isEIPMode()) {
				execution.sendRedirect("home_eip.zul");
			} else {
				execution.sendRedirect("home.zul");
			}
		} 
		
		if(isEIPMode())
		{
			rememberUserCkBox.setVisible(false);
			rememberMeLabel.setVisible(false);
		}
		else
		{
			tenantTxtBox.setVisible(false);
			tenantTxtBoxLabel.setVisible(false);
			samlLogin.setVisible(false);
		}
		
		HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		Cookie[] cookies = request.getCookies();		
		
		if(cookies != null) {
			Cookie aCookie;
			boolean isUserNameCookie = false;
			boolean isTenantNameCookie = false;
			
			for(int i=0;i<cookies.length;i++) 
			{
				aCookie = cookies[i];
				if(aCookie.getName().equals(USER_COOKIE_NAME)) {
					String lastUserEncoded = aCookie.getValue();
					String lastUser = URLDecoder.decode(lastUserEncoded, "UTF-8");
					
					if(lastUser != null) {
						// If we're in EipMode, then this will be disabled
						// NO NEED TO REVIEW HOW UID and TENANT IS STORED IN COOKIE
//						if(isEIPMode())
//						{
//							String lastTenant = URLDecoder.decode(lastUserEncoded, "UTF-8");
//							this.tenantTxtBox.setValue(lastTenant);
//						}
						//this.tenantTxtBox.setValue(lastTenant));
						this.rememberUserCkBox.setChecked(true);
						this.emailTxtBox.setValue(lastUser);
						this.passwordTxtBox.setFocus(true);
					}
					//break;
					isUserNameCookie = true;
				}
				// 19.04.2016 JWC - Get the (shared) Tenant Name Cookie, if in EIP Mode
				else if(isEIPMode())
				{
					if(aCookie.getName().equals(TENANT_COOKIE_NAME))
					{
						String aLastTenantEncoded = aCookie.getValue();
						String aLastTenant = URLDecoder.decode(aLastTenantEncoded, "UTF-8");
						if(aLastTenant != null)
						{
							this.tenantTxtBox.setValue(aLastTenant);
						}
						isTenantNameCookie = true;
					}															
				}
				// If we've got the Cookies we need, break the loop
				if(isUserNameCookie)
				{
					if(isEIPMode())
					{
						if(isTenantNameCookie)
						{
							// If we're in EIP Mode and have both cookies, break
							break;
						}
					}
					else
					{
						// If we're not in EIP Mode but have UserName Cookie, break
						break;
					}
				}
			}
		}
		
		
	/*	UserDataManager udm = UserDataManager.getIntance(desktop, getContextPath());
		udm.initUsersAndRoles();   */
	}
	
	
	public void onClick$loginBtn() {
		try {
		//	this.popupDebugWindow(exportlabel.getValue());
			
			if(isEIPMode())
			{
				if(tenantTxtBox.getValue() == null || tenantTxtBox.getValue().isEmpty()) {
					this.displayError("A tenant name must be provided", "Invalid Tenant");
					return;
				}
			}
			if(emailTxtBox.getValue() == null || emailTxtBox.getValue().isEmpty()) {
				this.displayError("An email must be provided", "Invalid Email");
				return;
			}
			
			if(passwordTxtBox.getValue() == null || passwordTxtBox.getValue().isEmpty()) {
				this.displayError("A password must be provided", "Invalid Password");
				return;
			}
			
			String userEmail = emailTxtBox.getValue();
			String password = passwordTxtBox.getValue();
			
			if(this.isEIPMode())
			{
				/**
				 * DK 15.05.2018
				 * Do nothing, we do not show the login page if we are in EIP mode. Instead we use redirectToAuthnServer.zul
				 * to redirect users to the AuthN Server in order to present their credentials.
				 * See RedirectToAuthnServerComposer.java for more details.
				 */
			}
			else
			{
				UserCredentialManager uCM = UserCredentialManager.getInstance(desktop, this.getContextPath());
				if(uCM.login(userEmail, password) == UserCredentialManager.LOGIN_SUCCESS) 
				{
					HttpServletResponse response = (HttpServletResponse) Executions.getCurrent().getNativeResponse();
					if(this.rememberUserCkBox.isChecked()) {
						//create a cookie with the user's email
						String userEmailEncoded = URLEncoder.encode(userEmail, "UTF-8");			
						Cookie userCookie = new Cookie(USER_COOKIE_NAME, userEmailEncoded);
						
						userCookie.setMaxAge(SECONDS_PER_YEAR);
						response.addCookie(userCookie); 
					} else {
						Cookie userCookie = new Cookie(USER_COOKIE_NAME, "");
						userCookie.setMaxAge(0);
						response.addCookie(userCookie);
					}
					
					loginWindow.detach();
					if(this.isEIPMode()) {
						execution.sendRedirect("home_eip.zul");
					} else {
						execution.sendRedirect("home.zul");
					}
				} 
				else 
				{			
					this.displayError("Invalid user email or password", "Invalid Username or Password");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	

}
