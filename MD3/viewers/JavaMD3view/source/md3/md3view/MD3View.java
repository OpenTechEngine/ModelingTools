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
import java.net.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*; //for JColorShooser

import widgets.awt.*;

import md3.md3model.*;
import md3.md3view.glmodel.*;
import md3.md3view.visitor.*;
import md3.util.*;

/**
 * <pre>
 * Java MD3 Model Viewer - A Java based Quake 3 model viewer.
 * Copyright (C) 1999  Erwin 'KLR8' Vervaet
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </pre>
 *
 * <p>Java MD3 Model Viewer application: A windowed program that uses
 * OpenGL to display a Quake 3 MD3 model file on the screen.
 *
 * <p>It uses Jausoft's GL4Java package. GL4Java maps the OpenGL API
 * to Java and implements window handle functions (native and java).
 *
 * <p>Some code of the Java MD3 Model Viewer was based on the PC MD3View 1.51
 * C/C++ source code, written by Matthew 'pagan' Baranowski & Sander
 * 'FireStorm' van Rossen. They did gread work reverse engineering the MD3
 * file format and building an OpenGL viewer for it. This program started as
 * a conversion of their C/C++ program to Java. However, the code was
 * completely reengineered to make it more object oriented and to expand the
 * feature set.
 *
 * <p>You can find the original MD3View at the following location:
 *   <a href="http://q3arena.net/mentalvortex/md3view/">Mental Vortex</a>.
 *
 * <p>Jausoft's GL4Java can be found on the following site:
 *   <a href="http://www.jausoft.com/">Jausoft Home-Page</a>.
 *
 * <p>Thanks also to Koen Hendrikx and Kris Cardinaels for help in getting
 * the code to work when I was starting this project! 
 *
 * <p>This class is really a singleton: the sole top level frame of the
 * application.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3View extends Frame {
  private static final String FRAME_TITLE = "Java MD3 Model Viewer 1.2.3";
  
  private static MD3View thisInstance; //the sole instance
  	
  /**
   * <p>Data source from which last file was opened.
   */
  private MD3ViewDataSource currentDataSource=MD3ViewDataSource.FILE_SYSTEM;
  
  /**
   * <p>Base open path of the application. This is the directory from which
   * the last file was opened.
   */
  private String baseOpenPath;
  
  /**
   * <p>Base save path of the application. This is the directory to which the
   * last file was saved.
   */
  private String baseSavePath;
  
  //gui resources owned by the viewer
  private FileDialog openDialog;
  private FileDialog saveDialog;
  private SplashScreen splashScreen;
  private MD3ViewTextViewer textViewer;
  private MD3ViewAnimationControl animationControl;  
  private MD3ViewPakFileControl pakFileControl;
  private MD3ViewOptions optionsControl;  
  private Menu attachFileMenu, detachPartMenu, applyTextureToMeshMenu;
  
  /**
   * <p>The OpenGL canvas to render on.
   */
  public MD3ViewGLCanvas md3canvas;  
  
  /**
   * <p>Private constructor, cannot instantiate directely: singleton class!
   */
  private MD3View(int width, int height) {
    super(FRAME_TITLE);
    
    //initialize and register our own abstract factory
    MD3ModelFactory.setFactory(new MD3ViewGLModelFactory());
    
    //load icon
    this.setIconImage(Toolkit.getDefaultToolkit().getImage(MD3View.class.getResource("MD3ViewIcon.gif")));
    
    //create dialogboxes
    openDialog=new FileDialog(this, "Open", FileDialog.LOAD);
    saveDialog=new FileDialog(this, "Save", FileDialog.SAVE);
    
    //create menu bar
    MenuBar mb=new MenuBar();
    this.setMenuBar(mb);
    
    //file menu
    Menu fileMenu=new Menu("File");
    mb.add(fileMenu);
    MenuItem openModelItem=new MenuItem("Open Model...", new MenuShortcut((int)'o'));
    fileMenu.add(openModelItem);    
    MenuItem saveModelItem=new MenuItem("Save Model...", new MenuShortcut((int)'s'));
    fileMenu.add(saveModelItem);
// -- remove when ready
saveModelItem.setEnabled(false);
// --
    MenuItem closeModelItem=new MenuItem("Close Model");
    fileMenu.add(closeModelItem);
    fileMenu.addSeparator();
    Menu importMenu=new Menu("Import");
    MenuItem import3DSItem=new MenuItem("Import 3DS...");
// -- remove when ready
import3DSItem.setEnabled(false);
// --
    importMenu.add(import3DSItem);
    fileMenu.add(importMenu);    
    Menu exportMenu=new Menu("Export");
    MenuItem exportRawItem=new MenuItem("Export Raw...");
    exportMenu.add(exportRawItem);
    MenuItem exportVRML97Item=new MenuItem("Export VRML 97...");
    exportMenu.add(exportVRML97Item);
    MenuItem exportPlgItem=new MenuItem("Export Plg...");
    exportMenu.add(exportPlgItem);
    MenuItem exportDXFItem=new MenuItem("Export Autocad DXF...");
    exportMenu.add(exportDXFItem);
    MenuItem exportObjItem=new MenuItem("Export Wavefront Obj...");
    exportMenu.add(exportObjItem);
    MenuItem exportTexCoordMapItem=new MenuItem("Export Texture Coordinate Maps...");
    exportMenu.add(exportTexCoordMapItem);    
    fileMenu.add(exportMenu);    
    fileMenu.addSeparator();
    MenuItem serializeItem=new MenuItem("Serialize Model...");
    fileMenu.add(serializeItem);
    MenuItem deserializeItem=new MenuItem("Load Serialized Model...");
    fileMenu.add(deserializeItem);
    fileMenu.addSeparator();
    MenuItem openPakItem=new MenuItem("Open Pak...", new MenuShortcut((int)'p'));
    fileMenu.add(openPakItem);
    MenuItem closePakItem=new MenuItem("Close Pak");
    fileMenu.add(closePakItem);
    fileMenu.addSeparator();
    MenuItem saveScreenshotItem=new MenuItem("Save Screenshot...");
    fileMenu.add(saveScreenshotItem);
    fileMenu.addSeparator();
    MenuItem optionsItem=new MenuItem("Options...");
    fileMenu.add(optionsItem);
    fileMenu.addSeparator();
    MenuItem exitItem=new MenuItem("Exit");
    fileMenu.add(exitItem);
    
    //view menu
    Menu viewMenu=new Menu("View");
    mb.add(viewMenu);
    //add render modes to view menu
    Iterator it=MD3ViewGLCanvasRenderMode.renderModes().iterator();
    while (it.hasNext()) {
      final MD3ViewGLCanvasRenderMode rm=(MD3ViewGLCanvasRenderMode)it.next();
      
      //setup menu item for render mode
      MenuItem rmItem=new MenuItem(rm.toString());
      rmItem.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          md3canvas.setRenderMode(rm);
          md3canvas.sDisplay();
        }
      });
      
      //add render mode to view menu
      viewMenu.add(rmItem);
    }    
    viewMenu.addSeparator();
    MenuItem resetViewItem=new MenuItem("Reset View", new MenuShortcut((int)'r'));
    viewMenu.add(resetViewItem);
    viewMenu.addSeparator();
    CheckboxMenuItem quickManipulationsItem=new CheckboxMenuItem("Quick Manipulations",false);
    viewMenu.add(quickManipulationsItem);
    CheckboxMenuItem showBoneFrameBoxItem=new CheckboxMenuItem("Show Bone Frame Box", false);
    viewMenu.add(showBoneFrameBoxItem);
    CheckboxMenuItem showVertexNormalsItem=new CheckboxMenuItem("Show Vertex Normals", false);
    viewMenu.add(showVertexNormalsItem);    
    viewMenu.addSeparator();
    MenuItem bgColorItem=new MenuItem("Set Background Color...");
    viewMenu.add(bgColorItem);
    
    //model menu
    Menu modelMenu=new Menu("Model");
    mb.add(modelMenu);
    MenuItem propertiesItem=new MenuItem("Properties");
    modelMenu.add(propertiesItem);
    modelMenu.addSeparator();
    attachFileMenu=new Menu("Attach Model");
    modelMenu.add(attachFileMenu);
    MenuItem applySkinItem=new MenuItem("Apply Skin...");
    modelMenu.add(applySkinItem);
    MenuItem applyTextureItem=new MenuItem("Apply Texture...");
    modelMenu.add(applyTextureItem);
    applyTextureToMeshMenu=new Menu("Apply Texture To Mesh");
    modelMenu.add(applyTextureToMeshMenu);    
    modelMenu.addSeparator();
    detachPartMenu=new Menu("Detach Part");
    modelMenu.add(detachPartMenu);
    modelMenu.addSeparator();
    MenuItem refreshTexturesItem=new MenuItem("Refresh Textures", new MenuShortcut((int)'t'));
    modelMenu.add(refreshTexturesItem);    
    
    //help menu
    Menu helpMenu=new Menu("Help");
    mb.add(helpMenu);
    MenuItem renderInfoItem=new MenuItem("Render Info...");
    helpMenu.add(renderInfoItem);
    MenuItem licenseItem=new MenuItem("License...");
    helpMenu.add(licenseItem);
    helpMenu.addSeparator();
    MenuItem aboutItem=new MenuItem("About...");
    helpMenu.add(aboutItem);
    
    //register listeners for menus
    
    openModelItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String filename=showOpenDialog("*.md3");        
        //open the requested file
        if (filename!=null) try {
        	InputStream fin=new FileInputStream(filename);
          openModel(filename, fin);
          fin.close();
        }
        catch (java.io.IOException ex) {
          showExceptionDialog(ex.getMessage());
        }
        
        md3canvas.sDisplay();
      }
    });
    
    saveModelItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (md3canvas.getModel()!=null) {
	        String filename=showSaveDialog(FilenameUtils.getShortFilename(md3canvas.getModel().loadFilename) + ".md3");        
	        //save to the requested file
	        if (filename!=null) try {
	          FileOutputStream fout=new FileOutputStream(filename);
	          md3canvas.getModel().accept(new MD3GLModelExportMD3Visitor(fout));
	          fout.close();
	        } catch (IOException ex) {
	          showExceptionDialog(ex.getMessage());
	        }
      	}
        
        md3canvas.sDisplay();
      }
    });

    closeModelItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	closeModel();
        md3canvas.sDisplay();
      }
    });
    
    import3DSItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String filename=showOpenDialog("*.3ds");
        //open the requested file
        if (filename!=null) try {
        	InputStream fin=new FileInputStream(filename);
          load3DS(filename, fin);
          fin.close();
        }
        catch (java.io.IOException ex) {
          showExceptionDialog(ex.getMessage());
        }
        
        md3canvas.sDisplay();
      }
    });

    exportRawItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (md3canvas.getModel()!=null) {
	        String filename=showSaveDialog(FilenameUtils.getShortFilename(md3canvas.getModel().loadFilename) + ".raw");        
	        //save to the requested file
	        if (filename!=null) try {
	          FileOutputStream fout=new FileOutputStream(filename);
	          md3canvas.getModel().accept(new MD3GLModelExportRawVisitor(fout));
	          fout.close();
	        } catch (IOException ex) {
	          showExceptionDialog(ex.getMessage());
	        }
      	}
        
        md3canvas.sDisplay();
      }
    });
    
    exportVRML97Item.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (md3canvas.getModel()!=null) {
	        String filename=showSaveDialog(FilenameUtils.getShortFilename(md3canvas.getModel().loadFilename) + ".wrl");        
	        //save to the requested file
	        if (filename!=null) try {
	          FileOutputStream fout=new FileOutputStream(filename);
	          md3canvas.getModel().accept(new MD3GLModelExportVRML97Visitor(fout));
	          if (MD3ViewOptions.autoExportTextures)
	          	md3canvas.getModel().accept(new MD3ModelExportTexturesVisitor(baseSavePath));
	          fout.close();
	        } catch (IOException ex) {
	          showExceptionDialog(ex.getMessage());
	        }
      	}
        
        md3canvas.sDisplay();
      }
    });

    exportPlgItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (md3canvas.getModel()!=null) {
	        String filename=showSaveDialog(FilenameUtils.getShortFilename(md3canvas.getModel().loadFilename) + ".plg");        
	        //save to the requested file
	        if (filename!=null) try {
	          FileOutputStream fout=new FileOutputStream(filename);
	          md3canvas.getModel().accept(new MD3GLModelExportPlgVisitor(fout));
	          fout.close();
	        } catch (IOException ex) {
	          showExceptionDialog(ex.getMessage());
	        }
      	}
        
        md3canvas.sDisplay();
      }
    });

    exportDXFItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (md3canvas.getModel()!=null) {
	        String filename=showSaveDialog(FilenameUtils.getShortFilename(md3canvas.getModel().loadFilename) + ".dxf");        
	        //save to the requested file
	        if (filename!=null) try {
	          FileOutputStream fout=new FileOutputStream(filename);
            MD3GLModelExportDXFVisitor v=new MD3GLModelExportDXFVisitor();
	          md3canvas.getModel().accept(v);
            v.writeOut(fout);
	          fout.close();
	        } catch (IOException ex) {
	          showExceptionDialog(ex.getMessage());
	        }
      	}
        
        md3canvas.sDisplay();
      }
    });

    exportObjItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (md3canvas.getModel()!=null) {
	        String filename=showSaveDialog(FilenameUtils.getShortFilename(md3canvas.getModel().loadFilename) + ".obj");
	        //save to the requested file
	        if (filename!=null) try {
	          MD3GLModelExportObjVisitor v=new MD3GLModelExportObjVisitor();
	          md3canvas.getModel().accept(v);

            FileOutputStream objOut=new FileOutputStream(filename);
            FileOutputStream mtlOut=new FileOutputStream(baseSavePath + "\\" + FilenameUtils.getShortFilename(filename) + ".mtl");            
	          v.writeOut(objOut, FilenameUtils.getShortFilename(filename) + ".mtl", mtlOut);
            if (MD3ViewOptions.autoExportTextures)
              md3canvas.getModel().accept(new MD3ModelExportTexturesVisitor(baseSavePath));
	          objOut.close();
            mtlOut.close();
	        } catch (IOException ex) {
	          showExceptionDialog(ex.getMessage());
	        }
      	}
        
        md3canvas.sDisplay();
      }
    });

    exportTexCoordMapItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (md3canvas.getModel()!=null) {
          MD3ModelExportTextureCoordinateMapsVisitor v=new MD3ModelExportTextureCoordinateMapsVisitor(MD3View.this);
          md3canvas.getModel().accept(v);
          v.writeOut();
        }
        
        md3canvas.sDisplay();
      }
    });
    
    serializeItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (md3canvas.getModel()!=null) {
        	String filename=showSaveDialog(FilenameUtils.getShortFilename(md3canvas.getModel().loadFilename) + ".ser");
        	//save to the specified file
	        if (filename!=null) try {
	          ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(filename));
	          out.writeObject(md3canvas.getModel());
	          out.close();
	        } catch (IOException ex) {
	          showExceptionDialog(ex.getMessage());
	        }
      	}
        
        md3canvas.sDisplay();
      }
    });
      
    deserializeItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {        
        String filename=showOpenDialog("*.ser");
        //open the requested file
        if (filename!=null) try {
    			ObjectInputStream in=new ObjectInputStream(new FileInputStream(filename));
    			MD3Model model=(MD3Model)in.readObject();
    			setModel(model);
    			in.close();
        } catch (Exception ex) {
          showExceptionDialog(ex.getMessage());
        }
        
        md3canvas.sDisplay();
      }
    });
    
    openPakItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String filename=showOpenDialog("*.pk3");
        //open the requested file
        if (filename!=null) try {
        	openPakFile(filename);
        } catch (Exception ex) {
          showExceptionDialog(ex.getMessage());
        }
        
        md3canvas.sDisplay();
      }
    });
    
    closePakItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	try {
      		pakFileControl.closePakFile();
      	} catch (Exception ex) {
          showExceptionDialog(ex.getMessage());
        }      		
      }
    });

    saveScreenshotItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (md3canvas.getModel()!=null) {
	      	String filename=showSaveDialog(FilenameUtils.getShortFilename(md3canvas.getModel().loadFilename) + ".tga");
	      	//dump to the specified file
	      	if (filename!=null) try {	      		
	        	md3canvas.dumpCanvasToTGA(filename);
	      	} catch (IOException ex) {
	          showExceptionDialog(ex.getMessage());
	      	}
      	}
        
        md3canvas.sDisplay();
      }
    });
    
    optionsItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (optionsControl==null)
					optionsControl=new MD3ViewOptions(MD3View.this);
      		
      	optionsControl.setVisible(true);
      }
    });
    
    resetViewItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        md3canvas.resetManipulations();
        md3canvas.sDisplay();
      }
    });
    
    quickManipulationsItem.addItemListener( new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        md3canvas.quickManipulations=e.getStateChange()==ItemEvent.SELECTED;
      }
    });
        
    showBoneFrameBoxItem.addItemListener( new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        md3canvas.showBoneFrame=e.getStateChange()==ItemEvent.SELECTED;
        md3canvas.sDisplay();        
      }
    });

    showVertexNormalsItem.addItemListener( new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        md3canvas.showVertexNormals=e.getStateChange()==ItemEvent.SELECTED;
        md3canvas.sDisplay();        
      }
    });
    
    bgColorItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Color bg=showColorDialog();
        
        if (bg!=null) {
          md3canvas.setBackgroundColor(bg);
          md3canvas.sDisplay();
        }
      }
    });

    propertiesItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (md3canvas.getModel()!=null) {
          MD3ModelGetPropertiesVisitor prop=new MD3ModelGetPropertiesVisitor();
          md3canvas.getModel().accept(prop);
          new MessageDialog(MD3View.this, "Model Properties", false, null, prop.toText(), true).setVisible(true);        
        }
        else if (md3canvas.getTexture()!=null) {
          MD3Texture tex=md3canvas.getTexture();
          String[] text={ tex.loadFilename,
                          "     Dimensions: " + tex.width + "x" + tex.height,
                          "     Size: " + tex.data.length + " bytes" };
          new MessageDialog(MD3View.this, "Texture Properties", false, null, text, true).setVisible(true);
        }
      }
    });
        
    applySkinItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String filename=showOpenDialog("*.skin");
                
        if (filename!=null) try {
        	InputStream fin=new FileInputStream(filename);
        	applySkin(filename, fin);
        	fin.close();
			  } catch (Exception ex) {
			    showExceptionDialog(ex.getMessage());
        }
        
        md3canvas.sDisplay();
      }
    });
    
    applyTextureItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String filename=showOpenDialog("*.tga;*.jpg");        

        if (filename!=null) try {
        	InputStream fin=new FileInputStream(filename);
        	applyTexture("", filename, fin);
        	fin.close();
			  } catch (Exception ex) {
			    showExceptionDialog(ex.getMessage());
        }
        
        md3canvas.sDisplay();
      }
    });
    
    refreshTexturesItem.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (md3canvas.getModel()!=null) {
        	md3canvas.getModel().accept(new MD3GLModelRefreshTextureVisitor());
        	//upload fresh textures to OpenGL
          md3canvas.getModel().accept(new MD3GLModelUploadTextureVisitor(md3canvas));
        }
        md3canvas.sDisplay();
      }
    });
    
    renderInfoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          ByteArrayInputStream in=new ByteArrayInputStream(md3canvas.getRenderInfo().getBytes());
          showTextViewer("OpenGL Render Information", in);
        } catch (IOException ex) {
          showExceptionDialog(ex.getMessage()); //this shouldn't happen
        }        
      }      
    });

    licenseItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          InputStream in=MD3View.class.getResource("gpl.txt").openStream();
          showTextViewer("GNU General Public License", in);
          in.close();
        } catch (IOException ex) {
          showExceptionDialog(ex.getMessage());
        }
      }      
    });

    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showSplashScreen();
      }      
    });
    
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        shutdown();
      }
    });
    
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        shutdown();
      }
    });
    
    //setup Swing Look & Feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
      //ignore, should not occur
    }
    
    //setup animation control
    animationControl=new MD3ViewAnimationControl(this);
    
    //setup pak file browsing control
    pakFileControl=new MD3ViewPakFileControl(this);    
        
    //setup control panel
    TabbedPane controlPanel=new TabbedPane(230, height);
    controlPanel.addTab("Animation", animationControl);
    controlPanel.addTab("Pak File", pakFileControl);
    
    //setup OpenGL drawing Canvas
    md3canvas=new MD3ViewGLCanvas(width, height);
    
    //put everything together
    this.setBackground(new Color(SystemColor.control.getRGB()));
    this.setLayout(new BorderLayout(3,0));
    this.add(md3canvas, BorderLayout.CENTER);
    this.add(controlPanel, BorderLayout.EAST);
            
    md3canvas.setInterpolate(false); //interpolation disabled on startup
    animationControl.setEnabled(false); //animation disabled on startup    	    

    this.pack();
  }
  
  /**
   * <p>Convenience method to set a new model as the top level model displayed by
   * the viewer. If the model contains animation frames, this will also enable
   * the animation control panel.
   */
  private void setModel(MD3Model model) {
  	closeModel();
  	
    //set model as top level model of OpenGL canvas
    md3canvas.setModel(model);
    //enable animations if model has animation frames
    animationControl.setEnabled(model.animated());
    //prepare model for display
    prepareModel(model);
  }
  
  /**
   * <p>Prepare a model to be displayed.
   */
  private void prepareModel(MD3Model model) {  	
  	//make sure current animation is active
    if (md3canvas.getAnimation()!=null)
    	model.accept(new MD3GLModelChangeCurrentFrameVisitor(md3canvas.getAnimation(), MD3GLModelChangeCurrentFrameVisitor.REWIND));
    
    //update menus
    model.accept(new MD3ModelUpdateTagMenuVisitor(attachFileMenu));
    model.accept(new MD3ModelUpdateMeshMenuVisitor(applyTextureToMeshMenu));
    model.accept(new MD3ModelUpdateDetachMenuVisitor(detachPartMenu));
    
    //upload texture data
    model.accept(new MD3GLModelUploadTextureVisitor(md3canvas));
  }
  
  /**
   * <p>Convenience method to try to automatically assemble an entire player model.
   */
  private void autoAssemblePlayerModel(MD3Model root) {
 		int tagIndex;
 		String filename="", name="";

    try {
      name=FilenameUtils.getShortFilename(root.loadFilename);    
      if (name.toLowerCase().startsWith("lower") && (tagIndex=root.getTagIndexByName("tag_torso")) != -1) {
        //load torso
        filename=searchForPath("upper" + FilenameUtils.getLODPostfix(name) + ".md3", false);
        root=attachModel(root, tagIndex, filename, getInputStreamForPath(filename));
      }
      
      name=FilenameUtils.getShortFilename(root.loadFilename);    
      if (name.toLowerCase().startsWith("upper") && (tagIndex=root.getTagIndexByName("tag_head")) != -1) {        
        //load head
        filename=searchForPath("head" + FilenameUtils.getLODPostfix(name) + ".md3", false);
        attachModel(root, tagIndex, filename, getInputStreamForPath(filename));
      }
    }
    catch (IOException e) {
      showExceptionDialog(filename + ": " + e.getMessage());
    }
  }

  /**
   * <p>Convenience method to try to automatically load the default skin of a player model,
   */  
  private void autoLoadSkin(String name) {
  	name=FilenameUtils.getShortFilename(name);
  	
  	String partName=null;
  	if (name.toLowerCase().startsWith("lower"))
  		partName="lower";
  	else if (name.toLowerCase().startsWith("upper"))
      partName="upper";
  	else if (name.toLowerCase().startsWith("head"))
      partName="head";
	
	  if (partName!=null) try {
	  	String defaultSkinName=searchForPath(partName + "_default.skin", false);
    	InputStream in=getInputStreamForPath(defaultSkinName);
    	applySkin(defaultSkinName, in);
    	in.close();
	  } catch (IOException e) {
      if (MD3ViewOptions.warningOnTexLoad)  
        showExceptionDialog(name + ": " + e.getMessage());
    }
  }

  /**
   * <p>If the specified name starts with a "pak://" identifier, the data
   * source will become PAK_FILE, otherwise it will become FILE_SYSTEM.
   */
  protected void updateCurrentDataSource(String name) {
   	//update current data source
 	  currentDataSource=name.toLowerCase().startsWith("pak://")?
                      MD3ViewDataSource.PAK_FILE:MD3ViewDataSource.FILE_SYSTEM;
  }
     
  /**
   * <p>Show the Open dialogbox and return the name of the selected file,
   * once the dialog is dismissed.
   *
   * @param fileMask The file selection mask to use (e.g. "*.md3").
   * @return The full pathname of the selected file, or null if no file was
   *         selected.
   */
  public String showOpenDialog(String fileMask) {
    //pop up file open dialogbox to get filename
    openDialog.setFile(fileMask);
    openDialog.setDirectory(baseOpenPath);
    openDialog.setVisible(true);
    if (openDialog.getDirectory()==null || openDialog.getFile()==null)
      return null;
    else {
      baseOpenPath=openDialog.getDirectory();
      return openDialog.getDirectory() + openDialog.getFile();
    }
  }
  
  /**
   * <p>Show the Save dialogbox and return the name of the selected file,
   * once the dialog is dismissed.
   *
   * @param defaultFile The default name of the file to save to.
   * @return The full pathname of the selected file, or null if no file was
   *         selected.
   */
  public String showSaveDialog(String defaultFile) {
  	saveDialog.setFile(defaultFile);
    saveDialog.setDirectory(baseSavePath);
    saveDialog.setVisible(true);
    if (saveDialog.getDirectory()==null || saveDialog.getFile()==null)
      return null;
    else {
      baseSavePath=saveDialog.getDirectory();
      return saveDialog.getDirectory() + saveDialog.getFile();
    }
  }
 
  /**
   * <p>Show a Color selection dialog and return the selected color. If no color is
   * selected or the dialog is cancelled, null is returned.
   */
  public Color showColorDialog() {
    return JColorChooser.showDialog(this, "Set Background Color", md3canvas.getBackgroundColor());    
  }

  /**
   * <p>Pops up a message dialog with a specified Error message.
   *
   * @param msg The error message that should be shown.
   */
  public void showExceptionDialog(String msg) {
    MessageDialog exDialog=new MessageDialog(this, "Exception", true, null, new String[] { msg }, true);
    exDialog.setVisible(true);
  }
  
  /**
   * <p>Display the splash screen of the application.
   */  
  public void showSplashScreen() {
    if (splashScreen==null)
      splashScreen=new SplashScreen(this, MD3View.class.getResource("MD3ViewSplashScreen.jpg"));
      
    splashScreen.setVisible(true);
  }
  
  /**
   * <p>Pop up the text file viewer to display a textfile with the given name, that's read
   * from the given input stream.
   */
  public void showTextViewer(String name, InputStream textIn) throws IOException {
  	if (textViewer==null) {
  		textViewer=new MD3ViewTextViewer();
      textViewer.setLocation(this.getLocation().x + this.getWidth()/2 - textViewer.getWidth()/2,
                             this.getLocation().y + this.getHeight()/2 - textViewer.getHeight()/2);
  	}
  	
  	Reader in=new InputStreamReader(textIn);	
  	StringWriter sout=new StringWriter(textIn.available());
  	
		char[] buf=new char[1024];
	  int len=0;
	  while((len=in.read(buf)) > 0 )
	  	sout.write(buf, 0, len);

		textViewer.setText(sout.toString());
		textViewer.setTitle(name);
		textViewer.setVisible(true);
  }
    
  /**
   * <p>Clients can use this method to resolve a relative path to an absolute path
   * in the context of the data sources (current open directory, open pak file) of
   * the application.
   *
   * <p>If the file is found in a pak file, the resulting path will be prefixed
   * with a "pak://" identifier.
   *
   * <p>If the relative path is actually absolute and denotes an existing file, it
   * is returned as is.
   *   
   * @param relativePath Complete path of file, or relative to (a part of) the base
   *                     open path of the application or relative to (a part of) the
   *                     base pak file open path.
   * @param allDataSources If true, all available data sources will be checked untill
   *                       a match is found. If false, only the current data source
   *                       is be checked.
   * @return Actual position of file.
   */
  public String searchForPath(String relativePath, boolean allDataSources) throws FileNotFoundException {    
  	String res=null;
        
    relativePath=relativePath.replace('\\', '/'); // replace \ with /
  	
  	if (pakFileControl.hasOpenPakFile() &&
        (allDataSources || currentDataSource==MD3ViewDataSource.PAK_FILE)) {
			//search for path in pak file, case insensitive!
			if (relativePath.toLowerCase().startsWith("pak://"))
				relativePath=relativePath.substring(6);
			
			Enumeration e=pakFileControl.getPakEntries();
			while(e.hasMoreElements()) {
	  		ZipEntry entry=(ZipEntry)e.nextElement();

	  		if (entry.getName().toUpperCase().endsWith(relativePath.toUpperCase())) {	  		
		  		String tmpPath=pakFileControl.basePakFileOpenPath;

		  		do {
			  		if (entry.getName().toUpperCase().equals((tmpPath + relativePath).toUpperCase()))
			  			res="pak://" + entry.getName();		  			
			  		else
	    				//tmpPath is either empty or ends with a '/', at index > 0
			  			if (tmpPath.length()==0)
			  				break;
			  			else {
			  				int i;
			  				if ((i=tmpPath.substring(0, tmpPath.lastIndexOf('/')).lastIndexOf('/')) != -1)		  					
			  				  tmpPath=tmpPath.substring(0, i+1);
			  				else
			  					tmpPath="";
				  		}
		  		}	while (res==null);
	  		}
		  }
  	}
  	
  	if (res==null && (allDataSources || currentDataSource==MD3ViewDataSource.FILE_SYSTEM)) {  	
	  	//search for path in file system
	    File f=new File(relativePath);
	        
	    if (!f.exists()) {
	      String tmpPath=baseOpenPath;
	      
	      if (tmpPath != null) do {
	        f=new File(tmpPath + "/" + relativePath);
	        tmpPath=new File(tmpPath).getParent();
	      } while (tmpPath != null && !f.exists());
	      
	      if (tmpPath==null)
	      	res=null; //not found
	    }
	    
	    res=f.getAbsolutePath();
  	}
  	
  	if (res==null)
      throw new FileNotFoundException(relativePath + " not found");
    else
    	return res;
  }  
  
  /**
   * <p>Try to get an input stream for the specified path. The given filename
   * should be absolute (use searchForPath()). If the filename starts with
   * a "pak://" identifier, the file is taken to be in a pak file, otherwise
   * is will be interpreted as a normal filename.
   *
   * @param filename File to look for.
   * @return Input stream for given file.
   */
  public InputStream getInputStreamForPath(String filename) throws IOException {
  	if (filename.toLowerCase().startsWith("pak://")) {
  		if (!pakFileControl.hasOpenPakFile())
  			throw new IOException("can't read " + filename + ": no pak file open");
  			
  		ZipEntry entry=pakFileControl.getPakEntry(filename.substring(6));
  		
  		if (entry==null)
  			throw new FileNotFoundException(filename + " not found");
  		else
  			return pakFileControl.getInputStream(entry);
  	}
  	else
  	  return new FileInputStream(filename);  	  		
  }
  
  /**
   * <p>Factory method that creates an MD3 Model Viewer Frame. It sets up
   * the menu's and the OpenGL Canvas that is used to display the model. By
   * default the window will be 640x480 pixels in size.
   *
   * <p>MD3View is a singleton class, so this will allways return the same instance!
   */
  public static MD3View instance() {
    if (thisInstance==null)
      thisInstance=new MD3View(640,480);
      
    return thisInstance;
  }
  
  /**
   * <p>Shutdown the MD3 Model Viewer application. This will do the
   * necessary cleanup operations and exit the JVM.
   */  
  public void shutdown() {
  	try {
  		closeModel();
	  	pakFileControl.closePakFile();
	    this.setVisible(false);
	    md3canvas.cvsDispose();
	    thisInstance=null;
	    System.exit(0);
  	} catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  
  /**
   * <p>Open a pak (*.pk3) file.
   *
   * @param filename Name of pak file to open.
   */
  public void openPakFile(String filename) throws IOException {
    pakFileControl.openPakFile(filename);
  }
    
  /**
   * <p>Loads an MD3 model from the given input stream and displays it
   * in the viewer window.
   *
   * @param name Name of file from which model is supposedly loaded.
   * @param in Stream from which to read data.
   */     
  public void openModel(String name, InputStream in) throws IOException {
    updateCurrentDataSource(name);
    
    MD3Model model=MD3ModelFactory.getFactory().makeMD3Model(name, in);
    this.setModel(model);
    
    if (MD3ViewOptions.autoAssemblePlayerModels)
    	autoAssemblePlayerModel(model);
    
    if (MD3ViewOptions.autoLoadSkin)
	    autoLoadSkin(name);
  }
 
  /**
   * <p>Loads a 3DS model from the given input stream and displays it
   * in the viewer window.
   *
   * @param name Name of file from which model is supposedly loaded.
   * @param in Stream from which to read data.
   *
	 * @author Donald Gray (dgray@widomaker.com)
	 * @author Erwin Vervaet (klr8@fragland.net)
   */     
  public void load3DS(String name, InputStream in) throws IOException {
    updateCurrentDataSource(name);
    
    MD3Model model=MD3ModelFactory.getFactory().makeMD3Model();
		model.filename=name;
		model.loadFilename=name;
		ThreeDSIO.loadModel(model, in);
    this.setModel(model);
  }
 
  /**
   * <p>Close the model currently displayed by the viewer.
   */
  public void closeModel() {
  	animationControl.setEnabled(false);
  	attachFileMenu.removeAll();  	
  	applyTextureToMeshMenu.removeAll();
  	detachPartMenu.removeAll();
  	md3canvas.setModel(null);
  	System.gc(); //dispose of old model
  }
  
  /**
   * <p>Attach a child model, read from the given input stream, to a parent model at
   * the specified tag position of the parent model. Returns the attached child.
   *
   * @param to Parent model to connect child to.
   * @param tag Tag position of parent to connect child to.
   * @param childName Name of file from which model is supposedly loaded.
   * @param childIn Stream from which to read child model data.
   */
  public MD3Model attachModel(MD3Model to, int tag, String childName, InputStream childIn) throws IOException {
  	updateCurrentDataSource(childName);
  	
    MD3Model child=MD3ModelFactory.getFactory().makeMD3Model(childName, childIn);
    //link child model to its parent model at correct tag 'position'
    to.addLinkedModel(tag, child);

    prepareModel(md3canvas.getModel());  	  	

    if (MD3ViewOptions.autoLoadSkin)
			autoLoadSkin(childName);
			
		return child;
  }
  
  
  /**
   * <p>Detach the given model from the model structure currently displayed by the viewer.
   */
  public void detachModel(MD3Model model) {
  	if (model!=null) {
	  	MD3Model parent=model.getParent();
	  	if (parent==null)
	  		closeModel();
	  	else {
	  		parent.removeLinkedModel(model.getParentTagIndex());
	  		prepareModel(md3canvas.getModel());
	  	}
  	}
  }
  
  /**
   * <p>Apply a skin to the model currently displayed by the viewer.
   *
   * @param name Name of skin file from which data is supposedly loaded.
   * @param in Stream from which to read data.
   */     
  public void applySkin(String name, InputStream in) throws IOException {
  	updateCurrentDataSource(name);

  	if (md3canvas.getModel()!=null) {
   		MD3Skin skin=MD3ModelFactory.getFactory().makeMD3Skin(in);
      md3canvas.getModel().accept(new MD3ModelApplySkinVisitor(skin));
      md3canvas.getModel().accept(new MD3GLModelUploadTextureVisitor(md3canvas));
  	}
  }
  
  /**
   * <p>Apply a texture to the model currently displayed by the viewer.
   *
   * @param name Name of texture as specified in .skin or .md3 file.
   * @param loadFilename Name of file from which texture is supposedly loaded.
   * @param in Stream from which to read data.
   */     
  public void applyTexture(String name, String loadFilename, InputStream in) throws IOException {
  	updateCurrentDataSource(loadFilename);
  	
  	if (md3canvas.getModel()!=null) {
    	MD3Texture tex=MD3ModelFactory.getFactory().makeMD3Texture(name, loadFilename, in);
      md3canvas.getModel().accept(new MD3ModelApplyTextureVisitor(tex));
      md3canvas.getModel().accept(new MD3GLModelUploadTextureVisitor(md3canvas));
  	}
  }
  
  /**
   * <p>Loads an texture from the given input stream and displays it
   * in the viewer window.
   *
   * @param name Name of texture as specified in .skin or .md3 file.
   * @param loadFilename Name of file from which texture is supposedly loaded.
   * @param in Stream from which to read data.
   */     
  public void viewTexture(String name, String loadFilename, InputStream in) throws IOException {
  	updateCurrentDataSource(loadFilename);

    closeModel();
    MD3GLTexture tex=(MD3GLTexture)MD3ModelFactory.getFactory().makeMD3Texture(name, loadFilename, in);
    MD3GLModelUploadTextureVisitor.uploadTextureData(md3canvas, tex);
    md3canvas.setTexture(tex);
  }
  
  /**
   * <p>Starts the MD3 Viewer application. This creates an MD3View Frame and
   * makes it visible.
   *
   * <p>The first command line argument, if present, will be intepreted as
   * the name of an .md3 file that should be opened or as the name of the intial
   * open-file directory.
   */
  public static void main(String[] args) {
  	System.out.println(FRAME_TITLE + ", Copyright (C) 1999 Erwin 'KLR8' Vervaet\n");
    System.out.println("This program comes with ABSOLUTELY NO WARRANTY; This is");
    System.out.println("free software, and you are welcome to redistribute it");
    System.out.println("under certain conditions; see the license for details.");
  		
    MD3View md3v=MD3View.instance();
    md3v.showSplashScreen();
    md3v.setVisible(true);
    
    //interpret first command line argument as filename
    if (args.length>0) try {
    	File f=new File(args[0]).getAbsoluteFile();
      if (f.isDirectory()) //directory
        md3v.baseOpenPath=f.getAbsolutePath();
      else { //file
        md3v.baseOpenPath=f.getParent();
        
        String filename = f.getAbsolutePath();
        if (filename.toLowerCase().endsWith(".md3")) { //MD3 model
          InputStream fin=new FileInputStream(f);
          md3v.openModel(filename, fin);
          fin.close();
        }
        else //pak file
           md3v.openPakFile(filename);
      }
    } catch (IOException ex) {
      md3v.showExceptionDialog(ex.getMessage());
    }    
  }
}