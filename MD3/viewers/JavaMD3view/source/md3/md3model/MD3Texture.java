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
import java.awt.*;

import md3.util.*;

/**
 * <p>Objets of this class represent textures with associated metadata. The
 * texture data is stored in the OpenGL compatible RGBA format.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3Texture implements Serializable {
	
	/**
	 * <p>Name as specified in the originating .md3 model or .skin file.
	 */
	public String name;
	
  /**
   * <p>Filename of actual file from which data was loaded.
   */
	public String loadFilename;

  /**
   * <p>Actual texture bitmap data. These are unsigned bytes in RGBA order.
   */
  public byte[] data;
  public int width, height; // size of texture data
    
  /**
   * <p>Copy the source data into the data member of this MD3Texture object.
   *
   * <p>If ARGBtoRGBA is specified, byte order is changed from ARGB to RGBA,
   * when copying the texture data. Otherwise the source data is left unchanged
   * (so it should allready be RGBA!).
   */
  protected void setTextureData(int[] source, int sourceWidth, int sourceHeight, boolean ARGBtoRGBA) {      
    width=sourceWidth;
    height=sourceHeight;
    data=new byte[width*height*4];    
    
    //copy data
    int dataPtr=0;
    for (int h=0;h<sourceHeight;h++) //run through all rows
      for (int w=0;w<sourceWidth;w++) { //run through each row
        int sourcePtr=w + h*sourceWidth;
        if (ARGBtoRGBA) {
          data[dataPtr++]=ByteUtils.getByte(source[sourcePtr], 1);
          data[dataPtr++]=ByteUtils.getByte(source[sourcePtr], 2);
          data[dataPtr++]=ByteUtils.getByte(source[sourcePtr], 3);
          data[dataPtr++]=ByteUtils.getByte(source[sourcePtr], 0);
        }
        else
          for (int i=0;i<4;i++)
            data[dataPtr++]=ByteUtils.getByte(source[sourcePtr], i);
      }
  }
  	
  /**
   * <p>Load the texture data from the given input stream and put it in
   * the data array.
   *
   * <p>TGA or JPG input is selected depending on the loadFilename, so
   * make sure it is set before calling this method!
   */
  protected void loadTextureData(InputStream in) throws IOException {
  	if (loadFilename.toLowerCase().endsWith(".jpg")) {
			//read image data into byte array
			ByteArrayOutputStream bout=new ByteArrayOutputStream(in.available());  		
	    byte[] buf=new byte[1024];
	    int len=0;
	    while((len=in.read(buf)) > 0 )
	    	bout.write(buf, 0, len);  		
			byte[] imgData=bout.toByteArray();
	
	    // load texture bitmap data
      MediaTracker tracker=new MediaTracker(new Canvas()); //use dummy component
      Image img=Toolkit.getDefaultToolkit().createImage(imgData);
      tracker.addImage(img, 1);
      try { tracker.waitForID(1); } catch (InterruptedException e) {}
	
	 		ImageIO.loadImage(this, img);
	  }
	  else if (loadFilename.toLowerCase().endsWith(".tga"))
     	TGAIO.loadTGA(this, in);
  }

  /**
   * <p>Constructor for use in subclasses that loads texture data from the
   * specified file and initializes a new MD3Texture object with the read data/metadata.
   *
   * @param name Name of the texture as specified in .md3 or .skin file.
   * @param loadFilename Full name of file from which texture data will be loaded.
   */
	protected MD3Texture(String name, String loadFilename) throws IOException {
		loadFilename=loadFilename.trim();
		FileInputStream fin=new FileInputStream(loadFilename);
    this.name=name.trim();
    this.loadFilename=loadFilename;
		this.loadTextureData(fin);
    fin.close();
	}
	
  /**
   * <p>Constructor for use in subclasses that loads texture data from the sepcified
   * input stream and initializes a new MD3Texture object with the read data/metadata.
   * The given loadFilename is stored in the corresponding data member.
   *
   * @param name Name of the texture as specified in .md3 or .skin file.
   * @param loadFilename Full name of file from which texture data is (supposedly) loaded.
   * @param in Stream from to load texture data from.
   */
	protected MD3Texture(String name, String loadFilename, InputStream in) throws IOException {
		this.name=name.trim();
		this.loadFilename=loadFilename.trim();
		this.loadTextureData(in);
	}
	
	/**
	 * <p>Create a new uninitialized MD3Texture object.
	 */
	protected MD3Texture() {	
	}	
}