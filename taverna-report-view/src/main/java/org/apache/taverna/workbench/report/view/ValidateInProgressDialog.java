
package org.apache.taverna.workbench.report.view;
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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.workbench.MainWindow;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;
import org.apache.taverna.workbench.icons.WorkbenchIcons;

/**
 * Dialog that is popped up while we are validating the workflow. This is just to let 
 * the user know that Taverna is doing something.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ValidateInProgressDialog extends HelpEnabledDialog {


	private boolean userCancelled = false;

	public ValidateInProgressDialog() {
		
		super(MainWindow.getMainWindow(), "Validating workflow", true, null);		
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(10,10,10,10));
		
		JPanel textPanel = new JPanel();
		JLabel text = new JLabel(WorkbenchIcons.workingIcon);
		text.setText("Validating workflow...");
		text.setBorder(new EmptyBorder(10,0,10,0));
		textPanel.add(text);
		panel.add(textPanel, BorderLayout.CENTER);

		/**
		 * Cancellation does not work

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
*/
		setContentPane(panel);
		setPreferredSize(new Dimension(300, 100));

		pack();		
	}

	public boolean hasUserCancelled() {
		return userCancelled;
	}

}
