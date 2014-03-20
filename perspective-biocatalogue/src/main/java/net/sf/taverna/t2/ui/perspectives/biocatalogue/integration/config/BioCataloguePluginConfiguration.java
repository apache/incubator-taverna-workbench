package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config.BioCataloguePluginConfigurationPanel.ServiceCatalogue;
import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;
import net.sf.taverna.t2.workbench.ui.impl.WorkbenchProfileProperties;

/**
 * 
 * 
 * @author Sergejs Aleksejevs
 */
public class BioCataloguePluginConfiguration extends AbstractConfigurable{
	
	public static final String SERVICE_CATALOGUE_BASE_URL_PROPERTY = "servicecatalogue.base.url";
	public static final String SERVICE_CATALOGUE_NAME_PROPERTY = "servicecatalogue.name";
	
	public static final String SOAP_OPERATIONS_IN_SERVICE_PANEL = "SOAP_Operations_in_Service_Panel";
	
	public static final String REST_METHODS_IN_SERVICE_PANEL = "REST_Methods_in_Service_Panel";

	public static final String DEFAULT_SERVICE_CATALOGUE_TYPE = "DEFAULT";
	public static final String USER_ADDED_SERVICE_CATALOGUE_TYPE = "USER_ADDED";
	
	 // Default Service Catalogue
	public static final String BIOCATALOGUE_URL = "https://www.biocatalogue.org";
	public static final String BIOCATALOGUE_NAME = "BioCatalogue";
	
	private static final Charset ENCODING = StandardCharsets.UTF_8;
	
	private static final String USER_ADDED_SERVICE_CATALOGUES_FILE = "user-added-service-catalogues.txt";
	
	private static File userAddedCataloguesFile;
	
	private static final String CONF = "conf";
	
	private static Logger logger = Logger
			.getLogger(BioCataloguePluginConfiguration.class);	

	private static class Singleton {
		private static BioCataloguePluginConfiguration instance = new BioCataloguePluginConfiguration();
	}
	
	private Map<String, String> defaultPropertyMap;

	public static BioCataloguePluginConfiguration getInstance() {
		return Singleton.instance;
	}

	public String getCategory() {
		return "general";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<String, String>();
			defaultPropertyMap.put(SERVICE_CATALOGUE_BASE_URL_PROPERTY,
					WorkbenchProfileProperties.getWorkbenchProfileProperty(SERVICE_CATALOGUE_BASE_URL_PROPERTY, BIOCATALOGUE_URL));
			defaultPropertyMap.put(SERVICE_CATALOGUE_NAME_PROPERTY,
					WorkbenchProfileProperties.getWorkbenchProfileProperty(SERVICE_CATALOGUE_NAME_PROPERTY, BIOCATALOGUE_NAME));
		}
		return defaultPropertyMap;
	}

	public String getDisplayName() {
		return "Service catalogue";
	}

	public String getFilePrefix() {
		return "ServiceCatalogue";
	}

	public String getUUID() {
		return "4daac25c-bd56-4f90-b909-1e49babe5197";
	}

	/**
	 * Just a "proxy" method - {@link AbstractConfigurable#store()} is not
	 * visible to the users of instances of this class otherwise.
	 */
	public void store() {
		super.store();
	}

	/*
	 * Read a list of users added catalogue entries from a specially formatted
	 * file. Each entry is on a separate line in the format:
	 * <SERVICE_CATALOGUE_FRIENDLY_NAME>\t<SERVICE_CATALOGUE_URL> (i.e. tab
	 * separated).
	 */
	public static List<ServiceCatalogue> getUserAddedServiceCatalogues() {

		List<ServiceCatalogue> catalogues = new ArrayList<ServiceCatalogue>();

		if (userAddedCataloguesFile == null) {
			File confDir = new File(ApplicationRuntime.getInstance()
					.getApplicationHomeDir(), CONF);
			if (!confDir.exists()) {
				confDir.mkdir();
			}
			userAddedCataloguesFile = new File(confDir,
					USER_ADDED_SERVICE_CATALOGUES_FILE);
		}
		
		Path path = userAddedCataloguesFile.toPath();

		List<String> list = new ArrayList<String>();

		if (userAddedCataloguesFile.exists()) {
			try {
				list = Files.readAllLines(path, ENCODING);
			} catch (IOException ioex) {
				logger.error(
						"Failed to read user-defined service catalogues from: "
								+ userAddedCataloguesFile.getAbsolutePath(), ioex);				
			}
			for (String catalogueEntry : list) {
				// Split the friendly name and URL our of the catalogue entry
				// line read from the file
				String[] parts = catalogueEntry.split("\\t");
				if (parts[0] != null && parts[1] != null) {
					ServiceCatalogue sc = new ServiceCatalogue(
							parts[0],
							parts[1],
							BioCataloguePluginConfiguration.USER_ADDED_SERVICE_CATALOGUE_TYPE);
					catalogues.add(sc);
				}
			}
		}
		return catalogues;
	}
	
	/*
	 * Write user-added service catalogues to a file. Each entry is on a
	 * separate line in the format:
	 * <SERVICE_CATALOGUE_FRIENDLY_NAME>\t<SERVICE_CATALOGUE_URL> (i.e. tab
	 * separated).
	 */
	public static void saveUserAddedServiceCatalogues(
			List<ServiceCatalogue> catalogues) throws IOException{

		if (userAddedCataloguesFile == null){
			File confDir = new File(ApplicationRuntime.getInstance()
					.getApplicationHomeDir(), CONF);
			if (!confDir.exists()) {
				confDir.mkdir();
			}
			userAddedCataloguesFile = new File(confDir,
					USER_ADDED_SERVICE_CATALOGUES_FILE);
		}

		List<String> list = new ArrayList<String>();
		for (ServiceCatalogue catalogue : catalogues) {
			list.add(catalogue.getName() + "\t" + catalogue.getUrl());
		}

		Files.write(userAddedCataloguesFile.toPath(), list, ENCODING,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
	}

	public static File getUserAddedCataloguesFile() {
		
		if (userAddedCataloguesFile == null){
			File confDir = new File(ApplicationRuntime.getInstance()
					.getApplicationHomeDir(), CONF);
			if (!confDir.exists()) {
				confDir.mkdir();
			}
			userAddedCataloguesFile = new File(confDir,
					USER_ADDED_SERVICE_CATALOGUES_FILE);
		}
		return userAddedCataloguesFile;
	}
}
