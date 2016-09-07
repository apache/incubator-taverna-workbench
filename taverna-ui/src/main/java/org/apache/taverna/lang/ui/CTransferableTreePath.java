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
/*

package org.apache.taverna.lang.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.tree.TreePath;

/**
 * This represents a TreePath (a node in a JTree) that can be transferred
 * between a drag source and a drop target.
 */
public class CTransferableTreePath implements Transferable {
	// The type of DnD object being dragged...
	public static final DataFlavor TREEPATH_FLAVOR = new DataFlavor(
			DataFlavor.javaJVMLocalObjectMimeType, "TreePath");

	private TreePath _path;

	private DataFlavor[] _flavors = { TREEPATH_FLAVOR };

	/**
	 * Constructs a transferrable tree path object for the specified path.
	 */
	public CTransferableTreePath(TreePath path) {
		_path = path;
	}

	// Transferable interface methods...
	public DataFlavor[] getTransferDataFlavors() {
		return _flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return java.util.Arrays.asList(_flavors).contains(flavor);
	}

	public synchronized Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException {
		if (flavor.isMimeTypeEqual(TREEPATH_FLAVOR.getMimeType())) // DataFlavor.javaJVMLocalObjectMimeType))
			return _path;
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
