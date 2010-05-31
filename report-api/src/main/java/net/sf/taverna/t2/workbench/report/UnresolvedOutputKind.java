/**
 * 
 */
package net.sf.taverna.t2.workbench.report;

import javax.swing.Icon;

/**
 * @author alanrw
 *
 */
public class UnresolvedOutputKind extends ValidationVisitorKind {

	public static final int OUTPUT = 1;

	private static class Singleton {
		private static UnresolvedOutputKind instance = new UnresolvedOutputKind();
	}
	
	public static UnresolvedOutputKind getInstance() {
		return Singleton.instance;
	}



}
