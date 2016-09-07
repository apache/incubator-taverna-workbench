/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.processor;

import java.net.URI;

import org.apache.taverna.scufl2.api.core.Processor;

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.items.contextualviews.EditSection;

public class ProcessorSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	public static final URI processorSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/processor");
	private ContextualSelection contextualSelection;

	public ProcessorSection() {
		super(EditSection.editSection, 200, processorSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Processor;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.action = null;
	}

}
