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
import md3.md3view.glmodel.*;
import md3.md3view.*;

/**
 * <p>An MD3GLModel visitor that walks through a MD3GLModel structure and
 * refreshes (reloads from file) all texture data of the visited models.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */
public class MD3GLModelRefreshTextureVisitor extends MD3GLModelVisitor {			
	public void visit(MD3GLModel model) {
		for (int i=0;i<model.meshNum;i++) {
			MD3Mesh mesh=model.meshes[i];
			for (int j=0;j<mesh.textureNum;j++)
				if (mesh.textures[j]!=null)
					MD3ViewGLModelFactory.refreshTexture(mesh.textures[j].loadFilename);
		}
		
    Iterator it=model.linkedModels();
    while (it.hasNext())
      ((MD3Model)it.next()).accept(this);		
	}	
}