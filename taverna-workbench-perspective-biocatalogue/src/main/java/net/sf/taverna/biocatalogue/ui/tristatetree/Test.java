package net.sf.taverna.biocatalogue.ui.tristatetree;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * @author Sergejs Aleksejevs
 */
public class Test extends JFrame
{
  public Test() {
    this.setLayout(new BorderLayout());
    
    this.add(new TriStateCheckBox("LOL", TriStateCheckBox.State.PARTIAL), BorderLayout.NORTH);
    
    TriStateTreeNode root = new TriStateTreeNode("root");
    TriStateTreeNode c1 = new TriStateTreeNode("child 1");
    TriStateTreeNode c2 = new TriStateTreeNode("child 2");
    TriStateTreeNode c3 = new TriStateTreeNode("child 3");
    
    TriStateTreeNode c1_1 = new TriStateTreeNode("child 1_1");
    TriStateTreeNode c1_2 = new TriStateTreeNode("child 1_2");
    TriStateTreeNode c1_3 = new TriStateTreeNode("child 1_3");
    
    TriStateTreeNode c2_1 = new TriStateTreeNode("child 2_1");
    TriStateTreeNode c2_2 = new TriStateTreeNode("child 2_2");
    TriStateTreeNode c2_3 = new TriStateTreeNode("child 2_3");
    
    TriStateTreeNode c3_1 = new TriStateTreeNode("child 3_1");
    TriStateTreeNode c3_2 = new TriStateTreeNode("child 3_2");
    TriStateTreeNode c3_3 = new TriStateTreeNode("child 3_3");
    
    TriStateTreeNode c1_1_1 = new TriStateTreeNode("child 1_1_1");
    TriStateTreeNode c1_1_2 = new TriStateTreeNode("child 1_1_2");
    TriStateTreeNode c1_1_3 = new TriStateTreeNode("child 1_1_3");
    
    // adding second level children
    root.add(c1); root.add(c2); root.add(c3);
    
    // adding third-level children
    c1.add(c1_1); c1.add(c1_2); c1.add(c1_3);
    c2.add(c2_1); c2.add(c2_2); c2.add(c2_3);
    c3.add(c3_1); c3.add(c3_2); c3.add(c3_3);
    
    // adding fourth-level children
    c1_1.add(c1_1_1); c1_1.add(c1_1_2); c1_1.add(c1_1_3);
    
    
    // NB! important to create the tree when 'root' is already populated with children
    JTriStateTree tree = new JTriStateTree(root);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    this.add(new JScrollPane(tree), BorderLayout.CENTER);
    
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.pack();
  }
  

  public static void main(String[] args) {
    JFrame a = new Test();
    a.setVisible(true);
  }

}