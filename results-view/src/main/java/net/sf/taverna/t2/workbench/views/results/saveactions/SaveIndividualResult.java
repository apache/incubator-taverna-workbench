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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import net.sf.jmimemagic.MagicMatch;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.views.results.ResultsUtils;

/**
 * Saves individual result to a file. A T2Reference to the result data is held
 * in the tree node.
 * 
 * @author Alex Nenadic
 * @author Alan R Williams
 *
 */
public class SaveIndividualResult extends AbstractAction implements SaveIndividualResultSPI{

	private static final long serialVersionUID = 4588945388830291235L;
	
	private static Logger logger = Logger.getLogger(SaveIndividualResult.class);

	/**
	 * T2Reference pointing to the result to be saved.
	 */
	private T2Reference resultReference = null;
	
	private InvocationContext context = null;
	
	public SaveIndividualResult(){
		super();
		putValue(NAME, "Save result");
		putValue(SMALL_ICON, WorkbenchIcons.saveIcon);
	}
	
	public AbstractAction getAction() {
		return this;
	}
	
	/**
	 * Saves a result either as a text or a binary file - depending on the
	 * retult data type.
	 */
	public void actionPerformed(ActionEvent e) {
		Identified identified = context.getReferenceService().resolveIdentifier(resultReference, null, context);
		MagicMatch magicMatch = null;
		
		if (identified instanceof ReferenceSet) { // Node contains an external reference to data
		
			ReferenceSet referenceSet = (ReferenceSet) identified;
			List<ExternalReferenceSPI> externalReferences = new ArrayList<ExternalReferenceSPI>(referenceSet.getExternalReferences());
			Collections.sort(externalReferences, new Comparator<ExternalReferenceSPI>() {
				public int compare(ExternalReferenceSPI o1, ExternalReferenceSPI o2) {
					return (int) (o1.getResolutionCost() - o2.getResolutionCost());
				}
			});
			
			// Get the MIME type so that we know which file extension to use when saving the result
			String mimeType = null;
			for (ExternalReferenceSPI externalReference : externalReferences) {
				mimeType = ResultsUtils.getMimeType(externalReference, context);
				if (mimeType != null) {
					break;
				}
			}
			if (mimeType == null) {
				mimeType = "text/plain";
			}

			// Get the file extension from the MIME type
			String fileExtension = null;
			for (ExternalReferenceSPI externalReference : externalReferences) {
				magicMatch = ResultsUtils.getMagicMatch(externalReference, context);
				if (magicMatch != null) {
					break;
				}
			}
			
			if (mimeType.equals("text/plain")){ // for some reason magicMatch.getExtension() returns empty string for mime type "text/plain"
				fileExtension = "txt";
			}
			else if (magicMatch == null || magicMatch.getExtension().equals("")) {
				// We do not know the extension - do not try to set it
				fileExtension = "";
			}
			else{
				fileExtension = magicMatch.getExtension();
			}
			
			final Object data;
			try{
				data = context.getReferenceService().renderIdentifier(resultReference, Object.class, context);
			}
			catch(ReferenceServiceException rse){
				JOptionPane.showMessageDialog(null, "Problem rendering T2Reference when saving the result data", "Save Result Error",
						JOptionPane.ERROR_MESSAGE);
				logger.error("SaveIndividualResult Error: Problem rendering T2Reference when saving the result data", rse);
				return;				
			}
			// All is fine - we have rendered the T2Reference correctly
			
			// Popup a save dialog and allow the user to store the data to disc
			JFileChooser fc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.resetChoosableFileFilters();
			FileFilter ff = null;

			// Set the file filter if we know the extension
			if (!fileExtension.equals("")){
				ff = new ExtensionFileFilter(new String[] { fileExtension });
				fc.setFileFilter(ff);
			}
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
						if ((ff != null) && fc.getFileFilter().equals(ff) && !file.getName().contains(".")) {
							String newFileName = file.getName() + "." + fileExtension;
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
							new Thread("SaveIndividualResult: Saving results to " + finalFile){
								@Override
								public void run(){
									try {
										saveData(finalFile, data);
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
									saveData(finalFile, data);
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
		} else if (identified instanceof ErrorDocument) { // Node contains a reference to ErrorDocument
			// Save ErrorDocument as text
			final String errorString = ResultsUtils.buildErrorDocumentString((ErrorDocument)identified, context);
			
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
										saveData(finalFile, errorString);
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
									saveData(finalFile, errorString);
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
	
	private void saveData (File file, Object data) throws Exception{
		FileOutputStream fos = new FileOutputStream(file);
		if (data instanceof byte[]) {
			logger.info("Saving result data as byte stream.");
			fos.write((byte[]) data);
			fos.flush();
			fos.close();
		} else if (data instanceof String){
			logger.info("Saving result data as text.");
			Writer out = new BufferedWriter(new OutputStreamWriter(fos));
			out.write((String) data);
			fos.flush();
			out.flush();
			fos.close();
			out.close();
		}
	}

	// Must be called before actionPerformed()
	public void setResultReference(T2Reference reference) {
		this.resultReference = reference;
	}

	// Must be called before actionPerformed()
	public void setInvocationContext(InvocationContext ctxt) {
		this.context = ctxt;
	}
}
