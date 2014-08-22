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
import java.util.*;

/**
 * <p>This class defines all available render modes for use with the MD3ViewGLCanvas
 * class. The class uses the <i>typesafe enum</i> pattern.
 *
 * @see md3.md3view.MD3ViewGLCanvas
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public abstract class MD3ViewGLCanvasRenderMode {
  private String name;
  
  //private constructor: only predefined object can be used.
  private MD3ViewGLCanvasRenderMode(String name) {
    this.name=name;
  }
  
  /**
   * <p>Wire frame render mode.
   */
  public static final MD3ViewGLCanvasRenderMode WIRE_FRAME =
    new MD3ViewGLCanvasRenderMode("Wire Frame") {
      public void apply(GLFunc gl) {
        gl.glPolygonMode( GLEnum.GL_FRONT_AND_BACK, GLEnum.GL_LINE );
        gl.glDisable( GLEnum.GL_TEXTURE_2D );
        gl.glDisable( GLEnum.GL_LIGHTING );
      }
    };

  /**
   * <p>Flat shaded render mode.
   */
  public static final MD3ViewGLCanvasRenderMode FLAT_SHADED =
    new MD3ViewGLCanvasRenderMode("Flat Shaded") {
      public void apply(GLFunc gl) {
        gl.glPolygonMode( GLEnum.GL_FRONT_AND_BACK, GLEnum.GL_FILL );
        gl.glDisable( GLEnum.GL_TEXTURE_2D );
        gl.glEnable( GLEnum.GL_LIGHTING );
      }
    };

  /**
   * <p>Flat textured render mode.
   */
  public static final MD3ViewGLCanvasRenderMode FLAT_TEXTURED =
    new MD3ViewGLCanvasRenderMode("Textured") {
      public void apply(GLFunc gl) {
        gl.glPolygonMode( GLEnum.GL_FRONT_AND_BACK, GLEnum.GL_FILL );
        gl.glEnable( GLEnum.GL_TEXTURE_2D );
        gl.glDisable( GLEnum.GL_LIGHTING );
      }
    };

  /**
   * <p>Shaded textured render mode.
   */
  public static final MD3ViewGLCanvasRenderMode SHADED_TEXTURED =
    new MD3ViewGLCanvasRenderMode("Textured Shaded") {
      public void apply(GLFunc gl) {
        gl.glPolygonMode( GLEnum.GL_FRONT_AND_BACK, GLEnum.GL_FILL );
        gl.glEnable( GLEnum.GL_TEXTURE_2D );
        gl.glEnable( GLEnum.GL_LIGHTING );
      }
    };
  
  /**
   * <p>Return a read-only list of all available render modes.
   */
  public static List renderModes() {
    MD3ViewGLCanvasRenderMode[] modes = { WIRE_FRAME, FLAT_SHADED, FLAT_TEXTURED, SHADED_TEXTURED };
    
    return Collections.unmodifiableList(Arrays.asList(modes));
  }
  
  /**
   * <p>Apply this render mode to given OpenGL context.
   */
  public abstract void apply(GLFunc gl);
  
  public String toString() {
    return name;
  }
}