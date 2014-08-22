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
import java.awt.*;
import java.awt.image.*;
import java.io.*;

import md3.md3view.*;
import md3.md3model.*;
import md3.util.*;

import com.sun.image.codec.jpeg.*; //we use the Sun JPG encoder

/**
 * <p>An MD3Model visitor that walks through a MD3Model structure and
 * saves all texture coordinates of all meshes in the encountered models
 * to <i>texture coordinate maps</i> images.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */
public class MD3ModelExportTextureCoordinateMapsVisitor extends MD3ModelVisitor {
  private Component imgContext;

  //hash table with all uv map images, indexed by loadFilename
  HashMap uvMaps=new HashMap();
  
  //current image related data
  private int imgWidth=0;
  private int imgHeight=0;  
  private Graphics imgG=null;
  
  private int unknownMeshCounter=0;
  
  /**
   * <p>Create a new visitor.
   *
   * @param imgContext Component used as a context to create offscreen images.
   */
  public MD3ModelExportTextureCoordinateMapsVisitor(Component imgContext) {
    this.imgContext=imgContext;    
  }
  
  public void visit(MD3Model model) {
    //run trough all meshes in the model
    for (int i=0;i<model.meshNum;i++) {
      MD3Mesh mesh=model.meshes[i];
      
      //setup image to draw to
      if (mesh.textureNum>0 && mesh.textures[0]!=null)
        setupImage(mesh.textures[0].loadFilename, mesh.textures[0].width, mesh.textures[0].height);
      else
        setupImage(null, 64, 64);
      
      //export texture coordinates for this mesh
      for (int t=0;t<mesh.triangleNum;t++) {
        int[] xCoords=new int[3],
              yCoords=new int[3];
                
        for (int j=0;j<3;j++) {
            int[] tmp=getCoordFromUV(mesh.textureCoord[mesh.triangles[t][j]]);
            xCoords[j]=tmp[0];
            yCoords[j]=tmp[1];
        }
        
        //draw texture coord map on image
        imgG.drawPolygon(xCoords, yCoords, 3);
      }
    }
    
    //visit attached models
    Iterator it=model.linkedModels();
    while (it.hasNext())
      ((MD3Model)it.next()).accept(this);    
  }
  
  /**
   * <p>Flush all UV map image data to disk.
   */
  public void writeOut() {
    Iterator it=uvMaps.keySet().iterator();
    while (it.hasNext()) {
      String imgName=(String)it.next();
      Image img=(Image)uvMaps.get(imgName);
      String filename=MD3View.instance().showSaveDialog(FilenameUtils.getShortFilename(imgName) + "_uv_map.jpg");
      
      //write image data to file
      if (filename!=null) try{ 
        FileOutputStream out=new FileOutputStream(filename); 
        JPEGCodec.createJPEGEncoder(out).encode((BufferedImage)img); 
        out.flush(); 
        out.close();
      }       
      catch(IOException e){
        MD3View.instance().showExceptionDialog(e.getMessage());
      }
    }
  }
  
  //convert given U/V texture coord. to a (x, y) coordinate in the current image
  private int[] getCoordFromUV(float[] uv) {
    int[] res=new int[2];
    res[0]=(int)(imgWidth * uv[0]);
    res[1]=(int)(imgWidth * uv[1]);
    return res;    
  }
  
  //setup the current image data with given texture name, width & height
  //if texName==null a texture name will be generated
  private void setupImage(String texName, int width, int height) {
    if (texName==null) {
      texName="unknownTexture" + unknownMeshCounter;
      unknownMeshCounter++;
    }

    if (!uvMaps.containsKey(texName.toLowerCase())) { //we need to generate a new image
      Image img=imgContext.createImage(width, height);          
      setImage(img);
      
      //make background black
      imgG.setColor(Color.black);
      imgG.fillRect(0, 0, width, height);
      imgG.setColor(Color.white);
      
      //add new image to hash table
      uvMaps.put(texName.toLowerCase(),img);
    }        
    else // else go back to the old image
      setImage((Image)uvMaps.get(texName.toLowerCase()));
  }
  
  //set current image data to given image
  private void setImage(Image img) {
    imgWidth=img.getWidth(null);
    imgHeight=img.getHeight(null);
    imgG=img.getGraphics();
    imgG.setColor(Color.white);
  }  
}