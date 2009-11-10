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
package net.sf.taverna.t2.activities.dataflow.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.actions.EditNestedDataflowAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.file.importworkflow.actions.ReplaceNestedWorkflowAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

public class DataflowActivityContextualView extends
		HTMLBasedActivityContextualView<Dataflow> {

	private static final long serialVersionUID = -552783425303398911L;

	static Logger logger = Logger
			.getLogger(DataflowActivityContextualView.class);

	private T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private FileManager fileManager = FileManager.getInstance();

	@Override
	public DataflowActivity getActivity() {
		return (DataflowActivity) super.getActivity();
	}

	@Override
	public JComponent getMainFrame() {
		JComponent mainFrame = super.getMainFrame();
		JButton viewWorkflowButton = new JButton("Edit workflow");
		viewWorkflowButton.addActionListener(new EditNestedDataflowAction(getActivity()));
		JButton configureButton = new JButton(new ReplaceNestedWorkflowAction(
						getActivity()));
		configureButton.setIcon(null);
		JPanel flowPanel = new JPanel(new FlowLayout());
		flowPanel.add(viewWorkflowButton);
		flowPanel.add(configureButton);
		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		return mainFrame;
	}

	public DataflowActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {

		return ("<tr><td colspan=2>" + getActivity().getConfiguration().getLocalName() + "</td></tr>");
	}

	@Override
	public String getViewTitle() {
		return "Nested workflow";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return null;
		// return new OpenNestedDataflowFromFileAction(
		// (DataflowActivity) getActivity(), owner);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
