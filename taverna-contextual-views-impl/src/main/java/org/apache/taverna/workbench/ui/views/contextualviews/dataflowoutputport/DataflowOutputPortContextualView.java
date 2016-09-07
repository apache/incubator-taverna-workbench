/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.ui.views.contextualviews.dataflowoutputport;

import static java.awt.FlowLayout.LEFT;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;

/**
 * Contextual view for dataflow's output ports.
 *
 * @author Alex Nenadic
 */
public class DataflowOutputPortContextualView extends ContextualView {
	private static final long serialVersionUID = 5496014085110553051L;

	private OutputWorkflowPort dataflowOutputPort;
	private JPanel dataflowOutputPortView;
	@SuppressWarnings("unused")
	private FileManager fileManager;

	public DataflowOutputPortContextualView(OutputWorkflowPort outputport,
			FileManager fileManager) {
		this.dataflowOutputPort = outputport;
		this.fileManager = fileManager;
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
		dataflowOutputPortView = new JPanel(new FlowLayout(LEFT));
		dataflowOutputPortView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel label = new JLabel(getTextForLabel());
		dataflowOutputPortView.add(label);
	}

	private String getTextForLabel() {
		//FIXME
		//return getTextFromDepth("port", dataflowOutputPort.getDepth());
		return "Fix depth for OutputWorkflowPort";
	}

	private void updatePrediction() {
		//FIXME
		// fileManager.getCurrentDataflow().checkValidity();
	}

	@Override
	@SuppressWarnings("serial")
	public Action getConfigureAction(Frame owner) {
		return new AbstractAction("Update prediction") {
			@Override
			public void actionPerformed(ActionEvent e) {
				updatePrediction();
				refreshView();
			}
		};
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
