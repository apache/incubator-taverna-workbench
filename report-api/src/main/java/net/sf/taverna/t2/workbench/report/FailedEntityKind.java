/**
 * 
 */
package net.sf.taverna.t2.workbench.report;


/**
 * @author alanrw
 *
 */
public class FailedEntityKind extends ValidationVisitorKind {

	public static final int FAILED_ENTITY = 1;

	private static class Singleton {
		private static FailedEntityKind instance = new FailedEntityKind();
	}
	
	public static FailedEntityKind getInstance() {
		return Singleton.instance;
	}



}
