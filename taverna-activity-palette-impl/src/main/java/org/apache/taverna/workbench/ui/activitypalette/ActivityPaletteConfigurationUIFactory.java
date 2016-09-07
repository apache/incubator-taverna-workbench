package org.apache.taverna.workbench.ui.activitypalette;

import javax.swing.JPanel;
import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationUIFactory;

public class ActivityPaletteConfigurationUIFactory implements
		ConfigurationUIFactory {
	private ActivityPaletteConfiguration activityPaletteConfiguration;

	@Override
	public boolean canHandle(String uuid) {
		return uuid != null && uuid.equals(getConfigurable().getUUID());
	}

	@Override
	public Configurable getConfigurable() {
		return activityPaletteConfiguration;
	}

	@Override
	public JPanel getConfigurationPanel() {
		return new ActivityPaletteConfigurationPanel(
				activityPaletteConfiguration);
	}

	public void setActivityPaletteConfiguration(
			ActivityPaletteConfiguration activityPaletteConfiguration) {
		this.activityPaletteConfiguration = activityPaletteConfiguration;
	}
}
