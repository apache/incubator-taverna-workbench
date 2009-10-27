package net.sf.taverna.t2.workbench.file.importworkflow;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestNaiveMerge extends AbstractTestHelper {

	@Test
	public void testname() throws Exception {
		DataflowMerger merger = new DataflowMerger(abc);
		assertEquals(abc, merger.getDataflow());
		
		merger.merge(p);
		assertEquals(abc, merger.getDataflow());

		// Check that it has everything from both
		assertHasProcessors(abc, "A", "B", "C", "P");
		assertHasInputPorts(abc, "in1", "in2", "i");
		assertHasOutputPorts(abc, "a", "b", "c", "o");
		assertHasDatalinks(abc, "in2->B.inputlist", "in1->A.string1",
				"in2->A.string2", "Merge0:Merge0_output->C.inputlist",
				"A.output->a", "B.outputlist->b",
				"B.outputlist->Merge0:outputlistToMerge0_input0",
				"A.output->Merge0:outputToMerge0_input0", "C.outputlist->c",
				"i->P.inputlist"
				//, "P.outputlist->o"
				);
		// FIXME: P.outputlist->o should also be included - need to modify links
		// to reflect new dataflow input and output ports
		
		// TODO: Test that links are referring to processors and ports in current workflow
		assertHasConditionals(abc, "A;B");
	}

	
}
