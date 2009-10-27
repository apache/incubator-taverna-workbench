package net.sf.taverna.t2.workbench.file.importworkflow;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;

public class DataflowMerger {

	private Edits edits = EditsRegistry.getEdits();

	private final Dataflow dataflow;

	public DataflowMerger() {
		dataflow = edits.createDataflow();
	}

	public DataflowMerger(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

	public void merge(Dataflow source) throws EditException {
		for (Processor processor : source.getProcessors()) {
			edits.getAddProcessorEdit(dataflow, processor).doEdit();
		}
		for (DataflowInputPort input : source.getInputPorts()) {
			DataflowInputPort newInpPort = edits.createDataflowInputPort(input
					.getName(), input.getDepth(),
					input.getGranularInputDepth(), dataflow);
			edits.getAddDataflowInputPortEdit(dataflow, newInpPort).doEdit();
		}
		for (DataflowOutputPort output : source.getOutputPorts()) {
			DataflowOutputPort newOutputPort = edits.createDataflowOutputPort(
					output.getName(), dataflow);
			edits.getAddDataflowOutputPortEdit(dataflow, newOutputPort).doEdit();
		}
	}

}
