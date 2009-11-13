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
package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import javax.swing.JFrame;

import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

public class RunIterationStrategyEditor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IterationStrategyImpl iterationStrategyImpl = new IterationStrategyImpl();
		NamedInputPortNode fishPort = new NamedInputPortNode("fish", 2);
		NamedInputPortNode otherPort = new NamedInputPortNode("other", 0);
		NamedInputPortNode soupPort = new NamedInputPortNode("soup", 1);
		iterationStrategyImpl.addInput(fishPort);
		iterationStrategyImpl.addInput(soupPort);
		iterationStrategyImpl.addInput(otherPort);

		iterationStrategyImpl.connectDefault(otherPort);
		iterationStrategyImpl.connectDefault(fishPort);
		iterationStrategyImpl.connectDefault(soupPort);
		
		IterationStrategyEditorControl editorControl = new IterationStrategyEditorControl(iterationStrategyImpl);
		
		JFrame frame = new JFrame("List handling editor");
		frame.add(editorControl);
		frame.setSize(500,400);
		frame.setVisible(true);
		
		
	}

}
