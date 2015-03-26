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

package org.apache.taverna.lang.beans;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * A {@link BeanInfo} that includes {@link PropertyDescriptor}s from methods
 * annotated using {@link PropertyAnnotation}.
 * <p>
 * The bean info from the PropertyAnnotation will then be available through
 * Java's {@link Introspector}, and allows you to specify details such as
 * {@link PropertyAnnotation#displayName()} and
 * {@link PropertyAnnotation#hidden()} for the properties of a Java Bean.
 * <p>
 * This class can either be used as a superclass for the classes containing
 * property annotated methods, or put in a neighbouring BeanInfo class.
 * <p>
 * For instance, if your class is called DescribedClass and has methods
 * annotated using {@link PropertyAnnotation}, either let DescribedClass
 * subclass {@link PropertyAnnotated}, or make a neighbouring {@link BeanInfo}
 * class called DescribedClassBeanInfo, which should subclass
 * {@link PropertyAnnotated}.
 * 
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class PropertyAnnotated extends SimpleBeanInfo {

	private static PropertyAnnotationExtractor extractor = new PropertyAnnotationExtractor();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return extractor.getPropertyDescriptors(getDescribedClass());
	}

	/**
	 * The class that is being described. By default this returns
	 * {@link #getClass()} so that {@link PropertyAnnotated} can be used as a
	 * superclass, but if instead the DescribedClassBeanInfo pattern is used,
	 * subclass PropertyAnnotated in each BeanInfo class, and override this
	 * method to return the described class. (DescribedClass in this example)
	 * 
	 */
	public Class<?> getDescribedClass() {
		return getClass();
	}

}
