/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.workbench.update.impl;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import uk.org.taverna.commons.update.UpdateManager;

/**
 * @author David Withers
 */
@SuppressWarnings({ "serial", "unused" })
public class UpdateManagerView extends JPanel {

	private UpdateManager updateManager;

	public UpdateManagerView(UpdateManager updateManager) {
		this.updateManager = updateManager;
		initialize();
	}

	private void initialize() {
		setLayout(new GridBagLayout());
	}
}
