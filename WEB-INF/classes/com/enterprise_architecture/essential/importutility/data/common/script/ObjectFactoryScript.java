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
package com.enterprise_architecture.essential.importutility.data.common.script;

import com.enterprise_architecture.essential.importutility.data.common.*;

/**
 * This class extends the common ObjectFactory class to generate instances of classes that provide 
 * the behaviours required to generate the Essential Import Script
 * 
 * @author Joson Powell <jason.powell@e-asolutions.com>
 * @version 1.0 - 29.06.2011
 *
 */
public class ObjectFactoryScript extends ObjectFactory {
	

	@Override
	  public DerivedInstanceType createDerivedInstanceType() {
	    return new DerivedInstanceTypeScript();
	  }
	
	
	public GlobalInstanceScript createGlobalInstanceScript() {
	    return new GlobalInstanceScript();
	  }
	
	
	@Override
	  public DerivedSimpleSlot createDerivedSimpleSlot() {
	    return new DerivedSimpleSlotScript();
	  }
	
	
	@Override
	  public DerivedValue createDerivedValue() {
	    return new DerivedValueScript();
	  }
	
	
	@Override
	  public DerivedValueRef createDerivedValueRef() {
	    return new DerivedValueRefScript();
	  }
	
	
	@Override
	  public DerivedValueString createDerivedValueString() {
	    return new DerivedValueStringScript();
	  }
	
	
	@Override
	  public InstanceSlot createInstanceSlot() {
	    return new InstanceSlotScript();
	  }
	
	
	@Override
	  public PrimitiveSlot createPrimitiveSlot() {
	    return new PrimitiveSlotScript();
	  }
	
	
	@Override
	  public SimpleInstanceType createSimpleInstanceType() {
	    return new SimpleInstanceTypeScript();
	  }
	
	
	@Override
	  public SimpleSlot createSimpleSlot() {
	    return new SimpleSlotScript();
	  }
}
