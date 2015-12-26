'''
io_export_md5_obj Blender plugin to extract MD5 objects from .blend files
Copyright (C) 2015 Mikko Kortelainen <mikko.kortelainen@fail-safe.net>

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of  MERCHANTABILITY or FITNESS FOR
A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program.  If not, see <http://www.gnu.org/licenses/>.
'''

'''
This script is based on ideas found on
Blender MD5 export 2.63 by keless.
It can be found on http://www.katsbits.com/tools/
'''

import os
import sys
import re

import mathutils
import bpy
from bpy.props import StringProperty, FloatProperty

import getopt
import traceback

bl_info = {
    "name": "Export MD5 format (.md5mesh, .md5anim)",
    "author": "Mikko Kortelainen <mikko.kortelainen@fail-safe.net>"
    " / OpenTechEngine",
    "version": (1, 0, 0),
    "blender": (2, 6, 3),
    "api": 31847,
    "location": "File > Export > Skeletal Mesh/Animation Data"
    " (.md5mesh/.md5anim)",
    "description": "Exports MD5 Format (.md5mesh, .md5anim)",
    "warning": "",
    "wiki_url": "https://github.com/OpenTechEngine/modWiki",
    "tracker_url": "https://github.com/OpenTechEngine/ModelingTools",
    "category": "Import-Export"
}


class Typewriter(object):

    def print_info(message):
        print ("INFO: " + message)

    def print_warn(message):
        print ("WARNING: " + message)

    def print_error(message):
        print ("ERROR: " + message)

    info = print_info
    warn = print_warn
    error = print_error


class MD5Math(object):

    def getminmax(listofpoints):
        if len(listofpoints[0]) == 0:
            return ([0, 0, 0], [0, 0, 0])
        min = [listofpoints[0][0], listofpoints[1][0], listofpoints[2][0]]
        max = [listofpoints[0][0], listofpoints[1][0], listofpoints[2][0]]
        if len(listofpoints[0]) > 1:
            for i in range(1, len(listofpoints[0])):
                if listofpoints[i][0] > max[0]:
                    max[0] = listofpoints[i][0]
                if listofpoints[i][1] > max[1]:
                    max[1] = listofpoints[i][1]
                if listofpoints[i][2] > max[2]:
                    max[2] = listofpoints[i][2]
                if listofpoints[i][0] < min[0]:
                    min[0] = listofpoints[i][0]
                if listofpoints[i][1] < min[1]:
                    min[1] = listofpoints[i][1]
                if listofpoints[i][2] < min[2]:
                    min[2] = listofpoints[i][2]
        return (min, max)

################################################################################
#
# MD5File object, should be it's own module but that and blender don't cope
# http://tfc.duke.free.fr/coding/md5-specs-en.html
#


class MD5Format(object):

    def __init__(self, commandline):
        self._version = 10  # MD5 File version, hardcoded
        self._commandline = commandline  # commandline used to generate file


