package org.apache.taverna.ui.menu.items.contextualviews;

import java.net.URI;

import org.apache.taverna.ui.menu.AbstractMenu;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;

public class ConfigureRunningContextualMenuSection extends AbstractMenu
implements ContextualMenuComponent {
	public static final String CONFIGURE_RUNNING = "Configure running";
	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");
	private ContextualSelection contextualSelection;

	public ConfigureRunningContextualMenuSection() {
		super(ConfigureSection.configureSection, 45, configureRunningSection, CONFIGURE_RUNNING);
	}

	@Override
	public boolean isEnabled() {
		return true;
//		return super.isEnabled() && contextualSelection instanceof Processor;
	}
	
	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}

}
