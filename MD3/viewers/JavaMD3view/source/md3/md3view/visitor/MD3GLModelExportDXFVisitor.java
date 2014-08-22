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
 * in AUTOCAD DXF syntax.
 *
 * @author Donald Gray (dgray@widomaker.com)
 * @author Erwin Vervaet (klr8@fragland.net)
 */ 
public class MD3GLModelExportDXFVisitor extends MD3GLModelVisitor {
  private StringWriter dxfData;
  private PrintWriter out;
  private Stack transformations;
  private float[][] currentTransformation;

  private final String EOL = "\r\n";

  private final String THREEDFACE_DEF = "0" + EOL +
  	                                    "3DFACE" + EOL +
    							                      "8" + EOL +
    							                      "1" + EOL;
  
  /**
   * <p>Create a new DXF export visitor.
   */
  public MD3GLModelExportDXFVisitor() {
    dxfData=new StringWriter();
    out=new PrintWriter(dxfData);
    transformations=new Stack();
    //currently no transformation -> 4x4 identity matrix
    currentTransformation=new float[4][4];
    currentTransformation[0][0]=currentTransformation[1][1]=currentTransformation[2][2]=currentTransformation[3][3]=1;

    writeHeader();
  }
  
  /**
   * <p>Write the generated DXF data to given output stream.
   *
   * @param outstream The stream to which the data should be written.
   */
  public void writeOut(OutputStream outstream) throws IOException {
    writeFooter();
    outstream.write(dxfData.toString().getBytes());
    outstream.flush();
  }
  
  private void writeHeader() {
    // start with file header
    out.print("0" + EOL + "SECTION" + EOL + "2" + EOL + "ENTITIES" + EOL);
  }
  
  private void writeFooter() {
    // finish off with the file footer
    out.print("0" + EOL + "ENDSEC" + EOL); // end of ENTITIES section
    out.print("0" + EOL + "EOF" + EOL); // end of file
    out.flush(); 
  }
  
  private void writeTriangles(MD3Mesh mesh, int currentFrame) {
    Vec3[] vecs = mesh.meshFrames[currentFrame];
    float[][] vec=new float[4][1];
    for (int t=0; t<mesh.triangleNum; t++) {
	    out.print(THREEDFACE_DEF);

			// first vertex
			vec[0][0]=vecs[mesh.triangles[t][0]].x;
			vec[1][0]=vecs[mesh.triangles[t][0]].y;
			vec[2][0]=vecs[mesh.triangles[t][0]].z;
			vec[3][0]=1;
			vec=MatrixMath.mult(currentTransformation,vec);
	
			out.print("10" + EOL);
			out.print(vec[0][0] + EOL);
			out.print("20" + EOL);
			out.print(vec[1][0] + EOL);
			out.print("30" + EOL);
			out.print(vec[2][0] + EOL);
	
			// second vertex
			vec[0][0]=vecs[mesh.triangles[t][1]].x;
			vec[1][0]=vecs[mesh.triangles[t][1]].y;
			vec[2][0]=vecs[mesh.triangles[t][1]].z;
			vec[3][0]=1;
			vec=MatrixMath.mult(currentTransformation,vec);
	
			out.print("11" + EOL);
			out.print(vec[0][0] + EOL);
			out.print("21" + EOL);
			out.print(vec[1][0] + EOL);
			out.print("31" + EOL);
			out.print(vec[2][0] + EOL);
	
			// third vertex
			vec[0][0]=vecs[mesh.triangles[t][2]].x;
			vec[1][0]=vecs[mesh.triangles[t][2]].y;
			vec[2][0]=vecs[mesh.triangles[t][2]].z;
			vec[3][0]=1;
			vec=MatrixMath.mult(currentTransformation,vec);
	
			out.print("12" + EOL);
			out.print(vec[0][0] + EOL);
			out.print("22" + EOL);
			out.print(vec[1][0] + EOL);
			out.print("32" + EOL);
			out.print(vec[2][0] + EOL);
	
			// fourth vertex (same as third)
			out.print("13" + EOL);
			out.print(vec[0][0] + EOL);
			out.print("23" + EOL);
			out.print(vec[1][0] + EOL);
			out.print("33" + EOL);
			out.print(vec[2][0] + EOL);
		}
  }
  
  public void visit(MD3GLModel model) {
    // write meshes in this model
    for (int i=0; i<model.meshNum; i++) {
      MD3Mesh mesh=model.meshes[i];
      
      // write out all the triangles of the current frame of the mesh
      writeTriangles(mesh, model.currentFrame);
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
  }
}