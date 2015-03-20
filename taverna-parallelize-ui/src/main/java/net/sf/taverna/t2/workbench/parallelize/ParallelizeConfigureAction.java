/**
 *
 */
package net.sf.taverna.t2.workbench.parallelize;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map.Entry;

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
import net.sf.taverna.t2.workflow.edits.AddChildEdit;
import net.sf.taverna.t2.workflow.edits.ChangeJsonEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author alanrw
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ParallelizeConfigureAction extends AbstractAction {

	private Frame owner;
	private final Processor processor;
	private final ParallelizeContextualView parallelizeContextualView;

	private EditManager editManager;

	private static Logger logger = Logger.getLogger(ParallelizeConfigureAction.class);

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();
	private final SelectionManager selectionManager;

	public ParallelizeConfigureAction(Frame owner,
			ParallelizeContextualView parallelizeContextualView,
			Processor processor, EditManager editManager, SelectionManager selectionManager) {
		super("Configure");
		this.owner = owner;
		this.parallelizeContextualView = parallelizeContextualView;
		this.processor = processor;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
	}

	public void actionPerformed(ActionEvent e) {
		String processorName = processor.getName();
		String title = "Parallel jobs for service " + processorName;
		final JDialog dialog = new HelpEnabledDialog(owner, title, true);
		Configuration configuration;
		try {
			configuration = scufl2Tools.configurationFor(processor, selectionManager.getSelectedProfile());
		} catch (IndexOutOfBoundsException ex) {
			configuration = new Configuration();
		}
		ParallelizeConfigurationPanel parallelizeConfigurationPanel = new ParallelizeConfigurationPanel(configuration, processorName);
		dialog.add(parallelizeConfigurationPanel, BorderLayout.CENTER);

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
		dialog.pack();
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

		public OKAction(JDialog dialog, ParallelizeConfigurationPanel parallelizeConfigurationPanel) {
			super("OK");
			this.dialog = dialog;
			this.parallelizeConfigurationPanel = parallelizeConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			if (parallelizeConfigurationPanel.validateConfig()) {
				try {
					try {
						Configuration configuration = scufl2Tools.configurationFor(processor, selectionManager.getSelectedProfile());
						ObjectNode json = configuration.getJsonAsObjectNode().deepCopy();
						ObjectNode parallelizeNode = null;
						if (json.has("parallelize")) {
							parallelizeNode = (ObjectNode) json.get("parallelize");
						} else {
							parallelizeNode = json.objectNode();
							json.put("parallelize", parallelizeNode);
						}
						JsonNode newParallelizeNode = parallelizeConfigurationPanel.getJson();
						Iterator<Entry<String, JsonNode>> fields = newParallelizeNode.fields();
						while (fields.hasNext()) {
							Entry<String, JsonNode> entry = fields.next();
							parallelizeNode.set(entry.getKey(), entry.getValue());
						}
						Edit<Configuration> edit = new ChangeJsonEdit(configuration, json);
						editManager.doDataflowEdit(selectionManager.getSelectedWorkflowBundle(), edit);
					} catch (IndexOutOfBoundsException ex) {
						Configuration configuration = new Configuration();
						configuration.setConfigures(processor);
						ObjectNode json = configuration.getJsonAsObjectNode();
						json.put("parallelize", parallelizeConfigurationPanel.getJson());
						Edit<Profile> edit = new AddChildEdit<Profile>(selectionManager.getSelectedProfile(), configuration);
						editManager.doDataflowEdit(selectionManager.getSelectedWorkflowBundle(), edit);
					}
					dialog.setVisible(false);
					if (parallelizeContextualView != null) {
						parallelizeContextualView.refreshView();
					}
				} catch (EditException e1) {
					logger.warn("Could not configure jobs", e1);
					JOptionPane.showMessageDialog(owner, "Could not configure jobs",
							"An error occured when configuring jobs: " + e1.getMessage(),
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
