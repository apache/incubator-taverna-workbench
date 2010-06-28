/**
 * 
 */
package net.sf.taverna.t2.visit.fragility;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
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
		Set<VisitReport> reports = new HashSet<VisitReport>();

		Dataflow d = (Dataflow) ancestry.get(0);
		for (ProcessorInputPort pip : o.getInputPorts()) {
		if (pip.getDepth() == -1) {
		    result = new VisitReport(FragilityCheck.getInstance(), o, "Invalid depth of list", FragilityCheck.INVALID_DEPTH, VisitReport.Status.SEVERE);
		}
		else {
		    int viaMerge = 0;
		    for (Datalink dl : d.getLinks()) {

			if (dl.getSink().equals(pip)) {
			    Port source = dl.getSource();
			    if (source instanceof MergeOutputPort) {
				viaMerge = 1;
			    }
			    break;
			}
		    }
		    for (ProcessorPort sourceProcessorPort : getSourceProcessorPorts(d, pip)) {
			boolean sourceRetries = false;
			Processor sourceProcessor = sourceProcessorPort.getProcessor();
			int resolvedDepth = ((ProcessorOutputPort) sourceProcessorPort).getOutgoingLinks().iterator().next().getResolvedDepth();
			boolean sourceIterates = resolvedDepth != sourceProcessorPort.getDepth();
			List<DispatchLayer<?>> layers = sourceProcessor.getDispatchStack().getLayers();
			for (DispatchLayer l : layers) {
			    if (l instanceof Retry) {
				Retry retry = (Retry) l;
				RetryConfig config = retry.getConfiguration();
				sourceRetries = config.getMaxRetries() != 0;
				break;
			    }
			}
			boolean listCreation = (sourceProcessorPort.getDepth() < pip.getDepth());

			
			if ((sourceIterates || (viaMerge != 0)) && listCreation && !sourceRetries) {
			    VisitReport report = new VisitReport(FragilityCheck.getInstance(), o, "Breaks on single error", FragilityCheck.SOURCE_FRAGILE, VisitReport.Status.WARNING);
			    report.setProperty("sinkPort", pip);
			    report.setProperty("sourceProcessor", sourceProcessor);
			    reports.add(report);
			}
		    }
		}
	    }
		if (reports.isEmpty()) {
		    return null;
		} else if (reports.size() == 1) {
		    return (reports.iterator().next());
		} else {
		    return new VisitReport(FragilityCheck.getInstance(), o, "Breaks on single error", FragilityCheck.SOURCE_FRAGILE, VisitReport.Status.WARNING, reports);
		}
	}

    private Set<ProcessorPort> getSourceProcessorPorts(Dataflow d, Port pip) {
	Set<ProcessorPort> result = new HashSet<ProcessorPort>();
	for (Datalink dl : d.getLinks()) {
	    if (dl.getSink().equals(pip)) {
		Port source = dl.getSource();
		if (source instanceof ProcessorPort) {
		    result.add((ProcessorPort)source);
		} else if (source instanceof MergeOutputPort) {
		    Merge merge = ((MergeOutputPort) source).getMerge();
		    for (MergeInputPort mip : merge.getInputPorts()) {
			result.addAll(getSourceProcessorPorts(d, mip));
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
