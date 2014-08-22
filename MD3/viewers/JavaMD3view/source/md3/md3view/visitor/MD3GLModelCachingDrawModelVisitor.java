/*---------------------
  CURRENTLY NOT USED!
---------------------*/

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

import md3.md3model.*;
import md3.md3view.glmodel.*;
import md3.md3view.*;
import md3.util.*;

/**
 * <p>Render class which caches interpolated data.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3GLModelCachingDrawModelVisitor extends MD3GLModelDrawModelVisitor {
	
	private static Map meshFrameCache=new HashMap();
	private class MeshFrameCacheKey {
		private Vec3[] mesh;
		private float frac;
		
		public MeshFrameCacheKey(Vec3[] mesh, float frac) {
			this.mesh=mesh;
			this.frac=frac;
		}
		
		public int hashCode() {
			return mesh.hashCode() + (int)(frac * 10.0f);
		}
		
		public boolean equals(Object o) {
			MeshFrameCacheKey k=(MeshFrameCacheKey)o;
			return this.mesh==k.mesh && this.frac==k.frac;
		}
	}
	
	private static Map transformationCache=new HashMap();
	private class TransformationCacheKey {
		private MD3Tag tag;
		private float frac;
		
		public TransformationCacheKey(MD3Tag tag, float frac) {
			this.tag=tag;
			this.frac=frac;
		}

		public int hashCode() {
			return tag.hashCode() + (int)(frac * 10.0f);
		}

		public boolean equals(Object o) {
			TransformationCacheKey k=(TransformationCacheKey)o;
			return this.tag==k.tag && this.frac==k.frac;
		}
	}
	
	public MD3GLModelCachingDrawModelVisitor(MD3ViewGLCanvas md3canvas) {
		super(md3canvas);
	}
	
  protected Vec3[] interpolateMeshFrame(Vec3[] currMeshFrame, Vec3[] nextMeshFrame, float frac) {
  	MeshFrameCacheKey k=new MeshFrameCacheKey(currMeshFrame, frac);
  	Vec3[] res=(Vec3[])meshFrameCache.get(k);  	
  	if (res==null) { //not in cache -> interpolate and put in cache
  		res=super.interpolateMeshFrame(currMeshFrame, nextMeshFrame, frac);
  		meshFrameCache.put(k, res);
  	}
	  	
  	return res;
  }
  
  protected float[] interpolateTransformation(MD3Tag currFrameTag, MD3Tag nextFrameTag, float frac) {
  	TransformationCacheKey k=new TransformationCacheKey(currFrameTag, frac);
  	float[] res=(float[])transformationCache.get(k);
  	if (res==null) {
  		res=super.interpolateTransformation(currFrameTag, nextFrameTag, frac);
  		transformationCache.put(k, res);
  	}
  	
  	return res;
  }
	
}