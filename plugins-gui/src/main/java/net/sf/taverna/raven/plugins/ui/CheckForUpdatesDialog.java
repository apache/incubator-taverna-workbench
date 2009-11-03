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
package net.sf.taverna.raven.plugins.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Dialog that lets user know that there are updates available.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class CheckForUpdatesDialog extends JDialog{
	
	private Logger logger = Logger.getLogger(CheckForUpdatesDialog.class);

	public CheckForUpdatesDialog(){
		super((Frame)null, "Updates available", true);
		initComponents();
	}
	
	public static void main (String[] args){
		CheckForUpdatesDialog dialog = new CheckForUpdatesDialog();
		dialog.setVisible(true);
	}

	private void initComponents() {
		// Base font for all components on the form
		Font baseFont = new JLabel("base font").getFont().deriveFont(11f);
		
		// Message saying that updates are available
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder(EtchedBorder.LOWERED)));
		JLabel message = new JLabel(
				"<html><body>Updates are available for some Taverna components. To review and <br>install them go to 'Updates and plugins' in the 'Advanced' menu.</body><html>");
		message.setFont(baseFont.deriveFont(12f));
		message.setBorder(new EmptyBorder(5,5,5,5));
		message.setIcon(UpdatesAvailableIcon.updateIcon);
		messagePanel.add(message, BorderLayout.CENTER);
		
		// Buttons
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton("OK"); // we'll check for updates again in 2 weeks
		okButton.setFont(baseFont);
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				okPressed();
			}
		});
		
		buttonsPanel.add(okButton);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(messagePanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		pack();
		setResizable(false);
		// Center the dialog on the screen (we do not have the parent)
		Dimension dimension = getToolkit().getScreenSize();
		Rectangle abounds = getBounds();
		setLocation((dimension.width - abounds.width) / 2,
				(dimension.height - abounds.height) / 2);
		setSize(getPreferredSize());
	}
	
	protected void okPressed() {
	       try {
	            FileUtils.touch(CheckForUpdatesStartupHook.lastUpdateCheckFile);
	        } catch (IOException ioex) {
	        	logger.error("Failed to touch the 'Last update check' file for Taverna updates.", ioex);
	        }
		closeDialog();		
	}
	
	private void closeDialog() {
		setVisible(false);
		dispose();
	}

}
