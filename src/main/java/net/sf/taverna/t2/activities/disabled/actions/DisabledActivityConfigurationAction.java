/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.disabled.actions;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.disabled.views.DisabledConfigView;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.report.impl.ReportManagerImpl;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

@SuppressWarnings("serial")
public class DisabledActivityConfigurationAction extends ActivityConfigurationAction<DisabledActivity, ActivityAndBeanWrapper>{

	public static final String FIX_DISABLED = "Edit properties";
	private final EditManager editManager;
	private final FileManager fileManager;
	private final ReportManager reportManager;

	public DisabledActivityConfigurationAction(DisabledActivity activity, Frame owner, EditManager editManager, FileManager fileManager, ReportManager reportManager) {
		super(activity);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManager = reportManager;
		putValue(NAME, FIX_DISABLED);
	}

	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog currentDialog = ActivityConfigurationAction.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		int answer = JOptionPane.showConfirmDialog((Component) e.getSource(),
							   "Directly editing properties can be dangerous. Are you sure you want to proceed?",
							   "Confirm editing",
							   JOptionPane.YES_NO_OPTION);
		if (answer != JOptionPane.YES_OPTION) {
		    return;
		}

		final DisabledConfigView disabledConfigView = new DisabledConfigView((DisabledActivity)getActivity());
		final DisabledActivityConfigurationDialog dialog =
			new DisabledActivityConfigurationDialog(getActivity(), disabledConfigView);

		ActivityConfigurationAction.setDialog(getActivity(), dialog, fileManager);

	}


	private class DisabledActivityConfigurationDialog extends ActivityConfigurationDialog<DisabledActivity, ActivityAndBeanWrapper> {
	    public DisabledActivityConfigurationDialog(DisabledActivity a, DisabledConfigView p) {
		super (a, p, editManager, fileManager);
		this.setModal(true);
		super.applyButton.setEnabled(false);
		super.applyButton.setVisible(false);
	    }


	public void configureActivity(Dataflow df, Activity a, Object bean) {
		Edit<?> configureActivityEdit = editManager.getEdits().getConfigureActivityEdit(a, bean);
		try {
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			editList.add(configureActivityEdit);
			Processor p = findProcessor(df, a);
			if (p != null && p.getActivityList().size() == 1) {
				editList.add(editManager.getEdits().getMapProcessorPortsForActivityEdit(p));
			}
			Edit e = Tools.getEnableDisabledActivityEdit(super.owningProcessor, activity, editManager.getEdits());
			if (e != null) {
			    editList.add(e);
			    editManager.doDataflowEdit(df,
								     new CompoundEdit(editList));
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
