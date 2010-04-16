/**
 * 
 */
package net.sf.taverna.t2.workbench.retry;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.RetryConfig;

/**
 * @author alanrw
 *
 */
public class RetryConfigureAction extends AbstractAction {

	private Frame owner;
	private final Retry retryLayer;
	private final RetryContextualView retryContextualView;

	public RetryConfigureAction(Frame owner, RetryContextualView retryContextualView, Retry retryLayer) {
		super("Configure");
		this.owner = owner;
		this.retryContextualView = retryContextualView;
		this.retryLayer = retryLayer;
	}

	public void actionPerformed(ActionEvent e) {
		String title = "Retries for " + retryLayer.getProcessor().getLocalName();
		final JDialog dialog = new JDialog(owner, title, true);
		RetryConfigurationPanel retryConfigurationPanel = new RetryConfigurationPanel(retryLayer.getConfiguration());
		dialog.add(retryConfigurationPanel, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton okButton = new JButton(new OKAction(dialog,
				retryConfigurationPanel));
		buttonPanel.add(okButton);

		JButton resetButton = new JButton(new ResetAction(
				retryConfigurationPanel));
		buttonPanel.add(resetButton);

		JButton cancelButton = new JButton(new CancelAction(dialog));
		buttonPanel.add(cancelButton);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setSize(450, 450);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public class ResetAction extends AbstractAction {

		private final RetryConfigurationPanel retryConfigurationPanel;

		public ResetAction(RetryConfigurationPanel retryConfigurationPanel) {
			super("Reset");
			this.retryConfigurationPanel = retryConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			retryConfigurationPanel.populate();
		}

	}

	public class OKAction extends AbstractAction {

		private final RetryConfigurationPanel retryConfigurationPanel;
		private final JDialog dialog;

		public OKAction(JDialog dialog,
				RetryConfigurationPanel retryConfigurationPanel) {
			super("OK");
			this.dialog = dialog;
			this.retryConfigurationPanel = retryConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			if (retryConfigurationPanel.validateConfig()) {
				retryLayer.configure(retryConfigurationPanel.getConfiguration());
				dialog.setVisible(false);
				retryContextualView.refreshView();
			}
		}

	}

	public class CancelAction extends AbstractAction {

		private final JDialog dialog;

		public CancelAction(JDialog dialog) {
			super("Cancel");
			this.dialog = dialog;

		}

		public void actionPerformed(ActionEvent e) {
			dialog.setVisible(false);
		}

	}


}
