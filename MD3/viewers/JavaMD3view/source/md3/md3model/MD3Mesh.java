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

import md3.util.*;

/**
 * <p>Represents data of an MD3 model mesh. This also includes the textures
 * of the mesh.
 *
 * <p>If the containing MD3 model has bone animation frames, a version of
 * the mesh for each of those animation key frames is provided.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3Mesh implements java.io.Serializable {
	
  public String id;
  public String name;
  
  /**
   * <p>Number of animation frames of mesh, same as boneFrameNum in MD3Model.
   *
   * @see md3.md3model.MD3Model
   */
  public int meshFrameNum; 
  public int textureNum; // number of textures this model has
  public int vertexNum; // number of vertices in mesh
  public int triangleNum; // number of triangles in mesh
  public int triangleStart;
  public int textureStart;
  public int texVecStart;
  public int vertexStart;
  public int meshSize;
  
  /**
   * <p>Array of textures of size textureNum. In most cases, there's only 1 texture per mesh.
   */
  public MD3Texture[] textures;
  
  /**
   * <p>Indices into meshFrames and textureCoord arrays of the triangle vertices. Size triangleNum * 3.
   */
  public int[][] triangles;

  /**
   * <p>U/V texture coordinates of vertices. Size vertexNum * 2.
   */
  public float[][] textureCoord;

  /**
   * <p>2d array of size meshFrameNum * vertexNum that stores mesh frame triangle vertices.
   */
  public Vec3[][] meshFrames;
  
  /**
   * <p>3d array of size meshFrameNum * vertexNum * 2 with spherical coordinates giving the
   * direction of the vertex normal. They are both unsigned byte values. The first one is
   * the inclination, and the second the rotation in the horizontal plane. Both actually
   * run 0..255 for full rotation.
   */
  public int[][][] meshVertexNormals;
  
  /**
   * <p>Create a mesh object with data coming from the specified input stream.
   */
  protected MD3Mesh(DataInput din) throws IOException {
  	MD3IO.loadMesh(this, din);
  } 
  
  /**
   * <p>Create empty new mesh object.
   */
  protected MD3Mesh() {
  } 
}