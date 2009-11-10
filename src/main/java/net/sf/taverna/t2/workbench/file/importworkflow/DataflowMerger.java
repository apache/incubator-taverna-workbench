package net.sf.taverna.t2.workbench.file.importworkflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * A tool that allows merging of two workflow.
 * <p>
 * The merge is performed as a series of edit, inserting a copy of the source
 * workflow into the destination workflow.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class DataflowMerger {

	private static Edits edits = EditsRegistry.getEdits();

	/**
	 * Make a copy of a dataflow by serializing and deserializing.
	 * 
	 * @param source
	 *            Dataflow to copy
	 * @return A copy of the dataflow.
	 * @throws DeserializationException
	 *             If a copy could not be made
	 * @throws EditException
	 *             If a copy could not be made
	 * @throws SerializationException
	 *             If a copy could not be made
	 */
	public static Dataflow copyWorkflow(Dataflow source)
			throws DeserializationException, EditException,
			SerializationException {
		XMLSerializer serialiser = new XMLSerializerImpl();
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		return deserializer.deserializeDataflow(serialiser
				.serializeDataflow(source));
	}

	private final Dataflow dataflow;

	/**
	 * Construct a {@link DataflowMerger} for the given destination dataflow.
	 * 
	 * @param destinationDataflow
	 *            Dataflow to be merged into
	 */
	public DataflowMerger(Dataflow destinationDataflow) {
		this.dataflow = destinationDataflow;
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
	public CompoundEdit getMergeEdit(Dataflow sourceDataflow)
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
	 * @param sourceDataflow
	 *            Dataflow to merge from
	 * @param prefix
	 *            A prefix which will be inserted in front of the names for the
	 *            merged workflow components.
	 * @return An edit that can perform and undo the insertion of the components
	 *         from the source dataflow.
	 * @throws MergeException
	 *             If the merge cannot be performed.
	 */
	public CompoundEdit getMergeEdit(Dataflow sourceDataflow, String prefix)
			throws MergeException {
		List<Edit<?>> compoundEdit = new ArrayList<Edit<?>>();

		try {
			sourceDataflow = copyWorkflow(sourceDataflow);
		} catch (Exception ex) {
			throw new MergeException("Could not copy workflow", ex);
		}

		// Mapping from *old* names to *new* ports
		Map<String, DataflowInputPort> inp = new HashMap<String, DataflowInputPort>();
		Map<String, DataflowOutputPort> outp = new HashMap<String, DataflowOutputPort>();

		for (DataflowInputPort input : sourceDataflow.getInputPorts()) {
			String portName = Tools.uniquePortName(prefix + input.getName(),
					dataflow.getInputPorts());
			DataflowInputPort newInpPort = edits.createDataflowInputPort(
					portName, input.getDepth(), input.getGranularInputDepth(),
					dataflow);
			inp.put(input.getName(), newInpPort);

			compoundEdit.add(edits.getAddDataflowInputPortEdit(dataflow,
					newInpPort));

		}
		for (DataflowOutputPort output : sourceDataflow.getOutputPorts()) {
			String portName = Tools.uniquePortName(prefix + output.getName(),
					dataflow.getOutputPorts());
			DataflowOutputPort newOutputPort = edits.createDataflowOutputPort(
					portName, dataflow);
			outp.put(output.getName(), newOutputPort);
			compoundEdit.add(edits.getAddDataflowOutputPortEdit(dataflow,
					newOutputPort));
		}

		for (Processor processor : sourceDataflow.getProcessors()) {
			String originalName = processor.getLocalName();
			String processorName = Tools.uniqueProcessorName(prefix
					+ originalName, dataflow);
			try {
				if (!processorName.equals(originalName)) {
					edits.getRenameProcessorEdit(processor, processorName)
							.doEdit();
				}
			} catch (EditException e) {
				throw new MergeException("Could not copy processor "
						+ originalName, e);
			}
			compoundEdit.add(edits.getAddProcessorEdit(dataflow, processor));
		}
		for (Merge merge : sourceDataflow.getMerges()) {
			String originalName = merge.getLocalName();
			String mergeName = Tools.uniqueProcessorName(prefix + originalName,
					dataflow);
			try {
				if (!originalName.equals(mergeName)) {
					edits.getRenameMergeEdit(merge, mergeName).doEdit();
				}
			} catch (EditException e) {
				throw new MergeException("Could not copy merge "
						+ merge.getLocalName(), e);
			}
			compoundEdit.add(edits.getAddMergeEdit(dataflow, merge));
		}

		// Re-map connections from input and output ports
		for (DataflowInputPort inputPort : sourceDataflow.getInputPorts()) {
			try {
				Set<? extends Datalink> outgoingLinks = inputPort
						.getInternalOutputPort().getOutgoingLinks();
				for (Datalink outgoingLink : new HashSet<Datalink>(
						outgoingLinks)) {
					EventHandlingInputPort originalSink = outgoingLink
							.getSink();
					edits.getDisconnectDatalinkEdit(outgoingLink).doEdit();

					EventForwardingOutputPort newSource = inp.get(
							inputPort.getName()).getInternalOutputPort();
					Datalink newLink = edits.createDatalink(newSource,
							originalSink);
					edits.getConnectDatalinkEdit(newLink).doEdit();
				}
			} catch (EditException e) {
				throw new MergeException("Could not remap port for input port "
						+ inputPort.getName());
			}
		}
		for (DataflowOutputPort outputPort : sourceDataflow.getOutputPorts()) {
			try {
				Datalink incomingLink = outputPort.getInternalInputPort()
						.getIncomingLink();

				EventForwardingOutputPort originalSource = incomingLink
						.getSource();

				edits.getDisconnectDatalinkEdit(incomingLink).doEdit();

				EventHandlingInputPort newSink = outp.get(outputPort.getName())
						.getInternalInputPort();
				Datalink newLink = edits
						.createDatalink(originalSource, newSink);
				edits.getConnectDatalinkEdit(newLink).doEdit();

			} catch (EditException e) {
				throw new MergeException(
						"Could not remap port for output port "
								+ outputPort.getName());
			}
		}
		return new CompoundEdit(compoundEdit);

	}

}
