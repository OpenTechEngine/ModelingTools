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

/**
 * <p>This is a factory class that follows the Abstract Factory pattern. The
 * MD3 model related classes in the package cannot be instantiated directely. Use
 * the factory methods provided here to obtain the necessary instances of those
 * classes.
 *
 * <p>Subclass this class if you want the MD3 model construction process to
 * create and use instances of your own subclasses. The default implementations
 * provided here return instances of the default MD3 model classes provided
 * in the md3.md3model package.
 *
 * <p>The factory methods that return empty objects can be used when creating your
 * own models.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ModelFactory {
	
	private static MD3ModelFactory currentFactory=null;
	
	/**
	 * <p>Set the factory that will be used during the MD3Model object creation
	 * process.
	 */
	public static void setFactory(MD3ModelFactory factory) {
		currentFactory=factory;
	}
	
	/**
	 * <p>Return the currently used factory, null if none.
	 */
	public static MD3ModelFactory getFactory() {
		return currentFactory;
	}
	
  /**
   * <p>Factory method that loads an MD3 model from a file and sets up an
   * MD3Model object with the read information.
   *
   * @param filename Name of file to load.
   * @return An MD3Model object containing the read information.
   */
	public MD3Model makeMD3Model(String filename) throws IOException {
		return new MD3Model(filename);		
	}
	
  /**
   * <p>Factory method that loads an MD3 model from a specified input stream and
   * sets up an MD3Model object with the read information. The given name will
   * be stored as the model's loadFilename.
   *
   * @param name Name of the model.
   * @param in Stream to read data from.
   * @return An MD3Model object containing the read information.
   */
	public MD3Model makeMD3Model(String name, InputStream in) throws IOException {
		return new MD3Model(name, in);
	}
	
	/**
	 * <p>Create a new, uninitialized MD3Model object.
	 */
	public MD3Model makeMD3Model() {
		return new MD3Model();
	}
	
	/**
	 * <p>Read a bone frame object with the specified amount of tag positions from
	 * the given input stream. Note that the tag information itself will NOT be initialized.
	 * This has to be done separately!
	 */
	public MD3BoneFrame makeMD3BoneFrame(int tagNum, DataInput din) throws IOException {
		return new MD3BoneFrame(tagNum, din);
	}
	
	/**
	 * <p>Create an empty bone frame with the specified amount of tag positions.
	 */
	public MD3BoneFrame makeMD3BoneFrame(int tagNum) {
		return new MD3BoneFrame(tagNum);
	}
	
	/**
	 * <p>Read a MD3Tag object from the specified input stream.
	 */
	public MD3Tag makeMD3Tag(DataInput din) throws IOException {
		return new MD3Tag(din);
	}
	
	/**
	 * <p>Create a new, uninitialized MD3Tag object.
	 */
	public MD3Tag makeMD3Tag() {
		return new MD3Tag();
	}
	
	/**
	 * <p>Create a new MD3Mesh object and initialize it with data read from
	 * the specified input stream.
	 */
	public MD3Mesh makeMD3Mesh(DataInput din) throws IOException {
		return new MD3Mesh(din);
	}
	
	/**
	 * <p>Creates an empty, uninitialized MD3Mesh object.
	 */
	public MD3Mesh makeMD3Mesh() {
		return new MD3Mesh();
	}
	
  /**
   * <p>Factory method that loads a texture from a file.
   *
   * @param name Name of the texture as specified in .md3 or .skin file.
   * @param loadFilename Full name of file from which texture data will be loaded.
   * @return The loaded MD3Texture object;
   */
	public MD3Texture makeMD3Texture(String name, String loadFilename) throws IOException {		
		return new MD3Texture(name, loadFilename);
	}
	
  /**
   * <p>Factory method that loads a texture from an input stream.
   *
   * @param name Name of the texture as specified in .md3 or .skin file.
   * @param loadFilename Full name of file from which texture is (supposedly) loaded.
   * @param in Stream to load texture data from.
   * @return The loaded MD3Texture object;
   */
	public MD3Texture makeMD3Texture(String name, String loadFilename, InputStream in) throws IOException {
		return new MD3Texture(name, loadFilename, in);
	}
	
	/**
	 * <p>Creates an empty, uninitialized MD3Texture object.
	 */
	public MD3Texture makeMD3Texture() {
		return new MD3Texture();
	}
	
	/**
	 * <p>Factory method that creates a new MD3Animation object and initialize it with
	 * the data in the specified string. Note that the name and type of the animation
	 * are NOT initialized! This has to be done separately!
	 */
	public MD3Animation makeMD3Animation(String line) throws IOException {
		return new MD3Animation(line);
	}
	
	/**
	 * <p>Creates an empty, uninitialized MD3Animation object.
	 */
	public MD3Animation makeMD3Animation() {
		return new MD3Animation();
	}
	
	/**
	 * <p>Load an <i>animation.cfg</i> file into the internal data stuctures of
	 * an AnimationCfg object.
	 *
	 * @param filename Name of the file to open.
	 */
	public AnimationCfg makeAnimationCfg(String filename) throws IOException {
		return new AnimationCfg(filename);
	}
	
	/**
	 * <p>Load <i>animation.cfg</i> data from the given input stream into the
	 * internal data stuctures of an AnimationCfg object.
	 *
	 * @param in Stream to load data from.
	 */
	public AnimationCfg makeAnimationCfg(InputStream in) throws IOException {
		return new AnimationCfg(in);
  }
	
	/**
	 * <p>Creates an empty AnimationCfg resource object.
	 */
	public AnimationCfg makeAnimationCfg() {
		return new AnimationCfg();
	}
	
	/**
	 * <p>Load data from the specified skin file and put it in an MD3Skin resource object.
	 */
	public MD3Skin makeMD3Skin(String filename) throws IOException {
		return new MD3Skin(filename);
	}
	
	/**
	 * <p>Load data from the specified input stream and put it in an MD3Skin resource object.
	 */
	public MD3Skin makeMD3Skin(InputStream in) throws IOException {
	  return new MD3Skin(in);	
	}
	
	/**
	 * <p>Creates an empty MD3Skin resource object.
	 */
	public MD3Skin makeMD3Skin() {
		return new MD3Skin();
	}
}