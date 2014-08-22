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

package md3.md3model;

import java.io.*;

import cio.LittleEndianDataInputStream;

import md3.util.*;

/**
 * <p>Utility class that provides .3ds file IO functions.
 *  
 * @author Donald Gray (dgray@widomaker.com)
 */
public class ThreeDSIO 
{

	private ThreeDSIO() {} //cannot instantiate

	/**
	 * <p>Load a .3ds file from the given input stream into an MD3Model object.
	 */ 
	public static void loadModel(MD3Model newModel, InputStream fin) throws IOException 
	{
		LittleEndianDataInputStream dfin=new LittleEndianDataInputStream(new BufferedInputStream(fin));

		// initialize some model fields
		newModel.boneFrameNum = 0;			// no bounding box
		newModel.tagNum = 0;				// no tag support, yet
		newModel.meshNum = 0;				// no meshes, yet
		newModel.maxTextureNum = 0;			// no textures, yet
		newModel.boneFrames = new MD3BoneFrame[1];
		newModel.boneFrames[0] = MD3ModelFactory.getFactory().makeMD3BoneFrame(0);

		parseChunk(newModel, dfin);
	}

	/**
	 * Reads in single 3DS chunk and passes it off to the appropriate loading function.
	 * <p>NOTE: recursive method.
	 */
	static int parseChunk(MD3Model newModel, LittleEndianDataInputStream dfin)
	{
		int chunkRead, amtOfChunkRead = 0;

		try
		{
			// read in the tag and length data
			int sID = dfin.readUnsignedShort();
			int chunkLen = dfin.readInt();
			amtOfChunkRead = 6;				// size of short (2) + size of int (4)

			// choose where to go next
			switch(sID)
			{
				case 0x3d3d:	// editor chunk
				case 0x4100:	// mesh header chunk
				case 0x4d4d:	// 3DS file type ID
					// read all chunks
					while (amtOfChunkRead < chunkLen)
					{
						chunkRead = parseChunk(newModel, dfin);

						if (chunkRead < 1)
							break;

						amtOfChunkRead += chunkRead;
					}
					break;

				case 0x4000:	// named object
					chunkRead = parseNamedObject(newModel, dfin);
					if (chunkRead < 1)
						break;
					amtOfChunkRead += chunkRead;

					// read all of the subchunks for this named object
					while (amtOfChunkRead < chunkLen)
					{
						chunkRead = parseChunk(newModel, dfin);

						if (chunkRead < 1)
							break;

						amtOfChunkRead += chunkRead;
					}
					break;

				case 0x4110:	// vertex data
					chunkRead = parseVertexData(newModel, dfin);
					if (chunkRead < 1)
						break;
					amtOfChunkRead += chunkRead;
					break;

				case 0x4120:	// triangle data
					chunkRead = parseTriangleData(newModel, dfin);
					if (chunkRead < 1)
						break;
					amtOfChunkRead += chunkRead;
					break;

				case 0x4140:	// texture coordinates
					chunkRead = parseTexVecData(newModel, dfin);
					if (chunkRead < 1)
						break;
					amtOfChunkRead += chunkRead;
					break;

				case 0x4130:	// material data
				default:		// otherwise, skip this chunk
					dfin.skipBytes((int)(chunkLen - amtOfChunkRead));
					amtOfChunkRead = chunkLen;
					break;
			}

			// skip remaining data
			if (amtOfChunkRead < chunkLen)
			{
				dfin.skipBytes((int)(chunkLen - amtOfChunkRead));
				amtOfChunkRead = chunkLen;
			}
		}
		catch (Exception e)
		{
		}

		return amtOfChunkRead;
	}

