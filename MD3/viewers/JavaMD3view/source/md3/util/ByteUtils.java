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
 * <p>Booch utility class with usefull byte-level functions.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public final class ByteUtils {
	private ByteUtils() {} //cannot instantiate
	
  /**
   * <p>Return byte at position pos in integer i.
   */
  public static byte getByte(int i, int pos) {
    return (byte)((i >> (24-pos*8)) & 0x000000FF);
  }
  
  public static void copyAllBytes(InputStream source, OutputStream target) throws IOException {
    byte[] buf=new byte[1024];
    int len=0;
    while((len=source.read(buf)) > 0 )
    	target.write(buf, 0, len);
  }
}