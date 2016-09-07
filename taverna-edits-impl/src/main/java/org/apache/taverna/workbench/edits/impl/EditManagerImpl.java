/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.edits.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.taverna.lang.observer.MultiCaster;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Implementation of {@link EditManager}.
 *
 * @author Stian Soiland-Reyes
 */
public class EditManagerImpl implements EditManager {
	private static Logger logger = Logger.getLogger(EditManagerImpl.class);

	private MultiCaster<EditManagerEvent> multiCaster = new MultiCaster<>(this);
	private Map<WorkflowBundle, DataflowEdits> editsForDataflow = new HashMap<>();

	@Override
	public void addObserver(Observer<EditManagerEvent> observer) {
		multiCaster.addObserver(observer);
	}

	@Override
	public boolean canRedoDataflowEdit(WorkflowBundle dataflow) {
		DataflowEdits edits = getEditsForDataflow(dataflow);
		return edits.canRedo();
	}

	@Override
	public boolean canUndoDataflowEdit(WorkflowBundle dataflow) {
		DataflowEdits edits = getEditsForDataflow(dataflow);
		return edits.canUndo();
	}

	@Override
	public void doDataflowEdit(WorkflowBundle dataflow, Edit<?> edit)
			throws EditException {
		// We do the edit before we notify the observers
		DataflowEdits edits = getEditsForDataflow(dataflow);
		synchronized (edits) {
			// Make sure the edits are in the order they were performed
			edit.doEdit();
			edits.addEdit(edit);
		}
		multiCaster.notify(new DataflowEditEvent(dataflow, edit));
	}

	@Override
	public List<Observer<EditManagerEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	@Override
	public void redoDataflowEdit(WorkflowBundle dataflow) throws EditException {
		DataflowEdits edits = getEditsForDataflow(dataflow);
		Edit<?> edit;
		synchronized (edits) {
			if (!edits.canRedo())
				return;
			edit = edits.getLastUndo();
			edit.doEdit();
			edits.addRedo(edit);
		}
		multiCaster.notify(new DataFlowRedoEvent(dataflow, edit));
	}

