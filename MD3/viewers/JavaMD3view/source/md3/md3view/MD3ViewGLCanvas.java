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

import gl4java.*;
import gl4java.awt.GLAnimCanvas;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import md3.md3model.*;
import md3.md3view.glmodel.*;
import md3.md3view.visitor.*;

import cio.*;

/**
 * <p>OpenGL Canvas with functionality to render MD3 models and MD3GLTextures.
 *
 * <p>It will also react to mouseDragged events. If you drag with
 * the first mouse button and don't hold the shift key, you will
 * rotate the model. Dragging the first mouse button while holding down
 * shift will pan the model in the x-y plane. Dragging with the third
 * mouse button scales the model.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ViewGLCanvas extends GLAnimCanvas {
  private static final float MOUSE_ROT_SCALE = 0.5f;
  private static final float MOUSE_PAN_SCALE = 0.1f;
  private static final float MOUSE_ZPOS_SCALE = 0.1f;
  
  private static final double NEAR_GL_PLANE = 0.1d;
  private static final double FAR_GL_PLANE = 512.0d;
  
  //possible operation modes of the canvas
  private static final int MODEL_MODE = 0; //Model rendering mode. The current MD3 model will be rendered.
  private static final int TEXTURE_MODE = 1; //Texture rendering mode. The current texture will be rendered.
  
  //state data
  private int mode; //operation mode
  private MD3ViewGLCanvasRenderMode rendermode, oldrendermode;  
  private MD3Model model=null; //the top level model that is redered on the canvas  
  private MD3Animation animation=null; //currently played model animation
  private MD3GLTexture texture=null;
  private boolean interpolate;  
  private Color backgroundColor=Color.black;
  
  //keep track of mouse movements
  private int m_x, m_y;
  
  //cache visitors, to prevent frequent reinstantiation
  private MD3GLModelChangeCurrentFrameVisitor nextVisitor, prevVisitor, rewindVisitor;  

  //display related data
  private float rotAngleX, rotAngleY; //rotation angles
  private float xPos, yPos; //pos in x-y plane, pans model
  private float zPos; //pos on Z axis, scales model
  
  /**
   * <p>Does the canvas run in OpenGL debug mode?
   */
  protected static boolean debug=false;

  //exposed rendering state data.
  public boolean quickManipulations,
                 showBoneFrame,
                 showVertexNormals,
                 mipmapping;

  //static initializer
  static {
    GLContext.gljNativeDebug=debug;
    GLContext.gljClassDebug=debug;        
  }
  
  /**
   * <p>Constructor that creates a new MD3 OpenGL Canvas with the specified
   * width and height. It also sets up the required listeners for the 
   * mouseDragged events.
   *
   * <p>By default, animation and quick manipulations are off, interpolation is on,
   * bone frames or not drawn and model are shown.
   *
   * @param width Width of the canvas.
   * @param height Height of the canvas.
   */
  public MD3ViewGLCanvas(int width, int height) {
    super(width,height);
      
    resetManipulations();
    rewindAnimation();
    
    //setup the OpenGL animation canvas
    setVerboseFps(false);    
    setUseRepaint(true);
    setUseFpsSleep(true);    
    
    //default config
    setAnimate(false);
    this.quickManipulations=false;
    this.showBoneFrame=false;
    this.showVertexNormals=false;
    setInterpolate(true);
    mode=MODEL_MODE;

    //do we use mipmapping? (only with GL4Java 2.1.2.1 and later!)
    this.mipmapping=glu.getClassVersion().compareTo("2.1.2.1") >= 0;
        
    //register listeners for user manipulations of model view
    
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        m_x=e.getX();
        m_y=e.getY();
        
        if (quickManipulations)
          oldrendermode=rendermode; //save render mode for quickManipulations function
      }
      
      public void mouseReleased(MouseEvent e) {
        if (quickManipulations)
          setRenderMode(oldrendermode); //revert rendermode in case of quickManipulations
            
        if (MD3ViewGLCanvas.this.isSuspended()) //if anim is running, sDisplay will be called by anim thread
        	sDisplay(); 
      }
    });
    
    this.addMouseMotionListener( new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        int x=e.getX(),
            y=e.getY();

        if (quickManipulations)
          setRenderMode(MD3ViewGLCanvasRenderMode.WIRE_FRAME);
        
        if (e.getModifiers()==InputEvent.BUTTON1_MASK) { //rotate model
          if (model != null && ((x != m_x) || (y != m_y))) {
            rotAngleX+=((float)(y - m_y)) * MOUSE_ROT_SCALE;
            rotAngleY+=((float)(x - m_x)) * MOUSE_ROT_SCALE;
            if (rotAngleX> 360.0f) rotAngleX-=360.0f;
            if (rotAngleX<-360.0f) rotAngleX+=360.0f;
            if (rotAngleY> 360.0f) rotAngleY-=360.0f;
            if (rotAngleY<-360.0f) rotAngleY+=360.0f;
          }
        }
        else if (e.getModifiers()==(InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) { //pan model
          if (model != null && ((x != m_x) || (y != m_y))) {
            xPos+=(((float)(x - m_x))/10.0f) * MOUSE_PAN_SCALE;
            yPos+=-1.0f * (((float)(y - m_y))/10.0f) * MOUSE_PAN_SCALE; //switch y orientation
          }
        }
        else if (e.getModifiers()==InputEvent.BUTTON3_MASK) { //scale model
          if (model != null && y != m_y) {
            zPos+=(((float)(y - m_y))/10.0f) * MOUSE_ZPOS_SCALE;
            if (zPos<-1000.0f) zPos=-1000.0f;
            if (zPos>1000.0f) zPos=1000.0f;
          }
        }
        
        if (MD3ViewGLCanvas.this.isSuspended())
        	sDisplay();
        m_x=x;
        m_y=y;
      }
    });
  }
  
  /**
   * <p>Return a GLFunc object that you can use to draw something on this canvas.
   *
   * @return GLFunc object of this GLCanvas.
   */
  public GLFunc getGL() {
    return this.gl;
  }

  /**
   * <p>Return the canvas's GLUFunc object.
   */  
  public GLUFunc getGLU() {
  	return this.glu;
  }
  
  /**
   * <p>Override needed when using GLCanvas: render 1 frame, called by paint.
   */
  public void display() {  	
    if( glj.gljMakeCurrent(true) ) {
			this.shallWeRender=true; //make sure we're rendering
      
	    //prepare the canvas for rendering
	    gl.glClear( GLEnum.GL_COLOR_BUFFER_BIT | GLEnum.GL_DEPTH_BUFFER_BIT );
	    gl.glLoadIdentity();
	          
	    //setup canvas according to user manipulations
	    gl.glTranslatef(xPos, yPos, zPos);
	    gl.glScalef( (float)0.05 , (float)0.05, (float)0.05 );
	    gl.glRotatef( rotAngleX, 1.0f ,  0.0f , 0.0f );
	    gl.glRotatef( rotAngleY, 0.0f ,  1.0f , 0.0f );  
      gl.glRotatef( -90.0f , 1.0f ,  0.0f , 0.0f );

      if (mode==MODEL_MODE) {		    							    
	      //draw the model on the canvas
	      if (!isSuspended()) nextFrame();
	      if (model!=null) model.accept(new MD3GLModelDrawModelVisitor(this));
      }
      else if (mode==TEXTURE_MODE) {
      	//draw the texture on the canvas
      	activateRenderMode();
      	MD3ViewGLTextureRenderer.renderTexture(this, texture);
      }

      glj.gljSwap();
      
      if (debug)      
      	glj.gljCheckGL();
    }
  }
  
  /**
   * <p>Override needed when using GLCanvas: resize canvas.
   */
  public void reshape(int width, int height) {
    gl.glViewport(0, 0, width, height);    
    gl.glMatrixMode(GLEnum.GL_PROJECTION); 
    gl.glLoadIdentity();
    if (height > 0)
      glu.gluPerspective((double)90, (double)width/(double)height, NEAR_GL_PLANE, FAR_GL_PLANE);
    gl.glMatrixMode(GLEnum.GL_MODELVIEW);
  }
  
  /** 
   * <p>Override needed when using GLCanvas: create java-stuff and do GL-inits.
   */
  public void init() {
    if( !glj.gljMakeCurrent(true))
      throw new RuntimeException("problem with gljMakeCurrent in init()");
    
    setBackgroundColor(backgroundColor);
      
    gl.glClearDepth(1.0);
    gl.glDepthFunc(GLEnum.GL_LEQUAL);
    gl.glEnable(GLEnum.GL_DEPTH_TEST);    

    gl.glPixelStorei(GLEnum.GL_UNPACK_ALIGNMENT, 1);  
    gl.glEnable(GLEnum.GL_BLEND);    
    gl.glEnable(GLEnum.GL_NORMALIZE);
       
    float[] mat_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
    float[] mat_shininess = { 20.0f };    
    float[] light_ambient = { 0.9f, 0.9f, 0.9f, 1.0f };
    float[] light0_diffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
    float[] light0_position = { 55.0f, -50.0f, -5.0f, 0.0f };
    float[] light1_diffuse = { 0.5f, 0.5f, 1.0f, 1.0f };
    float[] light1_position = { -50.0f, 45.0f, 15.0f, 0.0f };
  
    gl.glMaterialfv(GLEnum.GL_FRONT, GLEnum.GL_SPECULAR, mat_specular);
    gl.glMaterialfv(GLEnum.GL_FRONT, GLEnum.GL_SHININESS, mat_shininess);
    
    gl.glLightfv(GLEnum.GL_LIGHT0, GLEnum.GL_AMBIENT, light_ambient);
    gl.glLightfv(GLEnum.GL_LIGHT0, GLEnum.GL_DIFFUSE, light0_diffuse);
    gl.glLightfv(GLEnum.GL_LIGHT0, GLEnum.GL_POSITION, light0_position);
    gl.glLightfv(GLEnum.GL_LIGHT1, GLEnum.GL_AMBIENT, light_ambient);
    gl.glLightfv(GLEnum.GL_LIGHT1, GLEnum.GL_DIFFUSE, light1_diffuse);
    gl.glLightfv(GLEnum.GL_LIGHT1, GLEnum.GL_POSITION, light1_position);   
    gl.glEnable(GLEnum.GL_LIGHT0);
    gl.glEnable(GLEnum.GL_LIGHT1);
    gl.glEnable(GLEnum.GL_LIGHTING);

 		gl.glDisable(GLEnum.GL_LINE_SMOOTH);
    gl.glDisable(GLEnum.GL_POLYGON_SMOOTH);    

    gl.glShadeModel(GLEnum.GL_FLAT);   
    setRenderMode(MD3ViewGLCanvasRenderMode.FLAT_TEXTURED);

    reshape(this.getWidth(),this.getHeight()); //setup canvas for perspective view
  }
  
  /** 
   * <p>Override needed when using GLAnimCanvas: re-initialisation after stop for setSuspended(false).
   */
  public void ReInit() {}
  
  /**
   * <p>Reset all user manipulations (scaling + panning + rotating) of this model canvas.
   */
  public void resetManipulations() {
    this.rotAngleX=0;
    this.rotAngleY=0;
    this.xPos=0.0f;
    this.yPos=0.0f;
    this.zPos=-2.0f;
  }

  /**
   * <p>Set the top level model that is rendered on the canvas. This will
   * reset any manipulation that have been done on the canvas (rotate + pan +
   * scale).
   *
   * <p>Uploading of texture data to OpenGL is the responsability of the client!
   *
   * @param model The MD3Model to render.
   */  
  public void setModel(MD3Model model) {
    this.model=model;
    resetManipulations();
    mode=MODEL_MODE;
  }
  
  /**
   * <p>Return the top level model rendered on this canvas.
   *
   * @return The current MD3Model of the canvas.
   */
  public MD3Model getModel() {
    return this.model;
  }
  
  /**
   * <p>The canvas will show (render) the current model.
   */
  public void showModel() {
    mode=MODEL_MODE;
  }
  
  /**
   * <p>Set the texture that will be rendered on the canvas. This will reset any
   * manipulations.
   *
   * <p>Uploading of texture data to OpenGL is the responsability of the client!
   */
  public void setTexture(MD3GLTexture texture) {
  	this.texture=texture;
  	resetManipulations();
  	mode=TEXTURE_MODE;
  }

  /**
   * <p>Return the current texture of this canvas.
   */  
  public MD3GLTexture getTexture() {
  	return this.texture;
  }
  
  /**
   * <p>The canvas will show (render) the current texture.
   */
  public void showTexture() {
    mode=TEXTURE_MODE;
  }
    
  /**
   * <p>Start or stop the currently selected animation of the displayed model.
   *
   * @param b true will start the animation, false will stop it.
   */
  public void setAnimate(boolean b) {
  	if (b) {
  		this.threadSuspended=false;	
  		this.setSuspended(false, false); //start thread
  	}
  	else
  		this.threadSuspended=true; //Java 2 way of stopping thread
  }  
  
  /**
   * <p>Set the animation of the model that will be displayed on the canvas.
   *
   * @param anim The animation to play.
   */
  public void setAnimation(MD3Animation anim) {
  	this.animation=anim;
  	
  	//setup visitors to control the animation
  	this.nextVisitor=new MD3GLModelChangeCurrentFrameVisitor(this.animation, MD3GLModelChangeCurrentFrameVisitor.NEXT);
  	this.prevVisitor=new MD3GLModelChangeCurrentFrameVisitor(this.animation, MD3GLModelChangeCurrentFrameVisitor.PREVIOUS);
  	this.rewindVisitor=new MD3GLModelChangeCurrentFrameVisitor(this.animation, MD3GLModelChangeCurrentFrameVisitor.REWIND);
  	
  	rewindAnimation();
    setInterpolate(this.interpolate);
  }
  
  /**
   * <p>Return the currently active animation of the canvas.
   */
  public MD3Animation getAnimation() {
  	return this.animation;
  }  
  
  /**
   * <p>Set the background color of the canvas.
   */
  public void setBackgroundColor(Color c) {
    this.backgroundColor=c;
    gl.glClearColor(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f, 0.0f);    
  }
  
  /**
   * <p>Get the current background color of the canvas.
   */
  public Color getBackgroundColor() {
    return backgroundColor;
  }
    
  /**
   * <p>Switch to the next frame in the current animation of the model.
   */
  public void nextFrame() {
  	if (model!=null && animation!=null) {
  		nextVisitor.setInterpolate(this.interpolate);
  		model.accept(nextVisitor);
  	}
  }
  
  /**
   * <p>Switch to the previous frame in the current animation.
   */
  public void previousFrame() {
  	if (model!=null && animation!=null) {
  		prevVisitor.setInterpolate(this.interpolate);
  		model.accept(prevVisitor);
  	}
  }
  
  /**
   * <p>Rewind the animation of the model back to the first frame.
   */
  public void rewindAnimation() {
  	if (model!=null && animation!=null)
  		model.accept(rewindVisitor);
  }
  
  /**
   * <p>Enable or disable interpolation between key animation frames.
   */
  public void setInterpolate(boolean b) {
  	this.interpolate=b;
  	//set fps of animation accordingly
  	if (animation!=null) {
	  	if (b)
	  		setAnimateFps((double)(this.animation.fps / MD3GLModelChangeCurrentFrameVisitor.FRACTION));
	  	else
	  		setAnimateFps(animation.fps);
  	}
  }
  
  /**
   * <p>Switch the display to the specified render mode.
   *
   * <p>Note that the new rendermode will only become active after a call to
   * the activateRenderMode() method.
   * @param rendermode The render mode to switch to.
   */
  public void setRenderMode(MD3ViewGLCanvasRenderMode rendermode) {
  	if (mode==MODEL_MODE)
	    this.rendermode=rendermode;
  }  
  
  /**
   * <p>Activate the current rendering mode.
   */
  public void activateRenderMode() {
  	//textures allways rendered in textured mode!
  	MD3ViewGLCanvasRenderMode rm = (mode==TEXTURE_MODE)?
                                   MD3ViewGLCanvasRenderMode.FLAT_TEXTURED:rendermode; 
    rm.apply(gl);
  }
  
  /**
   * <p>Returns the currently used rendering mode.
   */
  public MD3ViewGLCanvasRenderMode getRenderMode() {
    return this.rendermode;
  }
  
  /**
   * <p>Dump the image on the canvas to a Targa file.
   *
   * @param filename Name of the output file.
   */
  public void dumpCanvasToTGA(String filename) throws java.io.IOException {
  	byte[] pixels=new byte[this.getWidth() * this.getHeight() * 3];
  	
  	sDisplay(); //make sure the image on the canvas is properly drawn!

    //setup pixel storage modes for data coming into client (our) memory
    gl.glPixelStorei(GLEnum.GL_PACK_ALIGNMENT, 1); /* byte alignment */
    gl.glPixelStorei(GLEnum.GL_PACK_ROW_LENGTH, this.getWidth());
          
  	//get viewport data
		gl.glReadBuffer(GLEnum.GL_BACK);
		gl.glReadPixels(0, 0, this.getWidth(), this.getHeight(), GLEnum.GL_RGB, GLEnum.GL_UNSIGNED_BYTE, pixels);		

    //reset pixel storage modes
    gl.glPixelStorei(GLEnum.GL_PACK_ALIGNMENT, 4);
    gl.glPixelStorei(GLEnum.GL_PACK_ROW_LENGTH, 0);
    
		LittleEndianDataOutputStream fout=new LittleEndianDataOutputStream(new java.io.FileOutputStream(filename));
		
		//write TGA header
		fout.writeByte(0); //ID length, 0 because no image id field
		fout.writeByte(0); //no color map
		fout.writeByte(2); //image type (24 bit RGB, uncompressed)
		fout.writeShort(0); //color map origin, ignore because no color map
		fout.writeShort(0); //color map length, ignore because no color map
		fout.writeByte(0); //color map entry size, ignore because no color map
		fout.writeShort(0); //x origin
		fout.writeShort(0); //y origin
		fout.writeShort((short)this.getWidth()); //image width
		fout.writeShort((short)this.getHeight()); //image height
		fout.writeByte(24); //bpp
		fout.writeByte(0); //description bits
		
		//process image data: TGA pixels should be written in BGR format, so R en B should be switched
		for (int i=0; i<(this.getWidth()*this.getHeight()*3); i+=3) {
			byte tmp=pixels[i];
			pixels[i]=pixels[i+2];
			pixels[i+2]=tmp;
		}
		
		//write TGA image data
		fout.writeFully(pixels, 0, pixels.length);
		
		fout.close();
  }
  
  /**
   * <p>Returns a string with info on the OpenGL rendering system used by the canvas.
   */
  public String getRenderInfo() {
    String info="GL4Java:\n" +
                "\n" +
                "gl4java.GLContext class version: " + GLContext.gljGetClassVersion() + "\n" +
                "gl4java.GLContext native-lib version: " + GLContext.gljGetNativeLibVersion() + "\n" +
                "gl4java.GLFunc class version: " + gl.getClassVersion() + "\n" +
                "gl4java.GLFunc native-lib version: " + gl.getNativeVersion() + "\n" +
                "gl4java.GLUFunc class version: " + glu.getClassVersion() + "\n" +
                "gl4java.GLUFunc native-lib version: " + glu.getNativeVersion() + "\n" +
                "\n" +
                "Mipmapping is " + (mipmapping?"active":"not active") + "\n" +
                "\n" +
                "-------------------------------------------------\n" +
                "\n" +
                "OpenGL:\n" +
                "\n" +
                "GL Vendor: " + gl.glGetString(GLFunc.GL_VENDOR) + "\n" +
                "GL Renderer: " + gl.glGetString(GLFunc.GL_RENDERER) + "\n" +
                "GL Version: " + gl.glGetString(GLFunc.GL_VERSION) + "\n" +
                "GL Extensions: " + gl.glGetString(GLFunc.GL_EXTENSIONS) + "\n" +
                "GLU Version: " + glu.gluGetString(GLUFunc.GLU_VERSION) + "\n" +
                "GLU Extensions: " + glu.gluGetString(GLUFunc.GLU_EXTENSIONS) + "\n";
                
    return info;
  }
}