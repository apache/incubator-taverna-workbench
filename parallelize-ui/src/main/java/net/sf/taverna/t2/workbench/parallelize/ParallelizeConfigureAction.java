/**
 * 
 */
package net.sf.taverna.t2.workbench.parallelize;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ParallelizeConfig;

/**
 * @author alanrw
 *
 */
public class ParallelizeConfigureAction extends AbstractAction {

	private Frame owner;
	private final Parallelize parallelizeLayer;
	private final ParallelizeContextualView parallelizeContextualView;

	public ParallelizeConfigureAction(Frame owner, ParallelizeContextualView parallelizeContextualView, Parallelize parallelizeLayer) {
		super("Configure");
		this.owner = owner;
		this.parallelizeContextualView = parallelizeContextualView;
		this.parallelizeLayer = parallelizeLayer;
	}

	public void actionPerformed(ActionEvent e) {
		String title = "Jobs for " + parallelizeLayer.getProcessor().getLocalName();
		final JDialog dialog = new JDialog(owner, title, true);
		ParallelizeConfigurationPanel parallelizeConfigurationPanel = new ParallelizeConfigurationPanel(parallelizeLayer.getConfiguration());
		dialog.add(parallelizeConfigurationPanel, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton okButton = new JButton(new OKAction(dialog,
				parallelizeConfigurationPanel));
		buttonPanel.add(okButton);

		JButton resetButton = new JButton(new ResetAction(
				parallelizeConfigurationPanel));
		buttonPanel.add(resetButton);

		JButton cancelButton = new JButton(new CancelAction(dialog));
		buttonPanel.add(cancelButton);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setSize(450, 450);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public class ResetAction extends AbstractAction {

		private final ParallelizeConfigurationPanel parallelizeConfigurationPanel;

		public ResetAction(ParallelizeConfigurationPanel parallelizeConfigurationPanel) {
			super("Reset");
			this.parallelizeConfigurationPanel = parallelizeConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			parallelizeConfigurationPanel.populate();
		}

	}

	public class OKAction extends AbstractAction {

		private final ParallelizeConfigurationPanel parallelizeConfigurationPanel;
		private final JDialog dialog;

		public OKAction(JDialog dialog,
				ParallelizeConfigurationPanel parallelizeConfigurationPanel) {
			super("OK");
			this.dialog = dialog;
			this.parallelizeConfigurationPanel = parallelizeConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			if (parallelizeConfigurationPanel.validateConfig()) {
				parallelizeLayer.configure(parallelizeConfigurationPanel.getConfiguration());
				dialog.setVisible(false);
				parallelizeContextualView.refreshView();
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
