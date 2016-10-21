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

/**
 * Bean containing the various other sub-beans
 * 
 * @author Tom Oinn
 * 
 */
public class TopLevelBean {

	private SampleEnum enumeratedField = SampleEnum.ABCD;
	private BeanWithBoundProps boundBean = new BeanWithBoundProps();
	private BeanWithNestedList nest = new BeanWithNestedList();

	public TopLevelBean() {
		//
	}

	public void setEnumeratedField(SampleEnum enumeratedField) {
		this.enumeratedField = enumeratedField;
	}

	public SampleEnum getEnumeratedField() {
		return enumeratedField;
	}

	public void setBoundBean(BeanWithBoundProps boundBean) {
		this.boundBean = boundBean;
	}

	public BeanWithBoundProps getBoundBean() {
		return boundBean;
	}

	public void setNest(BeanWithNestedList nest) {
		this.nest = nest;
	}

	public BeanWithNestedList getNest() {
		return nest;
	}

}
