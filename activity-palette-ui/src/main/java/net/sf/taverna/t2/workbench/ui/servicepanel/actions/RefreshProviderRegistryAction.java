package net.sf.taverna.t2.workbench.ui.servicepanel.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;

/**
 * Action for refreshing the service provider registry.
 * <p>
 * This would typically re-parse WSDLs, etc.
 * 
 * @see ServiceDescriptionRegistry#refresh()
 * 
 * @author Stian Soiland-Reyes
 *
 */
@SuppressWarnings("serial")
public class RefreshProviderRegistryAction extends AbstractAction {

	private ServiceDescriptionRegistry serviceDescriptionRegistry = ServiceDescriptionRegistryImpl.getInstance();

	public RefreshProviderRegistryAction() {
		super("Refresh");
	}

	public void actionPerformed(ActionEvent e) {
		getServiceDescriptionRegistry().refresh();
	}

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
		return serviceDescriptionRegistry;
	}

}
