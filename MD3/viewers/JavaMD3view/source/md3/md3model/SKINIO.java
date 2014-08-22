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
 * <p>Booch utility class that provides .skin file IO functions.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
class SKINIO {
	
	private SKINIO() {} //cannot instantiate
	
	static void loadSkin(MD3Skin skin, InputStream in) throws IOException {
		BufferedReader bin=new BufferedReader(new InputStreamReader(in));
		
		//parse lines:
	  //meshname,texturename
	  String line=bin.readLine();
	  while (line!=null) {
	  	line=line.trim();
	  	
	  	StringTokenizer st=new StringTokenizer(line, ",\n\r");
	  	if (st.countTokens()==2) { //ignore lines with 0, 1 or more than 2 tokens
	  		String meshName=st.nextToken().trim();
	  		String textureName=st.nextToken().trim();
	  		
	  		MD3Texture tex=MD3ModelFactory.getFactory().makeMD3Texture(textureName, textureName);
	  		
	  		skin.putTexture(meshName, tex);
	  	}
	  	
	  	line=bin.readLine();
	  }	  
	}
	
}