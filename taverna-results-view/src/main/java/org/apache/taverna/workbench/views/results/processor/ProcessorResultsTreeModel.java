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
