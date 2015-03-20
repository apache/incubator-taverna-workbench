/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.workflowview;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 * @author alanrw
 */
public class ShowExceptionRunnable implements Runnable {
	Exception e;
	private final String message;

	public ShowExceptionRunnable(String message, Exception e) {
		this.message = message;
		this.e = e;
	}

	@Override
	public void run() {
		showMessageDialog(null, message + ": " + e.getMessage(),
				"Service addition problem", ERROR_MESSAGE);
	}
}
