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
package net.sf.taverna.t2.servicedescriptions.impl;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionsConfiguration;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.events.AddedProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.PartialServiceDescriptionsNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderErrorNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderStatusNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderUpdatingNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderWarningNotification;
import net.sf.taverna.t2.servicedescriptions.events.RemovedProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import uk.org.taverna.configuration.app.ApplicationConfiguration;

public class ServiceDescriptionRegistryImpl implements ServiceDescriptionRegistry {

	/**
	 * If a writable property of this name on a provider exists (ie. the provider has a method
	 * setServiceDescriptionRegistry(ServiceDescriptionRegistry registry) - then this property will
	 * be set to the current registry.
	 */
	public static final String SERVICE_DESCRIPTION_REGISTRY = "serviceDescriptionRegistry";

	public static Logger logger = Logger.getLogger(ServiceDescriptionRegistryImpl.class);

	public static final ThreadGroup threadGroup = new ThreadGroup("Service description providers");

	private ServiceDescriptionsConfiguration serviceDescriptionsConfig;
	private ApplicationConfiguration applicationConfiguration;

	/**
	 * Total maximum timeout while waiting for description threads to finish
	 */
	private static final long DESCRIPTION_THREAD_TIMEOUT_MS = 3000;

	protected static final String CONF_DIR = "conf";

	protected static final String SERVICE_PROVIDERS_FILENAME = "service_providers.xml";

	private static final String DEFAULT_CONFIGURABLE_SERVICE_PROVIDERS_FILENAME = "default_service_providers.xml";

	/**
	 * <code>false</code> until first call to {@link #loadServiceProviders()} - which is done by
	 * first call to {@link #getServiceDescriptionProviders()}.
	 */
	private boolean hasLoadedProviders = false;

	/**
	 * <code>true</code> while {@link #loadServiceProviders(File)},
	 * {@link #loadServiceProviders(URL)} or {@link #loadServiceProviders()} is in progress, avoids
	 * triggering {@link #saveServiceDescriptions()} on
	 * {@link #addServiceDescriptionProvider(ServiceDescriptionProvider)} calls.
	 */
	private boolean loading = false;

	private MultiCaster<ServiceDescriptionRegistryEvent> observers = new MultiCaster<ServiceDescriptionRegistryEvent>(this);

	private List<ServiceDescriptionProvider> serviceDescriptionProviders;

	private Set<ServiceDescriptionProvider> allServiceProviders;

	private Map<ServiceDescriptionProvider, Set<ServiceDescription>> providerDescriptions = new HashMap<ServiceDescriptionProvider, Set<ServiceDescription>>();

	private Map<ServiceDescriptionProvider, FindServiceDescriptionsThread> serviceDescriptionThreads = new HashMap<ServiceDescriptionProvider, FindServiceDescriptionsThread>();

	/**
	 * Service providers added by the user, should be saved
	 */
	private Set<ServiceDescriptionProvider> userAddedProviders = new HashSet<ServiceDescriptionProvider>();

	private Set<ServiceDescriptionProvider> userRemovedProviders = new HashSet<ServiceDescriptionProvider>();

	private Set<ServiceDescriptionProvider> defaultServiceDescriptionProviders;

	/**
	 * File containing a list of configured ConfigurableServiceProviders which is used to get the
	 * default set of service descriptions together with those provided by AbstractTemplateServiceS.
	 * This file is located in the conf directory of the Taverna startup directory.
	 */
	private File defaultConfigurableServiceProvidersFile;

	private boolean defaultSystemConfigurableProvidersLoaded = false;

	static {
		threadGroup.setMaxPriority(Thread.MIN_PRIORITY);
	}

	public ServiceDescriptionRegistryImpl(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
		defaultConfigurableServiceProvidersFile = new File(getTavernaStartupConfigurationDirectory(),
				DEFAULT_CONFIGURABLE_SERVICE_PROVIDERS_FILENAME);
	}

	/**
	 * Get the Taverna distribution (startup) configuration directory.
	 */
	private File getTavernaStartupConfigurationDirectory() {
		File distroHome = null;
		File configDirectory = null;
		distroHome = applicationConfiguration.getStartupDir();
		configDirectory = new File(distroHome, "conf");
		if (!configDirectory.exists()) {
			configDirectory.mkdir();
		}
		return configDirectory;
	}

