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
package net.sf.taverna.t2.activities.stringconstant.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.activities.stringconstant.views.StringConstantConfigView;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import org.apache.taverna.commons.services.ServiceRegistry;
import org.apache.taverna.scufl2.api.activity.Activity;

public class StringConstantActivityConfigurationAction extends
		ActivityConfigurationAction {
	private static final long serialVersionUID = 2518716617809186972L;
	public static final String CONFIGURE_STRINGCONSTANT = "Edit value";

	private final EditManager editManager;
	private final FileManager fileManager;
	private final ServiceRegistry serviceRegistry;

	public StringConstantActivityConfigurationAction(Activity activity,
			Frame owner, EditManager editManager, FileManager fileManager,
			ActivityIconManager activityIconManager,
			ServiceDescriptionRegistry serviceDescriptionRegistry,
			ServiceRegistry serviceRegistry) {
		super(activity, activityIconManager, serviceDescriptionRegistry);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.serviceRegistry = serviceRegistry;
		putValue(NAME, CONFIGURE_STRINGCONSTANT);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog currentDialog = getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}

		StringConstantConfigView configView = new StringConstantConfigView(
				activity, serviceRegistry);
		ActivityConfigurationDialog dialog = new ActivityConfigurationDialog(
				getActivity(), configView, editManager);
		setDialog(getActivity(), dialog, fileManager);
	}
}
