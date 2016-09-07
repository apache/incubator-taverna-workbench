/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.activities.disabled.actions;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.taverna.scufl2.api.activity.Activity;

import org.apache.taverna.activities.disabled.views.DisabledConfigView;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.ui.actions.activity.ActivityConfigurationAction;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.utils.Tools;


@SuppressWarnings("serial")
public class DisabledActivityConfigurationAction extends ActivityConfigurationAction {

	public static final String FIX_DISABLED = "Edit properties";
	private final EditManager editManager;
	private final FileManager fileManager;
	private final ReportManager reportManager;

	public DisabledActivityConfigurationAction(Activity activity, Frame owner,
			EditManager editManager, FileManager fileManager, ReportManager reportManager,
			ActivityIconManager activityIconManager, ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super(activity, activityIconManager, serviceDescriptionRegistry);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManager = reportManager;
		putValue(NAME, FIX_DISABLED);
	}

	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		int answer = JOptionPane.showConfirmDialog((Component) e.getSource(),
				"Directly editing properties can be dangerous. Are you sure you want to proceed?",
				"Confirm editing", JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}

		final DisabledConfigView disabledConfigView = new DisabledConfigView(getActivity());
		final DisabledActivityConfigurationDialog dialog = new DisabledActivityConfigurationDialog(
				getActivity(), disabledConfigView);

		ActivityConfigurationAction.setDialog(getActivity(), dialog, fileManager);

	}

	private class DisabledActivityConfigurationDialog extends ActivityConfigurationDialog {
		public DisabledActivityConfigurationDialog(Activity a, DisabledConfigView p) {
			super(a, p, editManager);
			this.setModal(true);
			super.applyButton.setEnabled(false);
			super.applyButton.setVisible(false);
		}

		public void configureActivity(Dataflow df, Activity a, Object bean) {
			Edit<?> configureActivityEdit = editManager.getEdits()
					.getConfigureActivityEdit(a, bean);
			try {
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				editList.add(configureActivityEdit);
				Processor p = findProcessor(df, a);
				if (p != null && p.getActivityList().size() == 1) {
					editList.add(editManager.getEdits().getMapProcessorPortsForActivityEdit(p));
				}
				Edit e = Tools.getEnableDisabledActivityEdit(super.owningProcessor, activity,
						editManager.getEdits());
				if (e != null) {
					editList.add(e);
					editManager.doDataflowEdit(df, new CompoundEdit(editList));
					reportManager.updateObjectReport(super.owningDataflow, super.owningProcessor);

				}
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			} catch (EditException e) {
				logger.error(e);
			}
		}

	}

}
