package org.apache.taverna.workbench.loop.comparisons;

public class IsGreaterThan extends Comparison {

	public String getId() {
		return "IsGreaterThan";
	}

	public String getName() {
		return "is greater than";
	}

	public String getScriptTemplate() {
		return "${loopPort} = \"\" + (! (Double.parseDouble(${port}) > Double.parseDouble(${value})));";
	}

	public String getValueType() {
		return "number";
	}
}