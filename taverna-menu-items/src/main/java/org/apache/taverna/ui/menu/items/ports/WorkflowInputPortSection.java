package org.apache.taverna.ui.menu.items.ports;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.scufl2.api.port.InputWorkflowPort;

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;

public class WorkflowInputPortSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	public static final URI inputPort = URI
			.create("http://taverna.sf.net/2009/contextMenu/inputPort");
	private ContextualSelection contextualSelection;

	public WorkflowInputPortSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 10, inputPort);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof InputWorkflowPort;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.action = null;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		InputWorkflowPort proc = (InputWorkflowPort) getContextualSelection().getSelection();
		String name = "Workflow input port: " + proc.getName();
		return new AbstractAction(name) {
			public void actionPerformed(ActionEvent e) {
			}
		};
	}

}
