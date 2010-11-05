package net.sf.taverna.biocatalogue.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.BioCataloguePerspective;

/**
 * Class containing various reusable helper methods.
 * 
 * @author Sergejs Aleksejevs
 */
public class Util
{
  
  /**
   * Makes sure that one component (for example, a window) is centered horizontally and vertically
   * relatively to the other component. This is achieved by aligning centers of the two components.
   * 
   * This method can be used even if the 'dependentComponent' is larger than the 'mainComponent'. In
   * this case it is probably only useful for centering windows against each other rather than
   * components inside a container.
   * 
   * Method also makes sure that the dependent component will not be placed above the screen's upper
   * edge and to the left of the left edge.
   */
  public static void centerComponentWithinAnother(Component mainComponent, Component dependentComponent)
  {
    int iMainComponentCenterX = (int)Math.round(mainComponent.getLocationOnScreen().getX() + (mainComponent.getWidth() / 2));
    int iPosX = iMainComponentCenterX - (dependentComponent.getWidth() / 2);
    if (iPosX < 0) iPosX = 0;
    
    int iMainComponentCenterY = (int)Math.round(mainComponent.getLocationOnScreen().getY() + (mainComponent.getHeight() / 2));
    int iPosY = iMainComponentCenterY - (dependentComponent.getHeight() / 2);
    if (iPosY < 0) iPosY = 0;
    
    dependentComponent.setLocation(iPosX, iPosY);
  }
  
  
  /**
   * The parameter is the class name to be processed; class name is likely to be in
   * the form <class_name>$<integer_value>, where the trailing part starting with
   * the $ sign indicates the anonymous inner class within the base class. This will
   * strip out that part of the class name to get the base class name.
   */
  public static String getBaseClassName(String strClassName)
  {
    // strip out the class name part after the $ sign; return
    // the original value if the dollar sign wasn't found
    String strResult = strClassName;
    
    int iDollarIdx = strResult.indexOf("$");
    if (iDollarIdx != -1) strResult = strResult.substring(0, iDollarIdx);
    
    return (strResult);
  }
  
  
  /**
   * Makes sure that the supplied string is no longer than provided length.
   */
  public static String ensureStringLength(String str, int iLength) {
    if (str.length() > iLength) str = str.substring(0, iLength) + " (...)";
    return (str);
  }
  
  /**
   * Makes sure that the supplied string doesn't have any lines (separated by HTML line break tag) longer
   * than specified; assumes that there are no line breaks in the source line.
   * 
   * @param str The string to work with.
   * @param iLineLength Desired length of each line.
   * @param bIgnoreBrokenWords True if line breaks are to be inserted exactly each <code>iLineLength</code>
   *                           symbols (which will most likely cause broken words); false to insert line breaks
   *                           at the first space after <code>iLineLength</code> symbols since last line break.
   * @return New string with inserted HTML line breaks.
   */
  public static String ensureLineLengthWithinString(String str, int iLineLength, boolean bIgnoreBrokenWords)
  {
    StringBuilder out = new StringBuilder(str);
    
    // keep inserting line breaks from end of the line till the beginning until all done
    int iLineBreakPosition = 0;
    while (iLineBreakPosition >= 0 && iLineBreakPosition < out.length())
    {
      // insert line break either exactly at calculated position or 
      iLineBreakPosition += iLineLength;
      iLineBreakPosition = (bIgnoreBrokenWords ?
                            iLineBreakPosition :
                            out.indexOf(" ", iLineBreakPosition));
      
      if (iLineBreakPosition > 0 && iLineBreakPosition < out.length()) {
        out.insert(iLineBreakPosition, "<br>");
        iLineBreakPosition += 4;  // -- four is the length of "<br>"
      }
    }
    
    return (out.toString());
  }
  
  
  /**
   * This is a convenience method for calling
   * {@link Util#pluraliseNoun(String, long, boolean)}
   * with <code>false</code> as a value for third parameter.
   */
  public static String pluraliseNoun(String noun, long count) {
    return (pluraliseNoun(noun, count, false));
  }
  
