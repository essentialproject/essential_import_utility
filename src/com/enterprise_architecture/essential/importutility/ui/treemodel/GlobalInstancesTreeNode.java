/**
 * Copyright (c)2009-2011 Enterprise Architecture Solutions Ltd.
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
 * 
 */
package com.enterprise_architecture.essential.importutility.ui.treemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zkoss.zul.DefaultTreeNode;

import com.enterprise_architecture.essential.importutility.data.importspec.GlobalInstances;
import com.enterprise_architecture.essential.importutility.data.importspec.script.WorksheetImportSpecScript;

/**
 * This class extends the ZK DefaultTreeNode class to provide behaviour specific to an Essential
 * Import Specification
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class GlobalInstancesTreeNode extends DefaultTreeNode {
	// Node Control the default open
    private boolean open = false;
 
    public GlobalInstancesTreeNode(Object data, List children, boolean open) {
        super(data, children);
        if(children != null && data != null) {
        	initChildren();
        }
        this.setOpen(open);
    }
 
    public GlobalInstancesTreeNode(Object data, List children) {
        super(data, children);
        if(children != null && data != null) {
        	initChildren();
        }
    }
 
    public GlobalInstancesTreeNode(Object data) {
    	this(data, new ArrayList());
    }
 
    public boolean isOpen() {
        return true;
    }
 
    public void setOpen(boolean open) {
        this.open = open;
    }
    
    public boolean isLeaf() {
    	GlobalInstances globalInstances = (GlobalInstances) getData();
    	return (globalInstances.getDerivedInstance().size() == 0);
    }
    
    private void initChildren() {
    	GlobalInstances globalInstances = (GlobalInstances) getData();
    	Iterator instancesIter = globalInstances.getDerivedInstance().iterator();
    	
		while(instancesIter.hasNext()) {
			Object anInstance =  instancesIter.next(); 
			System.out.println("Adding Global Instance: " + anInstance.getClass().getName());
			if(anInstance.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.GlobalInstanceScript")) {
				GlobalInstanceTreeNode globalInstanceTreeNode = new GlobalInstanceTreeNode(anInstance, new ArrayList() );
				this.add(globalInstanceTreeNode);
			}
		}
    }
}
