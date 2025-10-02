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
 * 20.07.2015	JWC	Extended to work with EIP
 * 19.04.2016	JWC Added cookie to pick up selected tenant (from Viewer, EDM, etc.)
 * 23.01.2017	JWC Added to remove the SAML login option when not in EIP mode
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.enterprise_architecture.essential.report.security.ViewerSecurityManager;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * source repositories for Essential imports
 * 
 * Used in EIP mode to redirect the user to the AuthN Server in order to enter their credentials.
 * Upon successful entry of their credentials, the user is redirected back to the Import Utility
 * to the /authnServerCallback end-point where they are logged in to the Import Utility and taken
 * to the app home page.
 * 
 * @author David Kumar
 * @version 1.0 - 15.05.2018
 *
 */
public class RedirectToAuthnServerComposer extends EssentialImportInterface {
	private static final long serialVersionUID = -989946694671072407L;
	private static final String AUTHN_SERVER_CALLBACK_URI = "/authnServerCallback";
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
	 * 
	 * On page load, will automatically redirect the user to the AuthN Server to enter their credentials
	 * and then redirect them back to the home page of the Import Utility via the authnServerCallback end-point
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		redirectToAuthnServer(request);
	}
	
	/**
	 * Manual redirection to login page by clicking link
	 * @throws UnsupportedEncodingException
	 */
	public void onClick$redirectBtn() throws UnsupportedEncodingException {
		HttpServletRequest request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		redirectToAuthnServer(request);
	}

	private void redirectToAuthnServer(HttpServletRequest request) throws UnsupportedEncodingException {
		ViewerSecurityManager aSecurityMgr = new ViewerSecurityManager(request.getServletContext());
		String baseUrl = "https://"+request.getHeader("host");
		String callbackUri = baseUrl+request.getContextPath()+AUTHN_SERVER_CALLBACK_URI;
		//note we are not setting any state for the Import Utility as doesn't redirect to last page on successful authN
		String redirectUrl = baseUrl+aSecurityMgr.getPropsAuthnServerLoginUrl()
								+ "?redirect_uri="+URLEncoder.encode(callbackUri, "UTF-8");
		execution.sendRedirect(redirectUrl);
	}
	

}
