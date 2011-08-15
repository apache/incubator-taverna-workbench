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
package net.sf.taverna.t2.workbench.run;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;

public class ResultsPerspective implements PerspectiveSPI {

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private DataflowSelectionManager dataflowSelectionManager;
	private XMLDeserializer xmlDeserializer;
	private ReferenceService referenceService;

	private ResultsPerspectiveComponent resultsPerspectiveComponent;

	@Override
	public String getID() {
		return this.getClass().getName();
	}

	@Override
	public JComponent getPanel() {
		if (resultsPerspectiveComponent == null) {
			resultsPerspectiveComponent = ResultsPerspectiveComponent.getInstance();
			resultsPerspectiveComponent.setEditManager(editManager);
			resultsPerspectiveComponent.setFileManager(fileManager);
			resultsPerspectiveComponent.setMenuManager(menuManager);
			resultsPerspectiveComponent.setDataflowSelectionManager(dataflowSelectionManager);
			resultsPerspectiveComponent.setXmlDeserializer(xmlDeserializer);
			resultsPerspectiveComponent.setReferenceService(referenceService);
		}
		return resultsPerspectiveComponent ;
	}

	@Override
	public ImageIcon getButtonIcon() {
		return WorkbenchIcons.resultsPerspectiveIcon;
	}

	@Override
	public String getText() {
		return "Results";
	}

	@Override
	public int positionHint() {
		// TODO Auto-generated method stub
		return 30;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setDataflowSelectionManager(DataflowSelectionManager dataflowSelectionManager) {
		this.dataflowSelectionManager = dataflowSelectionManager;
	}

	public void setXmlDeserializer(XMLDeserializer xmlDeserializer) {
		this.xmlDeserializer = xmlDeserializer;
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

}
