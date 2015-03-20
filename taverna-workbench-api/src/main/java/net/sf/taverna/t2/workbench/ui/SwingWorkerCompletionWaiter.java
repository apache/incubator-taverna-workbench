/**
 * 
 */
package net.sf.taverna.t2.workbench.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

/**
 * @author alanrw
 *
 */
public class SwingWorkerCompletionWaiter implements PropertyChangeListener {
	
	private final JDialog dialog;

	public SwingWorkerCompletionWaiter(JDialog dialog) {
		this.dialog = dialog;	
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("state".equals(evt.getPropertyName())
                && SwingWorker.StateValue.DONE == evt.getNewValue()) {
            dialog.setVisible(false);
            dialog.dispose();
        }

	}

}
