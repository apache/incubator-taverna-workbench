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

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

public abstract class ActivityConfigurationAction<A extends Activity<ConfigurationBean>, ConfigurationBean>
		extends AbstractAction {

	private static Logger logger = Logger
			.getLogger(ActivityConfigurationAction.class);

	private A activity;

	public ActivityConfigurationAction(A activity) {
		this.activity = activity;
		putValue(SMALL_ICON, ActivityIconManager.getInstance().iconForActivity(activity));
	}

	protected A getActivity() {
		return activity;
	}

	protected void configureActivity(ConfigurationBean configurationBean) {
		Edits edits = EditsRegistry.getEdits();
		Edit<?> configureActivityEdit = edits.getConfigureActivityEdit(
				getActivity(), configurationBean);
		Dataflow currentDataflow = FileManager.getInstance()
				.getCurrentDataflow();
		try {
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			editList.add(configureActivityEdit);
			Processor p = findProcessor(currentDataflow);
			if (p != null && p.getActivityList().size() == 1) {
				editList.add(edits.getMapProcessorPortsForActivityEdit(p));
			}
			EditManager.getInstance().doDataflowEdit(currentDataflow,
					new CompoundEdit(editList));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EditException e) {
			e.printStackTrace();
		}
	}

	protected Processor findProcessor(Dataflow df) {
		Activity<?> activity = getActivity();

		for (Processor processor : df.getProcessors()) {
			if (processor.getActivityList().contains(activity))
				return processor;
		}
		return null;
	}
	
	protected String getRelativeName() {
		String result = "";
		Dataflow currentDataflow = FileManager.getInstance()
		.getCurrentDataflow();
		if (currentDataflow != null) {
			result += currentDataflow.getLocalName();
			Processor p = findProcessor(currentDataflow);
			if (p != null) {
				result += (":" + p.getLocalName());
			}
		}
		return result;
	}
}
