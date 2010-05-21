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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.annotation.VisitorDescription#getIcon()
	 */
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static class Singleton {
		private static UnresolvedOutputKind instance = new UnresolvedOutputKind();
	}
	
	public static UnresolvedOutputKind getInstance() {
		return Singleton.instance;
	}



}
