package net.sf.taverna.biocatalogue.test;

import com.google.gson.Gson;

public class GSONTest
{

  public static void main(String[] args) throws Exception
  {
    String json = "[{\"annotatable\":{\"name\":\"IndexerService\",\"resource\":\"http://sandbox.biocatalogue.org/services/2158\",\"type\":\"Service\"},\"self\":\"http://sandbox.biocatalogue.org/annotations/47473\",\"value\":{\"resource\":\"http://sandbox.biocatalogue.org/tags/indexing\",\"type\":\"Tag\",\"content\":\"indexing\"},\"version\":1,\"created\":\"2010-01-13T09:24:04Z\",\"source\":{\"name\":\"Marco Roos\",\"resource\":\"http://sandbox.biocatalogue.org/users/48\",\"type\":\"User\"},\"attribute\":{\"name\":\"Tag\",\"resource\":\"http://sandbox.biocatalogue.org/annotation_attributes/2\",\"identifier\":\"http://www.biocatalogue.org/attribute#Category\"}}]";
    
    Gson gson = new Gson();
    AnnotationBean[] a = gson.fromJson(json, AnnotationBean[].class);
    
    System.out.println("Self URL: " + a[0].self);
    System.out.println("Annotatable resource: " + a[0].annotatable.resource);
  }

}
