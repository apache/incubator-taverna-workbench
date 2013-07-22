/*******************************************************************************
 * Copyright (C) 2007-2008 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.processor;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * SPI factory for creating a {@link ProcessorContextualView}.
 *
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 */
public class ProcessorActivitiesContextualViewFactory implements ContextualViewFactory<Processor> {

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	private ContextualViewFactoryRegistry contextualViewFactoryRegistry;
	private SelectionManager selectionManager;

	public boolean canHandle(Object selection) {
		return selection instanceof Processor;
	}

	public List<ContextualView> getViews(Processor selection) {
		List<ContextualView> result = new ArrayList<ContextualView>();
		List<ProcessorBinding> processorBindings = scufl2Tools.processorBindingsForProcessor(
				selection, selectionManager.getSelectedProfile());
		for (ProcessorBinding processorBinding : processorBindings) {
			Activity activity = processorBinding.getBoundActivity();
			List<ContextualViewFactory> viewFactoryForBeanType = (List<ContextualViewFactory>) contextualViewFactoryRegistry
					.getViewFactoriesForObject(activity);
			for (ContextualViewFactory cvf : viewFactoryForBeanType) {
				result.addAll(cvf.getViews(activity));
			}
		}
		return result;
	}

	public void setContextualViewFactoryRegistry(
			ContextualViewFactoryRegistry contextualViewFactoryRegistry) {
		this.contextualViewFactoryRegistry = contextualViewFactoryRegistry;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
