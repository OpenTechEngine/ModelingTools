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

import java.util.*;
import java.io.*;

import md3.md3model.*;
import md3.md3view.glmodel.*;

/**
 * Abstract factory used by the viewer to create its md3 data objects.
 * The class itself has a static texture resource manager that will
 * cache read texture data.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */
public class MD3ViewGLModelFactory extends MD3GLModelFactory {

  private static transient Map textureRes=new HashMap();
    //map of texture names to MD3GLTexture objects, stores texture resources
    
  /**
   * <p>Factory method that loads a texture and enters it in the texture resource manager.
   *
   * <p>It will use the searchForPath() method of MD3View to resolve the loadFilename, so
   * the texture might be loaded from any data source the MD3View instance has.
   *
   * <p>In case of problems, the GUI (MD3View) will be used to notify the user (doesn't
   * throw exceptions!).
   *
   * @param name Name of the texture as specified in .md3 or .skin file.
   * @param loadFilename Full name of file from which texture data will be loaded.
   * @return The loaded MD3GLTexture object or null if there was a problem while loading the texture.
   */
  public MD3Texture makeMD3Texture(String name, String loadFilename) {
    String errorMsg=null;
    loadFilename=loadFilename.trim();
    
    if (loadFilename.length()==0)
      errorMsg="empty texture filename";
    else
      try {
        if (!(loadFilename.toLowerCase().endsWith(".tga") || loadFilename.toLowerCase().endsWith(".jpg")))
          loadFilename+=".tga";

        String path=MD3View.instance().searchForPath(loadFilename, false);
        InputStream in=MD3View.instance().getInputStreamForPath(path);
        MD3Texture res=makeMD3Texture(name, path, in);
        in.close();
        return res;
      }
      catch (IOException e) {
        //try other texture file types
        if (MD3ViewOptions.tryAltTexTypes)
          try {
            String ext;
            int i;
            if (loadFilename.toLowerCase().endsWith(".tga")) {
              i=loadFilename.toLowerCase().lastIndexOf(".tga");
              ext=".jpg";
            }
            else {
              i=loadFilename.toLowerCase().lastIndexOf(".jpg");
              ext=".tga";
            }
              
            if (i==-1)
              errorMsg="unable to try " + ext + " version for: " + loadFilename;
            else {
               loadFilename=loadFilename.substring(0, i) + ext;
               
              String path=MD3View.instance().searchForPath(loadFilename, false);
              InputStream in=MD3View.instance().getInputStreamForPath(path);
              MD3Texture res=makeMD3Texture(name, path, in);
              in.close();
              return res;
            }
          } 
          catch (IOException ex) {
            errorMsg=ex.getMessage();
          }
        else
          errorMsg=e.getMessage(); 
      }
    
    if (errorMsg!=null && MD3ViewOptions.warningOnTexLoad)
       MD3View.instance().showExceptionDialog(errorMsg);
      
    return null;
  }

  /**
   * <p>Loads a texture from the given input stream and enters it in the resource manager.
   *
   * <p>In case of problems, the GUI (MD3View) will be used to notify the user (doesn't
   * throw exceptions!).
   */
  public MD3Texture makeMD3Texture(String name, String loadFilename, InputStream in) {
    MD3Texture texture=null;
    
    try {
      // see if it has been loaded into the texture resource manager
      texture = (MD3GLTexture)textureRes.get(loadFilename);
  
      if (texture==null) { // if not we must load a new resource
        texture=super.makeMD3Texture(name, loadFilename, in);
          
        // insert texture keyed by file name
        textureRes.put(loadFilename, texture);
      }

      //record new name
      texture.name=name;
    } catch (IOException e) {
      if (MD3ViewOptions.warningOnTexLoad)  
        MD3View.instance().showExceptionDialog(e.getMessage());
    }
    
    return texture;    
  }

  /**
   * <p>Reloads cached texture data from source for given texture, if present in cache.
   */
  public static void refreshTexture(String loadFilename) {
    MD3GLTexture cachedTex=(MD3GLTexture)textureRes.get(loadFilename);
      
    if (cachedTex!=null)
      try {
        InputStream in=MD3View.instance().getInputStreamForPath(loadFilename);
        cachedTex.refreshData(in);
        in.close();
      } catch (java.io.IOException e) {
        if (MD3ViewOptions.warningOnTexLoad)  
          MD3View.instance().showExceptionDialog(e.getMessage());
      }
    else
      if (MD3ViewOptions.warningOnTexLoad)  
        MD3View.instance().showExceptionDialog(loadFilename + " not in texture cache");
  }  
}