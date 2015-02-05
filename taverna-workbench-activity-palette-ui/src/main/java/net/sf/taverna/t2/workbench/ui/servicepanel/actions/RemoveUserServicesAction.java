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
package net.sf.taverna.t2.workbench.ui.servicepanel.actions;

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;

@SuppressWarnings("serial")
public class RemoveUserServicesAction extends AbstractAction {
	private static final String CONFIRM_MESSAGE = "You are about to remove all services you have added. <br>"
			+ "Are you <b>really</b> sure you want to do this?";
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public RemoveUserServicesAction(
			ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super("Remove all user added service providers");
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int option = showConfirmDialog(null, new JLabel("<html><body>"
				+ CONFIRM_MESSAGE + "</body></html>"),
				"Confirm service deletion", YES_NO_OPTION);

		if (option == YES_OPTION)
			for (ServiceDescriptionProvider provider : serviceDescriptionRegistry
					.getUserAddedServiceProviders())
				serviceDescriptionRegistry
						.removeServiceDescriptionProvider(provider);
	}
}
