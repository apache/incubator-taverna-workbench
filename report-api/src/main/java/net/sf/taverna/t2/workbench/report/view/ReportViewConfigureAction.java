/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;

/**
 * @author alanrw
 *
 */
public class ReportViewConfigureAction extends AbstractAction {

	private Processor configuredProcessor = null;

	public ReportViewConfigureAction(Processor p) {
		super();
		this.configuredProcessor = p;
	}

	public void actionPerformed(ActionEvent e) {
		Action action = WorkflowView.getConfigureAction(configuredProcessor);
		if (action != null) {
			action.actionPerformed(e);
		}
	}

}
