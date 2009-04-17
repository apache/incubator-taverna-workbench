/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.servicepanel.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.uibuilder.UIBuilder;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * Action for adding a service provider
 * 
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("serial")
public class AddServiceProviderAction extends AbstractAction {

	protected static Dimension DIALOG_SIZE = new Dimension(400, 300);

	private ServiceDescriptionRegistry serviceDescriptionRegistry;

	private final class AddProviderAction extends AbstractAction {
		private final Object configurationBean;
		private final JDialog dialog;

		private AddProviderAction(Object configurationBean, JDialog dialog) {
			super("Add");
			this.configurationBean = configurationBean;
			this.dialog = dialog;
		}

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {

			ConfigurableServiceProvider cloned = confProvider.clone();
			try {
				cloned.configure(configurationBean);
				getServiceDescriptionRegistry().addServiceDescriptionProvider(
						cloned);
				System.out.println("Added with " + configurationBean);
				dialog.setVisible(false);
			} catch (ConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private final ConfigurableServiceProvider confProvider;

	@SuppressWarnings("unchecked")
	public AddServiceProviderAction(ConfigurableServiceProvider confProvider) {
		super(confProvider.getName() + "...", confProvider.getIcon());
		this.confProvider = confProvider;
	}

	public void actionPerformed(ActionEvent e) {
		Object configurationBean;
		try {
			configurationBean = BeanUtils.cloneBean(confProvider
					.getConfiguration());
		} catch (Exception ex) {
			throw new RuntimeException("Can't clone configuration bean", ex);
		}
		JPanel buildEditor = buildEditor(configurationBean);
		JDialog dialog = new JDialog();
		dialog.setTitle("Add " + confProvider.getName());
		dialog.add(buildEditor);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(
				new JButton(new AddProviderAction(configurationBean, dialog)),
				BorderLayout.WEST);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setSize(DIALOG_SIZE);
		dialog.setVisible(true);
	}

	protected JPanel buildEditor(Object configurationBean) {

		PropertyDescriptor[] properties;
		try {
			properties = PropertyUtils
					.getPropertyDescriptors(configurationBean);
		} catch (Exception ex) {
			throw new RuntimeException("Can't inspect configuration bean", ex);
		}
		List<String> uiBuilderConfig = new ArrayList<String>();		
		int lastPreferred = 0;
		for (PropertyDescriptor property : properties) {
			if (property.isHidden() || property.isExpert()) {
				// TODO: Add support for expert properties
				continue;
			}
			String propertySpec = property.getName() + ":name=" + property.getDisplayName();
			if (property.isPreferred()) {
				// Add it to the front
				uiBuilderConfig.add(lastPreferred++, propertySpec);
			} else {
				uiBuilderConfig.add(propertySpec);
			}
		}

		return UIBuilder.buildEditor(configurationBean, uiBuilderConfig
				.toArray(new String[uiBuilderConfig.size()]));
	}

	public void setServiceDescriptionRegistry(
			ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
		return serviceDescriptionRegistry;
	}
}