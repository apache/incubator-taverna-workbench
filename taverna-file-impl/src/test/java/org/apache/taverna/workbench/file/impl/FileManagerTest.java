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

package org.apache.taverna.workbench.file.impl;

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

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.impl.EditManagerImpl;
import org.apache.taverna.workbench.file.DataflowInfo;
import org.apache.taverna.workbench.file.DataflowPersistenceHandler;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.SetCurrentDataflowEvent;
import org.apache.taverna.workbench.file.exceptions.OpenException;
import org.apache.taverna.workbench.file.exceptions.OverwriteException;
import org.apache.taverna.workflow.edits.AddProcessorEdit;
import org.apache.taverna.workflow.edits.RenameEdit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.io.WorkflowBundleIO;
import org.apache.taverna.scufl2.api.io.WorkflowBundleReader;
import org.apache.taverna.scufl2.api.io.WorkflowBundleWriter;
import org.apache.taverna.scufl2.rdfxml.RDFXMLReader;
import org.apache.taverna.scufl2.rdfxml.RDFXMLWriter;
import org.apache.taverna.scufl2.translator.t2flow.T2FlowReader;

public class FileManagerTest {

	private static final WorkflowBundleFileType WF_BUNDLE_FILE_TYPE = new WorkflowBundleFileType();
	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private static final String DUMMY_WORKFLOW_T2FLOW = "dummy-workflow.t2flow";

	private FileManagerImpl fileManager;
	private EditManager editManager;

	private FileManagerObserver fileManagerObserver= new FileManagerObserver();;

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
		fileManagerObserver = new FileManagerObserver();
		fileManager.addObserver(fileManagerObserver);
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
				fileManagerObserver.messages.isEmpty());
		WorkflowBundle dataflow = openDataflow();
		assertNotNull("Dataflow was not loaded", dataflow);
		assertEquals("Loaded dataflow was not set as current dataflow",
				dataflow, fileManager.getCurrentDataflow());
		assertFalse("ModelMapObserver did not contain message",
				fileManagerObserver.messages.isEmpty());
		assertEquals("ModelMapObserver contained unexpected messages", 2,
				fileManagerObserver.messages.size());
		FileManagerEvent event = fileManagerObserver.messages.get(0);
		assertTrue(event instanceof SetCurrentDataflowEvent);
		assertEquals(dataflow, ((SetCurrentDataflowEvent) event).getDataflow());
	}

	@Test
	public void openSilently() throws Exception {
		assertTrue("ModelMapObserver already contained messages",
				fileManagerObserver.messages.isEmpty());
		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		DataflowInfo info = fileManager.openDataflowSilently(T2_FLOW_FILE_TYPE, url);

		WorkflowBundle dataflow = info.getDataflow();
		assertNotNull("Dataflow was not loaded", dataflow);

		assertNotSame("Loaded dataflow was set as current dataflow",
				dataflow, fileManager.getCurrentDataflow());
		assertTrue("ModelMapObserver contained unexpected messages",
				fileManagerObserver.messages.isEmpty());
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
				fileManagerObserver.messages.isEmpty());

		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		DataflowInfo info = fileManager.openDataflowSilently(T2_FLOW_FILE_TYPE, url);
		WorkflowBundle dataflow = info.getDataflow();
		assertTrue("ModelMapObserver contained unexpected messages",
				fileManagerObserver.messages.isEmpty());

		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		dataflowFile.delete();
		assertFalse("File should not exist", dataflowFile.isFile());

		fileManager.saveDataflowSilently(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, false);
		assertTrue("File should exist", dataflowFile.isFile());

		assertTrue("ModelMapObserver contained unexpected messages",
				fileManagerObserver.messages.isEmpty());

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
		Edit<Processor> renameEdit = new RenameEdit<Processor>(processor,
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
		WorkflowBundle dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.deleteOnExit();
		// Should fail as file already exists
		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, true);
	}

	@Test
	public void saveOverwriteWarningWorks() throws Exception {
		WorkflowBundle dataflow = openDataflow();
		File dataflowFile = File.createTempFile("test", ".t2flow");
		dataflowFile.delete();
		dataflowFile.deleteOnExit();
		// File did NOT exist, should not fail
		fileManager.saveDataflow(dataflow, WF_BUNDLE_FILE_TYPE, dataflowFile, true);
	}

	@After
	public void stopListeningToModelMap() {
		fileManager.removeObserver(fileManagerObserver);
	}

	protected WorkflowBundle openDataflow() throws OpenException {
		URL url = getClass().getResource(DUMMY_WORKFLOW_T2FLOW);
		assertNotNull(url);
		WorkflowBundle dataflow = fileManager.openDataflow(T2_FLOW_FILE_TYPE, url);
		assertNotNull(dataflow);
		return dataflow;
	}

	private final class FileManagerObserver implements Observer<FileManagerEvent> {
		protected List<FileManagerEvent> messages = new ArrayList<FileManagerEvent>();

		@Override
		public void notify(Observable<FileManagerEvent> sender, FileManagerEvent message) throws Exception {
			messages.add(message);
			if (message instanceof SetCurrentDataflowEvent) {
				assertTrue("Dataflow was not listed as open when set current",
						fileManager.getOpenDataflows().contains(
								((SetCurrentDataflowEvent) message).getDataflow()));
			}
		}
	}

}
