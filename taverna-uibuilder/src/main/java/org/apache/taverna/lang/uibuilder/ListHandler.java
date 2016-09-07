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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The list handler is used to allow the reflection based UI builder to handle
 * lists of non-bean value types such as String etc.
 * 
 * @author Tom Oinn
 * 
 */
public class ListHandler extends
		ArrayList<ListHandler.ListItem> {

	private static Logger logger = Logger
	.getLogger(ListHandler.class);

	private static final long serialVersionUID = -1361470859975889856L;

	private List<Object> wrappedList;
	
	public ListHandler(List<Object> theList) {
		this.wrappedList = theList;
		for (Object o : wrappedList) {
			this.add(new ListItem(o));
		}
	}

	/**@Override
	public boolean add(ListHandler.ListItem newItem) {
		wrappedList.add((T) newItem.getValue());
		return super.add(newItem);
	}*/

	/**
	 * Simple container class to handle list items, allowing them to present a
	 * bean interface
	 * 
	 * @author Tom Oinn
	 * 
	 */
	class ListItem {
		Object value;

		public ListItem(Object o) {
			this.value = o;
		}

		public void setValue(Object o) {
			try {
			wrappedList.set(indexOf(this), o);
			this.value = o;
			}
			catch (Exception ex) {
				logger.error("Unable to set value", ex);
			}
		}
				
		public Object getValue() {
			return this.value;
		}
	}

}
