/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.workbench.loop;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.AddLayerFactorySPI;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.ErrorBounce;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Loop;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;

public class AddLoopFactory implements AddLayerFactorySPI {

	private static Logger logger = Logger.getLogger(AddLoopFactory.class);

	public boolean canAddLayerFor(Processor proc) {
		DispatchStack dispatchStack = proc.getDispatchStack();
		for (DispatchLayer<?> layer : dispatchStack.getLayers()) {
			if (layer instanceof Loop) {
				return false;
			}
		}
		// Not found
		return true;
	}

	public Action getAddLayerActionFor(final Processor processor) {
		return new AbstractAction("Add looping") {
			public void actionPerformed(ActionEvent e) {
				try {
					findLoopLayer();
				} catch (EditException e1) {
					logger.warn("Can't add loop layer", e1);
				}
			}

			public void findLoopLayer() throws EditException {
				DispatchStack dispatchStack = processor.getDispatchStack();
				Loop loopLayer = null;
				for (DispatchLayer<?> layer : dispatchStack.getLayers()) {
					if (layer instanceof Loop) {
						loopLayer = (Loop) layer;
					}
				}
				if (loopLayer == null) {
					loopLayer = new Loop();
					insertLoopLayer(dispatchStack, loopLayer);
				}

			}

			private void insertLoopLayer(DispatchStack dispatchStack,
					Loop loopLayer) throws EditException {
				Edits edits = EditsRegistry.getEdits();

				// TODO: Make a real Edit for inserting layer
				List<DispatchLayer<?>> layers = dispatchStack.getLayers();
				int loopLayerPosition = 0;
				for (int layerPosition = 0; layerPosition < layers.size(); layerPosition++) {
					DispatchLayer<?> dispatchLayer = layers.get(layerPosition);
					if (dispatchLayer instanceof Parallelize) {
						// At least below Parallelize
						loopLayerPosition = layerPosition + 1;
					}
					if (dispatchLayer instanceof ErrorBounce) {
						// But preferably below ErrorBounce
						loopLayerPosition = layerPosition + 1;
						break;
					}
				}
				final EditManager editManager = EditManager.getInstance();
				final FileManager fileManager = FileManager.getInstance();
				Edit<DispatchStack> edit = edits.getAddDispatchLayerEdit(dispatchStack, loopLayer,
						loopLayerPosition);
				editManager.doDataflowEdit(fileManager.getCurrentDataflow(), edit);
			}

		};
	}
}
