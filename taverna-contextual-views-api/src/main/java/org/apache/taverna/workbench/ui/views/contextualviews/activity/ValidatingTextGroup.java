/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.views.contextualviews.activity;

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
		textComponents = new HashMap<>();
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
		textComponent.getDocument().removeDocumentListener(
				textComponents.remove(textComponent));
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
		Map<String, ValidatingTextField> textValues = new HashMap<>();
		Set<ValidatingTextField> maybeValid = new HashSet<>();
		for (ValidatingTextField textComponent : textComponents.keySet()) {
			ValidatingTextField duplicate = textValues.get(textComponent
					.getText());
			if (duplicate != null) {
				duplicate.setValid(false);
				maybeValid.remove(duplicate);
				textComponent.setValid(false);
			} else {
				textValues.put(textComponent.getText(), textComponent);
				maybeValid.add(textComponent);
			}
		}
		for (ValidatingTextField textComponent : maybeValid)
			textComponent.setValid(true);
	}

	private boolean textExists(String text) {
		for (ValidatingTextField currentTextComponent : textComponents.keySet())
			if (text.equals(currentTextComponent.getText()))
				return true;
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
