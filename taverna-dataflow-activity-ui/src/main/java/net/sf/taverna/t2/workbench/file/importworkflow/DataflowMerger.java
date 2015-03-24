package net.sf.taverna.t2.workbench.file.importworkflow;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workflow.edits.AddChildEdit;
import net.sf.taverna.t2.workflow.edits.AddDataLinkEdit;
import net.sf.taverna.t2.workflow.edits.AddProcessorEdit;
import net.sf.taverna.t2.workflow.edits.AddWorkflowInputPortEdit;
import net.sf.taverna.t2.workflow.edits.AddWorkflowOutputPortEdit;
import org.apache.taverna.scufl2.api.common.AbstractCloneable;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.ControlLink;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;

/**
 * A tool that allows merging of two workflow.
 * <p>
 * The merge is performed as a series of edit, inserting a copy of the source
 * workflow into the destination workflow.
 *
 * @author Stian Soiland-Reyes
 * @author David Withers
 */
public class DataflowMerger {

	/**
	 * Make a copy of a workflow.
	 *
	 * @param source
	 *            workflow to copy
	 * @return A copy of the workflow.
	 */
	public static Workflow copyWorkflow(Workflow source) {
		WorkflowBundle workflowBundle = AbstractCloneable.cloneWorkflowBean(source.getParent());
		return workflowBundle.getWorkflows().getByName(source.getName());
	}

	private final Workflow destinationWorkflow;

	/**
	 * Construct a {@link DataflowMerger} for the given destination workflow.
	 *
	 * @param destinationWorkflow
	 *            Workflow to be merged into
	 */
	public DataflowMerger(Workflow destinationWorkflow) {
		this.destinationWorkflow = destinationWorkflow;
	}

	/**
	 * Make an {@link Edit} that when performed merges the given source dataflow
	 * into the destination dataflow.
	 * <p>
	 * Internally a copy is made of the source dataflow, to avoid modifying the
	 * links and processors.
	 *
	 * @param sourceDataflow
	 *            Dataflow to merge from
	 * @return An edit that can perform and undo the insertion of the components
	 *         from the source dataflow.
	 * @throws MergeException
	 *             If the merge cannot be performed.
	 */
	public CompoundEdit getMergeEdit(Workflow sourceDataflow)
			throws MergeException {
		return getMergeEdit(sourceDataflow, "");
	}

	/**
	 * Make an {@link Edit} that when performed merges the given source dataflow
	 * into the destination dataflow.
	 * <p>
	 * Internally a copy is made of the source dataflow, to avoid modifying the
	 * links and processors.
	 *
	 * @param sourceWorkflow
	 *            Dataflow to merge from
	 * @param prefix
	 *            A prefix which will be inserted in front of the names for the
	 *            merged workflow components.
	 * @return An edit that can perform and undo the insertion of the components
	 *         from the source dataflow.
	 * @throws MergeException
	 *             If the merge cannot be performed.
	 */
	public CompoundEdit getMergeEdit(Workflow sourceWorkflow, String prefix)
			throws MergeException {
		List<Edit<?>> compoundEdit = new ArrayList<>();

		Workflow workflow = copyWorkflow(sourceWorkflow);

		for (InputWorkflowPort input : workflow.getInputPorts()) {
			destinationWorkflow.getInputPorts().addWithUniqueName(input);
			destinationWorkflow.getInputPorts().remove(input);
			compoundEdit.add(new AddWorkflowInputPortEdit(destinationWorkflow, input));
		}
		for (OutputWorkflowPort output : workflow.getOutputPorts()) {
			destinationWorkflow.getOutputPorts().addWithUniqueName(output);
			destinationWorkflow.getOutputPorts().remove(output);
			compoundEdit.add(new AddWorkflowOutputPortEdit(destinationWorkflow, output));
		}
		for (Processor processor : workflow.getProcessors()) {
			processor.setName(prefix + processor.getName());
			compoundEdit.add(new AddProcessorEdit(destinationWorkflow, processor));
		}
		for (DataLink dataLink : workflow.getDataLinks()) {
			compoundEdit.add(new AddDataLinkEdit(destinationWorkflow, dataLink));
		}
		for (ControlLink controlLink : workflow.getControlLinks()) {
			compoundEdit.add(new AddChildEdit<Workflow>(destinationWorkflow, controlLink));
		}

		return new CompoundEdit(compoundEdit);

	}

}
