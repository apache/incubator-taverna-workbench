package org.apache.taverna.reference.ui.tree;
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

import java.awt.Component;
import java.io.File;
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
	private static int MAXIMUM_TEXT_LENGTH = 14;

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
		if (value instanceof DefaultMutableTreeNode)
			renderPreRegistrationCell(tree, value, expanded,
					((DefaultMutableTreeNode) value).getUserObject());
		return this;
	}

	private void renderPreRegistrationCell(JTree tree, Object value,
			boolean expanded, Object userObject) {
		if (userObject == null) {
			setText("List");
		} else if (tree.getModel().getRoot() == value) {
			setText(userObject.toString());
		} else {
			// Handle rendering of string, file, url, byte[] here
			if (userObject instanceof String) {
				setIcon(textIcon);
				String string = (String) userObject;
				if (string.length() < MAXIMUM_TEXT_LENGTH)
					setText(string);
				else
					setText(string.substring(0, MAXIMUM_TEXT_LENGTH - 4)
							+ "...");
			} else if (userObject instanceof byte[]) {
				byte[] bytes = (byte[]) userObject;
				setIcon(binaryIcon);
				setText("byte[] " + getHumanReadableSize(bytes.length));
			} else if (userObject instanceof File) {
				setIcon(fileIcon);
				File f = (File) userObject;
				setText(f.getName());
			} else if (userObject instanceof URL) {
				setIcon(urlIcon);
				URL url = (URL) userObject;
				setText(url.getHost());
			} else {
				if (expanded) {
					// setIcon(expandedIcon);
				} else {
					// setIcon(unexpandedIcon);
				}
			}
		}
	}

	private static String getHumanReadableSize(int size) {
		if (size < 10000)
			return size + " bytes";
		else if (size < 2000000)
			return (int) (size / 1000) + " kB";
		else if (size < 2000000000)
			return (int) (size / 1000000) + " mB";
		else
			return (int) (size / 1000000000) + " gB";
	}
}
