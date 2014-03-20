/**
 * 
 */
package net.sf.taverna.t2.workbench.reference.config;

import net.sf.taverna.t2.workbench.ShutdownSPI;

/**
 * @author alanrw
 *
 */
public final class DataManagementShutdownHook implements ShutdownSPI {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ShutdownSPI#shutdown()
	 */
	@Override
	public boolean shutdown() {
		DataManagementHelper.unlockDatabase();
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ShutdownSPI#positionHint()
	 */
	@Override
	public int positionHint() {
		return 1100;
	}

}
