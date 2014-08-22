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

import java.io.*;

import md3.md3model.*;

import gl4java.*;

/**
 * <p>This class extends MD3Mesh objects with OpenGL specific state.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3GLMesh extends MD3Mesh {
  /**
   * <p>Source parameter to BlendFunc function.
   */
  public int GLSrcBlendFunc;
  
  /**
   * <p>Destination parameter to BlendFunc function.
   */
  public int GLDstBlendFunc;
  
  /**
   * <p>Parameter to DepthMask function.
   */
  public boolean GLDepthMask;
  
  //no argument constructor added by Donald Gray
  protected MD3GLMesh() {
  	// assume opaque mesh
  	GLSrcBlendFunc=GLEnum.GL_ONE;
  	GLDstBlendFunc=GLEnum.GL_ZERO;
  	GLDepthMask=true; //GLEnum.GL_TRUE
  }

  protected MD3GLMesh(DataInput din) throws IOException {
  	super(din);

		//this should be changed!!!!
    //the blending info is in the .shader scripts    
  	if ((this.name.toLowerCase().indexOf("energy") != -1) ||
  		  (this.name.toLowerCase().indexOf("f_") != -1) ||
  		  (this.name.toLowerCase().indexOf("sphere") != -1) ||
  		  (this.name.toLowerCase().indexOf("flash") != -1)) {
  		//transparent mesh
  		GLSrcBlendFunc=GLEnum.GL_ONE;
  		GLDstBlendFunc=GLEnum.GL_ONE;
  		GLDepthMask=false; //GLEnum.GL_FALSE
  	}
  	else {
  		//opaque mesh
  		GLSrcBlendFunc=GLEnum.GL_ONE;
  		GLDstBlendFunc=GLEnum.GL_ZERO;
  		GLDepthMask=true; //GLEnum.GL_TRUE
  	}
  }
}