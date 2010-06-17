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

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;

/**
 * Node in a processor enactments tree. Contains a particular enactment of the
 * processor.
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorEnactmentsTreeNode extends IterationTreeNode {
	
	private List<Integer> myIteration = new ArrayList<Integer>();
	private List<Integer> parentIteration = new ArrayList<Integer>();

	private ProcessorEnactment processorEnactment;
	private final boolean containsErrorsInOutputs;

	public ProcessorEnactmentsTreeNode(ProcessorEnactment processorEnactment, List<Integer> parentIteration, boolean containsErrorsInOutputs){
		super();
		this.containsErrorsInOutputs = containsErrorsInOutputs;
		setUserObject(processorEnactment);
		this.parentIteration = parentIteration;
		setProcessorEnactment(processorEnactment);		
	}
	
	public boolean hasErrors() {
		if (getChildCount() > 0) {
			return super.hasErrors();
		}
		return containsErrorsInOutputs();
		
	}



	protected void updateFullIteration() {
		List<Integer> fullIteration = new ArrayList<Integer>();
		if (getParentIteration() != null) {
			fullIteration.addAll(getParentIteration());
		}
		fullIteration.addAll(getMyIteration());
		setIteration(fullIteration);
	}

	public final List<Integer> getMyIteration() {
		return myIteration;
	}

	public final List<Integer> getParentIteration() {
		return parentIteration;
	}

	public final ProcessorEnactment getProcessorEnactment() {
		return processorEnactment;
	}

	public final void setMyIteration(List<Integer> myIteration) {
		this.myIteration = myIteration;
		updateFullIteration();
	}
	
	public final void setParentIteration(List<Integer> parentIteration) {
		this.parentIteration = parentIteration;
		updateFullIteration();
	}
	
	public final void setProcessorEnactment(ProcessorEnactment processorEnactment) {
		this.processorEnactment = processorEnactment;
		setMyIteration(ProcessorEnactmentsTreeModel.iterationToIntegerList(processorEnactment.getIteration()));
	}

	public boolean containsErrorsInOutputs() {
		return containsErrorsInOutputs;
	}
	

}
