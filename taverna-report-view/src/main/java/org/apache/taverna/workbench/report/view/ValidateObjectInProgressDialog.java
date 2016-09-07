
package org.apache.taverna.workbench.report.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.workbench.icons.WorkbenchIcons;

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
