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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;

@SuppressWarnings("serial")
public class RemoveUserServicesAction extends AbstractAction {

	public RemoveUserServicesAction() {
		super("Remove all user added service providers");
	}

	public void actionPerformed(ActionEvent e) {
		
		int option = JOptionPane
				.showConfirmDialog(
						null,
						new JLabel("<html><body>You are about to remove all services you have added. <br>Are you REALLY sure you want to do this?</body></html>"),
						"Confirm service deletion", JOptionPane.YES_NO_CANCEL_OPTION);
		
		if (option==JOptionPane.YES_OPTION){
			ServiceDescriptionRegistry serviceDescRegistry = ServiceDescriptionRegistryImpl.getInstance();
			for (ServiceDescriptionProvider provider : serviceDescRegistry
					.getUserAddedServiceProviders()) {
				serviceDescRegistry.removeServiceDescriptionProvider(provider);
			}
		}
	}
}

