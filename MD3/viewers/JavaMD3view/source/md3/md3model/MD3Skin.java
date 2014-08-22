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
 * <p>The MD3Skin class provides access tot the .skin files that accompany
 * player models. MD3Skin objects are nothing more than mappings between mesh
 * names and MD3Texture objects, as specified in a .skin file.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3Skin {	
	
	private Map meshTextures=new HashMap();
	
	/**
	 * <p>Create a new MD3Skin resource object and initialize it with data
	 * coming from the specified .skin file.
	 */
	protected MD3Skin(String filename) throws IOException {
		FileInputStream fin=new FileInputStream(filename.trim());
		SKINIO.loadSkin(this, fin);
		fin.close();
	}
	
	/**
	 * <p>Create a new MD3Skin resource object and initialize it with data
	 * coming from the specified input stream.
	 */
	protected MD3Skin(InputStream in) throws IOException {
		SKINIO.loadSkin(this, in);
	}
	
	/**
	 * <p>Create an empty MD3Skin resource object.
	 */
	protected MD3Skin() {		
	}
	
	/**
	 * <p>Return the texture associated with the specified mesh in this skin.
	 * This will return null if there is no mapping for the given mesh.
	 *
	 * <p>Note that only the first part of the mesh names have to match! So
	 * if you request l_legs_2, a mapping for l_legs might be returned.
	 */
	public MD3Texture getTexture(String meshName) {
		Iterator meshes=meshTextures.keySet().iterator();
		
		while (meshes.hasNext()) {
			String name=(String)meshes.next();
			if (meshName.startsWith(name))
				return (MD3Texture)meshTextures.get(name);				
		}
		
		return null;		
	}
	
	/**
	 * <p>Add a mesh to texture mapping to this MD3Skin object. Use this when
	 * creating you own skins.
	 */
	public void putTexture(String meshName, MD3Texture tex) {
		meshTextures.put(meshName, tex);
	}
}