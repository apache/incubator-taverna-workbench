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

/**
 * Superinterface for components which have a label and which may be mutually
 * aligned within a panel. This assumes the component is laid out with a label
 * to the left of the main editing area, and that we want to ensure that all
 * editing areas line up and can do this by setting the preferred size of the
 * label.
 * 
 * @author Tom Oinn
 * 
 */
public interface AlignableComponent {

	/**
	 * Set the preferred width of the label for this alignable component
	 * 
	 * @param newWidth
	 */
	public void setLabelWidth(int newWidth);

	/**
	 * Get the current preferred width of the label for this alignable component
	 * 
	 * @return
	 */
	public int getLabelWidth();

}
