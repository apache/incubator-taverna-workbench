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

import java.util.Map;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Implementing classes are capable of storing a collection
 * of T2References held in a result map. One possible 
 * implementation of this will create a directory
 * structure on disk to hold the results and populate it, others
 * could generate Excel or populate some kind of SRB system,
 * not that we're using the SRB at all, heaven forbid. Obviously
 * I meant the MIR. Yes. That would be it.
 * 
 * @author Tom Oinn
 * @author Alex Nenadic
 */
public interface SaveAllResultsSPI {

	/**
	 * Sets the map of references to the results to be saved. The key is the output 
	 * port name and the T2Reference points to an individual result, an error document
	 * or a list of results/error documents.
	 */
	public void setResultReferencesMap(Map<String, T2Reference> resultReferencesMap);

	/**
	 * Sets the InvocationContext to be used to get the Reference Service 
	 * to be used dereference the reference.
	 */
	public void setInvocationContext(InvocationContext ctxt);
	
	/**
	 * Returns the save result action implementing this interface. 
	 * The returned action will be bound to the appropriate UI component 
	 * used to trigger the save action.
	 */
	public AbstractAction getAction();

/**The Map passed into this method
     * contains the String -> T2Reference (port name to reference to 
     * value pairs) returned by the current set of results. 
     * The actual listener may well wish
     * to display some kind of dialog, for example in the case of an Excel
     * export plugin it would be reasonable to give the user some choice
     * over where the results would be inserted into the sheet, and
     * also where the generated file would be stored.<p>
     * The parent parameter is optional and may be set to null, if not
     * it is assumed to be the parent component in the UI which caused
     * this action to be created, this allows save dialogs etc to be
     * placed correctly.
     */
    
}

