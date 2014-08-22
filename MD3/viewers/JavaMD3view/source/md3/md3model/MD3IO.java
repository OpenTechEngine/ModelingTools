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
 * <p>Booch utility class that provides .md3 file IO functions.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
class MD3IO {
	
	private static int totalSizeOfMeshes;
	
  private MD3IO() {} //cannot instantiate
  
  //reads cnt chars, representing a '\0' terminated string, and returns
  //them as a String object
  private static String readName(DataInput din, int cnt) throws IOException {
    byte name[]=new byte[cnt];
    din.readFully(name);
    
    StringBuffer res=new StringBuffer();
    for (int i=0;i<cnt;i++) {
      if (name[i]==0) 
        break; //end of string data reached
      else
        res.append((char)name[i]);
    }
        
    return res.toString();    
  }
  
  /**
   * <p>Load a .md3 file from the given input stream into an MD3Model object.
   */ 
  static void loadModel(MD3Model newModel, InputStream fin) throws IOException {
    LittleEndianDataInputStream dfin=new LittleEndianDataInputStream(new BufferedInputStream(fin));
    
    int byteCount=0; //number of bytes read
    boolean boneFrames, tags, meshes; //parts of model that have been read
    
    //start reading .md3 file
    
    //read id of file, always "IDP3" 
    newModel.id=readName(dfin, 4);
    if (newModel.id.equals("IDP3")) {      
      //read version - i suspect this is a version number, always 15 
      newModel.version = dfin.readInt();

      //read filename - sometimes left blank (65 chars, 32bit aligned == 68 chars)
      newModel.filename = readName(dfin, 68);
      
      //read other header data
      newModel.boneFrameNum = dfin.readInt();
      newModel.tagNum = dfin.readInt(); 
      newModel.meshNum = dfin.readInt();  
      newModel.maxTextureNum = dfin.readInt(); 
      newModel.boneFrameStart = dfin.readInt(); 
      newModel.tagStart = dfin.readInt(); 
      newModel.meshStart = dfin.readInt();                     
      newModel.fileSize = dfin.readInt(); 

      byteCount+=108; //header is 108 bytes long

      if ( ( newModel.version == 15 ) &&
           ( newModel.fileSize > newModel.tagStart ) &&
           ( newModel.fileSize >= newModel.meshStart )
         ) {

        //mark parts with no data as read
        boneFrames = newModel.boneFrameNum==0;
        tags = newModel.tagNum==0;
        meshes = newModel.meshNum==0;
           
        //read different parts of model in correct order	
        while (!(boneFrames && tags && meshes)) {
	        if (byteCount==newModel.boneFrameStart && !boneFrames) {
	        	readBoneFrames(newModel, dfin);
	        	byteCount+=56 * newModel.boneFrameNum;
	        	boneFrames=true;
	        }
	        else if (byteCount==newModel.tagStart && !tags) {
	        	readTags(newModel, dfin);
	        	byteCount+=112 * newModel.boneFrameNum * newModel.tagNum;
	        	tags=true;
	        }
	        else if (byteCount==newModel.meshStart && !meshes) {
	        	totalSizeOfMeshes=0;
	        	readMeshes(newModel, dfin);
	        	byteCount+=totalSizeOfMeshes;
	        	meshes=true;
	        }
	        else
		        throw new IOException("corrupt md3 file data");
        }                
      }
      else
        throw new IOException("corrupt md3 file header");
    }
    else
      throw new IOException("not a IDP3 type md3 file");
  }
  
  //read MD3BoneFrames
  private static void readBoneFrames(MD3Model newModel, DataInput dfin) throws IOException {
    newModel.boneFrames = new MD3BoneFrame[newModel.boneFrameNum];
    for (int i=0;i<newModel.boneFrameNum;i++)
      newModel.boneFrames[i]=MD3ModelFactory.getFactory().makeMD3BoneFrame(newModel.tagNum, dfin);
  }
  
  //read MD3Tags for every bone frame
  private static void readTags(MD3Model newModel, DataInput dfin) throws IOException {
    for (int i=0;i<newModel.boneFrameNum;i++)
      for (int j=0;j<newModel.tagNum;j++)
        newModel.boneFrames[i].tags[j]=MD3ModelFactory.getFactory().makeMD3Tag(dfin);
  }
  
  //read MD3Meshes
  private static void readMeshes(MD3Model newModel, DataInput dfin) throws IOException {
	  newModel.meshes=new MD3Mesh[newModel.meshNum];
	  for (int i=0;i<newModel.meshNum;i++)
	    newModel.meshes[i]=MD3ModelFactory.getFactory().makeMD3Mesh(dfin);
  }
  
  static void loadBoneFrame(MD3BoneFrame newBoneFrame, DataInput din) throws IOException {
    newBoneFrame.mins.x = din.readFloat();
    newBoneFrame.mins.y = din.readFloat();
    newBoneFrame.mins.z = din.readFloat();
    newBoneFrame.maxs.x = din.readFloat();
    newBoneFrame.maxs.y = din.readFloat();
    newBoneFrame.maxs.z = din.readFloat();
    newBoneFrame.position.x = din.readFloat();
    newBoneFrame.position.y = din.readFloat();
    newBoneFrame.position.z = din.readFloat();
    newBoneFrame.scale = din.readFloat();
    
    newBoneFrame.creator=readName(din, 16);
  }
  
