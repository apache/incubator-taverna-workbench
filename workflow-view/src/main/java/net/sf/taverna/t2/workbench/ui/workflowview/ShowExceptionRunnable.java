/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.workflowview;

import javax.swing.JOptionPane;

/**
 * @author alanrw
 *
 */
public class ShowExceptionRunnable implements Runnable {
	
	Exception e;
	private final String message;

	public ShowExceptionRunnable(String message, Exception e) {
		this.message = message;
		this.e = e;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		JOptionPane.showMessageDialog(null, message + ": " + e.getMessage(), "Service addition problem", JOptionPane.ERROR_MESSAGE);
	}

}
