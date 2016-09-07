/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.ui.actions.activity;

import java.util.List;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.ClosingDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
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
