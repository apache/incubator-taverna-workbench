
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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static org.apache.taverna.workbench.MainWindow.getMainWindow;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.workingIcon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.workbench.helper.HelpEnabledDialog;

/**
 * Dialog that is popped up while we are copying the workflow in preparation for
 * the workflow execution. This is just to let the user know that Taverna is
 * doing something.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class CopyWorkflowInProgressDialog extends HelpEnabledDialog {
	private boolean userCancelled = false;

	public CopyWorkflowInProgressDialog() {
		super(getMainWindow(), "Initialising workflow run", true);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(10,10,10,10));

		JPanel textPanel = new JPanel();
		JLabel text = new JLabel(workingIcon);
		text.setText("Initialising workflow run...");
		text.setBorder(new EmptyBorder(10, 0, 10, 0));
		textPanel.add(text);
		panel.add(textPanel, CENTER);

		// Cancel button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userCancelled = true;
				setVisible(false);
				dispose();
			}
		});
		JPanel cancelButtonPanel = new JPanel();
		cancelButtonPanel.add(cancelButton);
		panel.add(cancelButtonPanel, SOUTH);

		setContentPane(panel);
		setPreferredSize(new Dimension(300, 130));

		pack();
	}

	public boolean hasUserCancelled() {
		return userCancelled;
	}
}
