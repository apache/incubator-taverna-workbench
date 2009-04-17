package net.sf.taverna.t2.servicedescriptions;

import java.util.Collection;

/**
 * A provider of service descriptions
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public interface ServiceDescriptionProvider {

	/**
	 * Get all service descriptions.
	 * 
	 * @param callBack
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack);

	/**
	 * 
	 * @author stain
	 * 
	 */
	public interface FindServiceDescriptionsCallBack {

		@SuppressWarnings("unchecked")
		public void partialResults(
				Collection<? extends ServiceDescription> serviceDescriptions);

		public void status(String message);
		
		public void warning(String message);

		public void finished();

		public void fail(String message, Throwable ex);
	}

	/**
	 * Name of this service description provider, for instance "BioCatalogue" or
	 * "WSDL". This name is typically used in a "Add service..." menu.
	 * 
	 * @return Name of provider
	 */
	public String getName();

}
