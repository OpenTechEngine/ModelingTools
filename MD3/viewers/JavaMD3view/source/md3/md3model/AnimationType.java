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

/**
 * <p>This class defines the different animation types that can be applied
 * to a MD3 model. The class uses the <i>typesafe enum</i> pattern.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class AnimationType {
  private String name; //name of animation type
  
  //private constructor: only predefined objects can be used!
  private AnimationType(String name) {
    this.name=name;
  }
  
  public String toString() {
    return name;
  }
    
  /**
   * <p>The animation is only applicable to the <i>LEGS</i> submodel of
   * a composed model. This submodel is normally the top level model,
   * loaded from a file lower.md3.
   */
  public static final AnimationType LEGS=new AnimationType("Legs");

  /**
   * <p>The animation is only applicable to the <i>TORSO</i> submodel of
   * a composed model. The TORSO submodel is normally loaded from a file upper.md3.
   */
  public static final AnimationType TORSO=new AnimationType("Torso");

  /**
   * <p>The animation is applicable to <i>BOTH</i> the LEGS and TORSO submodels of
   * a composed model.
   */
  public static final AnimationType BOTH=new AnimationType("Both");
  
  /**
   * <p>The animation is applicable to all submodels.
   */
  public static final AnimationType ALL=new AnimationType("All");
}