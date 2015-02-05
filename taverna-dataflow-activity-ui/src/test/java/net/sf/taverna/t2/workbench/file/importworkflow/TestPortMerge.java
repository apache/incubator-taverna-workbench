package net.sf.taverna.t2.workbench.file.importworkflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.SenderPort;

@Ignore
public class TestPortMerge extends AbstractTestHelper {

	@Test
	public void mergeQintoP() throws Exception {
		DataflowMerger merger = new DataflowMerger(p);
		merger.getMergeEdit(q).doEdit();
		Workflow merged = p;
		checkQ();

		assertHasProcessors(merged, "P", "Q");
		assertHasInputPorts(merged, "i", "p");
		assertHasOutputPorts(merged, "o", "p", "q");
		assertHasDatalinks(merged, "i->P.inputlist", "P.outputlist->o", "p->Q.inputlist",
				"Q.outputlist->q", "p->p");

		List<DataLink> datalinksTo = scufl2Tools.datalinksTo(findOutputPort(merged, "p"));
		assertEquals(1, datalinksTo.size());
		SenderPort source = datalinksTo.get(0).getReceivesFrom();
		assertSame("out port P not linked to input P", source, findInputPort(merged, "p"));

	}

}
