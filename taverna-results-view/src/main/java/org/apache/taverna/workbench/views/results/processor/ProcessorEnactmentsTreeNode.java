package org.apache.taverna.workbench.views.results.processor;

import static org.apache.taverna.workbench.views.results.processor.ProcessorEnactmentsTreeModel.iterationToIntegerList;

import java.util.ArrayList;
import java.util.List;
import org.apache.taverna.provenance.lineageservice.utils.ProcessorEnactment;


/**
 * Node in a processor enactments tree. Contains a particular enactment of the
 * processor.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class ProcessorEnactmentsTreeNode extends IterationTreeNode {
	private List<Integer> myIteration = new ArrayList<>();
	private List<Integer> parentIteration = new ArrayList<>();

	public ProcessorEnactmentsTreeNode(ProcessorEnactment processorEnactment,
			List<Integer> parentIteration) {
		super();
		this.parentIteration = parentIteration;
		setProcessorEnactment(processorEnactment);
	}

	protected void updateFullIteration() {
		List<Integer> fullIteration = new ArrayList<>();
		if (getParentIteration() != null)
			fullIteration.addAll(getParentIteration());
		fullIteration.addAll(getMyIteration());
		setIteration(fullIteration);
	}

	public final List<Integer> getMyIteration() {
		return myIteration;
	}

	@Override
	public final List<Integer> getParentIteration() {
		return parentIteration;
	}

	public final ProcessorEnactment getProcessorEnactment() {
		return (ProcessorEnactment) getUserObject();
	}

	public final void setMyIteration(List<Integer> myIteration) {
		this.myIteration = myIteration;
		updateFullIteration();
	}

	public final void setParentIteration(List<Integer> parentIteration) {
		this.parentIteration = parentIteration;
		updateFullIteration();
	}

	public final void setProcessorEnactment(
			ProcessorEnactment processorEnactment) {
		setUserObject(processorEnactment);
		setMyIteration(iterationToIntegerList(processorEnactment.getIteration()));
	}
}
