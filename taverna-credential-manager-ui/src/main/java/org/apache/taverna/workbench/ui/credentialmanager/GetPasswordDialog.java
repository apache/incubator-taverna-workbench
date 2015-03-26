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

package org.apache.taverna.workbench.ui.credentialmanager;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Font.PLAIN;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.WARN_TITLE;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * A general dialog for entering a password.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class GetPasswordDialog extends NonBlockedHelpEnabledDialog {
	/** Instructions for user explaining the purpose of the password */
	private String instructions = null;
	/* Password entry password field */
	private JPasswordField passwordField;
	/* Stores the password entered */
	private String password = null;

	public GetPasswordDialog(JFrame parent, String title, boolean modal,
			String instr) {
		super(parent, title, modal);
		instructions = instr;
		initComponents();
	}

	public GetPasswordDialog(JDialog parent, String title, boolean modal,
			String instr) {
		super(parent, title, modal);
		instructions = instr;
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());

		JLabel passwordLabel = new JLabel("Password");
		passwordField = new JPasswordField(15);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				okPressed();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelPressed();
			}
		});

		JLabel instructionsLabel; // Instructions
		if (instructions != null) {
			instructionsLabel = new JLabel(instructions);
			instructionsLabel.setFont(new Font(null, PLAIN, 11));
			instructionsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(instructionsLabel, NORTH);
		}
        
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        passwordPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        getContentPane().add(passwordPanel, CENTER);
        getContentPane().add(buttonsPanel, SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

        setResizable(false);
        getRootPane().setDefaultButton(okButton);
        pack();
	}

	/**
	 * Get the password entered in the dialog.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Check that the password entered is not empty and store the entered
	 * password.
	 */
	private boolean checkPassword() {
		password = new String(passwordField.getPassword());

		if (password.isEmpty()) {
			showMessageDialog(this, "The password cannot be empty",
					WARN_TITLE, WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	private void okPressed() {
        if (checkPassword())
            closeDialog();
    }

	private void cancelPressed() {
		password = null;
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}
}
