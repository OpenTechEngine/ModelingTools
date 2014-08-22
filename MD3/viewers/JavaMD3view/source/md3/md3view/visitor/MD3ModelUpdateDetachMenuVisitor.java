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

package md3.md3view.visitor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import md3.md3model.*;
import md3.md3view.*;

/**
 * <p>An MD3Model visitor that walks through an MD3Model structure and updates
 * a detach menu with the names of the visited models.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3ModelUpdateDetachMenuVisitor extends MD3ModelVisitor {
	private Menu menu;
	
	/**
	 * <p>Create a new visitor that will update the given menu.
	 */
	public MD3ModelUpdateDetachMenuVisitor(Menu detachMenu) {
		this.menu=detachMenu;
		detachMenu.removeAll();	
	}
	
	public void visit(final MD3Model model) {
		MenuItem modelItem=new MenuItem(new java.io.File(model.loadFilename).getName());
		modelItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MD3View.instance().detachModel(model);
        MD3View.instance().md3canvas.sDisplay();
			}
		});
		menu.add(modelItem);
		
    //visit children
    Iterator it=model.linkedModels();
    while (it.hasNext())
      ((MD3Model)it.next()).accept(this);		
	}
}