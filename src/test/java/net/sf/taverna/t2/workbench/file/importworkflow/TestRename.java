package net.sf.taverna.t2.workbench.file.importworkflow;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

import org.junit.Test;

public class TestRename extends AbstractTestHelper {

	@Test
	public void mergePintoP() throws Exception {
		DataflowMerger merger = new DataflowMerger(p);
		merger.getMergeEdit(p).doEdit();
		Dataflow merged = p;

		assertHasProcessors(merged, "P", "P_2");
		assertHasInputPorts(merged, "i", "i_2");
		assertHasOutputPorts(merged, "o", "o_2");
		assertHasDatalinks(merged, "i->P.inputlist", "P.outputlist->o",
				"i_2->P_2.inputlist", "P_2.outputlist->o_2");
	}

	@Test
	public void mergePintoPintoP() throws Exception {
		// Don't put p in constructor, or we would get exponential merging!	
		Dataflow merged = EditsRegistry.getEdits().createDataflow();
		DataflowMerger merger = new DataflowMerger(merged);
		merger.getMergeEdit(p).doEdit();
		merger.getMergeEdit(p).doEdit();
		merger.getMergeEdit(p).doEdit();

		assertHasProcessors(merged, "P", "P_2", "P_3");
		assertHasInputPorts(merged, "i", "i_2", "i_3");
		assertHasOutputPorts(merged, "o", "o_2", "o_3");
		assertHasDatalinks(merged, "i->P.inputlist", "P.outputlist->o",
				"i_2->P_2.inputlist", "P_2.outputlist->o_2",
				"i_3->P_3.inputlist", "P_3.outputlist->o_3");
	}

	@Test
	public void mergePintoPWithPrefix() throws Exception {
		// Don't put p in constructor, or we would get exponential merging!	
		Dataflow merged = EditsRegistry.getEdits().createDataflow();
		DataflowMerger merger = new DataflowMerger(merged);
		merger.getMergeEdit(p).doEdit();
		merger.getMergeEdit(p, "fish_").doEdit();
		merger.getMergeEdit(p, "soup_").doEdit();

		assertHasProcessors(merged, "P", "fish_P", "soup_P");
		assertHasInputPorts(merged, "i", "fish_i", "soup_i");
		assertHasOutputPorts(merged, "o", "fish_o", "soup_o");
		assertHasDatalinks(merged, "i->P.inputlist", "P.outputlist->o",
				"fish_i->fish_P.inputlist", "fish_P.outputlist->fish_o",
				"soup_i->soup_P.inputlist", "soup_P.outputlist->soup_o");
	}
	
}
