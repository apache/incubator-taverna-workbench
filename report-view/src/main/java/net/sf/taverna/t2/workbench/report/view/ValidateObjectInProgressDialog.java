
/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.report.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

/**
 * Dialog that is popped up while we are validating the workflow. This is just to let 
 * the user know that Taverna is doing something.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ValidateObjectInProgressDialog extends JDialog{


	private boolean userCancelled = false;

	public ValidateObjectInProgressDialog() {
		
		super((Frame) null, "Validating service", true);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(10,10,10,10));
		
		JPanel textPanel = new JPanel();
		JLabel text = new JLabel(WorkbenchIcons.workingIcon);
		text.setText("Validating service...");
		text.setBorder(new EmptyBorder(10,0,10,0));
		textPanel.add(text);
		panel.add(textPanel, BorderLayout.CENTER);

		// Cancel button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				userCancelled = true;
				setVisible(false);
				dispose();
			}
		});
		JPanel cancelButtonPanel = new JPanel();
		cancelButtonPanel.add(cancelButton);
		panel.add(cancelButtonPanel, BorderLayout.SOUTH);

		setContentPane(panel);
		setPreferredSize(new Dimension(300, 130));

		pack();		
	}

	public boolean hasUserCancelled() {
		return userCancelled;
	}

}
