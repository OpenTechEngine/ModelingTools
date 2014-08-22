/*-------------------
  UNFINISHED CODE!!
-------------------*/

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

package md3.md3view;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

/**
 * <p>Applet version of the MD3View application. This applet creates a button
 * that will pop up an MD3View frame with a specified serialized model in it.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ViewApplet extends Applet {
	
  private MD3View md3v;
  
  public void init() {
    //create MD3View window and make it visible
    //it must be visible, otherwise we can't render on it!
    md3v=MD3View.instance();
    md3v.setVisible(true);
    
    int skinNum=new Integer(getParameter("skinNum")).intValue();
    for (int i=0;i<skinNum;i++) {
      final int currentSkin=i; //we need this in the inner class
        
      Button showSkinButton=new Button("Show " + getParameter("skinName"+currentSkin) + " skin...");
      this.add(showSkinButton);      
      showSkinButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          md3v.setVisible(true);
          try {
            URL url=new URL(getDocumentBase(),getParameter("serializedModelFileName"+currentSkin));
            //md3v.loadModel(url);
          } catch (Exception ex) {
            MD3View.instance().showExceptionDialog(ex.getMessage());
          }
          md3v.md3canvas.sDisplay();
        }
      });
    }
  }
  
  public void destroy() {
    if (md3v!=null) md3v.shutdown();
  }  
}