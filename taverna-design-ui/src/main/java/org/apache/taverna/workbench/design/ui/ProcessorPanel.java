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

package org.apache.taverna.workbench.design.ui;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.VERTICAL;
import static java.awt.GridBagConstraints.WEST;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * UI for editing processors.
 * 
 * @author David Withers
 */
public class ProcessorPanel extends JPanel {
	private static final long serialVersionUID = 260705376633425003L;

	private JTextField processorNameField;

	public ProcessorPanel() {
		super(new GridBagLayout());

		processorNameField = new JTextField();

		setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 10;
		add(new JLabel("Name:"), constraints);

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		constraints.weightx = 1d;
		constraints.fill = HORIZONTAL;
		add(processorNameField, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = VERTICAL;
		constraints.weighty = 1d;
		add(new JPanel(), constraints);
	}

	/**
	 * Returns the processorNameField.
	 * 
	 * @return the processorNameField
	 */
	public JTextField getProcessorNameField() {
		return processorNameField;
	}

	/**
	 * Returns the processor name.
	 * 
	 * @return the processor name
	 */
	public String getProcessorName() {
		return processorNameField.getText();
	}

	/**
	 * Sets the processor name.
	 * 
	 * @param name
	 *            the name of the processor
	 */
	public void setProcessorName(String name) {
		processorNameField.setText(name);
	}
}
