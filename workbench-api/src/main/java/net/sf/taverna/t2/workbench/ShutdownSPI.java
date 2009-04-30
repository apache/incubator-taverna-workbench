/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench;

/**
 * SPI for components that want to be notified when the workbench is about to be
 * shutdown.
 * <p>
 * Components should implement this if they need to delay the shutdown while
 * they finish a task, e.g. writing out cached data to disk.
 * <p>
 * <b>NB</b> There is no guarantee that the workbench will actually shut down as
 * the user may decide to abort the shutdown.
 * 
 * @author David Withers
 */
public interface ShutdownSPI {

	/**
	 * Called when the workbench is about to shutdown. Implementations should
	 * block until they are ready for the shutdown process to proceed. If the
	 * shutdown for a component will take more than a few seconds a dialog
	 * should inform the user, possibly allowing the user to cancel the shutdown
	 * task for the component and/or the workbench shutdown process.
	 * <p>
	 * When the shutdown process has finished (or the user has canceled it) this
	 * method should return with a vale of <code>true</code>.
	 * <p>
	 * Only return <code>false</code> if the user has chosen to abort the
	 * workbench shutdown process.
	 * 
	 * @return
	 */
	public boolean shutdown();

	/**
	 * Provides a hint for the position in the shutdown sequence that shutdown
	 * should be called. The higher the number the earlier shutdown will be
	 * called.
	 * <p>
	 * Custom plugins are recommended to start with a value > 100.
	 */
	public int positionHint();

}
