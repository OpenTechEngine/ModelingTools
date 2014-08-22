/*
Java MD3 Model Viewer - A Java based Quake 3 model viewer.
Copyright (C) 1999  Erwin 'KLR8' Vervaet

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package widgets.awt;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

/**
 * <p>This class implements a message dialog box. It supports mutiple lines
 * of text and an icon image.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MessageDialog extends Dialog {
  /**
   * <p>Create a new message dialog box. The message box will center itself in its
   * parent's coordinate space.
   *
   * @param parent The owner of the dialog.
   * @param title The title of the dialog.
   * @param modal If true, dialog blocks input to other app windows when shown.
   * @param iconURL URL of the image icon to display. If null, no icon will be displayed.
   * @param text The lines of text that should be displayed. Use an empty
   *             String for a blank line.
   * @param okButton If true, an OK button will be shown, if false there will be no
   *                 buttons on the dialog.
   */
  public MessageDialog(Frame parent, String title, boolean modal, URL iconURL, String[] text, boolean okButton) {
    super(parent, title, modal);
    
    this.setLayout(new BorderLayout(5,0));
    this.setResizable(false);

    //create canvas to draw icon
    if (iconURL!=null) {
      //load the image
      MediaTracker tracker=new MediaTracker(this);
      final Image icon=Toolkit.getDefaultToolkit().getImage(iconURL);
      tracker.addImage(icon, 1);
      try { tracker.waitForID(1); } catch (InterruptedException e) {}
      
      Canvas iconCanvas=new Canvas() {
        public Dimension getPreferredSize() {
          return new Dimension(icon.getWidth(this), icon.getHeight(this));
        }
              
        public void paint(Graphics g) {
          g.drawImage(icon,0,0,this);
        }
      };
      this.add(iconCanvas, BorderLayout.WEST);
    }

    //add lines of text
    Panel textPanel=new Panel(new GridLayout(text.length,1));
    this.add(textPanel, BorderLayout.CENTER);
    for (int i=0;i<text.length;i++) {
      Label line=new Label(text[i]);
      line.setBackground(new Color(SystemColor.control.getRGB()));
      textPanel.add(line);
    }
    
    //add ok button        
    if (okButton) {
      Button ok=new Button("OK");
      ok.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          MessageDialog.this.setVisible(false);
        }
      });
      Panel okPanel=new Panel();
      okPanel.setBackground(new Color(SystemColor.control.getRGB()));
      okPanel.add(ok);
      this.add(okPanel, BorderLayout.SOUTH);
    }
    
    //register listener
    this.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        MessageDialog.this.setVisible(false);
      }
    });
    
    this.pack();
  }
  
  public void setVisible(boolean b) {
    if (b)
    	//center with respect to parent
      this.setLocation(getOwner().getLocation().x + getOwner().getWidth()/2 - this.getWidth()/2,
                       getOwner().getLocation().y + getOwner().getHeight()/2 - this.getHeight()/2);
    
    super.setVisible(b);
  }
}