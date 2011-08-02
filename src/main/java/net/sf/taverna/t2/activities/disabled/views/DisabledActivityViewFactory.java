package net.sf.taverna.t2.activities.disabled.views;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;

/**
 *
 * This class generates a contextual view for a DisabledActivity
 * @author alanrw
 *
 */
public class DisabledActivityViewFactory implements ContextualViewFactory<DisabledActivity>{

	private EditManager editManager;
	private FileManager fileManager;
	private ReportManager reportManager;

	/**
	 * The factory can handle a DisabledActivity
	 *
	 * @param object
	 * @return
	 */
	public boolean canHandle(Object object) {
		//changed since local worker sub classes beanshell which means instanceof can't be used any more
		return object.getClass().isAssignableFrom(DisabledActivity.class);
	}


	/**
	 * Return a contextual view that can display information about a DisabledActivity
	 *
	 * @param activity
	 * @return
	 */
	public List<ContextualView> getViews(DisabledActivity activity) {
		return Arrays.asList(new ContextualView[] {new DisabledContextualView(activity, editManager, fileManager, reportManager)});
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

}
