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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.global.EssentialImportUtility;
import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportScriptTemplate;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;

/**
 * This class is the UI controller for the modal dialog used to create and edit
 * Jython import script templates that are applied to generate the intermediate Essential
 * imports during testing and running of import activities
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */
public class ImportScriptComposer extends EssentialImportInterface {
	private static final int CREATE_MODE = 1;
	private static final int EDIT_MODE = 2;
	private int currentMode;
	private int currentImpScriptTempIndex;
	private ImportScriptTemplate currentImpScriptTemplate;
	private ImportUtilityDataManager dataManager;
	

	public Listbox impScriptElementListBox;
	public Textbox impScriptTempStringTxtBox;
	public Listbox impScriptModeListBox;
	public Listbox scriptTokenListBox;
	public Window impScriptTemplateWindow;
	
	private Listbox impScriptTemplateList;
	private int currentElementIndex;
	private HashMap<String, HashMap<String, String>> importScriptTokensMap;
	
	public static String EMPTY_SCRIPT_STRING = "<Type Script Here>";

	
	
	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.GenericForwardComposer#doAfterCompose(org.zkoss.zk.ui.Component)
	 */
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		this.impScriptTemplateList = (Listbox) Path.getComponent("//appHome/appHomeWin/impScriptTempListBox");		
		
		ImportScriptTemplate impScriptTemp = (ImportScriptTemplate) desktop.getSession().getAttribute("currentImportScriptTemplate");
		
