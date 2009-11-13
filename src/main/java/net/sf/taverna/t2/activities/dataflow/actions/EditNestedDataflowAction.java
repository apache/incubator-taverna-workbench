/**
 * 
 */
package net.sf.taverna.t2.activities.dataflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.filemanager.NestedDataflowSource;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class EditNestedDataflowAction extends AbstractAction {
	private static final long serialVersionUID = 8854590545492535080L;
	private static Logger logger = Logger
			.getLogger(EditNestedDataflowAction.class);
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();
	private final DataflowActivity dataflowActivity;
	private FileManager fileManager = FileManager.getInstance();

	public EditNestedDataflowAction(DataflowActivity activity) {
		super("Edit nested workflow");
		this.dataflowActivity = activity;
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
		NestedDataflowSource nestedDataflowSource = new NestedDataflowSource(
				getFileManager().getCurrentDataflow(), dataflowActivity);

		Dataflow alreadyOpen = getFileManager()
				.getDataflowBySource(nestedDataflowSource);
		if (alreadyOpen != null) {
			// The nested workflow is already opened - switch to it
			getFileManager().setCurrentDataflow(alreadyOpen);
			return;
		}

		try {
			getFileManager().openDataflow(T2_FLOW_FILE_TYPE, nestedDataflowSource);
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

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public FileManager getFileManager() {
		return fileManager;
	}
}