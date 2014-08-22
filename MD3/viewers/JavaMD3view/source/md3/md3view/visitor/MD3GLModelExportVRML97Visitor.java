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
 * the data of the current frame of the encountered models to a specified output stream
 * in VRML 97 syntax.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3GLModelExportVRML97Visitor extends MD3GLModelVisitor {
  private PrintWriter out;
  private Stack transformations;
  private float[][] currentTransformation;
  
  /**
   * <p>Create a new VRML 97 export visitor.
   *
   * @param outstream The stream to which the data should be written.
   */
  public MD3GLModelExportVRML97Visitor(OutputStream outstream) {
    out=new PrintWriter(new OutputStreamWriter(outstream));    
    transformations=new Stack();
    //currently no transformation -> 4x4 identity matrix
    currentTransformation=new float[4][4];
    currentTransformation[0][0]=currentTransformation[1][1]=currentTransformation[2][2]=currentTransformation[3][3]=1;

    writeHeader();
    writeViewpoint();
  }
  
  private void writeHeader() {
    out.println("#VRML V2.0 utf8");
  }
  
  private void writeViewpoint() {
    out.println("Viewpoint {");
    out.println("description \"start position\"");
    out.println("position 80.0 0.0 0.0");
    out.println("orientation 0.0 1.0 0.0 1.57");
    out.println("}");
  }
    
  private void writeTriangles(MD3Mesh mesh, String coordName, String texCoordName) {
    out.println("Shape {");
    
    //define geometry
    out.println("geometry IndexedFaceSet {");
    out.println("ccw FALSE");
    out.println("texCoord USE " + texCoordName);
    out.println("coord USE " + coordName);
    out.print("coordIndex [");
    for (int t=0; t<mesh.triangleNum; t++) {
      for (int v=0; v<3; v++)
        out.print(mesh.triangles[t][v] + ", ");
      out.print("-1, ");
    }
    out.println("]");
    out.println("}");
    
    //define appearance
    out.println("appearance Appearance {");
    out.println("texture ImageTexture {");
    out.print("url [");    
    for (int t=0;t<mesh.textureNum;t++)
    	if (mesh.textures[t] != null)
      	out.print("\"" + new File(mesh.textures[t].loadFilename).getName() + "\" ");
      else
      	out.print("\"\"");
    out.println("]");
    out.println("}");      
    out.println("}");
    
    out.println("}");
  }
  
  private void writeCoordinate(Vec3[] vecs, String coordName) {
    out.println("DEF " + coordName + " Coordinate {");
    out.print("point [");
    for (int v=0;v<vecs.length;v++) {
      //turn vertex into 4x1 matrix
      float[][] vec=new float[4][1];
      vec[0][0]=vecs[v].x;
      vec[1][0]=vecs[v].y;
      vec[2][0]=vecs[v].z;
      vec[3][0]=1;
      
      //transform vertex
      vec=MatrixMath.mult(currentTransformation,vec);
      
      for (int n=0;n<3;n++) out.print(" " + vec[n][0]);
      out.print(",");
    }
    out.println("]");
    out.println("}");
  }
  
  private void writeTextureCoordinate(float[][] texCoord, String texCoordName) {
    out.println("DEF " + texCoordName + " TextureCoordinate {");
    out.print("point [");
    for (int v=0;v<texCoord.length;v++) {
      out.print(" " + texCoord[v][0]);
      out.print(" " + (-1f * texCoord[v][1])); //insert y coord
      out.print(",");
    }
    out.println("]");
    out.println("}");
  }
  
  public void visit(MD3GLModel model) {
    // write meshes in this model
    for (int i=0; i<model.meshNum; i++) {
      MD3Mesh mesh=model.meshes[i];
      String coordName="mesh" + i + "Coord";
      String texCoordName="mesh" + i + "TexCoord";
      
      // write out vertex coords of current mesh frame
      writeCoordinate(mesh.meshFrames[model.currentFrame],coordName);
      // write out texture coords of mesh
      writeTextureCoordinate(mesh.textureCoord, texCoordName);
      // write out all the triangles of the current frame of the mesh
      writeTriangles(mesh, coordName, texCoordName);
    }
      
    
    // write children
    out.println("Group {");
    out.println("children [");
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
    out.println("]");
    out.println("}");
    
    out.flush(); 
  }
}