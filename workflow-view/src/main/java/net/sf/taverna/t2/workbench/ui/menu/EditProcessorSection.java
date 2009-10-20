package net.sf.taverna.t2.workbench.ui.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.DefaultContextualMenu;

public class EditProcessorSection extends AbstractMenuSection {

	public static final URI editSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/editSection");

	public EditProcessorSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 15, editSection);
	}

	@Override
	protected Action createAction() {
		return new DummyAction("Edit");
	}

}
