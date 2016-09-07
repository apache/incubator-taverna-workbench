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

package org.apache.taverna.workbench.helper;

import static org.apache.taverna.workbench.MainWindow.getMainWindow;
import static org.apache.taverna.workbench.helper.HelpCollator.registerComponent;
import static org.apache.taverna.workbench.helper.Helper.setKeyCatcher;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JDialog;

/**
 * This class extends JDialog to register the dialog and also attach a key
 * catcher so that F1 is interpreted as help
 *
 * @author alanrw
 */
public class HelpEnabledDialog extends JDialog {
	private static final long serialVersionUID = -5068807887477419800L;

	/**
	 * Create a HelpEnabledDialog, register it (if possible) with the
	 * HelpCollator and attach a keycatcher.
	 *
	 * @param owner
	 * @param title
	 * @param modal
	 * @param id
	 * @throws HeadlessException
	 */
	public HelpEnabledDialog(Frame owner, String title, boolean modal, String id)
			throws HeadlessException {
		super(owner == null ? getMainWindow() : owner, title, modal);

		if (id != null)
			registerComponent(this, id);
		else if (owner != null)
			registerComponent(this, owner.getClass().getCanonicalName()
					+ "-dialog");
		else if (title != null && !title.isEmpty())
			registerComponent(this, title);
		setKeyCatcher(this);
	}

	/**
	 * Create a HelpEnabledDialog, register it (if possible) with the
	 * HelpCollator and attach a keycatcher.
	 *
	 * @param owner
	 * @param title
	 * @param modal
	 * @param id
	 * @throws HeadlessException
	 */
	public HelpEnabledDialog(Dialog owner, String title, boolean modal,
			String id) throws HeadlessException {
		super(owner, title, modal);
		if (id != null)
			registerComponent(this, id);
		else if (owner != null)
			registerComponent(this, owner.getClass().getCanonicalName()
					+ "-dialog");
		setKeyCatcher(this);
	}

	/**
	 * Create a HelpEnabledDialog, register it (if possible) with the
	 * HelpCollator and attach a keycatcher.
	 *
	 * @param owner
	 * @param title
	 * @param modal
	 * @throws HeadlessException
	 */
	public HelpEnabledDialog(Frame parent, String title, boolean modal) {
		this(parent, title, modal, null);
	}

	/**
	 * Create a HelpEnabledDialog, register it (if possible) with the
	 * HelpCollator and attach a keycatcher.
	 *
	 * @param owner
	 * @param title
	 * @param modal
	 * @throws HeadlessException
	 */
	public HelpEnabledDialog(Dialog parent, String title, boolean modal) {
		this(parent, title, modal, null);
	}

	@Override
	public void setVisible(boolean b) {
		setLocationRelativeTo(getParent());
		super.setVisible(b);
	}
}
