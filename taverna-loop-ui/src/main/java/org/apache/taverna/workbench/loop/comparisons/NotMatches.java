/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.loop.comparisons;

public class NotMatches extends Comparison {

	public String getId() {
		return "NotMatches";
	}

	public String getName() {
		return "does not match";
	}

	public String getScriptTemplate() {
		return "${loopPort} = \"\" + ${port}.matches(${value});";
	}

	public String getValueType() {
		return "regular expression";
	}
}