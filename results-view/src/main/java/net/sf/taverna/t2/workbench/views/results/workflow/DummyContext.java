package net.sf.taverna.t2.workbench.views.results.workflow;

import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.reference.ReferenceService;

public class DummyContext extends
		InvocationContextImpl {
	public DummyContext(
			ReferenceService referenceService) {
		super(referenceService, null);
	}
}