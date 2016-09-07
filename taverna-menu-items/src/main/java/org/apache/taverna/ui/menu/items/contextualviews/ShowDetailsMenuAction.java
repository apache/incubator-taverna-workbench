package org.apache.taverna.ui.menu.items.contextualviews;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.ui.menu.DesignOnlyAction;
import org.apache.taverna.workbench.ui.Workbench;

public class ShowDetailsMenuAction extends AbstractMenuAction {
	private static final URI SHOW_DETAILS_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuShowDetailsComponent");

	private static final String SHOW_DETAILS = "Details";
	private String namedComponent = "contextualView";

	private Workbench workbench;

 	public ShowDetailsMenuAction() {
		super(ShowConfigureMenuAction.GRAPH_DETAILS_MENU_SECTION, 20, SHOW_DETAILS_URI);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
		// FIXME: Should we list all the applicable types here?
		// && getContextualSelection().getSelection() instanceof Processor;
	}

	@Override
	protected Action createAction() {
		return new ShowDetailsAction();
	}

	protected class ShowDetailsAction extends AbstractAction implements DesignOnlyAction {

		ShowDetailsAction() {
			super();
			putValue(NAME, "Show details");
			putValue(SHORT_DESCRIPTION, "Show details of selected component");
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			workbench.makeNamedComponentVisible(namedComponent);
		}

	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

}
