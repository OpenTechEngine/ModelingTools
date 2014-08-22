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
 * in Alias Wavefront's Object File (.obj) format (version 3.0).
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3GLModelExportObjVisitor extends MD3GLModelVisitor {
	private StringWriter vertexData;
	private StringWriter elementData;
  private StringWriter mtlData;
  private Set materials=new HashSet(); //keep track of used materials
	private int vertexCount=0,
		          meshCount=0;
  private Stack transformations;
  private float[][] currentTransformation;

  /**
   * <p>Create a new Wavefront object export visitor.
   */
	public MD3GLModelExportObjVisitor() {
		vertexData=new StringWriter();
		elementData=new StringWriter();
    mtlData=new StringWriter();
    transformations=new Stack();
    //currently no transformation -> 4x4 identity matrix
    currentTransformation=new float[4][4];
    currentTransformation[0][0]=currentTransformation[1][1]=currentTransformation[2][2]=currentTransformation[3][3]=1;
	}
	
  /**
   * <p>Write the generated output data to the given output streams.
   *
   * @param objOut The stream to which the .obj data should be written.
   * @param mtlName The name of the material library file.
   * @param mtlOut Output stream for the material library file.
   */
	public void writeOut(OutputStream objOut, String mtlName, OutputStream mtlOut) throws IOException {
    mtlOut.write(mtlData.toString().getBytes());
    mtlOut.flush();
    
    objOut.write(("mtllib " + mtlName + "\n").getBytes());
		objOut.write(vertexData.toString().getBytes());
		objOut.write(elementData.toString().getBytes());
		objOut.flush();
	}
	
  private void writeMtlData(MD3Mesh mesh) {
    for (int t=0;t<mesh.textureNum;t++)
      if (mesh.textures[t] != null) {        
        String mtlName=new File(mesh.textures[t].loadFilename).getName();
        if (!materials.contains(mtlName)) {
          materials.add(mtlName);
          mtlData.write("newmtl " + mtlName + "\n");
          mtlData.write("\tmap_Kd " + mtlName + "\n");
        }
      }
  }

  private void writeVertexData(MD3Mesh mesh, int frame) {
		for (int i=0; i<mesh.vertexNum; i++) {
      //turn vertex into 4x1 matrix
      float[][] vec=new float[4][1];
      vec[0][0]=mesh.meshFrames[frame][i].x;
      vec[1][0]=mesh.meshFrames[frame][i].y;
      vec[2][0]=mesh.meshFrames[frame][i].z;
      vec[3][0]=1;
      
      //transform vertex
      vec=MatrixMath.mult(currentTransformation, vec);
			
			//write vertex			
			vertexData.write("v " + vec[0][0] + " " + vec[1][0] + " " + vec[2][0] + "\n");
			
			//write texture coord (invert v coordinate?)
			vertexData.write("vt " + mesh.textureCoord[i][0] + " " + mesh.textureCoord[i][1] + "\n");

		  //write vertex normal
  		double alpha=mesh.meshVertexNormals[frame][i][0] * 2d * Math.PI / 255d;
  		double beta=mesh.meshVertexNormals[frame][i][1] * 2d * Math.PI / 255d;
		  float a=(float)(Math.cos(beta) * Math.sin(alpha)),
		  	    b=(float)(Math.sin(beta) * Math.sin(alpha)),
		  	    c=(float)Math.cos(alpha);			
			vertexData.write("vn " + a + " " + b + " " + c + "\n");
		}
	}	
		
	private void writeElementData(MD3Mesh mesh) {
		elementData.write("g " + mesh.name + "\n");
		elementData.write("s " + meshCount++ + "\n");
		elementData.write("c_interp off\n");
		elementData.write("d_interp off\n");
    if (mesh.textureNum>0 && mesh.textures[0]!=null) //only use first texture
      elementData.write("usemtl " + new File(mesh.textures[0].loadFilename).getName() + "\n");
		for (int t=0; t<mesh.triangleNum; t++) {
			int a=vertexCount + mesh.triangles[t][0] + 1,
				  b=vertexCount + mesh.triangles[t][1] + 1,
				  c=vertexCount + mesh.triangles[t][2] + 1;
      elementData.write("f " + c + "/" + c + "/" + c + " " + b + "/" + b + "/" + b + " " + a + "/" + a + "/" + a + "\n");
		}
	}
  		
	public void visit(MD3GLModel model) {
		//write meshes
		for (int i=0; i<model.meshNum; i++) {
			MD3Mesh mesh=model.meshes[i];
      
      writeMtlData(mesh);
			writeVertexData(mesh, model.currentFrame);
			writeElementData(mesh);
			
			vertexCount+=mesh.vertexNum;
		}
		
    //write children
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
	}
}