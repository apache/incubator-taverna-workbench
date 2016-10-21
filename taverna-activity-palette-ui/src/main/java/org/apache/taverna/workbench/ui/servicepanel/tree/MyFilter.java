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
package org.apache.taverna.workbench.ui.servicepanel.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public class MyFilter implements Filter {
	private static final String HTML_MATCH_END = "</font><font color=\"black\">";
	private static final String HTML_MATCH_START = "</font><font color=\"red\">";
	private static final String HTML_POSTFIX = "</font></html>";
	private static final String HTML_PREFIX = "<html><font color=\"black\">";

	private String filterString;
	private boolean superseded;
	private String filterLowerCase;

	public MyFilter(String filterString) {
		this.filterString = filterString;
		this.filterLowerCase = filterString.toLowerCase();
		this.superseded = false;
	}

	private boolean basicFilter(DefaultMutableTreeNode node) {
		if (filterString.isEmpty())
			return true;
		return node.getUserObject().toString().toLowerCase()
				.contains(filterLowerCase);
	}

	@Override
	public boolean pass(DefaultMutableTreeNode node) {
		return basicFilter(node);
	}

	@Override
	public String filterRepresentation(String original) {
		StringBuilder sb = new StringBuilder(HTML_PREFIX);
		int from = 0;
		String originalLowerCase = original.toLowerCase();
		int index = originalLowerCase.indexOf(filterLowerCase, from);
		while (index > -1) {
			sb.append(original.substring(from, index));
			sb.append(HTML_MATCH_START);
			sb.append(original.substring(index,
					index + filterLowerCase.length()));
			sb.append(HTML_MATCH_END);
			from = index + filterLowerCase.length();
			index = originalLowerCase.indexOf(filterLowerCase, from);
		}
		if (from < original.length())
			sb.append(original.substring(from, original.length()));
		return sb.append(HTML_POSTFIX).toString();
	}

	/**
	 * @return the superseded
	 */
	@Override
	public boolean isSuperseded() {
		return superseded;
	}

	/**
	 * @param superseded
	 *            the superseded to set
	 */
	@Override
	public void setSuperseded(boolean superseded) {
		this.superseded = superseded;
	}
}
