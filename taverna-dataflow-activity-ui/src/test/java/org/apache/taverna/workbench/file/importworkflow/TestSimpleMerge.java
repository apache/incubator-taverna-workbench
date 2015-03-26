package org.apache.taverna.workbench.file.importworkflow;

import org.apache.taverna.workbench.file.importworkflow.DataflowMerger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.SenderPort;

@Ignore
public class TestSimpleMerge extends AbstractTestHelper {

	private void checkMergedAbcP(Workflow merged) {
		// Check that it has everything from both
		assertHasProcessors(merged, "A", "B", "C", "P");
		assertHasInputPorts(merged, "in1", "in2", "i");
		assertHasOutputPorts(merged, "a", "b", "c", "o");
		assertHasDatalinks(merged, "in2->B.inputlist", "in1->A.string1",
				"in2->A.string2", "Merge0:Merge0_output->C.inputlist",
				"A.output->a", "B.outputlist->b",
				"B.outputlist->Merge0:outputlistToMerge0_input0",
				"A.output->Merge0:outputToMerge0_input0", "C.outputlist->c",
				"i->P.inputlist", "P.outputlist->o");
		assertHasConditionals(merged, "A;B");
	}

	private void checkCopiedFromP(Workflow merged) {
		Processor newProcP = findProcessor(merged, "P");
		Processor originalProcP = findProcessor(p, "P");
		assertNotSame("Did not copy processor P", newProcP, originalProcP);

		InputProcessorPort inp = newProcP.getInputPorts().first();
		InputWorkflowPort newInI = findInputPort(merged, "i");
		assertEquals(0, newInI.getDepth().intValue());

		InputWorkflowPort originalInI = findInputPort(p, "i");
		assertNotSame("Did not copy port 'i'", originalInI, newInI);

		List<DataLink> datalinksTo = scufl2Tools.datalinksTo(inp);
		assertEquals(1, datalinksTo.size());
		SenderPort source = datalinksTo.get(0).getReceivesFrom();

		assertSame("Not linked to new port", source, newInI);
		assertNotSame("Still linked to old port", source, originalInI);
	}


	@Test
	public void mergeAbcAndPIntoNew() throws Exception {
		Workflow merged = new Workflow();
		DataflowMerger merger = new DataflowMerger(merged);
		merger.getMergeEdit(abc).doEdit();

		assertNotSame(abc, merged);
		merger.getMergeEdit(p).doEdit();


		// Assert abc and p were not modified
		checkAbc();
		checkP();

		checkMergedAbcP(merged);
		checkCopiedFromP(merged);
	}

	@Test
	public void mergePintoAbc() throws Exception {
		DataflowMerger merger = new DataflowMerger(abc);
		Workflow merged = abc;

		merger.getMergeEdit(p).doEdit();
		checkMergedAbcP(merged);
		checkCopiedFromP(merged);
		// Assert P did not change
		checkP();
	}

	@Test
	public void mergeAbcintoP() throws Exception {
		Workflow merged = p;
		DataflowMerger merger = new DataflowMerger(merged);
		merger.getMergeEdit(abc).doEdit();

		checkMergedAbcP(merged);
		// Assert ABC did not change
		checkAbc();
	}

}
