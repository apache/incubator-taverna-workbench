package net.sf.taverna.t2.workbench.updatemanager;

import java.awt.Component;

import net.sf.taverna.raven.plugins.ui.UpdatesAvailableIcon;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;

public class UpdatesAvailableMenuAction extends AbstractMenuCustom {

	public UpdatesAvailableMenuAction() {
		super(UpdatesToolbarSection.UPDATES_SECTION, 10);
	}

	@Override
	protected Component createCustomComponent() {
		
		return new UpdatesAvailableIcon();
	}

}
