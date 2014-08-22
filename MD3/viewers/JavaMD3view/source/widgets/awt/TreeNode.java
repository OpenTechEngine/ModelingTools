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

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import widgets.awt.event.*;

/**
 * <p>This class represents a node in a tree that can be displayed by the
 * Tree widget.
 *
 * <p>TreeNode objects support ActionListeners that will be notified when
 * the node has been double-clicked in a Tree widget.
 *
 * <p>TreeNodes also supports popup menus. Registered PopupMenuListeners will
 * be notified when a popup menu was requested for a node.
 *
 * @see widgets.awt.Tree
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class TreeNode {
	
  private TreeNode parent, children, previous, next;
  private String name;
  private Object content;
  private Image icon;
  private boolean expanded, directory;
  private int countChildren;
  
  private ArrayList actionListeners=new ArrayList();
  private ArrayList popupMenuListeners=new ArrayList();
  
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
   * <p>Create a tree node with the given parent, name, content and icon.
   */
  public TreeNode(TreeNode parent, String name, Object content, Image icon) {
  	this(parent, name, icon);
      
    setContent(content);
  }

  /**
   * <p>Create a tree node with given parent, name and icon, but no associated
   * content.
   */
  public TreeNode(TreeNode parent, String name, Image icon) {
    this(parent, name);
    
    setIcon(icon);
  }

  /**
   * <p>Create a tree node with the given parent, name and content but no icon.
   */
  public TreeNode(TreeNode parent, String name, Object content) {
    this(parent, name);
      
    setContent(content);
  }

  /**
   * <p>Create a tree node with the given parent and name, but with no icon nor
   * associated content.
   */
  public TreeNode(TreeNode parent, String name) {
    this.parent = parent;
    this.name = name;
    
    content = null;
    previous = null;
    next = null;
    expanded = false;
    directory = false;
    children = null;
    icon = null;
    countChildren = 0;
  }

  public void setParent(TreeNode parent) {
    this.parent = parent;
  }    

  public TreeNode getParent() {
    return parent;
  }

  public void setName(String newname) {
    name = newname;
  }

  public String getName() {
    return name;
  }

  public void setContent(Object content) {
    this.content=content;
  }

  public Object getContent() {
    return content;
  }

  public void setIcon(Image icon) {
  	this.icon=icon;
  }

  public Image getIcon() {
    return icon;
  }  

  /**
   * <p>Set whether this node is expanded or collapsed. Note that a node
   * with no children cannot be expanded and thus a getExpanded() on a
   * leaf node will always return false!
   */
  public void setExpanded(boolean b) {
  	if (countChildren == 0)
  		expanded = false;  	
  	else
      expanded = b;
  }

  public boolean getExpanded() {
  	return expanded;
  }
  
  /**
   * <p>Specify whether or not this node represents a directory.
   */
  public void setDirectory(boolean b) {
  	directory=true;
  }
  
  /**
   * <p>Does this node represent a directory?
   */
  public boolean getDirectory() {
  	if (countChildren > 0)
  		return true;
  	else
  		return directory;
  }

  /**
   * <p>Get this node's successor within the list of children of this node's
   * parent. Returns null if this node is the last child of its parent.
   */
  public TreeNode next() {
    return next;
  }

  /**
   * <p>Get this node's predecessor within the list of children of this node's
   * parent. Returns null if this node is the first child of its parent.
   */
  public TreeNode previous() {
    return previous;
  }

  /**
   * <p>Returns this node's first child, or null if none.
   */
  public TreeNode firstChild() {
    return children;
  }
  
  /**
   * <p>Return the child node of this node with the given name or null if 
   * there is no such child.
   */
  public TreeNode childByName(String name) {
  	for (TreeNode t=children; t != null; t=t.next())
  		if (t.getName().equals(name))
  			return t;
  	
  	return null;
  }

  /**
   * <p>Return the level of this node in a tree. The level of the root is 0,
   * it's children have level 1 and so on.
   */
  public int level() {
    if(parent == null)
      return 0;
    else
      return 1 + parent.level();
  }
  
  /**
   * <p>Return the number of direct children of this node. Possible offspring
   * of those children are not included in the count!
   */
  public int countChildren() {
    return countChildren;
  }

  /**
   * <p>Is this node the last child of its parent?
   */
  public boolean isLastChild() {
    return next == null;
  }

  /**
   * <p>Is this node the first child of its parent?
   */
  public boolean isFirstChild() {
    return previous == null;
  }

  /**
   * <p>Return the number of children (direct or indirect) of this node
   * that are 'visible'. A child is visible if all its parents are expanded.
   */
  public int countVisibleChildren() {
    if(!getExpanded())
      return 0;
    else {
      int n = 0;
      for(TreeNode t = children; t != null; t = t.next())
        n += t.countVisibleChildren();

      return n + countChildren;
    }
  }

  /**
   * <p>Remove this node from its parent's list of children. Since a root
   * cannot remove itself, nothing will happen in this case.
   */
  public void remove() {
    if(level() == 0) 
        return; //root cannot remove itself
        
    if(previous() == null && next() == null)
      parent.children = null;
    else if(previous() != null && next() != null) {
      previous.next = next;
      next.previous = previous;
    }
    else if(previous() == null && next() != null) {
      next.previous = null;
      parent.children = next;
    }
    else if(previous() != null && next() == null)
      previous.next = null;
      
    parent.countChildren--;
  }

  /**
   * <p>Add a node to the end of the list of children of this node.
   */
  public TreeNode addChild(TreeNode node) {
    node.parent = this;
    this.countChildren++;
    
    if(this.children == null)
      this.children = node;
    else {
    	//walk to last child
    	TreeNode t;
      for(t=this.children; t.next()!=null; t=t.next());
      
      t.next = node;
      node.previous = t;
      node.next = null;
    }
    
    return node;
  }

  /**
   * <p>Insert the given node directely after this node in the parent's
   * list of children.
   */
  public TreeNode insertSibling(TreeNode node) {
    node.parent = this.parent;
    this.parent.countChildren++;
    
    TreeNode temp = this.next;
    this.next = node;
    node.previous = this;
    node.next = temp;
    if(temp != null)
      temp.previous = node;
      
    return node;
  }
}