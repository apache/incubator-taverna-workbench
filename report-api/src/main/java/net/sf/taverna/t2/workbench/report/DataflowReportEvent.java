/**
 * 
 */
package net.sf.taverna.t2.workbench.report;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class DataflowReportEvent implements ReportManagerEvent {

	private final Dataflow dataflow;

	public DataflowReportEvent(Dataflow d) {
		this.dataflow = d;
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

}
