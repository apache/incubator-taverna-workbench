package net.sf.taverna.t2.workbench.report;

import javax.swing.Icon;


public class IncompleteDataflowKind extends ValidationVisitorKind {
	
	private static class Singleton {
		private static IncompleteDataflowKind instance = new IncompleteDataflowKind();
	}

	public static final int INCOMPLETE_DATAFLOW = 1;
	
	public static IncompleteDataflowKind getInstance() {
		return Singleton.instance;
	}

	public Icon getIcon() {
		return null;
	}

}
