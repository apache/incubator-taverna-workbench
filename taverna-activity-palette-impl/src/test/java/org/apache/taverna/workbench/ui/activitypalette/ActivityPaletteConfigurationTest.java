package org.apache.taverna.workbench.ui.activitypalette;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.taverna.configuration.AbstractConfigurable;
import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationManager;
import org.apache.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import org.apache.taverna.configuration.impl.ConfigurationManagerImpl;

import org.junit.Before;
import org.junit.Test;

public class ActivityPaletteConfigurationTest {

	private ActivityPaletteConfiguration conf;
	private ConfigurationManagerImpl manager;

	@Before
	public void setup() {
		File f = new File(System.getProperty("java.io.tmpdir"));
		final File d = new File(f,UUID.randomUUID().toString());
		d.mkdir();
		manager = new ConfigurationManagerImpl(new ApplicationConfigurationImpl() {
			@Override
			public Path  getApplicationHomeDir() {
				return d.toPath();
			}
		});
		conf=new ActivityPaletteConfiguration(manager);
		conf.restoreDefaults();
	}

	@Test
	public void testEmptyList() throws Exception {
		conf.setPropertyStringList("list", new ArrayList<String>());
		assertTrue("Result was not a list but was:"+conf.getProperty("list"),conf.getPropertyStringList("list") instanceof List);
		assertTrue("Result was not a list but was:"+conf.getPropertyStringList("list"),conf.getPropertyStringList("list") instanceof List);
		List<String> list = conf.getPropertyStringList("list");
		assertEquals("There should be 0 elements",0,list.size());
	}

	@Test
	public void testSingleItem() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("fred");
		conf.setPropertyStringList("single", list);

		assertTrue("should be an ArrayList",conf.getPropertyStringList("single") instanceof List);
		List<String> l = conf.getPropertyStringList("single");
		assertEquals("There should be 1 element",1,l.size());
		assertEquals("Its value should be fred","fred",l.get(0));
	}

	@Test
	public void testList() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("fred");
		list.add("bloggs");
		conf.setPropertyStringList("list", list);

		assertTrue("should be an ArrayList",conf.getPropertyStringList("list") instanceof List);
		List<String> l = conf.getPropertyStringList("list");
		assertEquals("There should be 1 element",2,l.size());
		assertEquals("Its value should be fred","fred",l.get(0));
		assertEquals("Its value should be bloggs","bloggs",l.get(1));
	}

	@Test
	public void testNull() throws Exception {
		assertNull("Should return null",conf.getProperty("blah blah blah"));
	}

}
