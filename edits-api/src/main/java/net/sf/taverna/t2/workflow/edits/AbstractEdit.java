/*******************************************************************************
 * Copyright (C) 2007-2008 The University of Manchester
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
package net.sf.taverna.t2.workflow.edits;

import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.common.WorkflowBean;


/**
 * An abstract {@link Edit} implementation that checks if an edit has been
 * applied or not.
 *
 * @author Stian Soiland-Reyes
 *
 * @param <Subject>
 *            Subject of this edit
 */
public abstract class AbstractEdit<Subject extends WorkflowBean> implements Edit<Subject> {

	private boolean applied = false;
	protected Subject subject;
	protected Scufl2Tools scufl2Tools = new Scufl2Tools();

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
		if (subject == null && !isNullSubjectAllowed()) {
			throw new IllegalArgumentException(
					"Cannot construct an edit with null subject");
		}
		this.subject = subject;
	}

	protected boolean isNullSubjectAllowed() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Subject doEdit() throws EditException {
		if (applied) {
			throw new EditException("Edit has already been applied!");
		}
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

	/**
	 * {@inheritDoc}
	 */
	public final Subject getSubject() {
		return subject;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isApplied() {
		return applied;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		synchronized (subject) {
			undoEditAction(subject);
			applied = false;
		}
	}

}