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

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PartitionAlgorithmSPIRegistryTest {
	private static PartitionAlgorithmSetSPIRegistry registry;
	
	@BeforeClass
	public static void setUp() {
		registry = PartitionAlgorithmSetSPIRegistry.getInstance();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetInstance() {
		List<PartitionAlgorithmSetSPI> list = registry.getInstances();
		assertTrue("There should be at least 1 item in the list",list.size()>0);
		boolean found = false;
		for (PartitionAlgorithmSetSPI item : list) {
			if (item instanceof DummyPartitionAlgorithmSet) {
				found=true;
				break;
			}
		}
		assertTrue("There should have been a DummyPartitionAlgorithmSet",found);
	}
}
