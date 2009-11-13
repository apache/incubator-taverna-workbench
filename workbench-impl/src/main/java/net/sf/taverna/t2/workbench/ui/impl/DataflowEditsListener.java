package net.sf.taverna.t2.workbench.ui.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowRedoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;

import org.apache.log4j.Logger;

/**
 * Listens out for any edits on a dataflow and changes its internal id (or back
 * to the old one in the case of redo/undo). Is first created when the workbench
 * is initialised
 * 
 * @author Ian Dunlop
 * 
 */
public class DataflowEditsListener implements Observer<EditManagerEvent> {

	private static Logger logger = Logger
			.getLogger(DataflowEditsListener.class);

	private Map<Edit<?>, String> dataflowEditMap;
	private Edits edits;

	private DataflowEditsListener() {
		super();
		edits = EditManager.getInstance().getEdits();
		dataflowEditMap = new HashMap<Edit<?>, String>();
	}
	
	private static class Singleton {
		private static DataflowEditsListener instance = new DataflowEditsListener();
	}

	/**
	 * Returns a singleton instance of this listener
	 * 
	 * @return
	 */
	public static DataflowEditsListener getInstance() {
		return Singleton.instance;
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
			Dataflow dataFlow = ((DataflowEditEvent) event).getDataFlow();
			String internalIdentier = dataFlow.getInternalIdentier();
			Edit<?> edit = event.getEdit();
			dataflowEditMap.put(edit, internalIdentier);
			String newIdentifier = UUID.randomUUID().toString();
			edits.getUpdateDataflowInternalIdentifierEdit(dataFlow,
					newIdentifier).doEdit();
			logger.debug("Workflow edit, id changed from: " + internalIdentier
					+ " to " + newIdentifier);

		} else if (event instanceof DataFlowRedoEvent) {
			// change the id back to the old one and store the new one in case
			// we want to change it back
			Edit<?> edit = event.getEdit();
			Dataflow dataFlow = ((DataFlowRedoEvent) event).getDataFlow();
			String newId = dataFlow.getInternalIdentier();

			String oldId = dataflowEditMap.get(edit);
			dataflowEditMap.put(edit, newId);

			edits.getUpdateDataflowInternalIdentifierEdit(dataFlow, oldId)
					.doEdit();
			logger.debug("Workflow edit, id changed from: " + newId + " to "
					+ oldId);

		} else if (event instanceof DataFlowUndoEvent) {
			// change the id back to the old one and store the new one in case
			// we want to change it back
			Edit<?> edit = event.getEdit();
			Dataflow dataFlow = ((DataFlowUndoEvent) event).getDataFlow();
			String newId = dataFlow.getInternalIdentier();

			String oldId = dataflowEditMap.get(edit);
			dataflowEditMap.put(edit, newId);

			edits.getUpdateDataflowInternalIdentifierEdit(dataFlow, oldId)
					.doEdit();
			logger.debug("Workflow edit, id changed from: " + newId + " to "
					+ oldId);
		}

	}

}
