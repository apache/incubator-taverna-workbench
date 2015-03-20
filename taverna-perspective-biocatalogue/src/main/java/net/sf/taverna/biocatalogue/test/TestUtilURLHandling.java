package net.sf.taverna.biocatalogue.test;

import net.sf.taverna.biocatalogue.model.Util;

public class TestUtilURLHandling
{
  public static void main(String[] args)
  {
    String url1 = "http://sandbox.biocatalogue.org/search";
    String url2 = "http://sandbox.biocatalogue.org/search?";
    String url3 = "http://sandbox.biocatalogue.org/search?q=";
    String url4 = "http://sandbox.biocatalogue.org/search?q=franck";
    String url5 = "http://sandbox.biocatalogue.org/services?tag=%5Bbiology%5D";
    String url6 = "http://sandbox.biocatalogue.org/services?tag=%5B%3Chttp%3A%2F%2Fwww.mygrid.org.uk%2Fontology%23DDBJ%3E%5D";
    
    
    System.out.println("----------------------------");
    System.out.println("Extracting URL parameters:\n");
    
    
    System.out.println(url1 + "\nParameter map:\n" + Util.extractURLParameters(url1));
    System.out.println("Reconstructed query string from parameter map: " + Util.constructURLQueryString(Util.extractURLParameters(url1)) + "\n");
    
    
    System.out.println(url2 + "\nParameter map:\n" + Util.extractURLParameters(url2));
    System.out.println("Reconstructed query string from parameter map: " + Util.constructURLQueryString(Util.extractURLParameters(url2)) + "\n");
    
    
    System.out.println(url3 + "\nParameter map:\n" + Util.extractURLParameters(url3));
    System.out.println("Reconstructed query string from parameter map: " + Util.constructURLQueryString(Util.extractURLParameters(url3)) + "\n");
    
    
    System.out.println(url4 + "\nParameter map:\n" + Util.extractURLParameters(url4));
    System.out.println("Reconstructed query string from parameter map: " + Util.constructURLQueryString(Util.extractURLParameters(url4)) + "\n");
    
    
    System.out.println(url5 + "\nParameter map:\n" + Util.extractURLParameters(url5));
    System.out.println("Reconstructed query string from parameter map: " + Util.constructURLQueryString(Util.extractURLParameters(url5)) + "\n");
    
    
    System.out.println("\n\n----------------------------");
    System.out.println("Adding parameters:\n");
    
    String newUrl = Util.appendURLParameter(url1, "testParam", "testValue");
    System.out.println(url1 + "\n" + newUrl + "\n");
    
    newUrl = Util.appendURLParameter(url2, "testParam", "testValue");
    System.out.println(url2 + "\n" + newUrl + "\n");
    
    newUrl = Util.appendURLParameter(url3, "testParam", "testValue");
    System.out.println(url3 + "\n" + newUrl + "\n");
    
    newUrl = Util.appendURLParameter(url4, "testParam", "testValue");
    System.out.println(url4 + "\n" + newUrl + "\n");
    
    newUrl = Util.appendURLParameter(url5, "testParam", "testValue");
    System.out.println(url5 + "\n" + newUrl + "\n");
    
    
    System.out.println("\n\n----------------------------");
    System.out.println("Getting parameter values:\n");
    
    System.out.println("Value of '" + "testParam" + "' in the URL: " + url1 + " -- " + Util.extractURLParameter(url1, "testParam"));
    System.out.println("Value of '" + "testParam" + "' in the URL: " + url2 + " -- " + Util.extractURLParameter(url2, "testParam"));
    System.out.println("Value of '" + "q" + "' in the URL: " + url3 + " -- " + Util.extractURLParameter(url3, "q"));
    System.out.println("Value of '" + "q" + "' in the URL: " + url4 + " -- " + Util.extractURLParameter(url4, "q"));
    System.out.println("Value of '" + "tag" + "' in the URL: " + url5 + " -- " + Util.extractURLParameter(url5, "tag"));
    
    
    System.out.println("\n\n----------------------------");
    System.out.println("URL decoding:\n");
    
    System.out.println("Original URL: " + url6 + "\nDecoded URL: " + Util.urlDecodeQuery(url6));
    
    
    System.out.println("\n\n----------------------------");
    System.out.println("Appending a string before URL parameters:\n");
    
    String strToAppend = ".xml";
    System.out.println("Appending '" + strToAppend + "' in the URL: " + url1 + " -- " + Util.appendStringBeforeParametersOfURL(url1, strToAppend));
    System.out.println("Appending '" + strToAppend + "' in the URL: " + url2 + " -- " + Util.appendStringBeforeParametersOfURL(url2, strToAppend));
    System.out.println("Appending '" + strToAppend + "' in the URL: " + url3 + " -- " + Util.appendStringBeforeParametersOfURL(url3, strToAppend));
    System.out.println("Appending '" + strToAppend + "' in the URL: " + url4 + " -- " + Util.appendStringBeforeParametersOfURL(url4, strToAppend));
    System.out.println("Appending '" + strToAppend + "' in the URL: " + url5 + " -- " + Util.appendStringBeforeParametersOfURL(url5, strToAppend));
    System.out.println("Appending '" + strToAppend + "' in the URL: " + url6 + " -- " + Util.appendStringBeforeParametersOfURL(url6, strToAppend));
  }
}
