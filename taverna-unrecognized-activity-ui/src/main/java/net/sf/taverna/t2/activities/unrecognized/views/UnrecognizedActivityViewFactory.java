package net.sf.taverna.t2.activities.unrecognized.views;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import uk.org.taverna.scufl2.api.activity.Activity;

/**
 * This class generates a contextual view for a UnrecognizedActivity
 *
 * @author alanrw
 */
public class UnrecognizedActivityViewFactory implements ContextualViewFactory<Activity> {

	public static final URI ACTIVITY_TYPE = URI.create("http://ns.taverna.org.uk/2010/activity/unrecognized");

	private ColourManager colourManager;

	/**
	 * The factory can handle a UnrecognizedActivity
	 *
	 * @param object
	 * @return
	 */
	public boolean canHandle(Object object) {
		return object instanceof Activity && ((Activity) object).getType().equals(ACTIVITY_TYPE);
	}

	/**
	 * Return a contextual view that can display information about a UnrecognizedActivity
	 *
	 * @param activity
	 * @return
	 */
	public List<ContextualView> getViews(Activity activity) {
		return Arrays.asList(new ContextualView[] { new UnrecognizedContextualView(activity,
				colourManager) });
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

}
