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

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import md3.util.*;

/**
 * <p>Booch utility class that provides Image IO functions. A method
 * is provided that you can use to convert java.awt.Image data into an
 * MD3Texture object.
 *  
 * @see md3.md3model.MD3Texture
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
class ImageIO {

	private ImageIO() {} //cannot instantiate

  /**
   * <p>Convert the given java.awt.Image into a MD3Texture object with OpenGL
   * compatible texture data. The texture bytes that end up in the data member
   * of the MD3Texture object have a RGBA structure and are unsigned.
   *
   * @param tex MD3Texture object that should hold the read data.
   * @param sourceImg Source image data.
   */
	static void loadImage(MD3Texture tex, Image sourceImg) throws IOException {
    //get source data buffer from source image
    int sourceWidth=sourceImg.getWidth(null);
    int sourceHeight=sourceImg.getHeight(null);
    int[] sourcePixBuf=new int[sourceWidth * sourceHeight];
	  PixelGrabber pg=new PixelGrabber(sourceImg, 0, 0, sourceWidth, sourceHeight, sourcePixBuf, 0, sourceWidth);
		try {
		  pg.grabPixels();
		} catch (InterruptedException e) {
			throw new IOException("interrupted waiting for pixels");
		}
		
		if ((pg.getStatus() & ImageObserver.ABORT) != 0)
			throw new IOException("image fetch aborted or errored");
      
    //pixels in sourcePixBuf are in ARGB order, convert to RGBA
    tex.setTextureData(sourcePixBuf, sourceWidth, sourceHeight, true);			
	}
}