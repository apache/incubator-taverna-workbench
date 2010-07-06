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
package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;

/**
 * Exit the workbench
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class ExitAction extends AbstractMenuAction {

	private static final String MAC_OS_X = "Mac OS X";

	public ExitAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#file"),
				10000);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent e) {
				Workbench.getInstance().exit();
			}
		};
	}

	@Override
	public boolean isEnabled() {
		return !MAC_OS_X.equalsIgnoreCase(System.getProperty("os.name"));
	}

}
