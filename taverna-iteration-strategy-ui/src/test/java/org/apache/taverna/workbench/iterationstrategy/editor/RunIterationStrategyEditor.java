/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.taverna.workbench.iterationstrategy.editor;

import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.iterationstrategy.CrossProduct;
import org.apache.taverna.scufl2.api.iterationstrategy.PortNode;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.workbench.iterationstrategy.editor.IterationStrategyEditorControl;
import javax.swing.JFrame;


public class RunIterationStrategyEditor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Processor p = new Processor();
		InputProcessorPort fish = new InputProcessorPort(p, "fish");
		fish.setDepth(2);
		InputProcessorPort other = new InputProcessorPort(p, "other");
		other.setDepth(0);
		InputProcessorPort soup = new InputProcessorPort(p, "soup");
		soup.setDepth(1);
		
		CrossProduct iterationStrategy = new CrossProduct();
		iterationStrategy.add(new PortNode(iterationStrategy, fish));
		iterationStrategy.add(new PortNode(iterationStrategy, other));
		iterationStrategy.add(new PortNode(iterationStrategy, soup));
		p.getIterationStrategyStack().add(iterationStrategy);
		
		
		IterationStrategyEditorControl editorControl = new IterationStrategyEditorControl(p);
		
		JFrame frame = new JFrame("List handling editor");
		frame.add(editorControl);
		frame.setSize(500,400);
		frame.setVisible(true);
		
		
	}

}
