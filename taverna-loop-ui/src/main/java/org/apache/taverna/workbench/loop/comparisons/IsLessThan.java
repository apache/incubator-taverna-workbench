package org.apache.taverna.workbench.loop.comparisons;

public class IsLessThan extends Comparison {

	public String getId() {
		return "IsLessThan";
	}

	public String getName() {
		return "is less than";
	}

	public String getScriptTemplate() {
		return "${loopPort} = \"\" + (! (Double.parseDouble(${port}) < Double.parseDouble(${value})));";
	}

	public String getValueType() {
		return "number";
	}
}