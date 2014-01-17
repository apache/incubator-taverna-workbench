/**
 * 
 */
package net.sf.taverna.t2.visit;

import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.report.config.ReportManagerConfiguration;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * Check on the health of a DataflowOutputPort
 * 
 * @author alanrw
 *
 */
public class HighPortDepthHealthChecker implements HealthChecker<DataflowOutputPort> {

	/* *
	 * The visitor can visit DataflowOutputPorts.
	 * 
	 * (non-Javadoc)
	 * @see net.sf.taverna.t2.visit.Visitor#canVisit(java.lang.Object)
	 */
	public boolean canVisit(Object o) {
		return ((o != null) && (o instanceof DataflowOutputPort));
	}

	/** 
	 * The check is not time consuming as it simply constructs a VisitReport
	 * 
	 * (non-Javadoc)
	 * @see net.sf.taverna.t2.visit.Visitor#isTimeConsuming()
	 */
	public boolean isTimeConsuming() {
		return false;
	}

	/**
	 * The result of the visit is simply a VisitReport to state that the depth is high.
	 * 
	 * (non-Javadoc)
	 * @see net.sf.taverna.t2.visit.Visitor#visit(java.lang.Object, java.util.List)
	 */
	public VisitReport visit(DataflowOutputPort o, List<Object> ancestry) {
		int depth = o.getDepth();
		int maxDepth = Integer.parseInt(ReportManagerConfiguration.getInstance().getProperty(ReportManagerConfiguration.MAX_PORT_DEPTH));
		if (depth > maxDepth) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), o, "Port has high predicted depth",
					HealthCheck.HIGH_PORT_DEPTH, Status.WARNING);
			vr.setProperty("portname", o.getName());
			vr.setProperty("predictedportdepth", depth);
			return vr;
		}
		return null;
	}

}
