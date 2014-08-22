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

package cio;

import java.io.*;

/**
 * <p>Data output class with low level binary IO operations. The
 * methods in this class write little endian data and accept Java
 * big endian data.
 *
 * <p>Writing of bytes is unaffected. Also, the char io methods
 * act the same as in the standard DataOutputStream class. So in
 * the case that multiple bytes are written in writeLine() or writeUTF(),
 * the endian order is not switched!
 *
 * @see java.io.DataOutputStream
 *
 * @author Donald Gray (dgray@widomaker.com)
 */
public class LittleEndianDataOutputStream extends FilterOutputStream implements DataOutput {
  private DataOutputStream dout;
  
  public LittleEndianDataOutputStream(OutputStream out) {
    super(out);
    dout=new DataOutputStream(out);
  }
  
  public void writeFully(byte[] b) throws IOException {
    dout.write(b);
  }
  
  public void writeFully(byte[] b, int off, int len) throws IOException {
    dout.write(b, off, len);
  }
  
//  public int skipBytes(int n) throws IOException {
//    dout.skipBytes(n);
//  }
  
  public void writeBoolean(boolean b) throws IOException {
    dout.writeBoolean(b);
  }

  public void writeByte(byte b) throws IOException {
    dout.writeByte(b);
  }

  public void writeByte(int i) throws IOException {
    dout.writeByte((byte)i);
  }

  public void writeBytes(String s) throws IOException {
    writeChars(s);
  }

  public void writeUnsignedByte(int b) throws IOException {
    writeByte((byte)b);
  }
  
  public void writeShort(short s) throws IOException {
    dout.write((byte)(s & 0xff));
    dout.write((byte)((s >> 8) & 0xff));
  }
  
  public void writeShort(int i) throws IOException {
    writeShort((short)i);
  }
  
  public void writeUnsignedShort(int s) throws IOException {
    dout.write((byte)(s & 0xff));
    dout.write((byte)((s >> 8) & 0xff));
  }
  
  public void writeChar(char c) throws IOException {
    dout.writeChar(c);
  }
  
  public void writeChar(int i) throws IOException {
    dout.writeChar((char)i);
  }
  
  public void writeChars(String s) throws IOException {
    for(int i=0;i<s.length();i++)
		dout.writeChar(s.charAt(i));
  }
  
  public void writeInt(int it) throws IOException {
    byte[] res=new byte[4];
		res[3] = (byte)((it >> 24) & 0xff);
		res[2] = (byte)((it >> 16) & 0xff);
		res[1] = (byte)((it >>  8) & 0xff);
		res[0] = (byte)(it & 0xff);

    for(int i=0;i<4;i++)
      dout.write(res[i]);
  }

  public void writeLong(long l) throws IOException {
    byte[] res=new byte[8];
		res[7] = (byte)((l >> 56) & 0xff);
		res[6] = (byte)((l >> 48) & 0xff);
		res[5] = (byte)((l >> 40) & 0xff);
		res[4] = (byte)((l >> 32) & 0xff);
		res[3] = (byte)((l >> 24) & 0xff);
		res[2] = (byte)((l >> 16) & 0xff);
		res[1] = (byte)((l >>  8) & 0xff);
		res[0] = (byte)(l & 0xff);

    for(int i=0;i<8;i++)
      dout.write(res[i]);
  }
  
  public void writeFloat(float f) throws IOException {
    writeInt(Float.floatToIntBits(f));
  }
  
  public void writeDouble(double d) throws IOException {
		writeLong(Double.doubleToLongBits(d));
  }

//  public final void writeLine(String str) throws IOException {
//    dout.writeLine(str);
//  }

  public void writeUTF(String str) throws IOException {
    dout.writeUTF(str);
  }
}