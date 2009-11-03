/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.views.graph.config;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;

/**
 * Configuration for the GraphViewComponent.
 *
 * @author David Withers
 */
public class GraphViewConfiguration  extends AbstractConfigurable {

    public static final String PORT_STYLE = "portStyle";

    public static final String ALIGNMENT = "alignment";

    public static final String ANIMATION_ENABLED = "animationEnabled";

    public static final String ANIMATION_SPEED = "animationSpeed";

    private static GraphViewConfiguration instance;

    private Map<String, String> defaultPropertyMap;
    
    public static GraphViewConfiguration getInstance() {
        if (instance == null) {
            instance = new GraphViewConfiguration();
        }
        return instance;
    }

    private GraphViewConfiguration() {
    }

    public String getCategory() {
        return "general";
    }

    public Map<String, String> getDefaultPropertyMap() {
        if (defaultPropertyMap == null) {
            defaultPropertyMap = new HashMap<String, String>();
            defaultPropertyMap.put(PORT_STYLE, PortStyle.NONE.toString());
            defaultPropertyMap.put(ALIGNMENT, Alignment.VERTICAL.toString());
            defaultPropertyMap.put(ANIMATION_ENABLED, "false");
            defaultPropertyMap.put(ANIMATION_SPEED, "800");
        }
        return defaultPropertyMap;
    }

    public String getName() {
        return "Diagram";
    }

    public String getUUID() {
        return "3686BA31-449F-4147-A8AC-0C3F63AFC68F";
    }

    
}
