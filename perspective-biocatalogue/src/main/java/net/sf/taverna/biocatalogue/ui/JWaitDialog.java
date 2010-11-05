package net.sf.taverna.biocatalogue.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.t2.workbench.MainWindow;

/**
 * Creates a modal non-resizable dialog window.
 * 
 * Intended to be used for operations that consume some
 * time, but the user must wait for the to complete before
 * proceeding.
 * 
 * Initially the dialog shows a specified string message or
 * component and a "loader" bar - dynamic GIF image that
 * displays "activity" going on. At this stage the window
 * cannot be closed.
 * 
 * When the operation completes, the caller notifies the dialog
 * that it has finished, provides a new message / component to
 * display and allows the dialog to be closed.
 * 
 * If the operation doesn't complete within the specified time,
 * a timeout occurs and the dialog windows lets to close itself.
 * 
 * @author Sergejs Aleksejevs
 */
public class JWaitDialog extends JDialog
{
  private static final int DEFAULT_TIMEOUT = 10000;
  private static final ImageIcon LOADER_ICON = ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE);
  
  private JPanel jpInformationPanel;
  private JLabel jlLoaderIcon;
  
  private JButton bOK;
  private JPanel jpOKButton;
  
  private Timer timeoutTimer;
  private boolean hasTimedOut;
  
  
  /**
   * Creates a new Wait Dialog with no parent and default timeout on
   * operation - <code>JWaitDialog.DEFAULT_TIMEOUT</code>.
   * 
   * @param dialogTitle Title to set for the dialog window.
   * @param waitMessage Text to be displayed in the body of this dialog while
   *                    the user waits.
   */
  public JWaitDialog(String dialogTitle, String waitMessage) {
    this(null, dialogTitle, new JLabel(waitMessage, JLabel.CENTER), DEFAULT_TIMEOUT);
  }
  
  
  /**
   * Creates a new Wait Dialog with specified parent and default timeout on
   * operation - <code>JWaitDialog.DEFAULT_TIMEOUT</code>.
   * 
   * @param owner Specified JFrame is set as an owner for this Wait Dialog.
   * @param dialogTitle Title to set for the dialog window.
   * @param waitMessage Text to be displayed in the body of this dialog while
   *                    the user waits.
   */
  public JWaitDialog(JFrame owner, String dialogTitle, String waitMessage) {
    this(owner, dialogTitle, new JLabel(waitMessage, JLabel.CENTER), DEFAULT_TIMEOUT);
  }
  
  
  /**
   * Creates a new Wait Dialog with specified parent and timeout on
   * operation.
   * 
   * @param owner Specified JFrame is set as an owner for this Wait Dialog.
   * @param dialogTitle Title to set for the dialog window.
   * @param waitMessage Text to be displayed in the body of this dialog while
   *                    the user waits.
   * @param timeoutMillis Duration of the timeout on the operation - after this
   *                      time has passed the window will notify of the timeout
   *                      and allow to close itself. Value of 0 indicates that the timeout will never occur.
   */
  public JWaitDialog(JFrame owner, String dialogTitle, String waitMessage, int timeoutMillis) {
    this(owner, dialogTitle, new JLabel(waitMessage, JLabel.CENTER), timeoutMillis);
  }
  
  
  /**
   * Creates a new Wait Dialog with parent JFrame.
   * 
   * @param owner Specified JFrame is set as an owner for this Wait Dialog.
   * @param dialogTitle Title to set for the dialog window.
   * @param waitInformationComponent Component to be shown in the body of this
   *                    dialog windows while the user waits for an operation to complete.
   * @param timeoutMillis Duration of the timeout on the operation - after this
   *                      time has passed the window will notify of the timeout
   *                      and allow to close itself. Value of 0 indicates that the timeout will never occur.
   */
  public JWaitDialog(JFrame owner, String dialogTitle, JComponent waitInformationComponent, int timeoutMillis)
  {
    super(owner);
    this.setModal(true);
    this.setTitle(dialogTitle);
    
    // this will show the wait message to the user
    jpInformationPanel = new JPanel(new GridLayout());
    jpInformationPanel.add(waitInformationComponent);
    jpInformationPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
    
    // some graphical indication that the loading activity is going on
    jlLoaderIcon = new JLabel(LOADER_ICON);
    jlLoaderIcon.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
    
    // put components into the dialog box
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(jpInformationPanel, BorderLayout.CENTER);
    this.getContentPane().add(jlLoaderIcon, BorderLayout.SOUTH);
    
    this.pack();
    this.setResizable(false);
    this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    
    // center this window within the main Taverna Workbench window
    this.setLocationRelativeTo(MainWindow.getMainWindow());
    
    
    // start the timer - on timeout it will produce the
    // timeout message and allow to close the window
    hasTimedOut = false;
    if (timeoutMillis > 0)
    {
      timeoutTimer = new Timer();
      timeoutTimer.schedule(
          new TimerTask() {
            public void run() {
              waitFinished(new JLabel("<html><center>The operation did not complete within the " +
              		                    "allocated time.<br>Please try again.</center></html>",
              		                    UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER));
              hasTimedOut = true;
            }
          },
          timeoutMillis);
    }
  }
  
  
  public void waitFinished(String resultMessage) {
    waitFinished(new JLabel(resultMessage, JLabel.CENTER));
  }
  
  public void waitFinished(JComponent resultInformationComponent)
  {
    // this prevents the real response to be set after the
    // timeout message was already displayed
    if (!hasTimedOut)
    {
      // first of all stop the timeout timer: if this
      // method was called by the application explicitly, not on
      // timeout, we don't want the timeout message to appear after that
      if (timeoutTimer != null) { timeoutTimer.cancel(); }
      
      // change the information component
      jpInformationPanel.removeAll();
      jpInformationPanel.add(resultInformationComponent);
      
      // the OK button will allow closing the window
      bOK = new JButton("OK");
      bOK.setPreferredSize(new Dimension(LOADER_ICON.getIconWidth(), (int) (1.5 * LOADER_ICON.getIconHeight())));
      bOK.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // just remove the window
          dispose();
        }
      });
      bOK.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // a fallback mechanism - default button doesn't work for some reason
            // when the button is added into the dialog not in the constructor
            bOK.doClick();
          }
        }
      });
      bOK.setDefaultCapable(true);
      this.getRootPane().setDefaultButton(bOK);
      
      // wrap OK button into a panel to add empty borders
      jpOKButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
      jpOKButton.add(bOK);
      jpOKButton.setBorder(BorderFactory.createEmptyBorder(0, 20, 20 - (bOK.getPreferredSize().height - LOADER_ICON.getIconHeight()), 20));
      
      
      // add OK button instead of the loader icon 
      this.getContentPane().remove(jlLoaderIcon);
      this.getContentPane().add(jpOKButton, BorderLayout.SOUTH);
      this.bOK.requestFocusInWindow();
      
      // re-enable (X) button in the title bar
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      
      // update the size of this window - as the inner sizes of components have
      // been likely to change; then center the dialog box within its parent
      this.pack();
      this.setLocationRelativeTo(MainWindow.getMainWindow());
    }
  }
  
  
}
