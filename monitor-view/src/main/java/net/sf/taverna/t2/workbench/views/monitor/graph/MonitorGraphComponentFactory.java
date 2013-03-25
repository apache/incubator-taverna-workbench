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
package net.sf.taverna.t2.workbench.views.monitor.graph;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class MonitorGraphComponentFactory implements UIComponentFactorySPI {

	private EditManager editManager;
	private MenuManager menuManager;
	private SelectionManager selectionManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;

	public UIComponentSPI getComponent() {
		return new MonitorGraphComponent(editManager, menuManager, selectionManager, colourManager, workbenchConfiguration);
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Monitor View";
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

}
