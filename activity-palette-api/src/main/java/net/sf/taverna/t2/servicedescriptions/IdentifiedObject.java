package net.sf.taverna.t2.servicedescriptions;

import java.util.List;

import net.sf.taverna.t2.lang.beans.PropertyAnnotated;

public abstract class IdentifiedObject extends PropertyAnnotated {

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof IdentifiedObject)) {
			return false;
		}
		List<? extends Object> myIdentifyingData = getIdentifyingData();
		if (myIdentifyingData == null) {
			return super.equals(obj);
		}
		if (! getClass().isInstance(obj) && obj.getClass().isInstance(this)) {
			return false;
		}
		IdentifiedObject id = (IdentifiedObject) obj;
		return myIdentifyingData.equals(id.getIdentifyingData());
	}

	@Override
	public int hashCode() {
		List<? extends Object> identifyingData = getIdentifyingData();
		if (identifyingData == null) {
			return super.hashCode();
		}
		return identifyingData.hashCode();
	}

	protected abstract List<? extends Object> getIdentifyingData();

}