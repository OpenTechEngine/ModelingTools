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

package md3.md3view;

import gl4java.*;

import md3.md3view.glmodel.*;

/**
 * <p>This class provides functionality to render a texture directely
 * on a GLCanvas.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ViewGLTextureRenderer {
	
	/**
	 * <p>Render the given texture on the given OpenGL canvas.
	 */
	public static void renderTexture(MD3ViewGLCanvas canvas, MD3GLTexture tex) {
		GLFunc gl=canvas.getGL();
		
		float x=tex.width/8f;
		float z=tex.height/8f;
		
		gl.glBindTexture(GLEnum.GL_TEXTURE_2D, tex.bind);

		gl.glBegin(GLEnum.GL_QUADS);
		gl.glTexCoord2f(0.0f, 0.0f); 
		gl.glVertex3f(-x, 0.0f, z);
		gl.glTexCoord2f(1.0f, 0.0f); 
		gl.glVertex3f( x, 0.0f, z);
		gl.glTexCoord2f(1.0f, 1.0f); 
		gl.glVertex3f( x, 0.0f, -z);
		gl.glTexCoord2f(0.0f, 1.0f); 
		gl.glVertex3f(-x, 0.0f, -z);
		gl.glEnd();		
	}
}