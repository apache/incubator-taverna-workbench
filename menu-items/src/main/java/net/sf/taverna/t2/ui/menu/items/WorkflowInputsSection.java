package net.sf.taverna.t2.ui.menu.items;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.DefaultContextualMenu;

public class WorkflowInputsSection extends AbstractMenuSection {

	public static final URI inputsSection = URI.create("http://taverna.sf.net/2009/contextMenu/inputs");

	public WorkflowInputsSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 10, inputsSection);
	}
	
}
