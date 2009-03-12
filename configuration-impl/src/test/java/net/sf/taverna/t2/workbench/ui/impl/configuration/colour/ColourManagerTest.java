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
package net.sf.taverna.t2.workbench.ui.impl.configuration.colour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.util.UUID;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

import org.junit.Before;
import org.junit.Test;

public class ColourManagerTest {

	@Before
	public void setup() {
		ConfigurationManager manager = ConfigurationManager.getInstance();
		File f = new File(System.getProperty("java.io.tmpdir"));
		File d = new File(f, UUID.randomUUID().toString());
		d.mkdir();
		manager.setBaseConfigLocation(d);
	}

	@Test
	public void testGetInstance() throws Exception {
		ColourManager manager = ColourManager.getInstance();
		assertNotNull(manager);
		ColourManager manager2 = ColourManager.getInstance();
		assertSame("They should be the same instance", manager, manager2);
	}

	@Test
	public void testGetPreferredColourEqualsWhite() throws Exception {
		String dummy = new String();

		Color c = ColourManager.getInstance().getPreferredColour(dummy);
		assertEquals("The default colour should be WHITE", Color.WHITE, c);
	}

	@Test
	public void testConfigurableness() throws Exception {
		ColourManager manager = ColourManager.getInstance();
		assertTrue(manager instanceof Configurable);

		assertEquals("wrong category", "colour", manager.getCategory());
		assertEquals("wrong name", "Colour Management", manager.getName());
		assertEquals("wrong UUID", "a2148420-5967-11dd-ae16-0800200c9a66",
				manager.getUUID());
		assertNotNull("there is no default property map", manager
				.getDefaultPropertyMap());
	}

	@Test
	public void saveAsWrongArrayType() throws Exception {
		String dummy = "";
		ColourManager manager = ColourManager.getInstance();
		manager.setProperty(dummy.getClass().getCanonicalName(), "#ffffff");

		ConfigurationManager instance = ConfigurationManager.getInstance();
		File baseLoc = File.createTempFile("test", "scratch");
		baseLoc.delete();
		assertTrue("Could not make directory " + baseLoc, baseLoc.mkdir());
		instance.setBaseConfigLocation(baseLoc);
		instance.store(manager);
		instance.populate(manager);
		manager.getPreferredColour(dummy);
	}

}
