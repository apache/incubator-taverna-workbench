/**********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 **********************************************************************/
package org.apache.taverna.ui.menu.items.contextualviews;

import java.net.URI;

import org.apache.taverna.ui.menu.AbstractMenu;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;

public class ConfigureRunningContextualMenuSection extends AbstractMenu
implements ContextualMenuComponent {
	public static final String CONFIGURE_RUNNING = "Configure running";
	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");
	private ContextualSelection contextualSelection;

	public ConfigureRunningContextualMenuSection() {
		super(ConfigureSection.configureSection, 45, configureRunningSection, CONFIGURE_RUNNING);
	}

	@Override
	public boolean isEnabled() {
		return true;
//		return super.isEnabled() && contextualSelection instanceof Processor;
	}
	
	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}

}
