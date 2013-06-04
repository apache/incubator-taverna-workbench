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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

import uk.org.taverna.databundle.DataBundles;

/**
 * Saves individual result to a file. A T2Reference to the result data is held
 * in the tree node.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 * @author David Withers
 */
public class SaveIndividualResult extends AbstractAction implements SaveIndividualResultSPI{

	private static final long serialVersionUID = 4637392234806851345L;

	private static Logger logger = Logger.getLogger(SaveIndividualResult.class);

	/**
	 * Path pointing to the result to be saved.
	 */
	private Path resultReference = null;

	public SaveIndividualResult(){
		super();
		putValue(NAME, "Save value");
		putValue(SMALL_ICON, WorkbenchIcons.saveIcon);
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
		if (DataBundles.isValue(resultReference)) { // Node contains a data value
			// Popup a save dialog and allow the user to store the data to disc
			JFileChooser fc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.resetChoosableFileFilters();
			fc.setCurrentDirectory(new File(curDir));

			boolean tryAgain = true;
			while (tryAgain) {
				tryAgain = false;
				int returnVal = fc.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					prefs.put("currentDir", fc.getCurrentDirectory().toString());
					File file = fc.getSelectedFile();
					// If we know the extension and the user did not use it - append it to the file name
					if (!file.exists()) {
							file = new File(file.getParentFile(), file.getName());
					}
					final File finalFile = file;

					if (finalFile.exists()){ // File already exists
						// Ask the user if they want to overwrite the file
						String msg = file.getAbsolutePath() + " already exists. Do you want to overwrite it?";
						int ret = JOptionPane.showConfirmDialog(
								null, msg, "File already exists",
								JOptionPane.YES_NO_OPTION);

						if (ret == JOptionPane.YES_OPTION) {
							// Do this in separate thread to avoid hanging UI
							new Thread("SaveIndividualResult: Saving results to " + finalFile){
								@Override
								public void run(){
									try {
										Files.copy(resultReference, finalFile.toPath());
									} catch (Exception ex) {
										JOptionPane.showMessageDialog(null, "Problem saving result data", "Save Result Error",
												JOptionPane.ERROR_MESSAGE);
										logger.error("SaveIndividualResult Error: Problem saving result data", ex);
									}
								}
							}.start();
						}
						else{
							tryAgain = true;
						}
					}
					else{ // File does not already exist
						// Do this in separate thread to avoid hanging UI
						new Thread("SaveIndividualResult: Saving results to " + finalFile){
							@Override
							public void run(){
								try {
									Files.copy(resultReference, finalFile.toPath());
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(null, "Problem saving result data", "Save Result Error",
											JOptionPane.ERROR_MESSAGE);
									logger.error("SaveIndividualResult Error: Problem saving result data", ex);
								}
							}
						}.start();
					}
				}
			}
		} else if (DataBundles.isError(resultReference)) { // Node contains a reference to ErrorDocument
			// Popup a save dialog and allow the user to store the data to disc
			JFileChooser fc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.resetChoosableFileFilters();
			FileFilter ff = new ExtensionFileFilter(new String[] { "txt" });
			fc.setFileFilter(ff);
			fc.setCurrentDirectory(new File(curDir));

			boolean tryAgain = true;
			while (tryAgain) {
				tryAgain = false;
				int returnVal = fc.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					prefs.put("currentDir", fc.getCurrentDirectory().toString());
					File file = fc.getSelectedFile();

					// If user did not use the file extension - append it to the file name
					if (!file.exists()) {
						if (fc.getFileFilter().equals(ff) && !file.getName().contains(".")) {
							String newFileName = file.getName() + ".txt";
							file = new File(file.getParentFile(), newFileName);
						} else {
							file = new File(file.getParentFile(), file.getName());
						}
					}

					final File finalFile = file;

					if (finalFile.exists()){ // File already exists
						// Ask the user if they want to overwrite the file
						String msg = file.getAbsolutePath() + " already exists. Do you want to overwrite it?";
						int ret = JOptionPane.showConfirmDialog(
								null, msg, "File already exists",
								JOptionPane.YES_NO_OPTION);

						if (ret == JOptionPane.YES_OPTION) {
							// Do this in separate thread to avoid hanging UI
							new Thread("SaveIndividualResult: Saving error document to " + file){
								@Override
								public void run(){
									try {
										Files.copy(resultReference, finalFile.toPath());
									} catch (Exception ex) {
										JOptionPane.showMessageDialog(null, "Problem saving error document", "Save Result Error",
												JOptionPane.ERROR_MESSAGE);
										logger.error("SaveIndividualResult Error: Problem saving error document", ex);
									}
								}
							}.start();
						}
						else{
							tryAgain = true;
						}
					}
					else{ // File does not already exist
						// Do this in separate thread to avoid hanging UI
						new Thread("SaveIndividualResult: Saving results to " + file){
							@Override
							public void run(){
								try {
									Files.copy(resultReference, finalFile.toPath());
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(null, "Problem saving result data", "Save Result Error",
											JOptionPane.ERROR_MESSAGE);
									logger.error("SaveIndividualResult Error: Problem saving result data", ex);
								}
							}
						}.start();
					}
				}
			}
		}
	}


	// Must be called before actionPerformed()
	@Override
	public void setResultReference(Path reference) {
		this.resultReference = reference;
	}

}
