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
package org.apache.taverna.workflow.edits;

import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.common.WorkflowBean;

/**
 * An abstract {@link Edit} implementation that checks if an edit has been
 * applied or not.
 *
 * @author Stian Soiland-Reyes
 *
 * @param <Subject>
 *            Subject of this edit
 */
public abstract class AbstractEdit<Subject extends WorkflowBean> implements
		Edit<Subject> {
	private boolean applied = false;
	private final Subject subject;
	protected final Scufl2Tools scufl2Tools = new Scufl2Tools();

	/**
	 * Construct an AbstractEdit.
	 *
	 * @param subjectType
	 *            The expected implementation type of the subject. The edit will
	 *            not go through unless the subject is an instance of this type.
	 *            If the edit don't care about the implementation type, provide
	 *            the official SubjectInterface instead.
	 * @param subject
	 *            The subject of this edit
	 */
	public AbstractEdit(Subject subject) {
		if (subject == null && !isNullSubjectAllowed())
			throw new IllegalArgumentException(
					"Cannot construct an edit with null subject");
		this.subject = subject;
	}

	protected boolean isNullSubjectAllowed() {
		return false;
	}

	@Override
	public final Subject doEdit() throws EditException {
		if (applied)
			throw new EditException("Edit has already been applied!");
		try {
			synchronized (subject) {
				doEditAction(subject);
				applied = true;
				return this.subject;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
	}

	/**
	 * Do the actual edit here
	 *
	 * @param subject
	 *            The instance to which the edit applies
	 * @throws EditException
	 */
	protected abstract void doEditAction(Subject subject)
			throws EditException;

	/**
	 * Undo any edit effects here
	 *
	 * @param subject
	 *            The instance to which the edit applies
	 */
	protected abstract void undoEditAction(Subject subject);

	@Override
	public final Subject getSubject() {
		return subject;
	}

	@Override
	public final boolean isApplied() {
		return applied;
	}

	@Override
	public final void undo() {
		if (!applied)
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		synchronized (subject) {
			undoEditAction(subject);
			applied = false;
		}
	}
}
