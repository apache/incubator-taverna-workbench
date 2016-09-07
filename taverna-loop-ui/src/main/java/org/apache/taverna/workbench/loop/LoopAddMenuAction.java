/**********************************************************************
 **********************************************************************/
package org.apache.taverna.workbench.loop;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.scufl2.api.core.Processor;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;

public class LoopAddMenuAction extends AbstractContextualMenuAction {

	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI LOOP_ADD_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/loopAdd");

	private static final String LOOP_ADD = "Loop add";

	public LoopAddMenuAction() {
		super(configureRunningSection, 20, LOOP_ADD_URI);
	}

	private AddLoopFactory addLoopFactory;

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Looping...") {
			public void actionPerformed(ActionEvent e) {
				//Loop loopLayer = null;
				Processor p = (Processor) getContextualSelection().getSelection();
				addLoopFactory.getAddLayerActionFor(p).actionPerformed(e);
				//LoopConfigureMenuAction.configureLoopLayer(p, e); // Configuration dialog pop up is now done from getAddLayerActionFor()
			}
		};
	}

	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return (super.isEnabled() && (selection instanceof Processor) && (LoopConfigureMenuAction.getLoopLayer((Processor)selection) == null));
	}

	public void setAddLoopFactory(AddLoopFactory addLoopFactory) {
		this.addLoopFactory = addLoopFactory;
	}



}
