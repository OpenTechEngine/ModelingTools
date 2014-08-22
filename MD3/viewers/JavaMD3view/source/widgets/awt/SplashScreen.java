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
 * <p>This class implements a splash screen that applications can show
 * on startup. It hides itself when clicked.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class SplashScreen extends Window {
  /**
   * <p>Create a new splash screen.
   *
   * @param parent The parent frame of the splash screen.
   * @param imgURL URL of the image to show.
   */
  public SplashScreen(Frame parent, URL imgURL) {
    super(parent);
    
    //load image
    MediaTracker tracker=new MediaTracker(this);
    final Image img=Toolkit.getDefaultToolkit().getImage(imgURL);
    tracker.addImage(img, 1);
    try { tracker.waitForID(1); } catch (InterruptedException e) {}    

    Canvas imgCanvas=new Canvas() {
      public Dimension getPreferredSize() {
        return new Dimension(img.getWidth(this), img.getHeight(this));
      }
      
      public void paint(Graphics g) {
        g.drawImage(img,0,0,this);
      }
    };
    
    this.add(imgCanvas, BorderLayout.CENTER);
    
    //add listener   
    imgCanvas.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        SplashScreen.this.setVisible(false);
      }
    });
    	        
    this.pack();
  }

  public void setVisible(boolean b) {  
    if (b)
    	//center in owner coord. space
      this.setLocation(getOwner().getLocation().x + getOwner().getWidth()/2 - this.getWidth()/2,
                       getOwner().getLocation().y + getOwner().getHeight()/2 - this.getHeight()/2);
    
    super.setVisible(b);
  }
}