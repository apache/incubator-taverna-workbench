/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package org.apache.taverna.workbench.iterationstrategy.editor;

import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
//import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;
import org.apache.taverna.workflowmodel.processor.iteration.AbstractIterationStrategyNode;
import org.apache.taverna.workflowmodel.processor.iteration.IterationStrategy;
import org.apache.taverna.workflowmodel.processor.iteration.NamedInputPortNode;
import org.apache.taverna.workflowmodel.processor.iteration.TerminalNode;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class IterationStrategyEditor extends IterationStrategyTree implements
		UIComponentSPI {

	private static Logger logger = Logger
			.getLogger(IterationStrategyEditor.class);

	//private BufferedImage imgGhost; // The 'drag image'

	// mouse was clicked

	public IterationStrategyEditor() {
		super();
		// Make this a drag source
		if (!GraphicsEnvironment.isHeadless()) {
			this.setDragEnabled(true);  
	        this.setDropMode(DropMode.ON_OR_INSERT);  
	        this.setTransferHandler(new TreeTransferHandler());  
	        this.getSelectionModel().setSelectionMode(  
	                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);  
	        expandTree();
		}

		//
	}

	public IterationStrategyEditor(IterationStrategy theStrategy) {
		this();
		setIterationStrategy(theStrategy);
	}

    /**
     * 
     * This code is freely adapted from code derived 
     *
     */
    class TreeTransferHandler extends TransferHandler {  
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];  
       
        public TreeTransferHandler() {
        	getNodesFlavor();
          }
        
        private DataFlavor getNodesFlavor() {
        	if (nodesFlavor == null) {
                try {  
                     nodesFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                             ";class=" + AbstractIterationStrategyNode.class.getName(),
                             "AbstractIterationStrategyNode",
                             this.getClass().getClassLoader());  
                    flavors[0] = nodesFlavor;  
                } catch(Exception e) {  
                    logger.error("Problem creating nodesFlavor:" + e);
                }         		
        	}
        	return nodesFlavor;
        }
       
        public boolean canImport(TransferHandler.TransferSupport support) {  
            if(!support.isDrop()) {
            	logger.error("isDrop not supported");
                return false;  
            }  

            if(!support.isDataFlavorSupported(getNodesFlavor())) {
            	logger.info("Not correct flavor");
                return false;  
            }  
            // Do not allow a drop on the drag source selections.  
            JTree.DropLocation dl =  
                    (JTree.DropLocation)support.getDropLocation();  
            TreePath dest = dl.getPath();  
            AbstractIterationStrategyNode destination =  
                (AbstractIterationStrategyNode)dest.getLastPathComponent();
            Transferable t = support.getTransferable();
            if (destination instanceof TerminalNode) {
            	return false;
            }
            try {
				AbstractIterationStrategyNode node = (AbstractIterationStrategyNode) t.getTransferData(getNodesFlavor());
				if (node.isNodeDescendant(destination)) {
					return false;
				}
			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (IOException e) {
				return false;
			}  
//			JTree tree = (JTree) support.getComponent();
//			int dropRow = tree.getRowForPath(dl.getPath());
//			int selRow = tree.getLeadSelectionRow();
//			if (selRow == dropRow) {
//				logger.info("Dragging to source");
//				return false;
//
//			}
			support.setShowDropLocation(true);  
            return true;  
        }  
       
         protected Transferable createTransferable(JComponent c) {  
            JTree tree = (JTree)c;  
            TreePath[] paths = tree.getSelectionPaths();  
            if(paths != null) {  
                AbstractIterationStrategyNode node =  
                    (AbstractIterationStrategyNode)paths[0].getLastPathComponent();  
                 return new NodeTransferable(node);  
            }  
            return null;  
        }  
              
        protected void exportDone(JComponent source, Transferable data, int action) {  
        }  
       
        public int getSourceActions(JComponent c) {  
            return MOVE;  
        }  
       
        public boolean importData(TransferHandler.TransferSupport support) { 
            if(!canImport(support)) {
            	logger.info("Cannot import");
                return false;  
            }  
            // Extract transfer data.  
            AbstractIterationStrategyNode node = null;  
            try {  
                Transferable t = support.getTransferable();  
                node = (AbstractIterationStrategyNode) t.getTransferData(getNodesFlavor());  
            } catch(UnsupportedFlavorException ufe) {  
                logger.error("UnsupportedFlavor", ufe);  
            } catch (Exception e) {
            	logger.error("Problem getting transfer data", e);
            }

           // Get drop location info.  
            JTree.DropLocation dl =  
                    (JTree.DropLocation)support.getDropLocation();  
            int childIndex = dl.getChildIndex();  
            TreePath dest = dl.getPath();  
            AbstractIterationStrategyNode parent =  
                (AbstractIterationStrategyNode)dest.getLastPathComponent();
            int index = childIndex;
            logger.info ("parent is a " + parent.getClass().getName());
            if (parent instanceof NamedInputPortNode) {
            	AbstractIterationStrategyNode sibling = parent;
            	parent = (AbstractIterationStrategyNode) sibling.getParent();
            	index = parent.getIndex(sibling);
            } else if (index == -1) {
            	index = parent.getChildCount();
            }
            if (parent instanceof TerminalNode) {
            	if (parent.getChildCount() > 0) {
            		parent = (AbstractIterationStrategyNode) parent.getChildAt(0);
            		index = parent.getChildCount();
            	}
            	
            }
            logger.info("parent is a " + parent.getClass().getName());

			try {
				// The parent insert removes from the oldParent
				parent.insert(node, index++);
				DefaultTreeModel model = IterationStrategyEditor.this
						.getModel();
				refreshModel();
			} catch (IllegalStateException e) {
				logger.error(e);
			} catch (IllegalArgumentException e) {
				logger.error(e);
			}
          return true;  
        }  
       
        public String toString() {  
            return getClass().getName();  
        }  
       
        public class NodeTransferable implements Transferable {  
            AbstractIterationStrategyNode node;  
       
            public NodeTransferable(AbstractIterationStrategyNode node) {  
                this.node = node;  
             }  
       
            public AbstractIterationStrategyNode getTransferData(DataFlavor flavor)  
                                     throws UnsupportedFlavorException {  
                if(!isDataFlavorSupported(flavor))  
                    throw new UnsupportedFlavorException(flavor);  
                return node;  
            }  
       
            public DataFlavor[] getTransferDataFlavors() {  
                return flavors;  
            }  
       
            public boolean isDataFlavorSupported(DataFlavor flavor) {
            	 return getNodesFlavor().equals(flavor);  
            }  
        }  
    }
}
