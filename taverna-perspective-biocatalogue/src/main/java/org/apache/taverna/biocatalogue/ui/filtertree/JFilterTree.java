package org.apache.taverna.biocatalogue.ui.filtertree;
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

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.biocatalogue.ui.tristatetree.JTriStateTree;
import org.apache.taverna.biocatalogue.ui.tristatetree.TriStateTreeNode;

/**
 * This subclass of {@link JTriStateTree} provides custom behaviour
 * for tooltips: ontological terms will now always get a tooltip that
 * displays the namespace for the tag, but plain text tags will still
 * behave as before - the way it is defined in the superclass (so that
 * the tooltip will only be shown if the tag does not fully fit into
 * the visible part of the {@link FilterTreePane}.
 * 
 * @author Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public class JFilterTree extends JTriStateTree
{
  
  private static Map<String, String> nameSpaceToOntologyMap = new HashMap<String, String>(){
      {
          put("http://www.mygrid.org.uk/ontology", "mygrid-domain-ontology");
          put("http://www.mygrid.org.uk/mygrid-moby-service", "mygrid-service-ontology");
      }
  };
 

  public JFilterTree(TriStateTreeNode root) {
    super(root);
  }
  
  
  public String getToolTipText(MouseEvent e)
  {
    Object correspondingObject = super.getTreeNodeObject(e);
    if (correspondingObject != null && correspondingObject instanceof FilterTreeNode) {
      FilterTreeNode filterNode = (FilterTreeNode) correspondingObject;
      
      if (filterNode.isTagWithNamespaceNode())
      {
        String nameAndNamespace = filterNode.getUrlValue().substring(1, filterNode.getUrlValue().length() - 1);
        String[] namePlusNamespace = nameAndNamespace.split("#");
        
        return ("<html>" + namePlusNamespace[1] + " (<b>Namespace: </b>" + namePlusNamespace[0] + ")</html>");
      }
    }
    
    return super.getToolTipText(e);
  }
  
  public static String getOntologyFromNamespace(String namespace){
	  if (namespace == null){
		  return null;
	  }
	  else{
		  if (nameSpaceToOntologyMap.containsKey(namespace)){
			  return nameSpaceToOntologyMap.get(namespace);
		  }
		  else{
			  return null;
		  }
	  }
  }
  
}
