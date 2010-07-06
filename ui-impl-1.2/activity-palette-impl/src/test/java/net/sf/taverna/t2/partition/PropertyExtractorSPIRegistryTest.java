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

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class PropertyExtractorSPIRegistryTest {

	@Test
	public void testGetAllPropertiesFor() {
		Map<String,Object> map = PropertyExtractorSPIRegistry.getInstance().getAllPropertiesFor("A String");
		assertEquals("There should be 2 items in the map",2,map.size());
		assertEquals("There should be an elment for james=>brown","brown",map.get("james"));
		assertEquals("There should be an elment for fred=>blogs","blogs",map.get("fred"));
		
		map = PropertyExtractorSPIRegistry.getInstance().getAllPropertiesFor(Integer.valueOf(1));
		assertEquals("There should be 1 items in the map",1,map.size());
		assertEquals("There should be an elment for one=>1","1",map.get("one"));
		
		map = PropertyExtractorSPIRegistry.getInstance().getAllPropertiesFor(Float.valueOf(1f));
		assertEquals("There should be 0 items in the map",0,map.size());
	}

}
