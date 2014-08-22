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

package md3.md3view.glmodel;

import md3.md3model.*;

/**
 * <p>This class extends MD3Model objects with animation state data.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3GLModel extends MD3Model {
	
  /**
   * <p>Currently rendered animation key frame of this model. Note that this
   * model could actually be only a part of a larger model. Every part
   * has its own current frame!
   */
  public int currentFrame;
  
  /**
   * <p>Next animation key frame of the model.
   */
  public int nextFrame;
  
  /**
   * <p>Interpolation position between currentFrame and nextFrame. The value
   * should be in the interval [0,1]!
   */
  public transient float interpolationFraction;
  
  //no argument constructor added by Donald Gray
  protected MD3GLModel() {
  	this.currentFrame=0;
  	this.nextFrame=1;
  	this.interpolationFraction=0.0f;
  }

  protected MD3GLModel(String filename) throws java.io.IOException {
  	super(filename);
  	this.currentFrame=0;
  	this.nextFrame=this.boneFrameNum>0?1:0;
  	this.interpolationFraction=0.0f;
  }
  
  protected MD3GLModel(String name, java.io.InputStream in) throws java.io.IOException {
  	super(name, in);
  	this.currentFrame=0;
  	this.nextFrame=this.boneFrameNum>0?1:0;
  	this.interpolationFraction=0.0f;  	
  }
  
  public void accept(MD3GLModelVisitor v) {    
  	v.visit(this);
  }
}