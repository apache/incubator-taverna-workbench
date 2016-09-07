/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.loop.comparisons;

public class Matches extends Comparison {

	public String getId() {
		return "Matches";
	}

	public String getName() {
		return "matches";
	}

	public String getScriptTemplate() {
		return "${loopPort} = \"\" + ! ${port}.matches(${value});";
	}

	public String getValueType() {
		return "regular expression";
	}
}