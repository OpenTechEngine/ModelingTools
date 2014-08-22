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
 * <p>Booch utility class that provides .cfg file IO functions.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
class CFGIO {
	
//----- this information seems to be hard coded into the Q3 engine -----
	private static final int NROFANIMATIONS=25;
	private static final String[] animationNames =
		{ "Death 1", "Dead 1", "Death 2", "Dead 2", "Death 3", "Dead 3",
			"Gesture", "Shoot", "Hit", "Drop Weapon", "Raise Weapon", "Stand With Weapon", "Stand With Gauntlet",
			"Crouched Walk", "Walk", "Run", "Backpedal", "Swim", "Jump Forward", "Land Forward", "Jump Backward", "Land Backward", "Stand Idle", "Crouched Idle", "Turn In Place" };	
	private static final AnimationType[] animationTypes = //order of animation types corresponds to order of names!!
		{ AnimationType.BOTH, AnimationType.BOTH, AnimationType.BOTH, AnimationType.BOTH, AnimationType.BOTH, AnimationType.BOTH,
			AnimationType.TORSO, AnimationType.TORSO, AnimationType.TORSO, AnimationType.TORSO, AnimationType.TORSO, AnimationType.TORSO, AnimationType.TORSO,
			AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS, AnimationType.LEGS };
//----------------------------------------------------------------------
		
	private CFGIO() {} //cannot instantiate
		
	static void loadAnimationCfg(AnimationCfg newAnimCfg, InputStream in) throws IOException {
		BufferedReader bin=new BufferedReader(new InputStreamReader(in));
		
		int i=0; //last read animation, animation order is hard coded!
		int TORSO_OFFSET=-1;
		int firstTORSOFrame=-1; //first of the TORSO animation frames
		boolean IN_HEADER=true; //are we still in the header of the .cfg file?
		
		//parse file
		String line=bin.readLine();
		while (line!=null) try {
			line=line.trim();
			
			if (!line.equals("") && !line.startsWith("//")) { //ignore empty lines and comments
				if (IN_HEADER && line.startsWith("sex")) {
					//parse line: sex [m | f | ...]
					StringTokenizer st=new StringTokenizer(line, " \t\n\r\f/");
					
					st.nextToken(); //throw away first token
					String sexStr=st.nextToken();
					if (sexStr.length()!=1)
            //non-fatal error, don't thow exception
            newAnimCfg.sex='?';
					else
						newAnimCfg.sex=sexStr.charAt(0);
				}
				else if (IN_HEADER && line.startsWith("headoffset")) {
					//parse line: headoffset X Y Z
					StringTokenizer st=new StringTokenizer(line, " \t\n\r\f/");
					
					st.nextToken(); //throw away first token
					
					newAnimCfg.headOffset=
						new md3.util.Vec3(new Float(st.nextToken()).floatValue(),									
					                    new Float(st.nextToken()).floatValue(),
					                    new Float(st.nextToken()).floatValue());
				}
				else if (IN_HEADER && line.startsWith("footsteps")) {
					//parse line: footsteps [normal | mech | ...]
					StringTokenizer st=new StringTokenizer(line, " \t\n\r\f/");
					
					st.nextToken(); //throw away first token
					newAnimCfg.footsteps=st.nextToken().trim();
          
          if (!(newAnimCfg.footsteps.toLowerCase().equals("default") ||
                newAnimCfg.footsteps.toLowerCase().equals("normal") ||
                newAnimCfg.footsteps.toLowerCase().equals("boot") ||
                newAnimCfg.footsteps.toLowerCase().equals("flesh") ||
                newAnimCfg.footsteps.toLowerCase().equals("mech") ||
                newAnimCfg.footsteps.toLowerCase().equals("energy")
               ))
            //don't throw an exception, non-fatal error
            newAnimCfg.footsteps="illegal footsteps value (" + newAnimCfg.footsteps + ")";
				}
				else {
					//assume it's an animation line
					IN_HEADER=false; //no longer in header
					
					MD3Animation animation=MD3ModelFactory.getFactory().makeMD3Animation(line);
										
					animation.name=animationNames[i];
					animation.type=animationTypes[i++];				
	
					//workaround for strange numbering in animation.cfg: skip TORSO frames
					//for LEGS animation lines...
					if (animation.type==AnimationType.LEGS) {
						//when first LEGS animation is found, calc # of TORSO frames
						if (TORSO_OFFSET==-1)
							TORSO_OFFSET=animation.first-firstTORSOFrame;
												
						animation.first-=TORSO_OFFSET;
						animation.offset=TORSO_OFFSET;
					}
					else if (animation.type==AnimationType.TORSO)
						if (firstTORSOFrame==-1)
							firstTORSOFrame=animation.first;

					newAnimCfg.putAnimation(animation);
				}
			}
			
			line=bin.readLine();
		}
		catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
	
	static void loadAnimation(MD3Animation newAnimation, String line) throws IOException {
		//parse line:
		//first frame, num frames, looping frames, frames per second				
		try {
			StringTokenizer st=new StringTokenizer(line, " \t\n\r\f/");
			
			newAnimation.first=new Integer(st.nextToken()).intValue();									
			newAnimation.num=new Integer(st.nextToken()).intValue();
			newAnimation.looping=new Integer(st.nextToken()).intValue();
			newAnimation.fps=new Integer(st.nextToken()).intValue();
		} catch (Exception e) {
			throw new IOException("corrupt animation line");
		}
	}
}