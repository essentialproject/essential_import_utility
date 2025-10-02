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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * De-marshall a JSON string into a POJO representing an Essential Meta Class
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"metaClass", "abstract", "slots"})
public class EssentialMetaClass
{
	private String metaClass = "";
	private boolean isAbstract = false;
	private Collection<EssentialMetaClassSlot> slots = new ArrayList<EssentialMetaClassSlot>();
	
	/**
	 * Get the slots for the class
	 * @return
	 */
	public Collection<EssentialMetaClassSlot> getSlots()
	{
		return slots;
	}
	
	/**
	 * Get the name of the class
	 * @return
	 */
	public String getMetaClass()
	{
		return metaClass;
	}

	/**
	 * @return the isAbstract
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/**
	 * @param slots the slots to set
	 */
	public void setSlots(Collection<EssentialMetaClassSlot> theSlots) 
	{
		slots.clear();
		Iterator<EssentialMetaClassSlot> aCollectionIt = theSlots.iterator();
		while(aCollectionIt.hasNext())
		{
			EssentialMetaClassSlot aSlot = aCollectionIt.next();
			slots.add(aSlot);
		}
	}

	/**
	 * @param metaClass the metaClass to set
	 */
	public void setMetaClass(String metaClass) {
		this.metaClass = metaClass;
	}

	/**
	 * @param isAbstract the isAbstract to set
	 */
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}
		
	
}
