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

public class DataflowMerger {

	private Edits edits = EditsRegistry.getEdits();

	private final Dataflow dataflow;

	public DataflowMerger(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public CompoundEdit getMergeEdit(Dataflow source) throws MergeException {
		return getMergeEdit(source, "");
	}

	private Dataflow copyWorkflow(Dataflow source)
			throws DeserializationException, EditException,
			SerializationException {
		XMLSerializer serialiser = new XMLSerializerImpl();
		XMLDeserializer deserializer = new XMLDeserializerImpl();

		return deserializer.deserializeDataflow(serialiser
				.serializeDataflow(source));

	}

	public CompoundEdit getMergeEdit(Dataflow source, String prefix) throws MergeException {
		List<Edit<?>> compoundEdit = new ArrayList<Edit<?>>();

		try {
			source = copyWorkflow(source);
		} catch (Exception ex) {
			throw new MergeException("Could not copy workflow", ex);
		}

		// Mapping from *old* names to *new* ports
		Map<String, DataflowInputPort> inp = new HashMap<String, DataflowInputPort>();
		Map<String, DataflowOutputPort> outp = new HashMap<String, DataflowOutputPort>();

		for (DataflowInputPort input : source.getInputPorts()) {
			String portName = Tools.uniquePortName(prefix + input.getName(),
					dataflow.getInputPorts());
			DataflowInputPort newInpPort = edits.createDataflowInputPort(
					portName, input.getDepth(), input.getGranularInputDepth(),
					dataflow);
			inp.put(input.getName(), newInpPort);

			compoundEdit.add(edits.getAddDataflowInputPortEdit(dataflow,
					newInpPort));

		}
		for (DataflowOutputPort output : source.getOutputPorts()) {
			String portName = Tools.uniquePortName(prefix + output.getName(),
					dataflow.getOutputPorts());
			DataflowOutputPort newOutputPort = edits.createDataflowOutputPort(
					portName, dataflow);
			outp.put(output.getName(), newOutputPort);
			compoundEdit.add(edits.getAddDataflowOutputPortEdit(dataflow,
					newOutputPort));
		}

		for (Processor processor : source.getProcessors()) {
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
		for (Merge merge : source.getMerges()) {
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
		for (DataflowInputPort inputPort : source.getInputPorts()) {
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
		for (DataflowOutputPort outputPort : source.getOutputPorts()) {
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
