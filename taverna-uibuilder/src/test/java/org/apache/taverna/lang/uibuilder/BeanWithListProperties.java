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
package org.apache.taverna.lang.uibuilder;

import java.util.List;
import java.util.ArrayList;

/**
 * Sample bean with a couple of list properties
 * 
 * @author Tom Oinn
 * 
 */
public class BeanWithListProperties {

	private List<String> list1;
	private List<BeanWithBoundProps> list2;

	public BeanWithListProperties() {
		this.list1 = new ArrayList<String>();
		this.list2 = new ArrayList<BeanWithBoundProps>();
		list1.add("A list item");
		list1.add("Another item");
		for (int i = 0; i < 10; i++) {
			list2.add(new BeanWithBoundProps());
		}
	}

	public List<String> getList1() {
		return this.list1;
	}

	public List<BeanWithBoundProps> getList2() {
		return this.list2;
	}

}
