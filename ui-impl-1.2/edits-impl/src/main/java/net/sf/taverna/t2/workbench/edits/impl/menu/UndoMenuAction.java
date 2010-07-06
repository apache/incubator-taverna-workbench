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
package net.sf.taverna.t2.workbench.edits.impl.menu;

import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;

import org.apache.log4j.Logger;

/**
 * Undo the last {@link Edit} done on the current workflow using the
 * {@link EditManager}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class UndoMenuAction extends AbstractUndoMenuAction {

	private static Logger logger = Logger.getLogger(UndoMenuAction.class);

	public UndoMenuAction() {
		super(10);
	}

	@Override
	protected Action createAction() {
		return new AbstractUndoAction("Undo") {
			@Override
			protected boolean isActive(Dataflow dataflow) {
				return editManager.canUndoDataflowEdit(dataflow);
			}

			@Override
			protected void performUndoOrRedo(Dataflow dataflow) {
				try {
					editManager.undoDataflowEdit(dataflow);
				} catch (RuntimeException e) {
					logger.warn("Could not undo for " + dataflow, e);
					JOptionPane.showMessageDialog(null,
							"Could not undo for workflow " + dataflow + ":\n"
									+ e, "Could not undo",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}

}
