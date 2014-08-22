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

package md3.util;

/**
 * <p>The Vec3 class represents a 3-tuple of floats: (float, float, float).
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public final class Vec3 implements java.io.Serializable {
  //The 3-tuple data of this object.
  public float x, y, z;
  
  /**
   * <p>Default constructor.
   */
  public Vec3() {
  }
  
  /**
   * <p>Create a Vec3 object with given float tuple data.
   */
  public Vec3(float x, float y, float z) {
  	this.x=x;
  	this.y=y;
  	this.z=z;
  }
  
  /**
   * <p>Create a Vec3 object with given float array tuple data.
   */
  public Vec3(float[] v) {
  	if (v.length!=3)
  		throw new IllegalArgumentException();
  	else {
  		x=v[0];
  		y=v[1];
  		z=v[2];
  	}
  }
  
  /**
   * <p>Return the i-th element of the 3-tuple.
   */
  public float v(int i) {
  	switch(i) {
  		case 0: return x;
  		case 1: return y;
  		case 2: return z;
  		default: throw new ArrayIndexOutOfBoundsException(i);
  	}
  }
    
  /**
   * <p>Computes vector cross product.
   *
   * @param vec tuple to do the cross product with
   * @return this * vec
   */
  public Vec3 cross(Vec3 vec) {
    Vec3 res=new Vec3();
    res.x = this.y*vec.z - this.z*vec.y;
    res.y = this.z*vec.x - this.x*vec.z;
    res.z = this.x*vec.y - this.y*vec.x;
    return res;
  }
  
  /**
   * <p>Makes this Vec3 of normal unit length.
   */
  public void normalize() {
    float length = (float)Math.sqrt((double)(x*x + y*y + z*z));
    if (length != 0){
    	x/=length;
    	y/=length;
    	z/=length;
    }
  }
  
  public String toString() {
  	return "(" + x + "," + y + "," + z + ")";
  }
}