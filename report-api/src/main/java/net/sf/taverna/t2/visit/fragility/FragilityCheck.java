/**
 * 
 */
package net.sf.taverna.t2.visit.fragility;

import net.sf.taverna.t2.visit.VisitKind;

/**
 * @author alanrw
 *
 */
public class FragilityCheck extends VisitKind {
	
	public static final int INVALID_DEPTH = 1;
	public static final int SOURCE_FRAGILE = 2;
	
	public Class<FragilityChecker> getVisitorClass() {
		return FragilityChecker.class;
	}
	
	private static class Singleton {
		private static FragilityCheck instance = new FragilityCheck();
	}
	
	public static FragilityCheck getInstance() {
		return Singleton.instance;
	}

}
