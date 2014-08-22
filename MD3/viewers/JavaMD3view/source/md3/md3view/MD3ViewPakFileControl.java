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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import md3.util.*;
import md3.md3view.visitor.*;

import widgets.awt.*;
import widgets.awt.event.*;

/**
 * <p>This class implements the pak file control of the MD3View application.
 * A tree overview of the contents of a pak will be shown. The user can
 * interact with the tree by double clicking on nodes or by using popup menus.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ViewPakFileControl extends ScrollPane {
	
	private Tree pakFileTree;
	private MD3View viewFrame;		
	private ZipFile zipFile;
	private Menu attachToMenu, applyToMeshMenu;
	private Image unknownIcon, skinIcon, textureIcon, textIcon, shaderIcon;	
	
	/**
	 * <p>Path of last file loaded from the current pak file.
	 */
	protected String basePakFileOpenPath;

  /**
   * <p>Create a new pak file control widget for the given application.
   */
	public MD3ViewPakFileControl(MD3View container) {
		super(ScrollPane.SCROLLBARS_AS_NEEDED);
		 	
  	zipFile=null;
  	
		//setup tree
		
		this.viewFrame=container;		
    this.setBackground(new Color(SystemColor.control.getRGB()));    
		pakFileTree=new Tree(MD3View.class.getResource("openIcon.gif"), MD3View.class.getResource("closeIcon.gif"), 5);
		pakFileTree.setShowNodeIcons(true);
  	this.add(pakFileTree);

		//load icons
		
    MediaTracker tracker=new MediaTracker(this);
    tracker.addImage((unknownIcon=Toolkit.getDefaultToolkit().getImage(MD3View.class.getResource("unknownIcon.gif"))), 1);
    tracker.addImage((skinIcon=Toolkit.getDefaultToolkit().getImage(MD3View.class.getResource("skinIcon.gif"))), 2);    
    tracker.addImage((textureIcon=Toolkit.getDefaultToolkit().getImage(MD3View.class.getResource("textureIcon.gif"))), 3);
    tracker.addImage((textIcon=Toolkit.getDefaultToolkit().getImage(MD3View.class.getResource("textIcon.gif"))), 4);
    tracker.addImage((shaderIcon=Toolkit.getDefaultToolkit().getImage(MD3View.class.getResource("shaderIcon.gif"))), 5);
    try { tracker.waitForAll(); } catch (InterruptedException e) {}    
		
    //create popup menu  	
  	
  	final PopupMenu popupMenu=new PopupMenu();
  	final MenuItem openModelItem=new MenuItem("Open Model");
  	popupMenu.add(openModelItem);
  	attachToMenu=new Menu("Attach To");
  	popupMenu.add(attachToMenu);
  	popupMenu.addSeparator();
  	final MenuItem applySkinItem=new MenuItem("Apply Skin");
  	popupMenu.add(applySkinItem);
  	final MenuItem applyTextureItem=new MenuItem("Apply Texture");
  	popupMenu.add(applyTextureItem);
  	applyToMeshMenu=new Menu("Apply Texture To Mesh");
  	popupMenu.add(applyToMeshMenu);  	
  	final MenuItem viewTextureItem=new MenuItem("View Texture");
  	popupMenu.add(viewTextureItem);  	
  	popupMenu.addSeparator();
  	final MenuItem viewTextItem=new MenuItem("View Text");
  	popupMenu.add(viewTextItem);
  	popupMenu.addSeparator();
  	MenuItem extractFileItem=new MenuItem("Extract File...");
  	popupMenu.add(extractFileItem);
  	MenuItem filePropertiesItem=new MenuItem("File Properties");
  	popupMenu.add(filePropertiesItem);

    //add listeners for popup menu items
		
		openModelItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				openModel((ZipEntry)pakFileTree.getSelectedNode().getContent());
				viewFrame.md3canvas.sDisplay();	
			}			  				
		});
		
		applySkinItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				applySkin((ZipEntry)pakFileTree.getSelectedNode().getContent());	
				viewFrame.md3canvas.sDisplay();	
			}			  				
		});
		
		applyTextureItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				applyTexture((ZipEntry)pakFileTree.getSelectedNode().getContent());	
				viewFrame.md3canvas.sDisplay();	
			}			  				
		});
		
		viewTextureItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				viewTexture((ZipEntry)pakFileTree.getSelectedNode().getContent());	
				viewFrame.md3canvas.sDisplay();	
			}			  				
		});

		viewTextItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				viewText((ZipEntry)pakFileTree.getSelectedNode().getContent());
			}			  				
		});

		extractFileItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
	      String filename=viewFrame.showSaveDialog(pakFileTree.getSelectedNode().getName());        
        //save to the requested file
        if (filename!=null) try {
        	ZipEntry entry=(ZipEntry)pakFileTree.getSelectedNode().getContent();
		
          FileOutputStream fout=new FileOutputStream(filename);
          InputStream pakInputStream=getInputStream(entry);	
          
          
          ByteUtils.copyAllBytes(pakInputStream, fout);
				
				  pakInputStream.close();
          fout.close();
        } catch (IOException ex) {
          viewFrame.showExceptionDialog(ex.getMessage());
        }
			}			  				
		});
		
		filePropertiesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ZipEntry ze=(ZipEntry)pakFileTree.getSelectedNode().getContent();
				
				new MessageDialog(viewFrame,
					                pakFileTree.getSelectedNode().getName() + " File Properties",
					                false, null,
					                new String[] { "Complete File Name: " + ze.getName(),
					                	             "",
					                	             "File Size: " + ze.getSize() + " bytes",
					                	             "Compressed Size: " + ze.getCompressedSize() + " bytes",
					                	             "Modified: " + new Date(ze.getTime()), 
					                	             "CRC: " + Long.toHexString(ze.getCrc())
					                	           },
					                true).setVisible(true);
			}			  				
		});
    
		pakFileTree.add(popupMenu);		
		
		//setup popup menu listener for tree
		pakFileTree.addPopupMenuListener(new PopupMenuListener() {		  		
	  	public void popupMenu(PopupMenuEvent e) {
	  		TreeNode node=pakFileTree.getSelectedNode();
	  		String nodeName=node.getName().toUpperCase();
	  		
	  		if (!node.getDirectory()) {
		  		//prepare popup menu
		  		if (nodeName.endsWith(".MD3")) {
		  		  if (viewFrame.md3canvas.getModel()!=null)
		  		  	viewFrame.md3canvas.getModel().accept(new MD3ModelUpdateTagMenuVisitor(attachToMenu, "pak://" + ((ZipEntry)node.getContent()).getName()));
		  		  else
		  		  	attachToMenu.removeAll();
	  				openModelItem.setEnabled(true);
		  		  attachToMenu.setEnabled(true);
	  			} else {
	  				openModelItem.setEnabled(false);
		  		  attachToMenu.setEnabled(false);
	  			}
	  			
		  		applySkinItem.setEnabled(nodeName.endsWith(".SKIN"));
		  		
		  		if (nodeName.endsWith(".TGA") || nodeName.endsWith(".JPG")) {
		  		  if (viewFrame.md3canvas.getModel()!=null)
		  		  	viewFrame.md3canvas.getModel().accept(new MD3ModelUpdateMeshMenuVisitor(applyToMeshMenu, "pak://" + ((ZipEntry)node.getContent()).getName()));
		  		  else
		  		  	applyToMeshMenu.removeAll();
		  			applyTextureItem.setEnabled(true);
		  			applyToMeshMenu.setEnabled(true);		  		  		  			
		  		}
		  		else {
		  			applyTextureItem.setEnabled(false);
		  			applyToMeshMenu.setEnabled(false);
		  		}
		  		
		  		viewTextureItem.setEnabled(applyTextureItem.isEnabled());
		  		
		  		viewTextItem.setEnabled(nodeName.endsWith(".SKIN") || nodeName.endsWith(".CFG") ||
		  			                      nodeName.endsWith(".TXT") || nodeName.endsWith(".CONFIG") ||
		  			                      nodeName.endsWith(".H") || nodeName.endsWith(".C") ||
		  			                      nodeName.endsWith(".SHADER"));
		  			
		  		popupMenu.show(pakFileTree, e.getX(), e.getY());
	  		}
	  	}
	  });	  
	}
	
	//utility method to open a model in the appliction
	private void openModel(ZipEntry entry) {
		try {
			InputStream in=getInputStream(entry);
			viewFrame.openModel("pak://" + entry.getName(), in);
			in.close();
    }
    catch (java.io.IOException ex) {
      viewFrame.showExceptionDialog(ex.getMessage());
    }        
	}
	
	//utility method to apply a skin to the model of the appliction
	private void applySkin(ZipEntry entry) {
		try {
			InputStream in=getInputStream(entry);
			viewFrame.applySkin("pak://" + entry.getName(), in);
			in.close();
    }
    catch (java.io.IOException ex) {
      viewFrame.showExceptionDialog(ex.getMessage());
    }
	}
	
	//utility method to apply a texture to the model of the appliction
	private void applyTexture(ZipEntry entry) {
		try {
			InputStream in=getInputStream(entry);
			viewFrame.applyTexture("", "pak://" + entry.getName(), in);
			in.close();
    }
    catch (java.io.IOException ex) {
      viewFrame.showExceptionDialog(ex.getMessage());
    }
	}
	
	//utility method to display a texture in the appliction
	private void viewTexture(ZipEntry entry) {
		try {
			InputStream in=getInputStream(entry);
			viewFrame.viewTexture("", "pak://" + entry.getName(), in);
			in.close();
    }
    catch (java.io.IOException ex) {
      viewFrame.showExceptionDialog(ex.getMessage());
    }
	}	
	
	//utility method to view text using the appliction text viewer
	private void viewText(ZipEntry entry) {
		try {
			InputStream in=getInputStream(entry);
			MD3View.instance().showTextViewer(entry.getName(), in);
			in.close();
    } catch (IOException ex) {
      viewFrame.showExceptionDialog(ex.getMessage());
    }
	}
	
  /**
   * <p>Add a node specified by the given path to the given tree. Intermediate
   * nodes will be created as required. The most deeply nested node that is
   * created will be returned.
   */	
	private TreeNode addUsingPath(TreeNode t, String path) {		
		TreeNode child=null;
		
		int i=path.indexOf('/');
		if (i!=-1) { 
			//this is an internal node			
		  String dir=path.substring(0, i);
		  
		  child=t.childByName(dir);
  		if (child==null) {
	  		child=new TreeNode(t, dir);
	  		child.setDirectory(true);
				t.addChild(child);
			}
			
			if (i<path.length()-1) {
				path=path.substring(i+1);

				return addUsingPath(child, path);
			}
  	}
		else if (t.childByName(path)==null) {	//this is a leaf node
			child=new TreeNode(t, path);
			t.addChild(child);
	  }
		
		return child;						
	}

  /**
   * <p>Open the given pak file and display its contents in the widget.
   */	
	public void openPakFile(String filename) throws IOException {
		closePakFile();
		zipFile=new ZipFile(filename);
		
		basePakFileOpenPath="";
		
		//create root
		TreeNode root=new TreeNode(null, new File(filename.trim()).getName());
		
		//add children
		Enumeration e=zipFile.entries();
		while(e.hasMoreElements()) {
			ZipEntry ze=(ZipEntry)e.nextElement();
			
			TreeNode newNode=addUsingPath(root, ze.getName().toLowerCase());
			newNode.setContent(ze);

      //configure node: register icon & listener
			String zeName=ze.getName().toUpperCase();
			if (zeName.endsWith(".MD3")) {
				newNode.setIcon(viewFrame.getIconImage());
				
				newNode.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						openModel((ZipEntry)((TreeNode)e.getSource()).getContent());
						viewFrame.md3canvas.sDisplay();
					}			  				
				});
			}
			else if(zeName.endsWith(".SKIN")) {
				newNode.setIcon(skinIcon);				
				
				newNode.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						applySkin((ZipEntry)((TreeNode)e.getSource()).getContent());
						viewFrame.md3canvas.sDisplay();
					}			  				
				});				
			}
			else if(zeName.endsWith(".TGA") || zeName.endsWith(".JPG")) {
				newNode.setIcon(textureIcon);				
				
				newNode.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						viewTexture((ZipEntry)((TreeNode)e.getSource()).getContent());
						viewFrame.md3canvas.sDisplay();
					}			  				
				});				
			}
			else if(zeName.endsWith(".CFG") || zeName.endsWith(".CONFIG") ||
				      zeName.endsWith(".TXT") ||
				      zeName.endsWith(".C") || zeName.endsWith(".H")) {
				newNode.setIcon(textIcon);
				
				newNode.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						viewText((ZipEntry)((TreeNode)e.getSource()).getContent());
					}			  				
				});				
			}
			else if(zeName.endsWith(".SHADER")) {
				newNode.setIcon(shaderIcon);

        newNode.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            viewText((ZipEntry)((TreeNode)e.getSource()).getContent());
          }                
        });        
      }
			else //unknown file type
				newNode.setIcon(unknownIcon);
		}
					
		root.setExpanded(true);
		pakFileTree.setRoot(root);		
		pakFileTree.redraw();
	}
	
	/**
	 * <p>Close the current pak file of the control.
	 */
	public void closePakFile() throws IOException {
		pakFileTree.setRoot(null);
		pakFileTree.redraw();
		if (zipFile!=null)
			zipFile.close();
		zipFile=null;
	}
	
	/**
	 * <p>Is there currently a pak file open in the control?
	 */
	public boolean hasOpenPakFile() {
		return zipFile!=null;
	}
	
	/**
	 * <p>Return a ZipEntry from the pak file for the given name, or null
	 * if name is not found.
	 */
	public ZipEntry getPakEntry(String name) {
		return zipFile.getEntry(name);
	}
	
	/**
	 * <p>Return an iterator over all entries of the open pak file.
	 */
	public Enumeration getPakEntries() {
		return zipFile.entries();
	}
	
	/**
	 * <p>Return an input stream for the given pak file entry.
	 */
	public InputStream getInputStream(ZipEntry entry) throws IOException {
		//update basePakFileOpenPath
		int i;
		if ((i=entry.getName().lastIndexOf('/')) != -1)
			basePakFileOpenPath=entry.getName().substring(0, i+1);
		else
			basePakFileOpenPath="";
		
		return zipFile.getInputStream(entry);
	}
	
	public void finalize() throws Exception {
		closePakFile();
	}	
}