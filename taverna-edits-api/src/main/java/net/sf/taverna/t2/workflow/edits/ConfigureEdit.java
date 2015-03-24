/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
 ******************************************************************************/
package net.sf.taverna.t2.workflow.edits;

import org.apache.taverna.scufl2.api.common.Configurable;
import org.apache.taverna.scufl2.api.configurations.Configuration;

/**
 * An Edit that configures a {@link Configurable} with a given
 * {@link Configuration}.
 * 
 * @author David Withers
 */
public class ConfigureEdit<ConfigurableType extends Configurable> extends
		AbstractEdit<ConfigurableType> {
	private final Configuration oldConfiguration;
	private final Configuration newConfiguration;

	public ConfigureEdit(ConfigurableType configurable,
			Configuration oldConfiguration, Configuration newConfiguration) {
		super(configurable);
		this.oldConfiguration = oldConfiguration;
		this.newConfiguration = newConfiguration;
	}

	@Override
	protected void doEditAction(ConfigurableType configurable) {
		oldConfiguration.setConfigures(null);
		newConfiguration.setConfigures(configurable);
	}

	@Override
	protected void undoEditAction(ConfigurableType configurable) {
		oldConfiguration.setConfigures(configurable);
		newConfiguration.setConfigures(null);
	}
}
