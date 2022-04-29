/**
 * Copyright (c)2006-2018 Enterprise Architecture Solutions Ltd.
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
 * 31.08.2011	JP	1st coding.
 * 23.08.2018	JWC Upgrade to ZK 8.5, remove ZSS
 * 					Remove this capability from Import Utility v2.0
 */

package com.enterprise_architecture.essential.importutility.ui;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Window;

import com.enterprise_architecture.essential.importutility.data.global.ImportActivity;
import com.enterprise_architecture.essential.importutility.data.global.ImportUtilityDataManager;

/**
 * SheetDimensionComposer demonstrate how to change different sheet
 * @author Sam
 *
 */
public class PreviewSpreadsheetComposer extends EssentialImportInterface{
        
        public Combobox sheets;
        //public Spreadsheet spreadsheet;
        public Window previewSSDialog;
     
        
        
        //override
        @Override
        public void doAfterCompose(Component comp) throws Exception {
                super.doAfterCompose(comp);
                
                //retrieve the current Import Activity from the session
                ImportActivity currentImpAct = (ImportActivity) desktop.getSession().getAttribute("currentImpAct");
                
                //get the data manager for the application
                ImportUtilityDataManager dataManager = this.getImportUtilityDataManager();
                
                //get the path of the import activity's spreadsheet and associate it with the spreadsheet UI component
                if(currentImpAct != null) {
                	String ssFilePath = dataManager.getLocalImpActSpreadsheetPath(currentImpAct);
                	if(ssFilePath != null) {
                		System.out.println("OPENING SPREADSHEET AT PATH: " + ssFilePath);
                		//spreadsheet.setSrc(ssFilePath);
                	}
                }
                
                
                List<String> sheetNames = new ArrayList<String>();
                //int sheetSize = spreadsheet.getBook().getNumberOfSheets();
                //for (int i = 0; i < sheetSize; i++){
                //        sheetNames.add(spreadsheet.getSheet(i).getSheetName());
                //}
                
                //BindingListModelList model = new BindingListModelList(sheetNames, true);
                //sheets.setModel(model);
        }
        
        /**
         * Sets selected sheet
         * @param event
         */
        public void onSelect$sheets(Event event) {
                //spreadsheet.setSelectedSheet(sheets.getText());
        }
        
        public void onClick$closeBtn() {
    		try {
    			previewSSDialog.detach();
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    		}

    	}


}
