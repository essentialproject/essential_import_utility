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
 * 15.05.2018	DK	Updates to migrate to AuthN Server in EIP Mode
 * 11.06.2018	JWC	Fixed typo in redirect when not AuthN-ed in O/S mode
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

//import java.util.Iterator;
//import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Executions;
//import org.zkoss.zk.ui.Path;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

//import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;

/**
 * This class is the UI controller for the page used to execute the testing and running
 * of import activities
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class ImportActivityHomeComposer extends EssentialImportInterface {
	
	public Window excelImpActHome;
	public Label impActNameLbl;
	public Label impActDescLbl;
	public Label impActLastModLbl;
	public Label impActLastTestLbl;
	public Label impActLastRunLbl;
	
	
	private ImportActivity currentImpAct;
	private ImportUtilityDataManager appDataManager;
	private ImportSpecDataManager impSpecDataManager;
	
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);	
		
		this.currentImpAct = (ImportActivity) desktop.getSession().getAttribute("currentImpAct");
		
		if(this.currentImpAct != null) {
			//given an ImportActivity, set up a data manager for the import spec associated with the activity
			this.impSpecDataManager = this.getImportSpecDataManager();
			
			//...and set the labels in the window
			this.impActNameLbl.setValue(this.currentImpAct.getImportActivityName());
			this.impActDescLbl.setValue(this.currentImpAct.getImportActivityDescription());

			//Display the modified date for the Import Activity
	        XMLGregorianCalendar impActModDate = this.currentImpAct.getImportActivityModifiedDate();
	        if(impActModDate != null) {
	        	impActLastModLbl.setValue(this.formatXMLCalendar(impActModDate));
	        } else {
	        	impActLastModLbl.setValue("-");
	        }
	        
	        
	      //Display the last tested date for the Import Activity
	        XMLGregorianCalendar impActTestedDate = this.currentImpAct.getImportActivityTestedDate();
	        if(impActTestedDate != null) {
	        	impActLastTestLbl.setValue(this.formatXMLCalendar(impActTestedDate));
	        } else {
	        	impActLastTestLbl.setStyle("font-style:italic");
	        	impActLastTestLbl.setValue("Never");
	        }
	        
	      //Display the last executed date for the Import Activity
	        XMLGregorianCalendar impActRunDate = this.currentImpAct.getImportActivityToLiveDate();
	        if(impActRunDate != null) {
	        	impActLastRunLbl.setValue(this.formatXMLCalendar(impActRunDate));
	        } else {
	        	impActLastRunLbl.setStyle("font-style:italic");
	        	impActLastRunLbl.setValue("Never");
	        }
		} 
	}
	
	
	
	public void onClick$backHomeBtn() {
		try {
		//	Window appHomeWin = (Window) desktop.getSession().getAttribute("appHomeWin");
				//	appHomeWin.setClosable(false);
			excelImpActHome.detach();
			desktop.getSession().removeAttribute("currentImpAct");
			if (isEIPMode()) {
				execution.sendRedirect("redirectToAuthnServer.zul");
			} else {
				execution.sendRedirect("/index.zul");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
