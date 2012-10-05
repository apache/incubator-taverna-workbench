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
package net.sf.taverna.t2.workbench.edits;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;

/**
 * Manager that can handle {@link Edit edits} for a {@link WorkflowBundle}.
 * <p>
 * Edits to a workflow that are to be undoable or redoable should be created by
 * using {@link EditManager#getEdits()} to get an {@link Edits} object. Using
 * this to create {@link Edit}s, instead of calling {@link Edit#doEdit()}, use
 * {@link EditManager#doDataflowEdit(WorkflowBundle, Edit)} to associate the edit with
 * the specified Dataflow.
 * </p>
 * <p>
 * It is possible to undo a series of edits done on a particular dataflow in
 * this way by using {@link #undoDataflowEdit(WorkflowBundle)}. If one or more undoes
 * have been performed, they can be redone step by step using
 * {@link #redoDataflowEdit(WorkflowBundle)}. Note that it is no longer possible to
 * call {@link #redoDataflowEdit(WorkflowBundle)} after a
 * {@link #doDataflowEdit(WorkflowBundle, Edit)}.
 * <p>
 * The EditManager is {@link Observable}. If you
 * {@link Observable#addObserver(net.sf.taverna.t2.lang.observer.Observer) add an observer}
 * you can be notified on {@link DataflowEditEvent edits},
 * {@link DataFlowUndoEvent undoes} and {@link DataFlowRedoEvent redoes}.
 *
 * @author Stian Soiland-Reyes
 *
 */
public interface EditManager extends Observable<EditManagerEvent> {

	/**
	 * <code>true</code> if {@link #redoDataflowEdit(WorkflowBundle)} on the given
	 * dataflow would redo the last undone edit. If there are no previous edits,
	 * return <code>false</code>.
	 *
	 * @param dataflow
	 *            {@link WorkflowBundle} which last affecting edit is to be undone
	 * @return <code>true</code if and only if {@link #redoDataflowEdit(WorkflowBundle)} would undo
	 */
	public abstract boolean canRedoDataflowEdit(WorkflowBundle dataflow);

	/**
	 * <code>true</code> if {@link #undoDataflowEdit(WorkflowBundle)} on the given
	 * dataflow would undo the last edit. If there are no previous edits, return
	 * <code>false</code>.
	 *
	 * @param dataflow
	 *            {@link WorkflowBundle} which last affecting edit is to be undone
	 * @return <code>true</code if {@link #undoDataflowEdit(WorkflowBundle)} would undo
	 */
	public abstract boolean canUndoDataflowEdit(WorkflowBundle dataflow);

	/**
	 * Do an {@link Edit} affecting the given {@link WorkflowBundle}.
	 * <p>
	 * The edit is {@link Edit#doEdit() performed} and the edit can later be
	 * undone using {@link EditManager#undoDataflowEdit(WorkflowBundle)}.
	 * </p>
	 * <p>
	 * Note that any events previously undone with
	 * {@link EditManager#undoDataflowEdit(WorkflowBundle)} for the given dataflow can
	 * no longer be {@link EditManager#redoDataflowEdit(WorkflowBundle) redone} after
	 * calling this method.
	 * </p>
	 *
	 * @see EditManager#undoDataflowEdit(WorkflowBundle)
	 * @param dataflow
	 *            {@link WorkflowBundle} this edit is affecting
	 * @param edit
	 *            {@link Edit} that should be done using {@link Edit#doEdit()}.
	 * @throws EditException
	 *             If {@link Edit#doEdit()} fails
	 */
	public abstract void doDataflowEdit(WorkflowBundle dataflow, Edit<?> edit)
			throws EditException;

