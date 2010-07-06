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
package net.sf.taverna.t2.workbench.file.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FileManagerTest {

	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private static final String DUMMY_WORKFLOW_T2FLOW = "dummy-workflow.t2flow";

	private FileManager fileManager;

	private ModelMap modelmap = ModelMap.getInstance();
	private final ModelMapObserver modelMapObserver = new ModelMapObserver();

	@Test
	public void close() throws Exception {
		assertTrue("Non-empty set of open dataflows", fileManager
				.getOpenDataflows().isEmpty());
		Dataflow dataflow = openDataflow();
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(dataflow), fileManager.getOpenDataflows());
		fileManager.closeDataflow(dataflow, true);
		assertNotSame(dataflow, fileManager.getOpenDataflows().get(0));
		assertTrue("Did not insert empty dataflow after close", fileManager
				.getOpenDataflows().get(0).getProcessors().isEmpty());
	}

	@Test
	public void openRemovesEmptyDataflow() throws Exception {
		Dataflow newDataflow = fileManager.newDataflow();
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(newDataflow), fileManager.getOpenDataflows());
		Dataflow dataflow = openDataflow();
		// Should have removed newDataflow
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(dataflow), fileManager.getOpenDataflows());
	}

	@Test
	public void getFileManagerInstance() {
		FileManager instance = FileManager.getInstance();
		assertTrue("FileManager instance not a FileManagerImpl",
				instance instanceof FileManagerImpl);
	}

	@Test
	public void isChanged() throws Exception {
		Dataflow dataflow = openDataflow();
		assertFalse("Dataflow should not have changed", fileManager
				.isDataflowChanged(dataflow));

		// Do a change
		EditManager editManager = EditManager.getInstance();
		Edits edits = editManager.getEdits();
		Processor emptyProcessor = edits.createProcessor("emptyProcessor");
		Edit<Dataflow> addProcessorEdit = edits.getAddProcessorEdit(dataflow,
				emptyProcessor);
		editManager.doDataflowEdit(dataflow, addProcessorEdit);
		assertTrue("Dataflow should have changed", fileManager
				.isDataflowChanged(dataflow));

		// Save it with the change
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();

		fileManager.saveDataflow(dataflow, T2_FLOW_FILE_TYPE, dataflowFile, false);
		assertFalse("Dataflow should no longer be marked as changed",
				fileManager.isDataflowChanged(dataflow));
	}

	@Ignore("Undo support for ischanged not yet implemented")
	@Test
	public void isChangedWithUndo() throws Exception {
		Dataflow dataflow = openDataflow();
		// Do a change
		EditManager editManager = EditManager.getInstance();
		Edits edits = editManager.getEdits();
		Processor emptyProcessor = edits.createProcessor("emptyProcessor");
		Edit<Dataflow> addProcessorEdit = edits.getAddProcessorEdit(dataflow,
				emptyProcessor);
		editManager.doDataflowEdit(dataflow, addProcessorEdit);
		assertTrue("Dataflow should have changed", fileManager
				.isDataflowChanged(dataflow));
		editManager.undoDataflowEdit(dataflow);
		assertFalse(
				"Dataflow should no longer be marked as changed after undo",
				fileManager.isDataflowChanged(dataflow));
		editManager.redoDataflowEdit(dataflow);
		assertTrue("Dataflow should have changed after redo before save",
				fileManager.isDataflowChanged(dataflow));

		// Save it with the change
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		fileManager.saveDataflow(dataflow, T2_FLOW_FILE_TYPE, dataflowFile, false);
		assertFalse("Dataflow should no longer be marked as changed",
				fileManager.isDataflowChanged(dataflow));

		editManager.undoDataflowEdit(dataflow);
		assertTrue("Dataflow should have changed after undo", fileManager
				.isDataflowChanged(dataflow));
		fileManager.saveDataflow(dataflow, T2_FLOW_FILE_TYPE, dataflowFile, false);
		editManager.redoDataflowEdit(dataflow);
		assertTrue("Dataflow should have changed after redo after save",
				fileManager.isDataflowChanged(dataflow));
	}

	@Test
	public void isListed() throws Exception {
		assertTrue("Non-empty set of open data flows", fileManager
				.getOpenDataflows().isEmpty());
		Dataflow dataflow = openDataflow();
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(dataflow), fileManager.getOpenDataflows());
	}

	@Before
	public void listenToModelMap() {
		modelmap.addObserver(modelMapObserver);
	}

	/**
	 * Always uses a <strong>new</strong> file manager instead of the instance
	 * one from {@link FileManager#getInstance()}.
	 * 
	 * @see #getFileManagerInstance()
	 * 
	 */
	@Before
	public void makeFileManager() {
		fileManager = new FileManagerImpl();
	}

	@Test
	public void open() throws Exception {
		assertTrue("ModelMapObserver already contained messages",
				modelMapObserver.messages.isEmpty());
		Dataflow dataflow = openDataflow();
		assertNotNull("Dataflow was not loaded", dataflow);
		assertEquals("Loaded dataflow was not set as current dataflow",
				dataflow, modelmap.getModel(ModelMapConstants.CURRENT_DATAFLOW));
		assertFalse("ModelMapObserver did not contain message",
				modelMapObserver.messages.isEmpty());
		assertEquals("ModelMapObserver contained unexpected messages", 1,
				modelMapObserver.messages.size());
		ModelMapEvent event = modelMapObserver.messages.get(0);
		assertEquals("currentDataflow", event.getModelName());
		assertEquals(dataflow, event.getNewModel());
	}
	
	@Test
	public void openSilently() throws Exception {
		assertTrue("ModelMapObserver already contained messages",
				modelMapObserver.messages.isEmpty());
		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		DataflowInfo info = fileManager.openDataflowSilently(T2_FLOW_FILE_TYPE, url);
		
		Dataflow dataflow = info.getDataflow();
		assertNotNull("Dataflow was not loaded", dataflow);
		
		assertNotSame("Loaded dataflow was set as current dataflow",
				dataflow, modelmap.getModel(ModelMapConstants.CURRENT_DATAFLOW));
		assertTrue("ModelMapObserver contained unexpected messages",
				modelMapObserver.messages.isEmpty());
	}

	@Test
	public void canSaveDataflow() throws Exception {
		Dataflow savedDataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		fileManager.saveDataflow(savedDataflow, T2_FLOW_FILE_TYPE, dataflowFile, true);
		assertTrue(fileManager.canSaveWithoutDestination(savedDataflow));
		fileManager.saveDataflow(savedDataflow, true);
		fileManager.closeDataflow(savedDataflow, true);

		Dataflow otherFlow = fileManager.openDataflow(T2_FLOW_FILE_TYPE, dataflowFile.toURI()
				.toURL());
		assertTrue(fileManager.canSaveWithoutDestination(otherFlow));
	}

	@Test
	public void save() throws Exception {
		Dataflow savedDataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		assertFalse("File should not exist", dataflowFile.isFile());
		fileManager.saveDataflow(savedDataflow, T2_FLOW_FILE_TYPE, dataflowFile, false);
		assertTrue("File should exist", dataflowFile.isFile());
		Dataflow loadedDataflow = fileManager.openDataflow(T2_FLOW_FILE_TYPE, dataflowFile.toURI()
				.toURL());
		assertNotSame("Dataflow was not reopened", savedDataflow,
				loadedDataflow);
		assertEquals("Unexpected number of processors in saved dataflow", 1,
				savedDataflow.getProcessors().size());
		assertEquals("Unexpected number of processors in loaded dataflow", 1,
				loadedDataflow.getProcessors().size());

		Processor savedProcessor = savedDataflow.getProcessors().get(0);
		Processor loadedProcessor = loadedDataflow.getProcessors().get(0);
		assertEquals("Loaded processor had wrong name", savedProcessor
				.getLocalName(), loadedProcessor.getLocalName());

		BeanshellActivity savedActivity = (BeanshellActivity) savedProcessor
				.getActivityList().get(0);
		BeanshellActivity loadedActivity = (BeanshellActivity) loadedProcessor
				.getActivityList().get(0);
		String savedScript = savedActivity.getConfiguration().getScript();
		String loadedScript = loadedActivity.getConfiguration().getScript();
		assertEquals("Unexpected saved script",
				"String output = input + \"XXX\";", savedScript);
		assertEquals("Loaded script did not matched saved script", savedScript,
				loadedScript);
	}

	@Test
	public void saveSilent() throws Exception {
		assertTrue("ModelMapObserver contained unexpected messages",
				modelMapObserver.messages.isEmpty());

		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		DataflowInfo info = fileManager.openDataflowSilently(T2_FLOW_FILE_TYPE, url);
		Dataflow dataflow = info.getDataflow();
		assertTrue("ModelMapObserver contained unexpected messages",
				modelMapObserver.messages.isEmpty());

		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		assertFalse("File should not exist", dataflowFile.isFile());
		
		fileManager.saveDataflowSilently(dataflow, T2_FLOW_FILE_TYPE, dataflowFile, false);
		assertTrue("File should exist", dataflowFile.isFile());
		
		assertTrue("ModelMapObserver contained unexpected messages",
				modelMapObserver.messages.isEmpty());

	}
	
	
	@Test
	public void saveOverwriteAgain() throws Exception {
		Dataflow dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.delete();
		dataflowFile.deleteOnExit();
		// File did NOT exist, should not fail
		fileManager.saveDataflow(dataflow, T2_FLOW_FILE_TYPE, dataflowFile, true);

		EditManager editManager = EditManager.getInstance();
		Edits edits = editManager.getEdits();

		Processor processor = dataflow.getProcessors().get(0);
		Edit<Processor> renameEdit = edits.getRenameProcessorEdit(processor,
				processor.getLocalName() + "-changed");
		editManager.doDataflowEdit(dataflow, renameEdit);

		// Last save was OURs, so should *not* fail - even if we now use
		// the specific saveDataflow() method
		fileManager.saveDataflow(dataflow, T2_FLOW_FILE_TYPE, dataflowFile, true);

		//Thread.sleep(1500);
		Dataflow otherFlow = openDataflow();
		// Saving another flow to same file should still fail
		try {
			fileManager.saveDataflow(otherFlow,T2_FLOW_FILE_TYPE, dataflowFile, true);
			fail("Should have thrown OverwriteException");
		} catch (OverwriteException ex) {
			// Expected
		}
	}

	@Test(expected = OverwriteException.class)
	public void saveOverwriteWarningFails() throws Exception {
		@SuppressWarnings("unused")
		Dataflow dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		// Should fail as file already exists
		fileManager.saveDataflow(dataflow, T2_FLOW_FILE_TYPE, dataflowFile, true);
	}

	@Test
	public void saveOverwriteWarningWorks() throws Exception {
		@SuppressWarnings("unused")
		Dataflow dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.delete();
		dataflowFile.deleteOnExit();
		// File did NOT exist, should not fail
		fileManager.saveDataflow(dataflow, T2_FLOW_FILE_TYPE, dataflowFile, true);
	}

	@After
	public void stopListeningToModelMap() {
		modelmap.removeObserver(modelMapObserver);
	}

	protected Dataflow openDataflow() throws OpenException {
		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		return fileManager.openDataflow(T2_FLOW_FILE_TYPE, url);
	}

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		protected List<ModelMapEvent> messages = new ArrayList<ModelMapEvent>();

		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			messages.add(message);
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_DATAFLOW)) {
				assertTrue("Dataflow was not listed as open when set current",
						fileManager.getOpenDataflows().contains(
								message.getNewModel()));
			}
		}
	}

}
