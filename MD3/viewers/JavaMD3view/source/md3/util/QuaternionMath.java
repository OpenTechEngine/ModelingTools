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
 * <p>Booch utility class with quaternion math operations. The quaternions handled
 * by this class are represented as <tt>float[] quat</tt> with <tt>quat.length==4</tt>.
 * The structure of the arrays is as follows: <tt>quat[0]==X</tt>, <tt>quat[1]==Y</tt>,
 * <tt>quat[2]==Z</tt> and <tt>quat[3]==S</tt>, so the scalar part comes last!
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public final class QuaternionMath {
  private static final int X=0, Y=1, Z=2, S=3;
  private	static final float DELTA = 0.01f; // amount of error until quaternion spherical interpolation falls back on linear
  	
	private QuaternionMath() {} //cannot instantiate

  /**
   * <p>Creates a 3x3 rotation matrix from a given quaternion.
   *
   * @param quat The source quaternion.
   * @return The equivalent 3x3 rotation matrix.
   */
  public static final float[][] matrixFromQuaternion(float[] quat) {
  	if (quat.length!=4)
  		throw new IllegalArgumentException("quaternion must be of length 4");
  		
		float[][] res=new float[3][3];		

    // calculate coefficients
    float x2 = quat[X] * quat[X], y2 = quat[Y] * quat[Y], z2 = quat[Z] * quat[Z],
    			xy = quat[X] * quat[Y], xz = quat[X] * quat[Z], yz = quat[Y] * quat[Z],
    			sx = quat[S] * quat[X], sy = quat[S] * quat[Y], sz = quat[S] * quat[Z];
        
    res[0][0] = 1.0f - 2.0f*y2 - 2.0f*z2; res[0][1] = 2.0f*xy - 2.0f*sz;	  res[0][2] = 2.0f*xz + 2.0f*sy;
    res[1][0] = 2.0f*xy + 2.0f*sz; 				res[1][1] = 1.0f - 2.0f*x2 - 2.0f*z2; res[1][2] = 2.0f*yz - 2.0f*sx;
    res[2][0] = 2.0f*xz - 2.0f*sy; 				res[2][1] = 2.0f*yz + 2.0f*sx;	  res[2][2] = 1.0f - 2.0f*x2 - 2.0f*y2;
    
    return res;
  }

  /**
   * <p>Creates a quaternion from a 3x3 rotation matrix.
   *
   * @param mat The 3x3 source rotation matrix.
   * @return The equivalent 4 float quaternion.
   */
  public static final float[] quaternionFromMatrix(float[][] mat) {
  	if (mat.length!=3 || mat[0].length!=3 || mat[1].length!=3 || mat[2].length!=3)
  		throw new IllegalArgumentException("matrix must be 3x3");

  	float[] quat=new float[4];  	
    final int[] NXT = {1, 2, 0};  

	  // check the diagonal
	  float tr = mat[0][0] + mat[1][1] + mat[2][2];	
	  if (tr > 0.0f) {
      float s = (float)Math.sqrt(tr + 1.0f);
	    quat[S] = s * 0.5f;
      s = 0.5f / s;
	    quat[X] = (mat[1][2] - mat[2][1]) * s;
	    quat[Y] = (mat[2][0] - mat[0][2]) * s;
	    quat[Z] = (mat[0][1] - mat[1][0]) * s;
	  } else { //diagonal is negative
	  	//get biggest diagonal element
	    int i = 0;
	    if (mat[1][1] > mat[0][0]) i = 1;
	    if (mat[2][2] > mat[i][i]) i = 2;
	    //setup index sequence
	    int j = NXT[i];
	    int k = NXT[j];
      
      float s = (float)Math.sqrt((mat[i][i] - (mat[j][j] + mat[k][k])) + 1.0f);
	
	    quat[i] = s * 0.5f;
      
      if (s != 0.0f) s = 0.5f / s;
      
	    quat[j] = (mat[i][j] + mat[j][i]) * s;
	    quat[k] = (mat[i][k] + mat[k][i]) * s;
	    quat[3] = (mat[j][k] - mat[k][j]) * s;
	  }	  
	  
	  return quat;
  }

  /**
   * <p>Do slerp (spherical linear interpolation) interpolation of quaternions along unit 4d sphere.
   *
   * @param from Start rotation.
   * @param to End rotation.   
   * @param frac Specifies the slerp interpolation fraction.
   * @return Quaternion describing rotation interpolated between from and to quaternions.
   */
  public static final float[] quaternionSlerp(float[] from, float[] to, float frac) {
  	if (from.length!=4 || to.length!=4)
  		throw new IllegalArgumentException("quaternion must be of length 4");

		float[] res=new float[4];
	
	  // calc cosine
	  float costheta = from[X]*to[X] + from[Y]*to[Y] + from[Z]*to[Z] + from[S]*to[S];
	
	  // adjust signs (if necessary)
	  float flip;
	  if ( costheta < 0.0f ) { 
		  costheta = -costheta;
		  flip = -1.0f;
	  } else 
	  	flip = 1.0f;
	
	  // calculate coefficients
    float scale0, scale1;
	  if ( (1.0f - costheta) > DELTA ) {
      // standard case (slerp)
      float theta = (float)Math.acos(costheta);
      float sintheta = (float)Math.sin(theta);
      scale0 = (float)Math.sin((1.0 - frac) * theta) / sintheta;
      scale1 = (float)Math.sin(frac * theta) / sintheta;
    } else {        
      // "from" and "to" quaternions are very close 
      //  ... so we can do a linear interpolation
      scale0 = 1.0f - frac;
      scale1 = frac;
    }
   
	  // calculate final values
	  res[X] = scale0 * from[X] + flip * scale1 * to[X];
	  res[Y] = scale0 * from[Y] + flip * scale1 * to[Y];
	  res[Z] = scale0 * from[Z] + flip * scale1 * to[Z];
	  res[S] = scale0 * from[S] + flip * scale1 * to[S];
	 
	  return res;
  }
}