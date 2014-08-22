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

package md3.md3view;

/**
 * <p>Class that defines the ids of the data sources MD3 files can
 * be loaded from. This class uses the <i>typesafe enum</i> pattern.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ViewDataSource {  
  //private constructor: only predefined instances can be used!
  private MD3ViewDataSource() {}
  
  public static final MD3ViewDataSource FILE_SYSTEM=new MD3ViewDataSource();
  
  public static final MD3ViewDataSource PAK_FILE=new MD3ViewDataSource();
}