package org.apache.taverna.workbench.loop;
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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.workbench.MainWindow;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.AddLayerFactorySPI;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AddLoopFactory implements AddLayerFactorySPI {

    private static final URI LOOP_TYPE = URI.create("http://ns.taverna.org.uk/2010/scufl2/taverna/dispatchlayer/Loop");

    
    private static Logger logger = Logger.getLogger(AddLoopFactory.class);
    private static final JsonNodeFactory JSON_NODE_FACTORY = JsonNodeFactory.instance;
    private static Scufl2Tools scufl2Tools = new Scufl2Tools();
    
	private EditManager editManager;
	private FileManager fileManager;
	private SelectionManager selectionManager;
	private ApplicationConfiguration applicationConfig;

	public boolean canAddLayerFor(Processor processor) {
	   return findLoopLayer(processor) == null;
	}


    public ObjectNode findLoopLayer(Processor processor) {
        List<Configuration> configs = scufl2Tools.configurationsFor(processor, selectionManager.getSelectedProfile());
        for (Configuration config : configs) {
            if (config.getJson().has("loop")) {
                return (ObjectNode) config.getJson().get("loop");
            }
        }
        return null;
    }
	
	@SuppressWarnings("serial")
	public Action getAddLayerActionFor(final Processor processor) {
		return new AbstractAction("Add looping") {

            public void actionPerformed(ActionEvent e) {
				    ObjectNode loopLayer = findLoopLayer(processor);
				    if (loopLayer == null) {
				        loopLayer = JSON_NODE_FACTORY.objectNode();
				    }
					// Pop up the configure loop dialog
                LoopConfigureAction loopConfigureAction = new LoopConfigureAction(
                        MainWindow.getMainWindow(), null, processor, loopLayer,
                        selectionManager.getSelectedProfile(), editManager,
                        fileManager, getApplicationConfig());
					loopConfigureAction.actionPerformed(e);
			}
		};
	}

	@Override
	public boolean canCreateLayerClass(URI dispatchLayerType) {
	    return dispatchLayerType.equals(LOOP_TYPE);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void setSelectionManager(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }


    public ApplicationConfiguration getApplicationConfig() {
        return applicationConfig;
    }


    public void setApplicationConfig(ApplicationConfiguration applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

}
