/*
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 * Source code information
 * -----------------------
 * Filename           $RCSfile: PluginListModel.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:51:52 $
 *               by   $Author: sowen70 $
 * Created on 28 Nov 2006
package org.apache.taverna.raven.plugins.ui;

import javax.swing.AbstractListModel;

import org.apache.log4j.Logger;

/**
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class PluginListModel extends AbstractListModel implements PluginManagerListener {
	private PluginManager pluginManager;

	private static Logger logger = Logger.getLogger(PluginListModel.class);

	public PluginListModel(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
		PluginManager.addPluginManagerListener(this);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return pluginManager.getPlugins().get(index);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return pluginManager.getPlugins().size();
	}

	public void pluginAdded(PluginManagerEvent event) {
		fireIntervalAdded(this, event.getPluginIndex(), event.getPluginIndex());
	}

	public void pluginRemoved(PluginManagerEvent event) {
		fireIntervalRemoved(this, event.getPluginIndex(), event.getPluginIndex());
	}

	public void pluginUpdated(PluginManagerEvent event) {
		//fireContentsChanged(this, event.getPluginIndex(), event.getPluginIndex());
	}

	public void pluginStateChanged(PluginManagerEvent event) {
		fireContentsChanged(this, event.getPluginIndex(), event.getPluginIndex());
	}

	public void pluginIncompatible(PluginManagerEvent event) {

	}
}
