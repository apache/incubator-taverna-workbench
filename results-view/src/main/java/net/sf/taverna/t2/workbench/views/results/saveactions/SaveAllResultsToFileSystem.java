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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
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

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class SaveAllResultsToFileSystem extends SaveAllResultsSPI {


	private static Namespace namespace = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");

	public SaveAllResultsToFileSystem(){
		super();
		putValue(NAME, "Save as directory");
		putValue(SMALL_ICON, WorkbenchIcons.saveAllIcon);
	}
	
	public AbstractAction getAction() {
		return new SaveAllResultsToFileSystem();
	}
	
	
	/**
	 * Saves the result data as a file structure 
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
		
		for (String portName : dataThings.keySet()) {
			DataThing thing = dataThings.get(portName);
			thing.writeToFileSystem(file, portName);
		}
	}

	@Override
	protected String getFilter() {
		return null;
	}
}
