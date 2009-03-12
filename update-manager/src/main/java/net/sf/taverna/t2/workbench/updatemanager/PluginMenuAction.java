package net.sf.taverna.t2.workbench.updatemanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.plugins.ui.PluginManagerFrame;
import net.sf.taverna.raven.plugins.ui.UpdatesAvailableIcon;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class PluginMenuAction extends AbstractMenuAction {

	@SuppressWarnings("serial")
	protected class SoftwareUpdates extends AbstractAction {
		public SoftwareUpdates() {
			super("Software updates", UpdatesAvailableIcon.updateRecommendedIcon);
		}

		public void actionPerformed(ActionEvent e) {
			Component parent = null;
			if (e.getSource() instanceof Component) {
				parent = (Component) e.getSource();
			}
			final PluginManagerFrame pluginManagerUI = new PluginManagerFrame(
					PluginManager.getInstance());
			if (parent != null) {
				pluginManagerUI.setLocationRelativeTo(parent);
			}
			pluginManagerUI.setVisible(true);
		}
	}

	public PluginMenuAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#advanced"),
				100);
	}

	@Override
	protected Action createAction() {
		return new SoftwareUpdates();
	}

}