class MD5MeshFormat(MD5Format):

    class Joints(object):

        class _Joint(object):
            # "name" parent ( pos.x pos.y pos.z ) ( orient.x orient.y orient.z )

            def __init__(self, name, parent, pos_x, pos_y, pos_z, ori_x, ori_y, ori_z):
                self._name = name
                self._parent = parent
                self._pos_x = pos_x
                self._pos_y = pos_y
                self._pos_z = pos_z
                self._ori_x = ori_x
                self._ori_y = ori_y
                self._ori_z = ori_z

            def __str__(self):
                return "\t\"%s\" %i ( %f %f %f ) ( %f %f %f )\n" % \
                    (self._name, self._parent, self._pos_x, self._pos_y, self._pos_z, self._ori_x, self._ori_y, self._ori_z)

        def __init__(self):
            self._joints = []  # list of joints

        def __len__(self):
            return len(self._joints)

        def __str__(self):
            return "joints {\n%s}\n\n" % \
                ("".join([str(element) for element in self._joints]))

        def Joint(self, name, parent, pos_x, pos_y, pos_z, ori_x, ori_y, ori_z):
            created_joint = self._Joint(name, parent, pos_x, pos_y, pos_z, ori_x, ori_y, ori_z)
            self._joints.append(created_joint)
            return created_joint

    class _Mesh(object):

        class _Vert(object):
            # vert vertIndex ( s t ) startWeight countWeight

            def __init__(self, index, texture_x, texture_y, weightstart, weightcount):
                self.index = index
                self._texture_x = texture_x  # u, s
                self._texture_y = texture_y  # v, t
                self._weightstart = weightstart
                self._weightcount = weightcount

            def __str__(self):
                return "\tvert %i ( %f %f ) %i %i\n" % \
                    (self.index, self._texture_x, self._texture_y, self._weightstart, self._weightcount)

        class _Tri(object):
            # tri triIndex vertIndex[0] vertIndex[1] vertIndex[2]

            def __init__(self, index, vert1, vert2, vert3):
                self.index = index
                self._vert1 = vert1
                self._vert2 = vert2
                self._vert3 = vert3

            def __str__(self):
                return "\ttri %i %i %i %i\n" % \
                    (self.index, self._vert1, self._vert2, self._vert3)

        class _Weight(object):
            # weight weightIndex joint bias ( pos.x pos.y pos.z )

            def __init__(self, index, joint, bias, pos_x, pos_y, pos_z):
                self.index = index
                self._rel_joint = joint
                self._bias = bias
                self._pos_x = pos_x
                self._pos_y = pos_y
                self._pos_z = pos_z

            def __str__(self):
                return "\tweight %i %i %f ( %f %f %f )\n" % \
                    (self.index, self._rel_joint, self._bias, self._pos_x, self._pos_y, self._pos_z)

        def __init__(self, shader):
            self._shader = shader
            self._verts = []  # list of verts
            self._tris = []  # list of tris
            self._weights = []  # list of weights

        def __str__(self):
            return "mesh {\n\tshader \"%s\"\n\n\tnumverts %i\n%s\n\tnumtris %i\n%s\n\tnumweights %i\n%s}\n" % \
                (self._shader,
                 len(self._verts),
                 "".join([str(element) for element in self._verts]),
                 len(self._tris),
                 "".join([str(element) for element in self._tris]),
                 len(self._weights),
                 "".join([str(element) for element in self._weights]))

        def Vert(self, texture_x, texture_y, weightstart, weightcount):
            created_vert = self._Vert(len(self._verts), texture_x, texture_y, weightstart, weightcount)
            self._verts.append(created_vert)
            return created_vert

        def Tri(self, vert1, vert2, vert3):
            created_tri = self._Tri(len(self._tris), vert1, vert2, vert3)
            self._tris.append(created_tri)
            return created_tri

        def Weight(self, joint, bias, pos_x, pos_y, pos_z):
            created_weight = self._Weight(len(self._weights), joint, bias, pos_x, pos_y, pos_z)
            self._weights.append(created_weight)
            return created_weight

    def Mesh(self, shader):
        created_mesh = self._Mesh(shader)
        self._meshes.append(created_mesh)
        return created_mesh

    def __init__(self, commandline):
        super().__init__(commandline)
        self.Joints = self.Joints()  # joints
        self._meshes = []  # list of meshes

    def __str__(self):
        return "MD5Version %i\ncommandline \"%s\"\n\nnumJoints %i\nnumMeshes %i\n\n%s%s" % \
            (self._version, self._commandline, len(self.Joints), len(self._meshes),
             str(self.Joints),
             "".join([str(element) for element in self._meshes]))


