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
package net.sf.taverna.t2.workbench.ui.actions.activity;

import java.util.List;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosingDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

@SuppressWarnings("serial")
public abstract class ActivityConfigurationAction extends AbstractAction {
	private static WeakHashMap<Activity, ActivityConfigurationDialog> configurationDialogs = new WeakHashMap<>();
	private static DataflowCloseListener listener;

	protected Activity activity;
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public ActivityConfigurationAction(Activity activity,
			ActivityIconManager activityIconManager,
			ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.activity = activity;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		putValue(SMALL_ICON,
				activityIconManager.iconForActivity(activity.getType()));
	}

	protected Activity getActivity() {
		return activity;
	}

	protected ServiceDescription getServiceDescription() {
		return serviceDescriptionRegistry.getServiceDescription(activity
				.getType());
	}

	protected static void setDialog(Activity activity,
			ActivityConfigurationDialog dialog, FileManager fileManager) {
		if (listener == null) {
			listener = new DataflowCloseListener();
			/*
			 * Ensure that the DataflowCloseListener is the first notified
			 * listener. Otherwise you cannot save the configurations.
			 */
			List<Observer<FileManagerEvent>> existingListeners = fileManager
					.getObservers();
			fileManager.addObserver(listener);
			for (Observer<FileManagerEvent> observer : existingListeners)
				if (!observer.equals(listener)) {
					fileManager.removeObserver(observer);
					fileManager.addObserver(observer);
				}
		}
		if (configurationDialogs.containsKey(activity)) {
			ActivityConfigurationDialog currentDialog = configurationDialogs
					.get(activity);
			if (!currentDialog.equals(dialog) && currentDialog.isVisible())
				currentDialog.setVisible(false);
		}
		configurationDialogs.put(activity, dialog);
		dialog.setVisible(true);
	}

	public static void clearDialog(Activity activity) {
		if (configurationDialogs.containsKey(activity)) {
			ActivityConfigurationDialog currentDialog = configurationDialogs
					.get(activity);
			if (currentDialog.isVisible())
				currentDialog.setVisible(false);
			configurationDialogs.remove(activity);
			currentDialog.dispose();
		}
	}

	protected static void clearDialog(JDialog dialog) {
		if (configurationDialogs.containsValue(dialog)) {
			if (dialog.isVisible())
				dialog.setVisible(false);
			for (Activity activity : configurationDialogs.keySet())
				if (configurationDialogs.get(activity).equals(dialog))
					configurationDialogs.remove(activity);
			dialog.dispose();
		}
	}

	public static boolean closeDialog(Activity activity) {
		boolean closeIt = true;
		if (configurationDialogs.containsKey(activity)) {
			ActivityConfigurationDialog currentDialog = configurationDialogs
					.get(activity);
			if (currentDialog.isVisible())
				closeIt = currentDialog.closeDialog();
			if (closeIt)
				configurationDialogs.remove(activity);
		}
		return closeIt;
	}

	public static ActivityConfigurationDialog getDialog(Activity activity) {
		return configurationDialogs.get(activity);
	}

	private static class DataflowCloseListener implements
			Observer<FileManagerEvent> {
		private Scufl2Tools scufl2Tools = new Scufl2Tools();

		@Override
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof ClosingDataflowEvent) {
				ClosingDataflowEvent closingDataflowEvent = (ClosingDataflowEvent) message;
				if (closingDataflowEvent.isAbortClose())
					return;
				closingDataflow(closingDataflowEvent,
						((ClosingDataflowEvent) message).getDataflow());
			}
		}

		private void closingDataflow(ClosingDataflowEvent event,
				WorkflowBundle bundle) {
			Profile profile = bundle.getMainProfile();
			for (Workflow workflow : bundle.getWorkflows())
				for (Processor p : workflow.getProcessors()) {
					ProcessorBinding processorBinding = scufl2Tools
							.processorBindingForProcessor(p, profile);
					Activity activity = processorBinding.getBoundActivity();
					if (!closeDialog(activity))
						event.setAbortClose(true);
				}
		}
	}
}
