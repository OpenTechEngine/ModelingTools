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
 * <p>Abstract superclass of all classes that represent objects that can
 * visit MD3Model objects according to the visitor pattern.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public abstract class MD3ModelVisitor {
	
  /**
   * <p>Visit a MD3Model object.
   *
   * @param model The object to visit.
   */
  public abstract void visit(MD3Model model);
}