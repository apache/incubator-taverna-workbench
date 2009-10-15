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

    public static final String IN_MEMORY = "In Memory";
    public static final String ENABLE_PROVENANCE = "Provenance enabled";
    public static final String CONNECTOR_TYPE = "Connector type";
    public static final String PORT = "Port";
    public static final String CURRENT_PORT = "Current port";
    public static final String REFERENCE_SERVICE_CONTEXT = "referenceService.context";
    public static final String IN_MEMORY_CONTEXT = "inMemoryReferenceServiceContext.xml";
    public static final String HIBERNATE_CONTEXT = "hibernateReferenceServiceContext.xml";
    public static final String POOL_MAX_ACTIVE = "Pool max active";
    public static final String POOL_MIN_IDLE = "Pool min idle";
    public static final String POOL_MAX_IDLE = "Pool max idle";
    public static final String DRIVER_CLASS_NAME = "Driver class name";
    
    public static final String JNDI_NAME = "jdbc/taverna";
    
    private Map<String, String> defaultPropertyMap;
    private static DataManagementConfiguration instance;

    public static DataManagementConfiguration getInstance() {
        if (instance == null) {
            instance = new DataManagementConfiguration();
            
            //FIXME: Still a silly place to start it
            DataManagementHelper.startDerbyNetworkServer();
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
            defaultPropertyMap.put(DRIVER_CLASS_NAME, "org.apache.derby.jdbc.ClientDriver");
            defaultPropertyMap.put(POOL_MAX_ACTIVE, "50");
            defaultPropertyMap.put(POOL_MAX_IDLE, "50");
            defaultPropertyMap.put(POOL_MIN_IDLE, "10");
            
            defaultPropertyMap.put(CONNECTOR_TYPE,"Derby DB Connector");
        }
        return defaultPropertyMap;
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
}
