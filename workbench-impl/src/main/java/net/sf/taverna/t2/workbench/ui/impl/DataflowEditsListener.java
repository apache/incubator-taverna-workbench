package net.sf.taverna.t2.workbench.ui.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowRedoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workflow.edits.UpdateDataflowInternalIdentifierEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Listens out for any edits on a dataflow and changes its internal id (or back
 * to the old one in the case of redo/undo). Is first created when the workbench
 * is initialised
 *
 * @author Ian Dunlop
 *
 */
public class DataflowEditsListener implements Observer<EditManagerEvent> {

	private static Logger logger = Logger.getLogger(DataflowEditsListener.class);

	private Map<Edit<?>, URI> dataflowEditMap;

	public DataflowEditsListener() {
		super();
		dataflowEditMap = new HashMap<Edit<?>, URI>();
	}

	/**
	 * Receives {@link EditManagerEvent}s from the {@link EditManager} and
	 * changes the id of the {@link Dataflow} to a new one or back to its old
	 * one depending on whether it is a do/undo/redo event. Stores the actual
	 * edit and the pre-edit dataflow id in a Map and changes the id when it
	 * gets further actions against this same edit
	 */
	public void notify(Observable<EditManagerEvent> observable,
			EditManagerEvent event) throws Exception {

		if (event instanceof DataflowEditEvent) {
			// the dataflow has been edited in some way so change its internal
			// id and store the old one against the edit that is changing
			// 'something'
			WorkflowBundle dataFlow = ((DataflowEditEvent) event).getDataFlow();
			URI internalIdentifier = dataFlow.getGlobalBaseURI();
			Edit<?> edit = event.getEdit();
			dataflowEditMap.put(edit, internalIdentifier);
			URI newIdentifier = WorkflowBundle.generateIdentifier();
			new UpdateDataflowInternalIdentifierEdit(dataFlow,
					newIdentifier).doEdit();
			logger.debug("Workflow edit, id changed from: " + internalIdentifier
					+ " to " + newIdentifier);

		} else if (event instanceof DataFlowRedoEvent) {
			// change the id back to the old one and store the new one in case
			// we want to change it back
			Edit<?> edit = event.getEdit();
			WorkflowBundle dataFlow = ((DataFlowRedoEvent) event).getDataFlow();
			URI newId = dataFlow.getGlobalBaseURI();

			URI oldId = dataflowEditMap.get(edit);
			dataflowEditMap.put(edit, newId);

			new UpdateDataflowInternalIdentifierEdit(dataFlow, oldId)
					.doEdit();
			logger.debug("Workflow edit, id changed from: " + newId + " to "
					+ oldId);

		} else if (event instanceof DataFlowUndoEvent) {
			// change the id back to the old one and store the new one in case
			// we want to change it back
			Edit<?> edit = event.getEdit();
			WorkflowBundle dataFlow = ((DataFlowUndoEvent) event).getDataFlow();
			URI newId = dataFlow.getGlobalBaseURI();

			URI oldId = dataflowEditMap.get(edit);
			dataflowEditMap.put(edit, newId);

			new UpdateDataflowInternalIdentifierEdit(dataFlow, oldId)
					.doEdit();
			logger.debug("Workflow edit, id changed from: " + newId + " to "
					+ oldId);
		}

	}

}
