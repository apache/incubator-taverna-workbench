package net.sf.taverna.biocatalogue.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.biocatalogue.model.connectivity.BeanForPOSTToFilteredIndex;

import com.google.gson.Gson;

public class GSONTest_exportingJSON
{

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    Map<String, String[]> m = new HashMap<String, String[]>();
    m.put("a", new String[] {"b","c"});
    m.put("d", new String[] {"e","f"});
    
    BeanForPOSTToFilteredIndex b = new BeanForPOSTToFilteredIndex();
    b.filters = m;
    
    Gson gson = new Gson();
    System.out.println(gson.toJson(b));

  }

}
