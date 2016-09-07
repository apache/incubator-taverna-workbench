package org.apache.taverna.ui.menu.items.annotated;
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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.taverna.annotation.Annotated;
import org.apache.taverna.annotation.AnnotationBeanSPI;
import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.ui.menu.items.contextualviews.ConfigureSection;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.annotated.AnnotatedContextualView;

public class AnnotatedConfigureMenuAction extends AbstractContextualMenuAction {
	private static final String ANNOTATE = "Annotate...";
	private EditManager editManager;
	private SelectionManager selectionManager;
	private List<AnnotationBeanSPI> annotationBeans;

	public AnnotatedConfigureMenuAction() {
		super(ConfigureSection.configureSection, 40);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && (getContextualSelection().getSelection() instanceof Annotated);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction(ANNOTATE) {
			public void actionPerformed(ActionEvent e) {
				AnnotatedContextualView view = new AnnotatedContextualView((Annotated) getContextualSelection().getSelection(),
						editManager, selectionManager, annotationBeans);
				JOptionPane.showMessageDialog(null, view.getMainFrame(), "Annotation", JOptionPane.PLAIN_MESSAGE);
			}
		};
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setAnnotationBeans(List<AnnotationBeanSPI> annotationBeans) {
		this.annotationBeans = annotationBeans;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
