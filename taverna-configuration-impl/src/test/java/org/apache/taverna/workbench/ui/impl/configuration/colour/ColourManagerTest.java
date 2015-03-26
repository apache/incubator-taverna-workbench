/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.impl.configuration.colour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.util.UUID;

import org.apache.taverna.workbench.configuration.colour.ColourManager;

import org.junit.Before;
import org.junit.Test;

import uk.org.taverna.configuration.Configurable;
import uk.org.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import uk.org.taverna.configuration.impl.ConfigurationManagerImpl;

public class ColourManagerTest {

	private ConfigurationManagerImpl configurationManager;

	@Before
	public void setup() {
		configurationManager = new ConfigurationManagerImpl(new ApplicationConfigurationImpl());

		File f = new File(System.getProperty("java.io.tmpdir"));
		File d = new File(f, UUID.randomUUID().toString());
		d.mkdir();
		configurationManager.setBaseConfigLocation(d);
	}

	@Test
	public void testGetPreferredColourEqualsWhite() throws Exception {
		String dummy = new String();

		Color c = new ColourManagerImpl(configurationManager).getPreferredColour(dummy);
		assertEquals("The default colour should be WHITE", Color.WHITE, c);
	}

	@Test
	public void testConfigurableness() throws Exception {
		ColourManager manager = new ColourManagerImpl(configurationManager);
		assertTrue(manager instanceof Configurable);

		assertEquals("wrong category", "colour", manager.getCategory());
		assertEquals("wrong name", "Colour Management", manager.getDisplayName());
		assertEquals("wrong UUID", "a2148420-5967-11dd-ae16-0800200c9a66",
				manager.getUUID());
		assertNotNull("there is no default property map", manager
				.getDefaultPropertyMap());
	}

	@Test
	public void saveAsWrongArrayType() throws Exception {
		String dummy = "";
		ColourManager manager = new ColourManagerImpl(configurationManager);
		manager.setProperty(dummy.getClass().getCanonicalName(), "#ffffff");

		File baseLoc = File.createTempFile("test", "scratch");
		baseLoc.delete();
		assertTrue("Could not make directory " + baseLoc, baseLoc.mkdir());
		configurationManager.setBaseConfigLocation(baseLoc);
		configurationManager.store(manager);
		configurationManager.populate(manager);
		manager.getPreferredColour(dummy);
	}

}
