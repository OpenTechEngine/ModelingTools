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
import java.io.File;

import md3.md3model.*;

/**
 * <p>An MD3Model visitor that walks through an MD3Model structure and
 * collects different properties of the encountered models (number of
 * triangles, vertices, ...).
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3ModelGetPropertiesVisitor extends MD3ModelVisitor {
	private static final int LINE_LENGTH=150;
  private List infoText;
  private int totalVertexNum=0, totalTriangleNum=0;
  
  /**
   * <p>Create a new property collecting visitor.
   */
  public MD3ModelGetPropertiesVisitor() {
    infoText=new ArrayList();
  }
  
  public void visit(MD3Model model) {
  	String modelMeshes="";
    int modelVertexNum=0, modelTriangleNum=0;
    for (int i=0;i<model.meshNum;i++) {
    	modelMeshes+=model.meshes[i].name + ((i==model.meshNum-1)?(""):(", "));
      modelVertexNum+=model.meshes[i].vertexNum;
      modelTriangleNum+=model.meshes[i].triangleNum;
    }
    
    totalVertexNum+=modelVertexNum;
    totalTriangleNum+=modelTriangleNum;

		infoText.add(model.loadFilename);    
    infoText.add("     " + model.tagNum + " tag" + ((model.tagNum==1)?(", "):("s, ")) +
    						 model.boneFrameNum + " animation frame" + ((model.boneFrameNum==1)?(", "):("s, ")) +
                 modelVertexNum + " vert" + ((modelVertexNum==1)?("ex, "):("ices, ")) +
                 modelTriangleNum + " triangle" + ((modelTriangleNum==1)?(","):("s,"))
                );
    
    //add meshes to infoText, adding line breaks as necessary            
    infoText.add("     " + model.meshNum + " mesh" + ((model.meshNum==1)?(":"):("es:")));
    if (modelMeshes.length()>LINE_LENGTH)
    	while (modelMeshes.length()>LINE_LENGTH) {
    		int end=LINE_LENGTH + modelMeshes.substring(LINE_LENGTH).indexOf(',');
    		if (end==LINE_LENGTH-1) {
    			infoText.add("         " + modelMeshes.trim());
    			modelMeshes="";	
    		}
    		else {
		   		infoText.add("         " + modelMeshes.substring(0, end + 1));
    			modelMeshes=modelMeshes.substring(end + 1).trim();
    		}
    	}    	
   	infoText.add("         " + modelMeshes);
    
    //visit children
    Iterator it=model.linkedModels();
    while (it.hasNext())
      ((MD3Model)it.next()).accept(this);
  }
  
  /**
   * <p>Return the property information collected by this visitor as
   * an array of Strings.
   *
   * @return The collected information.
   */
  public String[] toText() {
    infoText.add(""); //add empty line
    infoText.add("Totals: " + totalVertexNum + " vertices, " + totalTriangleNum + " triangles");
    
    String[] res=new String[infoText.size()];
    for (int i=0;i<infoText.size();i++)
      res[i]=(String)infoText.get(i);
    return res;    
  }
}