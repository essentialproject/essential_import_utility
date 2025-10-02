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
 * De-marshall a JSON string into a POJO representing the Essential Meta Model
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EssentialMetaModel 
{
	private Collection<EssentialMetaClass> metaModel = new ArrayList<EssentialMetaClass>();
	
	public Collection<EssentialMetaClass> getMetaModel()
	{
		return metaModel;
	}
	
	public void setMetaModel(Collection<EssentialMetaClass> theMetaModel)
	{
		metaModel.clear();
		Iterator<EssentialMetaClass> aCollectionIt = theMetaModel.iterator();
		while(aCollectionIt.hasNext())
		{
			EssentialMetaClass aMetaClass = aCollectionIt.next();
			metaModel.add(aMetaClass);
		}
	}

}
