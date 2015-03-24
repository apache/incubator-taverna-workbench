/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;

/**
 * Tree model for the results of a processor (workflow's intermediate results).
 *
 * @author Alex Nenadic
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ProcessorResultsTreeModel extends DefaultTreeModel {
	private static final Logger logger = Logger
			.getLogger(ProcessorResultsTreeModel.class);
	// Tree root
	private ProcessorResultTreeNode root;

	public ProcessorResultsTreeModel(Path path) {
		super(new ProcessorResultTreeNode());
		root = (ProcessorResultTreeNode) getRoot();
		createTree(path, root);
	}

	private void createTree(Path path, ProcessorResultTreeNode parentNode){
		// If reference contains a list of data references
		if (DataBundles.isList(path)) {
			try {
				List<Path> list = DataBundles.getList(path);
				ProcessorResultTreeNode listNode = new ProcessorResultTreeNode(
						list.size(), path); // list node
				parentNode.add(listNode);
				for (Path ref : list)
					createTree(ref, listNode);
			} catch (IOException e) {
				logger.error("Could not resolve list " + path + ", was run with in-memory storage?");
			}
		} else // reference to single data or an error
			insertDataNode(path, parentNode);
	}

	private void insertDataNode(Path path, ProcessorResultTreeNode parent) {
		ProcessorResultTreeNode dataNode = new ProcessorResultTreeNode(path); // data node
		parent.add(dataNode);
	}
}
