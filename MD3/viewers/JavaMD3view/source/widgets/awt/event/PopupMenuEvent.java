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

package widgets.awt.event;

/**
 * <p>Event signaling the request for a popup menu to become visible
 * at a certain position.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class PopupMenuEvent extends java.util.EventObject {
	private int x, y;
	
	/**
	 * <p>Create a popup menu request event coming from a given source with
	 * a given location for the popup menu.
	 */
	public PopupMenuEvent(Object source, int x, int y) {
		super(source);
		
		this.x=x;
		this.y=y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}	
}