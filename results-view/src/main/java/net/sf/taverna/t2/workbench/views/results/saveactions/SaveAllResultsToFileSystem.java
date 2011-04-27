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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

@SuppressWarnings("serial")
public class SaveAllResultsToFileSystem extends SaveAllResultsSPI {

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
	 * @throws IOException 
	 */
	protected void saveData(File file) throws IOException {
		

		// First convert map of references to objects into a map of real result objects
		for (String portName : chosenReferences.keySet()) {
 			DataThing thing = DataThingFactory.bake(getObjectForName(portName));
			thing.writeToFileSystem(file, portName);
		}
	}

	@Override
	protected String getFilter() {
		return null;
	}
}
