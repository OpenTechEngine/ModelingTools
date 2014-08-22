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
 * <p>Booch utility class with matrix math operations.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public final class MatrixMath {
	private MatrixMath() {} //cannot instantiate
  
  /**
   * <p>Float matrix multiplication.
   *
   * @return a*b or null if a and b have incompatible dimensions.
   */
  public static final float[][] mult(float[][] a, float[][] b) {
    float[][] res;

    if (a[0].length!=b.length)
      res=null;
    else {
      res=new float[a.length][b[0].length];
      for (int i=0;i<res.length;i++)
        for (int j=0;j<res[i].length;j++)
          for (int k=0;k<a[0].length;k++)
            res[i][j]+=a[i][k]*b[k][j];
    }

    return res;
  }  
}