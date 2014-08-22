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

package md3.md3model;

import java.io.*;

import md3.util.*;

/**
 * <p>Represents metadata about an MD3 model bone animation frame. This includes
 * the bounding box for the animation frame and the tags associated with the frame.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3BoneFrame implements java.io.Serializable {
	
  /**
   * <p>Lower extrema of the bounding box of this bone animation frame.
   */
  public Vec3 mins=new Vec3();

  /**
   * <p>Upper extrema of the bounding box of this bone animation frame.
   */
  public Vec3 maxs=new Vec3();
  
  /**
   * <p>Coordinate origin within the bounding box.
   */
  public Vec3 position=new Vec3();
  public float scale;
  public String creator;

  /**
   * <p>Array of tags size tagNum, as defined in the MD3Model object that
   * owns this bone frame.
   *
   * @see md3.md3model.MD3Model
   */
  public MD3Tag[] tags;

  /**
   * <p>Create a MD3BoneFrame object with the data coming from the specified
   * input stream. Note that the tag information will NOT be initialized. This
   * has to be done separately!
   */
  protected MD3BoneFrame(int tagNum, DataInput din) throws IOException {
  	this(tagNum);
  	MD3IO.loadBoneFrame(this, din);
  }

	/**
	 * <p>Constructor for use in subclasses that creates an uninitialized object
	 * with the specified amount of tag positions.
	 */  
  protected MD3BoneFrame(int tagNum) {
  	tags=new MD3Tag[tagNum];
  }
}