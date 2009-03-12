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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.merge;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;

/**
 * Contextual view for dataflow's merges.
 * 
 * @author Alex Nenadic
 *
 */
public class MergeContextualView extends ContextualView{

	
	private static final long serialVersionUID = -8726212237088362797L;
	private Merge merge;
	private JPanel mergeView;

	
	public MergeContextualView(Merge merge) {
		this.merge = merge;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return mergeView;
	}

	@Override
	public String getViewTitle() {
		return "Data link";
	}

	@Override
	public void refreshView() {
		
		mergeView = new JPanel();
		mergeView.setLayout(new BoxLayout(mergeView, BoxLayout.PAGE_AXIS));
		mergeView.setBorder(new EmptyBorder(5, 5, 5, 5));
		mergeView.add(new JLabel("Merge: " + merge.getLocalName()));
		mergeView.add(Box.createRigidArea(new Dimension(0,5)));
		mergeView.add(new JLabel("Inputs: "));
		for (MergeInputPort mergeInputPort : merge.getInputPorts()) {
			mergeView.add(new JLabel(mergeInputPort.getName()));
		}
		mergeView.add(Box.createRigidArea(new Dimension(0,5)));
		mergeView.add(new JLabel("Outputs: "));
		mergeView.add(new JLabel(merge.getOutputPort().getName()));
	}
}
