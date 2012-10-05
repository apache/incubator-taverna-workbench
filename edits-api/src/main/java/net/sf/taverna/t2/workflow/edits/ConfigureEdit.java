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

import java.util.List;

import uk.org.taverna.scufl2.api.common.Configurable;
import uk.org.taverna.scufl2.api.configurations.Configuration;

/**
 * An Edit that configures a {@link Configurable} with a
 * given {@link Configuration}.
 *
 * @author David Withers
 */
public class ConfigureEdit<ConfigurableType extends Configurable> extends AbstractEdit<ConfigurableType> {

	private Configuration previousConfiguration;
	private final Configuration configuration;

	public ConfigureEdit(ConfigurableType configurable, Configuration configuration) {
		super(configurable);
		this.configuration = configuration;
	}

	@Override
	protected void doEditAction(ConfigurableType configurable) {
		List<Configuration> configurations = scufl2Tools.configurationsFor(configurable, configuration.getParent());
		if (configurations.size() == 1) {
			previousConfiguration = configurations.get(0);
			previousConfiguration.setConfigures(null);
		}
		configuration.setConfigures(configurable);
	}

	@Override
	protected void undoEditAction(ConfigurableType configurable) {
		configuration.setConfigures(null);
		if (previousConfiguration != null) {
			previousConfiguration.setConfigures(configurable);
		}
	}

}
