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

import java.awt.*;
import java.awt.event.*;

/**
 * <p>This class implements the text viewer of the MD3View application.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ViewTextViewer extends Frame {
	private TextArea textArea;
	
	/**
	 * <p>Create a new text viewer frame.
	 */
	public MD3ViewTextViewer() {
		super();
		
		this.setLayout(new BorderLayout());
		this.setIconImage(MD3View.instance().getIconImage());
		
		textArea=new TextArea();
		textArea.setEditable(false);
		this.add(textArea, BorderLayout.CENTER);

    this.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        MD3ViewTextViewer.this.setVisible(false);
      }
    });
    
		this.pack();
    this.setSize(350, 250);		
	}
	
	/**
	 * <p>Set the text that the viewer displays.
	 */
	public void setText(String text) {
		textArea.setText(text);
	}
}