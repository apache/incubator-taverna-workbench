package net.sf.taverna.t2.workbench.file.importworkflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;

import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;
import uk.org.taverna.scufl2.api.core.ControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.port.ProcessorPort;
import uk.org.taverna.scufl2.api.port.ReceiverPort;
import uk.org.taverna.scufl2.api.port.SenderPort;

public abstract class AbstractTestHelper {

	private static final String Q_T2FLOW = "/q.t2flow";

	private static final String ABC_T2FLOW = "/abc.t2flow";

	private static final String P_T2FLOW = "/p.t2flow";

	private WorkflowBundleIO workflowBundleIO = new WorkflowBundleIO();

	protected Scufl2Tools scufl2Tools = new Scufl2Tools();

	protected Workflow abc;

	protected Workflow p;

	protected Workflow q;

	protected void assertHasConditionals(Workflow dataflow,
			String... expectedConditionalDef) {
		Set<String> expectedConditionals = new HashSet<String>();
		for (String expected : expectedConditionalDef) {
			expectedConditionals.add(expected);
		}

		Set<String> foundConditionals = new HashSet<String>();

		for (ControlLink c : dataflow.getControlLinks()) {
			if (c instanceof BlockingControlLink) {
			BlockingControlLink bcl = (BlockingControlLink) c;
			foundConditionals.add(bcl.getUntilFinished().getName() + ";"
					+ bcl.getBlock().getName());
			}
		}

		Set<String> extras = new HashSet<String>(foundConditionals);
		extras.removeAll(expectedConditionals);
		assertTrue("Unexpected conditional  " + extras, extras.isEmpty());

		Set<String> missing = new HashSet<String>(expectedConditionals);
		missing.removeAll(foundConditionals);
		assertTrue("Could not find conditional  " + missing, missing.isEmpty());
	}

	protected void assertHasDatalinks(Workflow dataflow,
			String... expectedLinkDef) {
		Set<String> expectedLinks = new HashSet<String>();
		for (String expected : expectedLinkDef) {
			expectedLinks.add(expected);
		}

		Set<String> foundLinks = new HashSet<String>();

		for (DataLink link : dataflow.getDataLinks()) {
			StringBuilder linkRef = new StringBuilder();
			SenderPort source = link.getReceivesFrom();
			if (source instanceof ProcessorPort) {
				linkRef.append(((ProcessorPort) source).getParent()
						.getName());
				linkRef.append('.');
			}
			linkRef.append(source.getName());

			linkRef.append("->");

			ReceiverPort sink = link.getSendsTo();
			if (sink instanceof ProcessorPort) {
				linkRef.append(((ProcessorPort) sink).getParent()
						.getName());
				linkRef.append('.');
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

	protected void assertHasInputPorts(Workflow dataflow,
			String... expectedInputPorts) {
		Set<String> expectedNames = new HashSet<String>();
		for (String expected : expectedInputPorts) {
			expectedNames.add(expected);
		}
		Set<String> foundNames = new HashSet<String>();
		for (InputWorkflowPort port : dataflow.getInputPorts()) {
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

	protected void assertHasOutputPorts(Workflow dataflow,
			String... expectedOutputPorts) {
		Set<String> expectedNames = new HashSet<String>();
		for (String expected : expectedOutputPorts) {
			expectedNames.add(expected);
		}
		Set<String> foundNames = new HashSet<String>();
		for (OutputWorkflowPort port : dataflow.getOutputPorts()) {
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

	protected void assertHasProcessors(Workflow dataflow,
			String... expectedProcessors) {
		Set<String> expectedNames = new HashSet<String>();
		for (String expected : expectedProcessors) {
			expectedNames.add(expected);
		}
		Set<String> foundNames = new HashSet<String>();

		for (Processor proc : dataflow.getProcessors()) {
			String processorName = proc.getName();
			foundNames.add(processorName);
		}

		Set<String> extras = new HashSet<String>(foundNames);
		extras.removeAll(expectedNames);
		assertTrue("Unexpected processor  " + extras, extras.isEmpty());

		Set<String> missing = new HashSet<String>(expectedNames);
		missing.removeAll(foundNames);
		assertTrue("Could not find processor  " + missing, missing.isEmpty());
	}

	protected void checkAbc() throws Exception {
		assertHasProcessors(abc, "A", "B", "C");
		assertHasInputPorts(abc, "in1", "in2");
		assertHasOutputPorts(abc, "a", "b", "c");
		assertHasDatalinks(abc, "in2->B.inputlist", "in1->A.string1",
				"in2->A.string2", "Merge0:Merge0_output->C.inputlist",
				"A.output->a", "B.outputlist->b",
				"B.outputlist->Merge0:outputlistToMerge0_input0",
				"A.output->Merge0:outputToMerge0_input0", "C.outputlist->c");
		assertHasConditionals(abc, "A;B");
	}

	protected void checkP() throws Exception {
		assertHasProcessors(p, "P");
		assertHasInputPorts(p, "i");
		assertHasOutputPorts(p, "o");
		assertHasDatalinks(p, "i->P.inputlist", "P.outputlist->o");
		assertHasConditionals(p);

	}

	protected void checkQ() throws Exception {
		assertHasProcessors(q, "Q");
		assertHasInputPorts(q, "p");
		assertHasOutputPorts(q, "p", "q");
		assertHasDatalinks(q, "p->Q.inputlist", "Q.outputlist->q", "p->p");
		assertHasConditionals(q);

		List<DataLink> datalinksTo = scufl2Tools.datalinksTo(findOutputPort(q, "p"));
		assertEquals(1, datalinksTo.size());
		SenderPort source = datalinksTo.get(0).getReceivesFrom();
		assertEquals("out port P not linked to input P", source, findInputPort(q, "p"));

	}

	protected Workflow loadAbc() throws Exception {
		return openWorkflow(getClass().getResourceAsStream(ABC_T2FLOW));
	}

	protected Workflow loadP() throws Exception {
		return openWorkflow(getClass().getResourceAsStream(P_T2FLOW));
	}

	protected Workflow loadQ() throws Exception {
		return openWorkflow(getClass().getResourceAsStream(Q_T2FLOW));
	}

	@Before
	public void loadWorkflows() throws Exception {
		abc = loadAbc();
		p = loadP();
		q = loadQ();
	}

	protected Workflow openWorkflow(InputStream workflowXMLstream) throws Exception {
		assertNotNull(workflowXMLstream);
		WorkflowBundle workflowBundle = workflowBundleIO.readBundle(workflowXMLstream, "application/vnd.taverna.t2flow+xml");
		return workflowBundle.getMainWorkflow();
	}

	protected InputWorkflowPort findInputPort(Workflow wf, String name) {
		for (InputWorkflowPort inp : wf.getInputPorts()) {
			if (inp.getName().equals(name)) {
				return inp;
			}
		}
		throw new IllegalArgumentException("Unknown input port: " + name);
	}

	protected OutputWorkflowPort findOutputPort(Workflow wf, String name) {
		for (OutputWorkflowPort outp : wf.getOutputPorts()) {
			if (outp.getName().equals(name)) {
				return outp;
			}
		}
		throw new IllegalArgumentException("Unknown output port: " + name);
	}

	protected Processor findProcessor(Workflow wf, String name) {
		for (Processor proc : wf.getProcessors()) {
			if (proc.getName().equals(name)) {
				return proc;
			}
		}
		throw new IllegalArgumentException("Unknown processor: " + name);
	}

}
