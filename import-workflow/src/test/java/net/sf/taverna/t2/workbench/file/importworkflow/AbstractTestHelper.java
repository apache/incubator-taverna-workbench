package net.sf.taverna.t2.workbench.file.importworkflow;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractTestHelper {

	private static final String ABC_T2FLOW = "/abc.t2flow";

	private static final String P_T2FLOW = "/p.t2flow";

	protected Dataflow abc;

	protected Dataflow p;

	protected void assertHasDatalinks(Dataflow dataflow,
			String... expectedLinkDef) {
		Set<String> expectedLinks = new HashSet<String>();
		for (String expected : expectedLinkDef) {
			expectedLinks.add(expected);
		}

		Set<String> foundLinks = new HashSet<String>();

		for (Datalink link : dataflow.getLinks()) {
			StringBuilder linkRef = new StringBuilder();
			EventForwardingOutputPort source = link.getSource();
			if (source instanceof ProcessorPort) {
				linkRef.append(((ProcessorPort) source).getProcessor()
						.getLocalName());
				linkRef.append('.');
			} else if (source instanceof MergePort) {
				MergePort mergePort = (MergePort) source;
				linkRef.append(mergePort.getMerge().getLocalName());
				linkRef.append(':'); // : indicates merge ..
			}
			linkRef.append(source.getName());

			linkRef.append("->");

			EventHandlingInputPort sink = link.getSink();
			if (sink instanceof ProcessorPort) {
				linkRef.append(((ProcessorPort) sink).getProcessor()
						.getLocalName());
				linkRef.append('.');
			} else if (sink instanceof MergePort) {
				MergePort mergePort = (MergePort) sink;
				linkRef.append(mergePort.getMerge().getLocalName());
				linkRef.append(':');
			}
			linkRef.append(sink.getName());

			String linkStr = linkRef.toString();
			foundLinks.add(linkStr);
		}

		Set<String> extras = new HashSet<String>(foundLinks);
		extras.removeAll(expectedLinks);
		assertTrue("Unexpected links  " + extras, extras.isEmpty());

		Set<String> missing = new HashSet<String>(expectedLinks);
		missing.removeAll(foundLinks);
		assertTrue("Could not find links  " + missing, missing.isEmpty());
	}

	protected void assertHasConditionals(Dataflow dataflow,
			String... expectedConditionalDef) {
		Set<String> expectedConditionals = new HashSet<String>();
		for (String expected : expectedConditionalDef) {
			expectedConditionals.add(expected);
		}

		Set<String> foundConditionals = new HashSet<String>();

		for (Processor p : dataflow.getProcessors()) {
			for (Condition c : p.getPreconditionList()) {
				foundConditionals.add(c.getControl().getLocalName() + ";"
						+ c.getTarget().getLocalName());
			}
		}

		Set<String> extras = new HashSet<String>(foundConditionals);
		extras.removeAll(expectedConditionals);
		assertTrue("Unexpected conditional  " + extras, extras.isEmpty());

		Set<String> missing = new HashSet<String>(expectedConditionals);
		missing.removeAll(foundConditionals);
		assertTrue("Could not find conditional  " + missing, missing.isEmpty());
	}

	protected void assertHasInputPorts(Dataflow dataflow,
			String... expectedInputPorts) {
		Set<String> expectedNames = new HashSet<String>();
		for (String expected : expectedInputPorts) {
			expectedNames.add(expected);
		}
		Set<String> foundNames = new HashSet<String>();
		for (Port port : dataflow.getInputPorts()) {
			String name = port.getName();
			foundNames.add(name);
		}

		Set<String> extras = new HashSet<String>(foundNames);
		extras.removeAll(expectedNames);
		assertTrue("Unexpected input port  " + extras, extras.isEmpty());

		Set<String> missing = new HashSet<String>(expectedNames);
		missing.removeAll(foundNames);
		assertTrue("Could not find input port  " + missing, missing.isEmpty());

	}

	protected void assertHasOutputPorts(Dataflow dataflow,
			String... expectedOutputPorts) {
		Set<String> expectedNames = new HashSet<String>();
		for (String expected : expectedOutputPorts) {
			expectedNames.add(expected);
		}
		Set<String> foundNames = new HashSet<String>();
		for (Port port : dataflow.getOutputPorts()) {
			String name = port.getName();
			foundNames.add(name);
		}

		Set<String> extras = new HashSet<String>(foundNames);
		extras.removeAll(expectedNames);
		assertTrue("Unexpected output port  " + extras, extras.isEmpty());

		Set<String> missing = new HashSet<String>(expectedNames);
		missing.removeAll(foundNames);
		assertTrue("Could not find output port  " + missing, missing.isEmpty());
	}

	protected void assertHasProcessors(Dataflow dataflow,
			String... expectedProcessors) {
		Set<String> expectedNames = new HashSet<String>();
		for (String expected : expectedProcessors) {
			expectedNames.add(expected);
		}
		Set<String> foundNames = new HashSet<String>();

		for (Processor proc : dataflow.getProcessors()) {
			String processorName = proc.getLocalName();
			foundNames.add(processorName);
		}

		Set<String> extras = new HashSet<String>(foundNames);
		extras.removeAll(expectedNames);
		assertTrue("Unexpected processor  " + extras, extras.isEmpty());

		Set<String> missing = new HashSet<String>(expectedNames);
		missing.removeAll(foundNames);
		assertTrue("Could not find processor  " + missing, missing.isEmpty());
	}

	@Before
	public void loadWorkflows() throws Exception {
		abc = loadAbc();
		p = loadP();
	}

	protected Dataflow loadP() throws JDOMException, IOException,
			DeserializationException, EditException {
		return openWorkflow(getClass().getResourceAsStream(P_T2FLOW));
	}

	protected Dataflow loadAbc() throws JDOMException, IOException,
			DeserializationException, EditException {
		return openWorkflow(getClass().getResourceAsStream(ABC_T2FLOW));
	}

	protected Dataflow openWorkflow(InputStream workflowXMLstream)
			throws JDOMException, IOException, DeserializationException,
			EditException {
		assertNotNull(workflowXMLstream);
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		SAXBuilder builder = new SAXBuilder();
		Document document;
		document = builder.build(workflowXMLstream);

		Dataflow dataflow;
		dataflow = deserializer.deserializeDataflow(document.getRootElement());
		return dataflow;
	}

}