  static void loadTag(MD3Tag newTag, DataInput din) throws IOException {
    newTag.name=readName(din, 64);

    newTag.position.x = din.readFloat();
    newTag.position.y = din.readFloat();
    newTag.position.z = din.readFloat();
    newTag.matrix[0][0] = din.readFloat();
    newTag.matrix[1][0] = din.readFloat();
    newTag.matrix[2][0] = din.readFloat();
    newTag.matrix[0][1] = din.readFloat();
    newTag.matrix[1][1] = din.readFloat();
    newTag.matrix[2][1] = din.readFloat();
    newTag.matrix[0][2] = din.readFloat();
    newTag.matrix[1][2] = din.readFloat();
    newTag.matrix[2][2] = din.readFloat();
  }
  
  static void loadMesh(MD3Mesh newMesh, DataInput din) throws IOException {
  	int byteCount=0; //number of bytes in mesh read so far
  	boolean triangles, texVecs, vertices, textures; //parts of mesh that have been read
  	
    //start reading mesh
    
    newMesh.id=readName(din, 4);
    if (newMesh.id.equals("IDP3")) {        
      newMesh.name=readName(din, 68); // 65 chars, 32bit aligned == 68 chars

      newMesh.meshFrameNum = din.readInt();
      newMesh.textureNum = din.readInt();
      newMesh.vertexNum = din.readInt();
      newMesh.triangleNum = din.readInt();
      newMesh.triangleStart = din.readInt();
      newMesh.textureStart = din.readInt();
      newMesh.texVecStart = din.readInt();
      newMesh.vertexStart = din.readInt();
      newMesh.meshSize = din.readInt();
      
      byteCount+=108; //header is 108 bytes long

      if ( ( newMesh.meshSize > newMesh.triangleStart ) &&
           ( newMesh.meshSize > newMesh.texVecStart ) &&
           ( newMesh.meshSize > newMesh.vertexStart )
         ) {        
                   
        //mark parts with no data as read 
        triangles = newMesh.triangleNum==0;
        texVecs = newMesh.vertexNum==0;
        vertices = newMesh.meshFrameNum==0;
        textures = newMesh.textureNum==0;
        
        //read different parts of mesh in correct order
        while (!(triangles && texVecs && vertices && textures)) {
          if (byteCount==newMesh.triangleStart && !triangles) {
	        	readTriangles(newMesh, din);
	        	byteCount+=12 * newMesh.triangleNum;
	        	triangles=true;
	        }
	        else if (byteCount==newMesh.textureStart && !textures) {
	        	readTextures(newMesh, din);
	        	byteCount+=68 * newMesh.textureNum;
	        	textures=true;
	        }
	        else if (byteCount==newMesh.texVecStart && !texVecs) {
	        	readTexVecs(newMesh, din);
	        	byteCount+=8 * newMesh.vertexNum;
	        	texVecs=true;
	        }
	        else if (byteCount==newMesh.vertexStart && !vertices) {
	        	readVertices(newMesh, din);
	        	byteCount+=8 * newMesh.meshFrameNum * newMesh.vertexNum;
	        	vertices=true;
	        }
	        else 
		        throw new IOException("corrupt mesh data");
        }                
      }
      else
        throw new IOException("corrupt mesh header");
    }
    else 
      throw new IOException("not a IDP3 mesh");
      
    totalSizeOfMeshes+=byteCount;
  }
  
  //read triangles of a mesh
  private static void readTriangles(MD3Mesh newMesh, DataInput din) throws IOException {
	  newMesh.triangles = new int[newMesh.triangleNum][3];
	  for (int i=0;i<newMesh.triangleNum;i++) {
	    newMesh.triangles[i][0] = din.readInt();
	    newMesh.triangles[i][1] = din.readInt();
	    newMesh.triangles[i][2] = din.readInt();
	  }
	}
  
  //read texture names (and associated texture data) of a mesh
  private static void readTextures(MD3Mesh newMesh, DataInput din) throws IOException {
    newMesh.textures = new MD3Texture[newMesh.textureNum];        
    for (int i=0;i<newMesh.textureNum;i++) {
      String strTexName=readName(din, 68); // 65 chars, 32bit aligned == 68 chars
      newMesh.textures[i]=MD3ModelFactory.getFactory().makeMD3Texture(strTexName, strTexName);
    }
  }

  //read texture coord of a mesh
  private static void readTexVecs(MD3Mesh newMesh, DataInput din) throws IOException {
    newMesh.textureCoord = new float[newMesh.vertexNum][2];
    for (int i=0;i<newMesh.vertexNum;i++) {
      newMesh.textureCoord[i][0] = din.readFloat();
      newMesh.textureCoord[i][1] = din.readFloat();
    }
  }
  
  //read mesh vertex frames  
  private static void readVertices(MD3Mesh newMesh, DataInput din) throws IOException {
    newMesh.meshFrames = new Vec3[newMesh.meshFrameNum][newMesh.vertexNum];
    newMesh.meshVertexNormals = new int[newMesh.meshFrameNum][newMesh.vertexNum][2];
    for (int i=0;i<newMesh.meshFrameNum;i++) {
      for (int j=0;j<newMesh.vertexNum;j++) {
        newMesh.meshFrames[i][j]=new Vec3();            
        newMesh.meshFrames[i][j].x = (float)din.readShort() / 64.0f;
        newMesh.meshFrames[i][j].y = (float)din.readShort() / 64.0f;
        newMesh.meshFrames[i][j].z = (float)din.readShort() / 64.0f;
        
        newMesh.meshVertexNormals[i][j][0] = din.readUnsignedByte();
        newMesh.meshVertexNormals[i][j][1] = din.readUnsignedByte();
      }
    }
  }  
}