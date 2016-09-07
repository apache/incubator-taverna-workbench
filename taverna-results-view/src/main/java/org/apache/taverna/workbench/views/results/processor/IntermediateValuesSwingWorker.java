package org.apache.taverna.workbench.views.results.processor;

import javax.swing.SwingWorker;

public class IntermediateValuesSwingWorker extends
		SwingWorker<ProcessorResultsComponent, String> {
	private ProcessorResultsComponent component;
	private Exception exception = null;

	public IntermediateValuesSwingWorker(ProcessorResultsComponent component) {
		this.component = component;
	}

	@Override
	protected ProcessorResultsComponent doInBackground() throws Exception {
		try {
			component.populateEnactmentsMaps();
		} catch (Exception e) {
			this.exception = e;
		}
		return component;
	}

	public Exception getException() {
		return exception;
	}
}