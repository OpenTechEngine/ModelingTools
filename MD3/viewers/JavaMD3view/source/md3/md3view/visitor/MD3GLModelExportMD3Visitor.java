/*-------------------
  UNFINISHED CODE!!
-------------------*/

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

import cio.*;

import md3.md3model.*;
import md3.md3view.glmodel.*;
import md3.util.*;

/**
 * <p>An MD3GLModel visitor that walks through an MD3GLModel structure and writes
 * the data of the current frame of the encountered models to a specified output stream
 * in MD3 format.
 *
 * @author Donald Gray (dgray@widomaker.com) 
 */ 
public class MD3GLModelExportMD3Visitor extends MD3GLModelVisitor {
	private OutputStream out;
	private Stack transformations;
	private float[][] currentTransformation;
  
	/**
	 * <p>Create a new MD3 export visitor.
	 *
	 * @param outstream The stream to which the data should be written.
	 */
	public MD3GLModelExportMD3Visitor(OutputStream outstream) 
	{
		out=outstream;    
		transformations=new Stack();
		
		//currently no transformation -> 4x4 identity matrix
		currentTransformation=new float[4][4];
		currentTransformation[0][0]=currentTransformation[1][1]=currentTransformation[2][2]=currentTransformation[3][3]=1;
	}

