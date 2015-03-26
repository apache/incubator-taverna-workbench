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
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Test launcher for Credential Manager GUI (so it does not have to be
 * launched from Taverna).
 *
 * @author Alexandra Nenadic
 */
public class CredentialManagerUILauncher extends JFrame {
	private static final long serialVersionUID = 2079805060170251148L;

	private final ImageIcon launchCMIcon = new ImageIcon(
			CredentialManagerUILauncher.class
					.getResource("/images/cred_manager.png"));

	public CredentialManagerUILauncher() {
		JPanel jpLaunch = new JPanel();
		jpLaunch.setPreferredSize(new Dimension(300, 120));

		JLabel jlLaunch = new JLabel("T2: Launch Credential Manager GUI");

		JButton jbLaunch = new JButton();
		jbLaunch.setIcon(launchCMIcon);
		jbLaunch.setToolTipText("Launches Credential Manager");
		jbLaunch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CredentialManagerUI cmGUI = new CredentialManagerUI(null, null);
				if (cmGUI != null)
					cmGUI.setVisible(true);
			}
		});

		jpLaunch.add(jlLaunch);
		jpLaunch.add(jbLaunch);

		getContentPane().add(jpLaunch, CENTER);

		// Handle application close
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		pack();

        // Centre the frame in the centre of the desktop
        setLocationRelativeTo(null);
        // Set the frame's title
        setTitle("Credential Manager GUI Launcher");
        setVisible(true);
	}

	/**
	 * Launcher for the Credential Manager GUI.
	 */
	public static void main(String[] args) {
        // Create and show GUI on the event handler thread
        invokeLater(new Runnable(){
        	@Override
        	public void run() {
        		new CredentialManagerUILauncher();
        	}
        });
    }
}
