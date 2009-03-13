package net.sf.taverna.t2.workbench.provenance;

import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.spi.SPIRegistry;

public class ProvenanceConfigurationRegistry extends SPIRegistry<ProvenanceConfiguration>{
	
	private static ProvenanceConfigurationRegistry instance;
	
	protected ProvenanceConfigurationRegistry() {
		super(ProvenanceConfiguration.class);
	}
	
	public static synchronized ProvenanceConfigurationRegistry getRegistry() {
		
		if (instance == null) {
			instance = new ProvenanceConfigurationRegistry();
		}
		return instance;
	}
	
	public ProvenanceConfiguration getConfigForProvenanceConnector(ProvenanceConnector connector) {
		//loop through the available config and find the correct one for the type of connector
		return null;
	}

}
