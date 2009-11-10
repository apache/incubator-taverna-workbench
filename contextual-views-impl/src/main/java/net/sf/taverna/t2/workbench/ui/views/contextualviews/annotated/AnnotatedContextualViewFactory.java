package net.sf.taverna.t2.workbench.ui.views.contextualviews.annotated;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;

@SuppressWarnings("unchecked")
public class AnnotatedContextualViewFactory implements
		ContextualViewFactory<Annotated> {

	public boolean canHandle(Object selection) {
		return (selection instanceof Dataflow) ||
		(selection instanceof DataflowInputPort)  ||
		(selection instanceof DataflowOutputPort);
	}

	public List<ContextualView> getViews(Annotated selection) {
		return Arrays.asList(new ContextualView[] {new AnnotatedContextualView((Annotated) selection)});
	}

}
