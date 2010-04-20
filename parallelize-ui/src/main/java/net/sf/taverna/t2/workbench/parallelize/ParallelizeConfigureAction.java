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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
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

	private EditManager editManager = EditManager.getInstance();

	private Edits edits = EditManager.getInstance().getEdits();

	private FileManager fileManager = FileManager.getInstance();

	private static Logger logger = Logger.getLogger(ParallelizeConfigureAction.class);
	
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
				try {
					Edit edit = edits.getConfigureEdit(parallelizeLayer,
							parallelizeConfigurationPanel.getConfiguration());
					editManager.doDataflowEdit(
							fileManager.getCurrentDataflow(), edit);
					dialog.setVisible(false);
					parallelizeContextualView.refreshView();
				} catch (EditException e1) {
					logger.warn("Could not configure jobs", e1);
					JOptionPane.showMessageDialog(owner,
							"Could not configure jobs",
							"An error occured when configuring jobs: "
									+ e1.getMessage(),
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
