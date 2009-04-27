package net.sf.taverna.t2.servicedescriptions;



public interface CustomizedConfigurePanelProvider<ConfigurationBean> 
	extends ConfigurableServiceProvider<ConfigurationBean> {

	public void createCustomizedConfigurePanel(
			CustomizedConfigureCallBack<ConfigurationBean> callBack);

	
	public interface CustomizedConfigureCallBack<ConfigurationBean> {

		public void newProviderConfiguration(
				ConfigurationBean providerConfig);

		public ConfigurationBean getTemplateConfig();

		public ServiceDescriptionRegistry getServiceDescriptionRegistry();
		
	}
	
	
}
