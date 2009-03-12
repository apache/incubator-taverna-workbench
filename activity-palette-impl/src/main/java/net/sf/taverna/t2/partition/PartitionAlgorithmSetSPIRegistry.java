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

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * An SPI Registry that discovered PartitionAlgorithmSets, which are used to define the partitioning
 * within the Activity Palette.
 * <p>
 * The registry is a singleton factory class and should be accessed through the getInstance method.
 * </p>
 * <p>
 * The PartitionAlgorithmSetSPI classes to be discovered need to be defined in a resource named
 * <pre>
 * META-INF/services/net.sf.taverna.t2.partition.PartitionAlgorithmSetSPI
 * </pre>
 * </p>
 * 
 * @author Stuart Owen
 * @see PartitionAlgorithm
 * @see PartitionAlgorithmSetSPI
 */
@SuppressWarnings("unchecked")
public class PartitionAlgorithmSetSPIRegistry extends SPIRegistry<PartitionAlgorithmSetSPI> {

	private static PartitionAlgorithmSetSPIRegistry instance = new PartitionAlgorithmSetSPIRegistry();


	public static PartitionAlgorithmSetSPIRegistry getInstance() {
		return instance;
	}
	
	private PartitionAlgorithmSetSPIRegistry() {
		super(PartitionAlgorithmSetSPI.class);
	}
	
}
