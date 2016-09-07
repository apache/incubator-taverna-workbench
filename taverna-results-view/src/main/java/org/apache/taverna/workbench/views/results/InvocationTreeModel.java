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

import static javax.swing.SwingUtilities.invokeLater;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.apache.taverna.workbench.ui.Updatable;
import org.apache.taverna.platform.report.Invocation;
import org.apache.taverna.platform.report.StatusReport;

/**
 * TreeModel for displaying invocations.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class InvocationTreeModel extends DefaultTreeModel implements Updatable {
	private final StatusReport<?, ?> report;
	private Map<String, InvocationTreeNode> nodes = new HashMap<>();

	/**
	 * Constructs a new TreeModel for displaying invocations.
	 *
	 * @param report
	 *            the report to display
	 */
	public InvocationTreeModel(StatusReport<?, ?> report) {
		super(new DefaultMutableTreeNode());
		this.report = report;
		updateTree(report.getInvocations());
	}

	public InvocationTreeNode getFirstInvocationNode() {
		return nodes.get(report.getInvocations().first().getId());
	}

	private void updateTree(SortedSet<Invocation> invocations) {
		for (Invocation invocation : invocations) {
			String invocationId = invocation.getId();
			if (!nodes.containsKey(invocationId))
				nodes.put(invocationId, createNode(invocation));
		}
	}

	private InvocationTreeNode createNode(Invocation invocation) {
		InvocationTreeNode node = new InvocationTreeNode(invocation);
		Invocation parent = invocation.getParent();
		if (parent != null) {
			Invocation grandParent = parent.getParent();
			if (grandParent != null) {
				Invocation greatGrandParent = grandParent.getParent();
				if (greatGrandParent != null) {
					String invocationId = greatGrandParent.getId();
					if (!nodes.containsKey(invocationId))
						nodes.put(invocationId, createNode(greatGrandParent));
					MutableTreeNode parentNode = nodes.get(invocationId);
					insertNodeInto(node, parentNode, parentNode.getChildCount());
					return node;
				}
			}
		}

		MutableTreeNode parentNode = ((MutableTreeNode) getRoot());
		insertNodeInto(node, parentNode, parentNode.getChildCount());
		return node;
	}

	@Override
	public void update() {
		invokeLater(new Runnable() {
			@Override
			public void run() {
				updateTree(report.getInvocations());
			}
		});
	}
}
