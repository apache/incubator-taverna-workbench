package org.apache.taverna.workbench.views.results;
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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.taverna.platform.report.Invocation;

@SuppressWarnings("serial")
public class InvocationTreeNode extends DefaultMutableTreeNode {
	public static enum ErrorState {
		NO_ERRORS,
		INPUT_ERRORS,
		OUTPUT_ERRORS;
	}

	private ErrorState errorState = ErrorState.NO_ERRORS;
	private final Invocation invocation;

	public InvocationTreeNode(Invocation invocation) {
		super(invocation);
		this.invocation = invocation;
	}

	public InvocationTreeNode getParentInvocationTreeNode() {
		TreeNode parentNode = getParent();
		if (parentNode instanceof InvocationTreeNode)
			return (InvocationTreeNode) parentNode;
		return null;
	}

	public Invocation getInvocation() {
		return invocation;
	}

	public String getIndex() {
		StringBuilder sb = new StringBuilder();
		InvocationTreeNode parentNode = getParentInvocationTreeNode();
		if (parentNode != null) {
			String index = parentNode.getIndex();
			if (!index.isEmpty()) {
				sb.append(index);
				sb.append(".");
			}
		}
		int[] index = invocation.getIndex();
		if (index.length == 0)
			sb.append(1);
		else {
			String sep = "";
			for (int i = 0; i < index.length; i++) {
				sb.append(sep).append(index[i]+1);
				sep = ".";
			}
		}
		return sb.toString();
	}

	public Invocation getParentInvocation() {
		InvocationTreeNode parentIterationTreeNode = getParentInvocationTreeNode();
		if (parentIterationTreeNode != null)
			return parentIterationTreeNode.getInvocation();
		return null;
	}

	@Override
	public String toString(){
		boolean isNested = getChildCount() > 0;
		StringBuilder sb = new StringBuilder();
		if (isNested)
			sb.append("Nested invocation ");
		else
			sb.append("Invocation ");
		sb.append(getIndex());
		return sb.toString();
	}

	public void setErrorState(ErrorState errorState) {
		this.errorState = errorState;
		notifyParentErrorState();
	}

	private void notifyParentErrorState() {
		InvocationTreeNode parentIterationTreeNode = getParentInvocationTreeNode();
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
