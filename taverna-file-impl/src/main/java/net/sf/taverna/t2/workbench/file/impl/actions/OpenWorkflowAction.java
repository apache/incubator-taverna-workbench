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
package net.sf.taverna.t2.workbench.file.impl.actions;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_O;
import static java.util.prefs.Preferences.userNodeForPackage;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingUtilities.invokeLater;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.openIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.FileTypeFileFilter;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * An action for opening a workflow from a file. All file types exposed by the
 * {@link FileManager} as compatible with the {@link File} type are supported.
 *
 * @author Stian Soiland-Reyes
 */
public class OpenWorkflowAction extends AbstractAction {
	private static final long serialVersionUID = 103237694130052153L;
	private static Logger logger = Logger.getLogger(OpenWorkflowAction.class);
	private static final String OPEN_WORKFLOW = "Open workflow...";

	public final OpenCallback DUMMY_OPEN_CALLBACK = new OpenCallbackAdapter();
	protected FileManager fileManager;

	public OpenWorkflowAction(FileManager fileManager) {
		super(OPEN_WORKFLOW, openIcon);
		this.fileManager = fileManager;
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_O, getDefaultToolkit().getMenuShortcutKeyMask()));
		putValue(MNEMONIC_KEY, VK_O);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component)
			parentComponent = (Component) e.getSource();
		else
			parentComponent = null;
		openWorkflows(parentComponent);
	}

	/**
	 * Pop up an Open-dialogue to select one or more workflow files to open.
	 * <p>
	 * Note that the file opening occurs in a separate thread. If you want to
	 * check if the file was opened or not, which workflow was opened, etc, use
	 * {@link #openWorkflows(Component, OpenCallback)} instead.
	 *
	 * @see #openWorkflows(Component, OpenCallback)
	 * @param parentComponent
	 *            The UI parent component to use for pop up dialogues
	 *
	 * @return <code>false</code> if no files were selected or the dialogue was
	 *         cancelled, or <code>true</code> if the process of opening one or
	 *         more files has been started.
	 */
	public void openWorkflows(Component parentComponent) {
		openWorkflows(parentComponent, DUMMY_OPEN_CALLBACK);
	}

	/**
	 * Open an array of workflow files.
	 *
	 * @param parentComponent
	 *            Parent component for UI dialogues
	 * @param files
	 *            Array of files to be opened
	 * @param fileType
	 *            {@link FileType} of the files that are to be opened, for
	 *            instance
	 *            {@link net.sf.taverna.t2.workbench.file.impl.T2FlowFileType},
	 *            or <code>null</code> to guess.
	 * @param openCallback
	 *            An {@link OpenCallback} to be invoked during and after opening
	 *            the file. Use {@link OpenWorkflowAction#DUMMY_OPEN_CALLBACK}
	 *            if no callback is needed.
	 */
	public void openWorkflows(final Component parentComponent, File[] files,
			FileType fileType, OpenCallback openCallback) {
		ErrorLoggingOpenCallbackWrapper callback = new ErrorLoggingOpenCallbackWrapper(
				openCallback);
		for (File file : files)
			try {
				Object canonicalSource = fileManager.getCanonical(file);
				WorkflowBundle alreadyOpen = fileManager.getDataflowBySource(canonicalSource);
				if (alreadyOpen != null) {
					/*
					 * The workflow from the same source is already opened - ask
					 * the user if they want to switch to it or open another
					 * copy...
					 */

					Object[] options = { "Switch to opened", "Open new copy",
							"Cancel" };
					switch (showOptionDialog(
							null,
							"The workflow from the same location is already opened.\n"
									+ "Do you want to switch to it or open a new copy?",
							"File Manager Alert", YES_NO_CANCEL_OPTION,
							QUESTION_MESSAGE, null, options, // the titles of buttons
							options[0])) { // default button title
					case YES_OPTION:
						fileManager.setCurrentDataflow(alreadyOpen);
						return;
					case CANCEL_OPTION:
						// do nothing
						return;
					}
					// else open the workflow as usual
				}

				callback.aboutToOpenDataflow(file);
				WorkflowBundle workflowBundle = fileManager.openDataflow(fileType, file);
				callback.openedDataflow(file, workflowBundle);
			} catch (RuntimeException ex) {
				logger.warn("Failed to open workflow from " + file, ex);
				if (!callback.couldNotOpenDataflow(file, ex))
					showErrorMessage(parentComponent, file, ex);
			} catch (Exception ex) {
				logger.warn("Failed to open workflow from " + file, ex);
				if (!callback.couldNotOpenDataflow(file, ex))
					showErrorMessage(parentComponent, file, ex);
				return;
			}
	}

	/**
	 * Pop up an Open-dialogue to select one or more workflow files to open.
	 *
	 * @param parentComponent
	 *            The UI parent component to use for pop up dialogues
	 * @param openCallback
	 *            An {@link OpenCallback} to be called during the file opening.
	 *            The callback will be invoked for each file that has been
	 *            opened, as file opening happens in a separate thread that
	 *            might execute after the return of this method.
	 * @return <code>false</code> if no files were selected or the dialogue was
	 *         cancelled, or <code>true</code> if the process of opening one or
	 *         more files has been started.
	 */
	public boolean openWorkflows(final Component parentComponent,
			OpenCallback openCallback) {
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle(OPEN_WORKFLOW);

		fileChooser.resetChoosableFileFilters();
		fileChooser.setAcceptAllFileFilterUsed(false);
		List<FileFilter> fileFilters = fileManager.getOpenFileFilters();
		if (fileFilters.isEmpty()) {
			logger.warn("No file types found for opening workflow");
			showMessageDialog(parentComponent,
					"No file types found for opening workflow.", "Error",
					ERROR_MESSAGE);
			return false;
		}
		for (FileFilter fileFilter : fileFilters)
			fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setFileFilter(fileFilters.get(0));
		fileChooser.setCurrentDirectory(new File(curDir));
		fileChooser.setMultiSelectionEnabled(true);

		int returnVal = fileChooser.showOpenDialog(parentComponent);
		if (returnVal == APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			final File[] selectedFiles = fileChooser.getSelectedFiles();
			if (selectedFiles.length == 0) {
				logger.warn("No files selected");
				return false;
			}
			FileFilter fileFilter = fileChooser.getFileFilter();
			FileType fileType;
			if (fileFilter instanceof FileTypeFileFilter)
				fileType = ((FileTypeFileFilter) fileChooser.getFileFilter())
						.getFileType();
			else
				// Unknown filetype, try all of them
				fileType = null;
			new FileOpenerThread(parentComponent, selectedFiles, fileType,
					openCallback).start();
			return true;
		}
		return false;
	}

	/**
	 * Show an error message if a file could not be opened
	 * 
	 * @param parentComponent
	 * @param file
	 * @param throwable
	 */
	protected void showErrorMessage(final Component parentComponent,
			final File file, final Throwable throwable) {
		invokeLater(new Runnable() {
			@Override
			public void run() {
				Throwable cause = throwable;
				while (cause.getCause() != null)
					cause = cause.getCause();
				showMessageDialog(
						parentComponent,
						"Failed to open workflow from " + file + ": \n"
								+ cause.getMessage(), "Warning",
						WARNING_MESSAGE);
			}
		});

	}

	/**
	 * Callback interface for openWorkflows().
	 * <p>
	 * The callback will be invoked during the invocation of
	 * {@link OpenWorkflowAction#openWorkflows(Component, OpenCallback)} and
	 * {@link OpenWorkflowAction#openWorkflows(Component, File[], FileType, OpenCallback)}
	 * as file opening happens in a separate thread.
	 *
	 * @author Stian Soiland-Reyes
	 */
	public interface OpenCallback {
		/**
		 * Called before a workflowBundle is to be opened from the given file
		 *
		 * @param file
		 *            File which workflowBundle is to be opened
		 */
		void aboutToOpenDataflow(File file);

		/**
		 * Called if an exception happened while attempting to open the
		 * workflowBundle.
		 *
		 * @param file
		 *            File which was attempted to be opened
		 * @param ex
		 *            An {@link OpenException} or a {@link RuntimeException}.
		 * @return <code>true</code> if the error has been handled, or
		 *         <code>false</code>3 if a UI warning dialogue is to be opened.
		 */
		boolean couldNotOpenDataflow(File file, Exception ex);

		/**
		 * Called when a workflowBundle has been successfully opened. The workflowBundle
		 * will be registered in {@link FileManager#getOpenDataflows()}.
		 *
		 * @param file
		 *            File from which workflowBundle was opened
		 * @param workflowBundle
		 *            WorkflowBundle that was opened
		 */
		void openedDataflow(File file, WorkflowBundle workflowBundle);
	}

	/**
	 * Adapter for {@link OpenCallback}
	 *
	 * @author Stian Soiland-Reyes
	 */
	public static class OpenCallbackAdapter implements OpenCallback {
		@Override
		public void aboutToOpenDataflow(File file) {
		}

		@Override
		public boolean couldNotOpenDataflow(File file, Exception ex) {
			return false;
		}

		@Override
		public void openedDataflow(File file, WorkflowBundle workflowBundle) {
		}
	}

	private final class FileOpenerThread extends Thread {
		private final File[] files;
		private final FileType fileType;
		private final OpenCallback openCallback;
		private final Component parentComponent;

		private FileOpenerThread(Component parentComponent,
				File[] selectedFiles, FileType fileType,
				OpenCallback openCallback) {
			super("Opening workflows(s) " + Arrays.asList(selectedFiles));
			this.parentComponent = parentComponent;
			this.files = selectedFiles;
			this.fileType = fileType;
			this.openCallback = openCallback;
		}

		@Override
		public void run() {
			openWorkflows(parentComponent, files, fileType, openCallback);
		}
	}

	/**
	 * A wrapper for {@link OpenCallback} implementations that logs exceptions
	 * thrown without disrupting the caller of the callback.
	 *
	 * @author Stian Soiland-Reyes
	 */
	protected class ErrorLoggingOpenCallbackWrapper implements OpenCallback {
		private final OpenCallback wrapped;

		public ErrorLoggingOpenCallbackWrapper(OpenCallback wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public void aboutToOpenDataflow(File file) {
			try {
				wrapped.aboutToOpenDataflow(file);
			} catch (RuntimeException wrapperEx) {
				logger.warn("Failed OpenCallback " + wrapped
						+ ".aboutToOpenDataflow(File)", wrapperEx);
			}
		}

		@Override
		public boolean couldNotOpenDataflow(File file, Exception ex) {
			try {
				return wrapped.couldNotOpenDataflow(file, ex);
			} catch (RuntimeException wrapperEx) {
				logger.warn("Failed OpenCallback " + wrapped
						+ ".couldNotOpenDataflow(File, Exception)", wrapperEx);
				return false;
			}
		}

		@Override
		public void openedDataflow(File file, WorkflowBundle workflowBundle) {
			try {
				wrapped.openedDataflow(file, workflowBundle);
			} catch (RuntimeException wrapperEx) {
				logger.warn("Failed OpenCallback " + wrapped
						+ ".openedDataflow(File, Dataflow)", wrapperEx);
			}
		}
	}

}
