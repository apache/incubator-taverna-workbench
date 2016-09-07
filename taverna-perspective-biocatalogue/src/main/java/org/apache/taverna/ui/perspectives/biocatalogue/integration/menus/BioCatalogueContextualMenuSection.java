package org.apache.taverna.ui.perspectives.biocatalogue.integration.menus;
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
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.InputPort;
import org.apache.taverna.workflowmodel.Processor;


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