		//set up the list of Element types
		importScriptTokensMap = ImportSpecDataManager.getImportScriptTokensMap();
		Set<String> scriptElements = importScriptTokensMap.keySet();
		List<String> scriptElementList = new ArrayList<String>(scriptElements);
		Collections.sort(scriptElementList);
		scriptElementList.add(0,"- Select Element -");
		ListModelList scriptElementsModel = new ListModelList(scriptElementList);
		impScriptElementListBox.setModel(scriptElementsModel);
		
		
		//set up the list of Mode Types
		List<String> modeList = new ArrayList<String>();
		modeList.add(ImportSpecDataManager.IMPORT_MATCHMODE_NONE);
		modeList.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYNAME);
		modeList.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYID);
		modeList.add(ImportSpecDataManager.IMPORT_MATCHMODE_BYINTID);
		modeList.add(ImportSpecDataManager.IMPORT_MATCHMODE_NEW);
		modeList.add(ImportSpecDataManager.IMPORT_MATCHMODE_DUP);
		ListModelList modesModel = new ListModelList(modeList);
		impScriptModeListBox.setModel(modesModel);
		
		//initialise the script template text box
		impScriptTempStringTxtBox.setValue(ImportScriptComposer.EMPTY_SCRIPT_STRING);
		
		
		
		
	/*	if(scriptElementsModel.getSize() > 1) {
			String selectedScriptElement = (String) impScriptElementListBox.getModel().getElementAt(0);
			this.setAllowedTokens(selectedScriptElement);
		} */
		
		if(impScriptTemp != null) {
			//given an ImportActivity, set the current mode for the composer to EDIT
			this.currentMode = ImportScriptComposer.EDIT_MODE;
			impScriptTemplateWindow.setTitle("Edit Jython Script Template");
			
			this.currentImpScriptTempIndex = impScriptTemplateList.getSelectedIndex();
			this.currentImpScriptTemplate = (ImportScriptTemplate) impScriptTemplateList.getListModel().getElementAt(currentImpScriptTempIndex);
			this.impScriptTempStringTxtBox.setValue(this.currentImpScriptTemplate.getImportScriptTemplateString());

			//set the element type for the Import Script Template
			String elementType = this.currentImpScriptTemplate.getImportScriptTemplateClassName();		
			List types = this.impScriptElementListBox.getItems();
			
			int elementIndex = scriptElementList.indexOf(elementType);
			if(elementIndex > 0) {
				impScriptElementListBox.setSelectedIndex(elementIndex);	
				//String selectedScriptElement = (String) impScriptElementListBox.getModel().getElementAt(elementIndex);
				this.setAllowedTokens(elementType);
			}
			
			
			
			//set the instance matching mode for the Import Script Template
			String mode = this.currentImpScriptTemplate.getImportScriptTemplateMode();
			List modes = this.impScriptModeListBox.getItems();
			
			int modeIndex = modeList.indexOf(mode);
			if(modeIndex >= 0) {
				impScriptModeListBox.setSelectedIndex(modeIndex);
			}
			
			
		/*	Iterator typeIter = types.iterator();
			Listitem currentListItem;
			Iterator modeIter = modes.iterator();
			while(modeIter.hasNext()) {
				currentListItem = (Listitem) modeIter.next();
				String currentMode = currentListItem.getValue().toString();
				if((currentListItem.getLabel() != null) && (currentMode.equals(mode))) {
					int modeIndex = impScriptModeListBox.getIndexOfItem(currentListItem);
					impScriptModeListBox.setSelectedIndex(modeIndex);						
				}
			}  */
			
			desktop.getSession().removeAttribute("currentImportScriptTemplate");
		} else {
			//in the absence of an ImportActivity, set the current mode for the composer to CREATE
			this.currentMode = ImportScriptComposer.CREATE_MODE;
			impScriptTemplateWindow.setTitle("Create Jythin Script Template");
			
			//initialise the combo box of element types
			impScriptElementListBox.setSelectedIndex(0);
			
			//initialise the combo box of matching modes
			impScriptModeListBox.setSelectedIndex(0);
		}

	}
	
	public void onSelect$impScriptElementListBox() {
		try {
			final int selectedIndex = impScriptElementListBox.getSelectedIndex();
			String scriptText = impScriptTempStringTxtBox.getValue();
			if(selectedIndex > 0 && (scriptText != null) && (!scriptText.equals(ImportScriptComposer.EMPTY_SCRIPT_STRING))) {
				Messagebox.show(("Changing the element will clear the current script template. Continue?" ), "Delete Target Environment?", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, 
						new EventListener() {
							public void onEvent(Event evt) { 
									switch (((Integer)evt.getData()).intValue()) {
										case Messagebox.YES: 
											impScriptTempStringTxtBox.setValue(ImportScriptComposer.EMPTY_SCRIPT_STRING);
											break;
										case Messagebox.NO: 
											impScriptElementListBox.setSelectedIndex(currentElementIndex);
											break;
									}
							}
						}
				);
			}
			String selectedScriptElement = (String) impScriptElementListBox.getModel().getElementAt(selectedIndex);
			setAllowedTokens(selectedScriptElement);
			currentElementIndex = selectedIndex;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	private void setAllowedTokens(String selectedScriptElement) {
		try {
				HashMap<String, String> allowedTokenMap = this.importScriptTokensMap.get(selectedScriptElement);
				if(allowedTokenMap != null) {
					Set<String> tokenTypeLabels = allowedTokenMap.keySet();
					List<String> tokenTypeList = new ArrayList<String>(tokenTypeLabels);
					Collections.sort(tokenTypeList);
					tokenTypeList.add(0,"<Select Token>");
					ListModelList tokenTypeLabelModel = new ListModelList(tokenTypeList);
					scriptTokenListBox.setModel(tokenTypeLabelModel);
				}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$addTokenBtn() {
		try {
			String scriptTemplate = this.impScriptTempStringTxtBox.getValue();
			int scriptElementIndex = this.impScriptElementListBox.getSelectedIndex();
			int tokenIndex = scriptTokenListBox.getSelectedIndex();
			if(scriptElementIndex > 0 && tokenIndex > 0) {
				String scriptElement = (String) impScriptElementListBox.getModel().getElementAt(scriptElementIndex);
				String scriptTokenType = (String) scriptTokenListBox.getModel().getElementAt(tokenIndex);
			//	System.out.println("ADDING SCRIPT TOKEN: " + scriptElement + " - " + scriptTokenType);
				
				HashMap<String, String> tokenMap = importScriptTokensMap.get(scriptElement);
				String scriptToken = tokenMap.get(scriptTokenType);
				impScriptTempStringTxtBox.setValue(scriptTemplate + scriptToken);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	public void onClick$okBtn() {
		try {
			ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
			if (this.impScriptTempStringTxtBox.getValue().length() > 0 && (impScriptElementListBox.getSelectedIndex() >= 0) && (impScriptModeListBox.getSelectedIndex() >= 0)) {
				
				//Update the details of the current Import Script Template
				ListModelList listModel = (ListModelList) impScriptTemplateList.getListModel();
				
				if(this.currentMode == ImportScriptComposer.EDIT_MODE) {
							
					//update the attributes of the current Import Script Template
					this.currentImpScriptTemplate.setImportScriptTemplateClassName(impScriptElementListBox.getSelectedItem().getValue().toString());
					this.currentImpScriptTemplate.setImportScriptTemplateMode(impScriptModeListBox.getSelectedItem().getValue().toString());
					this.currentImpScriptTemplate.setImportScriptTemplateString(impScriptTempStringTxtBox.getValue());
					
					listModel.remove(this.currentImpScriptTempIndex);
					listModel.add(this.currentImpScriptTempIndex, this.currentImpScriptTemplate);
					this.impScriptTemplateList.setSelectedIndex(this.currentImpScriptTempIndex);
				}
				
				
				//If the dialog is in CREATE MODE, add the new Import Activity to the config data
				if(this.currentMode == ImportScriptComposer.CREATE_MODE) {
					
					//create the new Import Script Template and set the attributes from the UI fields
					ImportScriptTemplate impScriptTemp = dataManager.newImportScriptTemplate();
					impScriptTemp.setImportScriptTemplateClassName(impScriptElementListBox.getSelectedItem().getValue().toString());
					impScriptTemp.setImportScriptTemplateMode(impScriptModeListBox.getSelectedItem().getValue().toString());
					impScriptTemp.setImportScriptTemplateString(impScriptTempStringTxtBox.getValue());
					
					
					//add the new Import Script Template to the list in the main window
					listModel.add(impScriptTemp);
					this.impScriptTemplateList.setSelectedIndex(listModel.indexOf(impScriptTemp));
					
					//add the new Import Script Template to the config data set
					// EssentialImportUtility configData = dataManager.getImportUtilityData();
					// configData.getImportScriptTemplates().add(impScriptTemp);
					this.getImportUtilityDataManager().addImportScriptTemplate(impScriptTemp);
					
				}
				
				//save the config data
				dataManager.saveSystemData();
				impScriptTemplateWindow.detach();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void onClick$cancelBtn() {
		try {
			impScriptTemplateWindow.detach();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