  /**
   * Performs naive pluralisation of the supplied noun.
   * 
   * @param noun Noun in a singular form.
   * @param count Number of occurrences of the item, for which the noun is provided.
   * @param forceAppendingSByDefault <code>true</code> to make sure that "y" -> "ies"
   *                                 substitution is <b>not made</b>, but instead "s" is appended
   *                                 to unmodified root of the noun.
   * @return Pluralised <code>noun</code>: with appended -s or -y replaced with -ies.
   */
  public static String pluraliseNoun(String noun, long count, boolean forceAppendingSByDefault)
  {
    if (count % 10 != 1 || count == 11) {
      if (!forceAppendingSByDefault && noun.endsWith("y")) {
        return (noun.substring(0, noun.length() - 1) + "ies");  // e.g. ENTRY -> ENTRIES
      }
      else {
        return (noun + "s");  // e.g. SHIP -> SHIPS
      }
    }
    else {
      // no need to pluralise - count is of the type 21, 31, etc.. 
      return noun;
    }
  }
  
  
  /**
   * Calculates time difference between two {@link Calendar} instances.
   * 
   * @param earlier The "earlier" date.
   * @param later The "later" date.
   * @param maxDifferenceMillis The maximum allowed time difference between the two
   *                            {@link Calendar} instances, in milliseconds. If the calculated
   *                            difference will exceed <code>maxDifferenceMillis</code>,
   *                            <code>null</code> will be returned. If this parameter has
   *                            a value <code>less or equal to zero</code>, any time difference
   *                            between the {@link Calendar} instances will be permitted.
   * @return String in the form "XX seconds|minutes|hours|days ago". Proper pluralisation will
   *         be performed on the name of the time unit. <code>null</code> will be returned in
   *         cases, where one of the {@link Calendar} instances is <code>null</code>, time
   *         difference between the provided instances is greated than <code>maxDifferenceMillis</code>
   *         or <code>earlier</code> date is not really earlier than <code>later</code> one.
   */
  public static String getAgoString(Calendar earlier, Calendar later, long maxDifferenceMillis)
  {
    // one of the dates is missing
    if (earlier == null || later == null) {
      return null;
    }
    
    if (earlier.before(later)) {
      long differenceMillis = later.getTimeInMillis() - earlier.getTimeInMillis();
      
      if (maxDifferenceMillis <= 0 || (maxDifferenceMillis > 0 && differenceMillis <= maxDifferenceMillis)) 
      {
        long result = 0;
        String unitName = "";
        
        if (differenceMillis < 60 * 1000) {
          result = differenceMillis / 1000;
          unitName = "second";
        }
        else if (differenceMillis < 60 * 60 * 1000) {
          result = differenceMillis / (60 * 1000);
          unitName = "minute";
        }
        else if (differenceMillis < 24 * 60 * 60 * 1000) {
          result = differenceMillis / (60 * 60 * 1000);
          unitName = "hour"; 
        }
        else {
          result = differenceMillis / (24 * 60 * 60 * 1000);
          unitName = "day";
        }
        
        return (result + " " + Util.pluraliseNoun(unitName, result, true) + " ago");
      }
      else {
        // the difference is too large - larger than the supplied threshold
        return null;
      }
    }
    else {
      // the "later" date is not really later than the "earlier" one
      return null;
    }
  }
  
  
  /**
   * Joins the set of tokens in the provided list into a single string.
   * This method is a shorthand for {@link Util#join(List, String, String, String)}.
   * 
   * @param tokens List of strings to join.
   * @param separator Separator to insert between individual strings.
   * @return String of the form <code>[token1][separator][token2][separator]...[tokenN]</code>
   */
  public static String join(List<String> tokens, String separator) {
    return (join(tokens, null, null, separator));
  }
  
