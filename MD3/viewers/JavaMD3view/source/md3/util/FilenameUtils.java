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

package md3.util;

import java.io.*;

/**
 * <p>Booch utility class with filename manipulation functions.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public final class FilenameUtils {

  /**
   * <p>Return actual file name, without extension.
   */
  public static String getShortFilename(String filename) {
    String res=new File(filename.trim()).getName();
    int dotIndex=res.lastIndexOf('.');
    if (dotIndex!=-1)
      return res.substring(0, dotIndex);
    else
      return res;
  }
  
  /**
   * <p>Return LOD postfix of a short (first use getShortFilename())
   * filename (e.g. "_1").
   */
  public static String getLODPostfix(String filename) {
    int i=filename.lastIndexOf('_');
    if (i==-1)
      return "";
    else
      return filename.substring(i, filename.length());
  }
}