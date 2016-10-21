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
package org.apache.taverna.workbench.design.ui;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.event.ItemEvent.SELECTED;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * UI for creating/editing dataflow input ports.
 * 
 * @author David Withers
 */
public class DataflowInputPortPanel extends JPanel {
	private static final long serialVersionUID = 2650486705615513458L;

	private JTextField portNameField;
	private JRadioButton singleValueButton;
	private JRadioButton listValueButton;
	private JSpinner listDepthSpinner;

	public DataflowInputPortPanel() {
		super(new GridBagLayout());

		portNameField = new JTextField();
		singleValueButton = new JRadioButton("Single value");
		listValueButton = new JRadioButton("List of depth ");
		listDepthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

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
		add(portNameField, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.weightx = 0d;
		constraints.fill = NONE;
		constraints.ipadx = 10;
		constraints.insets = new Insets(10, 0, 0, 0);
		add(new JLabel("Type:"), constraints);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(singleValueButton);
		buttonGroup.add(listValueButton);

		final JLabel helpLabel = new JLabel(
				"Depth 1 is a list, 2 is a list of lists, etc.");
		helpLabel.setFont(helpLabel.getFont().deriveFont(11f));

		singleValueButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean selected = (e.getStateChange() == SELECTED);
				listDepthSpinner.setEnabled(!selected);
				helpLabel.setEnabled(!selected);
			}
		});

		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.ipadx = 0;
		add(singleValueButton, constraints);
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(0, 0, 0, 0);
		add(listValueButton, constraints);
		constraints.gridx = 2;
		add(listDepthSpinner, constraints);
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.gridwidth = 2;
		constraints.weighty = 1d;
		constraints.anchor = NORTHWEST;
		constraints.insets = new Insets(0, 20, 0, 0);
		add(helpLabel, constraints);
	}

	/**
	 * Returns the portNameField.
	 * 
	 * @return the portNameField
	 */
	public JTextField getPortNameField() {
		return portNameField;
	}

	/**
	 * Returns the singleValueButton.
	 * 
	 * @return the singleValueButton
	 */
	public JRadioButton getSingleValueButton() {
		return singleValueButton;
	}

	/**
	 * Returns the listValueButton.
	 * 
	 * @return the listValueButton
	 */
	public JRadioButton getListValueButton() {
		return listValueButton;
	}

	/**
	 * Returns the port name.
	 * 
	 * @return the port name
	 */
	public String getPortName() {
		return portNameField.getText();
	}

	/**
	 * Sets the port name.
	 * 
	 * @param name
	 *            the name of the port
	 */
	public void setPortName(String name) {
		portNameField.setText(name);
		// Select the text
		if (!name.isEmpty()) {
			portNameField.setSelectionStart(0);
			portNameField.setSelectionEnd(name.length());
		}
	}

	/**
	 * Returns the port depth.
	 * 
	 * @return the port depth
	 */
	public int getPortDepth() {
		if (singleValueButton.isSelected())
			return 0;
		return (Integer) listDepthSpinner.getValue();
	}

	/**
	 * Sets the port depth.
	 * 
	 * @param depth
	 *            the depth of the port
	 */
	public void setPortDepth(int depth) {
		if (depth == 0) {
			singleValueButton.setSelected(true);
		} else {
			listValueButton.setSelected(true);
			listDepthSpinner.setValue(depth);
		}
	}
}
