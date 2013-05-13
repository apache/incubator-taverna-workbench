/**
 *
 */
package net.sf.taverna.t2.activities.dataflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;

public class EditNestedDataflowAction extends AbstractAction {
	private static final long serialVersionUID = 8854590545492535080L;
	private static Logger logger = Logger
			.getLogger(EditNestedDataflowAction.class);
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();
	private final Activity dataflowActivity;
	private final FileManager fileManager;

	public EditNestedDataflowAction(Activity activity, FileManager fileManager) {
		super("Edit nested workflow");
		this.dataflowActivity = activity;
		this.fileManager = fileManager;
	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		openNestedWorkflow(parentComponent);
	}

	public void openNestedWorkflow(final Component parentComponent) {
		NestedDataflowSource nestedDataflowSource = new NestedDataflowActivitySource(
				fileManager.getCurrentDataflow(), dataflowActivity, fileManager);

		WorkflowBundle alreadyOpen = fileManager.getDataflowBySource(nestedDataflowSource);
		if (alreadyOpen != null) {
			// The nested workflow is already opened - switch to it
			fileManager.setCurrentDataflow(alreadyOpen);
			return;
		}

		try {
			fileManager.openDataflow(T2_FLOW_FILE_TYPE, nestedDataflowSource);
		} catch (OpenException e1) {
			logger.error("Could not open nested workflow from service "
					+ dataflowActivity, e1);
			JOptionPane
					.showMessageDialog(parentComponent,
							"Could not open nested workflow:\n"
									+ e1.getMessage(),
							"Could not open nested workflow",
							JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

}