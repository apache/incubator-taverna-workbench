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
package org.apache.taverna.workbench.ui.views.contextualviews.annotated;

import static javax.swing.BoxLayout.Y_AXIS;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.taverna.annotation.Annotated;
import org.apache.taverna.annotation.AnnotationBeanSPI;
import org.apache.taverna.lang.ui.DialogTextArea;
import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.impl.ContextualViewComponent;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * This is a ContextualView that should be able to display and allow editing of
 * Annotation information for any Annotated. At the moment it is only used for
 * Dataflow.
 * 
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class AnnotatedContextualView extends ContextualView {
	private static final int WORKFLOW_NAME_LENGTH = 20;
	public static final String VIEW_TITLE = "Annotations";
	private final static String MISSING_VALUE = "Type here to give details";
	private final static int DEFAULT_AREA_WIDTH = 60;
	private final static int DEFAULT_AREA_ROWS = 8;

	private static Logger logger = Logger
			.getLogger(AnnotatedContextualView.class);
	private static PropertyResourceBundle prb = (PropertyResourceBundle) ResourceBundle
			.getBundle("annotatedcontextualview");

	// TODO convert to scufl2
	// private static AnnotationTools annotationTools = new AnnotationTools();

	/**
	 * The object to which the Annotations apply
	 */
	private Annotated<?> annotated;
	private SelectionManager selectionManager;
	private EditManager editManager;
	private boolean isStandalone = false;
	private JPanel panel;
	@SuppressWarnings("unused")
	private final List<AnnotationBeanSPI> annotationBeans;

	public AnnotatedContextualView(Annotated<?> annotated,
			EditManager editManager, SelectionManager selectionManager,
			List<AnnotationBeanSPI> annotationBeans) {
		super();
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		this.annotationBeans = annotationBeans;
		this.annotated = annotated;

		initialise();
		initView();
	}

	@Override
	public void refreshView() {
		initialise();
	}

	private void initialise() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, Y_AXIS));
		} else
			panel.removeAll();
		populatePanel();
		revalidate();
	}

	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public String getViewTitle() {
		return VIEW_TITLE;
	}

	private Map<String,String> getAnnotations() {
		// TODO convert to scufl2
		Map<String, String> result = new HashMap<>();
		//for (Class<?> c : annotationTools.getAnnotatingClasses(annotated)) {
		// String name = "";
		// try {
		// name = prb.getString(c.getCanonicalName());
		// } catch (MissingResourceException e) {
		// name = c.getCanonicalName();
		// }
		// String value = annotationTools.getAnnotationString(annotated, c,
		// MISSING_VALUE);
		// result.put(name,value);
		//}
		return result;
	}
	public void populatePanel() {
		JPanel scrollPanel = new JPanel();
		scrollPanel.setLayout(new BoxLayout(scrollPanel, Y_AXIS));
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		Map<String,String>annotations = getAnnotations();
		for (String name : annotations.keySet()) {
			JPanel subPanel = new JPanel();
			subPanel.setBorder(new TitledBorder(name));
			subPanel.add(createTextArea(String.class, annotations.get(name)));
			scrollPanel.add(subPanel);
		}
		JScrollPane scrollPane = new JScrollPane(scrollPanel);
		panel.add(scrollPane);
	}

	private JScrollPane createTextArea(Class<?> c, String value) {
		DialogTextArea area = new DialogTextArea(value);
		area.setFocusable(true);
		area.addFocusListener(new TextAreaFocusListener(area, c));
		area.setColumns(DEFAULT_AREA_WIDTH);
		area.setRows(DEFAULT_AREA_ROWS);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);

		return new JScrollPane(area);
	}

	private class TextAreaFocusListener implements FocusListener {
		String oldValue = null;
		Class<?> annotationClass;
		DialogTextArea area = null;

		public TextAreaFocusListener(DialogTextArea area, Class<?> c) {
			annotationClass = c;
			oldValue = area.getText();
			this.area = area;
		}

		@Override
		public void focusGained(FocusEvent e) {
			if (area.getText().equals(MISSING_VALUE))
				area.setText("");
		}

		@Override
		public void focusLost(FocusEvent e) {
			String currentValue = area.getText();
			if (currentValue.isEmpty() || currentValue.equals(MISSING_VALUE)) {
				currentValue = MISSING_VALUE;
				area.setText(currentValue);
			}
			if (!currentValue.equals(oldValue)) {
				if (currentValue == MISSING_VALUE)
					currentValue = "";
				try {
					WorkflowBundle currentDataflow = selectionManager
							.getSelectedWorkflowBundle();
					List<Edit<?>> editList = new ArrayList<>();
					addWorkflowNameEdits(currentValue, currentDataflow,
							editList);
					if (!isStandalone)
						ContextualViewComponent.selfGenerated = true;
					editManager.doDataflowEdit(currentDataflow,
							new CompoundEdit(editList));
					ContextualViewComponent.selfGenerated = false;
				} catch (EditException e1) {
					logger.warn("Can't set annotation", e1);
				}
				oldValue = area.getText();
			}
		}

		private boolean isTitleAnnotation() {
			// TODO convert to scufl2
			return prb.getString(annotationClass.getCanonicalName()).equals(
					"Title");
		}

		// TODO convert to scufl2
		private void addWorkflowNameEdits(String currentValue,
				WorkflowBundle currentDataflow, List<Edit<?>> editList) {
			//editList.add(annotationTools.setAnnotationString(annotated,
			//		annotationClass, currentValue, edits));
			if (annotated == currentDataflow && isTitleAnnotation()
					&& !currentValue.isEmpty()) {
				@SuppressWarnings("unused")
				String sanitised = sanitiseName(currentValue);
				//editList.add(edits.getUpdateDataflowNameEdit(currentDataflow,
				//		sanitised));
			}
		}
	}

	/**
	 * Checks that the name does not have any characters that are invalid for a
	 * processor name.
	 * <p>
	 * The resulting name must contain only the chars [A-Za-z_0-9].
	 * 
	 * @param name
	 *            the original name
	 * @return the sanitised name
	 */
	private static String sanitiseName(String name) {
		if (name.length() > WORKFLOW_NAME_LENGTH)
			name = name.substring(0, WORKFLOW_NAME_LENGTH);
		if (Pattern.matches("\\w++", name))
			return name;
		StringBuilder temp = new StringBuilder();
		for (char c : name.toCharArray())
			temp.append(Character.isLetterOrDigit(c) || c == '_' ? c : '_');
		return temp.toString();
	}

	@Override
	public int getPreferredPosition() {
		return 500;
	}
}
