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

package org.apache.taverna.workbench.parallelize;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.scufl2.api.configurations.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("serial")
public class ParallelizeConfigurationPanel extends JPanel {

	private ObjectNode json;
	private JTextField maxJobsField = new JTextField(10);
	private final String processorName;

	public ParallelizeConfigurationPanel(Configuration configuration, String processorName) {
		if (configuration.getJson().has("parallelize")) {
			json = (ObjectNode) configuration.getJson().get("parallelize").deepCopy();
		} else {
			json = configuration.getJsonAsObjectNode().objectNode();
		}
		this.processorName = processorName;
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(10,10,10,10));
		populate();
	}

	public void populate() {
		this.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel jobs = new JLabel("<html><body>Maximum numbers of items to process at the same time</body></html>");

		jobs.setBorder(new EmptyBorder(0,0,0,10));
		gbc.weightx = 0.8;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(jobs, gbc);
		if (json.has("maximumJobs")) {
			maxJobsField.setText(json.get("maximumJobs").asText());
		} else {
			maxJobsField.setText("1");
		}
		gbc.weightx = 0.2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(maxJobsField, gbc);
		gbc.weightx = 0.1;
		this.add(new JPanel(), gbc);

		gbc.gridy=1;
		gbc.gridx=0;
		gbc.gridwidth=3;
		gbc.weightx=0;
		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		JLabel explanationLabel = new JLabel("<html><body><small>" +
					"The service <b>" +  processorName + "</b> will be invoked as soon as the required inputs " +
				    "for an iteration are available, but no more than the maximum number of items " +
					"will be invoked at the same time."
					+ "</small></body></html>");
		this.add(explanationLabel, gbc);

		this.setPreferredSize(new Dimension(350, 170));
	}

	public boolean validateConfig() {
		String errorText = "";
		int maxJobs = -1;
		try {
			maxJobs = Integer.parseInt(maxJobsField.getText());
			if (maxJobs < 1) {
				errorText += "The maximum number of items must be a positive integer.\n";
			}
		}
		catch (NumberFormatException e) {
			errorText += "The maximum number of items must be an integer.\n";
		}

		if (errorText.length() > 0) {
			JOptionPane.showMessageDialog(this, errorText, "", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public JsonNode getJson() {
		json.put("maximumJobs", maxJobsField.getText());
		return json;
	}

}
