package net.sf.taverna.t2.workbench.reference.config;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;

/**
 * A set of utility methods related to basic data management.
 * 
 * @author Stuart Owen
 *
 */
public class DataManagementHelper {
	
	private final static Logger logger = Logger.getLogger(DataManagementHelper.class); 
			
	private static NetworkServerControl server;
	
	public static void setupDataSource() {
		
		DataManagementConfiguration config = DataManagementConfiguration.getInstance();
        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.osjava.sj.memory.MemoryContextFactory");
            System.setProperty("org.osjava.sj.jndi.shared", "true");

            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(config.getDriverClassName());

            ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            ds.setMaxActive(config.getPoolMaxActive());
            ds.setMinIdle(config.getPoolMinIdle());
            ds.setMaxIdle(config.getPoolMaxIdle());
            ds.setDefaultAutoCommit(true);
            ds.setInitialSize(config.getPoolMinIdle());
            try {
                ds.setUrl(getJDBCUri());
                InitialContext context = new InitialContext();
                context.rebind(DataManagementConfiguration.JNDI_NAME, ds);
            } catch (IOException ex) {
                logger.error("Unable to set up DataSource",ex);
            }
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }
	
	public static boolean isRunning() {
		if (server==null) {
			return false;			
		}
		else {
			try {
				server.ping();
				return true;
			} catch (Exception e) {
				return false;
			}	
		}
	}
	
	public static Connection openConnection() throws NamingException, SQLException {
		return ((DataSource)new InitialContext().lookup(DataManagementConfiguration.JNDI_NAME)).getConnection();
	}
	
	public synchronized static void startDerbyNetworkServer() {
        String homeDir=ApplicationRuntime.getInstance().getApplicationHomeDir().getAbsolutePath();
        String logDir=homeDir+File.separator+"logs";
        
        //make the logs directory if it doesn't already exist
        File logDirFile=new File(logDir);
        if (!logDirFile.exists()) logDirFile.mkdir();
        
        System.setProperty("derby.drda.host","localhost");
        System.setProperty("derby.drda.minThreads","5");
        System.setProperty("derby.drda.maxThreads",String.valueOf(DataManagementConfiguration.getInstance().getPoolMaxActive()));        
        System.setProperty("derby.system.home",homeDir);
        System.setProperty("derby.stream.error.file",logDir+File.separator+"derby.log");
        int port=DataManagementConfiguration.getInstance().getPort();
        int maxPort = port+10;
        
        try {
        	System.setProperty("derby.drda.portNumber",String.valueOf(port));
            if (server==null) server = new NetworkServerControl();
            while(port<maxPort) { //loop to find another available port on which Derby isn't already running
            	if (!isRunning()) break; 
            	logger.info("Derby connection port: "+port+" is currently not available for Taverna, trying next value");
            	port++;
            	System.setProperty("derby.drda.portNumber",String.valueOf(port));
            	server = new NetworkServerControl();
            }
            server.start(null);
            DataManagementConfiguration.getInstance().setCurrentPort(port);
        } catch (Exception ex) {
            logger.error("Error starting up Derby network server",ex);
        }
    }
	
	public static void stopDerbyNetworkServer() {
		try {
			server.shutdown();
		} catch (Exception e) {
			logger.error("Error shuttong down theDerby network server",e);
		}
	}
	
	
	protected static  String getJDBCUri() throws IOException {	  
      return "jdbc:derby://localhost:" + DataManagementConfiguration.getInstance().getCurrentPort() + "/t2-database;create=true;upgrade=true";
	}
	

}
