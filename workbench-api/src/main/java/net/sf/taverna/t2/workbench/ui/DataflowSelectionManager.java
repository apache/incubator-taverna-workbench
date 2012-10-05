package net.sf.taverna.t2.workbench.ui;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;

public interface DataflowSelectionManager {

	/**
	 * Returns the <code>DataflowSelectionModel</code> for the WorkflowBundle.
	 *
	 * @param dataflow
	 * @return the <code>DataflowSelectionModel</code> for the WorkflowBundle
	 */
	public DataflowSelectionModel getDataflowSelectionModel(WorkflowBundle dataflow);

	public void removeDataflowSelectionModel(WorkflowBundle dataflow);

}