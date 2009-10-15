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

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.ClosingDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

import org.apache.log4j.Logger;

public abstract class ActivityConfigurationAction<A extends Activity<ConfigurationBean>, ConfigurationBean>
		extends AbstractAction {

	private static WeakHashMap<Activity, ActivityConfigurationDialog> configurationDialogs = new WeakHashMap<Activity, ActivityConfigurationDialog>();
	
	
	private static Logger logger = Logger
			.getLogger(ActivityConfigurationAction.class);

	protected A activity;
	
	private static DataflowCloseListener listener;

	public ActivityConfigurationAction(A activity) {
		this.activity = activity;
		putValue(SMALL_ICON, ActivityIconManager.getInstance().iconForActivity(activity));
	}

	protected A getActivity() {
		return activity;
	}


	protected static void setDialog(Activity activity, ActivityConfigurationDialog dialog) {
		if (listener == null) {
			listener = new DataflowCloseListener();
			// Ensure that the DataflowCloseListener is the first notified listener.  Otherwise you cannot save the configurations.
			List<Observer<FileManagerEvent>> existingListeners = FileManager.getInstance().getObservers();			
			FileManager.getInstance().addObserver(listener);
			for (Observer<FileManagerEvent> observer : existingListeners) {
				if (!observer.equals(listener)) {
					FileManager.getInstance().removeObserver(observer);
					FileManager.getInstance().addObserver(observer);
				}
			}
		}
		if (configurationDialogs.containsKey(activity)) {
			ActivityConfigurationDialog currentDialog = configurationDialogs.get(activity);
			if (!currentDialog.equals(dialog)) {
				if (currentDialog.isVisible()) {
					currentDialog.setVisible(false);
				}
			}
		}
		configurationDialogs.put(activity, dialog);
		dialog.setVisible(true);
	}
	
	public static void clearDialog(Activity activity) {
		if (configurationDialogs.containsKey(activity)) {
			ActivityConfigurationDialog currentDialog = configurationDialogs.get(activity);
			if (currentDialog.isVisible()) {
				currentDialog.setVisible(false);
			}
			configurationDialogs.remove(activity);
			currentDialog.dispose();
		}	
	}
	
	protected static void clearDialog(JDialog dialog) {
		if (configurationDialogs.containsValue(dialog)) {
			if (dialog.isVisible()) {
				dialog.setVisible(false);
			}
			for (Activity activity : configurationDialogs.keySet()) {
				if (configurationDialogs.get(activity).equals(dialog)) {
					configurationDialogs.remove(activity);
				}
			}
			dialog.dispose();
		}
	}
	
	public static boolean closeDialog(Activity activity) {
		boolean closeIt = true;
		if (configurationDialogs.containsKey(activity)) {
			ActivityConfigurationDialog currentDialog = configurationDialogs.get(activity);
			if (currentDialog.isVisible()) {
				closeIt = currentDialog.closeDialog();
			}
			if (closeIt) {
				configurationDialogs.remove(activity);
			}
		}
		return closeIt;
	}
	
	public static ActivityConfigurationDialog getDialog(Activity activity) {
		return configurationDialogs.get(activity);
	}
	
	private static class DataflowCloseListener implements Observer<FileManagerEvent> {

		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof ClosingDataflowEvent) {
				ClosingDataflowEvent closingDataflowEvent = (ClosingDataflowEvent) message;
				if (closingDataflowEvent.isAbortClose()) {
					return;
				}
				Dataflow dataflow = ((ClosingDataflowEvent) message).getDataflow();
				for (Processor p : dataflow.getProcessors()) {
					for (Activity a : p.getActivityList()) {
						if (!closeDialog(a)) {
							closingDataflowEvent.setAbortClose(true);
							return;
						}
					}
				}
			}
			
		}
		
	}
}
