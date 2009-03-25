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
package net.sf.taverna.t2.dataflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.filemanager.NestedDataflowSource;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenNestedWorkflowAction;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction.OpenCallbackAdapter;
import net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction.OpenCallback;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

public class DataflowActivityConfigurationAction extends
		ActivityConfigurationAction<DataflowActivity, Dataflow> {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(DataflowActivityConfigurationAction.class);

	private FileManager fileManager = FileManager.getInstance();

	private OpenNestedWorkflowAction openNestedWorkflowAction = new OpenNestedWorkflowAction();

	public DataflowActivityConfigurationAction(DataflowActivity activity) {
		super(activity);
	}

	/**
	 * Pop up a {@link JFileChooser} and let the user select a {@link Dataflow}
	 * to be opened. Deserialise it when selected, do the edits to add it to the
	 * current dataflow and get eh {@link FileManager} to open it in the GUI
	 */
	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		NestedDataflowSource nestedDataflowSource = new NestedDataflowSource(
				fileManager.getCurrentDataflow(), getActivity());
		
		Dataflow alreadyOpen = fileManager.getDataflowBySource(nestedDataflowSource);
		if (alreadyOpen != null) {
			fileManager.setCurrentDataflow(alreadyOpen);
			// Warn the user the nested workflow they are trying to replace is already opened
			JOptionPane.showMessageDialog(
							null,
							"The nested workflow you are trying to replace is already opened.\n"
									+ "Close the opened one first if you wish to continue.",
							"File Manager Alert",
							JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		openNestedWorkflowAction.openWorkflows(parentComponent,
				new SetNestedWorkflowOpenCallback(fileManager
						.getCurrentDataflow()));
	}

	protected class SetNestedWorkflowOpenCallback extends
			OpenCallbackAdapter implements OpenCallback {

		private final Dataflow owningDataflow;

		public SetNestedWorkflowOpenCallback(Dataflow owningDataflow) {
			this.owningDataflow = owningDataflow;
		}

		@Override
		public void openedDataflow(File file, Dataflow dataflow) {
			NestedDataflowSource nestedDataflowSource = new NestedDataflowSource(
					owningDataflow, getActivity());
			
			try {
				fileManager.saveDataflow(dataflow, new T2FlowFileType(),
						nestedDataflowSource, false);
				fileManager.closeDataflow(dataflow, false);
			} catch (SaveException e) {
				logger.warn("Could not save nested dataflow to activity "
						+ getActivity(), e);
			} catch (UnsavedException e) {
				logger.error("Unexpected UnsavedException", e);
			}
			// Switch back to owning dataflow
			fileManager.setCurrentDataflow(owningDataflow);
		}
	}

}
