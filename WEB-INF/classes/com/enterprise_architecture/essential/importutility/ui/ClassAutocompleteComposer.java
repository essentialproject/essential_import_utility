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
 * 17.09.2014	JWC Fixed a Git merge failure, changing superclass to EssentialImportInterface from
 * 					EssentialImportUIComposer
 * 
 */
package com.enterprise_architecture.essential.importutility.ui;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.*;

/**
 * This class used to support autocomplete of combo boxes used to select Essential Classes
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 28.06.2011
 *
 */

public class ClassAutocompleteComposer extends EssentialImportInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Combobox classNameTxtBox;
	Combobox slotNameTxtBox;
	
	public void doAfterCompose(Component comp) {
		try {
			super.doAfterCompose(comp);
			classNameTxtBox.setModel(ListModels.toListSubModel(new ListModelList(getClassNames()))); 
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void onChange$classNameTxtBox() {
		this.updateSlotList();
	}
	
	private void updateSlotList() {
		slotNameTxtBox.setModel(ListModels.toListSubModel(new ListModelList(getSlotNames()))); 
	}
	
	
	private List<String> getClassNames() { //return all items
		List<String> classList = this.getEssentialClasses();
		return classList;
	}
	
	
	private List<String> getSlotNames() { //return all items
		String className = classNameTxtBox.getValue();
		if(className.length() > 0) {
			List<String> slotList = this.getSlotsForEssentialClass(className);
			System.out.println("Getting Slot List for: " + className + " size: " + slotList.size());
			return slotList;
		}
		return null;
	}
}
