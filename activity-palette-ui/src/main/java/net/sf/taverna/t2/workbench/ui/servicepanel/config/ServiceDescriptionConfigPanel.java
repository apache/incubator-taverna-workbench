package net.sf.taverna.t2.workbench.ui.servicepanel.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionsConfig;

@SuppressWarnings("serial")
public class ServiceDescriptionConfigPanel extends JPanel {
	public class RemoveUserServicesAction extends AbstractAction {

		public RemoveUserServicesAction() {
			super("Remove user-added services");
		}
		
		public void actionPerformed(ActionEvent e) {
			for (ServiceDescriptionProvider provider : serviceDescRegistry.getUserAddedServiceProviders()) {
				serviceDescRegistry.removeServiceDescriptionProvider(provider);
			}
		}
	}

	public class RestoreAction extends AbstractAction {

		public RestoreAction() {
			super("Restore default services");
		}
		public void actionPerformed(ActionEvent e) {
			includeDefaults.setSelected(false);
			includeDefaults.setSelected(true);
		}
	}

	private static final String REMOVE_PERMANENTLY = "Allow permanent removal of default service providers";
	private static final String INCLUDE_DEFAULTS = "Include default service providers";
	private final ServiceDescriptionsConfig config;
	private JCheckBox includeDefaults;
	private JCheckBox removePermanently;
	private final ServiceDescriptionRegistry serviceDescRegistry;

	public ServiceDescriptionConfigPanel(ServiceDescriptionsConfig config,
			ServiceDescriptionRegistry serviceDescRegistry) {
		this.config = config;
		this.serviceDescRegistry = serviceDescRegistry;
		initialize();
	}

	protected void initialize() {
		removeAll();
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;
		
		includeDefaults = new JCheckBox(INCLUDE_DEFAULTS, config
				.isIncludeDefaults());
		includeDefaults.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				config.setIncludeDefaults(e.getStateChange() == ItemEvent.SELECTED);
				
				for (ServiceDescriptionProvider provider : serviceDescRegistry
						.getDefaultServiceDescriptionProviders()) {
					if (! (provider instanceof ConfigurableServiceProvider)) {
						continue;
					}
					if (config.isIncludeDefaults()) {
						serviceDescRegistry.addServiceDescriptionProvider(provider);
					} else {
						serviceDescRegistry.removeServiceDescriptionProvider(provider);
					}
				}
			}
		});
		add(includeDefaults, gbc);

		removePermanently = new JCheckBox(REMOVE_PERMANENTLY, config
				.isRemovePermanently());
		removePermanently.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				config.setRemovePermanently(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		add(removePermanently, gbc);


		gbc.gridwidth = 1;
		add(new JButton(new RestoreAction()), gbc);
		gbc.gridx = 1;
		add(new JButton(new RemoveUserServicesAction()), gbc);
		
		
		
		// Filler
		gbc.weighty = 0.1;
		gbc.weightx = 0.1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;
		add(new JPanel(), gbc);
		
	}
}
