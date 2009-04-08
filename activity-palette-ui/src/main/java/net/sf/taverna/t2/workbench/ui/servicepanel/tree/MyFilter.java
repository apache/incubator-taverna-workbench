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

	private String filterString;
	private boolean superseded;

	public MyFilter(String filterString) {
		this.filterString = filterString;
		this.superseded = false;
	}

	private boolean basicFilter(DefaultMutableTreeNode node) {
		return node.getUserObject().toString().contains(filterString);
	}

	public boolean pass(DefaultMutableTreeNode node) {
		return basicFilter(node);
	}

	public String filterRepresentation(String original) {
		return ("<html><font color=\"black\">"
				+ original.replace(filterString, "</font><font color=\"red\">"
						+ filterString + "</font><font color=\"black\">") + "</font></html>");
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
