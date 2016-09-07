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

package org.apache.taverna.workbench.edits;

import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.scufl2.api.common.WorkflowBean;

/**
 * Implementation of Edit which contains an ordered list of child edits. Child
 * edits are applied collectively and in order, any failure in any child edit
 * causes an undo of previously applied children and a propogation of the edit
 * exception.
 * 
 * @author Tom Oinn
 */
public class CompoundEdit implements Edit<WorkflowBean> {
	private final transient List<Edit<?>> childEdits;
	private transient boolean applied = false;

	/**
	 * Create a new compound edit with no existing Edit objects.
	 * 
	 */
	public CompoundEdit() {
		this.childEdits = new ArrayList<>();
	}

	/**
	 * Create a new compound edit with the specified edits as children.
	 */
	public CompoundEdit(List<Edit<?>> edits) {
		this.childEdits = edits;
	}

	/**
	 * Get the list of edits.
	 * @return a live-editable list.
	 */
	public List<Edit<?>> getChildEdits() {
		return childEdits;
	}

	/**
	 * Attempts to call the doEdit method of all child edits. If any of those
	 * children throws an EditException any successful edits are rolled back and
	 * the exception is rethrown as the cause of a new EditException from the
	 * CompoundEdit
	 */
	@Override
	public synchronized WorkflowBean doEdit() throws EditException {
		if (isApplied())
			throw new EditException("Cannot apply an edit more than once!");
		List<Edit<?>> doneEdits = new ArrayList<>();
		try {
			for (Edit<?> edit : childEdits) {
				edit.doEdit();
				/*
				 * Insert the done edit at position 0 in the list so we can
				 * iterate over the list in the normal order if we need to
				 * rollback, this ensures that the most recent edit is first.
				 */
				doneEdits.add(0, edit);
			}
			applied = true;
		} catch (EditException ee) {
			for (Edit<?> undoMe : doneEdits)
				undoMe.undo();
			applied = false;
			throw new EditException("Failed child of compound edit", ee);
		}
		return null;
	}

	/**
	 * There is no explicit subject for a compound edit, so this method always
	 * returns null.
	 */
	@Override
	public Object getSubject() {
		return null;
	}

	/**
	 * Rolls back all child edits in reverse order
	 */
	@Override
	public synchronized void undo() {
		for (int i = childEdits.size() - 1; i >= 0; i--)
			// Undo child edits in reverse order
			childEdits.get(i).undo();
		applied = false;
	}

	@Override
	public boolean isApplied() {
		return applied;
	}
}
