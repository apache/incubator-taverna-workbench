package org.apache.taverna.biocatalogue.test;
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

import org.apache.taverna.biocatalogue.ui.JWaitDialog;
import org.apache.taverna.ui.perspectives.biocatalogue.MainComponent;

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
