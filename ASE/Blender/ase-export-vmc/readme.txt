From https://code.google.com/p/ase-export-vmc/

Introduction

REQUEST: This plugin has been used by several small game studios. If you have published a game using this script, let me know! Send me a private message on the UDK forums or on my Youtube channel with the name of your game and where it is available for purchase or download.

An ASE file is a small, human readable file format used for static meshes (a 3d model without any skeletal data) in various game engines. This script fully supports features such as smoothing groups, vertex painting, collision objects, and more. For video tutorials visit my Youtube Channel.

Donate with PayPal
Details

ASE Export Script MCampagnini / Richisbored Version 2.00

Videos

    Blender 2.63a and ASE 2.01
    Blender 2.59 (old) 

Installation

    Download and extract the ASE Export Script
    In Blender, File / User Preferences
    Go to the Add-On Tab
    Install Add-On, select the extracted python script
    Click Save as Default to enable the script every time Blender loads (optional) 

You will find a new export option in File / Export called Ascii Scene Export (.ase). If you do not see the script in the export menu after following the installation instructions, open the Add-On tab and find the script under Import-Export: ASCII Scene Exporter. Be sure that it is enabled by checking the box next to the add-on name. Contact me on my website if you still have problems exporting.

Mesh Requirements

    All non-collision meshes must be UV unwrapped.
    All non-collision meshes must have a material applied. 

Optional

    Mesh may have more than one material.
    Mesh may have more than one UV texture and/or a lightmap uv.
    Mesh may be assigned as a collision object.
    Mesh may have more than one collision object to determine collision borders.
    Mesh may be assigned smoothing groups. 

Collision Objects

Assign a mesh as a collision object by prefixing each object name with UCX_NAME, where NAME can be anything you would like (UCX must be uppercase).

Smoothing Groups

Assign smoothing groups by edge selecting the border of the faces you want assigned to a smoothing group, press ctrl+e and mark sharp. Any face group separated with sharp edges will be assigned a smoothing group.

Vertex Painting

Apply vertex painting in Blender as normal, be sure that you have at least two uv texture slots. This is not a technical limitation, but due to time constraints I left the vertex painting code inside of a conditional that requires two uv textures. In order to view your vertex painting in UDK you will have to import and set up your materials correctly within UDK.

Change Log

Version 2.5 to 2.51

    Fixed material out of range error
    Fixed vertex painting (again...)
    Fixed scale option
    Performance updates 

Version 2.01a to 2.5

    Performance adjustments and code refactoring
    Use Submaterials toggle to give control on how to export materials
    Allow multiple materials to toggle whether a geometry node can have more than one material assigned
    The addition of these new features allow the ASE file to be read by the Id Tech 4 engine and other technologies 

Version 2.01 to 2.01a

    Fixed my vertex painting fix (doh!) 

Version 2.00 to 2.01

    Fixed scale bug that prevented proper scaling when exporting
    Fixed vertex painting MeshLoopColor object has no attribute color1
    Made default scale 1:1 (1 foot in Blender is 1 foot in UDK, depending on Blender grid setup)
    Random Cleanup 

Version 1.41 to 2.00

    Upgraded to work with Blender 2.63a
    Uses the new tessfaces / polygons api commands
    Added the ability to check which transformations to apply during export (rotation, location, scale)
    Removed origin repositioning during export, the origin will not change when you export
    Export Panel updates and changes 

Version 1.4 to 1.41

    Added an export option to recalculate normals 

Version 1.35 to 1.4

    Added vertex painting support. Requires two uv textures. 

Version 1.3 to 1.35

    Fixed a bug causing ugly output when over 32 smoothing groups were assigned
    Recoded a small portion of the script to remove complications when assigning more than 150 smoothing groups.
    Fixed a crash by adding in a bpy.ops.mesh.reveal before calculating mesh data. Any hidden parts of your mesh will be made visible before calculating mesh data. 
