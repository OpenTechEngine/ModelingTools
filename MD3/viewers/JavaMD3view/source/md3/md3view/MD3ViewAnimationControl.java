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

import md3.md3model.*;

import widgets.awt.*;

/**
 * <p>This class implements the animation control widget of the MD3View application.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ViewAnimationControl extends Panel {
	
	private MD3View viewFrame;
  private AnimationCfg anims;
  private Choice animationChoice;
  private Checkbox animateCheckbox;
	
	/**
	 * <p>Create a new animation control widget for the given application.
	 */
	public MD3ViewAnimationControl(MD3View container) {
		this.viewFrame=container;
		
		this.setLayout(new BorderLayout());
    this.setBackground(new Color(SystemColor.control.getRGB()));
    
    //animation control panel
    Panel p=new Panel(new GridLayout(4,1));

    Panel p1=new Panel(new FlowLayout(FlowLayout.LEFT));
    animateCheckbox=new Checkbox("Animate");
    p1.add(animateCheckbox);    
    animationChoice=new Choice();
    animationChoice.add("No Animations"); //we start of with no animation    
    p1.add(animationChoice);
    p.add(p1);
    
    Panel p2=new Panel(new FlowLayout(FlowLayout.LEFT));
    Checkbox interpolateCheckbox=new Checkbox("Interpolate",false);
    p2.add(interpolateCheckbox);    
    p.add(p2);

    Panel p3=new Panel(new FlowLayout());
    Button rewindButton=new Button("Rewind");
    p3.add(rewindButton);    
    Button prevButton=new Button("Previous");
    p3.add(prevButton);    
    Button nextButton=new Button("Next");
    p3.add(nextButton);
    p.add(p3);

    Panel p4=new Panel(new FlowLayout());
    Button animationPropertiesButton=new Button("Animation Properties");
    p4.add(animationPropertiesButton);
    p.add(p4);    

    this.add(p, BorderLayout.NORTH);
            
    //add listeners for animation panel
        
    animateCheckbox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
    		viewFrame.md3canvas.setAnimate(e.getStateChange()==ItemEvent.SELECTED);
      }
    });
    
    animationChoice.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
      	viewFrame.md3canvas.setAnimation(anims.getAnimation(animationChoice.getSelectedItem()));
      	viewFrame.md3canvas.sDisplay();
      }
    });
        
    interpolateCheckbox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
    		viewFrame.md3canvas.setInterpolate(e.getStateChange()==ItemEvent.SELECTED);
      }
    });

    rewindButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewFrame.md3canvas.rewindAnimation();
        viewFrame.md3canvas.sDisplay();
      }      
    });

    prevButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewFrame.md3canvas.previousFrame();
        viewFrame.md3canvas.sDisplay();
      }
    });
    
    nextButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewFrame.md3canvas.nextFrame();
        viewFrame.md3canvas.sDisplay();
      }
    });    

    animationPropertiesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	    	MD3Animation anim=anims.getAnimation(animationChoice.getSelectedItem());
	    	
        new MessageDialog(viewFrame, "Animation Properties", false, null,
        	                new String[] { "Specific Animation Properties:",
        	                	             "    Animation Name: " + anim.name,
        	                	             "    Animation Type: " + anim.type,
        	                	             "    First Frame: " + anim.first + " (" + (anim.first + anim.offset)+ ")",
        	                	             "    Number Of Frames: " + anim.num,
        	                	             "    Number Of Looping Frames: " + anim.looping,
        	                	             "    Intended Frames Per Second: " + anim.fps,
        	                	             "",
        	                	             "General Animation Properties:",
        	                	             "    Sex: " + anims.sex,
        	                	             "    Head Offset: " + anims.headOffset,
        	                	             "    Footsteps: " + anims.footsteps	
        	                },
        	                true).setVisible(true);        
      }
    });    
	}
	
	/**
	 * <p>Enable or disable the animation control panel. When the panel is enabled,
	 * an attempt is made to load the animation.cfg file from the current directory
	 * of the currrent data source.
	 *
	 * <p>Animation is allways stopped, also when you enable the panel!
	 */
  public void setEnabled(boolean b) {  	
		//forward message to contained components (which are 2 levels deep!)
		Component[] panels=((Container)this.getComponent(0)).getComponents();
		for (int i=0;i<panels.length;i++) {
			Component[] cmps=((Container)panels[i]).getComponents();
			for (int j=0;j<cmps.length;j++)
				cmps[j].setEnabled(b);
		}		
			
 		if (b) {
  		animationChoice.removeAll();
  		
    	//try to load animation.cfg file
     	try {
     		InputStream in=viewFrame.getInputStreamForPath(viewFrame.searchForPath("animation.cfg", false));
    		anims=MD3ModelFactory.getFactory().makeAnimationCfg(in);
    		in.close();	    	
    	} catch (IOException ex) {
    		anims=MD3ModelFactory.getFactory().makeAnimationCfg();
    		    	  
        viewFrame.showExceptionDialog("problem loading animation config file: " + ex.getMessage() + ", enabling 'All Frames' animation instead");
    	}
    	
   		//generate default animation
  		MD3Animation defaultAnimation=MD3ModelFactory.getFactory().makeMD3Animation();
  		defaultAnimation.type=AnimationType.ALL;
  		defaultAnimation.name="All Frames";
  	  defaultAnimation.first=0;
  	  defaultAnimation.num=-1;
  	  defaultAnimation.looping=0;
  	  defaultAnimation.fps=20;
  	  
  	  //add default animation
  	  anims.putAnimation(defaultAnimation);

			//update choice menu with loaded animation info
  		Iterator it=anims.animationNames();
  		while (it.hasNext())
  			animationChoice.add((String)it.next());
  			
  		animationChoice.select("All Frames");
  		
    	//make sure selected animation is active on canvas
    	MD3Animation anim=anims.getAnimation(animationChoice.getSelectedItem());
    	viewFrame.md3canvas.setAnimation(anim);
 		}
		
		//always stop animation, also when animation panels is (re)enabled!
		animateCheckbox.setState(false);
    viewFrame.md3canvas.setAnimate(false);    
	
		this.validate();
	}
}