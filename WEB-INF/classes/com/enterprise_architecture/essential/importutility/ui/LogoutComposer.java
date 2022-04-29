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
 * 28.06.2011	JP	1st coding.
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.user.UserCredentialManager;
import com.enterprisearchitecture.oauth.OauthTokenConsumerUtils;


/**
 * This class is the UI controller for the modal dialog used to create and edit
 * source repositories for Essential imports
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class LogoutComposer extends EssentialImportInterface {

	public Button logoutBtn;
	
	
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		/**
		 * DK 2020-06-16
		 * Removed for EIP mode as not required as we redirect to the login app if the user is not AuthN
		 * and is causing a null pointer when the user is logged out.
		 */
		if (!this.isEIPMode()) {
			if(UserCredentialManager.getInstance(desktop, this.getContextPath()).isAuthenticated(desktop)){
				logoutBtn.setVisible(true);
			} else {
				logoutBtn.setVisible(false);
			}
		}
	}
	
	
	public void onClick$logoutBtn() {
		try {
			UserCredentialManager uCM = UserCredentialManager.getInstance(desktop, this.getContextPath());
			uCM.logoutCurrentUser(desktop);
			if(this.isEIPMode()) {
				HttpServletResponse aResponse = (HttpServletResponse) desktop.getExecution().getNativeResponse();
				OauthTokenConsumerUtils.removeAllOauthTokenCookies(aResponse);
				execution.sendRedirect("redirectToAuthnServer.zul");
			} else {
				execution.sendRedirect("home.zul");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
