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

import gl4java.*;

import md3.md3view.*;
import md3.md3model.*;
import md3.md3view.glmodel.*;
import md3.util.*;

/**
 * <p>An MD3GLModel visitor that walks through an MD3GLModel structure and draws
 * all meshes of the current frame of the encountered models on a specified
 * OpenGL canvas. Interpolation is done if necessary, as specified by the 
 * interpolationFraction data member of the MD3GLModel class.
 *
 * @see md3.md3view.glmodel.MD3GLModel
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3GLModelDrawModelVisitor extends MD3GLModelVisitor {
	private MD3ViewGLCanvas canvas;
	private GLFunc gl;
  
  //these tmp vars are globals for efficiency: avoid freq. contructor calls
  private Vec3 tmpVec3_1=new Vec3(), tmpVec3_2=new Vec3();
  private MD3BoneFrame tmpBoneFrame_1=MD3ModelFactory.getFactory().makeMD3BoneFrame(0);

  /**
   * <p>Create a new visitor that will draw on the specified OpenGL canvas.
   *
   * @param md3canvas The OpenGL canvas to draw on.
   */
  public MD3GLModelDrawModelVisitor(MD3ViewGLCanvas md3canvas) {
  	this.canvas=md3canvas;
  	this.gl=md3canvas.getGL();
  }
    
  /**
   * <p>Renders a model on the canvas.
   *
   * @param model The model to render.
   */
  public void visit(MD3GLModel model) {
  	//draw the model
  	
		//draw current bone frame
		if (canvas.showBoneFrame) {
			//get bone frame, interpolate if necessary
      if (model.interpolationFraction!=0.0 && model.currentFrame!=model.nextFrame)
      	//interpolate bone frame      	
      	drawBoneFrame(interpolateBoneFrame(model.boneFrames[model.currentFrame], model.boneFrames[model.nextFrame], model.interpolationFraction));
      else
      	//stick with current bone frame  	
      	drawBoneFrame(model.boneFrames[model.currentFrame]);
		}

    //draw all meshes of current frame of this model
    for (int i=0; i<model.meshNum; i++) {    
      MD3GLMesh mesh = (MD3GLMesh)model.meshes[i];
	  
  		gl.glBlendFunc(mesh.GLSrcBlendFunc, mesh.GLDstBlendFunc);
  		gl.glDepthMask(mesh.GLDepthMask);
      if (mesh.textureNum > 0 && mesh.textures[0]!=null)
        gl.glBindTexture(GLEnum.GL_TEXTURE_2D, ((MD3GLTexture)mesh.textures[0]).bind);
      else
        gl.glBindTexture(GLEnum.GL_TEXTURE_2D, 0);
      
      //get mesh frame, do interpolation if necessary
	    Vec3[] frame;
      if (model.interpolationFraction!=0.0 && model.currentFrame!=model.nextFrame)
      	//interpolate mesh frame between the 2 current mesh frames
		  	frame=interpolateMeshFrame(mesh.meshFrames[model.currentFrame], mesh.meshFrames[model.nextFrame], model.interpolationFraction);
      else
      	//no interpolation needed, just draw current frame
      	frame=mesh.meshFrames[model.currentFrame];
      
      drawMesh(mesh, frame);
    
      //draw vertex normals
	    if (canvas.showVertexNormals) {
				//get vertex normals, interpolate if necessary
	      if (model.interpolationFraction!=0.0 && model.currentFrame!=model.nextFrame)
	      	//interpolate vertex normals
	      	drawVertexNormals(frame, interpolateVertexNormals(mesh.meshVertexNormals[model.currentFrame], mesh.meshVertexNormals[model.nextFrame], model.interpolationFraction));
	      else
	      	//stick with current vertex normals
			  	drawVertexNormals(frame, mesh.meshVertexNormals[model.currentFrame]);
	    }
    }
    
    //draw all models linked to this model

    Iterator it=model.linkedModels();
    while (it.hasNext()) {
      MD3Model child=(MD3Model)it.next();
      
      //build transformation array m from matrix, interpolate if necessary
      float[] m=new float[16];
      MD3Tag currFrameTag=model.boneFrames[model.currentFrame].tags[child.getParentTagIndex()];
      if (model.interpolationFraction!=0.0f && model.currentFrame!=model.nextFrame) {
      	//we need to interpolate
		    MD3Tag nextFrameTag=model.boneFrames[model.nextFrame].tags[child.getParentTagIndex()];
		    m=interpolateTransformation(currFrameTag, nextFrameTag, model.interpolationFraction);
      }
      else {
      	//no interpolation needed, stay with last transformation
	      //OpenGL matrix is in column-major order
	      m[0] = currFrameTag.matrix[0][0]; m[4] = currFrameTag.matrix[0][1]; m[8] = currFrameTag.matrix[0][2]; m[12] = currFrameTag.position.x;
	      m[1] = currFrameTag.matrix[1][0]; m[5] = currFrameTag.matrix[1][1]; m[9] = currFrameTag.matrix[1][2]; m[13] = currFrameTag.position.y;
	      m[2] = currFrameTag.matrix[2][0]; m[6] = currFrameTag.matrix[2][1]; m[10]= currFrameTag.matrix[2][2]; m[14] = currFrameTag.position.z;
	      m[3] = 0.0f;               				m[7] = 0.0f;  					          m[11]= 0.0f;                  	  m[15] = 1.0f;
      }

      //switch to child coord system and draw child
      gl.glPushMatrix();
      gl.glMultMatrixf(m);
      child.accept(this);
      gl.glPopMatrix();
    }
  }
    
  /**
   * <p>Interpolate a bone frame between 2 given bone frames.
   *
   * @param currBoneFrame Start bone frame.
   * @param nextBoneFrame End bone frame.
   * @param frac Interpolation fraction, in [0,1].   
   */
  protected MD3BoneFrame interpolateBoneFrame(MD3BoneFrame currBoneFrame, MD3BoneFrame nextBoneFrame, float frac) {
		tmpBoneFrame_1.mins.x = (1.0f - frac) * currBoneFrame.mins.x + frac * nextBoneFrame.mins.x;
		tmpBoneFrame_1.maxs.x = (1.0f - frac) * currBoneFrame.maxs.x + frac * nextBoneFrame.maxs.x;
		tmpBoneFrame_1.position.x = (1.0f - frac) * currBoneFrame.position.x + frac * nextBoneFrame.position.x;
		tmpBoneFrame_1.mins.y = (1.0f - frac) * currBoneFrame.mins.y + frac * nextBoneFrame.mins.y;
		tmpBoneFrame_1.maxs.y = (1.0f - frac) * currBoneFrame.maxs.y + frac * nextBoneFrame.maxs.y;
		tmpBoneFrame_1.position.y = (1.0f - frac) * currBoneFrame.position.y + frac * nextBoneFrame.position.y;
		tmpBoneFrame_1.mins.z = (1.0f - frac) * currBoneFrame.mins.z + frac * nextBoneFrame.mins.z;
		tmpBoneFrame_1.maxs.z = (1.0f - frac) * currBoneFrame.maxs.z + frac * nextBoneFrame.maxs.z;
		tmpBoneFrame_1.position.z = (1.0f - frac) * currBoneFrame.position.z + frac * nextBoneFrame.position.z;
				
		return tmpBoneFrame_1;
  }
  
  /**
   * <p>Interpolate a mesh animation frame between 2 given mesh animation frames.
   *
   * @param currMeshFrame Start mesh animation frame.
   * @param nextMeshFrame End mesh animation frame.
   * @param frac Interpolation fraction, in [0,1].
   */  
  protected Vec3[] interpolateMeshFrame(Vec3[] currMeshFrame, Vec3[] nextMeshFrame, float frac) {
  	int vertexNum=currMeshFrame.length;
  	Vec3[] interpolatedMeshFrame=new Vec3[vertexNum];
  	
  	//calc interpolated vertices			 
  	for (int t=0;t<vertexNum;t++) {
			interpolatedMeshFrame[t]=new Vec3();
			interpolatedMeshFrame[t].x = (1.0f - frac) * currMeshFrame[t].x + frac * nextMeshFrame[t].x;
			interpolatedMeshFrame[t].y = (1.0f - frac) * currMeshFrame[t].y + frac * nextMeshFrame[t].y;
			interpolatedMeshFrame[t].z = (1.0f - frac) * currMeshFrame[t].z + frac * nextMeshFrame[t].z;
  	}
  	
  	return interpolatedMeshFrame;
  }
  
  /**
   * <p>Interpolate a set of vertex normals between the 2 given sets.
   *
   * @param currNormals Start normal set.
   * @param nextNormals End normal set.
   * @param frac Interpolation fraction, in [0,1].
   */  
  protected int[][] interpolateVertexNormals(int[][] currNormals, int[][] nextNormals, float frac) {
  	int[][] res=new int[currNormals.length][2];
  	
  	for (int i=0;i<currNormals.length;i++) {
  		res[i][0] = (int)((1.0f - frac) * currNormals[i][0] + frac * nextNormals[i][0]);
  		res[i][1] = (int)((1.0f - frac) * currNormals[i][1] + frac * nextNormals[i][1]);
  	}  	
  	
  	return res;
  }
  
  /**
   * <p>Interpolate an OpenGL transformation array between the transformations of 2 given tags.
   *
   * @param currFrameTag Tag with start transformation.
   * @param nextFrameTag Tag with end transformation.
   * @param frac Interpolation fraction, in [0,1].
   * @return An OpenGL compatible transformation array of length 16.
   */
  protected float[] interpolateTransformation(MD3Tag currFrameTag, MD3Tag nextFrameTag, float frac) {    
  	float[] m=new float[16];
  	
		// interpolate position
		Vec3 interpolatedPosition=new Vec3();
		interpolatedPosition.x = (1.0f - frac) * currFrameTag.position.x + frac * nextFrameTag.position.x;
		interpolatedPosition.y = (1.0f - frac) * currFrameTag.position.y + frac * nextFrameTag.position.y;
		interpolatedPosition.z = (1.0f - frac) * currFrameTag.position.z + frac * nextFrameTag.position.z;

		// interpolate rotation matrix						
		float[] currRot=QuaternionMath.quaternionFromMatrix(currFrameTag.matrix);
		float[] nextRot=QuaternionMath.quaternionFromMatrix(nextFrameTag.matrix);
		float[] interpolatedRot=QuaternionMath.quaternionSlerp(currRot, nextRot, frac);
		float[][] interpolatedMatrix=QuaternionMath.matrixFromQuaternion(interpolatedRot);
		
		// quaternion code is column based, so use transposed matrix when spitting out to gl
		m[0] = interpolatedMatrix[0][0]; m[4] = interpolatedMatrix[1][0]; m[8] = interpolatedMatrix[2][0]; m[12] = interpolatedPosition.x;
		m[1] = interpolatedMatrix[0][1]; m[5] = interpolatedMatrix[1][1]; m[9] = interpolatedMatrix[2][1]; m[13] = interpolatedPosition.y;
		m[2] = interpolatedMatrix[0][2]; m[6] = interpolatedMatrix[1][2]; m[10]= interpolatedMatrix[2][2]; m[14] = interpolatedPosition.z;
		m[3] = 0.0f;        						 m[7] = 0.0f;         			      m[11]= 0.0f;   		       m[15] = 1.0f;  	
		
		return m;
  }
               
 /**
   * <p>Draw a given bone frame (bounding box) on the canvas.
   *
   * @param bf The bone frame to draw.
   */
  protected void drawBoneFrame(MD3BoneFrame bf) {
		float x1=bf.mins.x, y1=bf.mins.y, z1=bf.mins.z,
					x2=bf.maxs.x, y2=bf.maxs.y, z2=bf.maxs.z;
    
    gl.glDisable( GLEnum.GL_TEXTURE_2D );
    gl.glDisable( GLEnum.GL_LIGHTING );

		gl.glColor3f(1f,0f,0f);
		gl.glPointSize(6f);
		gl.glBegin(GLEnum.GL_POINTS);
		gl.glVertex3f(bf.position.x, bf.position.y, bf.position.z);		
		gl.glEnd();
		gl.glPointSize(1f);
		
		gl.glColor3f(0f,1f,0f);
		gl.glBegin(GLEnum.GL_LINE_LOOP);
		gl.glVertex3f(x1,y1,z1);
		gl.glVertex3f(x1,y1,z2);
		gl.glVertex3f(x1,y2,z2);
		gl.glVertex3f(x1,y2,z1);
		gl.glEnd();
		
		gl.glBegin(GLEnum.GL_LINE_LOOP);
		gl.glVertex3f(x2,y2,z2);
		gl.glVertex3f(x2,y1,z2);
		gl.glVertex3f(x2,y1,z1);
		gl.glVertex3f(x2,y2,z1);
		gl.glEnd();
		
		gl.glBegin(GLEnum.GL_LINES);
		gl.glVertex3f(x1,y1,z1);
		gl.glVertex3f(x2,y1,z1);
		
		gl.glVertex3f(x1,y1,z2);
		gl.glVertex3f(x2,y1,z2);
		
		gl.glVertex3f(x1,y2,z2);
		gl.glVertex3f(x2,y2,z2);
		
		gl.glVertex3f(x1,y2,z1);
		gl.glVertex3f(x2,y2,z1);
		gl.glEnd();
	} 

  /**
   * <p>Draw a given animation frame of a specified mesh on the canvas.
   *
   * @param mesh The MD3Mesh that is being rendered.
   * @param vecs The animation frame data to be rendered.
   */
  protected void drawMesh(MD3Mesh mesh, Vec3[] frame) {    
    canvas.activateRenderMode();
    
		gl.glColor3f(1f,1f,1f);
    gl.glBegin( GLEnum.GL_TRIANGLES );

    //upload all triangles in the frame to OpenGL
    for (int t=0; t<mesh.triangleNum; t++) {    	
    	//calc normal vector
      tmpVec3_1.x = frame[mesh.triangles[t][1]].x - frame[mesh.triangles[t][0]].x;
      tmpVec3_2.x = frame[mesh.triangles[t][2]].x - frame[mesh.triangles[t][0]].x;
      tmpVec3_1.y = frame[mesh.triangles[t][1]].y - frame[mesh.triangles[t][0]].y;
      tmpVec3_2.y = frame[mesh.triangles[t][2]].y - frame[mesh.triangles[t][0]].y;
      tmpVec3_1.z = frame[mesh.triangles[t][1]].z - frame[mesh.triangles[t][0]].z;
      tmpVec3_2.z = frame[mesh.triangles[t][2]].z - frame[mesh.triangles[t][0]].z;
      Vec3 normal=tmpVec3_1.cross(tmpVec3_2);
      
      gl.glNormal3f(normal.x, normal.y, normal.z); //no normalization necessary, GL_NORMALIZE is enabled!
      
      gl.glTexCoord2fv( mesh.textureCoord[mesh.triangles[t][0]] );
      gl.glVertex3f( frame[mesh.triangles[t][0]].x, frame[mesh.triangles[t][0]].y, frame[mesh.triangles[t][0]].z );
      gl.glTexCoord2fv( mesh.textureCoord[ mesh.triangles[t][1]] );
      gl.glVertex3f( frame[mesh.triangles[t][1]].x, frame[mesh.triangles[t][1]].y, frame[mesh.triangles[t][1]].z );
      gl.glTexCoord2fv( mesh.textureCoord[mesh.triangles[t][2]] );
      gl.glVertex3f( frame[mesh.triangles[t][2]].x, frame[mesh.triangles[t][2]].y, frame[mesh.triangles[t][2]].z );
    }
    
    gl.glEnd();
  }
  
  /**
   * <p>Draw the given vertex normals of the given vertices on the canvas.
   *
   * @param frame The animation frame vertex data for wich the normals are drawn.
   * @param normals The vertex normal data.
   */
  protected void drawVertexNormals(Vec3[] frame, int[][] normals) {
  	float x, y, z;
  	double alpha, beta;
  	Vec3 vec;
  	
    gl.glDisable( GLEnum.GL_TEXTURE_2D );
    gl.glDisable( GLEnum.GL_LIGHTING );

		gl.glColor3f(1f,1f,0f);
  	gl.glBegin(GLEnum.GL_LINES);
  	
  	for (int i=0;i<normals.length;i++) {
  		vec=frame[i];
  		
  		//angles in radians!
  		alpha=normals[i][0] * 2d * Math.PI / 255d;
  		beta=normals[i][1] * 2d * Math.PI / 255d;
 		
  	  x=vec.x + (float)(Math.cos(beta) * Math.sin(alpha));  	  
  		y=vec.y + (float)(Math.sin(beta) * Math.sin(alpha));
  	  z=vec.z + (float)Math.cos(alpha);

  	  gl.glVertex3f(vec.x, vec.y, vec.z);
  	  gl.glVertex3f(x, y, z);  	  
  	}
  	
  	gl.glEnd();
  }
}