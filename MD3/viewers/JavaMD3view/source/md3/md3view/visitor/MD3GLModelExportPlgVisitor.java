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
import md3.md3view.glmodel.*;
import md3.util.*;

/**
 * <p>An MD3GLModel visitor that walks through an MD3GLModel structure and writes
 * the vertex data of all meshes of the current frame of the encountered models
 * to a specified output stream in PLG format. PLG is a format specific to Luc
 * Van Deuren his 3D engine: OpenSpace.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3GLModelExportPlgVisitor extends MD3GLModelVisitor {
	private PrintWriter out;
  private Stack transformations;
  private float[][] currentTransformation;
	
  /**
   * <p>Create a new PLG export visitor.
   *
   * @param out The stream to which the data should be written.
   */
	public MD3GLModelExportPlgVisitor(OutputStream out) {
		this.out=new PrintWriter(new OutputStreamWriter(out));

    transformations=new Stack();
    //currently no transformation -> 4x4 identity matrix
    currentTransformation=new float[4][4];
    currentTransformation[0][0]=currentTransformation[1][1]=currentTransformation[2][2]=currentTransformation[3][3]=1;
	}
	
	private void writeVertices(Vec3[] vecs) {
		out.println("vertices {");
		for (int i=0;i<vecs.length;i++) {
      //turn vertex into 4x1 matrix
      float[][] vec=new float[4][1];
      vec[0][0]=vecs[i].x;
      vec[1][0]=vecs[i].y;
      vec[2][0]=vecs[i].z;
      vec[3][0]=1;
      
      //transform vertex
      vec=MatrixMath.mult(currentTransformation, vec);
			
			//output data
			out.print("(" + vec[0][0] + "," + vec[1][0] + "," + vec[2][0] + ")");
			if (i==vecs.length-1)
				out.println();
			else
				out.println(",");
		}
		out.println("}");
	}
	
	private void writePolygons(int[][] triangles) {
		out.println("polygons {");
		for (int i=0;i<triangles.length;i++) {
			out.print("(single,flat,(200,200,200),3,(" + triangles[i][0] + "," + triangles[i][1] + "," + triangles[i][2] + "))");
			if (i==triangles.length-1)
				out.println();
			else
				out.println(",");
		}
		out.println("}");
	}
	
	public void visit(MD3GLModel model) {
    // write meshes in this model
    for (int i=0; i<model.meshNum; i++) {
			out.println("object {");
			
			out.println("vertices [" + model.meshes[i].vertexNum + "];");
			out.println("polygons [" + model.meshes[i].triangleNum + "];");
			
			writeVertices(model.meshes[i].meshFrames[model.currentFrame]);
			writePolygons(model.meshes[i].triangles);
			
			out.println("}");
		}
		
    // write children
    Iterator it=model.linkedModels();
    while (it.hasNext()) {
      MD3Model child=(MD3Model)it.next();
      MD3Tag tag=model.boneFrames[model.currentFrame].tags[child.getParentTagIndex()];
      
      float[][] m=new float[4][4];
      m[0][0] = tag.matrix[0][0]; m[0][1] = tag.matrix[0][1]; m[0][2] = tag.matrix[0][2]; m[0][3] = tag.position.x;
      m[1][0] = tag.matrix[1][0]; m[1][1] = tag.matrix[1][1]; m[1][2] = tag.matrix[1][2]; m[1][3] = tag.position.y;
      m[2][0] = tag.matrix[2][0]; m[2][1] = tag.matrix[2][1]; m[2][2] = tag.matrix[2][2]; m[2][3] = tag.position.z;
      m[3][0] = 0;                m[3][1] = 0;                m[3][2] = 0;                m[3][3] = 1;  
      
      transformations.push(currentTransformation);
      currentTransformation=MatrixMath.mult(currentTransformation,m);
      child.accept(this);
      currentTransformation=(float[][])transformations.pop();
    }

		out.flush();
	}
}