/**
 * 
 */
package net.sf.taverna.t2.visit.fragility;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.RetryConfig;

/**
 * @author alanrw
 *
 */
public class ProcessorFragilityChecker implements FragilityChecker<Processor> {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.annotation.Visitor#canVisit(java.lang.Object)
	 */
	public boolean canVisit(Object o) {
		return o instanceof Processor;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.annotation.Visitor#visit(java.lang.Object, java.util.List)
	 */
	public VisitReport visit(Processor o, List<Object> ancestry) {
		VisitReport result = null;

		Dataflow d = (Dataflow) ancestry.get(0);
		for (ProcessorInputPort pip : o.getInputPorts()) {
		if (pip.getDepth() == -1) {
			result = new VisitReport(FragilityCheck.getInstance(), o, "Invalid depth", FragilityCheck.INVALID_DEPTH, VisitReport.Status.SEVERE);
		}
		else {
			for (Datalink dl : d.getLinks()) {

			if (dl.getSink().equals(pip)) {
				Port source = dl.getSource();
				boolean sourceRetries = false;
				boolean sourceIterates = true;
				boolean listCreation = source.getDepth() < pip.getDepth();
				if (source instanceof ProcessorPort) {
					ProcessorPort processorPort = (ProcessorPort) source;
					sourceIterates = dl.getResolvedDepth() != processorPort.getDepth();
					Processor sourceProcessor = processorPort.getProcessor();
					List<DispatchLayer<?>> layers = sourceProcessor.getDispatchStack().getLayers();
					for (DispatchLayer l : layers) {
						if (l instanceof Retry) {
							Retry retry = (Retry) l;
							RetryConfig config = retry.getConfiguration();
							sourceRetries = config.getMaxRetries() != 0;
						}
					}
				}
				
				if (sourceIterates && listCreation && !sourceRetries) {
					result = new VisitReport(FragilityCheck.getInstance(), o, "A single error element source of the data can prevent the service being invoked", FragilityCheck.SOURCE_FRAGILE, VisitReport.Status.WARNING);
				}
			}
			}
		}
		}
		return result;
	}

	public boolean isTimeConsuming() {
		return false;
	}

}
