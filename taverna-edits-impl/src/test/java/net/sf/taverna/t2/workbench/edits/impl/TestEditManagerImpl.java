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
package net.sf.taverna.t2.workbench.edits.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowRedoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workflow.edits.AddProcessorEdit;

import org.junit.Before;
import org.junit.Test;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;

public class TestEditManagerImpl {

	private Workflow dataflow;

	private EditManagerObserver editManagerObserver = new EditManagerObserver();

	private Processor processor;

	@Test
	public void addProcessor() throws Exception {
		EditManager editManager = new EditManagerImpl();
		editManager.addObserver(editManagerObserver);

		Edit<Workflow> edit = new AddProcessorEdit(dataflow, processor);
		assertFalse("Edit was already applied", edit.isApplied());
		assertTrue("Did already add processor", dataflow.getProcessors()
				.isEmpty());

		editManager.doDataflowEdit(dataflow.getParent(), edit);
		assertTrue("Edit was not applied", edit.isApplied());
		assertEquals("Did not add processor", processor, dataflow.getProcessors().first());

		// Should have received the edit event
		assertEquals("Incorrect number of events", 1,
				editManagerObserver.events.size());
		EditManagerEvent event = editManagerObserver.events.get(0);
		assertTrue("Event was not a DataflowEditEvent",
				event instanceof DataflowEditEvent);
		DataflowEditEvent dataEditEvent = (DataflowEditEvent) event;
		assertEquals("Event did not have correct workflow", dataflow,
				dataEditEvent.getDataFlow().getWorkflows().first());
		assertEquals("Event did not have correct edit", edit, dataEditEvent
				.getEdit());

	}

	@Test
	public void undoAddProcessor() throws Exception {
		EditManager editManager = new EditManagerImpl();
		editManager.addObserver(editManagerObserver);

		Edit<Workflow> edit = new AddProcessorEdit(dataflow, processor);
		editManager.doDataflowEdit(dataflow.getParent(), edit);

		assertFalse("Did not add processor", dataflow.getProcessors().isEmpty());
		editManager.undoDataflowEdit(dataflow.getParent());
		assertTrue("Did not undo add processor", dataflow.getProcessors()
				.isEmpty());

		// Should have received the undo event
		assertEquals("Incorrect number of events", 2,
				editManagerObserver.events.size());
		EditManagerEvent event = editManagerObserver.events.get(1);
		assertTrue("Event was not a DataflowEditEvent",
				event instanceof DataFlowUndoEvent);
		DataFlowUndoEvent dataEditEvent = (DataFlowUndoEvent) event;
		assertEquals("Event did not have correct workflow", dataflow,
				dataEditEvent.getDataFlow().getWorkflows().first());
		assertEquals("Event did not have correct edit", edit, dataEditEvent
				.getEdit());
		assertFalse("Edit was still applied", edit.isApplied());
	}

