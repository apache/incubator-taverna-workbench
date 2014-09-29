package net.sf.taverna.t2.servicedescriptions;

import java.util.Collection;

import javax.swing.Icon;

import net.sf.taverna.t2.lang.beans.PropertyAnnotation;

/**
 * A provider of service descriptions
 * 
 * @author Stian Soiland-Reyes
 */
public interface ServiceDescriptionProvider {
	/**
	 * Get all service descriptions.
	 * 
	 * @param callBack
	 */
	void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack);

	/**
	 * @author stain
	 */
	interface FindServiceDescriptionsCallBack {
		void partialResults(
				Collection<? extends ServiceDescription> serviceDescriptions);

		void status(String message);

		void warning(String message);

		void finished();

		void fail(String message, Throwable ex);
	}

	/**
	 * Name of this service description provider, for instance "BioCatalogue" or
	 * "WSDL". This name is typically used in a "Add service..." menu.
	 * 
	 * @return Name of provider
	 */
	String getName();

	@PropertyAnnotation(expert = true)
	abstract Icon getIcon();

	/**
	 * @return unique id of this provider.
	 */
	String getId();
}
