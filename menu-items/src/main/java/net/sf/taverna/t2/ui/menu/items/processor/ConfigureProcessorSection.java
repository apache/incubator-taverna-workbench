package net.sf.taverna.t2.ui.menu.items.processor;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.DefaultContextualMenu;

public class ConfigureProcessorSection extends AbstractMenuSection {

	public static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configureSection");

	public ConfigureProcessorSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 20, configureSection);
	}

	@Override
	protected Action createAction() {
		return new DummyAction("Configure");
	}

}
