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

package org.apache.taverna.servicedescriptions;

import java.util.List;

import org.apache.taverna.lang.beans.PropertyAnnotated;

public abstract class IdentifiedObject extends PropertyAnnotated {
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IdentifiedObject))
			return false;
		List<? extends Object> myIdentifyingData = getIdentifyingData();
		if (myIdentifyingData == null)
			return super.equals(obj);
		if (!getClass().isInstance(obj) && obj.getClass().isInstance(this))
			return false;
		IdentifiedObject id = (IdentifiedObject) obj;
		return myIdentifyingData.equals(id.getIdentifyingData());
	}

	@Override
	public int hashCode() {
		List<? extends Object> identifyingData = getIdentifyingData();
		if (identifyingData == null)
			return super.hashCode();
		return identifyingData.hashCode();
	}

	protected abstract List<? extends Object> getIdentifyingData();
}
