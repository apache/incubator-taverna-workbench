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
/*

package org.apache.taverna.workbench.iterationstrategy.menu;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;
import org.apache.taverna.workbench.iterationstrategy.contextview.IterationStrategyConfigurationDialog;
import org.apache.taverna.workbench.iterationstrategy.contextview.IterationStrategyContextualView;
import org.apache.taverna.workflowmodel.Processor;

public class IterationStrategyConfigureMenuAction extends AbstractContextualMenuAction {
	
	
	
	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");
	
	private static final URI ITERATION_STRATEGY_CONFIGURE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/iterationStrategyConfigure");

	public IterationStrategyConfigureMenuAction() {
		super(configureRunningSection, 40, ITERATION_STRATEGY_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("List handling...") {
			public void actionPerformed(ActionEvent e) {
				Processor p = (Processor) getContextualSelection().getSelection();
				final HelpEnabledDialog dialog = new IterationStrategyConfigurationDialog(null, p, IterationStrategyContextualView.copyIterationStrategyStack(p.getIterationStrategy()));		
				dialog.setVisible(true);
			}
		};
	}
	
	public boolean isEnabled() {
		return super.isEnabled() && (getContextualSelection().getSelection() instanceof Processor);
	}

}
