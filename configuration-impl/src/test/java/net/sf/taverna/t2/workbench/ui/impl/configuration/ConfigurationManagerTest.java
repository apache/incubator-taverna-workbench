/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.impl.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;

import org.junit.Ignore;
import org.junit.Test;

public class ConfigurationManagerTest {
	
	@Test
	public void createConfigManager() {
		ConfigurationManager instance = ConfigurationManager.getInstance();
		assertNotNull("Config Manager should not be null", instance);
	}
	
	@Ignore("Hardcoded /Users/Ian") //FIXME: update test to work using File.createTempFile(...)
	@Test
	public void populateConfigOfColourmanager() {
		ColourManager manager=ColourManager.getInstance();
		
		manager.setProperty("colour.first", "25");
		manager.setProperty("colour.second", "223");
		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		instance.setBaseConfigLocation(new File("/Users/Ian/scratch"));
		try {
			instance.store(manager);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		ColourManager manager2 = ColourManager.getInstance();
		
		try {
			instance.populate(manager2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		assertEquals("Properties do not match", manager2.getProperty("colour.first"), manager.getProperty("colour.first"));
		assertEquals("Properties do not match", manager2.getProperty("colour.second"), manager.getProperty("colour.second"));
		
		
	}
	
	@Test
	public void saveColoursForDummyColourable() {
		String dummy = "";
		ColourManager manager=ColourManager.getInstance();
		manager.setProperty(dummy.getClass().getCanonicalName(), "#000000");
		
		ConfigurationManager instance = ConfigurationManager.getInstance();
		instance.setBaseConfigLocation(new File(System.getProperty("java.io.tmpdir")+File.separatorChar+"scratch"));
		try {
			instance.store(manager);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			instance.populate(manager);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
