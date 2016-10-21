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
package org.apache.taverna.workbench.ui.views.contextualviews.condition;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.scufl2.api.core.BlockingControlLink;

/**
 * Contextual view for dataflow's control (condition) links.
 * 
 * @author David Withers
 */
class ConditionContextualView extends ContextualView {
	private static final long serialVersionUID = -894521200616176439L;

	private final BlockingControlLink condition;
	private JPanel contitionView;

	public ConditionContextualView(BlockingControlLink condition) {
		this.condition = condition;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return contitionView;
	}

	@Override
	public String getViewTitle() {
		return "Control link: " + condition.getBlock().getName()
				+ " runs after " + condition.getUntilFinished().getName();
	}

	@Override
	public void refreshView() {
		contitionView = new JPanel(new FlowLayout(FlowLayout.LEFT));
		contitionView.setBorder(new EmptyBorder(5, 5, 5, 5));
		JLabel label = new JLabel(
				"<html><body><i>No details available.</i></body><html>");
		contitionView.add(label);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
