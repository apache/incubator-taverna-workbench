package net.sf.taverna.t2.activities.disabled.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.disabled.actions.DisabledActivityConfigurationAction;
import net.sf.taverna.t2.activities.disabled.views.DisabledActivityViewFactory;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;

public class ConfigureDisabledMenuAction extends AbstractConfigureActivityMenuAction {

	private EditManager editManager;
	private FileManager fileManager;
	private ReportManager reportManager;
	private ActivityIconManager activityIconManager;
	private ServiceDescriptionRegistry serviceDescriptionRegistry;

	public ConfigureDisabledMenuAction() {
		super(DisabledActivityViewFactory.ACTIVITY_TYPE);
	}

	@Override
	protected Action createAction() {
		return new DisabledActivityConfigurationAction(findActivity(), getParentFrame(),
				editManager, fileManager, reportManager, activityIconManager, serviceDescriptionRegistry);
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

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

}
