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
package net.sf.taverna.t2.workbench.views.results.saveactions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.results.ResultsUtils;

import org.apache.log4j.Logger;

/**
 * Implementing classes are capable of storing a collection
 * of Paths held in a result map.
 *
 * @author Tom Oinn
 * @author Alex Nenadic
 * @author David Withers
 */
@SuppressWarnings("serial")
public abstract class SaveAllResultsSPI extends AbstractAction {

	protected static Logger logger = Logger.getLogger(SaveAllResultsSPI.class);

	protected Map<String, Path> chosenReferences;
	protected JDialog dialog;

	/**
	 * Returns the save result action implementing this interface. The returned
	 * action will be bound to the appropriate UI component used to trigger the
	 * save action.
	 */
	public abstract AbstractAction getAction();

	/**
	 * The Map passed into this method contains the String -> T2Reference (port
	 * name to reference to value pairs) returned by the current set of results.
	 * The actual listener may well wish to display some kind of dialog, for
	 * example in the case of an Excel export plugin it would be reasonable to
	 * give the user some choice over where the results would be inserted into
	 * the sheet, and also where the generated file would be stored.
	 * <p>
	 * The parent parameter is optional and may be set to null, if not it is assumed to be the
	 * parent component in the UI which caused this action to be created, this allows save dialogs
	 * etc to be placed correctly.
	 */
	public void setChosenReferences(Map<String, Path> chosenReferences) {
		this.chosenReferences = chosenReferences;
	}

	public void setParent(JDialog dialog) {
		this.dialog = dialog;
	}

	protected abstract String getFilter();

	protected int getFileSelectionMode() {
		return JFileChooser.FILES_ONLY;
	}

	/**
	 * Shows a standard save dialog and dumps the entire result
	 * set to the specified XML file.
	 */
	public void actionPerformed(ActionEvent e) {

		dialog.setVisible(false);

		JFileChooser fc = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		fc.resetChoosableFileFilters();
		if (getFilter() != null) {
			fc.setFileFilter(new ExtensionFileFilter(new String[] { getFilter() }));
		}
		fc.setCurrentDirectory(new File(curDir));
		fc.setFileSelectionMode(getFileSelectionMode());

		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			int returnVal = fc.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				File file = fc.getSelectedFile();

				if (getFilter() != null) {
					// If the user did not use the .xml extension for the file - append it to the
					// file name now
					if (!file.getName().toLowerCase().endsWith("." + getFilter())) {
						String newFileName = file.getName() + "." + getFilter();
						file = new File(file.getParentFile(), newFileName);
					}
				}
				final File finalFile = file;

				if (file.exists()) { // File already exists
					// Ask the user if they want to overwrite the file
					String msg = file.getAbsolutePath()
							+ " already exists. Do you want to overwrite it?";
					int ret = JOptionPane.showConfirmDialog(null, msg, "File already exists",
							JOptionPane.YES_NO_OPTION);

					if (ret == JOptionPane.YES_OPTION) {
						// Do this in separate thread to avoid hanging UI
						new Thread("SaveAllResults: Saving results to " + finalFile) {
							public void run() {
								try {
									synchronized (chosenReferences) {
										saveData(finalFile);
									}
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(null,
											"Problem saving result data\n" + ex.getMessage(),
											"Save Result Error", JOptionPane.ERROR_MESSAGE);
									logger.error(
											"SaveAllResults Error: Problem saving result data", ex);
								}
							}
						}.start();
					} else {
						tryAgain = true;
					}
				} else { // File does not already exist

					// Do this in separate thread to avoid hanging UI
					new Thread("SaveAllResults: Saving results to " + finalFile) {
						public void run() {
							try {
								synchronized (chosenReferences) {
									saveData(finalFile);
								}
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, "Problem saving result data\n"
										+ ex.getMessage(), "Save Result Error",
										JOptionPane.ERROR_MESSAGE);
								logger.error("SaveAllResults Error: Problem saving result data", ex);
							}
						}
					}.start();
				}
			}
		}
	}

	protected abstract void saveData(File f) throws IOException;

	protected Object getObjectForName(String name) {
		Object result = null;
		if (chosenReferences.containsKey(name)) {
			try {
				result = ResultsUtils.convertPathToObject(chosenReferences.get(name));
			} catch (IOException e) {
				logger.warn("Error getting value for " + name, e);
			}
		}
		if (result == null) {
			result = "null";
		}
		return result;

	}

	public Map<String, Path> getChosenReferences() {
		return chosenReferences;
	}

	public JDialog getDialog() {
		return dialog;
	}

}
