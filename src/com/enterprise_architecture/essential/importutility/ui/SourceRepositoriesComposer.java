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
 * 28.06.2011	JP	1st coding.
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportEnvironment;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.global.SourceRepository;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * source repositories for Essential imports
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class SourceRepositoriesComposer extends EssentialImportInterface {
	
	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private int currentMode;
	private int currentRepIndex;
	private SourceRepository currentRep;
	private ImportUtilityDataManager dataManager;
	
	public Button okBtn;
	public Button cancelBtn;
	public Textbox sourceRepNameTxtBox;
	public Textbox sourceRepDescTxtBox;

	
	public Window sourceRepWindow;
	
	private Listbox sourceRepList;
	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		this.sourceRepList = (Listbox) Path.getComponent("//appHome/appHomeWin/sourceRepListBox");		
		
		SourceRepository sourceRep = (SourceRepository) desktop.getSession().getAttribute("currentSrcRep");
		if(sourceRep != null) {
			//given a Source Repository, set the current mode for the composer to EDIT
			this.currentMode = SourceRepositoriesComposer.EDIT_MODE;
			sourceRepWindow.setTitle("Edit Source Repository");
			
			this.currentRepIndex = sourceRepList.getSelectedIndex();
			this.currentRep = (SourceRepository) sourceRepList.getListModel().getElementAt(currentRepIndex);
			this.sourceRepNameTxtBox.setValue(this.currentRep.getSourceRepositoryName());
			this.sourceRepDescTxtBox.setValue(this.currentRep.getSourceRepositoryDescription());
			
			desktop.getSession().removeAttribute("currentSrcRep");
		} else {
			//in the absence of a Source Repository, set the current mode for the composer to CREATE
			this.currentMode = SourceRepositoriesComposer.CREATE_MODE;
			sourceRepWindow.setTitle("Create Source Repository");
		}

	}
	
	
	
	public void onClick$okBtn() {
		try {
		//	Retrieve the object that manages the application data
			ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
			
			//if all input constraints are met, update or create the Source Repository
			if (sourceRepNameTxtBox.isValid()) {
				
				ListModelList listModel = (ListModelList) sourceRepList.getListModel();
				
				//If the dialog is in EDIT MODE, update the details of the current Source Repository
				if(this.currentMode == SourceRepositoriesComposer.EDIT_MODE) {
							
					this.currentRep.setSourceRepositoryName(sourceRepNameTxtBox.getValue());
					this.currentRep.setSourceRepositoryDescription(sourceRepDescTxtBox.getValue());

					//refresh the list of Source Repositories
					listModel.remove(this.currentRepIndex);
					listModel.add(this.currentRepIndex, this.currentRep);
					this.sourceRepList.setSelectedIndex(this.currentRepIndex);
				}
				
				
				//If the dialog is in CREATE MODE, add the new Target Environment to the config data
				if(this.currentMode == SourceRepositoriesComposer.CREATE_MODE) {
					SourceRepository srcRep = dataManager.newSourceRepository();
					srcRep.setSourceRepositoryName(sourceRepNameTxtBox.getValue());
					srcRep.setSourceRepositoryDescription(sourceRepDescTxtBox.getValue());
					
					listModel.add(srcRep);
					this.sourceRepList.setSelectedIndex(listModel.indexOf(srcRep));
					
					EssentialImportUtility configData = dataManager.getImportUtilityData();
					configData.getSourceRepositories().add(srcRep);
				}
				
				//save the config data and close the dialog
				dataManager.saveAppData();
				sourceRepWindow.detach();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	public void onClick$cancelBtn() {
		try {
		//	this.popupDebugWindow(exportlabel.getValue());
			sourceRepWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
