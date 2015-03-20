package net.sf.taverna.t2.workbench.updatemanager;

import static net.sf.taverna.t2.workbench.updatemanager.UpdatesToolbarSection.UPDATES_SECTION;

import java.awt.Component;

import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;

public class UpdatesAvailableMenuAction extends AbstractMenuCustom {
	public UpdatesAvailableMenuAction() {
		super(UPDATES_SECTION, 10);
	}

	@Override
	protected Component createCustomComponent() {
		//return new UpdatesAvailableIcon();
		return null;
	}
}
