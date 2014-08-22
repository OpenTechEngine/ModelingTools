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

import widgets.awt.*;

/**
 * <p>This class implements the option set of the MD3View application. It also doubles
 * as a widget to control these options.
 *  
 * @author Erwin Vervaet (klr8@fragland.net)
 */
public class MD3ViewOptions extends Dialog {
	
	private Checkbox warningOnTexLoadCheckbox, tryAltTexTypesCheckbox,
		               autoLoadSkinCheckbox, autoExportTexturesCheckbox,
		               autoAssemblePlayerModelsCheckbox;
	private Button applyButton;
	
	/**
	 * <p>Display a warning when there is a problem during texture loading?
	 */
	public static boolean warningOnTexLoad=false;
	
	/**
	 * <p>Automatically try alternate texture types when loading of a texture fails
	 * (i.e. try .jpg version when .tga version is not found or visa versa)?
	 */
	public static boolean tryAltTexTypes=true;
	
	/**
	 * <p>Automatically load the default skin when a player model is opened?
	 */
	public static boolean autoLoadSkin=true;
	
	/**
	 * <p>Automatically export textures when exporting to a format that supports them?
	 */
	public static boolean autoExportTextures=true;
	
	/**
	 * <p>Automatically assemble the lower, upper and head parts of a player model?
	 */
	public static boolean autoAssemblePlayerModels=true;
	
	//apply changes to data members
	private void apply() {
		warningOnTexLoad=warningOnTexLoadCheckbox.getState();
		tryAltTexTypes=tryAltTexTypesCheckbox.getState();
		autoLoadSkin=autoLoadSkinCheckbox.getState();
		autoExportTextures=autoExportTexturesCheckbox.getState();
		autoAssemblePlayerModels=autoAssemblePlayerModelsCheckbox.getState();
	}
	
	/**
	 * <p>Create a new option control widget for the given application.
	 */
	public MD3ViewOptions(MD3View owner) {				
		super(owner, "Options", true);
				
		Panel3D optionsPanel=new Panel3D(new GridLayout(5,1));
		
		warningOnTexLoadCheckbox=new Checkbox("Show warning on texture loading problem");
		optionsPanel.add(warningOnTexLoadCheckbox);
		tryAltTexTypesCheckbox=new Checkbox("Try alternate texture types");
		optionsPanel.add(tryAltTexTypesCheckbox);
		autoLoadSkinCheckbox=new Checkbox("Auto load default skin for player models");
		optionsPanel.add(autoLoadSkinCheckbox);
		autoExportTexturesCheckbox=new Checkbox("Auto export textures");
		optionsPanel.add(autoExportTexturesCheckbox);
		autoAssemblePlayerModelsCheckbox=new Checkbox("Auto assemble player models");
		optionsPanel.add(autoAssemblePlayerModelsCheckbox);
		
		Panel buttonPanel=new Panel(new FlowLayout(FlowLayout.RIGHT));				
		Button okButton=new Button("OK");
		buttonPanel.add(okButton);
		Button cancelButton=new Button("Cancel");
		buttonPanel.add(cancelButton);
		applyButton=new Button("Apply");
		buttonPanel.add(applyButton);
		
		//register listeners
		
		ItemListener enableApplyListener=new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
				applyButton.setEnabled(true);
      }
    };		
    warningOnTexLoadCheckbox.addItemListener(enableApplyListener);
    tryAltTexTypesCheckbox.addItemListener(enableApplyListener);
    autoLoadSkinCheckbox.addItemListener(enableApplyListener);
    autoExportTexturesCheckbox.addItemListener(enableApplyListener);
    autoAssemblePlayerModelsCheckbox.addItemListener(enableApplyListener);
    
    okButton.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		apply();
    		setVisible(false);
    	}
    });

    cancelButton.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		setVisible(false);
    	}
    });

    applyButton.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		apply();
    		applyButton.setEnabled(false);
    	}
    });

    this.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        setVisible(false);
      }
    });
    
    this.setResizable(false);
		this.setLayout(new BorderLayout());
		this.add(optionsPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.pack();
	}
	
  public void setVisible(boolean b) {
    if (b) {
    	applyButton.setEnabled(false);

    	//load current config
 	    warningOnTexLoadCheckbox.setState(warningOnTexLoad);
 	    tryAltTexTypesCheckbox.setState(tryAltTexTypes);
 	    autoLoadSkinCheckbox.setState(autoLoadSkin);   	
 	    autoExportTexturesCheckbox.setState(autoExportTextures);
 	    autoAssemblePlayerModelsCheckbox.setState(autoAssemblePlayerModels);
    	
    	//senter in parent coord. space
      this.setLocation(getOwner().getLocation().x + getOwner().getWidth()/2 - this.getWidth()/2,
                       getOwner().getLocation().y + getOwner().getHeight()/2 - this.getHeight()/2);
    }
    
    super.setVisible(b);
  }	
}