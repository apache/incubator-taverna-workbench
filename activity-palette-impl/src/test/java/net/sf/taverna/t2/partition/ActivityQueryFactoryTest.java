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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

import org.junit.BeforeClass;
import org.junit.Test;

public class ActivityQueryFactoryTest {
	
	@BeforeClass
	public static void setup() {
		ConfigurationManager.getInstance().setBaseConfigLocation(new File(System.getProperty("java.io.tmpdir")));
	}
	
	@Test
	public void testGetInstances() {
		List<QueryFactory> list = QueryFactoryRegistry.getInstance().getInstances();
		assertTrue("There should be at least 2 instance found",list.size()>1);
		boolean found = false;
		for (QueryFactory q : list) {
			if (q instanceof DummyActivityQueryFactory) {
				found = true;
				DummyActivityQueryFactory aq = (DummyActivityQueryFactory)q;
				assertNotNull("The query should be configured",aq.getConfigurable());
				break;
			}
		}
		assertTrue("The DummyActivityQueryFactory was not found",found);
	}
	
	@Test
	public void testGetQueries() {
		List<Query<?>> list = QueryFactoryRegistry.getInstance().getQueries();
		assertTrue("There should be at least 2 queries found",list.size()>=2);
		boolean found = false;
		for (Query<?> q : list) {
			if (q instanceof DummyActivityQuery) {
				found = true;
				ActivityQuery aq = (ActivityQuery)q;
				assertNotNull("The query should have its property set",aq.getProperty());
				assertEquals("The query should have its property set","fred",aq.getProperty());
				break;
			}
		}
		assertTrue("The DummyActivityQuery was not found",found);
		
		found = false;
		for (Query<?> q : list) {
			if (q instanceof DummyQuery) {
				found = true;
				break;
			}
		}
		assertTrue("The DummyActivityQuery was not found",found);
	}
}
