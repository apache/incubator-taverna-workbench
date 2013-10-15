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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.ChangeJsonEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.dispatchstack.DispatchStackLayer;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public class RetryConfigureAction extends AbstractAction {

	private static Logger logger = Logger.getLogger(RetryConfigureAction.class);

	private final Frame owner;
	private final DispatchStackLayer retryLayer;
	private final RetryContextualView retryContextualView;

	private final EditManager editManager;
	private final SelectionManager selectionManager;

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();

	private Configuration configuration;

	public RetryConfigureAction(Frame owner, RetryContextualView retryContextualView,
			DispatchStackLayer retryLayer, EditManager editManager, SelectionManager selectionManager) {
		super("Configure");
		this.owner = owner;
		this.retryContextualView = retryContextualView;
		this.retryLayer = retryLayer;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
	}

	public void actionPerformed(ActionEvent e) {
		String title = "Retries for service " + retryLayer.getParent().getParent().getName();
		final JDialog dialog = new HelpEnabledDialog(owner, title, true);
		configuration = scufl2Tools.configurationFor(retryLayer, selectionManager.getSelectedProfile());
		RetryConfigurationPanel retryConfigurationPanel = new RetryConfigurationPanel(configuration);
		dialog.add(retryConfigurationPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton okButton = new JButton(new OKAction(dialog, retryConfigurationPanel));
		buttonPanel.add(okButton);

		JButton resetButton = new JButton(new ResetAction(retryConfigurationPanel));
		buttonPanel.add(resetButton);

		JButton cancelButton = new JButton(new CancelAction(dialog));
		buttonPanel.add(cancelButton);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
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

		public OKAction(JDialog dialog, RetryConfigurationPanel retryConfigurationPanel) {
			super("OK");
			this.dialog = dialog;
			this.retryConfigurationPanel = retryConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			if (retryConfigurationPanel.validateConfig()) {
				try {
					Edit<Configuration> edit = new ChangeJsonEdit(configuration, retryConfigurationPanel.getJson());
					editManager.doDataflowEdit(selectionManager.getSelectedWorkflowBundle(), edit);
					dialog.setVisible(false);
					if (retryContextualView != null) {
						retryContextualView.refreshView();
					}
				} catch (EditException e1) {
					logger.warn("Could not configure retries", e1);
					JOptionPane.showMessageDialog(owner, "Could not configure retries",
							"An error occured when configuring retries: " + e1.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				}
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
