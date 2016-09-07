package org.apache.taverna.workbench.views.results.processor;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.apache.taverna.workbench.views.results.processor.ProcessorEnactmentsTreeModel.iterationToIntegerList;

import java.util.ArrayList;
import java.util.List;
import org.apache.taverna.provenance.lineageservice.utils.ProcessorEnactment;


/**
 * Node in a processor enactments tree. Contains a particular enactment of the
 * processor.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class ProcessorEnactmentsTreeNode extends IterationTreeNode {
	private List<Integer> myIteration = new ArrayList<>();
	private List<Integer> parentIteration = new ArrayList<>();

	public ProcessorEnactmentsTreeNode(ProcessorEnactment processorEnactment,
			List<Integer> parentIteration) {
		super();
		this.parentIteration = parentIteration;
		setProcessorEnactment(processorEnactment);
	}

	protected void updateFullIteration() {
		List<Integer> fullIteration = new ArrayList<>();
		if (getParentIteration() != null)
			fullIteration.addAll(getParentIteration());
		fullIteration.addAll(getMyIteration());
		setIteration(fullIteration);
	}

	public final List<Integer> getMyIteration() {
		return myIteration;
	}

	@Override
	public final List<Integer> getParentIteration() {
		return parentIteration;
	}

	public final ProcessorEnactment getProcessorEnactment() {
		return (ProcessorEnactment) getUserObject();
	}

	public final void setMyIteration(List<Integer> myIteration) {
		this.myIteration = myIteration;
		updateFullIteration();
	}

	public final void setParentIteration(List<Integer> parentIteration) {
		this.parentIteration = parentIteration;
		updateFullIteration();
	}

	public final void setProcessorEnactment(
			ProcessorEnactment processorEnactment) {
		setUserObject(processorEnactment);
		setMyIteration(iterationToIntegerList(processorEnactment.getIteration()));
	}
}
