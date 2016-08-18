package org.apache.taverna.workbench.ui.impl;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.taverna.configuration.ConfigurationManager;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import org.apache.taverna.configuration.impl.ConfigurationManagerImpl;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.platform.capability.activity.impl.ActivityServiceImpl;
import org.apache.taverna.platform.capability.api.ActivityService;
import org.apache.taverna.platform.capability.api.DispatchLayerService;
import org.apache.taverna.platform.capability.dispatch.impl.DispatchLayerServiceImpl;
import org.apache.taverna.platform.execution.api.ExecutionEnvironmentService;
import org.apache.taverna.platform.execution.api.ExecutionService;
import org.apache.taverna.platform.execution.impl.ExecutionEnvironmentServiceImpl;
import org.apache.taverna.platform.execution.impl.local.LocalExecutionService;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.platform.run.impl.RunServiceImpl;
import org.apache.taverna.plugin.Plugin;
import org.apache.taverna.plugin.PluginException;
import org.apache.taverna.plugin.PluginManager;
import org.apache.taverna.plugin.xml.jaxb.PluginVersions;
import org.apache.taverna.reference.ExternalReferenceBuilderSPI;
import org.apache.taverna.reference.ExternalReferenceTranslatorSPI;
import org.apache.taverna.reference.ListService;
import org.apache.taverna.reference.ReferenceService;
import org.apache.taverna.reference.StreamToValueConverterSPI;
import org.apache.taverna.reference.ValueToReferenceConverterSPI;
import org.apache.taverna.reference.impl.ErrorDocumentServiceImpl;
import org.apache.taverna.reference.impl.InMemoryErrorDocumentDao;
import org.apache.taverna.reference.impl.InMemoryListDao;
import org.apache.taverna.reference.impl.InMemoryReferenceSetDao;
import org.apache.taverna.reference.impl.ListServiceImpl;
import org.apache.taverna.reference.impl.ReferenceServiceImpl;
import org.apache.taverna.reference.impl.ReferenceSetAugmentorImpl;
import org.apache.taverna.reference.impl.ReferenceSetServiceImpl;
import org.apache.taverna.reference.impl.SimpleT2ReferenceGenerator;
import org.apache.taverna.reference.impl.external.object.InlineByteArrayReferenceBuilder;
import org.apache.taverna.reference.impl.external.object.InlineByteToInlineStringTranslator;
import org.apache.taverna.reference.impl.external.object.InlineStringReferenceBuilder;
import org.apache.taverna.reference.impl.external.object.InlineStringToInlineByteTranslator;
import org.apache.taverna.renderers.Renderer;
import org.apache.taverna.renderers.RendererRegistry;
import org.apache.taverna.renderers.impl.RendererRegistryImpl;
import org.apache.taverna.scufl2.api.common.WorkflowBean;
import org.apache.taverna.scufl2.api.profiles.Profile;
import org.apache.taverna.scufl2.validation.Status;
import org.apache.taverna.scufl2.validation.WorkflowBeanReport;
import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.impl.CredentialManagerImpl;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import org.apache.taverna.services.ServiceRegistry;
import org.apache.taverna.services.impl.ServiceRegistryImpl;
import org.apache.taverna.ui.menu.DefaultMenuBar;
import org.apache.taverna.ui.menu.DefaultToolBar;
import org.apache.taverna.ui.menu.MenuComponent;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.ui.menu.impl.MenuManagerImpl;
import org.apache.taverna.ui.perspectives.design.DesignPerspective;
import org.apache.taverna.ui.perspectives.results.ResultsPerspective;
import org.apache.taverna.workbench.ShutdownSPI;
import org.apache.taverna.workbench.StartupSPI;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.activityicons.ActivityIconSPI;
import org.apache.taverna.workbench.activityicons.impl.ActivityIconManagerImpl;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.impl.EditManagerImpl;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.impl.FileManagerImpl;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.report.ReportManagerEvent;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.impl.SelectionManagerImpl;
import org.apache.taverna.workbench.ui.credentialmanager.startup.InitialiseSSLStartupHook;
import org.apache.taverna.workbench.ui.credentialmanager.startup.SetCredManAuthenticatorStartupHook;
import org.apache.taverna.workbench.ui.impl.configuration.colour.ColourManagerImpl;
import org.apache.taverna.workbench.ui.servicepanel.ServicePanelComponentFactory;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.impl.ContextualViewFactoryRegistryImpl;
import org.apache.taverna.workbench.ui.views.contextualviews.impl.ContextualViewComponentFactory;
import org.apache.taverna.workbench.ui.workflowexplorer.WorkflowExplorerFactory;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;
import org.apache.taverna.workbench.views.graph.GraphViewComponentFactory;
import org.apache.taverna.workbench.views.graph.config.GraphViewConfiguration;
import org.apache.taverna.workbench.views.results.saveactions.SaveAllResultsSPI;
import org.apache.taverna.workbench.views.results.saveactions.SaveIndividualResultSPI;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.processor.activity.ActivityFactory;
import org.apache.taverna.workflowmodel.processor.dispatch.DispatchLayerFactory;
import org.apache.taverna.workflowmodel.processor.dispatch.layers.CoreDispatchLayerFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class WorkbenchTest {

	public static void main(String[] args) throws InterruptedException {
		System.setProperty("taverna.app.startup", Paths.get("target").toAbsolutePath().toString());
		
		WorkbenchImpl w = new WorkbenchTest().getWorkbench();
		w.initialize();
		synchronized(w) {
			// It won't actually get interrupted, but it means we persist 
			// as long as w rather than exiting
			w.wait();
		}
	}

	private EditManagerImpl editManagerImpl;
	private FileManagerImpl fileManager;
	private WorkbenchImpl workbench;
	private MenuManagerImpl menuManager;
	private SelectionManagerImpl selectionManager;
	private List<MenuComponent> menuComponents;
	private CredentialManagerImpl credentialManager;
	private ContextualViewFactoryRegistryImpl registry;
	private ServicePanelComponentFactory servicePanelFactory;
	private WorkflowExplorerFactory workflowExplorerFactory;
	private ServiceDescriptionRegistryImpl serviceDescriptionRegistry;
	private ConfigurationManagerImpl configurationManager;

	public FileManager getFileManager() {
		if (fileManager == null) {
			fileManager = new FileManagerImpl(getEditManager());
		}
		return fileManager;
	}

	public EditManager getEditManager() {
		if (editManagerImpl == null) {
			editManagerImpl = new EditManagerImpl();
		}
		return editManagerImpl;
	}

	public MenuManager getMenuManager() {
		if (menuManager == null) {
			menuManager = new MenuManagerImpl();
			menuManager.setSelectionManager(getSelectionManager());

			menuManager.setMenuComponents(getMenuComponents());

		}
		return menuManager;
	}

	public List<MenuComponent> getMenuComponents() {
		if (menuComponents == null) {
			// This does not work as many of the actions are set in  constructor
			// rather than with setters
//			menuComponents = serviceLoader(MenuComponent.class);
			
			// Instead, we'll make an empty menu
			menuComponents = new ArrayList<>();
			menuComponents.add(new DefaultMenuBar());
			menuComponents.add(new DefaultToolBar());			
		}
		return menuComponents;
	}

	public SelectionManager getSelectionManager() {
		if (selectionManager == null) {
			selectionManager = new SelectionManagerImpl();
			selectionManager.setEditManager(getEditManager());
			selectionManager.setFileManager(getFileManager());
			selectionManager.setPerspectives(getPerspectives());
		}
		return selectionManager;
	}

	public ResultsPerspective getResultsPerspective() {
		ResultsPerspective perspective = new ResultsPerspective();
		perspective.setActivityIconManager(getActivityIconManager());
		perspective.setApplicationConfiguration(getApplicationConfiguration());
		perspective.setColourManager(getColourManager());
		perspective.setRendererRegistry(getRendererRegistry());
		perspective.setRunService(getRunService());
		perspective.setSaveAllResultsSPIs(serviceLoader(SaveAllResultsSPI.class));
		perspective.setSaveIndividualResultSPIs(serviceLoader(SaveIndividualResultSPI.class));
		perspective.setSelectionManager(getSelectionManager());
		perspective.setWorkbenchConfiguration(getWorkbenchConfiguration());
		return perspective;
	}


	public RunService getRunService() {
		RunServiceImpl runServiceImpl = new RunServiceImpl();
		runServiceImpl.setEventAdmin(getEventAdmin());
		runServiceImpl.setExecutionEnvironmentService(getExecutionEnvironmentService());
		return runServiceImpl;
	}

	private ExecutionEnvironmentService getExecutionEnvironmentService() {
		ExecutionEnvironmentServiceImpl serviceImpl = new ExecutionEnvironmentServiceImpl();
		Set<ExecutionService> executionServices = Collections.singleton(getLocalExecutionService());
		serviceImpl.setExecutionServices(executionServices);
		return serviceImpl;
	}

	private ExecutionService getLocalExecutionService() {
		LocalExecutionService localExecutionService = new LocalExecutionService();
		localExecutionService.setActivityService(getActivityService());
		localExecutionService.setDispatchLayerService(getDispatchLayerService());
		localExecutionService.setEdits(getEdits());
		localExecutionService.setReferenceService(getReferenceService());
		return localExecutionService;
	}

	private Edits getEdits() {
		// TODO Auto-generated method stub
		return null;
	}

	private ReferenceService getReferenceService() {
		// Adapted from org.apache.taverna.activities.testutils.ActivityInvoker
		
		ReferenceServiceImpl referenceServiceImpl = new ReferenceServiceImpl();		
		ReferenceSetServiceImpl referenceSetService = new ReferenceSetServiceImpl();
		ReferenceServiceImpl referenceService = new ReferenceServiceImpl();
		
		SimpleT2ReferenceGenerator referenceGenerator = new SimpleT2ReferenceGenerator();
		ReferenceSetAugmentorImpl referenceSetAugmentor = new ReferenceSetAugmentorImpl();
		referenceSetAugmentor.setBuilders(getBuilders());
		referenceSetAugmentor.setTranslators(getTranslators());
		referenceSetService.setReferenceSetAugmentor(referenceSetAugmentor);
		
		referenceSetService.setT2ReferenceGenerator(referenceGenerator);
		referenceSetService.setReferenceSetDao(new InMemoryReferenceSetDao());
		referenceService.setReferenceSetService(referenceSetService);
		
		ListServiceImpl listService = new ListServiceImpl();
		listService.setT2ReferenceGenerator(referenceGenerator);		
		listService.setListDao(new InMemoryListDao());
		referenceService.setListService(listService);
		
		ErrorDocumentServiceImpl errorDocumentService = new ErrorDocumentServiceImpl();
		errorDocumentService.setT2ReferenceGenerator(referenceGenerator);
		errorDocumentService.setErrorDao(new InMemoryErrorDocumentDao());
		
		referenceService.setErrorDocumentService(errorDocumentService);
		referenceService.setConverters(serviceLoader(ValueToReferenceConverterSPI.class));
		referenceService.setValueBuilders(serviceLoader(StreamToValueConverterSPI.class));		
		return referenceService;		
		
	}

	@SuppressWarnings("unchecked")
	private List<ExternalReferenceBuilderSPI<?>> getBuilders() {		
		List<ExternalReferenceBuilderSPI<?>> builders = new ArrayList<>();
		builders.add(new InlineByteArrayReferenceBuilder());
		builders.add(new InlineStringReferenceBuilder());
		return builders;
	}

	private List<ExternalReferenceTranslatorSPI<?, ?>> getTranslators() {
		List<ExternalReferenceTranslatorSPI<?, ?>> translators = new ArrayList<>();
		translators.add(new InlineByteToInlineStringTranslator());
		translators.add(new InlineStringToInlineByteTranslator());
		return translators;
	}

	private DispatchLayerService getDispatchLayerService() {
		DispatchLayerServiceImpl dispatchLayerServiceImpl = new DispatchLayerServiceImpl();		
		List<DispatchLayerFactory> list = new ArrayList<>();
		list.add(new CoreDispatchLayerFactory());
		dispatchLayerServiceImpl.setDispatchLayerFactories(list);
		return dispatchLayerServiceImpl;
	}

	private ActivityService getActivityService() {
		ActivityServiceImpl serviceImpl = new ActivityServiceImpl();		
		serviceImpl.setActivityFactories(serviceLoader(ActivityFactory.class));
		return serviceImpl;
	}

	private EventAdmin getEventAdmin() {
		// We're outside osgi, so we'll have to make a fake one
		return new EventAdmin() {
			@Override
			public void postEvent(Event event) {
				System.out.println("Posted event: "  + event.getTopic());
			}
			@Override
			public void sendEvent(Event event) {
				System.out.println("Sent event: "  + event.getTopic());
			}};
	}

	public RendererRegistry getRendererRegistry() {
		RendererRegistryImpl rendererRegistryImpl = new RendererRegistryImpl();
		rendererRegistryImpl.setRenderers(serviceLoader(Renderer.class));
		return rendererRegistryImpl;
	}

	public DesignPerspective getDesignPerspective() {
		DesignPerspective p = new DesignPerspective();
		p.setEditManager(getEditManager());
		p.setFileManager(getFileManager());
		p.setMenuManager(getMenuManager());
		p.setSelectionManager(getSelectionManager());

		p.setServicePanelComponentFactory(getServicePanelComponentFactory());
		p.setWorkflowExplorerFactory(getWorkflowExplorerFactory());
		p.setReportViewComponentFactory(getReportViewComponentFactory());
		p.setContextualViewComponentFactory(getContextualViewComponentFactory());
		p.setGraphViewComponentFactory(getGraphViewComponentFactory());
		return p;
	}

	private UIComponentFactorySPI getGraphViewComponentFactory() {
		GraphViewComponentFactory f = new GraphViewComponentFactory();
		f.setColourManager(getColourManager());
		f.setEditManager(getEditManager());
		f.setFileManager(getFileManager());
		f.setGraphViewConfiguration(getGraphViewConfiguration());
		f.setMenuManager(getMenuManager());
		f.setSelectionManager(getSelectionManager());
		f.setServiceRegistry(getServiceRegistry());
		f.setWorkbenchConfiguration(getWorkbenchConfiguration());
		return f;
	}

	public GraphViewConfiguration getGraphViewConfiguration() {
		return new GraphViewConfiguration(getConfigurationManager());
	}

	public ColourManager getColourManager() {
		return new ColourManagerImpl(getConfigurationManager());
	}

	public ConfigurationManager getConfigurationManager() {
		if (configurationManager == null) {
			configurationManager = new ConfigurationManagerImpl(getApplicationConfiguration());			
		}
		return configurationManager;
	}

	public UIComponentFactorySPI getReportViewComponentFactory() {
		// The report view is broken, so we'll return a dummy instead.
		return dummyUiComponentFactory("Report view not implemented");
	}

	private UIComponentFactorySPI dummyUiComponentFactory(final String message) {
		return new UIComponentFactorySPI() {			
			@Override
			public String getName() {
				return message;
			}
			
			@Override
			public ImageIcon getIcon() {
				return null;
			}			
			@Override
			public UIComponentSPI getComponent() {
				return new UIComponentSPI() {					
					@Override
					public void onDispose() {
					}
					
					@Override
					public void onDisplay() {
					}
					
					@Override
					public String getName() {
						return message;
					}
					
					@Override
					public ImageIcon getIcon() {
						return null;
					}
				};
			}
		};
	}

	public UIComponentFactorySPI getWorkflowExplorerFactory() {
		if (workflowExplorerFactory == null) {
			workflowExplorerFactory = new WorkflowExplorerFactory();
			workflowExplorerFactory.setFileManager(getFileManager());
			workflowExplorerFactory.setEditManager(getEditManager());
			workflowExplorerFactory.setActivityIconManager(getActivityIconManager());
			workflowExplorerFactory.setMenuManager(getMenuManager());
			workflowExplorerFactory.setReportManager(getReportManager());
			workflowExplorerFactory.setSelectionManager(getSelectionManager());
			workflowExplorerFactory.setServiceRegistry(getServiceRegistry());			
		}
		return workflowExplorerFactory;
	}

	private ReportManager getReportManager() {
		// FIXME: ReportManagerImpl does not currently compile.. so we'll give a dummy instead
		return new ReportManager() {
			@Override
			public void updateReport(Profile p, boolean includeTimeConsuming, boolean remember) {
			}
			@Override
			public void updateObjectSetReport(Profile p, Set<WorkflowBean> objects) {
			}
			@Override
			public void updateObjectReport(Profile p, WorkflowBean o) {
			}
			@Override
			public void removeObserver(Observer<ReportManagerEvent> observer) {
			}
			@Override
			public boolean isStructurallySound(Profile p) {
				return true;
			}
			@Override
			public String getSummaryMessage(Profile p, WorkflowBean object) {
				return "Dummy report";
			}
			@Override
			public Status getStatus(Profile p, WorkflowBean object) {
				return Status.OK;
			}
			@Override
			public Status getStatus(Profile p) {
				return Status.OK;
			}
			@Override
			public Set<WorkflowBeanReport> getReports(Profile p, WorkflowBean object) {
				return Collections.emptySet();
			}
			@Override
			public Map<WorkflowBean, Set<WorkflowBeanReport>> getReports(Profile p) {
				return Collections.emptyMap();
			}
			@Override
			public List<Observer<ReportManagerEvent>> getObservers() {
				return Collections.emptyList();
			}
			@Override
			public long getLastFullCheckedTime(Profile p) {
				return 0;
			}
			@Override
			public long getLastCheckedTime(Profile p) {
				return 0;
			}			
			@Override
			public void addObserver(Observer<ReportManagerEvent> observer) {
			}
		};
	}

	public ActivityIconManager getActivityIconManager() {
		ActivityIconManagerImpl activityIconManagerImpl = new ActivityIconManagerImpl();
		activityIconManagerImpl.setActivityIcons(serviceLoader(ActivityIconSPI.class));
		return activityIconManagerImpl;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> serviceLoader(Class<T> klass) {
		List<T> spis = new ArrayList<>();
		for (T impl : ServiceLoader.load(klass)) { 
			spis.add(impl);
			try {
				BeanUtils.copyProperties(this, impl);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
				// continue ?
			}
		}
		return spis;
	}

	public UIComponentFactorySPI getServicePanelComponentFactory() {
		if (servicePanelFactory == null) {
			servicePanelFactory = new ServicePanelComponentFactory();		
			servicePanelFactory.setEditManager(getEditManager());
			servicePanelFactory.setMenuManager(getMenuManager());
			servicePanelFactory.setSelectionManager(getSelectionManager());
			servicePanelFactory.setServiceDescriptionRegistry(getServiceDescriptionRegistry());
			servicePanelFactory.setServiceRegistry(getServiceRegistry());
		}
		
		return servicePanelFactory;
	}

	public ServiceRegistry getServiceRegistry() {
		return new ServiceRegistryImpl();
	}

	public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
		if (serviceDescriptionRegistry == null) {
			serviceDescriptionRegistry = new ServiceDescriptionRegistryImpl(getApplicationConfiguration());
			serviceDescriptionRegistry.setServiceDescriptionProvidersList(serviceLoader(ServiceDescriptionProvider.class));
		}
		return serviceDescriptionRegistry;
	}

	public UIComponentFactorySPI getContextualViewComponentFactory() {
		ContextualViewComponentFactory contextualViewComponentFactory = new ContextualViewComponentFactory();
		contextualViewComponentFactory.setEditManager(getEditManager());
		contextualViewComponentFactory.setSelectionManager(getSelectionManager());
		contextualViewComponentFactory.setContextualViewFactoryRegistry(getContextualViewFactoryRegistry());
		return contextualViewComponentFactory;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ContextualViewFactoryRegistry getContextualViewFactoryRegistry() {
		if (registry == null) { 
			registry = new ContextualViewFactoryRegistryImpl();			
			List serviceLoader = serviceLoader(ContextualViewFactory.class);
			registry.setContextualViewFactories(serviceLoader);
		}
		return registry;
	}

	public List<PerspectiveSPI> getPerspectives() {
		return Arrays.asList(getDesignPerspective(), getResultsPerspective());
	}

	public WorkbenchImpl getWorkbench() {
		if (workbench == null) {
			workbench = new WorkbenchImpl(getStartupHooks(), getShutdownHooks(), getPerspectives());
			workbench.setApplicationConfiguration(getApplicationConfiguration());
			workbench.setEditManager(getEditManager());
			workbench.setFileManager(getFileManager());
			workbench.setMenuManager(getMenuManager());
			workbench.setSelectionManager(getSelectionManager());
			workbench.setPluginManager(getPluginManager());			
			workbench.setWorkbenchConfiguration(getWorkbenchConfiguration());
		}
		return workbench;
	}

	public WorkbenchConfiguration getWorkbenchConfiguration() {
		return new WorkbenchConfiguration() {
			@Override
			public Map<String, String> getDefaultPropertyMap() {
				return Collections.emptyMap();
			}
			@Override
			public String getUUID() {
				return "5c51f0d0-91b7-4d90-92e4-4416c61f6442";
			}
			@Override
			public String getDisplayName() {
				return "Taverna Test";
			}
			@Override
			public String getFilePrefix() {
				return "taverna-test";
			}
			@Override
			public String getCategory() {
				return "test";
			}
			@Override
			public void restoreDefaults() {
			}

			@Override
			public String getDefaultProperty(String key) {
				return null;
			}

			@Override
			public Set<String> getKeys() {
				return Collections.emptySet();
			}

			@Override
			public void clear() {
			}

			@Override
			public Map<String, String> getInternalPropertyMap() {
				return Collections.emptyMap();
			}

			@Override
			public String getProperty(String key) {
				return null;
			}

			@Override
			public void setProperty(String key, String value) {								
				// We'll just forget it anyway..
			}

			@Override
			public void deleteProperty(String key) {
			}

			@Override
			public List<String> getPropertyStringList(String key) {
				return Collections.emptyList();
			}

			@Override
			public void setPropertyStringList(String key, List<String> value) {
			}

			@Override
			public boolean getCaptureConsole() {
				return false;
			}

			@Override
			public void setCaptureConsole(boolean captureConsole) {
			}

			@Override
			public boolean getWarnInternalErrors() {
				return false;
			}

			@Override
			public void setWarnInternalErrors(boolean warnInternalErrors) {
			}

			@Override
			public int getMaxMenuItems() {
				return 1024;
			}

			@Override
			public void setMaxMenuItems(int maxMenuItems) {
			}

			@Override
			public String getDotLocation() {
				// Assume graphviz' dot is on PATH
				return "dot";
			}

			@Override
			public void setDotLocation(String dotLocation) {
			}
			
		};
	}

	private PluginManager getPluginManager() {
		// Can't install plugins while outside OSGi
		return new PluginManager() {			
			@Override
			public Plugin updatePlugin(PluginVersions pluginVersions) throws PluginException {
				throw new PluginException("Can't install plugins when running outside OSGi");
			}
			@Override
			public void loadPlugins() throws PluginException {
			}
			@Override
			public Plugin installPlugin(String pluginSiteURL, String pluginFile) throws PluginException {
				throw new PluginException("Can't install plugins when running outside OSGi");
			}
			@Override
			public Plugin installPlugin(Path pluginFile) throws PluginException {
				throw new PluginException("Can't install plugins when running outside OSGi");			}
			
			@Override
			public List<PluginVersions> getPluginUpdates() throws PluginException {
				return Collections.emptyList();
			}
			@Override
			public List<Plugin> getInstalledPlugins() throws PluginException {
				return Collections.emptyList();
			}
			@Override
			public List<PluginVersions> getAvailablePlugins() throws PluginException {
				return Collections.emptyList();
			}
			@Override
			public void checkForUpdates() throws PluginException {				
			}
		};
	}

	public List<StartupSPI> getStartupHooks() {
		SetCredManAuthenticatorStartupHook credManAuthenticatorStartupHook = new SetCredManAuthenticatorStartupHook();
		credManAuthenticatorStartupHook.setCredentialManager(getCredentialManager());
		InitialiseSSLStartupHook initialiseSSLStartupHook = new InitialiseSSLStartupHook();
		initialiseSSLStartupHook.setCredentialManager(getCredentialManager());
		return Arrays.asList(initialiseSSLStartupHook, credManAuthenticatorStartupHook);
	}

	public CredentialManager getCredentialManager() {
		if (credentialManager == null) {
			try {
				credentialManager = new CredentialManagerImpl();			
			} catch (CMException e) {
				e.printStackTrace();
			}
		}
		return credentialManager;
	}

	public List<ShutdownSPI> getShutdownHooks() {
		return Arrays.asList();
	}

	public ApplicationConfiguration getApplicationConfiguration() {
		return new ApplicationConfigurationImpl();
	}
	
}
