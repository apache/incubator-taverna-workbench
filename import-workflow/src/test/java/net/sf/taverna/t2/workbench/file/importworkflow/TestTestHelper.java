package net.sf.taverna.t2.workbench.file.importworkflow;

import org.junit.Test;

public class TestTestHelper extends AbstractTestHelper {

	@Test
	public void checkAbc() throws Exception {
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

	@Test
	public void checkP() throws Exception {
		assertHasProcessors(p, "P");
		assertHasInputPorts(p, "i");
		assertHasOutputPorts(p, "o");
		assertHasDatalinks(p, "i->P.inputlist", "P.outputlist->o");
		assertHasConditionals(p);

	}

}
