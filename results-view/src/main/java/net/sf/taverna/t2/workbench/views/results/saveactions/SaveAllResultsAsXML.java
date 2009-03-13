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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.views.results.ResultsUtils;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Stores the entire map of result objects to disk
 * as a single XML data document.
 * 
 * @author Tom Oinn
 * @author Alex Nenadic
 */
public class SaveAllResultsAsXML extends AbstractAction implements SaveAllResultsSPI {

	private static final long serialVersionUID = 452360182978773176L;

	private static Logger logger = Logger.getLogger(SaveAllResultsAsXML.class);

	private static Namespace namespace = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");

	private Map<String, T2Reference> resultReferencesMap = null;

	private InvocationContext context = null;
	
	public SaveAllResultsAsXML(){
		super();
		putValue(NAME, "Save as XML");
		putValue(SMALL_ICON, WorkbenchIcons.xmlNodeIcon);
	}
	
	public AbstractAction getAction() {
		return this;
	}
	
	// Must be called before actionPerformed()
	public void setInvocationContext(InvocationContext context) {
		this.context = context;
	}
	
	// Must be called before actionPerformed()
	public void setResultReferencesMap(Map<String, T2Reference> resultReferencesMap) {
		this.resultReferencesMap = resultReferencesMap;
	}

	/**
     * Shows a standard save dialog and dumps the entire result
     * set to the specified XML file.
     */
	public void actionPerformed(ActionEvent e) {
		
		JFileChooser fc = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
		fc.setCurrentDirectory(new File(curDir));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			int returnVal = fc.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				File file = fc.getSelectedFile();

				// If the user did not use the .xml extension for the file - append it to the file name now
				if (!file.getName().toLowerCase().endsWith(".xml")) {
					String newFileName = file.getName() + ".xml";
					file = new File(file.getParentFile(), newFileName);
				}

				final File finalFile = file;
				
				if (file.exists()){ // File already exists
					// Ask the user if they want to overwrite the file
					String msg = file.getAbsolutePath() + " already exists. Do you want to overwrite it?";
					int ret = JOptionPane.showConfirmDialog(
							null, msg, "File already exists",
							JOptionPane.YES_NO_OPTION);
					
					if (ret == JOptionPane.YES_OPTION) {
						// Do this in separate thread to avoid hanging UI
						new Thread("SaveAllResultsAsXML: Saving results to " + finalFile){
							public void run(){
								try {
									synchronized(resultReferencesMap){
										saveData(finalFile);
									}
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(null, "Problem saving result data", "Save Result Error",
											JOptionPane.ERROR_MESSAGE);
									logger.error("SaveAllResultsAsXML Error: Problem saving result data", ex);
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
					new Thread("SaveAllResultsAsXML: Saving results to " + finalFile){
						public void run(){
							try {
								synchronized(resultReferencesMap){
									saveData(finalFile);
								}
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, "Problem saving result data", "Save Result Error",
										JOptionPane.ERROR_MESSAGE);
								logger.error("SaveAllResultsAsXML Error: Problem saving result data", ex);
							}
						}
					}.start();
				}
			}
		}  
	}		
	
	/**
	 * Saves the result data to an XML Baclava file. 
	 */
	private void saveData(File file) throws Exception{
	    
		// Build the DataThing map from the resultReferencesMap
		// First convert map of references to objects into a map of real result objects
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (Iterator<String> i = resultReferencesMap.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			T2Reference reference = resultReferencesMap.get(portName);
			Object obj = convertReferencesToObjects(reference);
			resultMap.put(portName, obj);
		}
		Map<String, DataThing> dataThings = bakeDataThingMap(resultMap);
		
		// Build the string containing the XML document from the result map
		Document doc = getDataDocument(dataThings);
	    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	    String xmlString = xo.outputString(doc);
	    PrintWriter out = new PrintWriter(new FileWriter(file));
	    out.print(xmlString);
	    out.flush();
	    out.close();
	}
	
	/**
	 * Converts a T2References poining to results to 
	 * a list of (lists of ...) dereferenced result objects.
	 */
	Object convertReferencesToObjects(T2Reference reference) throws Exception{				

			if (reference.getReferenceType() == T2ReferenceType.ReferenceSet){
				// Dereference the object
				Object dataValue;
				try{
					dataValue = context.getReferenceService().renderIdentifier(reference, Object.class, context);
				}
				catch(ReferenceServiceException rse){
					String message = "Problem rendering T2Reference in convertReferencesToObjects().";
					logger.error("SaveAllResultsAsXML Error: "+ message, rse);
					throw new Exception(message);
				}
				return dataValue;
			}
			else if (reference.getReferenceType() == T2ReferenceType.ErrorDocument){
				// Dereference the ErrorDocument and convert it to some string representation
				ErrorDocument errorDocument = (ErrorDocument)context.getReferenceService().resolveIdentifier(reference, null, context);
				String errorString = ResultsUtils.buildErrorDocumentString(errorDocument, context);
				return errorString;
			}
			else { // it is an IdentifiedList<T2Reference> - go recursively
				IdentifiedList<T2Reference> identifiedList = context
				.getReferenceService().getListService().getList(reference);
				List<Object> list = new ArrayList<Object>();
				
				for (int j=0; j<identifiedList.size(); j++){
					T2Reference ref = identifiedList.get(j);
					list.add(convertReferencesToObjects(ref));
				}
				return list;
			}	
	}
	
	/**
	 * Returns a map of port names to DataThings from a map of port names to a 
	 * list of (lists of ...) result objects.
	 */
	Map<String, DataThing> bakeDataThingMap(Map<String, Object> resultMap){
		
		Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
		for (Iterator<String> i = resultMap.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			dataThingMap.put(portName, DataThingFactory.bake(resultMap.get(portName)));
		}
		return dataThingMap;
	}
	
	/**
	 * Returns a org.jdom.Document from a map of port named to DataThingS containing
	 * the port's results.
	 */
	public static Document getDataDocument(Map<String, DataThing> dataThings) {
		Element rootElement = new Element("dataThingMap", namespace);
		Document theDocument = new Document(rootElement);
		for (Iterator<String> i = dataThings.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			DataThing value = (DataThing) dataThings.get(key);
			Element dataThingElement = new Element("dataThing", namespace);
			dataThingElement.setAttribute("key", key);
			dataThingElement.addContent(value.getElement());
			rootElement.addContent(dataThingElement);
		}
		return theDocument;
	}
}
