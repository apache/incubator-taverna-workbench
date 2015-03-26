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
package org.apache.taverna.servicedescriptions.impl;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.MIN_PRIORITY;
import static java.lang.Thread.currentThread;

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

import org.apache.taverna.lang.observer.MultiCaster;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionsConfiguration;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.servicedescriptions.events.AddedProviderEvent;
import org.apache.taverna.servicedescriptions.events.PartialServiceDescriptionsNotification;
import org.apache.taverna.servicedescriptions.events.ProviderErrorNotification;
import org.apache.taverna.servicedescriptions.events.ProviderStatusNotification;
import org.apache.taverna.servicedescriptions.events.ProviderUpdatingNotification;
import org.apache.taverna.servicedescriptions.events.ProviderWarningNotification;
import org.apache.taverna.servicedescriptions.events.RemovedProviderEvent;
import org.apache.taverna.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import org.apache.taverna.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import org.apache.taverna.servicedescriptions.impl.ServiceDescriptionDeserializer.DeserializationException;

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
	/**
	 * Total maximum timeout while waiting for description threads to finish
	 */
	private static final long DESCRIPTION_THREAD_TIMEOUT_MS = 3000;
	protected static final String CONF_DIR = "conf";
	protected static final String SERVICE_PROVIDERS_FILENAME = "service_providers.xml";
	private static final String DEFAULT_CONFIGURABLE_SERVICE_PROVIDERS_FILENAME = "default_service_providers.xml";

	private ServiceDescriptionsConfiguration serviceDescriptionsConfig;
	private ApplicationConfiguration applicationConfiguration;
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
	private MultiCaster<ServiceDescriptionRegistryEvent> observers = new MultiCaster<>(this);
	private List<ServiceDescriptionProvider> serviceDescriptionProviders;
	private Set<ServiceDescriptionProvider> allServiceProviders;
	private Map<ServiceDescriptionProvider, Set<ServiceDescription>> providerDescriptions = new HashMap<>();
	private Map<ServiceDescriptionProvider, Thread> serviceDescriptionThreads = new HashMap<>();
	/**
	 * Service providers added by the user, should be saved
	 */
	private Set<ServiceDescriptionProvider> userAddedProviders = new HashSet<>();
	private Set<ServiceDescriptionProvider> userRemovedProviders = new HashSet<>();
	private Set<ServiceDescriptionProvider> defaultServiceDescriptionProviders;
	/**
	 * File containing a list of configured ConfigurableServiceProviders which is used to get the
	 * default set of service descriptions together with those provided by AbstractTemplateServiceS.
	 * This file is located in the conf directory of the Taverna startup directory.
	 */
	private File defaultConfigurableServiceProvidersFile;
	private boolean defaultSystemConfigurableProvidersLoaded = false;

	static {
		threadGroup.setMaxPriority(MIN_PRIORITY);
	}

	public ServiceDescriptionRegistryImpl(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
		defaultConfigurableServiceProvidersFile = new File(
				getTavernaStartupConfigurationDirectory(),
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
		if (!configDirectory.exists())
			configDirectory.mkdir();
		return configDirectory;
	}

	private static void joinThreads(Collection<? extends Thread> threads,
			long descriptionThreadTimeoutMs) {
		long finishJoinBy = currentTimeMillis() + descriptionThreadTimeoutMs;
		for (Thread thread : threads) {
			// No shorter timeout than 1 ms (thread.join(0) waits forever!)
			long timeout = Math.max(1, finishJoinBy - currentTimeMillis());
			try {
				thread.join(timeout);
			} catch (InterruptedException e) {
				currentThread().interrupt();
				return;
			}
			if (thread.isAlive())
				logger.debug("Thread did not finish " + thread);
		}
	}


	@Override
	public void addObserver(Observer<ServiceDescriptionRegistryEvent> observer) {
		observers.addObserver(observer);
	}

	@Override
	public void addServiceDescriptionProvider(ServiceDescriptionProvider provider) {
		synchronized (this) {
			userRemovedProviders.remove(provider);
			if (!getDefaultServiceDescriptionProviders().contains(provider))
				userAddedProviders.add(provider);
			allServiceProviders.add(provider);
		}

		// Spring-like auto-config
		try {
			// BeanUtils should ignore this if provider does not have that property
			BeanUtils.setProperty(provider, SERVICE_DESCRIPTION_REGISTRY, this);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.warn("Could not set serviceDescriptionRegistry on "
					+ provider, e);
		}

		if (!loading)
			saveServiceDescriptions();
		observers.notify(new AddedProviderEvent(provider));
		updateServiceDescriptions(false, false);
	}

	private File findServiceDescriptionsFile() {
		File confDir = new File(
				applicationConfiguration.getApplicationHomeDir(), CONF_DIR);
		confDir.mkdirs();
		if (!confDir.isDirectory())
			throw new RuntimeException("Invalid directory: " + confDir);
		File serviceDescriptionsFile = new File(confDir,
				SERVICE_PROVIDERS_FILENAME);
		return serviceDescriptionsFile;
	}

	@Override
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
	@Override
	public synchronized Set<ServiceDescriptionProvider> getDefaultServiceDescriptionProviders() {
		if (defaultServiceDescriptionProviders != null)
			return defaultServiceDescriptionProviders;
		defaultServiceDescriptionProviders = new HashSet<>();

		/*
		 * Add default configurable service description providers from the
		 * default_service_providers.xml file
		 */
		if (defaultConfigurableServiceProvidersFile.exists()) {
			try {
				ServiceDescriptionDeserializer deserializer = new ServiceDescriptionDeserializer(
						serviceDescriptionProviders);
				defaultServiceDescriptionProviders.addAll(deserializer
						.deserializeDefaults(this,
								defaultConfigurableServiceProvidersFile));
				/*
				 * We have successfully loaded the defaults for system
				 * configurable providers. Note that there are still defaults
				 * for third party configurable providers, which will be loaded
				 * below using getDefaultConfigurations().
				 */
				defaultSystemConfigurableProvidersLoaded = true;
			} catch (Exception e) {
				logger.error("Could not load default service providers from "
						+ defaultConfigurableServiceProvidersFile.getAbsolutePath(), e);

				/*
				 * Fallback on the old hardcoded method of loading default
				 * system configurable service providers using
				 * getDefaultConfigurations().
				 */
				defaultSystemConfigurableProvidersLoaded = false;
			}
		} else {
			logger.warn("Could not find the file "
					+ defaultConfigurableServiceProvidersFile.getAbsolutePath()
					+ " containing default system service providers. "
					+ "Using the hardcoded list of default system providers.");

			/*
			 * Fallback on the old hardcoded method of loading default system
			 * configurable service providers using getDefaultConfigurations().
			 */
			defaultSystemConfigurableProvidersLoaded = false;
		}

		/*
		 * Load other default service description providers - template, local
		 * workers and third party configurable service providers
		 */
		for (ServiceDescriptionProvider provider : serviceDescriptionProviders) {
			/*
			 * Template service providers (beanshell, string constant, etc. )
			 * and providers of local workers.
			 */
			if (!(provider instanceof ConfigurableServiceProvider)) {
				defaultServiceDescriptionProviders.add(provider);
				continue;
			}

			/*
			 * Default system or third party configurable service description
			 * provider. System ones are read from the
			 * default_service_providers.xml file so getDefaultConfigurations()
			 * on them will not have much effect here unless
			 * defaultSystemConfigurableProvidersLoaded is set to false.
			 */
			//FIXME needs to be designed to work using Configuration instances
			//FIXME needs to get configurations via OSGi discovery
			/*
			ConfigurableServiceProvider template = (ConfigurableServiceProvider) provider;
			// Get configurations
			for (ObjectNode config : template.getDefaultConfigurations()) {
				// Make a copy that we can configure
				ConfigurableServiceProvider configurableProvider = template.clone();
				try {
					configurableProvider.configure(config);
				} catch (ConfigurationException e) {
					logger.warn("Can't configure provider "
							+ configurableProvider + " with " + config);
					continue;
				}
				defaultServiceDescriptionProviders.add(configurableProvider);
			}
			*/
		}

		return defaultServiceDescriptionProviders;
	}

	@Override
	public synchronized Set<ServiceDescriptionProvider> getServiceDescriptionProviders() {
		if (allServiceProviders != null)
			return new HashSet<>(allServiceProviders);
		allServiceProviders = new HashSet<>(userAddedProviders);
		synchronized (this) {
			if (!hasLoadedProviders)
				try {
					loadServiceProviders();
				} catch (Exception e) {
					logger.error("Could not load service providers", e);
				} finally {
					hasLoadedProviders = true;
				}
		}
		for (ServiceDescriptionProvider provider : getDefaultServiceDescriptionProviders()) {
			if (userRemovedProviders.contains(provider))
				continue;
			if (provider instanceof ConfigurableServiceProvider
					&& !serviceDescriptionsConfig.isIncludeDefaults())
				// We'll skip the default configurable service provders
				continue;
			allServiceProviders.add(provider);
		}
		return new HashSet<>(allServiceProviders);
	}

	@Override
	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders(
			ServiceDescription sd) {
		Set<ServiceDescriptionProvider> result = new HashSet<>();
		for (ServiceDescriptionProvider sdp : providerDescriptions.keySet())
			if (providerDescriptions.get(sdp).contains(sd))
				result.add(sdp);
		return result;
	}

	@Override
	public Set<ServiceDescription> getServiceDescriptions() {
		updateServiceDescriptions(false, true);
		Set<ServiceDescription> serviceDescriptions = new HashSet<>();
		synchronized (providerDescriptions) {
			for (Set<ServiceDescription> providerDesc : providerDescriptions
					.values())
				serviceDescriptions.addAll(providerDesc);
		}
		return serviceDescriptions;
	}

	@Override
	public ServiceDescription getServiceDescription(URI serviceType) {
		for (ServiceDescription serviceDescription : getServiceDescriptions())
			if (serviceDescription.getActivityType().equals(serviceType))
				return serviceDescription;
		return null;
	}

	@Override
	public List<ConfigurableServiceProvider> getUnconfiguredServiceProviders() {
		List<ConfigurableServiceProvider> providers = new ArrayList<>();
		for (ServiceDescriptionProvider provider : serviceDescriptionProviders)
			if (provider instanceof ConfigurableServiceProvider)
				providers.add((ConfigurableServiceProvider) provider);
		return providers;
	}

	@Override
	public Set<ServiceDescriptionProvider> getUserAddedServiceProviders() {
		return new HashSet<>(userAddedProviders);
	}

	@Override
	public Set<ServiceDescriptionProvider> getUserRemovedServiceProviders() {
		return new HashSet<>(userRemovedProviders);
	}

	@Override
	public void loadServiceProviders() {
		File serviceProviderFile = findServiceDescriptionsFile();
		if (serviceProviderFile.isFile())
			loadServiceProviders(serviceProviderFile);
		hasLoadedProviders = true;
	}

	@Override
	public void loadServiceProviders(File serviceProvidersFile) {
		ServiceDescriptionDeserializer deserializer = new ServiceDescriptionDeserializer(
				serviceDescriptionProviders);
		loading = true;
		try {
			deserializer.deserialize(this, serviceProvidersFile);
		} catch (DeserializationException e) {
			logger.error("failed to deserialize configuration", e);
		}
		loading = false;
	}

	@Override
	public void loadServiceProviders(URL serviceProvidersURL) {
		ServiceDescriptionDeserializer deserializer = new ServiceDescriptionDeserializer(
				serviceDescriptionProviders);
		loading = true;
		try {
			deserializer.deserialize(this, serviceProvidersURL);
		} catch (DeserializationException e) {
			logger.error("failed to deserialize configuration", e);
		}
		loading = false;
	}

	@Override
	public void refresh() {
		updateServiceDescriptions(true, false);
	}

	@Override
	public void removeObserver(Observer<ServiceDescriptionRegistryEvent> observer) {
		observers.removeObserver(observer);
	}

	@Override
	public synchronized void removeServiceDescriptionProvider(
			ServiceDescriptionProvider provider) {
		if (!userAddedProviders.remove(provider))
			// Not previously added - must be a default one.. but should we remove it?
			if (loading || serviceDescriptionsConfig.isRemovePermanently()
					&& serviceDescriptionsConfig.isIncludeDefaults())
				userRemovedProviders.add(provider);
		if (allServiceProviders.remove(provider)) {
			synchronized (providerDescriptions) {
				Thread thread = serviceDescriptionThreads.remove(provider);
				if (thread != null)
					thread.interrupt();
				providerDescriptions.remove(provider);
			}
			observers.notify(new RemovedProviderEvent(provider));
		}
		if (!loading)
			saveServiceDescriptions();
	}

	@Override
	public void saveServiceDescriptions() {
		File serviceDescriptionsFile = findServiceDescriptionsFile();
		saveServiceDescriptions(serviceDescriptionsFile);
	}

	@Override
	public void saveServiceDescriptions(File serviceDescriptionsFile) {
		ServiceDescriptionSerializer serializer = new ServiceDescriptionSerializer();
		try {
			serializer.serializeRegistry(this, serviceDescriptionsFile);
		} catch (IOException e) {
			throw new RuntimeException("Can't save service descriptions to "
					+ serviceDescriptionsFile);
		}
	}

	/**
	 * Exports all configurable service providers (that give service
	 * descriptions) currently found in the Service Registry (apart from service
	 * templates and local services) regardless of who added them (user or
	 * default system providers).
	 * <p>
	 * Unlike {@link #saveServiceDescriptions}, this export does not have the
	 * "ignored providers" section as this is just a plain export of everything
	 * in the Service Registry.
	 * 
	 * @param serviceDescriptionsFile
	 */
	@Override
	public void exportCurrentServiceDescriptions(File serviceDescriptionsFile) {
		ServiceDescriptionSerializer serializer = new ServiceDescriptionSerializer();
		try {
			serializer.serializeFullRegistry(this, serviceDescriptionsFile);
		} catch (IOException e) {
			throw new RuntimeException("Could not save service descriptions to "
					+ serviceDescriptionsFile);
		}
	}

	public void setServiceDescriptionProvidersList(
			List<ServiceDescriptionProvider> serviceDescriptionProviders) {
		this.serviceDescriptionProviders = serviceDescriptionProviders;
	}

	private void updateServiceDescriptions(boolean refreshAll, boolean waitFor) {
		List<Thread> threads = new ArrayList<>();
		for (ServiceDescriptionProvider provider : getServiceDescriptionProviders()) {
			synchronized (providerDescriptions) {
				if (providerDescriptions.containsKey(provider) && !refreshAll)
					// We'll used the cached values
					continue;
				Thread oldThread = serviceDescriptionThreads.get(provider);
				if (oldThread != null && oldThread.isAlive()) {
					if (refreshAll)
						// New thread will override the old thread
						oldThread.interrupt();
					else {
						// observers.notify(new ProviderStatusNotification(provider, "Waiting for provider"));
						continue;
					}
				}
				// Not run yet - we'll start a new tread
				Thread thread = new FindServiceDescriptionsThread(provider);
				threads.add(thread);
				serviceDescriptionThreads.put(provider, thread);
				thread.start();
			}
		}
		if (waitFor)
			joinThreads(threads, DESCRIPTION_THREAD_TIMEOUT_MS);
	}

	@Override
	public boolean isDefaultSystemConfigurableProvidersLoaded() {
		return defaultSystemConfigurableProvidersLoaded;
	}

	/**
	 * Sets the serviceDescriptionsConfig.
	 * 
	 * @param serviceDescriptionsConfig
	 *            the new value of serviceDescriptionsConfig
	 */
	public void setServiceDescriptionsConfig(
			ServiceDescriptionsConfiguration serviceDescriptionsConfig) {
		this.serviceDescriptionsConfig = serviceDescriptionsConfig;
	}

	class FindServiceDescriptionsThread extends Thread implements
			UncaughtExceptionHandler, FindServiceDescriptionsCallBack {
		private final ServiceDescriptionProvider provider;
		private boolean aborting = false;
		private final Set<ServiceDescription> providerDescs = new HashSet<>();

		FindServiceDescriptionsThread(ServiceDescriptionProvider provider) {
			super(threadGroup, "Find service descriptions from " + provider);
			this.provider = provider;
			setUncaughtExceptionHandler(this);
			setDaemon(true);
		}

		@Override
		public void fail(String message, Throwable ex) {
			logger.warn("Provider " + getProvider() + ": " + message, ex);
			if (aborting)
				return;
			observers.notify(new ProviderErrorNotification(getProvider(),
					message, ex));
		}

		@Override
		public void finished() {
			if (aborting)
				return;
			synchronized (providerDescriptions) {
				providerDescriptions.put(getProvider(), providerDescs);
			}
			observers.notify(new ServiceDescriptionProvidedEvent(getProvider(),
					providerDescs));
		}

		@Override
		public void partialResults(
				Collection<? extends ServiceDescription> serviceDescriptions) {
			if (aborting)
				return;
			providerDescs.addAll(serviceDescriptions);
			synchronized (providerDescriptions) {
				providerDescriptions.put(getProvider(), providerDescs);
			}
			observers.notify(new PartialServiceDescriptionsNotification(
					getProvider(), serviceDescriptions));
		}

		@Override
		public void status(String message) {
			logger.debug("Provider " + getProvider() + ": " + message);
			if (aborting)
				return;
			observers.notify(new ProviderStatusNotification(getProvider(),
					message));
		}

		@Override
		public void warning(String message) {
			logger.warn("Provider " + getProvider() + ": " + message);
			if (aborting)
				return;
			observers.notify(new ProviderWarningNotification(getProvider(),
					message));
		}

		public ServiceDescriptionProvider getProvider() {
			return provider;
		}

		@Override
		public void interrupt() {
			aborting = true;
			super.interrupt();
		}

		@Override
		public void run() {
			observers.notify(new ProviderUpdatingNotification(provider));
			getProvider().findServiceDescriptionsAsync(this);
		}

		@Override
		public void uncaughtException(Thread t, Throwable ex) {
			logger.error("Uncaught exception in " + t, ex);
			fail("Uncaught exception", ex);
		}
	}
}
