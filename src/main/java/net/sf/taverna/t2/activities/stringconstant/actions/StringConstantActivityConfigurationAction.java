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
import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.activities.stringconstant.servicedescriptions.StringConstantActivityIcon;
import net.sf.taverna.t2.activities.stringconstant.servicedescriptions.StringConstantTemplateService;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.activities.stringconstant.views.StringConstantConfigView;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.property.MultiplePropertiesException;
import uk.org.taverna.scufl2.api.property.PropertyException;
import uk.org.taverna.scufl2.api.property.PropertyNotFoundException;
import uk.org.taverna.scufl2.api.property.UnexpectedPropertyException;

public class StringConstantActivityConfigurationAction extends ActivityConfigurationAction {

	public static final String CONFIGURE_STRINGCONSTANT = "Edit value";

	private static final long serialVersionUID = 2518716617809186972L;
	private final Frame owner;

	private final EditManager editManager;

	private final FileManager fileManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public StringConstantActivityConfigurationAction(Activity activity, Frame owner,
			EditManager editManager, FileManager fileManager,
			ActivityIconManager activityIconManager) {
		super(activity, activityIconManager);
		this.editManager = editManager;
		this.fileManager = fileManager;
		putValue(Action.NAME, CONFIGURE_STRINGCONSTANT);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog currentDialog = ActivityConfigurationAction.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}

//		URI activityURI = StringConstantTemplateService.ACTIVITY_TYPE;
//		Configuration configuration = new Configuration();
//		configuration.setConfigurableType(activityURI.resolve("#Config"));
//		configuration.setConfigures(activity);
//		configuration.getPropertyResource().addPropertyAsString(StringConstantTemplateService.ACTIVITY_TYPE.resolve("#string"), "");

		Configuration currentConfiguration = scufl2Tools.configurationFor(activity, activity.getParent());
//		String value;
//		try {
//			value = currentConfiguration.getPropertyResource().getPropertyAsString(activityURI.resolve("#string"));
//		} catch (PropertyException e1) {
//			value = "ERROR";
//		}

//		WorkflowBundle workflowBundle = fileManager.getCurrentDataflow();
		final StringConstantConfigView stringConstantConfigView = new StringConstantConfigView(getActivity(), currentConfiguration);
		final ActivityConfigurationDialog dialog = new ActivityConfigurationDialog(getActivity(), stringConstantConfigView, editManager, fileManager);

		ActivityConfigurationAction.setDialog(getActivity(), dialog, fileManager);
//		if (newValue != null) {
//			try {
//				configuration.getPropertyResource().getPropertyAsLiteral(activityURI.resolve("#string")).setLiteralValue(newValue);
//			} catch (UnexpectedPropertyException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (PropertyNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (MultiplePropertiesException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			ActivityConfigurationDialog.configureActivityStatic(workflowBundle, activity, configuration, editManager);
//		}
	}

}
