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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.SpiRegistry;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationBeanSPI;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.AppliesTo;
import net.sf.taverna.t2.annotation.annotationbeans.AbstractTextualValueAssertion;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

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
public class AnnotatedContextualView extends ContextualView {

	private static Logger logger = Logger
			.getLogger(AnnotatedContextualView.class);

	/**
	 * The object to which the Annotations apply
	 */
	private Annotated<?> annotated;
	private JPanel annotatedView;

	private SpiRegistry registry;

	private PropertyResourceBundle prb;

	private Map<Class<?>, JTextArea> classToAreaMap;
	private Map<Class<?>, String> classToCurrentValueMap;

	private static String MISSING_VALUE = "Unspecified";

	private static int DEFAULT_AREA_WIDTH = 29;
	
	private FileManager fileManager = FileManager.getInstance();
	private EditManager editManager = EditManager.getInstance();


	private static Map<Annotated, JPanel> annotatedToPanelMap = new HashMap<Annotated, JPanel>();

	public AnnotatedContextualView(Annotated<?> annotated) {
		super();
		prb = (PropertyResourceBundle) ResourceBundle
				.getBundle("annotatedcontextualview");
		this.annotated = annotated;
		classToAreaMap = new HashMap<Class<?>, JTextArea>();
		classToCurrentValueMap = new HashMap<Class<?>, String>();
		registry = new SpiRegistry(getRepository(),
				"net.sf.taverna.t2.annotation.AnnotationBeanSPI", this
						.getClass().getClassLoader());
		initView();
	}

	private Repository getRepository() {
		if (this.getClass().getClassLoader() instanceof LocalArtifactClassLoader) {
			return ((LocalArtifactClassLoader) this.getClass().getClassLoader())
					.getRepository();
		} else {
			return ApplicationRuntime.getInstance().getRavenRepository();
		}
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
			for (Class<?> c : getAnnotatingClasses()) {
				String name = "";
				try {
					name = prb.getString(c.getCanonicalName());
				} catch (MissingResourceException e) {
					name = c.getCanonicalName();
				}
				JPanel subPanel = new JPanel();
				subPanel.setBorder(new TitledBorder(name));
				String value = getAnnotationString(c);
				subPanel.add(createTextArea(c, value));
				scrollPanel.add(subPanel);
			}
			JScrollPane scrollPane = new JScrollPane(scrollPanel);
			annotatedView.add(scrollPane);
		}
	}

	private List<Class> getAnnotatingClasses() {
		List<Class> result = new ArrayList<Class>();
		for (Class<?> c : registry.getClasses()) {
			if (AbstractTextualValueAssertion.class.isAssignableFrom(c)) {
				AppliesTo appliesToAnnotation = (AppliesTo) c
						.getAnnotation(AppliesTo.class);
				if (appliesToAnnotation != null) {
					Class<?>[] targets = appliesToAnnotation
							.targetObjectType();
					for (Class<?> target : targets) {
						if (target.isInstance(annotated)) {
							result.add(c);
						}
					}
				}
			}
		}
		return result;
	}
	
	private JTextArea createTextArea(final Class<?> c, final String value) {
		classToCurrentValueMap.put(c, value);
		JTextArea area = new JTextArea(value);
		area.setFocusable(true);
		area.addFocusListener(new TextAreaFocusListener(area, c));
		area.setColumns(DEFAULT_AREA_WIDTH);
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
		JTextArea area = null;

		public TextAreaFocusListener(JTextArea area, Class<?> c) {
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
					Edit<?> edit = setAnnotationString(annotationClass, currentValue);
					editManager.doDataflowEdit(fileManager.getCurrentDataflow(), edit);
				} catch (EditException e1) {
					logger.warn("Can't set annotation", e1);
				}
				oldValue = area.getText();
			}
		}

	}

	private Edit<?> setAnnotationString(Class<?> c, String value) {
		AbstractTextualValueAssertion a = null;
		try {
			logger.info("Setting " + c.getCanonicalName() + " to " + value);
			a = (AbstractTextualValueAssertion) c.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		a.setText(value);
		return (addAnnotation(a));

	}

	private Edit<?> addAnnotation(AnnotationBeanSPI a) {
		return EditsRegistry.getEdits().getAddAnnotationChainEdit(annotated, a);
	}

	private String getAnnotationString(Class annotationClass) {
		AbstractTextualValueAssertion a = (AbstractTextualValueAssertion) getAnnotation(annotationClass);
		String value = (a == null) ? MISSING_VALUE : a.getText();
		return value;
	}

	private AnnotationBeanSPI getAnnotation(Class annotationClass) {
		AnnotationBeanSPI result = null;
		Date latestDate = null;
		for (AnnotationChain chain : annotated.getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				AnnotationBeanSPI detail = assertion.getDetail();
				if (annotationClass.isInstance(detail)) {
					Date assertionDate = assertion.getCreationDate();
					if ((latestDate == null)
							|| latestDate.before(assertionDate)) {
						result = detail;
						latestDate = assertionDate;
					}
				}
			}
		}

		return result;
	}

}
