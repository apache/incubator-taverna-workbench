package org.apache.taverna.ui.perspectives.biocatalogue.integration.contextual_views;
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

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.taverna.biocatalogue.model.BioCataloguePluginConstants;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;


public class ProcessorOutputPortView extends ContextualView
{
	private final ActivityOutputPort outputPort;
	private JPanel jPanel;

	public ProcessorOutputPortView(ActivityOutputPort outputPort) {
		this.outputPort = outputPort;
		
		jPanel = new JPanel();
		
		// NB! This is required to have the body of this contextual
		// view added to the main view; otherwise, body will be
		// blank
		initView();
	}
	
	@Override
	public JComponent getMainFrame()
	{
		return jPanel;
	}

	@Override
	public String getViewTitle() {
		return "Service Catalogue Information";
	} 

	@Override
	public void refreshView()
	{
	  // this actually causes the parent container to validate itself,
    // which is what is needed here
    this.revalidate();
    this.repaint();
	}
	
	@Override
	public int getPreferredPosition() {
		return BioCataloguePluginConstants.CONTEXTUAL_VIEW_PREFERRED_POSITION;
	}

}
