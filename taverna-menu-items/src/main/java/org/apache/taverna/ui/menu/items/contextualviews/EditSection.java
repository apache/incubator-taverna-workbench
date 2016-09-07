package org.apache.taverna.ui.menu.items.contextualviews;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.lang.ui.ShadedLabel;
import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;

/**
 * Menu section containing the actions to add service templates, i.e. activities
 * than are not readily runnable but need to be configured first. The actual actions that
 * go into this menu can be found in the ui modules for the activities.
 *
 * @author Alex Nenadic
 *
 */
public class EditSection extends AbstractMenuSection
		implements ContextualMenuComponent {

	private static final String EDIT = "Edit";
	public static final URI editSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/edit");
	private ContextualSelection contextualSelection;

	public EditSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 10, editSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}

	@Override
	protected Action createAction() {
		DummyAction action = new DummyAction(EDIT);
		// Set the colour for the section
		action.putValue(AbstractMenuSection.SECTION_COLOR, ShadedLabel.ORANGE);
		return action;
	}
}