	public void visit(MD3GLModel model) 
	{
		try
		{

			LittleEndianDataOutputStream dfout=new LittleEndianDataOutputStream(new BufferedOutputStream(out));

			// variables for the three sections of data
			byte[] baBoneFrames;
			byte[] baTags;
			byte[] baMeshes;

			// calculate section sizes
			int boneFrameSize = 56 * model.boneFrameNum;
			int tagSize = 112 * model.tagNum;
			int totalMeshSize = 0;
			MD3Mesh currMesh;
			for (int i=0;i<model.meshNum;i++)
			{
				currMesh = model.meshes[i];
				totalMeshSize += 108 + // header
				(12 * currMesh.triangleNum) +	// faces
				(68 * currMesh.textureNum) + // texture names
				( 8 * currMesh.vertexNum) +	// texture vertices
				( 8 * currMesh.meshFrameNum * currMesh.vertexNum); // vertices
			}

			// ##### Write out data for this model #####
			model.boneFrameStart = 108;			// this is the header size
			model.tagStart = model.boneFrameStart + boneFrameSize;
			model.meshStart = model.tagStart + tagSize;
			model.fileSize = model.meshStart + totalMeshSize;

			// start with the file type tag (IDP3)
			dfout.writeByte(0x49);
			dfout.writeByte(0x44);
			dfout.writeByte(0x50);
			dfout.writeByte(0x33);

			// add the file format version
			dfout.writeInt(15);

			// add the file name (68 chars)
			byte[] baFileName = new byte[68];
			for (int j=0;j<68;j++)
				baFileName[j]=0x00;
			model.filename.getBytes(0, model.filename.length(), baFileName, 0);
			dfout.writeFully(baFileName);

			// add the rest of the header
			dfout.writeInt(model.boneFrameNum);
			dfout.writeInt(model.tagNum);
			dfout.writeInt(model.meshNum);
			dfout.writeInt(model.maxTextureNum);
			dfout.writeInt(model.boneFrameStart);
			dfout.writeInt(model.tagStart);
			dfout.writeInt(model.meshStart);
			dfout.writeInt(model.fileSize);

			// build the bone frame section
			for (int i=0;i<model.boneFrameNum;i++)
			{
				dfout.writeFloat(model.boneFrames[i].mins.x);
				dfout.writeFloat(model.boneFrames[i].mins.y);
				dfout.writeFloat(model.boneFrames[i].mins.z);
				dfout.writeFloat(model.boneFrames[i].maxs.x);
				dfout.writeFloat(model.boneFrames[i].maxs.y);
				dfout.writeFloat(model.boneFrames[i].maxs.z);
				dfout.writeFloat(model.boneFrames[i].position.x);
				dfout.writeFloat(model.boneFrames[i].position.y);
				dfout.writeFloat(model.boneFrames[i].position.z);
				dfout.writeFloat(model.boneFrames[i].scale);

				byte[] baCreator=new byte[16];
				for (int j=0;j<16;j++)
					baCreator[j]=0x00;
				model.boneFrames[i].creator.getBytes(0, model.boneFrames[i].creator.length(), baCreator, 0);
				dfout.writeFully(baCreator);
			}

			// build the tags section
			for (int i=0;i<model.boneFrameNum;i++)
			{
				for (int j=0;j<model.tagNum;j++)
				{
					MD3Tag aTag = model.boneFrames[i].tags[j];

					byte[] baTagName=new byte[64];
					for (int k=0;k<64;k++)
						baTagName[k]=0x00;
					aTag.name.getBytes(0, aTag.name.length(), baTagName, 0);
					dfout.writeFully(baTagName);

					dfout.writeFloat(aTag.position.x);
					dfout.writeFloat(aTag.position.y);
					dfout.writeFloat(aTag.position.z);
					dfout.writeFloat(aTag.matrix[0][0]);
					dfout.writeFloat(aTag.matrix[0][1]);
					dfout.writeFloat(aTag.matrix[0][2]);
					dfout.writeFloat(aTag.matrix[1][0]);
					dfout.writeFloat(aTag.matrix[1][1]);
					dfout.writeFloat(aTag.matrix[1][2]);
					dfout.writeFloat(aTag.matrix[2][0]);
					dfout.writeFloat(aTag.matrix[2][1]);
					dfout.writeFloat(aTag.matrix[2][2]);
				}
			}

			// build the meshes section
			for (int i=0;i<model.meshNum;i++)
			{
				// first, the header (108 bytes)
				currMesh = model.meshes[i];
				currMesh.textureStart = 108;
				currMesh.triangleStart = currMesh.textureStart + (68 * currMesh.textureNum);
				currMesh.texVecStart = currMesh.triangleStart + (12 * currMesh.triangleNum);
				currMesh.vertexStart = currMesh.texVecStart + (8 * currMesh.vertexNum);
				currMesh.meshSize = currMesh.vertexStart + (8 * currMesh.meshFrameNum * currMesh.vertexNum);

				// start with the file type tag (IDP3)
				dfout.writeByte(0x49);
				dfout.writeByte(0x44);
				dfout.writeByte(0x50);
				dfout.writeByte(0x33);

				// mesh name
				byte[] baMeshName=new byte[68];
				for (int j=0;j<68;j++)
					baMeshName[j]=0x00;
				currMesh.name.getBytes(0, currMesh.name.length(), baMeshName, 0);
				dfout.writeFully(baMeshName);

				// rest of header
				dfout.writeInt(currMesh.meshFrameNum);
				dfout.writeInt(currMesh.textureNum);
				dfout.writeInt(currMesh.vertexNum);
				dfout.writeInt(currMesh.triangleNum);
				dfout.writeInt(currMesh.triangleStart);
				dfout.writeInt(currMesh.textureStart);
				dfout.writeInt(currMesh.texVecStart);
				dfout.writeInt(currMesh.vertexStart);
				dfout.writeInt(currMesh.meshSize);

				// texture section
				for (int j=0;j<currMesh.textureNum;j++)
				{
					byte[] baTxName=new byte[68];
					for (int k=0;k<68;k++)
						baTxName[k]=0x00;
					currMesh.textures[j].name.getBytes(0, currMesh.textures[j].name.length(), baTxName, 0);
					dfout.writeFully(baTxName);
				}

				// triangle (face) section
				for (int j=0;j<currMesh.triangleNum;j++)
				{
					dfout.writeInt(currMesh.triangles[j][0]);
					dfout.writeInt(currMesh.triangles[j][1]);
					dfout.writeInt(currMesh.triangles[j][2]);
				}

				// texture vertex section
				for (int j=0;j<currMesh.vertexNum;j++)
				{
					dfout.writeFloat(currMesh.textureCoord[j][0]);
					dfout.writeFloat(currMesh.textureCoord[j][1]);
				}

				// vertices (one set per animation frame)
				for (int j=0;j<currMesh.meshFrameNum;j++)
				{
					for (int k=0;k<currMesh.vertexNum;k++)
					{
						dfout.writeShort((short)(currMesh.meshFrames[j][k].x * 64.0f));
						dfout.writeShort((short)(currMesh.meshFrames[j][k].y * 64.0f));
						dfout.writeShort((short)(currMesh.meshFrames[j][k].z * 64.0f));

						dfout.writeUnsignedByte(currMesh.meshVertexNormals[j][k][0]);
						dfout.writeUnsignedByte(currMesh.meshVertexNormals[j][k][1]);
					}
				}

			}

			// write children - should we???  (to different files?)
			/****
			Iterator it=model.linkedModels();
			while (it.hasNext()) 
			{
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
			****/  

			dfout.flush(); 
		}
		catch(Exception e)
		{
		}
	}
}