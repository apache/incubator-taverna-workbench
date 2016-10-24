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
package org.apache.taverna.workbench.run.actions;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import org.apache.taverna.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

import org.apache.taverna.platform.run.api.RunService;

/**
 * An action for opening a workflow run from a file.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class OpenWorkflowRunAction extends AbstractAction {
	private static Logger logger = Logger.getLogger(OpenWorkflowRunAction.class);
	private static final String OPEN_WORKFLOW_RUN = "Open workflow run...";

	private final RunService runService;
	private final File runStore;

	public OpenWorkflowRunAction(RunService runService, File runStore) {
		super(OPEN_WORKFLOW_RUN, WorkbenchIcons.openIcon);
		this.runService = runService;
		this.runStore = runStore;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component)
			parentComponent = (Component) e.getSource();
		else
			parentComponent = null;
		openWorkflowRuns(parentComponent);
	}

	public void openWorkflowRuns(final Component parentComponent) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(OPEN_WORKFLOW_RUN);

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Workflow Run";
			}

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".wfRun");
			}
		});

		fileChooser.setCurrentDirectory(runStore);
		fileChooser.setMultiSelectionEnabled(true);

		if (fileChooser.showOpenDialog(parentComponent) == APPROVE_OPTION) {
			final File[] selectedFiles = fileChooser.getSelectedFiles();
			if (selectedFiles.length == 0) {
				logger.warn("No files selected");
				return;
			}
			new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					for (File file : selectedFiles)
						try {
							runService.open(file.toPath());
						} catch (IOException e) {
							showErrorMessage(parentComponent, file, e);
						}
					return null;
				}
			}.execute();
		}
	}

	/**
	 * Show an error message if a file could not be opened
	 * 
	 * @param parentComponent
	 * @param file
	 * @param throwable
	 */
	protected void showErrorMessage(final Component parentComponent,
			final File file, Throwable throwable) {
		Throwable cause = throwable;
		while (cause.getCause() != null)
			cause = cause.getCause();
		final String message = cause.getMessage();
		invokeLater(new Runnable() {
			@Override
			public void run() {
				showMessageDialog(parentComponent,
						"Failed to open workflow from " + file + ":\n"
								+ message, "Warning", WARNING_MESSAGE);
			}
		});
	}
}
