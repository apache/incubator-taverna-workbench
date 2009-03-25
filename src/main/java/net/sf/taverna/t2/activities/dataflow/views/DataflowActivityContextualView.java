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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.filemanager.NestedDataflowSource;
import net.sf.taverna.t2.dataflow.actions.DataflowActivityConfigurationAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class DataflowActivityContextualView extends
		HTMLBasedActivityContextualView<Dataflow> {

	private static final long serialVersionUID = -552783425303398911L;

	private static Logger logger = Logger
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
		viewWorkflowButton.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = -2561346588592416137L;

			public void actionPerformed(ActionEvent e) {
				NestedDataflowSource nestedDataflowSource = new NestedDataflowSource(
						fileManager.getCurrentDataflow(), getActivity());

				Dataflow alreadyOpen = fileManager.getDataflowBySource(nestedDataflowSource);
				if (alreadyOpen != null) {
					// The nested workflow is already opened - switch to it
					fileManager.setCurrentDataflow(alreadyOpen);
					return;
				}
				 
				try {
					fileManager.openDataflow(T2_FLOW_FILE_TYPE,
							nestedDataflowSource);
				} catch (OpenException e1) {
					logger.error(
							"Could not open nested dataflow from activity "
									+ getActivity(), e1);
					JOptionPane.showMessageDialog(
							DataflowActivityContextualView.this,
							"Could not open nested dataflow:\n"
									+ e1.getMessage(),
							"Could not open nested dataflow",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

		});
		JButton configureButton = new JButton("Open from file");
		configureButton
				.addActionListener(new DataflowActivityConfigurationAction(
						getActivity()));

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
		// return new DataflowActivityConfigurationAction(
		// (DataflowActivity) getActivity(), owner);
	}

}
