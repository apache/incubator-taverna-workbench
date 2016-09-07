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
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
