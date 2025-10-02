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
 * 31.01.2019	JWC Updated to resolve issue with creating new instance with ZK 8.5
 * 
 */
package com.enterprise_architecture.essential.importutility.ui.treemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zkoss.zul.DefaultTreeNode;

import com.enterprise_architecture.essential.importutility.data.common.script.DerivedInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.common.script.DerivedSimpleSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.InstanceSlotScript;
import com.enterprise_architecture.essential.importutility.data.common.script.SimpleInstanceTypeScript;
import com.enterprise_architecture.essential.importutility.data.importspec.GlobalInstances;

/**
 * This class extends the ZK DefaultTreeNode class to provide behaviour specific to an Essential
 * Import Specification
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class DerivedInstanceTreeNode extends DefaultTreeNode {
	// Node Control the default open
    private boolean open = false;
 
    public DerivedInstanceTreeNode(Object data, List children, boolean open) {
        super(data, children);
        if(children != null && data != null) {
        	initChildren();
        }
        this.setOpen(open);
    }
 
    public DerivedInstanceTreeNode(Object data, List children) {
        super(data, children);
        if(children != null && data != null) {
        	initChildren();
        }        
    }
 
    public DerivedInstanceTreeNode(Object data) {
        // 31.01.2019 JWC - initialise the children list
    	this(data, new ArrayList());    	
    }
 
    public boolean isOpen() {
        return open;
    }
 
    public void setOpen(boolean open) {
        this.open = open;
    }
    
    public boolean isLeaf() {
    	DerivedInstanceTypeScript instanceScript = (DerivedInstanceTypeScript) getData();
    	return (instanceScript.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().size() == 0);
    }
    
    private void initChildren() {
    	DerivedInstanceTypeScript derivedInstance = (DerivedInstanceTypeScript) getData();
    	Iterator slotsIter = derivedInstance.getInstanceSlotOrRemoveInstanceSlotOrDeleteInstanceSlot().iterator();
		
		while(slotsIter.hasNext()) {
			Object aSlot =  slotsIter.next(); 
			if(aSlot.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.DerivedSimpleSlotScript")) {
				DerivedSimpleSlotTreeNode derSimpleSlotTreeNode = new DerivedSimpleSlotTreeNode(aSlot, new ArrayList() );
				this.add(derSimpleSlotTreeNode);
			} else if(aSlot.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.InstanceSlotScript")) {
				InstanceSlotTreeNode instanceSlotTreeNode = new InstanceSlotTreeNode(aSlot, new ArrayList() );
				this.add(instanceSlotTreeNode);
			} else if(aSlot.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.SimpleSlotScript")) {
				SimpleSlotTreeNode simpleSlotTreeNode = new SimpleSlotTreeNode(aSlot, new ArrayList() );
				this.add(simpleSlotTreeNode);
			} else if(aSlot.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.PrimitiveSlotScript")) {
				PrimitiveSlotTreeNode primSlotTreeNode = new PrimitiveSlotTreeNode(aSlot, new ArrayList() );
				this.add(primSlotTreeNode);
			}  else if(aSlot.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.RemoveInstanceSlotScript")) {
				RemoveInstanceSlotTreeNode instanceSlotTreeNode = new RemoveInstanceSlotTreeNode(aSlot, new ArrayList() );
				this.add(instanceSlotTreeNode);
			} else if(aSlot.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.DeleteInstanceSlotScript")) {
				DeleteInstanceSlotTreeNode instanceSlotTreeNode = new DeleteInstanceSlotTreeNode(aSlot, new ArrayList() );
				this.add(instanceSlotTreeNode);
			} else if(aSlot.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.RemoveAllInstanceSlotScript")) {
				RemoveAllInstanceSlotTreeNode instanceSlotTreeNode = new RemoveAllInstanceSlotTreeNode(aSlot, new ArrayList() );
				this.add(instanceSlotTreeNode);
			} else if(aSlot.getClass().getName().equals("com.enterprise_architecture.essential.importutility.data.common.script.DeleteAllInstanceSlotScript")) {
				DeleteAllInstanceSlotTreeNode instanceSlotTreeNode = new DeleteAllInstanceSlotTreeNode(aSlot, new ArrayList() );
				this.add(instanceSlotTreeNode);
			}
		}
    }
}
