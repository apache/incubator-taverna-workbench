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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

import org.jdom.Element;

/**
 * Allow opening/saving of a nested workflow sourced from a
 * {@link DataflowActivity} - described by a {@link NestedDataflowSource}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class NestedDataflowPersistenceHandler extends
		AbstractDataflowPersistenceHandler implements
		DataflowPersistenceHandler {

	private static final T2FlowFileType T2_FLOW_FILE_TYPE = new T2FlowFileType();

	private EditManager editManager = EditManager.getInstance();

	private Edits edits = editManager.getEdits();

	@Override
	public List<FileType> getOpenFileTypes() {
		return Arrays.<FileType> asList(T2_FLOW_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getOpenSourceTypes() {
		return Arrays.<Class<?>> asList(NestedDataflowSource.class);
	}

	@Override
	public List<Class<?>> getSaveDestinationTypes() {
		return Arrays.<Class<?>> asList(NestedDataflowSource.class);
	}

	@Override
	public List<FileType> getSaveFileTypes() {
		return Arrays.<FileType> asList(T2_FLOW_FILE_TYPE);

	}

	@Override
	public DataflowInfo openDataflow(FileType fileType, Object source)
			throws OpenException {
		if (!(T2_FLOW_FILE_TYPE.equals(fileType))) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		if (!(source instanceof NestedDataflowSource)) {
			throw new IllegalArgumentException("Unsupported source " + source);
		}

		NestedDataflowSource nestedDataflowSource = (NestedDataflowSource) source;
		DataflowActivity dataflowActivity = nestedDataflowSource
				.getDataflowActivity();
		Dataflow dataflow = dataflowActivity.getConfiguration();
		if (dataflow == null) {
			throw new OpenException("Dataflow was null");
		}
		try {
			return new DataflowInfo(fileType, source, copyDataflow(dataflow));
		} catch (SerializationException e) {
			throw new OpenException("Could not serialize dataflow " + dataflow,
					e);
		} catch (DeserializationException e) {
			throw new OpenException("Could not deserialize dataflow "
					+ dataflow, e);
		} catch (EditException e) {
			throw new OpenException("Could not recreate dataflow " + dataflow,
					e);
		}
	}

	@Override
	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination) throws SaveException {
		if (dataflow == null) {
			throw new NullPointerException("dataflow can't be null");
		}
		if (!(T2_FLOW_FILE_TYPE.equals(fileType))) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		if (!(destination instanceof NestedDataflowSource)) {
			throw new IllegalArgumentException("Unsupported source "
					+ destination);
		}
		NestedDataflowSource nestedDataflowDestination = (NestedDataflowSource) destination;
		DataflowActivity dataflowActivity = nestedDataflowDestination
				.getDataflowActivity();

		Dataflow dataflowCopy;
		try {
			dataflowCopy = copyDataflow(dataflow);
		} catch (SerializationException e) {
			throw new SaveException("Could not serialize dataflow " + dataflow,
					e);
		} catch (DeserializationException e) {
			throw new SaveException("Could not deserialize dataflow "
					+ dataflow, e);
		} catch (EditException e) {
			throw new SaveException("Could not recreate dataflow " + dataflow,
					e);
		}
		Dataflow parentDataflow = nestedDataflowDestination.getParentDataflow();
		List<Processor> dataflowProcessors = findProcessors(parentDataflow, dataflowActivity);
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		editList.add(edits.getConfigureActivityEdit(dataflowActivity, dataflowCopy));
		for (Processor dataflowProcessor : dataflowProcessors) {
			editList.add(edits.getMapProcessorPortsForActivityEdit(dataflowProcessor));
		}
		try {
			editManager.doDataflowEdit(parentDataflow, new CompoundEdit(editList));
		} catch (EditException e) {
			throw new SaveException("Could not configure dataflow activity "
					+ dataflowActivity, e);
		}
		return new DataflowInfo(fileType, destination, dataflow);
	}

	@Override
	public boolean wouldOverwriteDataflow(Dataflow dataflow, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo) {
		// TODO: Check if the one in the activity was different or
		// have been changed
		return false;
	}

	protected Dataflow copyDataflow(Dataflow dataflow)
			throws SerializationException, DeserializationException,
			EditException {
		XMLSerializer xmlSerializer = new XMLSerializerImpl();
		Element dataflowElement = xmlSerializer.serializeDataflow(dataflow);
		XMLDeserializer xmlDeserializer = new XMLDeserializerImpl();
		return xmlDeserializer.deserializeDataflow(dataflowElement);
	}

	protected List<Processor> findProcessors(Dataflow dataflow, DataflowActivity activity) {
		List<Processor> processors = new ArrayList<Processor>();
		for (Processor processor : dataflow.getProcessors()) {
			if (processor.getActivityList().contains(activity)) {
				processors.add(processor);
			}
		}
		return processors;
	}
	
}
