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

import md3.md3model.*;

/**
 * <p>An MD3Model visitor that walks through a MD3Model structure and
 * replaces all textures the structure uses with a specified texture.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */
public class MD3ModelApplyTextureVisitor extends MD3ModelVisitor {
  private MD3Texture texture;
  
  /** 
   * <p>Create a new visitor.
   *
   * @param texture The texture to apply to the models.
   */
  public MD3ModelApplyTextureVisitor(MD3Texture texture) {
    this.texture=texture;
  }
  
  public void visit(MD3Model model) {
    //apply texture to this model
    for (int i=0;i<model.meshNum;i++) {
      if (model.meshes[i].textureNum==0) { //mesh has no textures, allocate one
        model.meshes[i].textureNum=1;
        model.meshes[i].textures=new MD3Texture[model.meshes[i].textureNum];
      }
      
      for (int j=0;j<model.meshes[i].textureNum;j++)
        model.meshes[i].textures[j]=texture;
    }
        
    //apply texture to linked models
    Iterator it=model.linkedModels();
    while (it.hasNext())
      ((MD3Model)it.next()).accept(this);
  }
}