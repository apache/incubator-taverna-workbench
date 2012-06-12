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
import java.util.Map;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.results.BaclavaDocumentHandler;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

/**
 * Stores the entire map of result objects to disk
 * as a single XML data document.
 *
 * For the most part, this class delegates to {@link BaclavaDocumentHandler}
 *
 * @author Tom Oinn
 * @author Alex Nenadic
 * @author Stuart Owen
 */
public class SaveAllResultsAsXML extends SaveAllResultsSPI {

	private static final long serialVersionUID = 452360182978773176L;

	private BaclavaDocumentHandler baclavaDocumentHandler = new BaclavaDocumentHandler();

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
	 * @throws IOException
	 */
	protected void saveData(File file) throws IOException {
		baclavaDocumentHandler.saveData(file);
	}

	@Override
	public void setInvocationContext(InvocationContext context) {
		super.setInvocationContext(context);
		baclavaDocumentHandler.setInvocationContext(context);
	}

	@Override
	public void setChosenReferences(Map<String, T2Reference> chosenReferences) {
		super.setChosenReferences(chosenReferences);
		baclavaDocumentHandler.setChosenReferences(chosenReferences);
	}

	@Override
	public void setReferenceService(ReferenceService referenceService) {
		super.setReferenceService(referenceService);
		baclavaDocumentHandler.setReferenceService(referenceService);
	}

	@Override
	protected String getFilter() {
		return "xml";
	}

}
