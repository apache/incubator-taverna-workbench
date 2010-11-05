package net.sf.taverna.biocatalogue.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


/**
 * This class converts a regular string into an InputStream.
 * 
 * Can be used, for example, to display an HTML string in an
 * XHTMLRenderer pane, which only accepts InputStream as its input.
 * 
 * @author Sergejs Aleksejevs
 */
public class StringToInputStreamConverter
{
  PipedOutputStream out;
  PipedInputStream in;
  ObjectInputStream objIn;
  
  
  /**
   * Constructs the InputStream from provided <code>strInput</code>.
   * 
   * Call <code>getInputStream()</code> after the constructor to get the
   * produced stream.
   * 
   * @param strInput The string which is to be converted into the <code>InputStream</code>.
   */
  public StringToInputStreamConverter(final String strInput)
  {
    try
    {
      // NB! input pipe should be connected to output before any further actions
      // with the pipes are performed
      out = new PipedOutputStream();
      in = new PipedInputStream(out);
      
      new Thread("") {
        public void run(){
          try {
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeBytes(strInput);
            objOut.close();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }.start();
      
      objIn = new ObjectInputStream(in);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  
  /**
   * @return InputStream initialised to provide contents of the string used in
   *         creation of this instance of the converter or <code>null</code> if
   *         an error occurred during creation of the stream.
   */
  public InputStream getInputStream() {
    return objIn;
  }
  
  
  /**
   * To be called when all operations with this converter are completed.
   */
  public void closeAllStreams()
  {
    try {
      out.close();
      in.close();
      objIn.close();
    }
    catch (IOException e) {
      // do nothing, this was a cleanup operation
    }
  }
}
