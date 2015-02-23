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
import java.util.UUID;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManagerImpl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import uk.org.taverna.configuration.impl.ConfigurationManagerImpl;

public class ConfigurationManagerTest {

	ConfigurationManagerImpl configurationManager;

	@Before
	public void setup() {
		configurationManager = new ConfigurationManagerImpl(new ApplicationConfigurationImpl());
	}

	@Test
	public void createConfigManager() {
		assertNotNull("Config Manager should not be null", configurationManager);
	}

	@Ignore("Hardcoded /Users/Ian") //FIXME: update test to work using File.createTempFile(...)
	@Test
	public void populateConfigOfColourmanager() {
		ColourManager manager= new ColourManagerImpl(null);

		manager.setProperty("colour.first", "25");
		manager.setProperty("colour.second", "223");

		configurationManager.setBaseConfigLocation(new File("/Users/Ian/scratch"));
		try {
			configurationManager.store(manager);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		ColourManager manager2 = new ColourManagerImpl(configurationManager);

		try {
			configurationManager.populate(manager2);
		} catch (Exception e) {
			e.printStackTrace();
		}


		assertEquals("Properties do not match", manager2.getProperty("colour.first"), manager.getProperty("colour.first"));
		assertEquals("Properties do not match", manager2.getProperty("colour.second"), manager.getProperty("colour.second"));


	}

	@Test
	public void saveColoursForDummyColourable() {
		String dummy = "";
		ColourManager manager=new ColourManagerImpl(configurationManager);
		manager.setProperty(dummy.getClass().getCanonicalName(), "#000000");

		File f = new File(System.getProperty("java.io.tmpdir"));
		File d = new File(f, UUID.randomUUID().toString());
		d.mkdir();
		configurationManager.setBaseConfigLocation(d);
		try {
			configurationManager.store(manager);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			configurationManager.populate(manager);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
