/**
 * Copyright (c)2019 Enterprise Architecture Solutions ltd  
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
 * @author Jonathan Carter
 * 
 * 12.11.2019	JWC	First coding
 */
package com.enterprise_architecture.essential.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * De-marshall a JSON string into a POJO representing a Slot of an Essential Meta Class
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EssentialMetaClassSlot 
{
	private String name = "";
	private String type = "";
	private Collection<String> allowedClasses = new ArrayList<String>();
	
	/**
	 * @return the name of the slot
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the type of the slot
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @return the allowedClasses of the slot or an empty list
	 */
	public Collection<String> getAllowedClasses() {
		return allowedClasses;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @param theAllowedClasses the allowedClasses to set
	 */
	public void setAllowedClasses(Collection<String> theAllowedClasses) 
	{
		allowedClasses.clear();
		//Iterator<EssentialMetaSlotAllowedClass> aCollectionIt = theAllowedClasses.iterator();
		Iterator<String> aCollectionIt = theAllowedClasses.iterator();
		while(aCollectionIt.hasNext())
		{
			//EssentialMetaSlotAllowedClass anAllowedClass = aCollectionIt.next();
			String anAllowedClass = aCollectionIt.next();
			allowedClasses.add(anAllowedClass);
		}		
	}
	
}
