package org.apache.taverna.ui.menu.items.contextualviews;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;
import org.apache.taverna.scufl2.api.core.Workflow;

public class InsertSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	private static final String INSERT = "Insert";
	public static final URI insertSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/insert");
	private ContextualSelection contextualSelection;

	public InsertSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 20, insertSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Workflow;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}

	@Override
	protected Action createAction() {
		return new DummyAction(INSERT);
	}
}
