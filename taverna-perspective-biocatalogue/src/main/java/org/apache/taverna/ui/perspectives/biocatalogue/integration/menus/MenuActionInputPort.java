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
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;

import org.apache.taverna.workflowmodel.InputPort;


/**
 * This class currently won't be used, as an entry for it was removed from
 * META-INF/services/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent
 * 
 * This is because no useful action is yet available for input/output ports.
 * 
 * @author Sergejs Aleksejevs
 */
public class MenuActionInputPort extends AbstractContextualMenuAction {

	public MenuActionInputPort() throws URISyntaxException {
		super(BioCatalogueContextualMenuSection.BIOCATALOGUE_MENU_SECTION_ID, 15);
	}

	@Override
	protected Action createAction() {
		return new AbstractAction("InputPort") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(getContextualSelection().getRelativeToComponent(), "Hoho!");
			}
		};
	}

	@Override
	public boolean isEnabled() {
	  return (super.isEnabled() && getContextualSelection().getSelection() instanceof InputPort);
	}
	
}
