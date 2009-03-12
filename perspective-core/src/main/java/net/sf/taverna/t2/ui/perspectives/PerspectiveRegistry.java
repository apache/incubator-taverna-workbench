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
package net.sf.taverna.t2.ui.perspectives;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

/**
 * SPI registry responsible for finding PerspectiveSPI's which define a UI
 * perspective
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 * 
 */
public class PerspectiveRegistry extends SPIRegistry<PerspectiveSPI> {

	private static PerspectiveRegistry instance = null;

	public static synchronized PerspectiveRegistry getInstance() {
		if (instance == null) {
			instance = new PerspectiveRegistry();
		}
		return instance;
	}

	protected PerspectiveSorter perspectiveSorter = new PerspectiveSorter();

	protected PerspectiveRegistry() {
		super(PerspectiveSPI.class);
	}

	/**
	 * Return a list of the discovered {@link PerspectiveSPI}s, sorted by
	 * increasing {@link PerspectiveSPI#positionHint()}s.
	 * 
	 * @return {@link List} of the discovered {@link PerspectiveSPI}s
	 */
	public List<PerspectiveSPI> getPerspectives() {
		List<PerspectiveSPI> result = getInstances();
		Collections.sort(result, perspectiveSorter);
		return result;
	}

	protected class PerspectiveSorter implements Comparator<PerspectiveSPI> {
		public int compare(PerspectiveSPI o1, PerspectiveSPI o2) {
			return new Integer(o1.positionHint()).compareTo(new Integer(o2
					.positionHint()));
		}
	}
}
