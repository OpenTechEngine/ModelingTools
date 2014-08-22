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
 * <p>A class with metadata describing an MD3 model animation.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3Animation {

	/**
	 * <p>The animation type specifies for what parts of a model this animation data
	 * is applicable (e.g. LEGS, BOTH, ...).
	 */
	public AnimationType type;
	
	/**
	 * <p>For LEGS animations, this value give the amount by which the first frame
	 * value was offset because of the need to skip TORSO frames.
	 *
	 * <p>For other types of animations, this value has no meaning.
	 */
	public int offset=0;
	
	/**
	 * <p>Name of the animation.
	 */
	public String name;
	
	/**
	 * <p>First frame.
	 */
	public int first=0;
	
	/**
	 * <p>Number of frames in the animation. A value of less then 0 indicates that all available
	 * frames should be used.
	 */
	public int num=0;
	
	/**
	 * <p>Number of looping frames.
	 */
	public int looping=0;
	
	/**
	 * <p>Frames per second.
	 */
	public int fps=0;
	
	public String toString() {
		return type + " - " + name + " - " + first + " " + num + " " + looping + " " + fps;
	}
	
	/**
	 * <p>Create a new MD3Animation object and initialize it with the data on the given line.
	 *
	 * <p>Note that the name and type of the animation are NOT initialized! This has to be done
	 * separately!
	 */
	protected MD3Animation(String line) throws IOException {
		CFGIO.loadAnimation(this, line);
	}
	
	/**
	 * <p>Create an empty animation object. Constructor for use in subclasses.
	 */
	protected MD3Animation() {
	}	
}