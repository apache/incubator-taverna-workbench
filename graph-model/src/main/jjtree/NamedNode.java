package net.sf.taverna.t2.workbench.models.graph.dot;

public class NamedNode  {
	
	protected String name, value, port;

	/**
	 * Returns the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of name.
	 * 
	 * @param name
	 *            the new value for name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of value.
	 * 
	 * @param value
	 *            the new value for value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the port.
	 * 
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * Sets the value of port.
	 * 
	 * @param port
	 *            the new value for port
	 */
	public void setPort(String port) {
		this.port = port;
	}

}

