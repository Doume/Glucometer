package main;
/*
 * Programming graphical user interfaces
3: * Example: DrawImage.java
4: * Jarkko Leponiemi 2003
5: */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DrawImage extends JPanel {
   
  private Image img = null;
   
   public DrawImage(String file) {
      // toolkit is an interface to the environment
      Toolkit toolkit = getToolkit();
      // create the image using the toolkit
      img = toolkit.createImage(file);
   }
   
   public void paint(Graphics g) {
      super.paint(g);
      // the size of the component
      Dimension d = getSize();
      // the internal margins of the component
      Insets i = getInsets();
      // draw to fill the entire component
      g.drawImage(img, i.left, i.top, d.width - i.left - i.right, d.height - i.top - i.bottom, this );
   }
   
}
