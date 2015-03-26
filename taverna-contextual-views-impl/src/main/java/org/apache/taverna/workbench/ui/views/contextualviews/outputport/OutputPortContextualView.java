/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.views.contextualviews.outputport;

import static java.awt.FlowLayout.LEFT;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;

/**
 * Contextual view for dataflow procerssor's output ports.
 * 
 * @author Alex Nenadic
 */
public class OutputPortContextualView extends ContextualView {
	private static final String NO_DETAILS_AVAILABLE_HTML = "<html><body>"
			+ "<i>No details available.</i>" + "</body><html>";
	private static final long serialVersionUID = -7743029534480678624L;

	private ActivityOutputPort outputPort;
	private JPanel outputPortView;

	public OutputPortContextualView(ActivityOutputPort outputport) {
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
		outputPortView = new JPanel(new FlowLayout(LEFT));
		outputPortView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel label = new JLabel(NO_DETAILS_AVAILABLE_HTML);
		outputPortView.add(label);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