class MD5AnimFormat(MD5Format):

    class Hierarchy(object):

        class _Joint(object):
            # name parent flags startIndex

            def __init__(self, name, parent, flags, startindex):
                self._name = name
                self._parent = parent
                self._flags = flags
                self._startindex = startindex

            def __str__(self):
                return "\t\"%s\" %s %i %i\n" % \
                    (self._name, self._parent, self._flags, self._startindex)

        def __init__(self):
            self._joints = []  # joint hierarchy

        def __str__(self):
            return "hierarchy {\n%s}\n" % \
                ("".join([str(element) for element in self._joints]))

        def __len__(self):
            return len(self._joints)

        def Joint(self, name, parent, flags, startindex):
            created_joint = self._Joint(name, parent, flags, startindex)
            self._joints.append(created_joint)
            return created_joint

    class Bounds(object):

        class _Bound(object):
            # ( min.x min.y min.z ) ( max.x max.y max.z )

            def __init__(self, min_x, min_y, min_z, max_x, max_y, max_z):
                self._min_x = min_x
                self._min_y = min_y
                self._min_z = min_z
                self._max_x = max_x
                self._max_y = max_y
                self._max_z = max_z

            def __str__(self):
                return "\t( %f %f %f ) ( %f %f %f )\n" % \
                    (self._min_x, self._min_y, self._min_z, self._max_x, self._max_y, self._max_z)

        def __init__(self):
            self._bounds = []  # bounding boxes for each frame

        def __str__(self):
            return "bounds {\n%s}\n\n" % \
                ("".join(str(element) for element in self._bounds))

        def Bound(self, min_x, min_y, min_z, max_x, max_y, max_z):
            created_bound = self._Bound(min_x, min_y, min_z, max_x, max_y, max_z)
            self._bounds.append(created_bound)
            return created_bound

    class BaseFrame(object):

        class _BasePosition(object):
            # ( pos.x pos.y pos.z ) ( orient.x orient.y orient.z )

            def __init__(self, pos_x, pos_y, pos_z, ori_x, ori_y, ori_z):
                self._pos_x = pos_x
                self._pos_y = pos_y
                self._pos_z = pos_z
                self._ori_x = ori_x
                self._ori_y = ori_y
                self._ori_z = ori_z

            def __str__(self):
                return "\t( %f %f %f ) ( %f %f %f )\n" % \
                    (self._pos_x, self._pos_y, self._pos_z, self._ori_x, self._ori_y, self._ori_z)

        def __init__(self):
            self._basepositions = []  # position and orientation of bones

        def __str__(self):
            return "baseframe {\n%s}\n\n" % \
                ("".join([str(element) for element in self._basepositions]))

        def __len__(self):
            return len(self._basepositions)

        def BasePosition(self, pos_x, pos_y, pos_z, ori_x, ori_y, ori_z):
            created_baseposition = self._BasePosition(pos_x, pos_y, pos_z, ori_x, ori_y, ori_z)
            self._basepositions.append(created_baseposition)
            return created_baseposition

    class _Frame(object):

        class _FramePosition(object):
            # <float> <float> <float> <float> <float> <float>

            def __init__(self, pos_x, pos_y, pos_z, ori_x, ori_y, ori_z):
                self._pos_x = pos_x
                self._pos_y = pos_y
                self._pos_z = pos_z
                self._ori_x = ori_x
                self._ori_y = ori_y
                self._ori_z = ori_z

            def __str__(self):
                return "\t%f %f %f %f %f %f\n" % \
                    (self._pos_x, self._pos_y, self._pos_z, self._ori_x, self._ori_y, self._ori_z)

        def __init__(self, frameindex):
            self._frameindex = frameindex
            self._framepositions = []  # bone positions for frame

        def __str__(self):
            return "frame %i {\n%s}\n\n" % \
                (self._frameindex, "".join([str(element) for element in self._framepositions]))

        def FramePosition(self, pos_x, pos_y, pos_z, ori_x, ori_y, ori_z):
            created_frameposition = self._FramePosition(pos_x, pos_y, pos_z, ori_x, ori_y, ori_z)
            self._framepositions.append(created_frameposition)
            return created_frameposition

    def __init__(self, commandline, framerate):
        super().__init__(commandline)
        self._framerate = framerate  # frame rate
        self.Hierarchy = self.Hierarchy()
        self.Bounds = self.Bounds()
        self.BaseFrame = self.BaseFrame()
        self._frames = []  # list of frames

    def __str__(self):
        # TODO hardcoded 6 animated components per bone because animation extractor uses 63
        return "MD5Version %i\ncommandline \"%s\"\n\nnumFrames %i\nnumJoints %i\nframeRate %i\nnumAnimatedComponents %i\n\n%s%s%s%s" % \
            (self._version, self._commandline, len(self._frames), len(self.Hierarchy), self._framerate, len(self.BaseFrame) * 6,
             str(self.Hierarchy),
             str(self.Bounds),
             str(self.BaseFrame),
             "".join([str(element) for element in self._frames]))

    def Frame(self):
        created_frame = self._Frame(len(self._frames))
        self._frames.append(created_frame)
        return created_frame

# unit test for MD5MeshFormat


class MD5MeshFormatTest(object):

    def __init__(self):
        a = MD5MeshFormat('commandline from inline code')
        a.Joints.Joint('name', -1, -0.01, -0.01, -0.01, -0.01, -0.01, -0.01)
        new_mesh = a.Mesh("shader")
        new_weight = new_mesh.Weight(-1, 1, 4, 5, 6)
        new_vert1 = new_mesh.Vert(0, 0, new_weight.index, 1)
        new_vert2 = new_mesh.Vert(0, 100, new_weight.index, 1)
        new_vert3 = new_mesh.Vert(100, 0, new_weight.index, 1)
        new_mesh.Tri(new_vert1.index, new_vert2.index, new_vert3.index)

        print(a)

# unit test for MD5AnimFormat


class MD5AnimFormatTest(object):

    def __init__(self):
        b = MD5AnimFormat('commandline from inline code', 24)
        b.Hierarchy.Joint('Legs', -1, 63, 0)
        b.Bounds.Bound(1, 2, 3, 4, 5, 6)
        b.BaseFrame.BasePosition(7, 8, 9, 1, 2, 3)
        new_frame = b.Frame()
        new_frame.FramePosition(7, 6, 5, 4, 3, 2)
        print(b)

################################################################################


