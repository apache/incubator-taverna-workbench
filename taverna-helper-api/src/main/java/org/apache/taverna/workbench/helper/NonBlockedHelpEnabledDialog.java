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

import static java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;

/**
 * @author alanrw
 */
public class NonBlockedHelpEnabledDialog extends HelpEnabledDialog {
	private static final long serialVersionUID = -2455471377333940417L;

	public NonBlockedHelpEnabledDialog(Dialog owner, String title,
			boolean modal, String id) throws HeadlessException {
		super(owner, title, modal, id);
		this.setModalExclusionType(APPLICATION_EXCLUDE);
	}

	public NonBlockedHelpEnabledDialog(Frame owner, String title,
			boolean modal, String id) throws HeadlessException {
		super(owner, title, modal, id);
		this.setModalExclusionType(APPLICATION_EXCLUDE);
	}

	public NonBlockedHelpEnabledDialog(Frame parent, String title, boolean modal) {
		super(parent, title, modal, null);
		this.setModalExclusionType(APPLICATION_EXCLUDE);
	}

	public NonBlockedHelpEnabledDialog(Dialog parent, String title,
			boolean modal) {
		super(parent, title, modal, null);
		this.setModalExclusionType(APPLICATION_EXCLUDE);
	}
}
