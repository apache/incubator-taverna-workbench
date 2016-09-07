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

package org.apache.taverna.workbench.ui.impl.menu;

import static java.awt.Desktop.getDesktop;
import static org.apache.taverna.workbench.ui.impl.menu.HelpMenu.HELP_URI;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;

import org.apache.log4j.Logger;

/**
 * MenuItem for feedback
 * 
 * @author alanrw
 */
public class FeedbackMenuAction extends AbstractMenuAction {
	private static Logger logger = Logger.getLogger(FeedbackMenuAction.class);

	private static String FEEDBACK_URL = "http://www.taverna.org.uk/about/contact-us/feedback/";

	public FeedbackMenuAction() {
		super(HELP_URI, 20);
	}

	@Override
	protected Action createAction() {
		return new FeedbackAction();
	}

	@SuppressWarnings("serial")
	private final class FeedbackAction extends AbstractAction {
		private FeedbackAction() {
			super("Contact us");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				getDesktop().browse(new URI(FEEDBACK_URL));
			} catch (IOException e1) {
				logger.error("Unable to open URL", e1);
			} catch (URISyntaxException e1) {
				logger.error("Invalid URL syntax", e1);
			}
		}
	}

}
