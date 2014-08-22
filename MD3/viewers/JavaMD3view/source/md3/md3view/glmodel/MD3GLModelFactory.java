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

/**
 * <p>An MD3Model factory that creates OpenGL compatible MD3 model objects, instead
 * of the default ones.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3GLModelFactory extends MD3ModelFactory {
	
  public MD3Model makeMD3Model(String name, InputStream in) throws IOException {
  	return new MD3GLModel(name, in);
  }
  
  //added by Donald Gray
  public MD3Model makeMD3Model() {
  	return new MD3GLModel();
  }
  
	public MD3Mesh makeMD3Mesh(DataInput din) throws IOException {
		return new MD3GLMesh(din);
	}
	
  //added by Donald Gray
	public MD3Mesh makeMD3Mesh() {
		return new MD3GLMesh();
	}	
  
  public MD3Texture makeMD3Texture(String name, String loadFilename) throws IOException {
    return new MD3GLTexture(name, loadFilename);
  }
  
  public MD3Texture makeMD3Texture(String name, String loadFilename, InputStream in) throws IOException {
    return new MD3GLTexture(name, loadFilename, in);
  }
}