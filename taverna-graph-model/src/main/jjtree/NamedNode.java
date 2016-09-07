package org.apache.taverna.workbench.models.graph.dot;
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

public class NamedNode  {
	
	protected String name, value, port;

	/**
	 * Returns the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of name.
	 * 
	 * @param name
	 *            the new value for name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of value.
	 * 
	 * @param value
	 *            the new value for value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the port.
	 * 
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * Sets the value of port.
	 * 
	 * @param port
	 *            the new value for port
	 */
	public void setPort(String port) {
		this.port = port;
	}

}

