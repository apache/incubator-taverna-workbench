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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.dataflowinputport;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.impl.DataflowInputPortImpl;

/**
 * Contextual view for dataflow's input ports.
 * 
 * @author Alex Nenadic
 *
 */
public class DataflowInputPortContextualView extends ContextualView{
	
	private static final long serialVersionUID = -8746856072335775933L;
	private DataflowInputPortImpl dataflowInputPort;
	private JPanel dataflowInputPortView;
	
	public DataflowInputPortContextualView(DataflowInputPortImpl inputport) {
		this.dataflowInputPort = inputport;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return dataflowInputPortView;
	}

	@Override
	public String getViewTitle() {
		return "Workflow input port: " + dataflowInputPort.getName();
	}

	@Override
	public void refreshView() {
		dataflowInputPortView = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dataflowInputPortView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel label = new JLabel (getTextFromDepth("port", dataflowInputPort.getDepth()));
		dataflowInputPortView.add(label);
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new AbstractAction("Update prediction") {

			public void actionPerformed(ActionEvent e) {
				FileManager.getInstance().getCurrentDataflow().checkValidity();
				refreshView();
			}};
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
