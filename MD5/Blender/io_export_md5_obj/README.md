---
format: Markdown
categories: Modelling
toc: no
title: io_export_md5_obj
...

io_export_md5_obj
---------


- MD5 Object Export
- version=1.0.0
- blender=2.6.3
- author=Mikko Kortelainen <mikko.kortelainen@fail-safe.net>
- license=GPLv3
- link=[github](https://github.com/OpenTechEngine/ModelingTools/tree/master/MD5/Blender/io_export_md5_obj)
- exe=io_import_md5_obj.py

**Introduction: Export**

Allows user to export blender objects to md5mesh and md5anim formats. 

Exports everything exportable in the blend file, destination directory is selected, not single file:


* For each armature, all armature bound meshes are exported to single md5mesh file. 
> Filenames Armature_name.md5mesh

* For each armature related animation md5anim file will be created.
> Filenames Armature_name.Animation_name.md5anim 

**Installation**

1. Download the script.
2. Open Blender
3. Navigate to File->User Preferences
4. Select 'Install from File...' from down bellow and locate the io_export_md5_obj.py and hit 'Install from file...'
5. Use search on the left to locate 'Export MD5 format'
6. Select the check box on the right to enable the addon
7. Hit 'Save User Settings'

**Instructions**


With the script enabled, you can use it via the File>Export menu as MD5 Mesh and Anim (.md5mesh .md5anim). Selecting that menu entry will open Blender's file browser where you can select destination directory and adjust the script's options. The script provides a few options for controlling the export, currently they are:

- Scale: Exported objects are scaled from blender by multiplying with this value. Default=1.00

