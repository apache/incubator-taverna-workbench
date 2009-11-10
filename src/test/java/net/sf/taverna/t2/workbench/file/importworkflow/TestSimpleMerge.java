package net.sf.taverna.t2.workbench.file.importworkflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;

import org.junit.Test;

public class TestSimpleMerge extends AbstractTestHelper {

	private void checkMergedAbcP(Dataflow merged) {
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
	
	private void checkCopiedFromP(Dataflow merged) {
		Processor newProcP = findProcessor(merged, "P");
		Processor originalProcP = findProcessor(p, "P");
		assertNotSame("Did not copy processor P", newProcP, originalProcP);

		ProcessorInputPort inp = newProcP.getInputPorts().get(0);
		DataflowInputPort newInI = findInputPort(merged, "i");
		assertEquals(0, newInI.getDepth());
		assertEquals(0, newInI.getGranularInputDepth());
				
		DataflowInputPort originalInI = findInputPort(p, "i");
		assertNotSame("Did not copy port 'i'", originalInI, newInI);
		assertSame("Not linked to new port", inp.getIncomingLink().getSource(),
				newInI.getInternalOutputPort());
		assertNotSame("Still linked to old port", inp.getIncomingLink().getSource(),
				originalInI.getInternalOutputPort());
	}
	
	
	@Test
	public void mergeAbcAndPIntoNew() throws Exception {
		Edits edit = EditsRegistry.getEdits();
		Dataflow merged = edit.createDataflow();
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
		Dataflow merged = abc;

		merger.getMergeEdit(p).doEdit();
		checkMergedAbcP(merged);
		checkCopiedFromP(merged);
		// Assert P did not change
		checkP();
	}
	
	@Test
	public void mergeAbcintoP() throws Exception {
		Dataflow merged = p;
		DataflowMerger merger = new DataflowMerger(merged);
		merger.getMergeEdit(abc).doEdit();

		checkMergedAbcP(merged);
		// Assert ABC did not change
		checkAbc();
	}

}
