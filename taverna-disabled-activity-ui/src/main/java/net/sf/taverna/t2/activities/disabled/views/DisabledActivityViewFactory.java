package net.sf.taverna.t2.activities.disabled.views;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import uk.org.taverna.scufl2.api.activity.Activity;

/**
 * This class generates a contextual view for a DisabledActivity
 *
 * @author alanrw
 * @author David Withers
 */
public class DisabledActivityViewFactory implements ContextualViewFactory<Activity> {

	public static final URI ACTIVITY_TYPE = URI.create("http://ns.taverna.org.uk/2010/activity/disabled");

	private EditManager editManager;
	private FileManager fileManager;
	private ReportManager reportManager;
	private ActivityIconManager activityIconManager;
	private ColourManager colourManager;
	private ServiceDescriptionRegistry serviceDescriptionRegistry;

	/**
	 * The factory can handle a DisabledActivity
	 *
	 * @param object
	 * @return
	 */
	public boolean canHandle(Object object) {
		return object instanceof Activity && ((Activity) object).getType().equals(ACTIVITY_TYPE);
	}

	/**
	 * Return a contextual view that can display information about a DisabledActivity
	 *
	 * @param activity
	 * @return
	 */
	public List<ContextualView> getViews(Activity activity) {
		return Arrays.asList(new ContextualView[] { new DisabledContextualView(activity,
				editManager, fileManager, reportManager, colourManager, activityIconManager,
				serviceDescriptionRegistry) });
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

}
