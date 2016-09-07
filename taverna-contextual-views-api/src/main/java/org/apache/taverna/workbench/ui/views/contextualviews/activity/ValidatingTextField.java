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
/*

package org.apache.taverna.workbench.ui.views.contextualviews.activity;

import javax.swing.JTextField;

/**
 * Adds a "<tt>valid</tt>" property to a JTextField.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ValidatingTextField extends JTextField {
	private boolean valid = true;

	public ValidatingTextField() {
	}

	public ValidatingTextField(String text) {
		super(text);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		if (this.valid != valid) {
			boolean old = this.valid;
			this.valid = valid;
			firePropertyChange("valid", old, valid);
		}
	}
}
