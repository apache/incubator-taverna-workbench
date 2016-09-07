/*******************************************************************************
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
package org.apache.taverna.workbench.edits;
