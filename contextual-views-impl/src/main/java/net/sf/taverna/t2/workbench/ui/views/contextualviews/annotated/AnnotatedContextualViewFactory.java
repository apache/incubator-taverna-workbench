package net.sf.taverna.t2.workbench.ui.views.contextualviews.annotated;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

@SuppressWarnings("unchecked")
public class AnnotatedContextualViewFactory implements
		ContextualViewFactory<Annotated> {

	public boolean canHandle(Object selection) {
		return (selection instanceof Dataflow) || (selection instanceof DataflowInputPort);
	}

	public ContextualView getView(Annotated selection) {
		return new AnnotatedContextualView((Annotated) selection);
	}

}
