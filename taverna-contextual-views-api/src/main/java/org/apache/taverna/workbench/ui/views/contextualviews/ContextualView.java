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
package org.apache.taverna.workbench.ui.views.contextualviews;

import static java.awt.BorderLayout.CENTER;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * An abstract class defining the base container to hold a contextual view over
 * Dataflow element.
 * <p>
 * The specific implementation of this class to support a given dataflow element
 * needs to implement the {@link #getMainFrame()} and {@link #getViewTitle()}.
 * <p>
 * If a view is associated with an action handler to configure this component,
 * then the {@link #getConfigureAction(Frame) getConfigureAction} handler must
 * be over-ridden. If this returns null then the configure button is left
 * disabled and it is not possible to configure the element.
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public abstract class ContextualView extends JPanel {
	/**
	 * When implemented, this method should define the main frame that is placed
	 * in this container, and provides a static view of the Dataflow element.
	 * 
	 * @return a JComponent that represents the dataflow element.
	 */
	public abstract JComponent getMainFrame();

	/**
	 * @return a String providing a title for the view
	 */
	public abstract String getViewTitle();

	/**
	 * Allows the item to be configured, but returning an action handler that
	 * will be invoked when selecting to configure. By default this is provided
	 * by a button.
	 * <p>
	 * If there is no ability to configure the given item, then this should
	 * return null.
	 * 
	 * @param owner
	 *            the owning dialog to be used when displaying dialogues for
	 *            configuration options
	 * @return an action that allows the element being viewed to be configured.
	 */
	public Action getConfigureAction(Frame owner) {
		return null;
	}

	/**
	 * This <i>must</i> be called by any sub-classes after they have initialised
	 * their own view since it gets their main panel and adds it to the main
	 * contextual view. If you don't do this you will get a very empty frame
	 * popping up!
	 */
	public void initView() {
		setLayout(new BorderLayout());
		add(getMainFrame(), CENTER);
		setName(getViewTitle());
	}

	public abstract void refreshView();

	public abstract int getPreferredPosition();

	public static String getTextFromDepth(String kind, Integer depth) {
		String labelText = "The last prediction said the " + kind;
		if (depth == null) {
			labelText += " would not transmit a value";
		} else if (depth == -1) {
			labelText += " was invalid/unpredicted";
		} else if (depth == 0) {
			labelText += " would carry a single value";
		} else {
			labelText += " would carry a list of depth " + depth;
		}
		return labelText;
	}
}
