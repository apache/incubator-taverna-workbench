package org.apache.taverna.reference.ui;
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
 * UI for editing url's.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class UrlPanel extends JPanel {
	private JTextField urlField;

	public UrlPanel() {
		super(new GridBagLayout());

		urlField = new JTextField();

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
		add(urlField, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = VERTICAL;
		constraints.weighty = 1d;
		add(new JPanel(), constraints);
	}

	/**
	 * Returns the urlField.
	 * 
	 * @return the urlField
	 */
	public JTextField getUrlField() {
		return urlField;
	}

	/**
	 * Returns the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return urlField.getText();
	}

	/**
	 * Sets the url.
	 * 
	 * @param url
	 *            the url
	 */
	public void setUrl(String url) {
		urlField.setText(url);
	}
}