	/**
	 * Redo the last {@link Edit} that was undone using
	 * {@link #undoDataflowEdit(WorkflowBundle)}.
	 * <p>
	 * Note that the {@link EditManager} might only be able to redo a reasonable
	 * number of steps.
	 * </p>
	 * <p>
	 * It is not possible to use {@link #redoDataflowEdit(WorkflowBundle)} after a
	 * {@link #doDataflowEdit(WorkflowBundle, Edit)} affecting the same
	 * {@link WorkflowBundle}, or if no edits have been undone yet. No action would
	 * be taken in these cases.
	 * </p>
	 *
	 * @param dataflow
	 *            {@link WorkflowBundle} which last affecting edit is to be redone
	 * @throws EditException
	 *             If {@link Edit#doEdit()} fails
	 */
	public abstract void redoDataflowEdit(WorkflowBundle dataflow)
			throws EditException;

	/**
	 * Undo the last {@link Edit} affecting the given {@link WorkflowBundle}.
	 * <p>
	 * This can be called in succession until there are no more known undoes.
	 * Note that the {@link EditManager} might only be able to undo a reasonable
	 * number of steps.
	 * <p>
	 * The last edit must have been performed using
	 * {@link EditManager#doDataflowEdit(WorkflowBundle, Edit)} or
	 * {@link EditManager#redoDataflowEdit(WorkflowBundle)}. The undo is done using
	 * {@link Edit#undo()}. If no edits have been performed for the dataflow
	 * yet, no action is taken.
	 * </p>
	 * <p>
	 * Undoes can be redone using {@link #redoDataflowEdit(WorkflowBundle)}.
	 *
	 * @param dataflow
	 *            {@link WorkflowBundle} which last affecting edit is to be undone
	 */
	public abstract void undoDataflowEdit(WorkflowBundle dataflow);

	/**
	 * An event about an {@link Edit} on a {@link WorkflowBundle}, accessible through
	 * {@link AbstractDataflowEditEvent#getEdit()} and
	 * {@link AbstractDataflowEditEvent#getDataFlow()}.
	 *
	 */
	public static abstract class AbstractDataflowEditEvent implements
			EditManagerEvent {
		private final WorkflowBundle dataFlow;
		private final Edit<?> edit;

		public AbstractDataflowEditEvent(WorkflowBundle dataFlow, Edit<?> edit) {
			if (dataFlow == null || edit == null) {
				throw new NullPointerException(
						"Dataflow and/or Edit can't be null");
			}
			this.dataFlow = dataFlow;
			this.edit = edit;

		}

		/**
		 * The {@link WorkflowBundle} this event affected.
		 *
		 * @return A {@link WorkflowBundle}
		 */
		public WorkflowBundle getDataFlow() {
			return dataFlow;
		}

		/**
		 * The {@link Edit} that was performed, undoed or redone on the
		 * {@link #getDataFlow() dataflow}.
		 *
		 * @return An {@link Edit}
		 */
		public Edit<?> getEdit() {
			return edit;
		}
	}

	/**
	 * An event sent when an {@link Edit} has been performed on a
	 * {@link WorkflowBundle}.
	 *
	 */
	public static class DataflowEditEvent extends AbstractDataflowEditEvent {
		public DataflowEditEvent(WorkflowBundle dataFlow, Edit<?> edit) {
			super(dataFlow, edit);
		}
	}

	/**
	 * An event sent when a previously undone {@link Edit} has been redone on a
	 * {@link WorkflowBundle}.
	 *
	 */
	public static class DataFlowRedoEvent extends AbstractDataflowEditEvent {
		public DataFlowRedoEvent(WorkflowBundle dataFlow, Edit<?> edit) {
			super(dataFlow, edit);
		}
	}

	/**
	 * An event sent when an {@link Edit} has been undone on a {@link WorkflowBundle}.
	 *
	 */
	public static class DataFlowUndoEvent extends AbstractDataflowEditEvent {
		public DataFlowUndoEvent(WorkflowBundle dataFlow, Edit<?> edit) {
			super(dataFlow, edit);
		}
	}

	/**
	 * An event given to {@link Observer}s registered with
	 * {@link Observable#addObserver(Observer)}.
	 */
	public interface EditManagerEvent {
		public Edit<?> getEdit();
	}

}
