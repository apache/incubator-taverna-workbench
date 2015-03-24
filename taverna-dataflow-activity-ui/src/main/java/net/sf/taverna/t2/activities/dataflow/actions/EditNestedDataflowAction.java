/**
 *
 */
package net.sf.taverna.t2.activities.dataflow.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.fasterxml.jackson.databind.JsonNode;

import net.sf.taverna.t2.activities.dataflow.servicedescriptions.DataflowTemplateService;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Workflow;

@SuppressWarnings("serial")
public class EditNestedDataflowAction extends AbstractAction {

	private final Activity activity;
	private final SelectionManager selectionManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public EditNestedDataflowAction(Activity activity, SelectionManager selectionManager) {
		super("Edit nested workflow");
		this.activity = activity;
		this.selectionManager = selectionManager;
	}

	public void actionPerformed(ActionEvent e) {
		if (activity.getType().equals(DataflowTemplateService.ACTIVITY_TYPE)) {
			for (Configuration configuration : scufl2Tools.configurationsFor(activity, selectionManager.getSelectedProfile())) {
				JsonNode nested = configuration.getJson().get("nestedWorkflow");
				Workflow nestedWorkflow = selectionManager.getSelectedWorkflowBundle().getWorkflows().getByName(nested.asText());
				if (nestedWorkflow != null) {
					selectionManager.setSelectedWorkflow(nestedWorkflow);
					break;
				}
			}
		}
	}

}