	@Test
	public void multipleUndoesRedoes() throws Exception {
		EditManager editManager = new EditManagerImpl();
		editManager.addObserver(editManagerObserver);

		Workflow dataflowA = createDataflow();
		Workflow dataflowB = createDataflow();
		Workflow dataflowC = createDataflow();

		Processor processorA1 = createProcessor();
		Processor processorA2 = createProcessor();
		Processor processorA3 = createProcessor();
		Processor processorB1 = createProcessor();
		Processor processorC1 = createProcessor();

		Edit<Workflow> edit = new AddProcessorEdit(dataflowA, processorA1);
		editManager.doDataflowEdit(dataflowA.getParent(), edit);

		edit = new AddProcessorEdit(dataflowB, processorB1);
		editManager.doDataflowEdit(dataflowB.getParent(), edit);

		edit = new AddProcessorEdit(dataflowA, processorA2);
		editManager.doDataflowEdit(dataflowA.getParent(), edit);

		edit = new AddProcessorEdit(dataflowC, processorC1);
		editManager.doDataflowEdit(dataflowC.getParent(), edit);

		edit = new AddProcessorEdit(dataflowA, processorA3);
		editManager.doDataflowEdit(dataflowA.getParent(), edit);



		assertFalse("Did not add processors", dataflowA.getProcessors().isEmpty());
		assertEquals(3, dataflowA.getProcessors().size());
		editManager.undoDataflowEdit(dataflowA.getParent());
		assertEquals(2, dataflowA.getProcessors().size());
		editManager.undoDataflowEdit(dataflowA.getParent());
		assertEquals(1, dataflowA.getProcessors().size());
		editManager.undoDataflowEdit(dataflowA.getParent());
		assertEquals(0, dataflowA.getProcessors().size());

		assertEquals(1, dataflowB.getProcessors().size());
		assertEquals(1, dataflowC.getProcessors().size());

		assertTrue(editManager.canUndoDataflowEdit(dataflowC.getParent()));
		editManager.undoDataflowEdit(dataflowC.getParent());
		assertFalse(editManager.canUndoDataflowEdit(dataflowC.getParent()));
		editManager.undoDataflowEdit(dataflowC.getParent()); // extra one
		assertFalse(editManager.canUndoDataflowEdit(dataflowC.getParent()));


		assertEquals(1, dataflowB.getProcessors().size());
		assertEquals(0, dataflowC.getProcessors().size());

		editManager.undoDataflowEdit(dataflowB.getParent());
		assertEquals(0, dataflowA.getProcessors().size());
		assertEquals(0, dataflowB.getProcessors().size());
		assertEquals(0, dataflowC.getProcessors().size());

		editManager.redoDataflowEdit(dataflowA.getParent());
		assertEquals(1, dataflowA.getProcessors().size());

		editManager.redoDataflowEdit(dataflowA.getParent());
		assertEquals(2, dataflowA.getProcessors().size());

		editManager.redoDataflowEdit(dataflowA.getParent());
		assertEquals(3, dataflowA.getProcessors().size());

		// does not affect it
		editManager.redoDataflowEdit(dataflowA.getParent());
		assertEquals(3, dataflowA.getProcessors().size());
		assertEquals(0, dataflowB.getProcessors().size());
		assertEquals(0, dataflowC.getProcessors().size());
	}

	@Test
	public void emptyUndoDoesNotFail() throws Exception {
		EditManager editManager = new EditManagerImpl();
		editManager.addObserver(editManagerObserver);
		editManager.undoDataflowEdit(dataflow.getParent());
	}

	@Test
	public void extraUndoesDoesNotFail() throws Exception {
		EditManager editManager = new EditManagerImpl();
		editManager.addObserver(editManagerObserver);

		Edit<Workflow> edit = new AddProcessorEdit(dataflow, processor);
		editManager.doDataflowEdit(dataflow.getParent(), edit);

		assertFalse("Did not add processor", dataflow.getProcessors().isEmpty());
		editManager.undoDataflowEdit(dataflow.getParent());
		assertTrue("Did not undo add processor", dataflow.getProcessors()
				.isEmpty());
		editManager.undoDataflowEdit(dataflow.getParent());
	}

	@Before
	public void makeDataflow() {
		dataflow = createDataflow();
	}

	protected Workflow createDataflow() {
		WorkflowBundle workflowBundle = new WorkflowBundle();
		Workflow workflow = new Workflow();
		workflow.setParent(workflowBundle);
		return workflow;
	}

	protected Processor createProcessor() {
		Processor processor = new Processor();
		processor.setName("proc-" + UUID.randomUUID());
		return processor;
	}

	@Before
	public void makeProcessor() {
		processor = createProcessor();
	}

	private class EditManagerObserver implements Observer<EditManagerEvent> {

		public List<EditManagerEvent> events = new ArrayList<>();

		@Override
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			events.add(message);
			if (message instanceof DataflowEditEvent) {
				DataflowEditEvent dataflowEdit = (DataflowEditEvent) message;
				assertTrue("Edit was not applied on edit event", dataflowEdit
						.getEdit().isApplied());
			} else if (message instanceof DataFlowUndoEvent) {
				DataFlowUndoEvent dataflowUndo = (DataFlowUndoEvent) message;
				assertFalse("Edit was applied on undo event", dataflowUndo
						.getEdit().isApplied());
			} else if (message instanceof DataFlowRedoEvent) {
				DataFlowRedoEvent dataflowEdit = (DataFlowRedoEvent) message;
				assertTrue("Edit was not applied on edit event", dataflowEdit
						.getEdit().isApplied());
			} else {
				fail("Unknown event: " + message);
			}
		}
	}

}
