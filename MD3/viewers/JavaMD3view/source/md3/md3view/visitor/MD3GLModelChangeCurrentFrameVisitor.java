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

import java.util.*;

import md3.md3model.*;
import md3.md3view.glmodel.*;

/**
 * <p>An MD3GLModel visitor that walks through an MD3GLModel structure and applies
 * the specified animation operation (NEXT, PREVIOUS or REWIND) to the animation state
 * data of the encounterd models, taking the specified animation into account.
 *
 * @author Erwin Vervaet (klr8@fragland.net) 
 */ 
public class MD3GLModelChangeCurrentFrameVisitor extends MD3GLModelVisitor {

  public static final float FRACTION = 0.34f;

	/**
	 * <p>Switch to the next frame.
	 */
	public static final int NEXT=0;

	/**
	 * <p>Switch to the previous frame.
	 */
	public static final int PREVIOUS=1;

	/**
	 * <p>Rewind the animation.
	 */
	public static final int REWIND=2;
		
	private MD3Animation anim;
	private int op, upperBound;
	private boolean interpolate;
	
	/**
	 * <p>Create a new visitor to apply an animation operation (NEXT, REWIND, ...)
	 * to a MD3 model. No interpolation will be done.
	 *
	 * @param anim The animation that provides the context for the operation.
	 * @param op The operation to apply.
	 */
	public MD3GLModelChangeCurrentFrameVisitor(MD3Animation anim, int op) {
		this(anim, op, false);
	}
	
	/**
	 * <p>Create a new visitor to apply an animation operation (NEXT, REWIND, ...)
	 * to a MD3 model. The operation is executed in the context of the specified
	 * animation.
	 *
	 * @param anim The animation that provides the context for the operation.
	 * @param op The operation to apply.
	 * @param interpolate Should interpolation be done?
	 */
	public MD3GLModelChangeCurrentFrameVisitor(MD3Animation anim, int op, boolean interpolate) {
		this.anim=anim;
    if (op==NEXT || op==PREVIOUS || op==REWIND)
		  this.op=op;
    else
      throw new IllegalArgumentException("unknown animation operation type: " + op);
		this.interpolate=interpolate;
	}
	
	//calc next frame number
	private int next(int nr) {
    if (nr<(upperBound-1))
			return nr+1;
	  else { //rewind needed
      if (anim.num<0)
        return anim.first;
      else {
        nr = (anim.looping != 0)?(anim.num - anim.looping):0;      
	  	  return anim.first + nr;
      }
    }
	}
	
	//calc prev frame number
	private int prev(int nr) {
    if (nr==anim.first)
			return upperBound-1;
		else
			return nr-1;
	}
	
	//apply the specified operation to the animation state data members of the model
	//taking the specified animation into account
	private void doOp(MD3GLModel model) {
    //anim to be applied could have illegal data with respect to this model,
    //ignore anim in this case
    if (anim.first>=model.boneFrameNum || anim.first<0)
      return;
    
		//calc upper bound for animation frames in this model
		if (anim.num<0)
			upperBound=model.boneFrameNum; //use all available frames
		else
			upperBound=model.boneFrameNum<(anim.first+anim.num)?model.boneFrameNum:(anim.first+anim.num);

    switch (op) {
			case NEXT:
				if (interpolate) {
					model.interpolationFraction+=FRACTION;
					if (model.interpolationFraction>=1.0f) {
						model.currentFrame=model.nextFrame;
						model.nextFrame=next(model.nextFrame);
						model.interpolationFraction=0.0f;
					}
				}
				else {
					model.currentFrame=model.nextFrame;
					model.nextFrame=next(model.nextFrame);
				}
				break;
			case PREVIOUS:
				if (interpolate) {
					model.interpolationFraction-=FRACTION;
					if (model.interpolationFraction<0.0f) {
						model.nextFrame=model.currentFrame;
						model.currentFrame=prev(model.currentFrame);
						model.interpolationFraction=0.8f;
					}
				}
				else {
					model.nextFrame=model.currentFrame;
					model.currentFrame=prev(model.currentFrame);
				}
				break;
			case REWIND:
				model.currentFrame=anim.first;
				model.nextFrame=next(model.currentFrame);
				model.interpolationFraction=0.0f;
				break;
		}
	}
	
	public void visit(MD3GLModel model) {
		if ( (model.loadFilename.toLowerCase().indexOf("lower") != -1 &&
			    (anim.type==AnimationType.LEGS || anim.type==AnimationType.BOTH))
  		   // this is the LEGS model and the animation is applicable
  		  ||
			   (model.loadFilename.toLowerCase().indexOf("upper") != -1 &&
		      (anim.type==AnimationType.TORSO || anim.type==AnimationType.BOTH))
			   // this is the TORSO model and the animation is applicable
			  ||
			   anim.type==AnimationType.ALL
			   // the animation is allways applicable
			 )
			doOp(model); 
		//else do nothing
		
		//visit children
		Iterator it=model.linkedModels();
		while (it.hasNext())
			((MD3Model)it.next()).accept(this);
	}
	
	/**
	 * <p>Enable or disable interpolation.
	 */
	public void setInterpolate(boolean b) {
		this.interpolate=b;
	}
	
	/**
	 * <p>Is interpolation currently enabled?
	 */
	public boolean getInterpolate() {
		return this.interpolate;
	}
}