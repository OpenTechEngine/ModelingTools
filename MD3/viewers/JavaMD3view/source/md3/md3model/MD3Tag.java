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
 * <p>The MD3Tag class represents a so called 'tag', as they are found
 * in the MD3 model files. Tags can be seen as references to other
 * attached models, i.e. the weapon a player holds.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3Tag implements java.io.Serializable {
	
  /**
   * <p>Name of 'tag' as it's usually called in the md3 files. Try
   * to see it as a sub-mesh/seperate mesh-part.
   */
  public String name;
    
  /**
   * <p>Position of tag relative to the model that contains the tag.
   */
  public Vec3 position=new Vec3(); 
  
  /**
   * <p>3x3 rotation matrix.
   */
  public float[][] matrix=new float[3][3];
  
  /**
   * <p>Constructor that creates a new tag object and initializes
   * it with the data coming from the input stream.
   */
  protected MD3Tag(DataInput din) throws IOException {
  	MD3IO.loadTag(this, din);
  }
  
  /**
   * <p>Constructor that creates a new empty tag object.
   */
  protected MD3Tag() {
  }
}