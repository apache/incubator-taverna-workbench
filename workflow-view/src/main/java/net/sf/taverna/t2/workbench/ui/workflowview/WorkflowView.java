package net.sf.taverna.t2.workbench.ui.workflowview;

import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.dndhandler.ServiceTransferHandler;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * 
 * Super class for all UIComponentSPIs that display a Workflow
 * @author alanrw
 *
 */
public abstract class WorkflowView extends JPanel implements UIComponentSPI{
	
	/**
	 * Create a WorkflowView and set it up to receive services.
	 */
	public WorkflowView() {
		super();
		this.setTransferHandler(new ServiceTransferHandler());
	}

}
