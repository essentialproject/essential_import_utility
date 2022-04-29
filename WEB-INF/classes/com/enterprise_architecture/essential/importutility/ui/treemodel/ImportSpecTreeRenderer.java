/**
 * Copyright (c)2009-2019 Enterprise Architecture Solutions Ltd.
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
 * 18.07.2011	JP	1st coding.
 * 23.08.2018	JWC Upgrade to ZK 8.5, remove ZSS. Added new 'theIndex' parameter to render()
 * 30.01.2019	JWC Remove all System.out.printlns which are hang-over from debugging v2 ZK (8.5) upgrade
 * 
 */
package com.enterprise_architecture.essential.importutility.ui.treemodel;

//import java.util.ArrayList;
//import java.util.List;
import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
//import org.zkoss.zk.ui.Page;
//import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
//import org.zkoss.zkplus.databind.BindingListModelList;
//import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecData;
import com.enterprise_architecture.essential.importutility.data.importspec.script.ImportSpecDataManager;

/**
 * This class extends the ZK TreeitemRenderer class to provide behaviour specific to Essential
 * Import Specifications
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class ImportSpecTreeRenderer implements TreeitemRenderer {
	
	private static String TOP_LEVEL_NODE_STYLE = "font-weight:bold;color:#C41E3A;";
	private static String INSTANCE_LEVEL_NODE_STYLE = "font-weight:bold";
	private static String SLOT_LEVEL_NODE_STYLE = "font-style:oblique";
	private static String VALID_NODE_STYLE = "font-weight:bold;color:green;";
	private static String NODE_ERROR_STYLE = "font-weight:bold;color:red;";	
	
    public void render(Treeitem item, Object node, int theIndex) throws Exception {
    	final DefaultTreeNode treeNode = (DefaultTreeNode) node;        
    	Treerow tr = item.getTreerow();
    	    	
    	if (tr == null) {
            tr = new Treerow();
        }else{
        	//System.out.println(">>>> Clearing children in render()");
            tr.getChildren().clear();
        }
    	
    	//Treerow tr = new Treerow();
        //System.out.println(">>>>> ImportSpecTreeRenderer.render(), appending a child row");
        //System.out.println(">>>>> ImportSpecTreeRenderer.render(), node: " + item.toString() + " Class: " + item.getClass());
        try
        {
        	item.appendChild(tr);
        }
        catch(Exception anEx)
        {
        	System.out.println(">>>>> Caught exception in ImportSpecTreeRenderer, appending a child row");
        	System.out.println(">>>>> ImportSpecTreeRenderer.render(), child list size: " + item.getChildren().size());
//        	Iterator<Component> aChildListIt = item.getChildren().iterator();
//        	while(aChildListIt.hasNext())
//        	{
//        		System.out.println(">>>>> ImportSpecTreeRenderer.render(), child of row is: " + aChildListIt.next().toString());
//        	}        	
        }
        
        ImportSpecData treeData = (ImportSpecData) treeNode.getData();
        Treecell tc = new Treecell(treeData.toString());
        tr.appendChild(tc);
        
        Treecell validTC;
        if(treeData.isValid()) {
        	validTC = new Treecell("VALID");
	        validTC.setStyle(ImportSpecTreeRenderer.VALID_NODE_STYLE);
	        tr.appendChild(validTC);
        } else {
        	validTC = new Treecell("ERRORS");
	        validTC.setStyle(ImportSpecTreeRenderer.NODE_ERROR_STYLE);
	        tr.appendChild(validTC);
        }
        
        //System.out.println("ImportSpecTreeRenderer.render(): 106 - " + item.getDesktop().getSession().getAttribute("currentTreeNode"));
        DefaultTreeNode selectedNode = (DefaultTreeNode) item.getDesktop().getSession().getAttribute("currentTreeNode");
        
        //System.out.println("ImportSpecTreeRenderer.render(): 117 treeNode - " + treeNode);        

        ImportSpecData specData = (ImportSpecData) treeNode.getData();
        if(specData.getElementLevel() == ImportSpecDataManager.ROOT_ELEMENT_LEVEL) {
        	tc.setStyle(ImportSpecTreeRenderer.TOP_LEVEL_NODE_STYLE);
        } else if(specData.getElementLevel() == ImportSpecDataManager.INSTANCE_ELEMENT_LEVEL) {
        	tc.setStyle(ImportSpecTreeRenderer.INSTANCE_LEVEL_NODE_STYLE);
        } else {
        	tc.setStyle(ImportSpecTreeRenderer.SLOT_LEVEL_NODE_STYLE);
        }
        
        final Desktop desktop = tr.getDesktop();
        IdSpace window = item.getSpaceOwner();
        final Borderlayout borderLayout = (Borderlayout) window.getFellow("editExcelBorderLayout");               
        tr.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
            	//System.out.println("ImportSpecTreeRenderer.onEvent(): 107. Tree Node Class name is: " + treeNode.getClass().getName()); 
            	//System.out.println("ImportSpecTreeRenderer.onEvent(): 110. Event is: " + event.getName());
            	//System.out.println(":::::.. ImportSpecTreeRenderer.onEvent(): selected item = " + event.toString());
            	if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.WorksheetTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("worksheet_element_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.GlobalInstanceTreeNode")) {
            		clearEditorPanel();	
            		this.initEditor("global_instance_editor.zul");
                }
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleInstanceTreeNode")) {
            		clearEditorPanel();	
            		this.initEditor("simple_instance_editor.zul");
                }
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedInstanceTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("derived_instance_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteSimpleInstanceTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("delete_simple_instance_editor.zul");
	            }
	        	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteDerivedInstanceTreeNode")) {
	        		clearEditorPanel();
	        		this.initEditor("delete_derived_instance_editor.zul");
	        	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.SimpleSlotTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("simple_slot_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.PrimitiveSlotTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("primitive_slot_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.InstanceSlotTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("instance_slot_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DerivedSimpleSlotTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("derived_simple_slot_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.RemoveInstanceSlotTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("remove_instance_slot_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteInstanceSlotTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("delete_instance_slot_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.RemoveAllInstanceSlotTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("remove_all_instance_slot_editor.zul");
            	}
            	else if(treeNode.getClass().getName().equals("com.enterprise_architecture.essential.importutility.ui.treemodel.DeleteAllInstanceSlotTreeNode")) {
            		clearEditorPanel();
            		this.initEditor("delete_all_instance_slot_editor.zul");
            	}
                else {
                	//System.out.println("::::: ImportSpecTreeRenderer.onEvent: Default else clause. TreeNode not matched");
                	this.clearEditorPanel();
            	}
            	
            }
            
            private void clearEditorPanel() {
            	East eastpanel = borderLayout.getEast();
            	
            	eastpanel.getChildren().clear();
        		//eastpanel.invalidate();
            }
            
            private void initEditor(String editorName) {
            	East eastpanel = borderLayout.getEast();
            	
            	//eastpanel.getChildren().clear();
            	
            	//System.out.println("ImportSpecTreeREnderer.initEditor(): editorName = " + editorName);
            	//System.out.println("ImportSpecTreeREnderer.initEditor(): eastpanel = " + eastpanel.getId());
            	//System.out.println("ImportSpecTreeREnderer.initEditor(): treeNode.getChildCount() = " + treeNode.getChildCount());
            	//System.out.println("ImportSpecTreeREnderer.initEditor(): treeNode.getData() = " + treeNode.getData().toString());
            	
            	//System.out.println("ImportSpecTreeRenderer.initEditor(): Set session Attributes. Calling createComponents");
            	Component aComponent = Executions.createComponents("/ui/import_spec_editor/" + editorName, eastpanel, null);
            	//System.out.println(">>>! in initEditor(). Component is: " + aComponent.toString());
            	//System.out.println(">>!! in initEditor(). editorName is: " + editorName);
            	//System.out.println("ImportSpecTreeRenderer.initEditor(): Called createComponents");
            	
            	// Include this to ensure that the session variables are set for when the Import Specification is manipulated
            	desktop.getSession().setAttribute("currentTreeNode", treeNode);	
            	desktop.getSession().setAttribute("currentTreeData", treeNode.getData());            	            	
        		
        		//System.out.println("ImportSpecTreeRenderer.initEditor(): Invalidated East Panel");
        		//System.out.println("ImportSpecTreeREnderer.initEditor(), 180: treeNode.getChildCount() = " + treeNode.getChildCount());
            	//System.out.println("ImportSpecTreeREnderer.initEditor(), 181: treeNode.getData() = " + treeNode.getData().toString());
            	            	
            }
            
        });
    }
}