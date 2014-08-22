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

package md3.md3view.visitor;

import java.util.*;

import gl4java.*;

import md3.md3model.*;
import md3.md3view.glmodel.*;
import md3.md3view.*;

/**
 * <p>An MD3GLModel visitor that walks through a MD3GLModel structure and
 * uploads all texture data of the visited models into a specified
 * OpenGL context.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */
public class MD3GLModelUploadTextureVisitor extends MD3GLModelVisitor {
  private MD3ViewGLCanvas md3canvas;
  
  /**
   * <p>Create a new texture upload visitor that will upload data into
   * the OpenGL context of the specified MD3ViewGLCanvas.
   *
   * @param md3canvas Texture data will be uploaded to this object's OpenGL context.
   */
  public MD3GLModelUploadTextureVisitor(MD3ViewGLCanvas md3canvas) {
    this.md3canvas=md3canvas;    
  }
  
  /**
   * <p>Upload texture information associated with a model into the OpenGL Context.
   *
   * @param model The model of which the textures will be uploaded.
   */
  public void visit(MD3GLModel model) {
    // upload textures of this model to OpenGL
    for (int i=0;i<model.meshNum;i++)
      for (int j=0;j<model.meshes[i].textureNum;j++)
        uploadTextureData(md3canvas, (MD3GLTexture)model.meshes[i].textures[j]);
      
    // upload texture data of children
    Iterator it=model.linkedModels();
    while (it.hasNext()) {
      MD3Model child=(MD3Model)it.next();
      child.accept(this);
    }
  }
  
  //naive way of getting 'relevant' (64, 128 or 256) nr smaller or equal to given number
  private static int getRelevantPart(int nr) {
    if (nr < 64)
      return 0;
    else if (64 <= nr && nr < 128)
      return 64;
    else if (128 <= nr && nr < 256)
      return 128;
    else
      return 256;
  }

  //clip non relevant parts of given texture
  private static byte[] getClippedTextureData(int targetWidth, int targetHeight, byte[] source, int sourceWidth, int sourceHeight) {
    byte[] res=new byte[targetWidth*targetHeight*4];    

    for (int h=0;h<targetHeight;h++)
      for (int w=0;w<sourceWidth*4;w++)
        if (w<(targetWidth*4)) //relevant pixel?
          res[h*targetWidth*4 + w]=source[h*sourceWidth*4 + w];
    
    return res;
  }

  /**
   * <p>Upload the data of the given texture into the GL context of the given canvas.
   */
  public static void uploadTextureData(MD3ViewGLCanvas md3canvas, MD3GLTexture texture) {
    GLFunc gl=md3canvas.getGL();
    
    // set active texture binding and set texture parameters
    gl.glBindTexture( GLEnum.GL_TEXTURE_2D, texture==null?0:texture.bind );
    gl.glTexEnvf(GLEnum.GL_TEXTURE_ENV, GLEnum.GL_TEXTURE_ENV_MODE, GLEnum.GL_MODULATE);
    gl.glHint( GLEnum.GL_PERSPECTIVE_CORRECTION_HINT, GLEnum.GL_NICEST );

    if (md3canvas.mipmapping) {
      if (texture!=null) {
        //filtered texture mode
        gl.glTexParameteri(GLEnum.GL_TEXTURE_2D, GLEnum.GL_TEXTURE_MAG_FILTER, GLEnum.GL_LINEAR);
        gl.glTexParameteri(GLEnum.GL_TEXTURE_2D, GLEnum.GL_TEXTURE_MIN_FILTER, GLEnum.GL_LINEAR_MIPMAP_LINEAR); 
        md3canvas.getGLU().gluBuild2DMipmaps(GLEnum.GL_TEXTURE_2D, 4, texture.width, texture.height, GLEnum.GL_RGBA, GLEnum.GL_UNSIGNED_BYTE, texture.data);
      }      
    }
    else {
      if (texture!=null) {
        //we need to clip the texture to an OpenGL compatible size (OpenGL textures should have width & height
        //of 64, 128 or 256 pixels when not using mipmapping).
        int width=getRelevantPart(texture.width);
        int height=getRelevantPart(texture.height);
        byte[] data=getClippedTextureData(width, height, texture.data, texture.width, texture.height);
        
        //filtered texture mode
        gl.glTexParameterf(GLEnum.GL_TEXTURE_2D, GLEnum.GL_TEXTURE_MAG_FILTER, GLEnum.GL_LINEAR);
        gl.glTexParameterf(GLEnum.GL_TEXTURE_2D, GLEnum.GL_TEXTURE_MIN_FILTER, GLEnum.GL_LINEAR);        
        gl.glTexImage2D( GLEnum.GL_TEXTURE_2D, 0, 4, width, height, 0, GLEnum.GL_RGBA, GLEnum.GL_UNSIGNED_BYTE, data );
      }
    }
  }
}