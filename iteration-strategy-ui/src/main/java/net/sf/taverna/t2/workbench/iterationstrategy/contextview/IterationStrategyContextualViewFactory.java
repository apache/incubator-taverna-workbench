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
package net.sf.taverna.t2.workbench.iterationstrategy.contextview;

import java.util.Collection;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class IterationStrategyContextualViewFactory implements
		ContextualViewFactory<ActivityInputPort> {

	public boolean canHandle(Object selection) {
		return selection instanceof ActivityInputPort;
	}

	public ContextualView getView(ActivityInputPort inputPort) {
		// FIXME: The selection should include the full path
		// of the selection, not just the end-node

		FileManager fileManager = FileManager.getInstance();
		// Try first to find the selection within the current dataflow
		Dataflow currentDataflow = fileManager.getCurrentDataflow();
		Collection<Processor> processors = Tools
				.getProcessorsWithActivityInputPort(currentDataflow, inputPort);
		if (processors.isEmpty()) {
			// Look in all the open dataflows
			for (Dataflow openDataflow : fileManager.getOpenDataflows()) {
				processors = Tools.getProcessorsWithActivityInputPort(
						openDataflow, inputPort);
				if (!processors.isEmpty()) {
					// First hit is as good as any
					break;
				}
			}
		}
		if (processors.isEmpty()) {
			throw new IllegalStateException(
					"Can't find processor which activity contains input port "
							+ inputPort);
		}

		// Note: We don't know which of these processors contained the
		// activity, so we'll just use the first one
		Processor firstProcessor = processors.iterator().next();
		return new IterationStrategyContextualView(firstProcessor);
	}

}
