/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.MockupPanel;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * A panel of available services
 * 
 * @author Stian Soiland-Reyes
 *
 */
@SuppressWarnings("serial")
public class ServicePanel extends JPanel implements UIComponentSPI {

	public ServicePanel() {
		initialise();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Service panel";
	}

	public void onDisplay() {
	}

	public void onDispose() {
	}

	protected void initialise() {
		removeAll();
		setLayout(new BorderLayout());
		FilterTreeNode root = new FilterTreeNode("Available services");
		FilterTreeNode templateServices = new FilterTreeNode("Template services");
		root.add(templateServices);
		templateServices.add(new FilterTreeNode("Beanshell"));
		templateServices.add(new FilterTreeNode("Nested workflow"));

		
		root.add(new FilterTreeNode("Local services"));
		root.add(new FilterTreeNode("WSDL services"));

		
		add(new MockupPanel(new FilterTreeModel(root)));
	}


}
