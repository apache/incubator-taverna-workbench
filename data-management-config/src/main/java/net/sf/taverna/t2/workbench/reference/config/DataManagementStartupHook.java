/**
 * 
 */
package net.sf.taverna.t2.workbench.reference.config;




import net.sf.taverna.t2.workbench.StartupSPI;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public final class DataManagementStartupHook implements StartupSPI {
	
	final static Logger logger = Logger.getLogger(DataManagementStartupHook.class); 
	
	final static DatabaseProblemPanel databaseProblemPanel = new DatabaseProblemPanel();
	
	DataManagementConfiguration config = DataManagementConfiguration.getInstance();

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.StartupSPI#startup()
	 */
	@Override
	public boolean startup() {
		final boolean ok = DataManagementHelper.checkDatabase();
		if (!ok) {
			databaseProblemPanel.showDialog();
		}
		return ok;
	}
	

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.StartupSPI#positionHint()
	 */
	@Override
	public int positionHint() {
		return 850;
	}

}
