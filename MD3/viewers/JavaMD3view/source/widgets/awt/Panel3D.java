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

/**
 * <p>This class implements a Panel container with a 3D look.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class Panel3D extends Panel {

  /**
   * <p>Depressed 3D look.
   */
  public static final int DEPRESSED = 1;

  /**
   * <p>Protruded 3D look.
   */
  public static final int PROTRUDED = 2;

  private int type=PROTRUDED;

  /**
   * <p>Default constructor. Initially the 3D type is PROTRUDED.
   */
  public Panel3D() {
  	super();
  }

  /**
   * <p>Create a 3D panel with given layout manager. Initially the
   * 3D type is PROTRUDED.
   */
  public Panel3D(LayoutManager lm) {
  	super(lm);
  }

  /**
   * <p>Set the type of 3D look the panel will have. Either PROTRUDED
   * or DEPRESSED.
   */
  public void setPanel3DType(int t) {
    if (t == DEPRESSED || t == PROTRUDED) {
      type = t;
      repaint();
    }
    else
    	throw new IllegalArgumentException(String.valueOf(t));
  }

  public void paint(Graphics g) {
    super.paint(g);
    
    if(type == DEPRESSED)
      g.setColor(Color.black);
    else
      g.setColor(Color.white);
      
    g.drawLine(0, 0, getWidth()-1, 0);
    g.drawLine(0, 0, 0, getHeight()-1);
    
    if(type == DEPRESSED)
      g.setColor(Color.white);
    else
      g.setColor(Color.black);
      
    g.drawLine(0, getHeight()-1, getWidth()-1, getHeight()-1);
    g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight()-1);
  }
  
  public Insets getInsets() {
  	return new Insets(1, 4, 1, 4);
  }
}