/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.credentialmanager;

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
