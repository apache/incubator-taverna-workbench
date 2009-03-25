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
package net.sf.taverna.t2.activities.dataflow.filemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link NestedDataflowPersistenceHandler}
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestNestedDataflowPersistenceHandler {

	private static final String DUMMY = "dummy";

	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private Dataflow dataflow;

	private EditManager editManager = EditManager.getInstance();

	private Edits edits = editManager.getEdits();

	private FileManager fileManager = FileManager.getInstance();

	private DataflowActivity nestedDataflowActivity;

	private Processor nestedProc;

	private Dataflow originalNested;

	@Before
	public void makeNestedDataflow() throws EditException {
		dataflow = fileManager.newDataflow();
		nestedDataflowActivity = new DataflowActivity();

		nestedProc = edits.createProcessor("nested");

		List<Edit<?>> addEdits = new ArrayList<Edit<?>>();
		addEdits.add(edits.getAddProcessorEdit(dataflow, nestedProc));				
		addEdits.add(edits.getAddActivityEdit(nestedProc,
				nestedDataflowActivity));
		addEdits.add(edits.getMapProcessorPortsForActivityEdit(nestedProc));
		originalNested = edits.createDataflow();
		addEdits.add(edits.getConfigureActivityEdit(nestedDataflowActivity,
				originalNested));

		assertFalse("Owner dataflow should not have changed before doing edit",
				fileManager.isDataflowChanged(dataflow));
		editManager.doDataflowEdit(dataflow, new CompoundEdit(addEdits));
		assertTrue("Owner dataflow should have changed after doing edit",
				fileManager.isDataflowChanged(dataflow));
		fileManager.setDataflowChanged(dataflow, false);
		assertFalse("Owner dataflow should no longer been set as changed",
				fileManager.isDataflowChanged(dataflow));
	}

	@Test
	public void openIsolation() throws Exception {
		Dataflow openedNested = openNested();
		assertNotNull("Opened workflow was null", openedNested);
		assertNotSame("Opened workflow was the same as original dataflow",
				originalNested, openedNested);
		assertSame(fileManager.getCurrentDataflow(), openedNested);
	}

	@Test
	public void saveIsolation() throws Exception {
		Dataflow openedNested = openNested();
		addDummyProcessor(openedNested);
		assertTrue("Original dataflow was changed", originalNested
				.getProcessors().isEmpty());
		fileManager.saveDataflow(openedNested, true);
		assertNotSame("Saved workflow was the same as opened dataflow",
				originalNested, openedNested);
		Dataflow nowNested = nestedDataflowActivity.getConfiguration();
		assertNotSame("Did not change activity configuration", nowNested,
				originalNested);
		assertNotSame("Did not copy activity configuration", openedNested);
		assertFalse("Did not add dummy processor", nowNested.getProcessors()
				.isEmpty());
		assertEquals(DUMMY, nowNested.getProcessors().get(0).getLocalName());
	}

	@Test
	public void saveUpdatesParent() throws Exception {
		Dataflow openedNested = openNested();
		addDummyProcessor(openedNested);
		assertTrue("Original dataflow was changed", originalNested
				.getProcessors().isEmpty());
		assertFalse("Owner dataflow should not have changed "
				+ "before saving nested workflow", fileManager
				.isDataflowChanged(dataflow));
		fileManager.saveDataflow(openedNested, true);
		assertTrue("Owner dataflow should have changed "
				+ "after saving nested workflow", fileManager
				.isDataflowChanged(dataflow));
	}

	private Processor addDummyProcessor(Dataflow openedNested)
			throws EditException {
		Processor dummyProc = edits.createProcessor(DUMMY);
		Edit<Dataflow> addEdit = edits.getAddProcessorEdit(openedNested,
				dummyProc);
		editManager.doDataflowEdit(openedNested, addEdit);
		return dummyProc;
	}

	private Dataflow openNested() throws OpenException {
		NestedDataflowSource nestedDataflowSource = new NestedDataflowSource(
				dataflow, nestedDataflowActivity);
		Dataflow openedNested = fileManager.openDataflow(T2_FLOW_FILE_TYPE,
				nestedDataflowSource);
		return openedNested;
	}

}
