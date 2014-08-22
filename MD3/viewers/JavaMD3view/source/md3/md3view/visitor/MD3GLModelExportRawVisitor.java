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
 * the raw vertex data of all meshes of the current frame of the encountered models
 * to a specified output stream.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3GLModelExportRawVisitor extends MD3GLModelVisitor {
  private PrintWriter out;
  private int objectNum;
  private Stack transformations;
  private float[][] currentTransformation;
  
  /**
   * <p>Create a new RAW export visitor.
   *
   * @param outstream The stream to which the data should be written.
   */
  public MD3GLModelExportRawVisitor(OutputStream outstream) {
    out=new PrintWriter(new OutputStreamWriter(outstream));
    objectNum=1;
    transformations=new Stack();
    //currently no transformation -> 4x4 identity matrix
    currentTransformation=new float[4][4];
    currentTransformation[0][0]=currentTransformation[1][1]=currentTransformation[2][2]=currentTransformation[3][3]=1;
  }

  public void visit(MD3GLModel model) {
    // write header
    out.println("Object" + objectNum++);
    
    // write meshes in this model
    for (int i=0; i<model.meshNum; i++) {    
      MD3Mesh mesh = model.meshes[i];            
      Vec3[] vecs = mesh.meshFrames[model.currentFrame];  
          
      // write out all the triangles of the current frame of the mesh as 3 vertices
      for (int t=0; t<mesh.triangleNum; t++) {
        for (int v=0; v<3; v++) {
          //turn vertex into 4x1 matrix
          float[][] vec=new float[4][1];
          vec[0][0]=vecs[mesh.triangles[t][v]].x;
          vec[1][0]=vecs[mesh.triangles[t][v]].y;
          vec[2][0]=vecs[mesh.triangles[t][v]].z;
          vec[3][0]=1;
          
          //transform vertex
          vec=MatrixMath.mult(currentTransformation,vec);
          
          //ouput vertex data
          for (int n=0; n<3; n++)
            out.print(vec[n][0] + " ");          
        }
        out.println(); 
      }
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