package org.apache.taverna.workbench.views.results.saveactions;
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

import java.nio.file.Path;

import javax.swing.AbstractAction;

/**
 * Defines an interface for various actions for saving results of a workflow
 * run. Path to a single result data is contained inside a MutableTreeNode,
 * which can be used by actions that only want to save the current result. The
 * interface also contains a list of output ports that can be used to
 * dereference all outputs, for actions wishing to save a all results (e.g. in
 * different formats).
 * 
 * @author Alex Nenadic
 * @author David Withers
 */
public interface SaveIndividualResultSPI {
	/**
	 * Sets the Path pointing to the result to be saved.
	 */
	void setResultReference(Path reference);

	/**
	 * Returns the save result action implementing this interface.
	 */
	AbstractAction getAction();
}
