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

package org.apache.taverna.lang.uibuilder;

/**
 * Bean containing all the primitive types in Java (AFAIK)
 * 
 * @author Tom Oinn
 * 
 */
public class PrimitiveTypeBean {

	private int intValue = 1;
	private short shortValue = 2;
	private long longValue = (long) 3.0123;
	private double doubleValue = 4.01234;
	private boolean booleanValue = false;
	private byte byteValue = 5;
	private float floatValue = 6.012345f;
	private char charValue = 'a';

	public PrimitiveTypeBean() {
		//
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public String toString() {
		return intValue + "," + shortValue + "," + longValue + ","
				+ doubleValue + "," + booleanValue + "," + byteValue + ","
				+ floatValue + "," + charValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setShortValue(short shortValue) {
		this.shortValue = shortValue;
		System.out.println(this);
	}

	public short getShortValue() {
		return shortValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
		System.out.println(this);
	}

	public long getLongValue() {
		return longValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
		System.out.println(this);
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
		System.out.println(this);
	}

	public boolean getBooleanValue() {
		return booleanValue;
	}

	public void setByteValue(byte byteValue) {
		this.byteValue = byteValue;
		System.out.println(this);
	}

	public byte getByteValue() {
		return byteValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
		System.out.println(this);
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setCharValue(char charValue) {
		this.charValue = charValue;
		System.out.println(this);
	}

	public char getCharValue() {
		return charValue;
	}

}
