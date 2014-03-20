/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * @author alanrw
 *
 */
public class ReportViewConfigureAction extends AbstractAction {

	private Processor configuredProcessor = null;
	
	public ReportViewConfigureAction() {
		
	}

	public void setConfiguredProcessor(Processor configuredProcessor) {
		this.configuredProcessor = configuredProcessor;
	}

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
