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

package org.apache.taverna.lang.uibuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Models lists requiring use of the ListHandler helper class within the UI
 * 
 * @author Tom Oinn
 */
public class WrappedListComponent extends AbstractListComponent {

	private static final long serialVersionUID = -4457073442579747674L;
	private ListHandler lh = null;

	@SuppressWarnings("unchecked")
	public WrappedListComponent(String fieldName, List theList,
			Properties props, Map<String, Properties> fieldProps,
			Class<?> newItemClass, List<String> subFields, String parent)
			throws NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException {
		super(fieldName, theList, props, fieldProps, newItemClass, subFields,
				parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addNewItemToList(Object o) throws IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException {
		// Keep lists in sync
		getListHandler();
		synchronized (lh) {
			getUnderlyingList().add(0, o);
			lh.add(0, lh.new ListItem(o));
		}
		updateListContents();
	}

	@Override
	protected void deleteItemAtIndex(int index)
			throws IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException {
		getListHandler();
		synchronized (lh) {
			getUnderlyingList().remove(index);
			lh.remove(index);
		}
		updateListContents();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void itemMoved(int fromIndex, int toIndex)
			throws IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException {
		getListHandler();
		synchronized (lh) {
			Object toMove = getUnderlyingList().remove(fromIndex);
			ListHandler.ListItem wrapperToMove = lh.remove(fromIndex);
			getUnderlyingList().add(toIndex, toMove);
			lh.add(toIndex, wrapperToMove);
		}
		updateListContents();
	}

	@Override
	protected List<JComponent> getListComponents() throws NoSuchMethodException {
		getListHandler();
		List<JComponent> result = new ArrayList<JComponent>();
		for (ListHandler.ListItem item : lh) {
			JPanel listItem = new JPanel();
			listItem.setOpaque(false);
			listItem.setLayout(new BoxLayout(listItem, BoxLayout.PAGE_AXIS));
			Class<?> itemClass = item.getValue().getClass();
			if (itemClass.isEnum()) {
				result.add(new BeanEnumComboBox(item, "value", false,
						new Properties()));
			} else {
				result.add(new BeanTextField(item, "value", false,
						new Properties()));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private ListHandler getListHandler() {
		if (this.lh == null) {
			lh = new ListHandler(getUnderlyingList());
		}
		return lh;
	}

}
