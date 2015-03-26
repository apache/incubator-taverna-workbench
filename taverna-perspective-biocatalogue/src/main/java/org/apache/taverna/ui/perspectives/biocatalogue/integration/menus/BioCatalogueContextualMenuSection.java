package org.apache.taverna.ui.perspectives.biocatalogue.integration.menus;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.biocatalogue.model.ResourceManager;
import org.apache.taverna.lang.ui.ShadedLabel;
import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Processor;


public class BioCatalogueContextualMenuSection extends AbstractMenuSection implements ContextualMenuComponent
{
  // TODO - this shouldn't be here, must reference this field in AbstractMenuSection!!
  public static final String SECTION_COLOR = "sectionColor";

  
  public static final URI BIOCATALOGUE_MENU_SECTION_ID = URI.create("http://biocatalogue.org/2010/contextMenu/biocatalogue_section");
  private static final String SECTION_TITLE = "Service Catalogue";
  
  private ContextualSelection contextualSelection;
  
  
  public BioCatalogueContextualMenuSection() {
          super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 100000, BIOCATALOGUE_MENU_SECTION_ID);
  }

  public ContextualSelection getContextualSelection() {
          return contextualSelection;
  }
  
  public void setContextualSelection(ContextualSelection contextualSelection) {
    this.contextualSelection = contextualSelection;
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled()
                    && (getContextualSelection().getSelection() instanceof Dataflow ||
                        getContextualSelection().getSelection() instanceof Processor ||
                        getContextualSelection().getSelection() instanceof InputPort);
  }
  
  @SuppressWarnings("serial")
  protected Action createAction()
  {
    Action action = new AbstractAction(SECTION_TITLE, ResourceManager.getImageIcon(ResourceManager.FAVICON)) {
      public void actionPerformed(ActionEvent e) {
      }
    };
    action.putValue(SECTION_COLOR, ShadedLabel.GREEN);
    return (action);
  }
}