  /**
   * Joins the set of tokens in the provided list into a single string.
   * 
   * Any empty strings or <code>null</code> entries in the <code>tokens</code> list 
   * will be removed to achieve a better resulting joined string.
   * 
   * @param tokens List of strings to join.
   * @param prefix String to prepend to each token.
   * @param suffix String to append to each token.
   * @param separator Separator to insert between individual strings.
   * @return String of the form <code>[prefix][token1][suffix][separator][prefix][token2][suffix][separator]...[prefix][tokenN][suffix]</code>
   *         <br/><br/>
   *         Example: call <code>join(["cat","sat","on","the","mat"], "[", "]", ", ")</code> results in the following output:
   *                  <code>"[cat], [sat], [on], [the], [mat]"</code>
   */
  public static String join(List<String> tokens, String prefix, String suffix, String separator)
  {
    if (tokens == null) {
      // nothing to join
      return (null);
    }
    
    // list of strings is not empty, but some pre-processing is necessary
    // to remove any empty strings that may be there
    for (int i = tokens.size() - 1; i >= 0; i--) {
      if (tokens.get(i) == null || tokens.get(i).length() == 0) {
        tokens.remove(i);
      }
    }
    
    // now start the actual processing, but it may be the case that all strings
    // were empty and we now have an empty list
    if (tokens.isEmpty()) {
      // nothing to join - just return an empty string
      return ("");
    }
    else {
      // there are some tokens -- perform the joining
      String effectivePrefix = (prefix == null ? "" : prefix);
      String effectiveSuffix = (suffix == null ? "" : suffix);
      String effectiveSeparator = (separator == null ? "" : separator);
      
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < tokens.size(); i++) {
        result.append(effectivePrefix + tokens.get(i) + effectiveSuffix);
        result.append(i == tokens.size() - 1 ? "" : effectiveSeparator);
      }
      
      return (result.toString());
    }
  }
  
  /**
   * Generates a JLabel with a "none-text" style - i.e. the text will be grayed out and in italics.
   */
  public static JLabel generateNoneTextLabel(String strLabel)
  {
    JLabel lNoneText = new JLabel(strLabel);
    lNoneText.setFont(lNoneText.getFont().deriveFont(Font.ITALIC));
    lNoneText.setForeground(Color.GRAY);
    lNoneText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    return (lNoneText);
  }
  
  
  /**
   * Determines whether the plugin is running as a standalone JFrame or inside Taverna Workbench.
   * This is a naive test, based only on the fact that Taverna uses Raven ApplicationRuntime.
   */
  public static boolean isRunningInTaverna()
  {
    try {
      // ApplicationRuntime class is defined within Taverna API. If this is available,
      // it should mean that the plugin runs within Taverna.
      ApplicationRuntime.getInstance();
      return true;
    }
    catch (NoClassDefFoundError e) {
      return false;
    }
  }
  
  
  // === STRIPPING OUT HTML FROM STRINGS ===

  /**
   * Tiny helper to strip out HTML tags. Basic HTML tags like &nbsp; and <br>
   * are left in place, because these can be rendered by JLabel. This helps to
   * present HTML content inside JAVA easier.
   */
  public static String stripHTML(String source) {
        // don't do anything if not string is provided
        if (source == null)
          return ("");

        // need to preserve at least all line breaks
        // (ending and starting paragraph also make a line break)
        source = source.replaceAll("</p>[\r\n]*<p>", "<br>");
        source = source.replaceAll("\\<br/?\\>", "[-=BR=-]");

        // strip all HTML
        source = source.replaceAll("\\<.*?\\>", "");

        // put the line breaks back
        source = source.replaceAll("\\[-=BR=-\\]", "<br><br>");

        return (source);
  }

  /**
   * Tiny helper to strip out all HTML tags. This will not leave any HTML tags
   * at all (so that the content can be displayed in DialogTextArea - and the
   * like - components. This helps to present HTML content inside JAVA easier.
   */
  public static String stripAllHTML(String source) {
        // don't do anything if not string is provided
        if (source == null)
          return ("");

        // need to preserve at least all line breaks
        // (ending and starting paragraph also make a line break)
        source = source.replaceAll("</p>[\r\n]*<p>", "<br>");
        source = source.replaceAll("\\<br/?\\>", "\n\n");

        // strip all HTML
        source = source.replaceAll("\\<.*?\\>", ""); // any HTML tags
        source = source.replaceAll("&\\w{1,4};", ""); // this is for things like "&nbsp;", "&gt;", etc

        return (source);
  }

  
  
  /*
   * === The following section is providing URL handling methods. ===
   */
  
  /**
   * See: {@link Util#appendStringBeforeParametersOfURL(String, String, boolean)}
   * 
   * Assumes the last parameter as false, so that the URL encoding will be done.
   */
  public static String appendStringBeforeParametersOfURL(String url, String strToAppend) {
    return (appendStringBeforeParametersOfURL(url, strToAppend, false));
  }
  
  /**
   * Appends given string at the end of the provided URL just before the list of parameters in the url.
   * 
   * For example, appending ".xml" to URL "http://www.sandbox.biocatalogue.org/services?tag=blast" will
   * yield "http://www.sandbox.biocatalogue.org/services.xml?tag=blast".
   * 
   * No duplication checking is made - if the URL is already ending (before parameters) with the value of
   * the string to append, that string will still be appended.
   * 
   * @param url URL to append the string to.
   * @param strToAppend The string to append. The value will be url-encode before appending.
   * @return New string containing modified <code>url</code> with the <code>strToAppend</code> appended
   *         before the "query string" of the URL.
   */
  public static String appendStringBeforeParametersOfURL(String url, String strToAppend, boolean ignoreURLEncoding)
  {
    StringBuilder modifiedURL = new StringBuilder(url);
    
    int iPositionToInsertProvidedString = modifiedURL.indexOf("?");
    if (iPositionToInsertProvidedString == -1) iPositionToInsertProvidedString = modifiedURL.length();
    
    String stringToInsert = (ignoreURLEncoding ? strToAppend : Util.urlEncodeQuery(strToAppend));
    modifiedURL.insert(iPositionToInsertProvidedString, stringToInsert);
    
    return (modifiedURL.toString());
  }
  
  
  /**
   * This method takes a collection of name-value pairs in the form of a map.
   * It then adds all parameters from this map to the provided URL. 
   * 
   * If any parameter has the same name as was already present in the URL, the new value
   * will replace the existing one.
   * 
   * The implementation of this method is not particularly efficient - it makes a
   * lot of overhead, but it's fine for non-intensive usage.
   * 
   * @param url The URL to add a new parameter to.
   * @param Map of parameters to add to the URL. Keys and values of the map are the names and values for URL parameters.
   * @return New string which is the original <code>url</code> with all provided
   *         parameters (in the form <code>name</code>=<code>value</code> pair) added to it.
   */
  public static String appendAllURLParameters(String url, Map<String,String> parameterMap)
  {
    if (parameterMap == null || parameterMap.size() == 0) {
      // nothing to add, return the same URL
      return (url);
    }
    else {
      // just call an overloaded method which has the main logic
      // to do this action for each name-value pair in the map
      String out = url;
      for (Map.Entry<String,String> anotherParameter : parameterMap.entrySet()) {
        out = appendURLParameter(out, anotherParameter);
      }
      return (out);
    }
  }
  
  
  
  /**
   * This method takes a string representation of a URL and a name-value pair
   * of strings - in the form of Map.Entry instance to add to the URL as a new parameter.
   * 
   * If parameter with the same name was already present in the URL, the new value
   * will replace the existing one.
   * 
   * @param url The URL to add a new parameter to.
   * @param Map.Entry instance containing the name & the value for the parameter to add.
   * @return New string which is the original <code>url</code> with the desired
   *         parameter (<code>name</code>=<code>value</code> pair) added to it.
   */
  public static String appendURLParameter(String url, Map.Entry<String,String> parameter)
  {
    if (parameter == null) {
      // nothing to add, return the same URL
      return (url);
    }
    else {
      // just call an overloaded method which has the main logic to do this action
      return (appendURLParameter(url, parameter.getKey(), parameter.getValue()));
    }
  }
  
  
  /**
   * This method takes a string representation of a URL and a name-value pair
   * of strings to add to the URL as a new parameter.
   * 
   * If parameter with the same name was already present in the URL, the new value
   * will replace the existing one.
   * 
   * @param url The URL to add a new parameter to.
   * @param parameter String array with 2 elements - first element is the name
   *        of the parameter to add, second - the value of the parameter.
   * @return New string which is the original <code>url</code> with the desired
   *         parameter (<code>name</code>=<code>value</code> pair) added to it.
   */
  public static String appendURLParameter(String url, String[] parameter)
  {
    if (parameter == null || parameter.length != 2) {
      // nothing to add, return the same URL
      return (url);
    }
    else {
      // just call an overloaded method which has the main logic to do this action
      return (appendURLParameter(url, parameter[0], parameter[1]));
    }
  }
  
  
  /**
   * This method takes a string representation of a URL and a name-value pair
   * of strings to add to the URL as a new parameter.
   * 
   * If parameter with the same name was already present in the URL, the new value
   * will replace the existing one.
   * 
   * @param url The URL to add a new parameter to.
   * @param name Name of the parameter to add.
   * @param value Value of the parameter to add.
   * @return New string which is the original <code>url</code> with the desired
   *         parameter (<code>name</code>=<code>value</code> pair) added to it.
   */
  public static String appendURLParameter(String url, String name, String value)
  {
    // if name of the parameter is not given, ignore this request
    // (makes sense to return the same URL as the input in this case -
    //  as appending "nothing" wouldn't make it any different)
    if (name == null || name.length() == 0) {
      return (url);
    }
    
    // do everything in the protected block
    try
    {
      // parse the parameters of the given URL
      Map<String,String> urlParameters = extractURLParameters(url);
      if (urlParameters == null) {
        // there were no parameters in the original URL, create new map
        urlParameters = new HashMap<String,String>();
      }
      
      // add the new parameter (this will replace a parameter with identical
      // name if it was already present in the map)
      urlParameters.put(name, value);
      
      // parse the URL string into the URL object to extract original query string
      URL theURL = new URL(url);
      String originalQueryString = theURL.getQuery();
      
      // prepare the basis for the new URL to return
      String newUrl = null;
      if (originalQueryString != null) {
        // replace the original query string with empty space to
        // give way for appending the new query string
        newUrl = url.replace(originalQueryString, "");
      }
      else {
        // there were no parameters in the original URL
        newUrl = url + "?";  
      }
      
      // append the new query string
      newUrl += constructURLQueryString(urlParameters);
      
      return (newUrl);
    }
    catch (Exception e)
    {
      System.err.println("\nCouldn't append parameter ('" + name + "', '" + value + "') to the URL: " + url); 
      e.printStackTrace();
      return (null);
    }
  }
  
  
  /**
   * Extracts a value of a specific parameter from the supplied URL.
   *  
   * @param url The URL to extract the parameter from.
   * @param parameterName Name of the URL parameter to extract the value for.
   * @return Value of the parameter with <code>parameterName</code> in the given <code>url</code>.
   *         If the parameter with specified name is not found in the given <code>url</code>,
   *         <code>null</code> is returned instead. 
   */
  public static String extractURLParameter(String url, String parameterName)
  {
    // both URL and the name of the required parameter must be supplied
    if (url == null || url.length() == 0 || parameterName == null || parameterName.length() == 0) return null;
    
    Map<String,String> urlParameters = extractURLParameters(url);
    if (urlParameters != null) {
      // the URL has some parameters; check what's the value of the desired parameter
      return (urlParameters.get(parameterName));
    }
    else {
      // the URL doesn't contain any parameters
      return (null);
    }
  }
  
  
  /**
   * Extracts the query string from the provided URL. Parses this query string into
   * a map of parameters.
   * 
   * All parameters (both names and values) will have special characters unescaped
   * (i.e. decoded from the standard url-encoding) and can be used directly.
   * 
   * @param url The string representation of the URL to parse.
   */
  public static Map<String,String> extractURLParameters(String url)
  {
    try {
      // extract the query part of the supplied URL
      URL theURL = new URL(url);
      String queryString = theURL.getQuery();
      
      // prepare storage for output
      Map<String,String> parameterMap = null;
      
      // extract each name-value pair from query string (if any are specified in the URL)
      if (queryString != null && queryString.length() > 0)
      {
        // only initialise if there are some parameters
        parameterMap = new HashMap<String,String>();
        
        for (String parameter : queryString.split("&")) {
          String[] nameValueArr = parameter.split("=");
          
          String name = nameValueArr[0]; // parameter name must always be present
          String value = (nameValueArr.length == 2 ? nameValueArr[1] : null); // could be that parameter value is not set (e.g. "q=") - insert null then
          
          // decode possible special characters
          name = urlDecodeQuery(name);
          if (value != null) value = urlDecodeQuery(value);
          
          parameterMap.put(name, value);
        }
      }
      
      return (parameterMap);
    }
    catch (MalformedURLException e)
    {
      // some problem occurred - report it; can't return any data in this case
      System.err.println("Couldn't parse parameters of a URL: " + url + "; details below:");
      e.printStackTrace();
      return null;
    }
  }
  
  
  /**
   * This method is the opposite for <code>extractURLParameters(String url)</code>.
   * It takes a map of parameters, performs URL-encoding of each and assembles them
   * into a query string.
   * 
   * The query string then can be added to the <code>URL</code> object by using standard
   * Java API. 
   * 
   * @param urlParameters Map of parameters to use in query string construction.
   * @return URL-encoded query string.
   */
  public static String constructURLQueryString(Map<String,String> urlParameters)
  {
    if (urlParameters != null) {
      StringBuilder queryString = new StringBuilder();
      
      // iterate through all parameters and reconstruct the query string
      for (Map.Entry<String,String> parameter : urlParameters.entrySet())
      {
        if (queryString.length() > 0) queryString.append("&"); // parameter separator
        queryString.append(urlEncodeQuery(parameter.getKey()) + "=" + urlEncodeQuery(parameter.getValue()));
      }
      
      return (queryString.toString());
    }
    else {
      return (null);
    }
  }
  
  
  /**
   * Prepares the string to serve as a part of url query to the server.
   * @param query The string that needs URL encoding.
   * @return URL encoded string that can be inserted into the request URL.
   */
  public static String urlEncodeQuery(String query)
  {
    // "fast exit" - if null supplied, just return an empty string;
    // this is because in the URLs we have "q=", rather than "q=null" - this will cater for such cases
    if (query == null) return ("");
    
    // encode the query
    String strRes = "";
    try {
      strRes = URLEncoder.encode(query, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      // do nothing
    }
    
    return (strRes);
  }
  
  
  /**
   * Decodes a string which came as a part of of URL (e.g. a URL parameter). This converts
   * codes of escaped special characters back into those special characters.
   * 
   * @param query The string that needs URL decoded.
   * @return Decoded string that will contain all the special characters.
   */
  public static String urlDecodeQuery(String query)
  {
    String strRes = "";
    
    try {
      strRes = URLDecoder.decode(query, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      // do nothing
    }
    
    return (strRes);
  }
  
  
  /**
   * This method is "clones" an object supplied as an argument. It uses
   * serialisation to achieve this (as opposed to manually implementing deep
   * copying of all referenced objects in the graph of the provided object).
   * This technique is used to make sure that the new object will be exact
   * replica, but totally independent of the original one.
   * 
   * Note that this code works ~100 times slower than it would do if deep copying
   * was implemented. However, this will not be used in tight loops (and in loops
   * at all), so for one-off tasks it is fine.
   * 
   * @author Dave Miller<br/>
   * Original version of the code in this method is taken from
   * <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip76.html?page=2">
   *    http://www.javaworld.com/javaworld/javatips/jw-javatip76.html?page=2
   * </a> [accessed on 25/Feb/2010].
   * <br/><br/>
   * 
   * @author Subhajit Dasgupta<br/>
   * Example of using an alternative class loader during object de-serialisation
   * was taken from
   * <a href="http://blogs.sun.com/adventures/entry/desrializing_objects_custom_class_loaders">
   *    http://blogs.sun.com/adventures/entry/desrializing_objects_custom_class_loaders
   * </a> [accessed on 29/Mar/2010].
   * 
   * @return Deep copy of the provided object. If deep copying doesn't succeed,
   *         <code>null</code> is returned.
   */
  public static Object deepCopy(Object objectToCopy)
  {
    // a "safety net" - a class loader of BioCatalogue perspective may be used in
    // de-serialisation process to make sure that all classes are recognised
    // (system class loader may not be able to "see" all BioCatalogue plugin's files,
    //  but just those in Taverna's /lib folder)
    final ClassLoader[] customClassLoaders = new ClassLoader[] { BioCataloguePerspective.class.getClassLoader() };
    
    try
    {
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      try
      {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         oos = new ObjectOutputStream(bos);
         
         // serialise and pass the object
         oos.writeObject(objectToCopy);
         oos.flush();
         
         // read and return the new object
         ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
         ois = new ObjectInputStream(bin) {
                     /**
                      * <code>resolveClass()</code> method is overridden to make use of
                      * custom ClassLoader in the de-serialisation process.
                      * <br/>
                      * This is needed to make sure that the ClassLoader of the BioCatalogue
                      * perspective is used as opposed to the system ClassLoader which will
                      * only be able to see classes from Taverna's /lib folder.
                      */
                     protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
                     {
                       String className = desc.getName();
                       try {
                         // attempt to use default class loader
                         return Class.forName(className);
                       }
                       catch (ClassNotFoundException exc)
                       {
                         // default class loader was unable to locate a required class -
                         // attempt to use one of the provided class loaders
                         for (ClassLoader cl : customClassLoaders) {
                           try {
                             return cl.loadClass(className);
                           } 
                           catch (ClassNotFoundException e) {
                             /* do nothing here - there may be other class loaders to try */
                           }
                         }
                         // none of the class loaders was able to recognise the currently
                         // de-serialised class, so it's indeed an exception
                         throw new ClassNotFoundException(className + 
                             " -- neither system, nor alternative class loaders were able to load this class");
                       }
                     }
                   };
         return ois.readObject();
      }
      catch(Exception e)
      {
         System.err.println("ERROR: couldn't perform deep copy of " + objectToCopy.getClass() + " instance; details:\n");
         e.printStackTrace();
      }
      finally
      {
         oos.close();
         ois.close();
      }
    }
    catch (Exception e) {
      System.err.println("ERROR: couldn't close object streams during deep copy of " + objectToCopy.getClass() + " instance; details:\n");
      e.printStackTrace();
    }
    
    // Error occurred - couldn't produce the deep copy...
    return null;
  }
}
