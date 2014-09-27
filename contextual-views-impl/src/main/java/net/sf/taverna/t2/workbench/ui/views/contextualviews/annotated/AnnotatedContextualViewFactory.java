package net.sf.taverna.t2.workbench.ui.views.contextualviews.annotated;

import static java.util.Collections.singletonList;

import java.util.List;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class AnnotatedContextualViewFactory implements
		ContextualViewFactory<Annotated<?>> {
	private EditManager editManager;
	private List<AnnotationBeanSPI> annotationBeans;
	private SelectionManager selectionManager;

	@Override
	public boolean canHandle(Object selection) {
		return ((selection instanceof Annotated) && !(selection instanceof Activity));
	}

	@Override
	public List<ContextualView> getViews(Annotated<?> selection) {
		return singletonList((ContextualView) new AnnotatedContextualView(
				selection, editManager, selectionManager, annotationBeans));
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setAnnotationBeans(List<AnnotationBeanSPI> annotationBeans) {
		this.annotationBeans = annotationBeans;
	}
}
