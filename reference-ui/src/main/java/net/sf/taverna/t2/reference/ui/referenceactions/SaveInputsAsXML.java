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
package net.sf.taverna.t2.reference.ui.referenceactions;

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
import net.sf.taverna.t2.reference.ui.RegistrationPanel;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
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
public class SaveInputsAsXML extends AbstractAction implements ReferenceActionSPI {

	private static final long serialVersionUID = 452360182978773176L;

	private static Logger logger = Logger.getLogger(SaveInputsAsXML.class);

	private static Namespace namespace = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");

	private InvocationContext context = null;

	private Map<String, RegistrationPanel> inputPanelMap;
	
	public SaveInputsAsXML(){
		super();
		putValue(NAME, "Save values");
		putValue(SMALL_ICON, WorkbenchIcons.xmlNodeIcon);
	}
	
	public AbstractAction getAction() {
		return new SaveInputsAsXML();
	}
	
	// Must be called before actionPerformed()
	public void setInvocationContext(InvocationContext context) {
		this.context = context;
	}

	/**
     * Shows a standard save dialog and dumps the entire input
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
						new Thread("Save(InputsAsXML: Saving inputs to " + finalFile){
							public void run(){
								try {
									synchronized(inputPanelMap){
										saveData(finalFile);
									}
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(null, "Problem saving input data", "Save Inputs Error",
											JOptionPane.ERROR_MESSAGE);
									logger.error("SaveInputsAsXML Error: Problem saving input data", ex);
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
					new Thread("SaveInputsAsXML: Saving inputs to " + finalFile){
						public void run(){
							try {
								synchronized(inputPanelMap){
									saveData(finalFile);
								}
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, "Problem saving input data", "Save Inputs Error",
										JOptionPane.ERROR_MESSAGE);
								logger.error("SaveInputsAsXML Error: Problem saving input data", ex);
							}
						}
					}.start();
				}
			}
		}  
	}		
	
	/**
	 * Saves the input data to an XML Baclava file. 
	 */
	private void saveData(File file) throws Exception{
	    
		// Build the DataThing map from the inputPanelMap
		Map<String, Object> valueMap = new HashMap<String, Object>();
		for (Iterator<String> i = inputPanelMap.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			RegistrationPanel panel = inputPanelMap.get(portName);
			Object obj = panel.getValue();
			if (obj != null) {
				valueMap.put(portName, obj);
			}
		}
		Map<String, DataThing> dataThings = bakeDataThingMap(valueMap);
		
		// Build the string containing the XML document from the panel map
		Document doc = getDataDocument(dataThings);
	    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	    String xmlString = xo.outputString(doc);
	    PrintWriter out = new PrintWriter(new FileWriter(file));
	    out.print(xmlString);
	    out.flush();
	    out.close();
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

	public void setInputPanelMap(Map<String, RegistrationPanel> inputPanelMap) {
		this.inputPanelMap = inputPanelMap;
	}

}
