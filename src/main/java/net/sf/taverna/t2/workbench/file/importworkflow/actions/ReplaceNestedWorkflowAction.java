package net.sf.taverna.t2.workbench.file.importworkflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.ui.Utils;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;

public class ReplaceNestedWorkflowAction extends
		ActivityConfigurationAction<DataflowActivity, Dataflow> {
	private static final long serialVersionUID = 1L;

	private FileManager fileManager = FileManager.getInstance();
	private EditManager editManager = EditManager.getInstance();
	private Edits edits = editManager.getEdits();

	public ReplaceNestedWorkflowAction(DataflowActivity activity) {
		super(activity);
		putValue(NAME, "Replace nested workflow");
	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		ImportWorkflowWizard wizard = new ImportWorkflowWizard(Utils
				.getParentFrame(parentComponent)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Edit<?> makeInsertNestedWorkflowEdit(Dataflow nestedFlow,
					String name) {
				return edits
						.getConfigureActivityEdit(getActivity(), nestedFlow);
			}
		};

		wizard.setMergeEnabled(false);
		wizard.setCustomDestinationDataflow(fileManager.getCurrentDataflow(),
				"Existing nested workflow");
		wizard.setDestinationEnabled(false);
		wizard.setVisible(true);
	}

}
