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

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.impl.EditManagerImpl;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workflow.edits.AddProcessorEdit;
import net.sf.taverna.t2.workflow.edits.RenameProcessorEdit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
import uk.org.taverna.scufl2.api.io.WorkflowBundleReader;
import uk.org.taverna.scufl2.api.io.WorkflowBundleWriter;
import uk.org.taverna.scufl2.rdfxml.RDFXMLReader;
import uk.org.taverna.scufl2.rdfxml.RDFXMLWriter;
import uk.org.taverna.scufl2.translator.t2flow.T2FlowReader;

public class FileManagerTest {

	private static final WorkflowBundleFileType WF_BUNDLE_FILE_TYPE = new WorkflowBundleFileType();
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private static final String DUMMY_WORKFLOW_T2FLOW = "dummy-workflow.t2flow";

	private FileManagerImpl fileManager;
	private EditManager editManager;

	private ModelMap modelmap = ModelMap.getInstance();
	private final ModelMapObserver modelMapObserver = new ModelMapObserver();

	@Test
	public void close() throws Exception {
		assertTrue("Non-empty set of open dataflows", fileManager
				.getOpenDataflows().isEmpty());
		WorkflowBundle dataflow = openDataflow();
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(dataflow), fileManager.getOpenDataflows());
		fileManager.closeDataflow(dataflow, true);
		assertNotSame(dataflow, fileManager.getOpenDataflows().get(0));
		assertTrue("Did not insert empty dataflow after close", fileManager
				.getOpenDataflows().get(0).getMainWorkflow().getProcessors().isEmpty());
	}

	@Test
	public void openRemovesEmptyDataflow() throws Exception {
		WorkflowBundle newDataflow = fileManager.newDataflow();
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(newDataflow), fileManager.getOpenDataflows());
		WorkflowBundle dataflow = openDataflow();
		// Should have removed newDataflow
		assertEquals("Unexpected list of open dataflows", Arrays
				.asList(dataflow), fileManager.getOpenDataflows());
	}

	@Test
	public void isChanged() throws Exception {
		WorkflowBundle dataflow = openDataflow();
		assertFalse("Dataflow should not have changed", fileManager
				.isDataflowChanged(dataflow));

		// Do a change
		Processor emptyProcessor = new Processor();
		Edit<Workflow> addProcessorEdit = new AddProcessorEdit(dataflow.getMainWorkflow(),
				emptyProcessor);
		editManager.doDataflowEdit(dataflow, addProcessorEdit);
		assertTrue("Dataflow should have changed", fileManager
				.isDataflowChanged(dataflow));

		// Save it with the change
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();

		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, false);
		assertFalse("Dataflow should no longer be marked as changed",
				fileManager.isDataflowChanged(dataflow));
	}

	@Ignore("Undo support for ischanged not yet implemented")
	@Test
	public void isChangedWithUndo() throws Exception {
		WorkflowBundle dataflow = openDataflow();
		// Do a change
		Processor emptyProcessor = new Processor();
		Edit<Workflow> addProcessorEdit = new AddProcessorEdit(dataflow.getMainWorkflow(),
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
		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, false);
		assertFalse("Dataflow should no longer be marked as changed",
				fileManager.isDataflowChanged(dataflow));

		editManager.undoDataflowEdit(dataflow);
		assertTrue("Dataflow should have changed after undo", fileManager
				.isDataflowChanged(dataflow));
		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, false);
		editManager.redoDataflowEdit(dataflow);
		assertTrue("Dataflow should have changed after redo after save",
				fileManager.isDataflowChanged(dataflow));
	}

	@Test
	public void isListed() throws Exception {
		assertTrue("Non-empty set of open data flows", fileManager
				.getOpenDataflows().isEmpty());
		WorkflowBundle dataflow = openDataflow();
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
		System.setProperty("java.awt.headless", "true");
		editManager = new EditManagerImpl();
		fileManager = new FileManagerImpl(editManager);
		WorkflowBundleIO workflowBundleIO = new WorkflowBundleIO();
		workflowBundleIO.setReaders(Arrays.<WorkflowBundleReader>asList(new RDFXMLReader(), new T2FlowReader()));
		workflowBundleIO.setWriters(Arrays.<WorkflowBundleWriter>asList(new RDFXMLWriter()));
		T2DataflowOpener t2DataflowOpener = new T2DataflowOpener();
		t2DataflowOpener.setWorkflowBundleIO(workflowBundleIO);
		WorkflowBundleOpener workflowBundleOpener = new WorkflowBundleOpener();
		workflowBundleOpener.setWorkflowBundleIO(workflowBundleIO);
		WorkflowBundleSaver workflowBundleSaver = new WorkflowBundleSaver();
		workflowBundleSaver.setWorkflowBundleIO(workflowBundleIO);
		DataflowPersistenceHandlerRegistry dataflowPersistenceHandlerRegistry = new DataflowPersistenceHandlerRegistry();
		dataflowPersistenceHandlerRegistry.setDataflowPersistenceHandlers(Arrays.asList(
				new DataflowPersistenceHandler[] {t2DataflowOpener, workflowBundleOpener, workflowBundleSaver}));
		dataflowPersistenceHandlerRegistry.updateColletions();
		fileManager.setDataflowPersistenceHandlerRegistry(dataflowPersistenceHandlerRegistry);
	}

	@Test
	public void open() throws Exception {
		assertTrue("ModelMapObserver already contained messages",
				modelMapObserver.messages.isEmpty());
		WorkflowBundle dataflow = openDataflow();
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

		WorkflowBundle dataflow = info.getDataflow();
		assertNotNull("Dataflow was not loaded", dataflow);

		assertNotSame("Loaded dataflow was set as current dataflow",
				dataflow, modelmap.getModel(ModelMapConstants.CURRENT_DATAFLOW));
		assertTrue("ModelMapObserver contained unexpected messages",
				modelMapObserver.messages.isEmpty());
	}

	@Test
	public void canSaveDataflow() throws Exception {
		WorkflowBundle savedDataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		fileManager.saveDataflow(savedDataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, true);
		assertTrue(fileManager.canSaveWithoutDestination(savedDataflow));
		fileManager.saveDataflow(savedDataflow, true);
		fileManager.closeDataflow(savedDataflow, true);

		WorkflowBundle otherFlow = fileManager.openDataflow(WF_BUNDLE_FILE_TYPE, dataflowFile.toURI()
				.toURL());
		assertTrue(fileManager.canSaveWithoutDestination(otherFlow));
	}

	@Test
	public void save() throws Exception {
		WorkflowBundle savedDataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		assertFalse("File should not exist", dataflowFile.isFile());
		fileManager.saveDataflow(savedDataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, false);
		assertTrue("File should exist", dataflowFile.isFile());
		WorkflowBundle loadedDataflow = fileManager.openDataflow(WF_BUNDLE_FILE_TYPE, dataflowFile.toURI()
				.toURL());
		assertNotSame("Dataflow was not reopened", savedDataflow,
				loadedDataflow);
		assertEquals("Unexpected number of processors in saved dataflow", 1,
				savedDataflow.getMainWorkflow().getProcessors().size());
		assertEquals("Unexpected number of processors in loaded dataflow", 1,
				loadedDataflow.getMainWorkflow().getProcessors().size());

		Processor savedProcessor = savedDataflow.getMainWorkflow().getProcessors().first();
		Processor loadedProcessor = loadedDataflow.getMainWorkflow().getProcessors().first();
		assertEquals("Loaded processor had wrong name", savedProcessor
				.getName(), loadedProcessor.getName());

		// TODO convert to scufl2
//		BeanshellActivity savedActivity = (BeanshellActivity) savedProcessor
//				.getActivityList().get(0);
//		BeanshellActivity loadedActivity = (BeanshellActivity) loadedProcessor
//				.getActivityList().get(0);
//		String savedScript = savedActivity.getConfiguration().getScript();
//		String loadedScript = loadedActivity.getConfiguration().getScript();
//		assertEquals("Unexpected saved script",
//				"String output = input + \"XXX\";", savedScript);
//		assertEquals("Loaded script did not matched saved script", savedScript,
//				loadedScript);
	}

	@Test
	public void saveSilent() throws Exception {
		assertTrue("ModelMapObserver contained unexpected messages",
				modelMapObserver.messages.isEmpty());

		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		DataflowInfo info = fileManager.openDataflowSilently(T2_FLOW_FILE_TYPE, url);
		WorkflowBundle dataflow = info.getDataflow();
		assertTrue("ModelMapObserver contained unexpected messages",
				modelMapObserver.messages.isEmpty());

		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		assertFalse("File should not exist", dataflowFile.isFile());

		fileManager.saveDataflowSilently(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, false);
		assertTrue("File should exist", dataflowFile.isFile());

		assertTrue("ModelMapObserver contained unexpected messages",
				modelMapObserver.messages.isEmpty());

	}

	@Test
	public void saveOverwriteAgain() throws Exception {
		WorkflowBundle dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.delete();
		dataflowFile.deleteOnExit();
		// File did NOT exist, should not fail
		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, true);

		Processor processor = dataflow.getMainWorkflow().getProcessors().first();
		Edit<Processor> renameEdit = new RenameProcessorEdit(processor,
				processor.getName() + "-changed");
		editManager.doDataflowEdit(dataflow, renameEdit);

		// Last save was OURs, so should *not* fail - even if we now use
		// the specific saveDataflow() method
		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, true);

		//Thread.sleep(1500);
		WorkflowBundle otherFlow = openDataflow();
		// Saving another flow to same file should still fail
		try {
			fileManager.saveDataflow(otherFlow,WF_BUNDLE_FILE_TYPE, dataflowFile, true);
			fail("Should have thrown OverwriteException");
		} catch (OverwriteException ex) {
			// Expected
		}
	}

	@Test(expected = OverwriteException.class)
	public void saveOverwriteWarningFails() throws Exception {
		@SuppressWarnings("unused")
		WorkflowBundle dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		// Should fail as file already exists
		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, true);
	}

	@Test
	public void saveOverwriteWarningWorks() throws Exception {
		@SuppressWarnings("unused")
		WorkflowBundle dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.delete();
		dataflowFile.deleteOnExit();
		// File did NOT exist, should not fail
		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, true);
	}

	@After
	public void stopListeningToModelMap() {
		modelmap.removeObserver(modelMapObserver);
	}

	protected WorkflowBundle openDataflow() throws OpenException {
		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		assertNotNull(url);
		WorkflowBundle dataflow = fileManager.openDataflow(T2_FLOW_FILE_TYPE, url);
		assertNotNull(dataflow);
		return dataflow;
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
