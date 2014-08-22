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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import md3.md3model.*;
import md3.md3view.*;

/**
 * <p>An MD3Model visitor that walks through an MD3Model structure and updates
 * a meshes menu with the meshes found in the visited models.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3ModelUpdateMeshMenuVisitor extends MD3ModelVisitor {
	private Menu meshesMenu;
	private String textureToApplyName=null;
	
  /**
   * <p>Create a new visitor to update the given meshes menu.
   *
   * <p>The meshes menu will have menu items that pop up file dialogs to 
   * obtain the name of the texture to apply.
   *
   * @param meshesMenu Menu to update.
   */
	public MD3ModelUpdateMeshMenuVisitor(Menu meshesMenu) {
		this.meshesMenu=meshesMenu;
		meshesMenu.removeAll();
	}
	
  /**
   * <p>Create a new visitor to update the given meshes menu.
   *
   * <p>The meshes menu will have menu items that apply the texture with the
   * given name to a certain mesh.
   *
   * @param meshesMenu Menu to update.
   * @param name Name of texture to apply.
   */
	public MD3ModelUpdateMeshMenuVisitor(Menu meshesMenu, String name) {
		this(meshesMenu);
		this.textureToApplyName=name;
	}
	
	public void visit(MD3Model model) {
    //create submenu for the model
    Menu modelSubMenu=new Menu(new File(model.loadFilename).getName());
    meshesMenu.add(modelSubMenu);
    
  	for (int i=0;i<model.meshNum;i++) {
  		final String meshName=model.meshes[i].name;
  		MenuItem meshItem=new MenuItem(meshName + "...");
  		modelSubMenu.add(meshItem);
  		
  		if (textureToApplyName==null)
  			meshItem.addActionListener(new ActionListener() {
  				public void actionPerformed(ActionEvent e) {
  					String filename=MD3View.instance().showOpenDialog("*.tga;*.jpg");
  					if (filename!=null) try {
  						String skin=meshName + "," + filename + "\n";
  						MD3View.instance().applySkin(filename, new ByteArrayInputStream(skin.getBytes()));
  					}
            catch (IOException ex) {
              MD3View.instance().showExceptionDialog(ex.getMessage());
            }
            
            MD3View.instance().md3canvas.sDisplay();
  				}
  			});
  		else
  			meshItem.addActionListener(new ActionListener() {
  				public void actionPerformed(ActionEvent e) {
  					try {
  						String skin=meshName + "," + textureToApplyName + "\n";
  						MD3View.instance().applySkin(textureToApplyName, new ByteArrayInputStream(skin.getBytes()));
  					}
            catch (IOException ex) {
              MD3View.instance().showExceptionDialog(ex.getMessage());
            }  					
            
            MD3View.instance().md3canvas.sDisplay();
  				}
  			});
  	}
		
    //visit children
    Iterator it=model.linkedModels();
    while (it.hasNext())
      ((MD3Model)it.next()).accept(this);
	}
}