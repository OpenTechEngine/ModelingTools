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
 * <p>This class extends MD3Texture objects with an OpenGL compatible texture binding.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3GLTexture extends MD3Texture {

  private static transient int topTextureBind=0;
    //simple index to keep track of texture bindings	

  /**
   * <p>OpenGL id the texture is bound to. 0 if unbound.
   */
  public int bind;
  	
  //we must update the topTextureBind to the highest allready generated binding
  //when deserializing objects    
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    if (this.bind>topTextureBind)
      topTextureBind=this.bind;
  }
  
  /**
   * <p>Loads a MD3 texture from the specified file and generates an OpenGL compatible
   * binding.
   *
   * @param name Name of the texture as specified in .md3 or .skin file.
   * @param loadFilename Full name of file from which texture data will be loaded.
	 */
  protected MD3GLTexture(String name, String loadFilename) throws IOException  {
	 	super(name, loadFilename);
    // generate a binding
    this.bind=++topTextureBind;
  }
  
  /**
   * <p>Loads a MD3 texture from the specified input stream and generates an OpenGL
   * compatible texture binding.
   *
   * @param name Name of the texture as specified in .md3 or .skin file.
   * @param loadFilename Full name of file from which the texture data is (supposedly) loaded.
   * @param in Stream from which to read data.
	 */
  protected MD3GLTexture(String name, String loadFilename, InputStream in) throws IOException {
  	super(name, loadFilename, in);
  	this.bind=++topTextureBind;
  }
  
  /**
   * <p>Reload this texture's bitmap data from the given input stream.
   */
  public void refreshData(InputStream in) throws IOException {
  	//throw away old data
  	this.data=null;
  	System.gc();
  	
  	//get new data
  	this.loadTextureData(in);
  }
}