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
 * <p>A class providing functionality to access the animation data found in
 * the <i>animation.cfg</i> files that come with (some) animated models.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class AnimationCfg {

	/**
	 * <p>Mapping of animation names to MD3Animation objects.
	 */
	private Map animations=null;
			
	/**
	 * <p>Sex of animated model: 'm' for male, 'f' for female, 'n' for neuter.
   * Defaults to '?'.
	 */
	public char sex='?';
	
	/**
	 * <p>Head offset, none by default. This is the offset of the head model in
	 * the HUD in X,Y,Z coordinates.
	 */
	public md3.util.Vec3 headOffset=new md3.util.Vec3(0f, 0f, 0f);
	
	/**
	 * <p>Type of footstep sounds associated with animations (e.g. "mech").
	 * Defaults to "unspecified". Posible values: "default", "normal", "boot",
   * "flesh", "mech" and "energy".
	 */
	public String footsteps="unspecified";
	
	/**
	 * <p>Create an empty animation data resource. Use this when creating and
	 * managing your own animations.
	 */
	protected AnimationCfg() {
		animations=new TreeMap();
	}

	/**
	 * <p>Constructor that loads an <i>animation.cfg</i> file into the internal
	 * data stuctures. The read information can later be consulted using the
	 * animationNames() and getAnimation() methods.
	 *
	 * @param file The file to open.
	 */
	protected AnimationCfg(String file) throws IOException {
		this();
		InputStream fin=new FileInputStream(file);
		CFGIO.loadAnimationCfg(this, fin);
		fin.close();
	}
	
	/**
	 * <p>Constructor that loads <i>animation.cfg</i> data coming from the specified
	 * input stream into the internal data stuctures. The read information can later
	 * be consulted using the animationNames() and getAnimation() methods.
	 *
	 * @param in The stream to read data from.
	 */
	protected AnimationCfg(InputStream in) throws IOException {
		this();
		CFGIO.loadAnimationCfg(this, in);
	}

	/**
	 * <p>Return an Iterator listing the names of all known animations.
	 */
	public Iterator animationNames() {
		return animations.keySet().iterator();
	}
	
	/**
	 * <p>Look up the animation data for the animation with the specified name.
	 *
	 * @param name Name of the animation.
	 * @return The animation data associated with the name or null if no data was found.
	 */
	public MD3Animation getAnimation(String name) {
		return (MD3Animation)animations.get(name);
	}
	
	/**
	 * <p>Add the specified animation to the list of known animations.
	 */
	public void putAnimation(MD3Animation anim) {
		animations.put(anim.name, anim);
	}
}