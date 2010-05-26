/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;

/**
 * Model of the tree that contains enactments of a processor.
 * Clicking on the nodes of this tree triggers showing of
 * results for this processor for this particular enactment (invocation).
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorEnactmentsTreeModel extends DefaultTreeModel{

	public ProcessorEnactmentsTreeModel(List<ProcessorEnactment> processorEnactments){
		
		super(new DefaultMutableTreeNode("Invocations of processor"));
		
		for (ProcessorEnactment processorEnactment : processorEnactments){
			((DefaultMutableTreeNode) getRoot()).add(new ProcessorEnactmentsTreeNode(processorEnactment));
		}
	}
}