	public static void joinThreads(Collection<? extends Thread> threads,
			long descriptionThreadTimeoutMs) {
		long finishJoinBy = System.currentTimeMillis() + descriptionThreadTimeoutMs;
		for (Thread thread : threads) {
			// No shorter timeout than 1 ms (thread.join(0) waits forever!)
			long timeout = Math.max(1, finishJoinBy - System.currentTimeMillis());
			try {
				thread.join(timeout);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			if (thread.isAlive()) {
				logger.debug("Thread did not finish " + thread);
			}
		}
	}


	public void addObserver(Observer<ServiceDescriptionRegistryEvent> observer) {
		observers.addObserver(observer);
	}

	public void addServiceDescriptionProvider(ServiceDescriptionProvider provider) {
		synchronized (this) {
			userRemovedProviders.remove(provider);
			if (!getDefaultServiceDescriptionProviders().contains(provider)) {
				userAddedProviders.add(provider);
			}
			allServiceProviders.add(provider);
		}

		// Spring-like auto-config
		try {
			// BeanUtils should ignore this if provider does not have that property
			BeanUtils.setProperty(provider, SERVICE_DESCRIPTION_REGISTRY, this);
		} catch (IllegalAccessException e) {
			logger.warn("Could not set serviceDescriptionRegistry on " + provider, e);
		} catch (InvocationTargetException e) {
			logger.warn("Could not set serviceDescriptionRegistry on " + provider, e);
		}

		if (!loading) {
			saveServiceDescriptions();
		}
		observers.notify(new AddedProviderEvent(provider));
		updateServiceDescriptions(false, false);
	}

	public File findServiceDescriptionsFile() {
		File confDir = new File(applicationConfiguration.getApplicationHomeDir(), CONF_DIR);
		confDir.mkdirs();
		if (!confDir.isDirectory()) {
			throw new RuntimeException("Invalid directory: " + confDir);
		}
		File serviceDescriptionsFile = new File(confDir, SERVICE_PROVIDERS_FILENAME);
		return serviceDescriptionsFile;
	}

	public List<Observer<ServiceDescriptionRegistryEvent>> getObservers() {
		return observers.getObservers();
	}

	// Fallback to this method that uses hardcoded default services if you cannot read them from
	// the file.
//	@SuppressWarnings("unchecked")
//	public synchronized Set<ServiceDescriptionProvider> getDefaultServiceDescriptionProvidersFallback() {
//		/*if (defaultServiceDescriptionProviders != null) {
//	 return defaultServiceDescriptionProviders;
//	 }
//	 defaultServiceDescriptionProviders = new HashSet<ServiceDescriptionProvider>();
//		 */
//		for (ServiceDescriptionProvider provider : serviceDescriptionProviders) {
//
//			/* We do not need these - already loaded them from getDefaultServiceDescriptionProviders()
//	 if (!(provider instanceof ConfigurableServiceProvider)) {
//	 defaultServiceDescriptionProviders.add(provider);
//	 continue;
//	 }*/
//
//			// Just load the hard coded default configurable service providers
//			if (provider instanceof ConfigurableServiceProvider){
//				ConfigurableServiceProvider<Object> template = ((ConfigurableServiceProvider<Object>)
//						provider);
//				// Get configurations
//				List<Object> configurables = template.getDefaultConfigurations();
//				for (Object config : configurables) {
//					// Make a copy that we can configure
//					ConfigurableServiceProvider<Object> configurableProvider = template.clone();
//					try {
//						configurableProvider.configure(config);
//					} catch (ConfigurationException e) {
//						logger.warn("Can't configure provider "
//								+ configurableProvider + " with " + config);
//						continue;
//					}
//					defaultServiceDescriptionProviders.add(configurableProvider);
//				}
//			}
//		}
//		return defaultServiceDescriptionProviders;
//	}

	// Get the default services.
	@SuppressWarnings("unchecked")
	public synchronized Set<ServiceDescriptionProvider> getDefaultServiceDescriptionProviders() {
		if (defaultServiceDescriptionProviders != null) {
			return defaultServiceDescriptionProviders;
		}
		defaultServiceDescriptionProviders = new HashSet<ServiceDescriptionProvider>();

		// Add default configurable service description providers
		// from the default_service_providers.xml file
		ServiceDescriptionDeserializer deserializer = new ServiceDescriptionDeserializer(
				serviceDescriptionProviders);
		if (defaultConfigurableServiceProvidersFile.exists()) {
			try {
				defaultServiceDescriptionProviders.addAll(deserializer
						.xmlToServiceRegistryForDefaultServices(this,
								defaultConfigurableServiceProvidersFile));
				// We have successfully loaded the defaults for system configurable providers.
				// Note that there are still defaults for third party configurable providers,
				// which will be loaded below using getDefaultConfigurations().
				defaultSystemConfigurableProvidersLoaded = true;
			} catch (Exception e) {
				logger.error("Could not load default service providers from "
						+ defaultConfigurableServiceProvidersFile.getAbsolutePath(), e);

				// Fallback on the old hardcoded method of loading default system configurable
				// service providers using getDefaultConfigurations().
				defaultSystemConfigurableProvidersLoaded = false;
			}
		} else {
			logger.warn("Could not find the file "
					+ defaultConfigurableServiceProvidersFile.getAbsolutePath()
					+ " containing default system service providers. Using the hardcoded list of default system providers.");

			// Fallback on the old hardcoded method of loading default system configurable
			// service providers using getDefaultConfigurations().
			defaultSystemConfigurableProvidersLoaded = false;
		}

		// Load other default service description providers - template, local workers
		// and third party configurable service providers
		for (ServiceDescriptionProvider provider : serviceDescriptionProviders) {
			// Template service providers (beanshell, string constant, etc. )
			// and providers of local workers.
			if (!(provider instanceof ConfigurableServiceProvider)) {
				defaultServiceDescriptionProviders.add(provider);
			} else {
				// Default system or third party configurable service description provider.
				// System ones are read from the default_service_providers.xml file so
				// getDefaultConfigurations() on them will not have much effect here unless
				// defaultSystemConfigurableProvidersLoaded is set to false.
				ConfigurableServiceProvider<Object> template = ((ConfigurableServiceProvider<Object>) provider);
				// Get configurations
				List<Object> configurables = template.getDefaultConfigurations();
				for (Object config : configurables) {
					// Make a copy that we can configure
					ConfigurableServiceProvider<Object> configurableProvider = template.clone();
					try {
						configurableProvider.configure(config);
					} catch (ConfigurationException e) {
						logger.warn("Can't configure provider " + configurableProvider + " with "
								+ config);
						continue;
					}
					defaultServiceDescriptionProviders.add(configurableProvider);
				}
			}
		}

		return defaultServiceDescriptionProviders;
	}

	public synchronized Set<ServiceDescriptionProvider> getServiceDescriptionProviders() {
		if (allServiceProviders != null) {
			return new HashSet<ServiceDescriptionProvider>(allServiceProviders);
		}
		allServiceProviders = new HashSet<ServiceDescriptionProvider>(userAddedProviders);
		synchronized (this) {
			if (!hasLoadedProviders) {
				try {
					loadServiceProviders();
				} catch (Exception e) {
					logger.error("Could not load service providers", e);
				} finally {
					hasLoadedProviders = true;
				}
			}
		}
		for (ServiceDescriptionProvider provider : getDefaultServiceDescriptionProviders()) {
			if (userRemovedProviders.contains(provider)) {
				continue;
			}
			if (provider instanceof ConfigurableServiceProvider<?>
					&& !serviceDescriptionsConfig.isIncludeDefaults()) {
				// We'll skip the default configurable service provders
				continue;
			}
			allServiceProviders.add(provider);
		}
		return new HashSet<ServiceDescriptionProvider>(allServiceProviders);
	}

	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders(ServiceDescription sd) {
		Set<ServiceDescriptionProvider> result = new HashSet<ServiceDescriptionProvider>();
		for (ServiceDescriptionProvider sdp : providerDescriptions.keySet()) {
			if (providerDescriptions.get(sdp).contains(sd)) {
				result.add(sdp);
			}
		}
		return result;
	}

	public Set<ServiceDescription> getServiceDescriptions() {
		updateServiceDescriptions(false, true);
		Set<ServiceDescription> serviceDescriptions = new HashSet<ServiceDescription>();
		synchronized (providerDescriptions) {
			for (Set<ServiceDescription> providerDesc : providerDescriptions.values()) {
				serviceDescriptions.addAll(providerDesc);
			}
		}
		return serviceDescriptions;
	}

	@Override
	public ServiceDescription getServiceDescription(URI serviceType) {
		for (ServiceDescription serviceDescription : getServiceDescriptions()) {
			if (serviceDescription.getActivityType().equals(serviceType)) {
				return serviceDescription;
			}
		}
		return null;
	}

	public List<ConfigurableServiceProvider> getUnconfiguredServiceProviders() {
		List<ConfigurableServiceProvider> providers = new ArrayList<ConfigurableServiceProvider>();
		for (ServiceDescriptionProvider provider : serviceDescriptionProviders) {
			if (provider instanceof ConfigurableServiceProvider) {
				ConfigurableServiceProvider confProvider = (ConfigurableServiceProvider) provider;
				providers.add(confProvider);
			}
		}
		return providers;
	}

	public Set<ServiceDescriptionProvider> getUserAddedServiceProviders() {
		return new HashSet<ServiceDescriptionProvider>(userAddedProviders);
	}

	public Set<ServiceDescriptionProvider> getUserRemovedServiceProviders() {
		return new HashSet<ServiceDescriptionProvider>(userRemovedProviders);
	}

	public void loadServiceProviders() throws DeserializationException {
		File serviceProviderFile = findServiceDescriptionsFile();
		if (serviceProviderFile.isFile()) {
			loadServiceProviders(serviceProviderFile);
		}
		hasLoadedProviders = true;
	}

	public void loadServiceProviders(File serviceProvidersFile) throws DeserializationException {
		ServiceDescriptionDeserializer deserializer = new ServiceDescriptionDeserializer(
				serviceDescriptionProviders);
		loading = true;
		deserializer.xmlToServiceRegistry(this, serviceProvidersFile);
		loading = false;
	}

	public void loadServiceProviders(URL serviceProvidersURL) throws DeserializationException {
		ServiceDescriptionDeserializer deserializer = new ServiceDescriptionDeserializer(
				serviceDescriptionProviders);
		loading = true;
		deserializer.xmlToServiceRegistry(this, serviceProvidersURL);
		loading = false;
	}

	public void refresh() {
		updateServiceDescriptions(true, false);
	}

	public void removeObserver(Observer<ServiceDescriptionRegistryEvent> observer) {
		observers.removeObserver(observer);
	}

	public synchronized void removeServiceDescriptionProvider(ServiceDescriptionProvider provider) {
		if (!userAddedProviders.remove(provider)) {
			// Not previously added - must be a default one.. but should we remove it?
			if (loading || serviceDescriptionsConfig.isRemovePermanently()
					&& serviceDescriptionsConfig.isIncludeDefaults()) {
				userRemovedProviders.add(provider);
			}
		}
		if (allServiceProviders.remove(provider)) {
			synchronized (providerDescriptions) {
				FindServiceDescriptionsThread serviceDescriptionsThread = serviceDescriptionThreads
						.remove(provider);
				if (serviceDescriptionsThread != null) {
					serviceDescriptionsThread.interrupt();
				}
				providerDescriptions.remove(provider);
			}
			observers.notify(new RemovedProviderEvent(provider));
		}
		if (!loading) {
			saveServiceDescriptions();
		}
	}

	public void saveServiceDescriptions() {
		File serviceDescriptionsFile = findServiceDescriptionsFile();
		saveServiceDescriptions(serviceDescriptionsFile);
	}

	public void saveServiceDescriptions(File serviceDescriptionsFile) {
		ServiceDescriptionSerializer serializer = new ServiceDescriptionSerializer();
		try {
			serializer.serviceRegistryToXML(this, serviceDescriptionsFile);
		} catch (IOException e) {
			throw new RuntimeException("Can't save service descriptions to "
					+ serviceDescriptionsFile);
		}
	}

	/**
	 * Exports all configurable service providers (that give service descriptions) currently found
	 * in the Service Registry (apart from service templates and local services) regardless of who
	 * added them (user or default system providers).
	 *
	 * Unlike {@link #saveServiceDescriptions}, this export does not have the "ignored providers"
	 * section as this is just a plain export of everything in the Service Registry.
	 *
	 * @param serviceDescriptionsFile
	 */
	public void exportCurrentServiceDescriptions(File serviceDescriptionsFile) {
		ServiceDescriptionSerializer serializer = new ServiceDescriptionSerializer();
		try {
			serializer.exportServiceRegistryToXML(this, serviceDescriptionsFile);
		} catch (IOException e) {
			throw new RuntimeException("Could not save service descriptions to "
					+ serviceDescriptionsFile);
		}
	}

	public void setServiceDescriptionProvidersList(
			List<ServiceDescriptionProvider> serviceDescriptionProviders) {
		this.serviceDescriptionProviders = serviceDescriptionProviders;
	}

	public void updateServiceDescriptions(boolean refreshAll, boolean waitFor) {
		List<FindServiceDescriptionsThread> threads = new ArrayList<FindServiceDescriptionsThread>();
		for (ServiceDescriptionProvider provider : getServiceDescriptionProviders()) {
			synchronized (providerDescriptions) {
				if (providerDescriptions.containsKey(provider) && !refreshAll) {
					// We'll used the cached values
					continue;
				}
				FindServiceDescriptionsThread oldThread = serviceDescriptionThreads.get(provider);
				if (oldThread != null && oldThread.isAlive()) {
					if (refreshAll) {
						// New thread will override the old thread
						oldThread.interrupt();
					} else {
						// observers.notify(new ProviderStatusNotification(
						// provider, "Waiting for provider"));
						continue;
					}
				}
				// Not run yet - we'll start a new tread
				FindDescriptionsCallBack callBack = new FindDescriptionsCallBack(provider);
				FindServiceDescriptionsThread thread = new FindServiceDescriptionsThread(provider,
						callBack);
				threads.add(thread);
				serviceDescriptionThreads.put(provider, thread);
				thread.start();
			}
		}
		if (waitFor) {
			joinThreads(threads, DESCRIPTION_THREAD_TIMEOUT_MS);
		}
	}

	public boolean isDefaultSystemConfigurableProvidersLoaded() {
		return defaultSystemConfigurableProvidersLoaded;
	}

	/**
	 * Sets the serviceDescriptionsConfig.
	 *
	 * @param serviceDescriptionsConfig the new value of serviceDescriptionsConfig
	 */
	public void setServiceDescriptionsConfig(ServiceDescriptionsConfiguration serviceDescriptionsConfig) {
		this.serviceDescriptionsConfig = serviceDescriptionsConfig;
	}

	public class FindDescriptionsCallBack implements FindServiceDescriptionsCallBack {
		private boolean aborting = false;

		private final ServiceDescriptionProvider provider;

		final Set<ServiceDescription> providerDescs = new HashSet<ServiceDescription>();

		public FindDescriptionsCallBack(ServiceDescriptionProvider provider) {
			this.provider = provider;
		}

		public void fail(String message, Throwable ex) {
			logger.warn("Provider " + getProvider() + ": " + message, ex);
			if (aborting) {
				return;
			}
			observers.notify(new ProviderErrorNotification(getProvider(), message, ex));
		}

		public void finished() {
			if (aborting) {
				return;
			}
			synchronized (providerDescriptions) {
				providerDescriptions.put(getProvider(), providerDescs);
			}
			observers.notify(new ServiceDescriptionProvidedEvent(getProvider(), providerDescs));
		}

		public ServiceDescriptionProvider getProvider() {
			return provider;
		}

		public void partialResults(Collection<? extends ServiceDescription> serviceDescriptions) {
			if (aborting) {
				return;
			}
			providerDescs.addAll(serviceDescriptions);
			synchronized (providerDescriptions) {
				providerDescriptions.put(getProvider(), providerDescs);
			}
			observers.notify(new PartialServiceDescriptionsNotification(getProvider(),
					serviceDescriptions));
		}

		public void status(String message) {
			logger.debug("Provider " + getProvider() + ": " + message);
			if (aborting) {
				return;
			}
			observers.notify(new ProviderStatusNotification(getProvider(), message));
		}

		public void warning(String message) {
			logger.warn("Provider " + getProvider() + ": " + message);
			if (aborting) {
				return;
			}
			observers.notify(new ProviderWarningNotification(getProvider(), message));
		}

		/**
		 * Ignore any further callbacks
		 */
		protected void abort() {
			aborting = true;
		}
	}

	public class FindServiceDescriptionsThread extends Thread implements UncaughtExceptionHandler {
		private FindDescriptionsCallBack callBack;
		private final ServiceDescriptionProvider provider;

		public FindServiceDescriptionsThread(ServiceDescriptionProvider provider,
				FindDescriptionsCallBack callBack) {
			super(threadGroup, "Find service descriptions from " + provider);
			this.provider = provider;
			this.callBack = callBack;
			this.setUncaughtExceptionHandler(this);
			this.setDaemon(true);
		}

		public ServiceDescriptionProvider getProvider() {
			return provider;
		}

		@Override
		public void interrupt() {
			callBack.abort();
			super.interrupt();
		}

		@Override
		public void run() {
			observers.notify(new ProviderUpdatingNotification(provider));
			getProvider().findServiceDescriptionsAsync(callBack);
		}

		public void uncaughtException(Thread t, Throwable ex) {
			logger.error("Uncaught exception in " + t, ex);
			callBack.fail("Uncaught exception", ex);
		}
	}

}
