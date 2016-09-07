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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class IterationTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -7522904828725470216L;

	public static enum ErrorState {
		NO_ERRORS, INPUT_ERRORS, OUTPUT_ERRORS;
	}

	private ErrorState errorState = ErrorState.NO_ERRORS;
	private List<Integer> iteration;

	public IterationTreeNode() {
		this.setIteration(new ArrayList<Integer>());
	}

	public IterationTreeNode(List<Integer> iteration) {
		this.setIteration(iteration);
	}

	public void setIteration(List<Integer> iteration) {
		this.iteration = iteration;
	}

	public List<Integer> getIteration() {
		return iteration;
	}

	public IterationTreeNode getParentIterationTreeNode() {
		TreeNode parentNode = getParent();
		if (parentNode instanceof IterationTreeNode)
			return (IterationTreeNode) parentNode;
		return null;
	}

	public List<Integer> getParentIteration() {
		IterationTreeNode parentIterationTreeNode = getParentIterationTreeNode();
		if (parentIterationTreeNode != null)
			return parentIterationTreeNode.getIteration();
		return null;
	}

	@Override
	public String toString() {
		boolean isNested = getChildCount() > 0;
		StringBuilder sb = new StringBuilder();
		if (!getIteration().isEmpty() || isNested) {
			// Iteration 3.1.3
			if (isNested)
				sb.append("Nested iteration ");
			else {
				if (getUserObject() == null)
					sb.append("Waiting for iteration ");
				else
					sb.append("Iteration ");
			}
			for (Integer index : getIteration()) {
				sb.append(index + 1);
				sb.append(".");
			}
			if (!getIteration().isEmpty())
				// Remove last .
				sb.delete(sb.length() - 1, sb.length());
		} else
			sb.append("Invocation");

		return sb.toString();
	}

	public void setErrorState(ErrorState errorState) {
		this.errorState = errorState;
		notifyParentErrorState();
	}

	private void notifyParentErrorState() {
		IterationTreeNode parentIterationTreeNode = getParentIterationTreeNode();
		if (parentIterationTreeNode == null)
			return;
		if (parentIterationTreeNode.getErrorState().compareTo(errorState) < 0)
			parentIterationTreeNode.setErrorState(errorState);
	}

	@Override
	public void setParent(MutableTreeNode newParent) {
		super.setParent(newParent);
		notifyParentErrorState();
	}

	public ErrorState getErrorState() {
		return errorState;
	}
}
