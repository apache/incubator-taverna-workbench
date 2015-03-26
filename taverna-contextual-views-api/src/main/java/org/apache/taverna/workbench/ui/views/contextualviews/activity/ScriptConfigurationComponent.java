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

package org.apache.taverna.workbench.ui.views.contextualviews.activity;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.WHITE;
import static java.awt.Font.PLAIN;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.lang.ui.FileTools.readStringFromFile;
import static org.apache.taverna.lang.ui.FileTools.saveStringToFile;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.apache.taverna.lang.ui.KeywordDocument;
import org.apache.taverna.lang.ui.LineEnabledTextPanel;
import org.apache.taverna.lang.ui.LinePainter;
import org.apache.taverna.lang.ui.NoWrapEditorKit;

/**
 * Component for configuring activities that have scripts.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ScriptConfigurationComponent extends JPanel {
	private JTextPane scriptTextArea;

	public ScriptConfigurationComponent(String script, Set<String> keywords,
			Set<String> ports, final String scriptType,
			final String fileExtension) {
		this(script, keywords, ports, scriptType, fileExtension, "");
	}

	public ScriptConfigurationComponent(String script, Set<String> keywords,
			Set<String> ports, final String scriptType,
			final String fileExtension, final String resetScript) {
		super(new BorderLayout());
		scriptTextArea = new JTextPane();
		new LinePainter(scriptTextArea, WHITE);

		final KeywordDocument doc = new KeywordDocument(keywords, ports);

		// NOTE: Due to T2-1145 - always set editor kit BEFORE setDocument
		scriptTextArea.setEditorKit(new NoWrapEditorKit());
		scriptTextArea.setFont(new Font("Monospaced", PLAIN, 14));
		scriptTextArea.setDocument(doc);
		scriptTextArea.setText(script);
		scriptTextArea.setCaretPosition(0);
		scriptTextArea.setPreferredSize(new Dimension(200, 100));

		add(new LineEnabledTextPanel(scriptTextArea), CENTER);

		final JButton checkScriptButton = new JButton("Check script");
		checkScriptButton.setToolTipText("Check the " + scriptType + " script");
		checkScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ex) {
				showMessageDialog(ScriptConfigurationComponent.this, scriptType
						+ " script check not implemented", scriptType
						+ " script check", INFORMATION_MESSAGE);
			}
		});

		JButton loadScriptButton = new JButton("Load script");
		loadScriptButton.setToolTipText("Load a " + scriptType
				+ " script from a file");
		loadScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newScript = readStringFromFile(
						ScriptConfigurationComponent.this, "Load " + scriptType
								+ " script", fileExtension);
				if (newScript != null) {
					scriptTextArea.setText(newScript);
					scriptTextArea.setCaretPosition(0);
				}
			}
		});

		JButton saveRScriptButton = new JButton("Save script");
		saveRScriptButton.setToolTipText("Save the " + scriptType
				+ " script to a file");
		saveRScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveStringToFile(ScriptConfigurationComponent.this, "Save "
						+ scriptType + " script", fileExtension,
						scriptTextArea.getText());
			}
		});

		JButton clearScriptButton = new JButton("Clear script");
		clearScriptButton.setToolTipText("Clear current script from the edit area");
		clearScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (showConfirmDialog(ScriptConfigurationComponent.this,
						"Do you really want to clear the script?",
						"Clearing the script", YES_NO_OPTION) == YES_OPTION)
					scriptTextArea.setText(resetScript);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(checkScriptButton);
		buttonPanel.add(loadScriptButton);
		buttonPanel.add(saveRScriptButton);
		buttonPanel.add(clearScriptButton);

		add(buttonPanel, SOUTH);
	}

	public String getScript() {
		return scriptTextArea.getText();
	}
}
