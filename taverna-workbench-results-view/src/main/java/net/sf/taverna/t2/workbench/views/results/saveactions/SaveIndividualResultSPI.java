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
package net.sf.taverna.t2.workbench.views.results.saveactions;

import java.nio.file.Path;

import javax.swing.AbstractAction;

/**
 * Defines an interface for various actions for saving results of a workflow
 * run. Path to a single result data is contained inside a MutableTreeNode,
 * which can be used by actions that only want to save the current result. The
 * interface also contains a list of output ports that can be used to
 * dereference all outputs, for actions wishing to save a all results (e.g. in
 * different formats).
 * 
 * @author Alex Nenadic
 * @author David Withers
 */
public interface SaveIndividualResultSPI {
	/**
	 * Sets the Path pointing to the result to be saved.
	 */
	void setResultReference(Path reference);

	/**
	 * Returns the save result action implementing this interface.
	 */
	AbstractAction getAction();
}
