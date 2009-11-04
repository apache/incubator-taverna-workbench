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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import net.sf.taverna.t2.reference.ui.RegistrationPanel;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * Loads a set of input values from an XML document
 */
public class LoadInputsFromXML extends AbstractAction implements ReferenceActionSPI {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5031867688853589341L;
	private Map<String, RegistrationPanel> inputPanelMap;
	
	public LoadInputsFromXML(){
		super();
		putValue(NAME, "Load previous values");
		putValue(SMALL_ICON, WorkbenchIcons.xmlNodeIcon);
	}
	
	public AbstractAction getAction() {
		return new LoadInputsFromXML();
	}


	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		int returnValue = chooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
        	try {
            File file = chooser.getSelectedFile();
            InputStreamReader stream;
				stream = new InputStreamReader(
				        new FileInputStream(file), Charset.forName("UTF-8"));
            Document inputDoc = new SAXBuilder(false).build(stream);
            Map<String, DataThing> inputMap = DataThingXMLFactory
                    .parseDataDocument(inputDoc);
            for (String portName : inputMap.keySet()) {
            	RegistrationPanel panel = inputPanelMap.get(portName);
            	Object o = inputMap.get(portName).getDataObject();
            	if (o != null) {
            		int objectDepth = getObjectDepth(o);
                   	if ((panel != null) && (objectDepth <= panel.getDepth())) {
                		panel.setValue(o, objectDepth);
                	}
           	}
             	
            }
        	}
        	catch (Exception ex) {
        		// Nothing
        	}
        }
	}

	public void setInputPanelMap(Map<String, RegistrationPanel> inputPanelMap) {
		this.inputPanelMap = inputPanelMap;
	}
	
	@SuppressWarnings("unchecked")
	private int getObjectDepth(Object o) {
		int result = 0;
		if (o instanceof Iterable) {
			result++;
			Iterator i = ((Iterable) o).iterator();
			
			if (i.hasNext()) {
				Object child = i.next();
				result = result + getObjectDepth(child);
			}
		}
		return result;
	}
}
