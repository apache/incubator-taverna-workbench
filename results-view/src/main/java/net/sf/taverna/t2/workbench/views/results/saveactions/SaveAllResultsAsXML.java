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

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.embl.ebi.escience.baclava.DataThing;
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
public class SaveAllResultsAsXML extends SaveAllResultsSPI {

	private static final long serialVersionUID = 452360182978773176L;

	private static Namespace namespace = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");

	public SaveAllResultsAsXML(){
		super();
		putValue(NAME, "Save in single XML document");
		putValue(SMALL_ICON, WorkbenchIcons.xmlNodeIcon);
	}
	
	public AbstractAction getAction() {
		return new SaveAllResultsAsXML();
	}
	
	
	/**
	 * Saves the result data to an XML Baclava file. 
	 */
	protected void saveData(File file) throws Exception{
		

	    
		// Build the DataThing map from the resultReferencesMap
		// First convert map of references to objects into a map of real result objects
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (Iterator<String> i = chosenReferences.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			T2Reference reference = chosenReferences.get(portName);
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

	@Override
	protected String getFilter() {
		return "xml";
	}

}
