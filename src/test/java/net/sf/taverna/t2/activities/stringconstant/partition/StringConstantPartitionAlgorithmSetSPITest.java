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
package net.sf.taverna.t2.activities.stringconstant.partition;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPI;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPIRegistry;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

import org.junit.Before;
import org.junit.Test;

public class StringConstantPartitionAlgorithmSetSPITest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSPI() {
		List<PartitionAlgorithmSetSPI> list = PartitionAlgorithmSetSPIRegistry.getInstance().getInstances();
		int c=0;
		for (PartitionAlgorithmSetSPI p : list) {
			if (p instanceof StringConstantPartitionAlgorithmSetSPI) {
				c++;
			}
		}
		assertEquals("There should be 1 SoaplabPartitionAlgorithmSetSPI discovered",1,c);
	}
	
	@Test
	public void getPartitonAlgorithms() {
		PartitionAlgorithmSetSPI p = new StringConstantPartitionAlgorithmSetSPI();
		Set<PartitionAlgorithm<?>> set = p.getPartitionAlgorithms();
		assertEquals("There should be 1 partition algorithm in the set",1,set.size());
		assertTrue("should contain an algorithm for 'type'",set.contains(new LiteralValuePartitionAlgorithm("type")));
		
	}
}
