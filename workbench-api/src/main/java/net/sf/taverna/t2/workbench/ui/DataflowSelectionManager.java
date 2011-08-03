package net.sf.taverna.t2.workbench.ui;

import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public interface DataflowSelectionManager {

	/**
	 * Returns the <code>DataflowSelectionModel</code> for the dataflow.
	 *
	 * @param dataflow
	 * @return the <code>DataflowSelectionModel</code> for the dataflow
	 */
	public DataflowSelectionModel getDataflowSelectionModel(Dataflow dataflow);

	public void removeDataflowSelectionModel(Dataflow dataflow);

}