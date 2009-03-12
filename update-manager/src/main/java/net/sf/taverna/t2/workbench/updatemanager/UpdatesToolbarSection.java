package net.sf.taverna.t2.workbench.updatemanager;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;

public class UpdatesToolbarSection extends AbstractMenuSection {

	public static final URI UPDATES_SECTION = URI
			.create("http://taverna.sf.net/2008/t2workbench/toolbar#updatesSection");

	public UpdatesToolbarSection() {
		super(DefaultToolBar.DEFAULT_TOOL_BAR, 10000, UPDATES_SECTION);
	}

}
