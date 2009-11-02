/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.reference.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * Configuration for the reference service and provenance.
 * 
 * @author David Withers
 * @author Stuart Owen
 */

public class DataManagementConfiguration extends AbstractConfigurable {

    public static final String IN_MEMORY = "in_memory";
    public static final String ENABLE_PROVENANCE = "provenance";
    public static final String CONNECTOR_TYPE = "connector";
    public static final String PORT = "port";
    public static final String CURRENT_PORT = "current_port";
    public static final String REFERENCE_SERVICE_CONTEXT = "referenceService.context";
    public static final String IN_MEMORY_CONTEXT = "inMemoryReferenceServiceContext.xml";
    public static final String HIBERNATE_CONTEXT = "hibernateReferenceServiceContext.xml";
    public static final String HIBERNATE_DIALECT = "dialect";
    public static final String START_INTERNAL_DERBY = "start_derby";
    public static final String POOL_MAX_ACTIVE = "pool_max_active";
    public static final String POOL_MIN_IDLE = "pool_min_idle";
    public static final String POOL_MAX_IDLE = "pool_max_idle";
    public static final String DRIVER_CLASS_NAME = "driver";
    public static final String JDBC_URI = "jdbcuri";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    
    //FIXME: these should me just mysql & derby - but build & dependency issues is causing the provenance to expect these values:
    public static final String CONNECTOR_MYSQL="mysql";
    public static final String CONNECTOR_DERBY="derby";
    
    public static final String JNDI_NAME = "jdbc/taverna";
    
    private Map<String, String> defaultPropertyMap;
    private static DataManagementConfiguration instance;

    public static DataManagementConfiguration getInstance() {
        if (instance == null) {
            instance = new DataManagementConfiguration();
                        
            if (instance.getStartInternalDerbyServer()) {
            	DataManagementHelper.startDerbyNetworkServer();
            }
            
            DataManagementHelper.setupDataSource();
        }
        return instance;
    }

    public String getDatabaseContext() {
        if (getProperty(IN_MEMORY).equalsIgnoreCase("true")) {
            return IN_MEMORY_CONTEXT;
        } else {
            return HIBERNATE_CONTEXT;
        }
    }
    
    public String getDriverClassName() {
    	return getProperty(DRIVER_CLASS_NAME);
    }
    
    public boolean isProvenanceEnabled() {            	
        return getProperty(ENABLE_PROVENANCE).equalsIgnoreCase("true");
    }
    
    public boolean getStartInternalDerbyServer() {
    	return getProperty(START_INTERNAL_DERBY).equalsIgnoreCase("true");
    }
    
    public int getPort() {
    	return Integer.valueOf(getProperty(PORT));
    }
    
    public void setCurrentPort(int port) {
    	setProperty(CURRENT_PORT, String.valueOf(port));
    }
    
    public int getCurrentPort() {
    	return Integer.valueOf(getProperty(CURRENT_PORT));
    }
    
    public int getPoolMaxActive() {
    	return Integer.valueOf(getProperty(POOL_MAX_ACTIVE));
    }
    
    public int getPoolMinIdle() {
    	return Integer.valueOf(getProperty(POOL_MIN_IDLE));
    }
    
    public int getPoolMaxIdle() {
    	return Integer.valueOf(getProperty(POOL_MAX_IDLE));
    }

    private DataManagementConfiguration() {
        
    }

    public String getCategory() {
        return "general";
    }

    public Map<String, String> getDefaultPropertyMap() {
    	
        if (defaultPropertyMap == null) {
            defaultPropertyMap = new HashMap<String, String>();
            defaultPropertyMap.put(IN_MEMORY, "false");
            defaultPropertyMap.put(ENABLE_PROVENANCE, "true");
            defaultPropertyMap.put(PORT, "1527");
            //defaultPropertyMap.put(DRIVER_CLASS_NAME, "org.apache.derby.jdbc.ClientDriver");
            defaultPropertyMap.put(DRIVER_CLASS_NAME, "org.apache.derby.jdbc.EmbeddedDriver");
            defaultPropertyMap.put(HIBERNATE_DIALECT, "org.hibernate.dialect.DerbyDialect");
            defaultPropertyMap.put(POOL_MAX_ACTIVE, "50");
            defaultPropertyMap.put(POOL_MAX_IDLE, "50");
            defaultPropertyMap.put(POOL_MIN_IDLE, "10");
            defaultPropertyMap.put(USERNAME,"");
            defaultPropertyMap.put(PASSWORD,"");            
            defaultPropertyMap.put(JDBC_URI,"jdbc:derby:t2-database;create=true;upgrade=true");
            defaultPropertyMap.put(START_INTERNAL_DERBY, "false");
            
            defaultPropertyMap.put(CONNECTOR_TYPE,CONNECTOR_DERBY);
        }
        return defaultPropertyMap;
    }

    public String getHibernateDialect() {
    	return getProperty(HIBERNATE_DIALECT);
    }
    
    public String getName() {
        return "Data & provenance";
    }

    public String getUUID() {
        return "6BD3F5C1-C68D-4893-8D9B-2F46FA1DDB19";
    }


    public String getConnectorType() {        
        return getProperty(CONNECTOR_TYPE);
    }

    
	public String getJDBCUri() {
		if (CONNECTOR_DERBY.equals(getConnectorType()) && getStartInternalDerbyServer()) {
			return "jdbc:derby://localhost:" + getCurrentPort() + "/t2-database;create=true;upgrade=true";			
		}
		else {
			return getProperty(JDBC_URI);
		}
	}

	public String getUsername() {
		return getProperty(USERNAME);
	}
	
	public String getPassword() {
		return getProperty(PASSWORD);
	}
}