class BlenderExtractor(object):

    class _StructureExtractor(object):
        # operates only with bpy.data

        class _ArmatureRelated(object):

            def __init__(self, armature):
                self.armature = armature
                self.meshes = []
                self.animations = []

            def __len__(self):
                return len(self.meshes)

            def AddMesh(self, blenderobject):
                self.meshes.append(blenderobject)

            def AddAnim(self, blenderobject):
                self.animations.append(blenderobject)

        def populate_animations(self, structure_group):

            # we roll over all animations in blender and
            # check if they have our bones in data_path
            # in case yes, this is valid for our armature and will be added to list

            # dict of our bones
            armature_bones = structure_group.armature.data.bones

            # TODO hack, here, however data_path contains a string of kind:
            # 'pose.bones["Torso"].location'
            data_path_matcher = re.compile('pose.bones\["(.*)"\]')

            for animation in bpy.data.actions:
                for fcurve_index in range(len(animation.fcurves)):
                    data_path = animation.fcurves[fcurve_index].data_path
                    match_groups = data_path_matcher.match(data_path)
                    if match_groups is not None:
                        data_path_bone = match_groups.group(1)
                        if armature_bones.get(data_path_bone):
                            structure_group.AddAnim(animation)
                            # One match is enough to indicate it belongs for this armature
                            break
            #print("animations for this armature "+str(structure_group.animations))

        def armatureless_check(self):
            # objects without armature
            for blender_object in bpy.data.objects:
                if (blender_object.type == 'MESH'):
                    # we search for objects with no related armature
                    armature = 0
                    for group in self.groups:
                        for grouped_mesh in group.meshes:
                            if grouped_mesh == blender_object:
                                # group was found
                                armature = 1
                                break
                    # not found
                    if armature == 0:
                        # we should call .lwo or .ase exporter for these
                        Typewriter.warn("Non-armature mesh found: " + blender_object.name)
                        new_group = self._ArmatureRelated(None)
                        new_group.AddMesh(blender_object)
                        # MD5 cant do these
                        # self.groups.append(new_group)

        def __init__(self):
            # structure lookup can only be done via armature, as it seems to be singly linked
            self.groups = []

            # armature and child objects
            for blender_object in bpy.data.objects:
                if (blender_object.type == 'ARMATURE') and (len(blender_object.children) > 0):
                    new_group = self._ArmatureRelated(blender_object)

                    # all meshes on this armature are added so they can be in same MD5MeshFormat object
                    for child in blender_object.children:
                        if (child.type == 'MESH'):
                            new_group.AddMesh(child)

                    # type check can leave them empty, aka no meshes on this armature
                    if len(new_group) > 0:
                        self.populate_animations(new_group)
                        self.groups.append(new_group)

            # catch all MESH objects not belonging to armature and warn
            self.armatureless_check()

    class _MeshDataExtractor(object):

        class _JointExtractor(object):

            def create_joint(self, name, matrix, parent_id):
                # local variable for transformations
                self.matrix = matrix

                pos1 = self.matrix.col[3][0]
                pos2 = self.matrix.col[3][1]
                pos3 = self.matrix.col[3][2]

                bquat = self.matrix.to_quaternion()
                bquat.normalize()
                qx = bquat.x
                qy = bquat.y
                qz = bquat.z
                if bquat.w > 0:
                    qx = -qx
                    qy = -qy
                    qz = -qz

                self.format_object.Joints.Joint(name, parent_id, pos1 * self.scale, pos2 * self.scale, pos3 * self.scale, qx, qy, qz)

            # recursive bone extractor function
            def recurse_bone(self, bone, parent=None, parent_id=None):

                # only recurse to attached bones
                if (parent and not bone.parent.name == parent.name):
                    return
                elif parent is None:
                    parent_id = -1

                our_id = self._joint_index
                self._joint_index = self._joint_index + 1

                bone_matrix = self.armature.matrix_world * bone.matrix_local

                self.create_joint(bone.name, bone_matrix, parent_id)
                self._bone_dict[bone.name] = [our_id, bone_matrix]

                # attached bones
                if(bone.children):
                    for child in bone.children:
                        self.recurse_bone(child, bone, our_id)

            def get_bone_dict(self):
                return self._bone_dict

            def __init__(self, format_object, armature, scale):

                self.format_object = format_object
                self.armature = armature
                self.scale = scale
                self._joint_index = 0
                # bone dictionary is for weight calculations to find bone by name
                self._bone_dict = {}

                for bone in self.armature.data.bones:
                    # search root bone
                    if not bone.parent:
                        #Typewriter.info( "Armature: "+self.armature.name+" root bone: " + bone.name )
                        self.recurse_bone(bone)

        class _MeshExtractor(object):

            class _TriExtractor(object):

                class _VertExtractor(object):

                    class _WeightExtractor(object):

                        def _create_weight(self, bone_name, bias, pos_x, pos_y, pos_z, scale):
                            bone_index = self._bone_dict[bone_name][0]

                            bone_matrix = self._bone_dict[bone_name][1]
                            inv_trans_bone_matrix = bone_matrix.transposed().inverted()
                            trl_pos_x, trl_pos_y, trl_pos_z = mathutils.Vector((pos_x, pos_y, pos_z)) * inv_trans_bone_matrix
                            new_weight = self._new_mesh.Weight(bone_index, bias, trl_pos_x * scale, trl_pos_y * scale, trl_pos_z * scale)

                            if self.firstweight is None:
                                self.firstweight = new_weight.index

                            self.weightcount = self.weightcount + 1

                        def __init__(self, new_mesh, blender_mesh, mesh_vertex, vertex_index, bone_dict, scale):
                            self._new_mesh = new_mesh
                            self._blender_mesh = blender_mesh
                            self._mesh_vertex = mesh_vertex
                            self._vertex_index = vertex_index
                            self._bone_dict = bone_dict
                            self.firstweight = None
                            self.weightcount = 0

                            # for all vertices, find weights
                            influences = []
                            vertice_groups = self._blender_mesh.data.vertices[self._vertex_index].groups
                            for j in range(len(vertice_groups)):
                                bonename = self._blender_mesh.vertex_groups[self._mesh_vertex.groups[j].group].name
                                weight = self._mesh_vertex.groups[j].weight
                                inf = [bonename, weight]
                                influences.append(inf)
                            if not influences:
                                Typewriter.warn("There is a vertex without attachment to a bone in mesh: no info here atm ")

                            # total of all weights
                            sum = 0.0
                            for bone_name, weight in influences:
                                sum += weight

                            loc_vector = mesh_vertex.co

                            w_matrix = self._blender_mesh.matrix_world
                            coord = loc_vector * w_matrix  # verify this

                            for bone_name, weight in influences:
                                if sum != 0:
                                    # influence_by_bone should total 1.0
                                    influence_by_bone = weight / sum
                                    self._create_weight(bone_name, influence_by_bone, coord[0], coord[1], coord[2], scale)
                                else:
                                    # we have a vertex that is probably not skinned. export anyway with full weight
                                    self._create_weight(bone_name, weight, coord[0], coord[1], coord[2], scale)
                                    Typewriter.warn("Vertex without weight paint: %i" % vertex_index)

                    class _TempVert(object):

                        def __init__(self, texture_x, texture_y, loc_z):
                            self.texture_x = texture_x
                            self.texture_y = texture_y
                            self.loc_z = loc_z
                            self.md5index = None

                    def _temp_vert_uniq(self, vertex_index, temp_vert):
                        try:
                            vertex_instances = self._vertices[vertex_index]
                        except KeyError:
                            # definitely uniq, we did not even find a vertex_index
                            return True

                        for vertex in vertex_instances:
                            if (vertex.texture_y == temp_vert.texture_y) and \
                               (vertex.texture_x == temp_vert.texture_x) and \
                               (vertex.loc_z == temp_vert.loc_z):
                                return False
                        return True

                    def _temp_vert_get(self, vertex_index, temp_vert):
                        try:
                            vertex_instances = self._vertices[vertex_index]
                        except KeyError:
                            #  we did not even find a vertex_index
                            return None

                        for vertex in vertex_instances:
                            if (vertex.texture_y == temp_vert.texture_y) and \
                               (vertex.texture_x == temp_vert.texture_x) and \
                               (vertex.loc_z == temp_vert.loc_z):
                                return vertex
                        return None

                    def _temp_vert_add(self, vertex_index, temp_vert):
                        try:
                            vertex_instances = self._vertices[vertex_index]
                        except KeyError:
                            self._vertices[vertex_index] = []
                        vertex_instances = self._vertices[vertex_index]
                        vertex_instances.append(temp_vert)

                    def extract(self, polygon):
                        polygons_vertices = []
                        for loop_index in polygon.loop_indices:
                            vertex_index = self._blender_mesh.data.loops[loop_index].vertex_index

                            # print("vertex: %d" % vertex_index) # development printout

                            vertex = self._blender_mesh.data.vertices[vertex_index]
                            loc_vector = vertex.co

                            try:
                                # vertex has uv
                                loc_vector = self._blender_mesh.data.uv_layers.active.data[loop_index].uv
                                # print("UV: %r" % loc_vector) # development printout
                            except AttributeError:
                                # vertex does not have uv
                                Typewriter.warn("vertex without uv: %i" % vertex_index)

                            # print(loc_vector)
                            temp_vert = self._TempVert(loc_vector[0], loc_vector[1], loc_vector[2])

                            if self._temp_vert_uniq(vertex_index, temp_vert):
                                # if unique, create new md5 vertex
                                w_matrix = self._blender_mesh.matrix_world
                                coord = loc_vector * w_matrix  # verify this

                                weightextractor = self._WeightExtractor(self._new_mesh, self._blender_mesh, vertex, vertex_index, self._bone_dict, self._scale)
                                weightstart = weightextractor.firstweight
                                weightcount = weightextractor.weightcount

                                md5vert = self._new_mesh.Vert(coord[0], coord[1], weightstart, weightcount)
                                temp_vert.md5index = md5vert.index
                                self._temp_vert_add(vertex_index, temp_vert)
                            else:
                                # existing md5 vertex found, use it
                                temp_vert = self._temp_vert_get(vertex_index, temp_vert)

                            # add this to list of polygon faces to be returned to form a tri
                            polygons_vertices.append(temp_vert.md5index)

                        return polygons_vertices

                    def __init__(self, new_mesh, blender_mesh, bone_dict, scale):
                        self._new_mesh = new_mesh
                        self._blender_mesh = blender_mesh
                        self._bone_dict = bone_dict
                        self._scale = scale
                        # key vertex_index of mesh
                        # value list type, containing _TempVert objects
                        # this allows us to see if we already have this
                        # md5 vert created
                        self._vertices = {}

                def polygon_validate(self, polygon, material_index):
                    # a face has to have at least 3 vertices.
                    if (len(polygon.vertices) < 3) or \
                       (polygon.vertices[0] == polygon.vertices[1]) or \
                       (polygon.vertices[0] == polygon.vertices[2]) or \
                       (polygon.vertices[1] == polygon.vertices[2]):
                        Typewriter.warn("Degenerate polygon: %i" % polygon.index)
                        return False
                    # check same material_index as rest of the mesh
                    elif polygon.material_index != material_index:
                        Typewriter.warn("Invalid material on polygon: %i" % polygon.index)
                        # we skip here, however we should not, but for the time being..
                        return True
                    else:
                        return True

                def __init__(self, new_mesh, blender_mesh, bone_dict, scale):
                    self._new_mesh = new_mesh
                    self._blender_mesh = blender_mesh
                    self._bone_dict = bone_dict
                    self._vertextractor = self._VertExtractor(self._new_mesh, self._blender_mesh, self._bone_dict, scale)

                    for polygon in self._blender_mesh.data.polygons:
                        if self.polygon_validate(polygon, self._blender_mesh.data.materials[0].name):
                            # polygon vertice extractor
                            face_vertices = self._vertextractor.extract(polygon)

                            # Split faces with more than 3 vertices
                            for i in range(1, polygon.loop_total - 1):
                                # tri
                                self._new_mesh.Tri(face_vertices[0], face_vertices[i + 1], face_vertices[i])

            def __init__(self, format_object, blender_mesh, export_scale, bone_dict):
                self._format_object = format_object
                self._blender_mesh = blender_mesh
                self._export_scale = export_scale
                self._bone_dict = bone_dict
                self._new_mesh = None

                # Typewriter.info( "Processing mesh: "+ self._blender_mesh.name )

                if len(self._blender_mesh.data.materials) > 0:
                    self._new_mesh = self._format_object.Mesh(self._blender_mesh.data.materials[0].name)
                    # tri extractor runs over all tris in mesh
                    self._TriExtractor(self._new_mesh, self._blender_mesh, self._bone_dict, self._export_scale)
                else:
                    Typewriter.error("No material found for mesh: " + self._blender_mesh.name + " skipping.")

        def __init__(self, format_object, structure_group, scale):
            # Typewriter.info(str(structure_group.armature)) # development printout
            # Typewriter.info(str(structure_group.meshes)) # development printout

            if (structure_group.armature is not None):
                joint_extractor = self._JointExtractor(format_object, structure_group.armature, scale)
                bone_dict = joint_extractor.get_bone_dict()

            # group can not exist without a mesh, not checking
            for mesh in structure_group.meshes:
                self._MeshExtractor(format_object, mesh, scale, bone_dict)

    class _AnimExtractor(object):

        class _HierarchyBaseExtractor(object):
            # does basically the same as in mesh armature extraction
            # extracts hierarchy and baseframe

            def create_baseframe(self, matrix):
                # local variable for transformations
                self.matrix = matrix

                pos1 = self.matrix.col[3][0]
                pos2 = self.matrix.col[3][1]
                pos3 = self.matrix.col[3][2]

                bquat = self.matrix.to_quaternion()
                bquat.normalize()
                qx = bquat.x
                qy = bquat.y
                qz = bquat.z
                if bquat.w > 0:
                    qx = -qx
                    qy = -qy
                    qz = -qz

                self.format_object.BaseFrame.BasePosition(pos1 * self._scale, pos2 * self._scale, pos3 * self._scale, qx, qy, qz)

            def recurse_bone(self, bone, parent=None, parent_id=None):
                # only recurse to attached bones
                if (parent and not bone.parent.name == parent.name):
                    return
                elif parent is None:
                    parent_id = -1

                our_id = self._joint_index
                self._joint_index = self._joint_index + 1

                bone_matrix = self.armature.matrix_world * bone.matrix_local

                # TODO 63 means this animation involves all possible
                # animation actions, loc rot scale, you name it?
                # also startindex 6 means all are recalculated:
                # this can be done with a peek into animation
                # and checking for location/rotation/scale fcurves
                # on our bone
                '''
                "name"   parent flags startIndex
                flags variable description: starting from the right, the frist three
                bits are for the position vector and the next three for the orientation
                quaternion. If a bit is set, then you have to replace the corresponding
                (x, y, z) component by a value from the frame's data. Which value? This
                is given by the startIndex. You begin at the startIndex in the frame's
                data array and increment the position each time you have to replace a
                value to a component.
                '''
                if parent:
                    self._arm_bone_dict[bone.name] = parent
                else:
                    self._arm_bone_dict[bone.name] = None

                self.format_object.Hierarchy.Joint(bone.name, parent_id, 63, self._start_index)
                self._start_index = self._start_index + 6
                self.create_baseframe(bone_matrix)

                # attached bones
                if(bone.children):
                    for child in bone.children:
                        self.recurse_bone(child, bone, our_id)

            def get_arm_bone_dict(self):
                return self._arm_bone_dict

            def __init__(self, format_object, armature, scale):
                self.format_object = format_object
                self.armature = armature
                self._joint_index = 0
                self._start_index = 0
                self._scale = scale
                self._arm_bone_dict = {}

                for bone in self.armature.data.bones:
                    # search root bone
                    if not bone.parent:
                        # Typewriter.info( "Armature animation: "+self.armature.name+" root bone: " + bone.name )
                        self.recurse_bone(bone)

        class _BoundExtractor(object):
            # TODO performance seems suboptimal

            def __init__(self, format_object, meshes, animation, scale):
                scene = bpy.context.scene
                first_frame = int(animation.frame_range[0])
                last_frame = int(animation.frame_range[1])

                for i in range(first_frame, last_frame + 1):
                    corners = []
                    scene.frame_set(i)

                    for mesh in meshes:
                        (lx, ly, lz) = mesh.location
                        bbox = mesh.bound_box
                        matrix = mathutils.Matrix([[1.0,  0.0, 0.0, 0.0],
                                                   [0.0,  1.0, 0.0, 0.0],
                                                   [0.0,  1.0, 1.0, 0.0],
                                                   [0.0,  0.0, 0.0, 1.0],
                                                   ])
                        for v in bbox:
                            vecp = mathutils.Vector((v[0], v[1], v[2]))
                            corners.append(vecp * matrix)

                    (min, max) = MD5Math.getminmax(corners)
                    format_object.Bounds.Bound(min[0] * scale, min[1] * scale, min[2] * scale, max[0] * scale, max[1] * scale, max[2] * scale)

        class _FrameExtractor(object):

            def __init__(self, format_object, armature, animation, arm_bone_dict, scale):
                self._format_object = format_object
                self._armature = armature
                self._animation = animation
                self._arm_bone_dict = arm_bone_dict

                first_frame = int(self._animation.frame_range[0])
                last_frame = int(self._animation.frame_range[1])

                frame_index = first_frame

                for i in range(first_frame, last_frame + 1):
                    new_frame = self._format_object.Frame()

                    bpy.context.scene.frame_set(frame_index)

                    pose = self._armature.pose
                    for bonename in self._armature.data.bones.keys():
                        posebonemat = mathutils.Matrix(pose.bones[bonename].matrix)  # transformation of this PoseBone including constraints

                        if self._arm_bone_dict[bonename]:  # need parent space-matrix
                            # transformation of this PoseBone including constraints
                            parentposemat = mathutils.Matrix(pose.bones[self._arm_bone_dict[bonename].name].matrix)
                            parentposemat.invert()
                            posebonemat = parentposemat * posebonemat
                        else:
                            posebonemat = self._armature.matrix_world * posebonemat

                        loc_x = posebonemat.col[3][0]
                        loc_y = posebonemat.col[3][1]
                        loc_z = posebonemat.col[3][2]
                        rot = posebonemat.to_quaternion()
                        rot.normalize()

                        if rot.w > 0:
                            qx, qy, qz = -rot.x, -rot.y, -rot.z

                        new_frame.FramePosition(loc_x * scale, loc_y * scale, loc_z * scale, qx, qy, qz)

                    # next frame
                    frame_index = frame_index + 1

        def __init__(self, format_object, structure_group, animation, scale):
            self._hierarchyextractor = self._HierarchyBaseExtractor(format_object, structure_group.armature, scale)
            self._arm_bone_dict = self._hierarchyextractor.get_arm_bone_dict()
            self._BoundExtractor(format_object, structure_group.meshes, animation, scale)
            self._FrameExtractor(format_object, structure_group.armature, animation, self._arm_bone_dict, scale)

    def __init__(self, path, scale=1):
        # extracting structure: armature and meshes that belong to it
        self.structure = self._StructureExtractor()

        if len(self.structure.groups) > 0:
            for structure_group in self.structure.groups:
                # md5mesh
                mesh_format_object = MD5MeshFormat('testing extractor')
                self._MeshDataExtractor(mesh_format_object, structure_group, scale)
                # print(str(format_object))
                file = open(path + "/" + structure_group.armature.name + '.md5mesh', 'w')
                file.write(str(mesh_format_object))
                file.close()

                # md5anims
                # TODO these are quite hacks dirty for using context
                # it should be all fixed to access fcurves and data instead
                # also the Extractor classes use context
                '''
                bpy.context.object.animation_data.action.fcurves[0].evaluate(5)
                #~ -3.2612
                bpy.context.object.animation_data.action.fcurves[0].keyframe_points[0].co
                #~ Vector((1.0, -3.2612))
                bpy.context.object.animation_data.action.fcurves[0].keyframe_points[0].interpolation
                #~ 'BEZIER'
                '''
                if len(structure_group.animations) > 0:
                    for animation in structure_group.animations:
                        # set animation for context
                        structure_group.armature.animation_data.action = animation
                        frames_per_second = bpy.context.scene.render.fps

                        anim_format_object = MD5AnimFormat('testing extractor', frames_per_second)
                        self._AnimExtractor(anim_format_object, structure_group, animation, scale)

                        file = open(path + "/" + structure_group.armature.name + '.' + animation.name + '.md5anim', 'w')
                        file.write(str(anim_format_object))
                        file.close()
                else:
                    Typewriter.warn('No animations to export. Create at least idle animation.')
        else:
            Typewriter.error('No valid meshes to export')

