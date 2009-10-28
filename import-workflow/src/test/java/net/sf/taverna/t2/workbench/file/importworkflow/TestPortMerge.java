package net.sf.taverna.t2.workbench.file.importworkflow;

import static org.junit.Assert.*;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;

import org.junit.Test;

public class TestPortMerge extends AbstractTestHelper {

	@Test
	public void mergeQintoP() throws Exception {
		DataflowMerger merger = new DataflowMerger(p);
		merger.merge(q);
		Dataflow merged = merger.getDataflow();
		checkQ();

		assertHasProcessors(merged, "P", "Q");
		assertHasInputPorts(merged, "i", "p");
		assertHasOutputPorts(merged, "o", "p", "q");
		assertHasDatalinks(merged, "i->P.inputlist", "P.outputlist->o",
				"p->Q.inputlist", "Q.outputlist->q", "p->p");
		EventForwardingOutputPort source = findOutputPort(merged, "p")
				.getInternalInputPort().getIncomingLink().getSource();
		assertEquals("out port P not linked to input P", source, findInputPort(
				merged, "p").getInternalOutputPort());

	}

}
