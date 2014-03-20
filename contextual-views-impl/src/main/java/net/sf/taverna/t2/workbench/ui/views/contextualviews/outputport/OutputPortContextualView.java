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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.outputport;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityOutputPortImpl;

/**
 * Contextual view for dataflow procerssor's output ports.
 * 
 * @author Alex Nenadic
 *
 */
public class OutputPortContextualView extends ContextualView{

	private static final long serialVersionUID = -7743029534480678624L;
	
	private ActivityOutputPortImpl outputPort;
	private JPanel outputPortView;

	public OutputPortContextualView(ActivityOutputPortImpl outputport) {
		this.outputPort = outputport;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return outputPortView;
	}

	@Override
	public String getViewTitle() {
		return "Service output port: " + outputPort.getName();
	}

	@Override
	public void refreshView() {
		
		outputPortView = new JPanel(new FlowLayout(FlowLayout.LEFT));
		outputPortView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel label = new JLabel("<html><body><i>No details available.</i></body><html>");
		outputPortView.add(label);

	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
