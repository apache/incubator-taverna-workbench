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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.dataflowoutputport;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.impl.DataflowOutputPortImpl;

/**
 * Contextual view for dataflow's output ports.
 * 
 * @author Alex Nenadic
 *
 */
public class DataflowOutputPortContextualView extends ContextualView{

	private static final long serialVersionUID = 5496014085110553051L;
	private DataflowOutputPortImpl dataflowOutputPort;
	private JPanel dataflowOutputPortView;

	public DataflowOutputPortContextualView(DataflowOutputPortImpl outputport) {
		this.dataflowOutputPort = outputport;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return dataflowOutputPortView;
	}

	@Override
	public String getViewTitle() {
		return "Workflow output port: " + dataflowOutputPort.getName();
	}

	@Override
	public void refreshView() {
		dataflowOutputPortView = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dataflowOutputPortView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel label = new JLabel("<html><body><i>No details provided.</i></body><html>");
		dataflowOutputPortView.add(label);
	}
}
