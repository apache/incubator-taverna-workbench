package net.sf.taverna.biocatalogue.model;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * @author Sergejs Aleksejevs
 */
public class Tag implements Serializable
{
  private static final long serialVersionUID = 784872111173271581L;
  
  private String tagURI;          // URI to use on BioCatalogue to fetch all tagged items
	private String tagNamespace;    // namespace where this tag is defined
	private String tagDisplayName;  // only the actual tag (for display within the tag cloud)
  private String fullTagName;     // full tag name - including namespace
	private int itemCount;          // count of tagged items
  
	
	/**
	 * Constructs a Tag instance from primitive components.
	 * All values are set directly, no internal inference made.
	 * 
	 * @param tagURI
	 * @param tagNamespace
	 * @param tagDisplayName
	 * @param fullTagName
	 * @param itemCount
	 */
	public Tag(String tagURI, String tagNamespace, String tagDisplayName, String fullTagName, int itemCount)
	{
	  this.tagURI = tagURI;
    this.tagNamespace = tagNamespace;
    this.tagDisplayName = tagDisplayName;
    this.setFullTagName(fullTagName);
    this.itemCount = itemCount;
	}
	
	
	/**
	 * Constructs Tag instance from an XML representation of the Tag from BioCatalogue API.
	 * 
	 * @param xmlTag
	 */
	public Tag(org.biocatalogue.x2009.xml.rest.Tag xmlTag)
	{
	  // these values come directly from the XML data obtained via the API 
	  this.tagURI = xmlTag.getHref();
	  this.fullTagName = xmlTag.getName();
	  this.itemCount = xmlTag.getTotalItemsCount().intValue();
	  
	  // NB! Namespace and the display name need to be inferred 'manually'.
	  // First - set the namespace; it's value is taken from the 'namespace'
	  // attribute of the tag URI.
    this.tagNamespace = Util.extractURLParameter(this.tagURI, "namespace");
	  
	  // Now set the display name; if full tag name is not a part of any ontology,
	  // display name will be identical to the full name. 
	  if (this.fullTagName.startsWith("<") && this.fullTagName.endsWith(">")) {
	    int iStart = this.fullTagName.lastIndexOf('#') + 1;
	    this.tagDisplayName = this.fullTagName.substring(iStart, this.fullTagName.length() - 1);
	  }
	  else {
	    this.tagDisplayName = this.fullTagName;
	  }
	}
	
  
	// *** Various getters and setters ***
	
	public void setTagURI(String tagURI) {
    this.tagURI = tagURI;
  }
	
  public String getTagURI() {
    return tagURI;
  }
  
  
  public void setTagNamespace(String tagNamespace) {
    this.tagNamespace = tagNamespace;
  }
  
  public String getTagNamespace() {
    return tagNamespace;
  }
  
  
  public void setTagDisplayName(String tagDisplayName) {
    this.tagDisplayName = tagDisplayName;
  }
  
  public String getTagDisplayName() {
    return tagDisplayName;
  }
  
  
  public void setFullTagName(String fullTagName) {
    this.fullTagName = fullTagName;
  }
  
  /**
   * @return Unique and unambiguous name of this tag on BioCatalogue:<br/>
   *         <ul>
   *         <li>for tags with no namespaces, they it is just plain text names;</li>
   *         <li>for those with namespaces, it will have the following form:<br/>
   *             "<code>< http://www.mygrid.org.uk/ontology#retrieving ></code>" (without spaces, though), where
   *             the first part before the '#' symbol is the namespace and the second part
   *             is the actual tag within that namespace.</li></ul>
   */
  public String getFullTagName() {
    return fullTagName;
  }
  
  
	public int getItemCount() {
		return itemCount;
	}
	
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}
	
	
	// *** Tag Comparators ***
	
	public static class ReversePopularityComparator implements Comparator<Tag>
	{
	  public ReversePopularityComparator() {
	    super();
	  }
	  
	  public int compare(Tag t1, Tag t2)
	  {
	    if (t1.getItemCount() == t2.getItemCount()) {
	      // in case of the same popularity, compare by full tag names (which are unique)
	      return (t1.getFullTagName().compareTo(t2.getFullTagName()));
	    }
	    else {
	      // popularity isn't the same; arrange by popularity (more popular first)
	      return (t2.getItemCount() - t1.getItemCount());
	    }
	  }
	}
	
	
	public static class AlphanumericComparator implements Comparator<Tag>
  {
    public AlphanumericComparator() {
      super();
    }
    
    public int compare(Tag t1, Tag t2) {
      // full tag names are unique on BioCatalogue
      return (t1.getFullTagName().compareTo(t2.getFullTagName()));
    }
  }
	
	
	/**
   * This makes sure that things like instanceOf() and remove() in List interface
   * work properly - this way resources are treated to be the same if they store
   * identical data, rather than they simply hold the same reference.
   */
	public boolean equals(Object other) {
    // could only be equal to another Tag object, not anything else
    if (! (other instanceof Tag)) return (false);
    
    // 'other' object is a Tag; equality is based on the data stored
    // in the current and 'other' Tag instances
    Tag otherTag = (Tag)other;
    return (this.itemCount == otherTag.itemCount && this.fullTagName.equals(otherTag.fullTagName));
  }
	
  
  public String toString()
  {
    return ("Tag (" + this.fullTagName + ", " + this.itemCount + ")");
  }
  
  
  /**
   * This method is used to generate the tooltip to be shown over the tag
   * in the tagcloud. Shown text will contain the full tag name, namespace
   * and frequency.
   * 
   * @return HTML encoded string ready to be put into the tooltip.
   */
  public String getTagCloudTooltip()
  {
    StringBuilder tooltip = new StringBuilder("<html>");
    
    tooltip.append("&nbsp;<b>" + (this.fullTagName.length() > this.tagDisplayName.length() ? "Full tag" : "Tag") + ": </b>" + StringEscapeUtils.escapeHtml(this.fullTagName));
    if (this.tagNamespace != null && this.tagNamespace.length() > 0) {
      tooltip.append("<br>&nbsp;<b>Namespace: </b>" + StringEscapeUtils.escapeHtml(this.tagNamespace));
    }
    tooltip.append("<br>&nbsp;<b>Frequency: </b>" + this.itemCount);
    tooltip.append("</html>");
    
    return tooltip.toString();
  }
  
	
}
