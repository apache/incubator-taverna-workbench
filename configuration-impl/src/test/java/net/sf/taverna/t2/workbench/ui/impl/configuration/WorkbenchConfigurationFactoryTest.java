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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.UUID;

import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIRegistry;

import org.junit.Before;
import org.junit.Test;


public class WorkbenchConfigurationFactoryTest {
	
	@Before
	public void setup() throws Exception {
		ConfigurationManager manager = ConfigurationManager.getInstance();
		File f = new File(System.getProperty("java.io.tmpdir"));
		File configTestsDir = new File(f,"configTests");
		if (!configTestsDir.exists()) configTestsDir.mkdir();
		File d = new File(configTestsDir,UUID.randomUUID().toString());
		d.mkdir();
		manager.setBaseConfigLocation(d);
	}
	
	@Test
	public void testFoundByRegistry() {
		
		List<ConfigurationUIFactory> list = ConfigurationUIRegistry.getInstance().getConfigurationUIFactories();
		assertTrue("There should be at least 1 item in the list",list.size()>=1);
		
		boolean found=false;
		for (ConfigurationUIFactory f : list) {
			if (f instanceof WorkbenchConfigurationUIFactory) {
				found=true;
				break;
			}
		}
		assertTrue("The WorkbenchConfigurationUIFactory was not found",found);
	} 
}
