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
/**
 * A {@link net.sf.taverna.t2.workbench.edits.EditManager} that can manage
 * {@link net.sf.taverna.t2.workflowmodel.Edit}s performed from the UI.
 * <p>
 * To perform an edit that is to be undoable, use
 * {@link EditManager#doDataflowEdit(net.sf.taverna.t2.workflowmodel.Dataflow, net.sf.taverna.t2.workflowmodel.Edit)}
 * instead of {@link net.sf.taverna.t2.workflowmodel.Edit#doEdit()}. Such edits
 * can be
 * {@link EditManager#undoDataflowEdit(net.sf.taverna.t2.workflowmodel.Dataflow) undone}
 * and
 * {@link EditManager#redoDataflowEdit(net.sf.taverna.t2.workflowmodel.Dataflow) redone}.
 * </p>
 * <p>
 * Edits are organised by {@link net.sf.taverna.t2.workflowmodel.Dataflow} so
 * that if a user changes the active workflow in the Workbench and does "Undo" -
 * that would undo the last undo done related to that workflow.
 * </p>
 * <p>
 * The {@link net.sf.taverna.t2.workbench.edits.impl} implementation of the
 * EditManager is discovered by {@link net.sf.taverna.t2.workbench.edits.EditManager#getInstance()}. The
 * implementation also includes {@link net.sf.taverna.t2.ui.menu.MenuComponent}s
 * for Undo and Redo.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
package net.sf.taverna.t2.workbench.edits;
