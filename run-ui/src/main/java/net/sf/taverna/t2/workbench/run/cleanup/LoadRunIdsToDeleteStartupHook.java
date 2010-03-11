/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.workbench.run.cleanup;

import net.sf.taverna.t2.workbench.StartupSPI;

/**
 * Load previously saved workflow ids that were scheduled to be deleted before
 * previous Taverna shutdown, and initiate deletion of them now.
 * 
 * @see StoreRunIdsToDeleteLaterShutdownHook
 * @see DatabaseCleanup
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class LoadRunIdsToDeleteStartupHook implements StartupSPI {

	public int positionHint() {
		return 1000;
	}

	public boolean startup() {
		DatabaseCleanup.getInstance().load();
		// TODO: Start deletion thread
		return true;
	}

}
