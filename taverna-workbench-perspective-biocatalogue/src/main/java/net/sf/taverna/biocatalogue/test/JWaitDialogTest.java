package net.sf.taverna.biocatalogue.test;

import net.sf.taverna.biocatalogue.ui.JWaitDialog;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;

public class JWaitDialogTest
{

  public static void main(String[] args)
  {
    System.out.println("start test");
    
    final JWaitDialog jwd = new JWaitDialog(MainComponent.dummyOwnerJFrame, "Old title", "Please wait... Please wait... Please wait... Please wait...");
    
    // NB! Background process must be started before the modal dialog box
    //     is made visible - otherwise processing freezes.
    new Thread("testing delayed update of JWaitDialog")
    {
      public void run()
      {
        // wait for some time
        try { Thread.sleep(3000); }
        catch (InterruptedException e) { /* do nothing */ }
        
        // update the dialog
        jwd.setTitle("New title");
        jwd.waitFinished("Great, all done!");
        
        System.out.println("end test");
      }
    }.start();
    
    jwd.setVisible(true);
  }

}