################################################################################

# export class registration and interface


class ExportMD5(bpy.types.Operator):

    '''Export to MD5 Mesh and Anim (.md5mesh .md5anim)'''
    bl_idname = "export.md5_obj"
    bl_label = 'MD5Export'

    logenum = [("console", "Console", "log to console"),
               ("append", "Append", "append to log file"),
               ("overwrite", "Overwrite", "overwrite log file")]

    directory = StringProperty(subtype='DIR_PATH', name="", description="Export target directory", maxlen=1024, default="")
    scale = FloatProperty(name="Scale", description="Scale all objects from world origin (0,0,0)", min=0.001, max=1000.0, default=1.0, precision=6)

    def setup_typewriter(self):
        def print_info(message):
            self.report({'INFO'}, message)

        def print_warn(message):
            self.report({'WARNING'}, message)

        def print_error(message):
            self.report({'ERROR'}, message)

        Typewriter.info = print_info
        Typewriter.warn = print_warn
        Typewriter.error = print_error

    def execute(self, context):
        self.setup_typewriter()

        BlenderExtractor(self.properties.directory, self.properties.scale)
        Typewriter.info("Export complete")
        return {'FINISHED'}

    def invoke(self, context, event):
        WindowManager = context.window_manager
        WindowManager.fileselect_add(self)
        return {"RUNNING_MODAL"}


