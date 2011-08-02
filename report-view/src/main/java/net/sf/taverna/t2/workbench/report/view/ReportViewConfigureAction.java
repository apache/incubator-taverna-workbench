/**
 *
 */
package net.sf.taverna.t2.workbench.report.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * @author alanrw
 *
 */
public class ReportViewConfigureAction extends AbstractAction {

	private Processor configuredProcessor = null;
	private MenuManager menuManager;

	public ReportViewConfigureAction() {

	}

	public void setConfiguredProcessor(Processor configuredProcessor, MenuManager menuManager) {
		this.configuredProcessor = configuredProcessor;
		this.menuManager = menuManager;
	}

	public ReportViewConfigureAction(Processor p) {
		super();
		this.configuredProcessor = p;
	}

	public void actionPerformed(ActionEvent e) {
		Action action = WorkflowView.getConfigureAction(configuredProcessor, menuManager);
		if (action != null) {
			action.actionPerformed(e);
		}
	}

}
