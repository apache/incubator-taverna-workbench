/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.servicepanel.tree;

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
		if (filterString.equals("")) {
			return true;
		}
		return node.getUserObject().toString().toLowerCase().contains(filterLowerCase);
	}

	public boolean pass(DefaultMutableTreeNode node) {
		return basicFilter(node);
	}

	public String filterRepresentation(String original) {
		StringBuffer sb = new StringBuffer();
		sb.append(HTML_PREFIX);
		int from = 0;
		String originalLowerCase = original.toLowerCase();
		int index = originalLowerCase.indexOf(filterLowerCase, from);
		while (index > -1) {
			sb.append(original.substring(from, index));
			sb.append(HTML_MATCH_START);
			sb.append(original.substring(index, index+filterLowerCase.length()));
			sb.append(HTML_MATCH_END);
			from = index+filterLowerCase.length();
			index = originalLowerCase.indexOf(filterLowerCase, from);
		}
		if (from < original.length()) {
			sb.append(original.substring(from, original.length()));
		}
		sb.append(HTML_POSTFIX);
		return sb.toString();
	}

	/**
	 * @return the superseded
	 */
	public boolean isSuperseded() {
		return superseded;
	}

	/**
	 * @param superseded
	 *            the superseded to set
	 */
	public void setSuperseded(boolean superseded) {
		this.superseded = superseded;
	}

}
