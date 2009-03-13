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
package net.sf.taverna.t2.workbench.models.graph.svg;

import java.util.Timer;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

public class SVGGraphComponent extends JComponent {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SVGGraphComponent.class);

	static final String COMPLETED_COLOUR = "grey";

	private static final String OUTPUT_COLOUR = "blue";

	static final String ERROR_COLOUR = "red";

	static final String SELECTED_COLOUR = "blue";

	private static final int OUTPUT_FLASH_PERIOD = 200;

	private static Timer timer = new Timer("SVGGraphComponent timer", true);
	
	public SVGGraphComponent() {
	}
	
//	/**
//	 * Resets the diagram to its original appearance.
//	 * 
//	 */
//	public void reset() {
//		for (SVGShape node : processorMap.values()) {
//			node.setCompleted(0f);
//			node.setIteration(0);
//			node.setErrors(0);
//		}
//	}
//

//	/**
//	 * Returns <code>true</code> if this diagrams contains a Processor with
//	 * the given name.
//	 * 
//	 * @param processorId
//	 *            the id of the Processor
//	 * @return <code>true</code> if this diagrams contains a Processor with
//	 *         the given id.
//	 */
//	public boolean containsProcessor(String processorId) {
//		return processorMap.containsKey(processorId);
//	}
//
//	/**
//	 * Returns <code>true</code> if this diagrams contains a Datalink with the
//	 * given name.
//	 * 
//	 * @param datalinkId
//	 *            the id of the Datalink
//	 * @return <code>true</code> if this diagrams contains a Datalink with the
//	 *         given id.
//	 */
//	public boolean containsDatalink(String datalinkId) {
//		return datalinkMap.containsKey(datalinkId);
//	}
//
//	/**
//	 * Sets the proportion of the processor's jobs that have been completed.
//	 * 
//	 * @param processorId
//	 *            the id of the processor
//	 * @param complete
//	 *            the proportion of the processor's jobs that have been
//	 *            completed, a value between 0.0 and 1.0
//	 */
//	public void setProcessorCompleted(String processorId, float complete) {
//		if (processorMap.containsKey(processorId)) {
//			processorMap.get(processorId).setCompleted(complete);
//		}
//	}
//
//	/**
//	 * Sets the processor's iteration count.
//	 * 
//	 * @param processorId
//	 *            the id of the processor
//	 * @param iteration
//	 *            the number of iteration count
//	 */
//	public void setIteration(String processorId, int iteration) {
//		if (processorMap.containsKey(processorId)) {
//			processorMap.get(processorId).setIteration(iteration);
//		}
//	}
//
//	/**
//	 * Sets the processor's error count.
//	 * 
//	 * @param processorId
//	 *            the id of the processor
//	 * @param errors
//	 *            the number of error count
//	 */
//	public void setErrors(String processorId, int errors) {
//		if (processorMap.containsKey(processorId)) {
//			processorMap.get(processorId).setErrors(errors);
//		}
//	}
//
//	public void fireDatalink(final String datalinkId) {
//		if (datalinkMap.containsKey(datalinkId)) {
//			for (SVGGraphEdge datalink : datalinkMap.get(datalinkId)) {
//				datalink.setColour(OUTPUT_COLOUR);
//			}
//			timer.schedule(new TimerTask() {
//				public void run() {
//					for (SVGGraphEdge datalink : datalinkMap.get(datalinkId)) {
//						datalink.resetStyle();
//					}
//				}
//			}, OUTPUT_FLASH_PERIOD);
//		}
//	}

}