	static int parseNamedObject(MD3Model newModel, LittleEndianDataInputStream dfin)
	{
		int numBytesRead = 0;
		try
		{
			// read the object's name
			String sName = readCString(dfin);
			numBytesRead += sName.length() + 1;

			// create a mesh
			MD3Mesh newMesh = MD3ModelFactory.getFactory().makeMD3Mesh();
			newMesh.name = sName;
			newMesh.meshFrameNum = 1;
			newMesh.vertexNum = 0;
			newMesh.triangleNum = 0;
			newMesh.textureNum = 0;
			newMesh.textures = new MD3Texture[1];
			newMesh.textures[0] = null;

			// add the mesh to the model
			newModel.meshNum++;
			MD3Mesh[] meshArray = new MD3Mesh[newModel.meshNum];
			for (int i=0;i<newModel.meshNum-1;i++)
				meshArray[i] = newModel.meshes[i];
			newModel.meshes = meshArray;
			newModel.meshes[newModel.meshNum-1] = newMesh;
		}
		catch(Exception e)
		{
		}
		return numBytesRead;
	}

	static int parseVertexData(MD3Model newModel, LittleEndianDataInputStream dfin)
	{
		int numBytesRead = 0;
		try
		{
			// figure out which mesh we're working on
			MD3Mesh currMesh = newModel.meshes[newModel.meshNum-1];

			// read the number of vertices
			short numVertices = dfin.readShort();
			currMesh.vertexNum += numVertices;
			numBytesRead += 2;

			// allocate memory
			currMesh.meshFrames = new Vec3[currMesh.meshFrameNum][currMesh.vertexNum];
			currMesh.meshVertexNormals = new int[currMesh.meshFrameNum][currMesh.vertexNum][2];
			currMesh.textureCoord = new float[currMesh.vertexNum][2];

			// read the vertex data
			for (int i=0;i<numVertices;i++)
			{
				currMesh.meshFrames[0][i]=new Vec3();            
				currMesh.meshFrames[0][i].x = dfin.readFloat();
				currMesh.meshFrames[0][i].y = dfin.readFloat();
				currMesh.meshFrames[0][i].z = dfin.readFloat();
				numBytesRead += 12;
			}
		}
		catch(Exception e)
		{
		}
		return numBytesRead;
	}

	static int parseTriangleData(MD3Model newModel, LittleEndianDataInputStream dfin)
	{
		int numBytesRead = 0;
		try
		{
			// figure out which mesh we're working on
			MD3Mesh currMesh = newModel.meshes[newModel.meshNum-1];

			// read the number of triangles
			short numTris = dfin.readShort();
			currMesh.triangleNum += numTris;
			numBytesRead += 2;

			// allocate memory
			currMesh.triangles = new int[currMesh.triangleNum][3];

			// read the triangle data
			for (int i=0;i<numTris;i++)
			{
				currMesh.triangles[i][0] = dfin.readShort();
				currMesh.triangles[i][1] = dfin.readShort();
				currMesh.triangles[i][2] = dfin.readShort();

				// there's an extra Flags short after the triangle indexes (typically 0x0007)
				dfin.readShort();
				numBytesRead += 8;
			}
		}
		catch(Exception e)
		{
		}
		return numBytesRead;
	}

	static int parseTexVecData(MD3Model newModel, LittleEndianDataInputStream dfin)
	{
		int numBytesRead = 0;
		try
		{
			// figure out which mesh we're working on
			MD3Mesh currMesh = newModel.meshes[newModel.meshNum-1];

			// read the number of tex coords
			short numTexVecs = dfin.readShort();
			currMesh.textureCoord = new float[numTexVecs][2];
			numBytesRead += 2;

			// read the texture coordinate data
			for (int i=0;i<numTexVecs;i++)
			{
				currMesh.textureCoord[i][0] = dfin.readFloat();
				currMesh.textureCoord[i][1] = dfin.readFloat();
				numBytesRead += 8;
			}
		}
		catch(Exception e)
		{
		}
		return numBytesRead;
	}

	static String readCString(LittleEndianDataInputStream dfin)
	{
		String s = "";
		try
		{
			char c = dfin.readChar();
			while (c != '\0')
			{
				s = s + c;
				c = dfin.readChar();
			}
		}
		catch(Exception e)
		{
		}

		return s;
	}
}