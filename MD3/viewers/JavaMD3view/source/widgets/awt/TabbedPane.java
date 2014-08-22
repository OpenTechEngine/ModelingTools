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

/**
 * <p>Implements a tab panel: a component which lets the user switch
 * between a group of components by clicking on a tab with a given title.
 *
 * <p>After programmatic changes to the component (i.e. using selectTab())
 * repaint should be called.
 *
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class TabbedPane extends Panel {
	
	private static final int MAX_NR_OF_TABS = 25;
	private static final int BORDER_WIDTH = 2;
	private static final int TAB_BACK_OFFSET = 2;
	private static final int TAB_TOP_OFFSET = 2;
	private static final int HORZ_LABEL_GAP = 4;
	private static final int VERT_LABEL_GAP = 0;

  private int tabHeight, tabPanelWidth, tabPanelHeight;
  private int[] tabLeftBorder, tabRightBorder;
  private FontMetrics fontMetric;
  private Component[] tabContents;
  private String[] tabNames;
  private boolean[] tabEnabled;
  private int tabCount, selectedTab;

  /**
   * <p>Create a new tab panel with the given width and height.
   */
  public TabbedPane(int width, int height) {
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);        
  	
    setFont(new Font("Helvetica", Font.PLAIN, 12));
    tabHeight = fontMetric.getHeight() + 2 * BORDER_WIDTH + TAB_TOP_OFFSET + 2*VERT_LABEL_GAP;
    setSize(width, height);
    setBackground(new Color(SystemColor.control.getRGB()));
    setLayout(null);
    
    tabLeftBorder = new int[MAX_NR_OF_TABS];
    tabRightBorder = new int[MAX_NR_OF_TABS];
    tabContents = new Component[MAX_NR_OF_TABS];
    tabNames = new String[MAX_NR_OF_TABS];
    tabEnabled = new boolean[MAX_NR_OF_TABS];
    
    tabCount=0;
    selectedTab=0;
  }
  
  public void setSize(int width, int height) {
  	super.setSize(width, height);
    tabPanelWidth = width - 2 * BORDER_WIDTH;
    tabPanelHeight = height - tabHeight - BORDER_WIDTH;
    
    for (int i=0;i<tabCount; i++)
    	tabContents[i].setSize(tabPanelWidth, tabPanelHeight);
  }
  
  public void setSize(Dimension d) {
  	setSize((int)d.getWidth(), (int)d.getHeight());
  }
    
  public void setFont(Font f) {
    fontMetric = getFontMetrics(f);  	
  }

  /**
   * <p>Add a tab with the given name to the end of the tab list.
   */
  public void addTab(String name, Component comp) {
    if(tabCount < MAX_NR_OF_TABS && comp != this) {
	    for(int i = 0; i < tabCount; i++)
	      if(tabContents[i] == comp || tabNames[i].equals(name))
	         return;

	    tabNames[tabCount] = name;
	    tabEnabled[tabCount] = true;
	    tabContents[tabCount] = comp;
	    
	    tabLeftBorder[tabCount] = tabCount != 0 ? tabCount != 1 ? tabRightBorder[tabCount - 1] : tabRightBorder[tabCount - 1] - BORDER_WIDTH : 0;
	    tabRightBorder[tabCount] = tabCount != 0 ? tabLeftBorder[tabCount] + 2 * HORZ_LABEL_GAP + fontMetric.stringWidth(name) + 2 * BORDER_WIDTH : 2 * HORZ_LABEL_GAP + fontMetric.stringWidth(name) + 4 * BORDER_WIDTH;

	    if(tabRightBorder[tabCount] > getWidth())
	      return;	        
	        
	    comp.setBounds(BORDER_WIDTH, tabHeight, tabPanelWidth, tabPanelHeight);
	    add(comp);
	    
	    if(tabCount != selectedTab)
	      comp.setVisible(false);
	      
	    tabCount++;
    }
  }

  /**
   * <p>Remove the specified tab.
   */
  public void removeTab(String name) {
    for(int i = 0; i < tabCount; i++)
      if(tabNames[i].equals(name)) {      	
        remove(tabContents[i]);
        selectTab(tabNames[0]);
        
        int removedTabWidth = tabRightBorder[i] - tabLeftBorder[i];
        for(int j = i; j < tabCount - 1; j++) {
          if(j == 0) {
            tabRightBorder[j] += BORDER_WIDTH;
            tabLeftBorder[j] += BORDER_WIDTH;
          }
          else {
            tabRightBorder[j] = tabRightBorder[j + 1] - removedTabWidth;
            tabLeftBorder[j] = tabLeftBorder[j + 1] - removedTabWidth;
          }
          
          tabNames[j] = tabNames[j + 1];
          tabEnabled[j] = tabEnabled[j + 1];
          tabContents[j] = tabContents[j + 1];
        }

        tabCount--;
        return;
      }
  }

  /**
   * <p>Enable/disable the specified tab.
   */
  public void setEnabledTab(String name, boolean b) {
    for(int i = 0; i < tabCount; i++)
      if(tabNames[i].equals(name)) {
          tabEnabled[i] = b;
          return;
      }
  }
  
  /**
   * <p>Rename the oldName tab to newName.
   */
  public void renameTab(String oldName, String newName) {
    for(int i = 0; i < tabCount; i++)
      if(tabNames[i].equals(oldName)) {
        int widthChange = fontMetric.stringWidth(newName) - fontMetric.stringWidth(oldName);
        
        //is there enough space for the name change?
        if(tabRightBorder[tabCount - 1] + widthChange > getWidth())
          return;
        
        tabNames[i] = newName;        
        tabRightBorder[i] += widthChange;
        
        for(int j = i + 1; j < tabCount; j++) {
          tabRightBorder[j] += widthChange;
          tabLeftBorder[j] += widthChange;
        }

        return;
      }
  }
  
  /**
   * <p>Select and show the tab with given name.
   */
  public void selectTab(String name) {
    for (int i = 0; i < tabCount; i++)
      if (tabNames[i].equals(name)) {
      	selectTab(i);
        return;
      }
  }
  
  /**
   * <p>To select and show the tab with given index.
   */
  public void selectTab(int i) {
  	if (i!=selectedTab && tabEnabled[i] && i>=0 && i<tabCount) {  	
	    tabLeftBorder[selectedTab] += BORDER_WIDTH;
	    tabRightBorder[selectedTab] -= BORDER_WIDTH;
	    tabContents[selectedTab].setVisible(false);
	    tabContents[i].setVisible(true);
	    selectedTab = i;
	    tabLeftBorder[i] -= BORDER_WIDTH;
	    tabRightBorder[i] += BORDER_WIDTH;
  	}
  }
  
  /**
   * <p>Return the current number of tabs.
   */
  public int countTabs() {      	
  	return tabCount;
  }
  
  /**
   * <p>Return the index of the tab with given name, or -1 if the
   * tab is not present.
   */
  public int getTabIndex(String name) {
    for (int i = 0; i < tabCount; i++)
      if (tabNames[i].equals(name))
      	return i;
  	
  	return -1;
  }
  
  /**
   * <p>Return the name of the tab with specified index or null if
   * the index is illegal.
   */
  public String getTabName(int i) {
  	if (i>=0 && i<tabCount)
  		return tabNames[i];
  	else	
  		return null;
  }

  private void drawTab(Graphics g, int i) {
    int selected = ((i != selectedTab) ? 1 : 0);

    //draw right border of tab      
    g.setColor(new Color(127, 127, 127));
    for (int j = BORDER_WIDTH; (i != selectedTab - 1) && (j > 0); j--) {
      if (j == 1) {
      	g.setColor(Color.black);
        g.drawLine(tabRightBorder[i] - BORDER_WIDTH, selected * 2 + 1, tabRightBorder[i] - 1, selected * 2 + BORDER_WIDTH);
      }
      
      g.drawLine(tabRightBorder[i] - j, TAB_TOP_OFFSET + BORDER_WIDTH + selected * TAB_BACK_OFFSET - j, tabRightBorder[i] - j, tabHeight - BORDER_WIDTH + j - 1);
    }

    //draw tab label
    if(tabEnabled[i])
      g.setColor(Color.black);
    else
      g.setColor(new Color(127, 127, 127));
    g.drawString(tabNames[i], tabLeftBorder[i] + BORDER_WIDTH * (2 - selected) + HORZ_LABEL_GAP, TAB_TOP_OFFSET + BORDER_WIDTH + selected * TAB_BACK_OFFSET + VERT_LABEL_GAP + fontMetric.getAscent() - 2);

    //draw top and border of tab
    g.setColor(Color.white);
    for (int j = 0; j < BORDER_WIDTH; j++) {
      if (j == 1) g.setColor(new Color(223, 223, 223));

      //rounding of left upper corner         
      int round = j != BORDER_WIDTH - 1 || j == 0 ? 2 : 1;      
      g.drawLine(tabLeftBorder[i] + j + round, selected * TAB_BACK_OFFSET + j, tabRightBorder[i] - BORDER_WIDTH - 1, selected * TAB_BACK_OFFSET + j);
      
      if (i != (selectedTab + 1)) {
        if (j == 0) //draw rounding dot
          g.drawLine(tabLeftBorder[i] + 1, selected * TAB_BACK_OFFSET + 1, tabLeftBorder[i] + 1, selected * TAB_BACK_OFFSET + 1);

        g.drawLine(tabLeftBorder[i] + j, selected * TAB_BACK_OFFSET + TAB_TOP_OFFSET, tabLeftBorder[i] + j, tabHeight - BORDER_WIDTH + j * (1 - selected));
      }
    }
  }

  public void paint(Graphics g) {
  	//draw the tabs;
    g.setColor(getBackground());
    g.fillRect(0, 0, tabPanelWidth, tabHeight);
    for (int i = 0; i < tabCount; i++)
      drawTab(g, i);

		//draw top and left border of tab contents
    g.setColor(Color.white);
    for (int j = 0; j < BORDER_WIDTH; j++) {
      if (j == 1) g.setColor(new Color(223, 223, 223));

      if (selectedTab != 0)
        g.drawLine(j, tabHeight - BORDER_WIDTH + j, tabLeftBorder[selectedTab] + j, tabHeight - BORDER_WIDTH + j);      
      g.drawLine(tabRightBorder[selectedTab] - j - 1, tabHeight - BORDER_WIDTH + j, getWidth(), tabHeight - BORDER_WIDTH + j);

      g.drawLine(j, tabHeight - BORDER_WIDTH + j, j, getHeight());
    }

    //draw bottom and right border of tab contents
    g.setColor(new Color(127, 127, 127));
    for (int j = BORDER_WIDTH; j > 0; j--) {
      if (j == 1) g.setColor(Color.black);

      g.drawLine(j - 1, getHeight() - j, getWidth(), getHeight() - j);
      
      g.drawLine(getWidth() - j, tabHeight - BORDER_WIDTH + j, getWidth() - j, getHeight());
    }
  }

  protected void processMouseEvent(MouseEvent e) {
  	if (e.getID() == MouseEvent.MOUSE_CLICKED && e.getY() < tabHeight)
      for(int i = 0; i < tabCount; i++)
        if(e.getX() > tabLeftBorder[i] && e.getX() < tabRightBorder[i] && i != selectedTab && tabEnabled[i]) {
        	repaint(); //strange: if called after selectedTab, sometimes correct tab is not visible !?!
          selectTab(i);
        }
        
    super.processMouseEvent(e);
  }  
}