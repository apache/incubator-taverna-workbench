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

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;

/**
 * Manager that can handle {@link Edit edits} for a {@link Dataflow}.
 * <p>
 * Edits to a workflow that are to be undoable or redoable should be created by
 * using {@link EditManager#getEdits()} to get an {@link Edits} object. Using
 * this to create {@link Edit}s, instead of calling {@link Edit#doEdit()}, use
 * {@link EditManager#doDataflowEdit(Dataflow, Edit)} to associate the edit with
 * the specified Dataflow.
 * </p>
 * <p>
 * It is possible to undo a series of edits done on a particular dataflow in
 * this way by using {@link #undoDataflowEdit(Dataflow)}. If one or more undoes
 * have been performed, they can be redone step by step using
 * {@link #redoDataflowEdit(Dataflow)}. Note that it is no longer possible to
 * call {@link #redoDataflowEdit(Dataflow)} after a
 * {@link #doDataflowEdit(Dataflow, Edit)}.
 * <p>
 * The EditManager is {@link Observable}. If you
 * {@link Observable#addObserver(net.sf.taverna.t2.lang.observer.Observer) add an observer}
 * you can be notified on {@link DataflowEditEvent edits},
 * {@link DataFlowUndoEvent undoes} and {@link DataFlowRedoEvent redoes}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class EditManager implements Observable<EditManagerEvent> {

	private static EditManager instance;

	/**
	 * Get the {@link EditManager} implementation singleton as discovered
	 * through an {@link SPIRegistry}.
	 * 
	 * @throws IllegalStateException
	 *             If no implementation was found.
	 * @return Discovered {@link EditManager} implementation singleton.
	 */
	public static synchronized EditManager getInstance()
			throws IllegalStateException {
		if (instance == null) {
			SPIRegistry<EditManager> registry = new SPIRegistry<EditManager>(
					EditManager.class);
			try {
				instance = registry.getInstances().get(0);
			} catch (IndexOutOfBoundsException ex) {
				throw new IllegalStateException(
						"Could not find implementation of " + EditManager.class);
			}
		}
		return instance;
	}

	/**
	 * <code>true</code> if {@link #redoDataflowEdit(Dataflow)} on the given
	 * dataflow would redo the last undone edit. If there are no previous edits,
	 * return <code>false</code>.
	 * 
	 * @param dataflow
	 *            {@link Dataflow} which last affecting edit is to be undone
	 * @return <code>true</code if and only if {@link #redoDataflowEdit(Dataflow)} would undo
	 */
	public abstract boolean canRedoDataflowEdit(Dataflow dataflow);

	/**
	 * <code>true</code> if {@link #undoDataflowEdit(Dataflow)} on the given
	 * dataflow would undo the last edit. If there are no previous edits, return
	 * <code>false</code>.
	 * 
	 * @param dataflow
	 *            {@link Dataflow} which last affecting edit is to be undone
	 * @return <code>true</code if {@link #undoDataflowEdit(Dataflow)} would undo
	 */
	public abstract boolean canUndoDataflowEdit(Dataflow dataflow);

	/**
	 * Do an {@link Edit} affecting the given {@link Dataflow}.
	 * <p>
	 * The edit is {@link Edit#doEdit() performed} and the edit can later be
	 * undone using {@link EditManager#undoDataflowEdit(Dataflow)}.
	 * </p>
	 * <p>
	 * Note that any events previously undone with
	 * {@link EditManager#undoDataflowEdit(Dataflow)} for the given dataflow can
	 * no longer be {@link EditManager#redoDataflowEdit(Dataflow) redone} after
	 * calling this method.
	 * </p>
	 * 
	 * @see EditManager#undoDataflowEdit(Dataflow)
	 * @param dataflow
	 *            {@link Dataflow} this edit is affecting
	 * @param edit
	 *            {@link Edit} that should be done using {@link Edit#doEdit()}.
	 * @throws EditException
	 *             If {@link Edit#doEdit()} fails
	 */
	public abstract void doDataflowEdit(Dataflow dataflow, Edit<?> edit)
			throws EditException;

	/**
	 * Get an implementation of {@link Edits} from where {@link Edit} objects
	 * can be created.
	 * 
	 * @return An {@link Edits} implementation.
	 */
	public abstract Edits getEdits();

	/**
	 * Redo the last {@link Edit} that was undone using
	 * {@link #undoDataflowEdit(Dataflow)}.
	 * <p>
	 * Note that the {@link EditManager} might only be able to redo a reasonable
	 * number of steps.
	 * </p>
	 * <p>
	 * It is not possible to use {@link #redoDataflowEdit(Dataflow)} after a
	 * {@link #doDataflowEdit(Dataflow, Edit)} affecting the same
	 * {@link Dataflow}, or if no edits have been undone yet. No action would
	 * be taken in these cases.
	 * </p>
	 * 
	 * @param dataflow
	 *            {@link Dataflow} which last affecting edit is to be redone
	 * @throws EditException
	 *             If {@link Edit#doEdit()} fails
	 */
	public abstract void redoDataflowEdit(Dataflow dataflow)
			throws EditException;

	/**
	 * Undo the last {@link Edit} affecting the given {@link Dataflow}.
	 * <p>
	 * This can be called in succession until there are no more known undoes.
	 * Note that the {@link EditManager} might only be able to undo a reasonable
	 * number of steps.
	 * <p>
	 * The last edit must have been performed using
	 * {@link EditManager#doDataflowEdit(Dataflow, Edit)} or
	 * {@link EditManager#redoDataflowEdit(Dataflow)}. The undo is done using
	 * {@link Edit#undo()}. If no edits have been performed for the dataflow
	 * yet, no action is taken.
	 * </p>
	 * <p>
	 * Undoes can be redone using {@link #redoDataflowEdit(Dataflow)}.
	 * 
	 * @param dataflow
	 *            {@link Dataflow} which last affecting edit is to be undone
	 */
	public abstract void undoDataflowEdit(Dataflow dataflow);

	/**
	 * An event about an {@link Edit} on a {@link Dataflow}, accessible through
	 * {@link AbstractDataflowEditEvent#getEdit()} and
	 * {@link AbstractDataflowEditEvent#getDataFlow()}.
	 * 
	 */
	public static abstract class AbstractDataflowEditEvent implements
			EditManagerEvent {
		private final Dataflow dataFlow;
		private final Edit<?> edit;

		public AbstractDataflowEditEvent(Dataflow dataFlow, Edit<?> edit) {
			if (dataFlow == null || edit == null) {
				throw new NullPointerException(
						"Dataflow and/or Edit can't be null");
			}
			this.dataFlow = dataFlow;
			this.edit = edit;

		}

		/**
		 * The {@link Dataflow} this event affected.
		 * 
		 * @return A {@link Dataflow}
		 */
		public Dataflow getDataFlow() {
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
	 * {@link Dataflow}.
	 * 
	 */
	public static class DataflowEditEvent extends AbstractDataflowEditEvent {
		public DataflowEditEvent(Dataflow dataFlow, Edit<?> edit) {
			super(dataFlow, edit);
		}
	}

	/**
	 * An event sent when a previously undone {@link Edit} has been redone on a
	 * {@link Dataflow}.
	 * 
	 */
	public static class DataFlowRedoEvent extends AbstractDataflowEditEvent {
		public DataFlowRedoEvent(Dataflow dataFlow, Edit<?> edit) {
			super(dataFlow, edit);
		}
	}

	/**
	 * An event sent when an {@link Edit} has been undone on a {@link Dataflow}.
	 * 
	 */
	public static class DataFlowUndoEvent extends AbstractDataflowEditEvent {
		public DataFlowUndoEvent(Dataflow dataFlow, Edit<?> edit) {
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
