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
package net.sf.taverna.t2.reference.ui.tree;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * A cell renderer for the pre-registration tree model, with appropriate
 * rendering for inline strings, web URLs and files. The renderer doesn't
 * attempt to show the contents (other than in the case of inline strings), but
 * does show the URL and File paths for those types along with sensible icons
 * stolen from Eclipse.
 * 
 * @author Tom Oinn
 * 
 */
public class PreRegistrationTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 5284952103994689024L;
	private ImageIcon textIcon = new ImageIcon(getClass().getResource(
			"/icons/wordassist_co.gif"));
	private ImageIcon fileIcon = new ImageIcon(getClass().getResource(
			"/icons/topic.gif"));
	private ImageIcon urlIcon = new ImageIcon(getClass().getResource(
			"/icons/web.gif"));
	private ImageIcon binaryIcon = new ImageIcon(getClass().getResource(
			"/icons/genericregister_obj.gif"));

	@Override
	public synchronized Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		if (value instanceof DefaultMutableTreeNode) {
			Object userObject = ((DefaultMutableTreeNode) value)
					.getUserObject();
			if (userObject == null) {
				setText("List");
			}
			if (tree.getModel().getRoot() == value) {
				setText(userObject.toString());
			} else {
				if (userObject != null) {
					// Handle rendering of string, file, url, byte[] here
					if (userObject instanceof String) {
						setIcon(textIcon);
						String string = (String) userObject;
						if (string.length() < 10) {
							setText(string);
						} else {
							setText(string.substring(0, 10) + "...");
						}
					} else if (userObject instanceof byte[]) {
						byte[] bytes = (byte[]) userObject;
						setIcon(binaryIcon);
						setText("byte[] " + getHumanReadableSize(bytes.length));
					} else if (userObject instanceof File) {
						setIcon(fileIcon);
						setText(((File) userObject).getName());
					} else if (userObject instanceof URL) {
						setIcon(urlIcon);
						setText(((URL) userObject).getHost());
					}
				} else {
					if (expanded) {
						// setIcon(expandedIcon);
					} else {
						// setIcon(unexpandedIcon);
					}
				}
			}
		}
		return this;
	}

	private static String getHumanReadableSize(int size) {
		if (size < 10000) {
			return size + " bytes";
		} else if (size < 2000000) {
			return (int) (size / 1000) + " kB";
		} else if (size < 2000000000) {
			return (int) (size / (1000000)) + " mB";
		} else {
			return (int) (size / (1000000000)) + " gB";
		}
	}

}
