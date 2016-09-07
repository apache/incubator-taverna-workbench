/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.views.results.saveactions;

import static java.lang.System.getProperty;
import static java.nio.file.Files.copy;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.saveIcon;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.taverna.lang.ui.ExtensionFileFilter;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;

/**
 * Saves individual result to a file. A T2Reference to the result data is held
 * in the tree node.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 * @author David Withers
 */
public class SaveIndividualResult extends AbstractAction implements
		SaveIndividualResultSPI {
	private static final long serialVersionUID = 4637392234806851345L;
	private static final Logger logger = Logger
			.getLogger(SaveIndividualResult.class);

	/**
	 * Path pointing to the result to be saved.
	 */
	private Path resultReference = null;

	public SaveIndividualResult(){
		super();
		putValue(NAME, "Save value");
		putValue(SMALL_ICON, saveIcon);
	}

	@Override
	public AbstractAction getAction() {
		return new SaveIndividualResult();
	}

	/**
	 * Saves a result either as a text or a binary file - depending on the
	 * result data type.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (DataBundles.isValue(resultReference)) {
			// Node contains a data value
			// Popup a save dialog and allow the user to store the data to disc
			JFileChooser fc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String curDir = prefs.get("currentDir", getProperty("user.home"));
			fc.resetChoosableFileFilters();
			fc.setCurrentDirectory(new File(curDir));

			File file;
			do {
				if (fc.showSaveDialog(null) != APPROVE_OPTION)
					return;
				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				file = fc.getSelectedFile();
				/*
				 * If we know the extension and the user did not use it, append
				 * it to the file name.
				 * 
				 * TODO the comment is inconsistent with the functionality
				 */
				if (!file.exists())
					file = new File(file.getParentFile(), file.getName());

				// Ask the user if they want to overwrite the file if it exists
			} while (!shouldWrite(file));

			// File does not already exist, or user has OK'd overwrite
			doSaveThread(file, "result data");
		} else if (DataBundles.isError(resultReference)) {
			// Node contains a reference to ErrorDocument
			// Popup a save dialog and allow the user to store the data to disc
			JFileChooser fc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String curDir = prefs.get("currentDir", getProperty("user.home"));
			fc.resetChoosableFileFilters();
			FileFilter ff = new ExtensionFileFilter(new String[] { "txt" });
			fc.setFileFilter(ff);
			fc.setCurrentDirectory(new File(curDir));

			File file;
			do {
				if (fc.showSaveDialog(null) != APPROVE_OPTION)
					return;
				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				file = fc.getSelectedFile();

				// If user did not use the file extension - append it to the file name
				if (!file.exists()) {
					if (fc.getFileFilter().equals(ff) && !file.getName().contains(".")) {
						String newFileName = file.getName() + ".txt";
						file = new File(file.getParentFile(), newFileName);
					} else
						file = new File(file.getParentFile(), file.getName());
				}

				// Ask the user if they want to overwrite the file if it exists
			} while (!shouldWrite(file));

			// File does not already exist, or user has OK'd overwrite
			doSaveThread(file, "error document");
		}
	}

	private boolean shouldWrite(File file) {
		return !file.exists()
				|| showConfirmDialog(null, file.getAbsolutePath()
						+ " already exists. Do you want to overwrite it?",
						"File already exists", YES_NO_OPTION) == YES_OPTION;
	}

	private void doSaveThread(final File file, final String type) {
		// Do this in separate thread to avoid hanging UI
		new Thread("SaveIndividualResult: Saving " + type + " to " + file) {
			@Override
			public void run() {
				try {
					doSave(file);
				} catch (Exception ex) {
					showMessageDialog(null, "Problem saving " + type,
							"Save Result Error", ERROR_MESSAGE);
					logger.error("SaveIndividualResult Error: Problem saving "
							+ type, ex);
				}
			}
		}.start();
	}

	/**
	 * The core of how to save a result. This is called in a context that
	 * handles exceptions and is running inside a worker thread.
	 */
	protected void doSave(File file) throws IOException {
		copy(resultReference, file.toPath());
	}

	// Must be called before actionPerformed()
	@Override
	public void setResultReference(Path reference) {
		this.resultReference = reference;
	}
}
