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

package widgets.awt;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

import widgets.awt.event.*;

/**
 * <p>This class implements a tree widget: a graphical representation
 * of hierarchical data.
 *
 * <p>The widget supports ActionListener and PopupMenuListener objects.
 * Registered listeners will be notified when a node is double
 * clicked or when a popup menu is requested. Note that TreeNode objects
 * also support these listeners. It is up to the programmer to decide
 * at which level to handle events.
 *
 * <p>Scroll bars are not directely supported by this widget. If you want
 * a scrollable tree you'll have to put this component in a ScrollPane.
 *
 * <p>Although the tree accepts images of any size, it is designed to work
 * best with images of size about 16x16 pixels.
 *
 * @see widgets.awt.TreeNode
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class Tree extends Canvas {
	
  private static final int PRE_RECT_GAP = 3;  
  private static final int RECT_WIDTH = 8;
  private static final int RECT_HEIGHT = 8;
  private static final int RECT_IMG_GAP = 5;
  private static final int LABEL_GAP = 4;

  private TreeNode root=null, selectedNode=null;

  private int DELTAX;
  private int DELTAY;
  private int RECT_VERT_GAP;
  private int IMG_VERT_GAP;
  private int IMG_WIDTH;
  private int IMG_HEIGHT;
  private int PARENT_LINE_OFFSET ;
  
  private Image backBuffer=null;
  private Image openImg=null, closeImg=null;

  private int maxLineLength;  
  
  private boolean showNodeIcons;

  private ArrayList actionListeners=new ArrayList();
  private ArrayList popupMenuListeners=new ArrayList();

  //does an image need to be drawn for the given node?
  private boolean hasImage(TreeNode t) {
  	return (t.getDirectory() && closeImg != null && openImg != null) ||
  		     (showNodeIcons && t.getIcon()!=null);
  }

  //Calc. the max line length in pixels of all visible 'lines' in the specified
  //tree, the result is stored in the global maxLineLength data member
  private void updateMaxLineLength(TreeNode t, int x) {
   	int lineLength=x+PRE_RECT_GAP+RECT_WIDTH+RECT_IMG_GAP;
   	
   	//with icon
    if (hasImage(t))    	
	   	lineLength+=IMG_WIDTH;

	  lineLength+=LABEL_GAP + this.getFontMetrics(this.getFont()).stringWidth(t.getName()) + PRE_RECT_GAP;
	  	
	  if (lineLength>maxLineLength)
	  	maxLineLength=lineLength;
 
    //draw children
    if(t.getExpanded()) {
	    x += DELTAX;
	    
	    for(TreeNode node = t.firstChild(); node != null; node = node.next())
	    	updateMaxLineLength(node, x);
    }
  }

  //is the given x coord in the expand/collapse rectangle of the given level?
  private boolean isInRect(int x, int level) {
    return level * DELTAX + PRE_RECT_GAP < x && x < level * DELTAX + PRE_RECT_GAP + RECT_WIDTH;
  }
  
  //draw the given tree on the graphics object starting at coord (x,y)
  private void drawTree(Graphics g, TreeNode t, int x, int y) {        
   	//draw the rectangle with + or minus sign to expand/collapse the children of this node
    if(t.countChildren() > 0) {
      g.drawRect(x + PRE_RECT_GAP, y + RECT_VERT_GAP, RECT_WIDTH, RECT_HEIGHT);
      
      //draw + or - sign in rectangle
      g.drawLine(x + PRE_RECT_GAP + 3, y + DELTAY / 2, (x + PRE_RECT_GAP + RECT_WIDTH) - 3, y + DELTAY / 2);
      if(!t.getExpanded())
        g.drawLine(x + PRE_RECT_GAP + RECT_WIDTH / 2, (y + DELTAY / 2) - 2, x + PRE_RECT_GAP + RECT_WIDTH / 2, (y + DELTAY / 2) + 2);
      
      //line connecting rectangle with icon (or text if no icon)  
      g.drawLine(x + PRE_RECT_GAP + RECT_WIDTH, y + DELTAY / 2, x + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP, y + DELTAY / 2);
      
      //draw connector with previous line above rectangle
      if(t.level() > 0)
        g.drawLine(x + PRE_RECT_GAP + RECT_WIDTH / 2, y, x + PRE_RECT_GAP + RECT_WIDTH / 2, y + RECT_VERT_GAP);

      //draw connector with nexe line under rectangle
      if(t.next() != null)      	
        g.drawLine(x + PRE_RECT_GAP + RECT_WIDTH / 2, (y + DELTAY) - RECT_VERT_GAP, x + PRE_RECT_GAP + RECT_WIDTH / 2, y + DELTAY);
    }
    else {
    	//draw vertical parent connection line
      if(t.level() > 0)
        g.drawLine(x + PRE_RECT_GAP + RECT_WIDTH / 2, y, x + PRE_RECT_GAP + RECT_WIDTH / 2, t.next() != null ? y + DELTAY : y + DELTAY / 2);
        
      //draw line connecting parent connection line with icon (or text if no icon)  
      g.drawLine(x + PRE_RECT_GAP + RECT_WIDTH / 2, y + DELTAY / 2, x + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP, y + DELTAY / 2);
    }
    
    //draw vertical parent connection lines
    int X = x + PRE_RECT_GAP + RECT_WIDTH / 2;
    for(TreeNode p=t.getParent(); p != null; p = p.getParent()) {
	    X -= DELTAX;
	    if(!p.isLastChild())
	      g.drawLine(X, y, X, y + DELTAY);
	  }	  
    
    //draw icon and text
    if (t==selectedNode)
    	g.setColor(Color.blue);
    
    if (hasImage(t)) {
    	if (t.getDirectory())
	    	if (!t.getExpanded())
	      	g.drawImage(closeImg, x + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP, y + IMG_VERT_GAP, IMG_WIDTH, IMG_HEIGHT, this);
	    	else {
		      g.drawImage(openImg, x + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP, y + IMG_VERT_GAP, IMG_WIDTH, IMG_HEIGHT, this);    	      
		      
					//draw connection of icon with child
				  g.drawLine(x + DELTAX + PRE_RECT_GAP + RECT_WIDTH /2 , (y + DELTAY) - IMG_VERT_GAP, x + DELTAX + PRE_RECT_GAP + RECT_WIDTH / 2, y + DELTAY);
	    	}
	    else //its a non-directory node
	      g.drawImage(t.getIcon(), x + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP, y + IMG_VERT_GAP, IMG_WIDTH, IMG_HEIGHT, this);
	      
	   	g.drawString(t.getName(), x + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP + IMG_WIDTH + LABEL_GAP, y + DELTAY / 2 + getFont().getSize() / 2);
    }
    else
	   	g.drawString(t.getName(), x + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP + LABEL_GAP, y + DELTAY / 2 + getFont().getSize() / 2);
	   	
	  g.setColor(Color.black);

    //draw children
    if(t.getExpanded()) {
	    x += DELTAX;
	    
	    for(TreeNode node = t.firstChild(); node != null; node = node.next()) {
        y += DELTAY + ((node.previous()==null)?0:(DELTAY * node.previous().countVisibleChildren()));
	          
	      drawTree(g, node, x, y);
	    }
    }
  }

  //return the node at the given 'level' ('line') of the tree, null if none
  private TreeNode getNodeFromLevel(TreeNode node, int l) {
    if (l == 1)
      return node;
    else if (!node.getExpanded())
      return null;
    else {
	    l--;
	    
	    for(TreeNode t = node.firstChild(); t != null; t = t.next()) {
	      TreeNode n = getNodeFromLevel(t, l);
	      if(n != null)
	        return n;
	      else
	        l = l - (1 + t.countVisibleChildren());
	    }
	
	    return null;
    }
  }

  protected void processActionEvent(ActionEvent e) {
  	Iterator it=actionListeners.iterator();  	
  	while (it.hasNext())
  		((ActionListener)it.next()).actionPerformed(e);
  }

  public void addActionListener(ActionListener l) {
  	if (!actionListeners.contains(l))
  		actionListeners.add(l);
  }  
  
  public void removeActionListener(ActionListener l) {
  	actionListeners.remove(l);
  }
  
  protected void processPopupMenuEvent(PopupMenuEvent e) {
  	Iterator it=popupMenuListeners.iterator();
  	while (it.hasNext())
  		((PopupMenuListener)it.next()).popupMenu(e);
  }
  
  public void addPopupMenuListener(PopupMenuListener l) {
  	if (!popupMenuListeners.contains(l))
  		popupMenuListeners.add(l);
  }
  
  public void removePopupMenuListener(PopupMenuListener l) {
  	popupMenuListeners.remove(l);
  }
    
  /**
   * <p>Default constructor. Creates a tree with no directory icons.
   */
  public Tree() {
  	this(null, null, 2);
  }
  
  /**
   * <p>Create a tree widget that will display the specified open/close directory
   * icons before each directory in the tree. The icons will only be used if both
   * icons can be loaded!
   *
   * <p>The parentLineOffset specifies by how many pixels the parent connection
   * line should be indented to center under the given directory icons.
   */
  public Tree(URL openDirectoryIconURL, URL closeDirectoryIconURL, int parentLineOffset) {
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);        
    
    setIcons(openDirectoryIconURL, closeDirectoryIconURL, parentLineOffset);    
    setShowNodeIcons(false);    
    	
    DELTAX = RECT_WIDTH + RECT_IMG_GAP + PARENT_LINE_OFFSET;
    
    setFont(new Font("Arial", Font.PLAIN, 12));
  }
  
  /**
   * <p>Redraw the tree in the off-screen back buffer, then make sure this back buffer
   * is visible.
   *
   * <p>Make sure you call redraw after programmatic changes to the tree diplayed by
   * the Tree widget!
   */
  public void redraw() {
  	if (root!=null) {
  		//setup back buffer
    	maxLineLength=0;
  		updateMaxLineLength(root, 0);
	  	backBuffer=createImage(maxLineLength, (root.countVisibleChildren() + 1) * DELTAY);
	  	
	  	//draw tree
	  	Graphics g=backBuffer.getGraphics();
	  	g.setColor(Color.black);
	    drawTree(g, root, 0, 0);
  	}
  	else
  		backBuffer=createImage(1,1);

    invalidate();
    repaint();
    getParent().validate();
  }
  
  public void setFont(Font f) {
    super.setFont(f);
    DELTAY = f.getSize() + f.getSize() / 2;
    RECT_VERT_GAP = (DELTAY - RECT_HEIGHT) / 2;
    IMG_VERT_GAP = (DELTAY - IMG_HEIGHT) / 2 + 1;
  }
  
  /**
   * <p>Set the open and close directory icons of this tree to the given icons. Only
   * if both icons can be loaded will they be used in the tree.
   *
   * <p>The parentLineOffset specifies by how many pixels the parent connection
   * line should be indented to center under the given directory icons.
   */
  public void setIcons(URL openDirectoryIconURL, URL closeDirectoryIconURL, int parentLineOffset) {
   	PARENT_LINE_OFFSET=parentLineOffset;

    if (openDirectoryIconURL!=null && closeDirectoryIconURL!=null) {	  	
	    MediaTracker tracker=new MediaTracker(this);
	  	openImg = Toolkit.getDefaultToolkit().getImage(openDirectoryIconURL);
	  	closeImg = Toolkit.getDefaultToolkit().getImage(closeDirectoryIconURL);
	  	tracker.addImage(openImg, 1);
	  	tracker.addImage(closeImg, 2);
	  	try { tracker.waitForAll(); } catch (InterruptedException e) {}    
	  	
	  	IMG_WIDTH = openImg.getWidth(this);
	  	IMG_HEIGHT = openImg.getHeight(this);
    }
    else 
    	IMG_WIDTH = IMG_HEIGHT = 16;
  }

  /**
   * <p>Specifies whether or not to display the node icons in the tree. If a tree node has
   * an icon it will be displayed in front of its name.
   */
  public void setShowNodeIcons(boolean show) {
  	this.showNodeIcons=show;
  }

  /**
   * <p>Are node icons being shown?
   */
  public boolean getShowNodeIcons() {
    return this.showNodeIcons;
  }

  /**
   * <p>Set the root of the tree displayed by the widget.
   */
  public void setRoot(TreeNode root) {  	
  	this.root=root;
  }

  /**
   * <p>Get the root of the tree displayed by the widget.
   */
  public TreeNode getRoot() {
    return root;
  }
  
  /**
   * <p>Return the currently selected node of the tree, or null if none.
   */
  public TreeNode getSelectedNode() {
  	return selectedNode;
  }
  
  protected void processMouseEvent(MouseEvent e) {
  	if (e.getID()==MouseEvent.MOUSE_RELEASED && root != null) {
	    TreeNode node = getNodeFromLevel(root, e.getY() / DELTAY + 1);
		    
	    if (node != null) {	    		    	
		    if(isInRect(e.getX(), node.level())) {
		    	//the expand/collapse rectangle was clicked
		    	node.setExpanded(!node.getExpanded());
		    	
		    	redraw();
		    }
		    else if (e.getX() > node.level() * DELTAX + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP
		    	       && e.getX() < node.level() * DELTAX + PRE_RECT_GAP + RECT_WIDTH + RECT_IMG_GAP + 
		    	       	  (hasImage(node) ? IMG_WIDTH : 0) + LABEL_GAP + this.getFontMetrics(this.getFont()).stringWidth(node.getName())) {		    			    	
		    	if (e.isPopupTrigger() && node==selectedNode) {
		    		//generate popup menu event
		    		node.processPopupMenuEvent(new PopupMenuEvent(node, e.getX(), e.getY()));
		    		this.processPopupMenuEvent(new PopupMenuEvent(this, e.getX(), e.getY()));
		    	}
          else if (!e.isPopupTrigger()) {
						//the label was clicked
			    	selectedNode=node;
	          if (e.getClickCount() == 2) { //was the node double clicked?
	      			node.setExpanded(!node.getExpanded());
		    				
					    //generate action event
					    node.processActionEvent(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, null, e.getModifiers()));
					    this.processActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, e.getModifiers()));
		  			}
	
			    	redraw();
          }
		    }		    
	    }	    
  	}
  	
  	super.processMouseEvent(e);
  }  

  public void paint(Graphics g) {
  	if (backBuffer==null)  		  	
  		redraw();
  	
  	g.drawImage(backBuffer, 0, 0, this);
  }
  
  public boolean isDoubleBuffered() {
  	return true;
  }
  
  public Dimension getPreferredSize() {
  	if (backBuffer!=null)
  		return new Dimension(backBuffer.getWidth(this), backBuffer.getHeight(this));
  	else
  		return new Dimension(0, 0);
  }  
}