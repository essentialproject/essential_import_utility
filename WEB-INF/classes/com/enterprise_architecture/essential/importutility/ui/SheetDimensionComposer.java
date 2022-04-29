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
 */

package com.enterprise_architecture.essential.importutility.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;

/**
 * SheetDimensionComposer demonstrate how to change different sheet
 * @author Sam
 *
 */
public class SheetDimensionComposer extends GenericForwardComposer{
        
        Combobox sheets;
        Workbook spreadsheet;
        //override
        @Override
        public void doAfterCompose(Component comp) throws Exception {
                super.doAfterCompose(comp);
                
                List<String> sheetNames = new ArrayList<String>();
                int sheetSize = spreadsheet.getNumberOfSheets();
                for (int i = 0; i < sheetSize; i++){
                        sheetNames.add(spreadsheet.getSheetName(i));
                }
                
                //BindingListModelList model = new BindingListModelList(sheetNames, true);
                // 23.08.2018 JWC - Move away from deprecated List Model class.
		        // Check that the type specified is correct for ListModelList
		        ListModelList<String> model = new ListModelList<String>(sheetNames, true);
		        
                sheets.setModel(model);
        }
        
        /**
         * Sets selected sheet
         * @param event
         */
        public void onSelect$sheets(Event event) 
        {
            //spreadsheet.setSelectedSheet(sheets.getText());
            spreadsheet.setActiveSheet(sheets.getSelectedIndex());
        }


}
