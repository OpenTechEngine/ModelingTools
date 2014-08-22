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
 * <p>An MD3GLModel visitor that walks through a MD3GLModel structure and
 * applies a given skin to the visited models.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */
public class MD3ModelApplySkinVisitor extends MD3ModelVisitor {
	private MD3Skin skin;
	
	/**
	 * <p>Create a new skin application visitor for the given skin.
	 */
	public MD3ModelApplySkinVisitor(MD3Skin skin) {
		this.skin=skin;
	}
	
	public void visit(MD3Model model) {
		for (int i=0;i<model.meshNum;i++) {
			MD3Mesh mesh=model.meshes[i];
			
			MD3Texture tex=skin.getTexture(mesh.name);			
			if (tex!=null) {
        if (mesh.textureNum==0) { //mesh has no textures, allocate one
          mesh.textureNum=1;
          mesh.textures=new MD3Texture[mesh.textureNum];
        }
        
				for (int j=0;j<mesh.textureNum;j++)
					mesh.textures[j]=tex;
      }
		}
		
    Iterator it=model.linkedModels();
    while (it.hasNext())
      ((MD3Model)it.next()).accept(this);		
	}
}