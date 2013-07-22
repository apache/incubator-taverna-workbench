/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import net.sf.taverna.t2.lang.ui.FileTools;
import net.sf.taverna.t2.lang.ui.KeywordDocument;
import net.sf.taverna.t2.lang.ui.LineEnabledTextPanel;
import net.sf.taverna.t2.lang.ui.LinePainter;
import net.sf.taverna.t2.lang.ui.NoWrapEditorKit;

/**
 * Component for configuring activities that have scripts.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ScriptConfigurationComponent extends JPanel {

	private JTextPane scriptTextArea;

	public ScriptConfigurationComponent(String script, Set<String> keywords, Set<String> ports, final String scriptType, final String fileExtension) {
		super(new BorderLayout());
		scriptTextArea = new JTextPane();
		new LinePainter(scriptTextArea, Color.WHITE);

		final KeywordDocument doc = new KeywordDocument(keywords, ports);

		// NOTE: Due to T2-1145 - always set editor kit BEFORE setDocument
		scriptTextArea.setEditorKit(new NoWrapEditorKit());
		scriptTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scriptTextArea.setDocument(doc);
		scriptTextArea.setText(script);
		scriptTextArea.setCaretPosition(0);
		scriptTextArea.setPreferredSize(new Dimension(200, 100));

		add(new LineEnabledTextPanel(scriptTextArea), BorderLayout.CENTER);

		final JButton checkScriptButton = new JButton("Check script");
		checkScriptButton.setToolTipText("Check the "+scriptType+" script");
		checkScriptButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ex) {
				JOptionPane.showMessageDialog(ScriptConfigurationComponent.this,
						scriptType + " script check not implemented", scriptType + " script check",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		JButton loadScriptButton = new JButton("Load script");
		loadScriptButton.setToolTipText("Load a "+scriptType+" script from a file");
		loadScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newScript = FileTools.readStringFromFile(ScriptConfigurationComponent.this,
						"Load "+scriptType+" script", fileExtension);
				if (newScript != null) {
					scriptTextArea.setText(newScript);
					scriptTextArea.setCaretPosition(0);
				}
			}
		});

		JButton saveRScriptButton = new JButton("Save script");
		saveRScriptButton.setToolTipText("Save the "+scriptType+" script to a file");
		saveRScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTools.saveStringToFile(ScriptConfigurationComponent.this,
						"Save "+scriptType+" script", fileExtension, scriptTextArea.getText());
			}
		});

		JButton clearScriptButton = new JButton("Clear script");
		clearScriptButton.setToolTipText("Clear current script from the edit area");
		clearScriptButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(ScriptConfigurationComponent.this,
						"Do you really want to clear the script?", "Clearing the script",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					scriptTextArea.setText("");
				}
			}

		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(checkScriptButton);
		buttonPanel.add(loadScriptButton);
		buttonPanel.add(saveRScriptButton);
		buttonPanel.add(clearScriptButton);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	public String getScript() {
		return scriptTextArea.getText();
	}

}
