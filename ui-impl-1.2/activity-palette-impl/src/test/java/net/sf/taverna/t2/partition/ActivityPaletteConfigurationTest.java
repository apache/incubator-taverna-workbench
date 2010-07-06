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
package net.sf.taverna.t2.partition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteConfiguration;

import org.junit.Before;
import org.junit.Test;

public class ActivityPaletteConfigurationTest {

	private ActivityPaletteConfiguration conf;
	@Before
	public void setup() {
		ConfigurationManager manager = ConfigurationManager.getInstance();
		File f = new File(System.getProperty("java.io.tmpdir"));
		File d = new File(f,UUID.randomUUID().toString());
		d.mkdir();
		manager.setBaseConfigLocation(d);
		conf=ActivityPaletteConfiguration.getInstance();
		conf.restoreDefaults();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEmptyList() throws Exception {
		conf.setPropertyStringList("list", new ArrayList<String>());
		assertTrue("Result was not a list but was:"+conf.getProperty("list"),conf.getPropertyStringList("list") instanceof List);
		ConfigurationManager.getInstance().store(conf);
		ConfigurationManager.getInstance().populate(conf);
		assertTrue("Result was not a list but was:"+conf.getPropertyStringList("list"),conf.getPropertyStringList("list") instanceof List);
		List<String> list = conf.getPropertyStringList("list");
		assertEquals("There should be 0 elements",0,list.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSingleItem() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("fred");
		conf.setPropertyStringList("single", list);
		ConfigurationManager.getInstance().store(conf);
		ConfigurationManager.getInstance().populate(conf);
		
		assertTrue("should be an ArrayList",conf.getPropertyStringList("single") instanceof List);
		List<String> l = conf.getPropertyStringList("single");
		assertEquals("There should be 1 element",1,l.size());
		assertEquals("Its value should be fred","fred",l.get(0));
	}
	
	@Test
	public void testList() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("fred");
		list.add("bloggs");
		conf.setPropertyStringList("list", list);
		ConfigurationManager.getInstance().store(conf);
		ConfigurationManager.getInstance().populate(conf);
		
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
