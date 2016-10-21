/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.workbench.ui.impl.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.UUID;
import org.apache.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import org.apache.taverna.configuration.impl.ConfigurationManagerImpl;

import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.ui.impl.configuration.colour.ColourManagerImpl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

//import uk.org.taverna.configuration.app.impl.ApplicationConfigurationImpl;
//import uk.org.taverna.configuration.impl.ConfigurationManagerImpl;

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
