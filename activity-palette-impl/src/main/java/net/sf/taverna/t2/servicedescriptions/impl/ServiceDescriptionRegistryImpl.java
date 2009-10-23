package net.sf.taverna.t2.servicedescriptions.impl;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider.FindServiceDescriptionsCallBack;
import net.sf.taverna.t2.servicedescriptions.events.AddedProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.PartialServiceDescriptionsNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderErrorNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderStatusNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderUpdatingNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderWarningNotification;
import net.sf.taverna.t2.servicedescriptions.events.RemovedProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

public class ServiceDescriptionRegistryImpl implements
		ServiceDescriptionRegistry {

	/**
	 * If a writable property of this name on a provider exists (ie. the
	 * provider has a method
	 * setServiceDescriptionRegistry(ServiceDescriptionRegistry registry) - then
	 * this property will be set to the current registry.
	 */
	public static final String SERVICE_DESCRIPTION_REGISTRY = "serviceDescriptionRegistry";

	public static Logger logger = Logger
			.getLogger(ServiceDescriptionRegistryImpl.class);

	public static final ThreadGroup threadGroup = new ThreadGroup(
			"Service description providers");

	private ServiceDescriptionsConfig serviceDescriptionsConfig = ServiceDescriptionsConfig
			.getInstance();

	/**
	 * Total maximum timeout while waiting for description threads to finish
	 */
	private static final long DESCRIPTION_THREAD_TIMEOUT_MS = 3000;

	protected static final String CONF_DIR = "conf";

	protected static final String PROVIDERS_FILENAME = "service_providers.xml";

	static {
		threadGroup.setMaxPriority(Thread.MIN_PRIORITY);
	}

	public static ServiceDescriptionRegistryImpl getInstance() {
		return Singleton.instance;
	}

	public static void joinThreads(Collection<? extends Thread> threads,
			long descriptionThreadTimeoutMs) {
		long finishJoinBy = System.currentTimeMillis()
				+ descriptionThreadTimeoutMs;
		for (Thread thread : threads) {
			// No shorter timeout than 1 ms (thread.join(0) waits forever!)
			long timeout = Math.max(1, finishJoinBy
					- System.currentTimeMillis());
			try {
				thread.join(timeout);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			if (thread.isAlive()) {
				logger.warn("Thread did not finish " + thread);
			}
		}
	}

	/**
	 * <code>false</code> until first call to {@link #loadServiceProviders()} -
	 * which is done by first call to {@link #getServiceDescriptionProviders()}.
	 */
	private boolean hasLoadedProviders = false;

	/**
	 * <code>true</code> while {@link #loadServiceProviders(File)} or
	 * {@link #loadServiceProviders()} is in progress, avoids triggering
	 * {@link #saveServiceDescriptions()} on
	 * {@link #addServiceDescriptionProvider(ServiceDescriptionProvider)} calls.
	 */
	private boolean loading = false;

	protected Set<ServiceDescriptionProvider> allServiceProviders;

	protected MultiCaster<ServiceDescriptionRegistryEvent> observers = new MultiCaster<ServiceDescriptionRegistryEvent>(
			this);

	@SuppressWarnings("unchecked")
	protected Map<ServiceDescriptionProvider, Set<ServiceDescription>> providerDescriptions = new HashMap<ServiceDescriptionProvider, Set<ServiceDescription>>();

	protected SPIRegistry<ServiceDescriptionProvider> providerRegistry = new SPIRegistry<ServiceDescriptionProvider>(
			ServiceDescriptionProvider.class);

	protected Map<ServiceDescriptionProvider, FindServiceDescriptionsThread> serviceDescriptionThreads = new HashMap<ServiceDescriptionProvider, FindServiceDescriptionsThread>();

	/**
	 * Service providers added by the user, should be saved
	 */
	protected Set<ServiceDescriptionProvider> userAddedProviders = new HashSet<ServiceDescriptionProvider>();

	protected Set<ServiceDescriptionProvider> userRemovedProviders = new HashSet<ServiceDescriptionProvider>();

	private Set<ServiceDescriptionProvider> defaultServiceDescriptionProviders;

	public void addObserver(Observer<ServiceDescriptionRegistryEvent> observer) {
		observers.addObserver(observer);
	}

	public void addServiceDescriptionProvider(
			ServiceDescriptionProvider provider) {
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
		File confDir = new File(ApplicationRuntime.getInstance()
				.getApplicationHomeDir(), CONF_DIR);
		confDir.mkdirs();
		if (!confDir.isDirectory()) {
			throw new RuntimeException("Invalid directory: " + confDir);
		}
		File serviceDescriptionsFile = new File(confDir, PROVIDERS_FILENAME);
		return serviceDescriptionsFile;
	}

	public List<Observer<ServiceDescriptionRegistryEvent>> getObservers() {
		return observers.getObservers();
	}

	public SPIRegistry<ServiceDescriptionProvider> getProviderRegistry() {
		return providerRegistry;
	}

	@SuppressWarnings("unchecked")
	public synchronized Set<ServiceDescriptionProvider> getDefaultServiceDescriptionProviders() {
		if (defaultServiceDescriptionProviders != null) {
			return defaultServiceDescriptionProviders;
		}
		defaultServiceDescriptionProviders = new HashSet<ServiceDescriptionProvider>();
		for (ServiceDescriptionProvider provider : getProviderRegistry()
				.getInstances()) {
			if (!(provider instanceof ConfigurableServiceProvider)) {
				defaultServiceDescriptionProviders.add(provider);
				continue;
			}
			// It's a template, we'll need to configure it first
			ConfigurableServiceProvider template = ((ConfigurableServiceProvider) provider);
			List<Object> configurables = template.getDefaultConfigurations();
			for (Object config : configurables) {
				// Make a copy that we can configure
				ConfigurableServiceProvider configurableProvider = template
						.clone();
				try {
					configurableProvider.configure(config);
				} catch (ConfigurationException e) {
					logger.warn("Can't configure provider "
							+ configurableProvider + " with " + config);
					continue;
				}
				defaultServiceDescriptionProviders.add(configurableProvider);
			}
		}
		return defaultServiceDescriptionProviders;
	}

	public synchronized Set<ServiceDescriptionProvider> getServiceDescriptionProviders() {
		if (allServiceProviders != null) {
			return allServiceProviders;
		}
		allServiceProviders = new HashSet<ServiceDescriptionProvider>(
				userAddedProviders);
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
			if (provider instanceof ConfigurableServiceProvider
					&& !serviceDescriptionsConfig.isIncludeDefaults()) {
				// We'll skip the default configurables
				continue;
			}
			allServiceProviders.add(provider);
		}
		return allServiceProviders;
	}

	@SuppressWarnings("unchecked")
	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders(
			ServiceDescription sd) {
		Set<ServiceDescriptionProvider> result = new HashSet<ServiceDescriptionProvider>();
		for (ServiceDescriptionProvider sdp : providerDescriptions.keySet()) {
			if (providerDescriptions.get(sdp).contains(sd)) {
				result.add(sdp);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public Set<ServiceDescription> getServiceDescriptions() {
		updateServiceDescriptions(false, true);
		Set<ServiceDescription> serviceDescriptions = new HashSet<ServiceDescription>();
		synchronized (providerDescriptions) {
			for (Set<ServiceDescription> providerDesc : providerDescriptions
					.values()) {
				serviceDescriptions.addAll(providerDesc);
			}
		}
		return serviceDescriptions;
	}

	@SuppressWarnings("unchecked")
	public List<ConfigurableServiceProvider> getUnconfiguredServiceProviders() {
		List<ConfigurableServiceProvider> providers = new ArrayList<ConfigurableServiceProvider>();
		List<ServiceDescriptionProvider> possibleProviders = new ArrayList<ServiceDescriptionProvider>(
				getProviderRegistry().getInstances());
		for (ServiceDescriptionProvider provider : possibleProviders) {
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

	public void loadServiceProviders(File serviceProviderFile)
			throws DeserializationException {
		ServiceDescriptionDeserializer deserializer = new ServiceDescriptionDeserializer();
		loading = true;
		deserializer.xmlToServiceRegistry(this, serviceProviderFile);
		loading = false;
	}

	public void refresh() {
		updateServiceDescriptions(true, false);
	}

	public void removeObserver(
			Observer<ServiceDescriptionRegistryEvent> observer) {
		observers.removeObserver(observer);
	}

	public void removeServiceDescriptionProvider(
			ServiceDescriptionProvider provider) {
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

	public void setProviderRegistry(
			SPIRegistry<ServiceDescriptionProvider> providerRegistry) {
		this.providerRegistry = providerRegistry;
	}

	public void updateServiceDescriptions(boolean refreshAll, boolean waitFor) {
		List<FindServiceDescriptionsThread> threads = new ArrayList<FindServiceDescriptionsThread>();
		for (ServiceDescriptionProvider provider : getServiceDescriptionProviders()) {
			synchronized (providerDescriptions) {
				if (providerDescriptions.containsKey(provider) && !refreshAll) {
					// We'll used the cached values
					continue;
				}
				FindServiceDescriptionsThread oldThread = serviceDescriptionThreads
						.get(provider);
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
				FindDescriptionsCallBack callBack = new FindDescriptionsCallBack(
						provider);
				FindServiceDescriptionsThread thread = new FindServiceDescriptionsThread(
						provider, callBack);
				threads.add(thread);
				serviceDescriptionThreads.put(provider, thread);
				thread.start();
			}
		}
		if (waitFor) {
			joinThreads(threads, DESCRIPTION_THREAD_TIMEOUT_MS);
		}
	}

	public class FindDescriptionsCallBack implements
			FindServiceDescriptionsCallBack {
		private boolean aborting = false;

		private final ServiceDescriptionProvider provider;

		@SuppressWarnings("unchecked")
		final Set<ServiceDescription> providerDescs = new HashSet<ServiceDescription>();

		public FindDescriptionsCallBack(ServiceDescriptionProvider provider) {
			this.provider = provider;
		}

		public void fail(String message, Throwable ex) {
			logger.warn("Provider " + getProvider() + ": " + message, ex);
			if (aborting) {
				return;
			}
			observers.notify(new ProviderErrorNotification(getProvider(),
					message, ex));
		}

		public void finished() {
			if (aborting) {
				return;
			}
			synchronized (providerDescriptions) {
				providerDescriptions.put(getProvider(), providerDescs);
			}
			observers.notify(new ServiceDescriptionProvidedEvent(getProvider(),
					providerDescs));
		}

		public ServiceDescriptionProvider getProvider() {
			return provider;
		}

		@SuppressWarnings("unchecked")
		public void partialResults(
				Collection<? extends ServiceDescription> serviceDescriptions) {
			if (aborting) {
				return;
			}
			providerDescs.addAll(serviceDescriptions);
			synchronized (providerDescriptions) {
				providerDescriptions.put(getProvider(), providerDescs);
			}
			observers.notify(new PartialServiceDescriptionsNotification(
					getProvider(), serviceDescriptions));
		}

		public void status(String message) {
			logger.debug("Provider " + getProvider() + ": " + message);
			if (aborting) {
				return;
			}
			observers.notify(new ProviderStatusNotification(getProvider(),
					message));
		}

		public void warning(String message) {
			logger.warn("Provider " + getProvider() + ": " + message);
			if (aborting) {
				return;
			}
			observers.notify(new ProviderWarningNotification(getProvider(),
					message));
		}

		/**
		 * Ignore any further callbacks
		 */
		protected void abort() {
			aborting = true;
		}
	}

	public class FindServiceDescriptionsThread extends Thread implements
			UncaughtExceptionHandler {
		private FindDescriptionsCallBack callBack;
		private final ServiceDescriptionProvider provider;

		public FindServiceDescriptionsThread(
				ServiceDescriptionProvider provider,
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

	private static class Singleton {
		private static final ServiceDescriptionRegistryImpl instance = new ServiceDescriptionRegistryImpl();
	}
}