class console(object):
    # blender uses it's own, can't override it so we set it only here

    def exception_handler(self, type, value, trace):
        Typewriter.error(''.join(traceback.format_tb(trace)))
        Typewriter.error(type.__name__ + ": " + str(value))

    def get_parameters(self):
        accepted_arguments = ["output-dir=", "scale=", "help"]

        def print_executed_string():
            Typewriter.info("Executed string: " + " ".join(sys.argv))

        def usage():
            Typewriter.info('Usage: blender file.blend --background --python io_export_md5.py -- --arg1 val1 --arg2 val2')
            Typewriter.info("Available arguments")
            for argument in accepted_arguments:
                Typewriter.info("\t--" + argument)

        # check if '--' entered, arguments after that are for us
        dashes_at = 0
        i = 0
        for arg in sys.argv:
            if arg == "--":
                dashes_at = i + 1
            i = i + 1
        if dashes_at == 0:
            usage()

        # if no valid arguments entered, print usage
        try:
            opts, args = getopt.getopt(sys.argv[dashes_at:], "", accepted_arguments)
        except getopt.GetoptError as err:
            print_executed_string()
            Typewriter.error(str(err))
            usage()
            sys.exit(2)

        for opt, arg in opts:
            if opt == '--output-dir':
                self.output_dir = arg
                if os.access(self.output_dir, os.W_OK) is False:
                    print_executed_string()
                    Typewriter.error('Cannot write to directory: ' + self.output_dir)
                    sys.exit(2)
            if opt == '--scale':
                try:
                    self.scale = float(arg)
                except ValueError:
                    print_executed_string()
                    Typewriter.error("--scale expected float, received: " + arg)
                    sys.exit(2)
            if opt == '--help':
                usage()
                sys.exit(0)

    def __init__(self):
        self.output_dir = os.getcwd()
        self.scale = 1

        sys.excepthook = self.exception_handler
        self.get_parameters()

        BlenderExtractor(self.output_dir, self.scale)
        Typewriter.info("Export complete")
        sys.exit(0)


# blender gui module function
def menu_func(self, context):
    default_path = os.path.splitext(bpy.data.filepath)[0]
    self.layout.operator(ExportMD5.bl_idname, text="MD5 Mesh and Anim (.md5mesh .md5anim)", icon='BLENDER').directory = default_path

# blender gui module register


def register():
    bpy.utils.register_module(__name__)
    bpy.types.INFO_MT_file_export.append(menu_func)

# blender gui module unregister


def unregister():
    bpy.utils.unregister_module(__name__)
    bpy.types.INFO_MT_file_export.remove(menu_func)

# running as external script
if __name__ == "__main__":
    # MD5MeshFormatTest()
    # MD5AnimFormatTest()
    console()
