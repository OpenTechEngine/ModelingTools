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
import java.io.*;

import md3.md3model.*;
import md3.md3view.*;

/**
 * <p>An MD3Model visitor that walks through an MD3Model structure and updates
 * a tags menu with the tags found in the visited models.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3ModelUpdateTagMenuVisitor extends MD3ModelVisitor {
  private Menu tagsMenu;
  private String modelToAttachName=null;
  
  /**
   * <p>Create a new visitor to update the given tags menu.
   *
   * <p>The tags menu will have menu items that pop up file dialogs to 
   * obtain the name of the files to attach.
   *
   * @param tagsMenu Menu to update.
   */
  public MD3ModelUpdateTagMenuVisitor(Menu tagsMenu) {
    this.tagsMenu=tagsMenu;
    tagsMenu.removeAll();
  }
  
  /**
   * <p>Create a new visitor to update the given tags menu.
   *
   * <p>The tags menu will have menu items that attach the given model to
   * a certain tag position.
   *
   * @param tagsMenu Menu to update.
   * @param name Name of model to attach.
   */
  public MD3ModelUpdateTagMenuVisitor(Menu tagsMenu, String name) {
  	this(tagsMenu);
  	this.modelToAttachName=name;
  }
  
  public void visit(final MD3Model model) {
    //create submenu for the model
    Menu modelSubMenu=new Menu(new File(model.loadFilename).getName());
    tagsMenu.add(modelSubMenu);
    
    //enter tags of this model in a new sub menu
    for (int i=0;i<model.tagNum;i++) {
      final int tagIndex=i; //we need this index in the listener
      MenuItem tagItem=new MenuItem(model.boneFrames[0].tags[i].name + "...");
      modelSubMenu.add(tagItem);
      
      if (modelToAttachName==null) //register a listener for models coming from files
        tagItem.addActionListener( new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String filename=MD3View.instance().showOpenDialog("*.md3");
            //open the requested file
            if (filename!=null) try {
            	InputStream fin=new FileInputStream(filename);
            	MD3View.instance().attachModel(model, tagIndex, filename, fin);
            	fin.close();
            }
            catch (IOException ex) {
              MD3View.instance().showExceptionDialog(ex.getMessage());
            }
            
            MD3View.instance().md3canvas.sDisplay();
          }
        });
      else //register a listener for the given model
        tagItem.addActionListener( new ActionListener() {
          public void actionPerformed(ActionEvent e) {
          	try {
          		InputStream in=MD3View.instance().getInputStreamForPath(modelToAttachName);
	            MD3View.instance().attachModel(model, tagIndex, modelToAttachName, in);
	            in.close();
            }
            catch (java.io.IOException ex) {
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