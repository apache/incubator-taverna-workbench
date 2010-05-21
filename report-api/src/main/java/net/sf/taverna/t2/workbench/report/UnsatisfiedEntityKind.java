package net.sf.taverna.t2.workbench.report;

import javax.swing.Icon;

public class UnsatisfiedEntityKind extends ValidationVisitorKind {

	public static final int UNSATISFIED_ENTITY = 1;

	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static class Singleton {
		private static UnsatisfiedEntityKind instance = new UnsatisfiedEntityKind();
	}
	
	public static UnsatisfiedEntityKind getInstance() {
		return Singleton.instance;
	}



}
