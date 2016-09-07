package org.apache.taverna.workbench.loop.comparisons;

public class EqualTo extends Comparison {

	public String getId() {
		return "EqualTo";
	}

	public String getName() {
		return "is equal to";
	}

	public String getScriptTemplate() {
		return "${loopPort} = \"\" + ! ${port}.equals(${value}); ";
	}

	public String getValueType() {
		return "string";
	}
}