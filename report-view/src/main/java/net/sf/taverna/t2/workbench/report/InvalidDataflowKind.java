package net.sf.taverna.t2.workbench.report;



public class InvalidDataflowKind extends ValidationVisitorKind {
	
	private static class Singleton {
		private static InvalidDataflowKind instance = new InvalidDataflowKind();
	}

	public static final int INVALID_DATAFLOW = 1;
	
	public static InvalidDataflowKind getInstance() {
		return Singleton.instance;
	}

}
