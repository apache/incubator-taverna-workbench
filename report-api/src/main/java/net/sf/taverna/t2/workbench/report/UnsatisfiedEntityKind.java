package net.sf.taverna.t2.workbench.report;


public class UnsatisfiedEntityKind extends ValidationVisitorKind {

	public static final int UNSATISFIED_ENTITY = 1;

	private static class Singleton {
		private static UnsatisfiedEntityKind instance = new UnsatisfiedEntityKind();
	}
	
	public static UnsatisfiedEntityKind getInstance() {
		return Singleton.instance;
	}



}
