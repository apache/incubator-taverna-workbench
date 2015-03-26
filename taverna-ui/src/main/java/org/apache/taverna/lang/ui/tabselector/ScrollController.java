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

package org.apache.taverna.lang.ui.tabselector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * Controls tab scrolling when there is not enough space to show all the tabs.
 *
 * @author David Withers
 */
public class ScrollController {

	private int position;
	private final JButton scrollLeft;
	private final JButton scrollRight;

	public ScrollController(final JComponent component) {
		scrollLeft = new JButton("<");
		scrollRight = new JButton(">");
		scrollLeft.setOpaque(true);
		scrollRight.setOpaque(true);
		scrollLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				increment();
				component.doLayout();
			}
		});
		scrollRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				decrement();
				component.doLayout();
			}
		});
	}

	public JButton getScrollLeft() {
		return scrollLeft;
	}

	public JButton getScrollRight() {
		return scrollRight;
	}

	public int getPosition() {
		return position;
	}

	public void reset() {
		position = 0;
	}

	public void increment() {
		position++;
	}

	public void decrement() {
		position--;
	}

}
