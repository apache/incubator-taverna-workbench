/**
 * 
 */
package net.sf.taverna.t2.workbench.report;

import javax.swing.Icon;

/**
 * @author alanrw
 *
 */
public class FailedEntityKind extends ValidationVisitorKind {

	public static final int FAILED_ENTITY = 1;

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.annotation.VisitorDescription#getIcon()
	 */
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static class Singleton {
		private static FailedEntityKind instance = new FailedEntityKind();
	}
	
	public static FailedEntityKind getInstance() {
		return Singleton.instance;
	}



}
