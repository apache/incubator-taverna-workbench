package net.sf.taverna.t2.activities.unrecognized.views;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.UnrecognizedActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

/**
 * 
 * This class generates a contextual view for a UnrecognizedActivity
 * @author alanrw
 *
 */
public class UnrecognizedActivityViewFactory implements ContextualViewFactory<UnrecognizedActivity>{

	/**
	 * The factory can handle a UnrecognizedActivity
	 * 
	 * @param object
	 * @return
	 */
	public boolean canHandle(Object object) {
		return object.getClass().isAssignableFrom(UnrecognizedActivity.class);
	}

	
	/**
	 * Return a contextual view that can display information about a UnrecognizedActivity
	 * 
	 * @param activity
	 * @return
	 */
	public List<ContextualView> getViews(UnrecognizedActivity activity) {
		return Arrays.asList(new ContextualView[] {new UnrecognizedContextualView(activity)});
	}

}
