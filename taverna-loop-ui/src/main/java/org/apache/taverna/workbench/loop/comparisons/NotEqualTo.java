/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.loop.comparisons;

public class NotEqualTo extends Comparison {

	public String getId() {
		return "NotEqualTo";
	}

	public String getName() {
		return "is not equal to";
	}

	public String getScriptTemplate() {
		return "${loopPort} = \"\" + ${port}.equals(${value});";
	}

	public String getValueType() {
		return "string";
	}
}