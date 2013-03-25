/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 *
 * @author David Withers
 */
public class ValidatingTextGroup {
	private Map<ValidatingTextField, DocumentListener> textComponents;

	public ValidatingTextGroup() {
		textComponents = new HashMap<ValidatingTextField, DocumentListener>();
	}

	public void addValidTextComponent(ValidatingTextField textComponent) {
		setUniqueText(textComponent);
		DocumentListener documentListener = new ValidatorDocumentListener();
		textComponent.getDocument().addDocumentListener(documentListener);
		textComponents.put(textComponent, documentListener);
	}

	public void addTextComponent(ValidatingTextField textComponent) {
		DocumentListener documentListener = new ValidatorDocumentListener();
		textComponent.getDocument().addDocumentListener(documentListener);
		textComponents.put(textComponent, documentListener);
		validate();
	}

	public void removeTextComponent(ValidatingTextField textComponent) {
		textComponent.getDocument().removeDocumentListener(textComponents.remove(textComponent));
		validate();
	}

	private void setUniqueText(ValidatingTextField textComponent) {
		String text = textComponent.getText();
		if (textExists(text)) {
			// Remove any existing number suffix
			String nameTemplate = text.replaceAll("_\\d+$", "_");
			long i = 1;
			do {
				text = nameTemplate + i++;
			} while (textExists(text));

			textComponent.setText(text);
		}
	}

	private void validate() {
		Map<String, ValidatingTextField> textValues = new HashMap<String, ValidatingTextField>();
		Set<ValidatingTextField> maybeValid = new HashSet<ValidatingTextField>();
		for (ValidatingTextField textComponent : textComponents.keySet()) {
			ValidatingTextField duplicate = textValues.get(textComponent.getText());
			if (duplicate != null) {
				duplicate.setValid(false);
				maybeValid.remove(duplicate);
				textComponent.setValid(false);
			} else {
				textValues.put(textComponent.getText(), textComponent);
				maybeValid.add(textComponent);
			}
		}
		for (ValidatingTextField textComponent : maybeValid) {
			textComponent.setValid(true);
		}
	}

	private boolean textExists(String text) {
		for (ValidatingTextField currentTextComponent : textComponents.keySet()) {
			if (text.equals(currentTextComponent.getText())) {
				return true;
			}
		}
		return false;
	}

	class ValidatorDocumentListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			validate();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			validate();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			validate();
		}

	}
}
