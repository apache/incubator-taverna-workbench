package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.menus;

import java.awt.event.ActionEvent;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;

import net.sf.taverna.t2.workflowmodel.InputPort;


/**
 * This class currently won't be used, as an entry for it was removed from
 * META-INF/services/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent
 * 
 * This is because no useful action is yet available for input/output ports.
 * 
 * @author Sergejs Aleksejevs
 */
public class MenuActionInputPort extends AbstractContextualMenuAction {

	public MenuActionInputPort() throws URISyntaxException {
		super(BioCatalogueContextualMenuSection.BIOCATALOGUE_MENU_SECTION_ID, 15);
	}

	@Override
	protected Action createAction() {
		return new AbstractAction("InputPort") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(getContextualSelection().getRelativeToComponent(), "Hoho!");
			}
		};
	}

	@Override
	public boolean isEnabled() {
	  return (super.isEnabled() && getContextualSelection().getSelection() instanceof InputPort);
	}
	
}
