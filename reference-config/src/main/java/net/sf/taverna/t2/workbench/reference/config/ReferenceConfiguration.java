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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.derby.drda.NetworkServerControl;

/**
 * Configuration for the reference service.
 * 
 * @author David Withers
 */
public class ReferenceConfiguration extends AbstractConfigurable {

    public static final String IN_MEMORY = "In Memory";
    public static final String ENABLE_PROVENANCE = "Provenance enabled";
    public static final String CONNECTOR_TYPE = "Connector type";
    public static final String PORT = "Port";
    public static final String REFERENCE_SERVICE_CONTEXT = "referenceService.context";
    public static final String IN_MEMORY_CONTEXT = "inMemoryReferenceServiceContext.xml";
    public static final String HIBERNATE_CONTEXT = "hibernateReferenceServiceContext.xml";
    private Map<String, String> defaultPropertyMap;
    private static ReferenceConfiguration instance;

    public static ReferenceConfiguration getInstance() {
        if (instance == null) {
            instance = new ReferenceConfiguration();
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

    public boolean isProvenanceEnabled() {
        System.out.println("Provenenance enabled = "+getProperty(ENABLE_PROVENANCE).equalsIgnoreCase("true"));
        return getProperty(ENABLE_PROVENANCE).equalsIgnoreCase("true");
    }

    public void startDerbyNetworkServer() {
        NetworkServerControl server;
        System.setProperty("derby.drda.host","localhost");
        System.setProperty("derby.drda.portNumber",getProperty(PORT));
        try {
            server = new NetworkServerControl();
            server.start(null);
        } catch (Exception ex) {
            Logger.getLogger(ReferenceConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setupDataSource() {
        //FIXME: this is temporary placeholder for setting up the database connection.

        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.osjava.sj.memory.MemoryContextFactory");
            System.setProperty("org.osjava.sj.jndi.shared", "true");

            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
            //ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
            ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            ds.setMaxActive(50);
            ds.setMinIdle(10);
            ds.setMaxIdle(50);
            ds.setDefaultAutoCommit(true);
            ds.setInitialSize(20);
            try {
                ds.setUrl(getJDBCUri());
                InitialContext context = new InitialContext();
                context.rebind("jdbc/taverna", ds);
            } catch (IOException ex) {
                Logger.getLogger(ReferenceConfiguration.class.getName()).log(Level.SEVERE, "Unable to set up local database", ex);
            }
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }

    private ReferenceConfiguration() {
        startDerbyNetworkServer();
        setupDataSource();
    }

    public String getCategory() {
        return "general";
    }

    public Map<String, String> getDefaultPropertyMap() {
        if (defaultPropertyMap == null) {
            defaultPropertyMap = new HashMap<String, String>();
            defaultPropertyMap.put(IN_MEMORY, "false");
            defaultPropertyMap.put(ENABLE_PROVENANCE, "true");
            defaultPropertyMap.put(PORT, "1529");
            defaultPropertyMap.put(CONNECTOR_TYPE,"Derby DB Connector");
        }
        return defaultPropertyMap;
    }

    public String getName() {
        return "Data storage & provenance";
    }

    public String getUUID() {
        return "6BD3F5C1-C68D-4893-8D9B-2F46FA1DDB19";
    }

    private String getJDBCUri() throws IOException {

//        File applicationHomeDir = ApplicationRuntime.getInstance().getApplicationHomeDir();
//        File dbFile = new File(applicationHomeDir, "t2-database");
//        deleteDirectory(dbFile);
//        if (!dbFile.mkdir()) {
//            throw new IOException("Could not create database " + dbFile);
//        }
        //return "jdbc:derby:" + dbFile.toString() + "/database;create=true;upgrade=true";
        return "jdbc:derby://localhost:" + getProperty(PORT) + "/t2-database;create=true;upgrade=true";

    }

    // copied from http://www.rgagnon.com/javadetails/java-0483.html
    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public String getConnectorType() {
        System.out.println("Connector type = "+getProperty(CONNECTOR_TYPE));
        return getProperty(CONNECTOR_TYPE);
    }
}