	@Override
	public void removeObserver(Observer<EditManagerEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	@Override
	public void undoDataflowEdit(WorkflowBundle dataflow) {
		DataflowEdits edits = getEditsForDataflow(dataflow);
		Edit<?> edit;
		synchronized (edits) {
			if (!edits.canUndo())
				return;
			edit = edits.getLastEdit();
			edit.undo();
			edits.addUndo(edit);
		}
		logger.info("Undoing an edit");
		multiCaster.notify(new DataFlowUndoEvent(dataflow, edit));
	}

	/**
	 * Get the set of edits for a given dataflow, creating if neccessary.
	 *
	 * @param dataflow
	 *            Dataflow the edits relate to
	 * @return A {@link DataflowEdits} instance to keep edits for the given
	 *         dataflow
	 */
	protected synchronized DataflowEdits getEditsForDataflow(WorkflowBundle dataflow) {
		DataflowEdits edits = editsForDataflow.get(dataflow);
		if (edits == null) {
			edits = new DataflowEdits();
			editsForDataflow.put(dataflow, edits);
		}
		return edits;
	}

	/**
	 * A set of edits and undoes for a {@link Dataflow}
	 *
	 * @author Stian Soiland-Reyes
	 *
	 */
	public class DataflowEdits {
		/**
		 * List of edits that have been performed and can be undone.
		 */
		private List<Edit<?>> edits = new ArrayList<>();
		/**
		 * List of edits that have been undone and can be redone
		 */
		private List<Edit<?>> undoes = new ArrayList<>();

		/**
		 * Add an {@link Edit} that has been done by the EditManager.
		 * <p>
		 * This can later be retrieved using {@link #getLastEdit()}. After
		 * calling this {@link #canRedo()} will be false.
		 *
		 * @param edit
		 *            {@link Edit} that has been undone
		 */
		public synchronized void addEdit(Edit<?> edit) {
			addEditOrRedo(edit, false);
		}

		/**
		 * Add an {@link Edit} that has been redone by the EditManager.
		 * <p>
		 * The {@link Edit} must be the same as the last undo returned through
		 * {@link #getLastUndo()}.
		 * <p>
		 * This method works like {@link #addEdit(Edit)} except that instead of
		 * removing all possible redoes, only the given {@link Edit} is removed.
		 *
		 * @param edit
		 *            {@link Edit} that has been redone
		 */
		public synchronized void addRedo(Edit<?> edit) {
			addEditOrRedo(edit, true);
		}

		/**
		 * Add an {@link Edit} that has been undone by the EditManager.
		 * <p>
		 * After calling this method {@link #canRedo()} will be true, and the
		 * edit can be retrieved using {@link #getLastUndo()}.
		 * </p>
		 * <p>
		 * The {@link Edit} must be the last edit returned from
		 * {@link #getLastEdit()}, after calling this method
		 * {@link #getLastEdit()} will return the previous edit or
		 * {@link #canUndo()} will be false if there are no more edits.
		 *
		 * @param edit
		 *            {@link Edit} that has been undone
		 */
		public synchronized void addUndo(Edit<?> edit) {
			int lastIndex = edits.size() - 1;
			if (lastIndex < 0 || !edits.get(lastIndex).equals(edit))
				throw new IllegalArgumentException("Can't undo unknown edit "
						+ edit);
			undoes.add(edit);
			edits.remove(lastIndex);
		}

		/**
		 * True if there are undone events that can be redone.
		 *
		 * @return <code>true</code> if there are undone events
		 */
		public boolean canRedo() {
			return !undoes.isEmpty();
		}

		/**
		 * True if there are edits that can be undone and later added with
		 * {@link #addUndo(Edit)}.
		 *
		 * @return <code>true</code> if there are edits that can be undone
		 */
		public boolean canUndo() {
			return !edits.isEmpty();
		}

		/**
		 * Get the last edit that can be undone. This edit was the last one to
		 * be added with {@link #addEdit(Edit)} or {@link #addRedo(Edit)}.
		 *
		 * @return The last added {@link Edit}
		 * @throws IllegalStateException
		 *             If there are no more edits (Check with {@link #canUndo()}
		 *             first)
		 *
		 */
		public synchronized Edit<?> getLastEdit() throws IllegalStateException {
			if (edits.isEmpty())
				throw new IllegalStateException("No more edits");
			int lastEdit = edits.size() - 1;
			return edits.get(lastEdit);
		}

		/**
		 * Get the last edit that can be redone. This edit was the last one to
		 * be added with {@link #addUndo(Edit)}.
		 *
		 * @return The last undone {@link Edit}
		 * @throws IllegalStateException
		 *             If there are no more edits (Check with {@link #canRedo()}
		 *             first)
		 *
		 */
		public synchronized Edit<?> getLastUndo() throws IllegalStateException {
			if (undoes.isEmpty())
				throw new IllegalStateException("No more undoes");
			int lastUndo = undoes.size() - 1;
			return undoes.get(lastUndo);
		}

		/**
		 * Add an edit or redo. Common functionallity called by
		 * {@link #addEdit(Edit)} and {@link #addRedo(Edit)}.
		 *
		 * @see #addEdit(Edit)
		 * @see #addRedo(Edit)
		 * @param edit
		 *            The {@link Edit} to add
		 * @param isRedo
		 *            True if this is a redo
		 */
		protected void addEditOrRedo(Edit<?> edit, boolean isRedo) {
			edits.add(edit);
			if (undoes.isEmpty())
				return;
			if (isRedo) {
				// It's a redo, remove only the last one
				int lastUndoIndex = undoes.size() - 1;
				Edit<?> lastUndo = undoes.get(lastUndoIndex);
				if (!edit.equals(lastUndo))
					throw new IllegalArgumentException(
							"Can only redo last undo");
				undoes.remove(lastUndoIndex);
			} else
				// It's a new edit, remove all redos
				undoes.clear();
		}
	}
}
