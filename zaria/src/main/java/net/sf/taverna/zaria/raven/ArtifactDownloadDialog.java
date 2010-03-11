/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
package net.sf.taverna.zaria.raven;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;

@SuppressWarnings("serial")
public class ArtifactDownloadDialog extends JDialog {

	private static Artifact artifact = null;
	private static ArtifactDownloadDialog dialog;

	public static Artifact showDialog(Component frameComp,
			Component locationComp, String labelText, String title,
			String[] suggestedGroups, String[] suggestedVersions) {
		Frame frame = JOptionPane.getFrameForComponent(frameComp);
		dialog = new ArtifactDownloadDialog(frame, locationComp, labelText,
				title, suggestedGroups, suggestedVersions);
		dialog.setVisible(true);
		return artifact;
	}

	private ArtifactDownloadDialog(Frame frame, Component locationComp,
			String labelText, String title, String[] suggestedGroups,
			String[] suggestedVersions) {
		super(frame, title, true);

		// Content area
		JPanel sP = new JPanel();
		sP.setLayout(new BoxLayout(sP, BoxLayout.PAGE_AXIS));

		final JComboBox groupID = new JComboBox(suggestedGroups);
		groupID.setEditable(true);
		final JComboBox version = new JComboBox(suggestedVersions);
		version.setEditable(true);
		final JTextField artifactID = new JTextField("");
		sP.add(groupID);
		sP.add(Box.createRigidArea(new Dimension(3, 3)));
		sP.add(artifactID);
		sP.add(Box.createRigidArea(new Dimension(3, 3)));
		sP.add(version);
		sP.add(Box.createRigidArea(new Dimension(3, 3)));

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ("set".equals(e.getActionCommand())
						&& artifactID.getText().equals("") == false
						&& groupID.getSelectedItem() != null
						&& version.getSelectedItem() != null) {
					artifact = new BasicArtifact((String) groupID
							.getSelectedItem(), artifactID.getText(),
							(String) version.getSelectedItem());
				} else {
					artifact = null;
				}
				ArtifactDownloadDialog.dialog.setVisible(false);
			}
		};

		// Buttons
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(listener);
		final JButton setButton = new JButton("Download");
		setButton.setActionCommand("set");
		setButton.addActionListener(listener);
		getRootPane().setDefaultButton(setButton);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(setButton);

		// Show the thing
		getContentPane().add(new JLabel(labelText), BorderLayout.NORTH);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(sP, BorderLayout.CENTER);
		getContentPane().add(Box.createRigidArea(new Dimension(3, 3)),
				BorderLayout.EAST);
		getContentPane().add(Box.createRigidArea(new Dimension(3, 3)),
				BorderLayout.WEST);
		pack();
		setLocationRelativeTo(locationComp);
	}

}
