/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.contextualviews;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.ui.Workbench;

import org.apache.log4j.Logger;

public class ShowDetailsContextualMenuAction extends AbstractContextualMenuAction {
	private static final String SHOW_DETAILS = "Show details";
	private String namedComponent = "contextualView";

	private static Logger logger = Logger.getLogger(ShowDetailsContextualMenuAction.class);
	private Workbench workbench;

	public ShowDetailsContextualMenuAction() {
		super(ConfigureSection.configureSection, 40);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
		// FIXME: Should we list all the applicable types here?
		// && getContextualSelection().getSelection() instanceof Processor;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction(SHOW_DETAILS) {
			public void actionPerformed(ActionEvent e) {
				workbench.makeNamedComponentVisible(namedComponent);
			}
		};
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

}
