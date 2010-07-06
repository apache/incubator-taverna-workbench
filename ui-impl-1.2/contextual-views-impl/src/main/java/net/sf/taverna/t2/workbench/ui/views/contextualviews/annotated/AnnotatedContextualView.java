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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.annotated;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.annotation.annotationbeans.AbstractTextualValueAssertion;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.log4j.Logger;

/**
 * 
 * This is a ContextualView that should be able to display and allow editing of
 * Annotation information for any Annotated. At the moment it is only used for
 * Dataflow.
 * 
 * @author Alan R Williams
 * 
 */
@SuppressWarnings("serial")
public class AnnotatedContextualView extends ContextualView {

	private static Logger logger = Logger
			.getLogger(AnnotatedContextualView.class);

	private AnnotationTools annotationTools = new AnnotationTools();
	
	/**
	 * The object to which the Annotations apply
	 */
	private Annotated<?> annotated;
	private JPanel annotatedView;

	private PropertyResourceBundle prb;

	private Map<Class<?>, DialogTextArea> classToAreaMap;
	private Map<Class<?>, String> classToCurrentValueMap;

	private static String MISSING_VALUE = "Type here to give details";

	private static int DEFAULT_AREA_WIDTH = 29;
	private static int DEFAULT_AREA_ROWS = 5;
	
	private FileManager fileManager = FileManager.getInstance();
	private EditManager editManager = EditManager.getInstance();


	@SuppressWarnings("unchecked")
	private static Map<Annotated, JPanel> annotatedToPanelMap = new HashMap<Annotated, JPanel>();


	
	public AnnotatedContextualView(Annotated<?> annotated) {
		super();
		prb = (PropertyResourceBundle) ResourceBundle
				.getBundle("annotatedcontextualview");
		this.annotated = annotated;
		classToAreaMap = new HashMap<Class<?>, DialogTextArea>();
		classToCurrentValueMap = new HashMap<Class<?>, String>();
		
		initView();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#
	 * getMainFrame()
	 */
	@Override
	public JComponent getMainFrame() {
		refreshView();
		annotatedView.setVisible(true);
		annotatedView.requestFocus();
		return annotatedView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#
	 * getViewTitle()
	 */
	@Override
	public String getViewTitle() {
		return "Annotations";
	}

	public void refreshView() {
		if (annotatedToPanelMap.containsKey(annotated)) {
			annotatedView = annotatedToPanelMap.get(annotated);
		} else {
			annotatedView = new JPanel();
			annotatedToPanelMap.put(annotated, annotatedView);
			annotatedView.setLayout(new BoxLayout(annotatedView,
					BoxLayout.Y_AXIS));
			JPanel scrollPanel = new JPanel();
			scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));
			annotatedView.setBorder(new EmptyBorder(5, 5, 5, 5));
			for (Class<?> c : annotationTools.getAnnotatingClasses(annotated)) {
				String name = "";
				try {
					name = prb.getString(c.getCanonicalName());
				} catch (MissingResourceException e) {
					name = c.getCanonicalName();
				}
				JPanel subPanel = new JPanel();
				subPanel.setBorder(new TitledBorder(name));
				String value = annotationTools.getAnnotationString(annotated, c, MISSING_VALUE);
				subPanel.add(createTextArea(c, value));
				scrollPanel.add(subPanel);
			}
			JScrollPane scrollPane = new JScrollPane(scrollPanel);
			annotatedView.add(scrollPane);
		}
	}
	

	
	private DialogTextArea createTextArea(final Class<?> c, final String value) {
		classToCurrentValueMap.put(c, value);
		DialogTextArea area = new DialogTextArea(value);
		area.setFocusable(true);
		area.addFocusListener(new TextAreaFocusListener(area, c));
		area.setColumns(DEFAULT_AREA_WIDTH);
		area.setRows(DEFAULT_AREA_ROWS);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		classToAreaMap.put(c, area);
		logger.info("Adding to map " + c.getCanonicalName() + "("
				+ c.hashCode() + ") to " + area.hashCode());
		return area;
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

		public void focusGained(FocusEvent e) {
			if (area.getText().equals(MISSING_VALUE)) {
				area.setText("");
			}
		}

		public void focusLost(FocusEvent e) {
			String currentValue = area.getText();
			if (currentValue.equals("") || currentValue.equals(MISSING_VALUE)) {
				currentValue = MISSING_VALUE;
				area.setText(currentValue);
			}
			if (!currentValue.equals(oldValue)) {
				if (currentValue == MISSING_VALUE) {
					currentValue = "";
				}
				try {
					Edits edits = EditsRegistry.getEdits();
					Dataflow currentDataflow = fileManager.getCurrentDataflow();
					List<Edit<?>> editList = new ArrayList<Edit<?>>();
					editList.add(annotationTools.setAnnotationString(annotated, annotationClass, currentValue));
					if ((annotated == currentDataflow) && (prb.getString(annotationClass.getCanonicalName()).equals("Title"))) {
						editList.add(edits.getUpdateDataflowNameEdit(currentDataflow,
								sanitiseName(currentValue)));
					}
					editManager.doDataflowEdit(currentDataflow, new CompoundEdit(editList));
				} catch (EditException e1) {
					logger.warn("Can't set annotation", e1);
				}
				oldValue = area.getText();
			}
		}

	}

	/**
	 * Checks that the name does not have any characters that are invalid for a
	 * processor name.
	 * 
	 * The name must contain only the chars[A-Za-z_0-9].
	 * 
	 * @param name
	 *            the original name
	 * @return the sanitised name
	 */
	private static String sanitiseName(String name) {
		String result = name;
		if (Pattern.matches("\\w++", name) == false) {
			result = "";
			for (char c : name.toCharArray()) {
				if (Character.isLetterOrDigit(c) || c == '_') {
					result += c;
				} else {
					result += "_";
				}
			}
		}
		return result;
	}

	@Override
	public int getPreferredPosition() {
		return 500;
	}



}
