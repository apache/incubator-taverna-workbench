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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;

/**
 * Node in a processor enactments tree. Contains a particular enactment of the
 * processor.
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorEnactmentsTreeNode extends DefaultMutableTreeNode {
	
	private ProcessorEnactment processorEnactment;
	private List<Integer> iteration;
	private static SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	
	public ProcessorEnactmentsTreeNode(ProcessorEnactment processorEnactment){
		super(processorEnactment);
		this.processorEnactment = processorEnactment;
		this.iteration = ProcessorEnactmentsTreeModel.iterationToIntegerList(processorEnactment.getIteration());
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();		
		if (! iteration.isEmpty()) {			
			// Iteration 3.1.3
			sb.append("Iteration ");
			for (Integer index : iteration) {				
				sb.append(index+1);
				sb.append(".");
			}
			// Remove last .
			sb.delete(sb.length()-1, sb.length());
		} else {
			sb.append("Invocation");
		}
		sb.append(" (Started: ");
		sb.append(ISO_8601.format(processorEnactment.getEnactmentStarted()));
		if (processorEnactment.getEnactmentEnded() != null) {
			sb.append("; Finished: ");
			sb.append(ISO_8601.format(processorEnactment.getEnactmentEnded()));
		} else {
			sb.append("; Not finished");
		}
		sb.append(")");
		return sb.toString();
	}

}
