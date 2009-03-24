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
import java.util.Map;

import net.sf.taverna.t2.activities.stringconstant.query.StringConstantActivityItem;
import net.sf.taverna.t2.partition.PropertyExtractorSPI;
import net.sf.taverna.t2.partition.PropertyExtractorSPIRegistry;

import org.junit.Test;

public class StringConstantPropertyExtractorTest {

	@Test
	public void testSPI() {
		List<PropertyExtractorSPI> instances = PropertyExtractorSPIRegistry.getInstance().getInstances();
		assertTrue("There should be more than one instance found",instances.size()>0);
		boolean found = false;
		for (PropertyExtractorSPI spi : instances) {
			if (spi instanceof StringConstantPropertyExtractor) {
				found=true;
				break;
			}
		}
		assertTrue("A StringConstantPropertyExtractor should have been found",found);
	}
	
	@Test
	public void testExtractProperties() {
		StringConstantActivityItem item = new StringConstantActivityItem();
		
		Map<String,Object> props = new StringConstantPropertyExtractor().extractProperties(item);

		assertEquals("missing or incorrect property","String Constant",props.get("type"));
	}

}
