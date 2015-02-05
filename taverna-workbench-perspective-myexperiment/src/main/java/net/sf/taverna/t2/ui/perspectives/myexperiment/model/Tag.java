// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class Tag extends Resource implements Serializable {
  private String tagName;
  private int count;

  public Tag() {
	super();
	this.setItemType(Resource.TAG);
  }

  public String getTagName() {
	return tagName;
  }

  public void setTagName(String tagName) {
	this.tagName = tagName;
  }

  public int getCount() {
	return count;
  }

  public void setCount(int count) {
	this.count = count;
  }

  public static class ReversePopularityComparator implements Comparator<Tag> {
	public ReversePopularityComparator() {
	  super();
	}

	public int compare(Tag t1, Tag t2) {
	  if (t1.getCount() == t2.getCount()) {
		// in case of the same popularity, compare by tag name
		return (t1.getTagName().compareTo(t2.getTagName()));
	  } else {
		// popularity isn't the same; arrange by popularity (more popular first)
		return (t2.getCount() - t1.getCount());
	  }
	}
  }

  public static class AlphanumericComparator implements Comparator<Tag> {
	public AlphanumericComparator() {
	  super();
	}

	public int compare(Tag t1, Tag t2) {
	  return (t1.getTagName().compareTo(t2.getTagName()));
	}
  }

  /**
   * This makes sure that things like instanceOf() and remove() in List
   * interface work properly - this way resources are treated to be the same if
   * they store identical data, rather than they simply hold the same reference.
   */
  public boolean equals(Object other) {
	// could only be equal to another Tag object, not anything else
	if (!(other instanceof Tag))
	  return (false);

	// 'other' object is a Tag; equality is based on the data stored
	// in the current and 'other' Tag instances
	Tag otherTag = (Tag) other;
	return (this.count == otherTag.count && this.tagName.equals(otherTag.tagName));
  }

  public String toString() {
	return ("Tag (" + this.tagName + ", " + this.count + ")");
  }

  /**
   * A helper method to return a set of API elements that are needed to satisfy
   * request of a particular type - e.g. creating a listing of resources or
   * populating full preview, etc.
   * 
   * @param iRequestType
   *          A constant value from Resource class.
   * @return Comma-separated string containing values of required API elements.
   */
  public static String getRequiredAPIElements(int iRequestType) {
	String strElements = "";

	// cases higher up in the list are supersets of those that come below -
	// hence no "break" statements are required, because 'falling through' the
	// switch statement is the desired behaviour in this case
	switch (iRequestType) {
	  case Resource.REQUEST_DEFAULT_FROM_API:
		strElements += ""; // no change needed - defaults will be used
	}

	return (strElements);
  }

  /**
   * Instantiates a Tag object from action command string that is used to
   * trigger tag search events in the plugin. These action commands should look
   * like "tag:<tag_name>".
   * 
   * @param strActionCommand
   *          The action command to parse.
   * @return A Tab object instance or null if action command was invalid.
   */
  public static Tag instantiateTagFromActionCommand(String strActionCommand) {
	if (!strActionCommand.startsWith("tag:")) {
	  return (null);
	} else {
	  // instantiate the Tag object, strip out the leading "tag:" and return result
	  Tag t = new Tag();
	  t.setTagName(strActionCommand.replaceFirst("tag:", ""));
	  return (t);
	}
  }

}
