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

import java.io.*;
import java.util.*;

import md3.md3model.*;
import md3.md3view.*;
import md3.util.*;

/**
 * <p>An MD3Model visitor that walks through a MD3Model structure and
 * saves all textures of the visited models to files in a given directory.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */
public class MD3ModelExportTexturesVisitor extends MD3ModelVisitor {
	
	private String targetDir;
	
	public MD3ModelExportTexturesVisitor(String targetDir) {
		this.targetDir=targetDir;
	}
				
	public void visit(MD3Model model) {
		for (int i=0;i<model.meshNum;i++) {
			MD3Mesh mesh=model.meshes[i];
			for (int j=0;j<mesh.textureNum;j++)
				if (mesh.textures[j]!=null) try {
					File f=new File(targetDir, new File(mesh.textures[j].loadFilename).getName());
					if (!f.exists()) {
	          FileOutputStream out=new FileOutputStream(f);
	          InputStream in=MD3View.instance().getInputStreamForPath(mesh.textures[j].loadFilename);	
	          
	          ByteUtils.copyAllBytes(in, out);
					
					  in.close();
	          out.close();						
					}
				} catch (IOException e) {
					MD3View.instance().showExceptionDialog(e.getMessage());
				}
				
		}
		
    Iterator it=model.linkedModels();
    while (it.hasNext())
      ((MD3Model)it.next()).accept(this);		
	}	
}