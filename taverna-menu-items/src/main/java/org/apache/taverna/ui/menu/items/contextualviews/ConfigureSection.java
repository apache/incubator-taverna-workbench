package org.apache.taverna.ui.menu.items.contextualviews;

import java.net.URI;

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;

/**
 * Menu section containing the actions to add service templates, i.e. activities
 * than are not readily runnable but need to be configured first. The actual actions that
 * go into this menu can be found in the ui modules for the activities.
 *
 * @author Alex Nenadic
 *
 */
public class ConfigureSection extends AbstractMenuSection
		implements ContextualMenuComponent {

	public static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");
	private ContextualSelection contextualSelection;

	public ConfigureSection() {
		super(EditSection.editSection, 100, configureSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return super.isEnabled();
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}
}
