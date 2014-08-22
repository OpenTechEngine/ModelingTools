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
import java.util.*;

/**
 * <p>The MD3Model class contains data structures to hold all information
 * contained in an MD3 model file.
 *
 * <p>MD3Model really is a 1 class composite pattern: you can link models
 * to a model and iterate over the linked models. Normally you link an object
 * to an MD3Model if that object represents a submodel of the model at a
 * certain tag <i>position</i>.
 *
 * <p>MD3Model objects support visitor objects to do certain operations on/with
 * the information contained in an MD3Model.
 *
 * @see md3.md3model.MD3Tag
 * @see md3.md3model.MD3ModelVisitor
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3Model implements Serializable {

	public String id;
  public int version;
  
  /**
   * <p>Filename as recorded in the .md3 file. This might be empty.
   */
  public String filename;
    
  /**
   * <p>Filename of actual file from which data was loaded.
   */
	public String loadFilename;
  
  /**
   * <p>Number of animation key frames in the model.
   */
  public int boneFrameNum;
  public int tagNum; 
  public int meshNum;
  
  /**
   * <p>Maximum number of unique textures used in an md3 file.
   */
  public int maxTextureNum;
  
  /**
   * <p>Starting position of bone frame data structures.
   */
  public int boneFrameStart;
  
  /**
   * <p>Starting position of tag-structures.
   */
  public int tagStart;
  
  /**
   * <p>Starting position of mesh structures.
   */ 
  public int meshStart;
  public int fileSize;
  
  /**
   * <p>Array of bone frames size boneFrameNum. The contains the metadata (bounding box, tags, ..,)
   * for each of the bone animation frames in the model.
   */
  public MD3BoneFrame[] boneFrames;  
    
  /**
   * <p>Array of meshes in the model size meshNum. Each mesh contains the data of that mesh
   * for each of the animation frames in the model.
   */
  public MD3Mesh[] meshes; 

  /**
   * <p>Map used to store the models linked to this model. The map is indexed by tag index number and
   * also ordered by index number.
   */  
  protected SortedMap linkedModels=new TreeMap();
    
  private int parentTagIndex=-1;
  private MD3Model parent=null;

  /**
   * <p>Constructor for use in subclasses that reads an MD3 model from a file.
   */
  protected MD3Model(String filename) throws IOException {
  	filename=filename.trim();
  	FileInputStream fin=new FileInputStream(filename);
    MD3IO.loadModel(this, fin);
    this.loadFilename=filename;
    fin.close();
  }
  
  /**
   * <p>Constructor for use in subclasses that reads an MD3 model from the specified
   * input stream. The given loadFilename is stored in the corresponding data member.
   */
  protected MD3Model(String loadFilename, InputStream in) throws IOException {
  	MD3IO.loadModel(this, in);
  	this.loadFilename=loadFilename.trim();
  }
  
  /**
   * <p>Constructor for use in subclasses that creates a new, uninitialized MD3Model object.
   */
  protected MD3Model() {
  }
             
  /**
   * <p>Link a model at the specified tag <i>position</i> to this model. If
   * the position is allready occupied, the old submodel will be replaced.
   *
   * @param tagIndex Tag to link the submodel to.
   * @param child The submodel that should be linked to this model.
   */
  public void addLinkedModel(int tagIndex, MD3Model child) {
    linkedModels.put(new Integer(tagIndex), child);
    child.parentTagIndex=tagIndex;
    child.parent=this;
  }
  
  /**
   * <p>Remove the model linked to this model at the specified tag <i>position</i>.
   */
  public void removeLinkedModel(int tagIndex) {
    linkedModels.remove(new Integer(tagIndex));
  }
  
  /**
   * <p>Request an iterator to step through all models linked to this model.
   *
   * @return An iterator looping through the linked models.
   */
  public Iterator linkedModels() {
    return linkedModels.values().iterator();
  }
  
  /**
   * <p>Return the index of the tag this model is linked to in it's parent's tags
   * array. This can be used to figure out the position of this model relative to
   * that of it's parent.
   *
   * <p>This will return -1 if there is no parent model.
   */
  public int getParentTagIndex() {
  	return parentTagIndex;
  }
  
  /**
   * <p>Return the index of the tag with the given name for this model.
   *
   * <p>This will return -1 if their is no such tag.
   */
  public int getTagIndexByName(String tagName) {
  	int res=-1;
  	
  	if (boneFrameNum>0) {
  		MD3Tag[] tags=boneFrames[0].tags;
  		for (int i=0; i<tags.length; i++)
  			if (tags[i].name.equals(tagName))
  				return i;
  	}
  	
  		  	
  	return res;
  }
  
  /**
   * <p>Return the parent model of this model, or null if none.
   */
  public MD3Model getParent() {
  	return parent;
  }

  /**
   * <p>MD3Model objects can be visited, according to the visitor pattern, by
   * MD3ModelVistor objects.
   *
   * @param v The visitor.
   */
  public void accept(MD3ModelVisitor v) {
    v.visit(this);
  }
  
  /**
   * <p>Return wether or not this model contains animation data.
   */
  public boolean animated() {
  	return boneFrameNum>1;
  }
}