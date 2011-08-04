/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui;

import java.util.List;

import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

public interface WorkbenchPerspectives {

	/**
	 * Ensures that the current perspective is an instance of
	 * WorkflowPerspective. If the current perspective is not a
	 * WorkflowPerspective, the first such instance from the PerspectiveSPI
	 * registry will be selected, normally the Design perspective.
	 * <p>
	 * This method can be used by UI operations that change or modify the
	 * current workflow, so that the user is shown the new or modified workflow,
	 * and not stuck in say the Result perspective.
	 */
	public void setWorkflowPerspective();

	public void switchPerspective(PerspectiveSPI perspective);

	public List<PerspectiveSPI> getPerspectives();

	public void setPerspectives(List<PerspectiveSPI> perspectives);

	public void refreshPerspectives();

}