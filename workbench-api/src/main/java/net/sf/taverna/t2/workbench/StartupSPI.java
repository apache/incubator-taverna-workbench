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
 * SPI for components/plugins that want to be able to perform some configuration on 
 * Workbench startup.
 * 
 * @author Alex Nenadic
 */
public interface StartupSPI {

	/**
	 * Called when the Workbench is starting up for implementations of this
	 * interface to perform any configuration on start up.
	 * <p>
	 * When the configuration process has finished this
	 * method should return <code>true</code>.
	 * <p>
	 * Return <code>false</code> if and only if failure in this method will cause Workbench 
	 * not to function at all.
	 * 
	 */
	public boolean startup();

	/**
	 * Provides a hint for the order in which the startup hooks (that implement this interface)
	 * will be called. Higher the number earlier will the startup hook be
	 * invoked.
	 * <p>
	 * Custom plugins are recommended to start with a value > 100.
	 */
	public int positionHint();

}

