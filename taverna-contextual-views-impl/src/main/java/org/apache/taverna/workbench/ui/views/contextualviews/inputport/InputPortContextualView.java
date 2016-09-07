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

package org.apache.taverna.workbench.ui.views.contextualviews.inputport;

import static java.awt.FlowLayout.LEFT;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;

/**
 * Contextual view for dataflow procerssor's input ports.
 * 
 * @author Alex Nenadic
 */
class InputPortContextualView extends ContextualView {
	private static final String NO_DETAILS_AVAILABLE_HTML = "<html><body>"
			+ "<i>No details available.</i>" + "</body><html>";
	private static final long serialVersionUID = -7743029534480678624L;

	private InputActivityPort inputPort;
	private JPanel inputPortView;

	public InputPortContextualView(InputActivityPort inputport) {
		this.inputPort = inputport;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return inputPortView;
	}

	@Override
	public String getViewTitle() {
		return "Service input port: " + inputPort.getName();
	}

	@Override
	public void refreshView() {
		inputPortView = new JPanel(new FlowLayout(LEFT));
		inputPortView.setBorder(new EmptyBorder(5, 5, 5, 5));
		JLabel label = new JLabel(NO_DETAILS_AVAILABLE_HTML);
		inputPortView.add(label);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
