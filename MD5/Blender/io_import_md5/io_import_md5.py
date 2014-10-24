"""
This script imports an id Tech 4 MD5MESH file to Blender.

Usage:
Run this script from "File->Import" menu and then load the desired MD5MESH file.
"""

bl_info = {
	'name': 'Import id Tech 4 MESH Format (.md5mesh)',
	'author': 'Ali Scissons, Bob Holcomb, Thomas "der_ton" Hutter',
	'version': (0, 1, 0),
	"blender": (2, 6, 0),
	"api": 40791,
	'location': 'File > Import > id Tech 4 (.md5mesh)',
	'description': 'Import files in the id Tech 4 format (.md5mesh)',
	'category': 'Import-Export',
	}

__version__ = '.'.join([str(s) for s in bl_info['version']])

import os
import codecs
import math
import string
from math import sin, cos, radians, sqrt
import bpy
from bpy.types import Object
from mathutils import Vector, Matrix, Quaternion

def quaternion2matrix(q):
  xx = q[0] * q[0]
  yy = q[1] * q[1]
  zz = q[2] * q[2]
  xy = q[0] * q[1]
  xz = q[0] * q[2]
  yz = q[1] * q[2]
  wx = q[3] * q[0]
  wy = q[3] * q[1]
  wz = q[3] * q[2]
  return [[1.0 - 2.0 * (yy + zz),       2.0 * (xy + wz),       2.0 * (xz - wy), 0.0],
          [      2.0 * (xy - wz), 1.0 - 2.0 * (xx + zz),       2.0 * (yz + wx), 0.0],
          [      2.0 * (xz + wy),       2.0 * (yz - wx), 1.0 - 2.0 * (xx + yy), 0.0],
          [0.0                  , 0.0                  , 0.0                  , 1.0]]

def vector_by_matrix(p, m):
  return [p[0] * m[0][0] + p[1] * m[1][0] + p[2] * m[2][0],
          p[0] * m[0][1] + p[1] * m[1][1] + p[2] * m[2][1],
          p[0] * m[0][2] + p[1] * m[1][2] + p[2] * m[2][2]]

def vector_dotproduct(v1, v2):
  return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2]

def vector_normalize(v):
  l = math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
  try:
    return v[0] / l, v[1] / l, v[2] / l
  except:
    return 1, 0, 0
          
class md5_vert:
	vert_index=0
	co=[]
	uvco=[]
	blend_index=0
	blend_count=0

	def __init__(self):
		self.vert_index=0
		self.co=[0.0]*3
		self.uvco=[0.0]*2
		self.blend_index=0
		self.blend_count=0

	def dump(self):
		print("vert index: ", self.vert_index)
		print("co: ", self.co)
		print("couv: ", self.couv)
		print("blend index: ", self.blend_index)
		print("belnd count: ", self.blend_count)

class md5_weight:
	weight_index=0
	bone_index=0
	bias=0.0
	weights=[]

	def __init__(self):
		self.weight_index=0
		self.bone_index=0
		self.bias=0.0
		self.weights=[0.0]*3

	def dump(self):
		print("weight index: ", self.weight_index)
		print("bone index: ", self.bone_index)
		print("bias: ", self.bias)
		print("weights: ", self.weights)
		
class md5_bone:
	bone_index=0
	name=""
	bindpos=[]
	bindmat=[]
	parent=""
	parent_index=0
	blenderbone=None
	roll=0

	def __init__(self):
		self.bone_index=0
		self.name=""
		self.bindpos=[0.0]*3
		self.bindmat=[None]*3  #is this how you initilize a 2d-array
		for i in range(3): self.bindmat[i] = [0.0]*3
		self.parent=""
		self.parent_index=0
		self.blenderbone=None

	def dump(self):
		print("bone index: ", self.bone_index)
		print("name: ", self.name)
		print("bind position: ", self.bindpos)
		print("bind translation matrix: ", self.bindmat)
		print("parent: ", self.parent)
		print("parent index: ", self.parent_index)
		print("blenderbone: ", self.blenderbone)

class md5_tri:
	tri_index=0
	vert_index=[]

	def __init__(self):
		self.tri_index=0;
		self.vert_index=[0]*3

	def dump(self):
		print("tri index: ", self.tri_index)
		print("vert index: ", self.vert_index)

class md5_mesh:
	name=""
	mesh_index=0
	verts=[]
	tris=[]
	weights=[]
	shader=""

	def __init__(self):
		self.mesh_index=0
		self.verts=[]
		self.tris=[]
		self.weights=[]
		self.shader=""

	def dump(self):
		print("mesh name: ", self.name)
		print("mesh index: ", self.mesh_index)
		print("verts: ", self.verts)
		print("tris: ", self.tris)
		print("weights: ", self.weights)
		print("shader: ", self.shader)

def load_md5(filepath):
	# do some error checking here
	file=open(filepath,"r")
	lines=file.readlines()
	file.close()

	md5_model = []
	md5_bones = []
	
	num_lines = len(lines)

	mesh_counter = 0
	for line_counter in range(0, num_lines):
		current_line = lines[line_counter]
		words = current_line.split()

		if words and words[0] == "numJoints":
			# print("Found a bunch of bones")
			num_bones = int(words[1])
			print("num_bones: ", num_bones)
		elif words and words[0] == "joints":
			for bone_counter in range(0, num_bones):
				# make a new bone
				md5_bones.append(md5_bone())
				# next line
				line_counter += 1
				current_line = lines[line_counter]
				words = current_line.split()
				# skip over blank lines
				while not words:
					line_counter += 1
					current_line = lines[line_counter]
					words = current_line.split()
				
				md5_bones[bone_counter].bone_index = bone_counter
				# get rid of the quotes on either side
				temp_name = str(words[0])
				temp_name = temp_name[1:-1]
				md5_bones[bone_counter].name = temp_name
				# print("found a bone: ", md5_bones[bone_counter].name)
				md5_bones[bone_counter].parent_index = int(words[1])
				if md5_bones[bone_counter].parent_index >= 0:
					md5_bones[bone_counter].parent = md5_bones[md5_bones[bone_counter].parent_index].name
				md5_bones[bone_counter].bindpos[0] = float(words[3])
				md5_bones[bone_counter].bindpos[1] = float(words[4])
				md5_bones[bone_counter].bindpos[2] = float(words[5])
				qx = float(words[8])
				qy = float(words[9])
				qz = float(words[10])
				qw = 1 - qx*qx - qy*qy - qz*qz
				if qw < 0:
					qw = 0
				else:
					qw = -sqrt(qw)
				md5_bones[bone_counter].bindmat = Quaternion((qw, qx, qy, qz)).to_matrix().to_4x4()
				#print("bindmat: ", md5_bones[bone_counter].bindmat)
				
		elif words and words[0] == "numMeshes":
			num_meshes = int(words[1])
			print("num_meshes: ", num_meshes)
				
		elif words and words[0] == "mesh":
			# create a new mesh and name it
			md5_model.append(md5_mesh())
			md5_model[mesh_counter].mesh_index = mesh_counter
			while (not words or (words and words[0] != "}")):
				line_counter += 1
				current_line = lines[line_counter]
				words = current_line.split()
				if words and words[0] == "//" and words[1] == "meshes:":
					md5_model[mesh_counter].name = words[2]
				if words and words[0] == "shader":
					# print("found a shader")
					temp_name = str(words[1])
					temp_name = temp_name[1:-1]
					md5_model[mesh_counter].shader = temp_name
				if words and words[0] == "vert":
					# print("found a vert")
					md5_model[mesh_counter].verts.append(md5_vert())
					vert_counter = len(md5_model[mesh_counter].verts) - 1
					# load it with raw data
					md5_model[mesh_counter].verts[vert_counter].vert_index = int(words[1])
					md5_model[mesh_counter].verts[vert_counter].uvco[0] = float(words[3])
					md5_model[mesh_counter].verts[vert_counter].uvco[1] = (1 - float(words[4]))
					md5_model[mesh_counter].verts[vert_counter].blend_index = int(words[6])
					md5_model[mesh_counter].verts[vert_counter].blend_count = int(words[7])
				if words and words[0] == "tri":
					# print("found a tri")
					md5_model[mesh_counter].tris.append(md5_tri())
					tri_counter = len(md5_model[mesh_counter].tris) - 1
					# load it with raw data
					md5_model[mesh_counter].tris[tri_counter].tri_index = int(words[1])
					md5_model[mesh_counter].tris[tri_counter].vert_index[0] = int(words[2])
					md5_model[mesh_counter].tris[tri_counter].vert_index[1] = int(words[3])
					md5_model[mesh_counter].tris[tri_counter].vert_index[2] = int(words[4])
				if words and words[0] == "weight":
					# print("found a weight")
					md5_model[mesh_counter].weights.append(md5_weight())
					weight_counter = len(md5_model[mesh_counter].weights) - 1
					# load it with raw data
					md5_model[mesh_counter].weights[weight_counter].weight_index = int(words[1])
					md5_model[mesh_counter].weights[weight_counter].bone_index = int(words[2])
					md5_model[mesh_counter].weights[weight_counter].bias = float(words[3])
					md5_model[mesh_counter].weights[weight_counter].weights[0] = float(words[5])
					md5_model[mesh_counter].weights[weight_counter].weights[1] = float(words[6])
					md5_model[mesh_counter].weights[weight_counter].weights[2] = float(words[7])
			# print("end of this mesh structure")
			if md5_model[mesh_counter].name == "":
				md5_model[mesh_counter].name = "MESH" + str(mesh_counter)
			mesh_counter += 1
	# figure out the base pose for each vertex from the weights
	for mesh in md5_model:
		print("updating vertex info for mesh: ", mesh.mesh_index)
		for vert_counter in range(0, len(mesh.verts)):
			blend_index = mesh.verts[vert_counter].blend_index
			for blend_counter in range(0, mesh.verts[vert_counter].blend_count):
				# get the current weight info
				w = mesh.weights[blend_index + blend_counter]
				#print("w: ")
				#w.dump()
				# the bone that the current weight is reffering to
				b = md5_bones[w.bone_index]
				#print("b: ")
				#b.dump()
				#pos = Vector((w.weights[0], w.weights[1], w.weights[2])).to_4d()
				#print("before bindmat: ", pos)
				pos = [0.0] * 3
				pos = vector_by_matrix(w.weights, b.bindmat)
				#print("after bindmat: ", pos)
				pos[0] = (pos[0] + b.bindpos[0]) * w.bias
				pos[1] = (pos[1] + b.bindpos[1]) * w.bias
				pos[2] = (pos[2] + b.bindpos[2]) * w.bias
				#print("after bindpos: ", pos)
				mesh.verts[vert_counter].co[0] += pos[0]
				mesh.verts[vert_counter].co[1] += pos[1]
				mesh.verts[vert_counter].co[2] += pos[2]
	# build the armature in blender
	translationtable = str.maketrans("\\", "/")
	tempstring = str.translate(filepath, translationtable)
	lindex = str.rfind(tempstring, "/")
	rindex = str.rfind(tempstring, ".")
	if lindex == -1: lindex = 0
	tempstring = str.rstrip(tempstring, ".md5mesh")
	tempstring = tempstring[lindex + 1 : len(tempstring)]
	bpy.ops.object.armature_add()
	armObj = bpy.context.active_object
	armObj.name = tempstring
	armObj.data.name = "MD5_ARM"
	bpy.ops.object.mode_set(mode='EDIT')
	bones = armObj.data.edit_bones
	# new armatures include a default bone, remove it
	bones.remove(bones[0])
	
	for bone in md5_bones:
		bone.blenderbone = bones.new(bone.name)
		# TODO need scale global val (default is 1.0)
		headData = Vector((bone.bindpos[0] * 1.0, bone.bindpos[1] * 1.0, bone.bindpos[2] * 1.0))
		bone.blenderbone.head = headData
		# TODO need bonesize global val (default is 3.0) (also needs scale global val)
		tailData = Vector((bone.bindpos[0] * 1.0 + 3.0 * 1.0 * bone.bindmat[1][0], bone.bindpos[1] * 1.0 + 3.0 * 1.0 * bone.bindmat[1][1], bone.bindpos[2] * 1.0 + 3.0 * 1.0 * bone.bindmat[1][2]))
		bone.blenderbone.tail = tailData
		if bone.parent != "":
			#print(bone.name, " is child of ", bone.parent)
			bone.blenderbone.parent = md5_bones[bone.parent_index].blenderbone
			
		boneisaligned = False
		for i in range(0, 359):
			bineisaligned = False
			m = Matrix(bone.blenderbone.matrix)
			mb = bone.bindmat
			#cos = Vector((m[0][0], m[0][1], m[0][2])).normalized().dot(Vector((mb[0][0], mb[0][1], mb[0][2])).normalized())
			cos = vector_dotproduct(vector_normalize(m[0]), vector_normalize(mb[0]))
			if cos > 0.9999:
				boneisaligned = True
				break
			bone.blenderbone.roll = i
		if not boneisaligned:
			print("Eeek!! ", bone.name, boneisaligned)
		#bones[string(bone.name)] = bone.blenderbone
	bpy.ops.object.mode_set(mode='OBJECT')
	
	# dump the meshes into blender
	for mesh in md5_model:
		print("adding mesh ", mesh.mesh_index, " to blender")
		print("it has ", len(mesh.verts), " verts")
		blender_mesh = bpy.data.meshes.new(mesh.name)
		blender_mesh.vertices.add(len(mesh.verts))
		blender_mesh.faces.add(len(mesh.tris))
		
		for vi in range(len(mesh.verts)):
			# TODO uses scale (1.0 is default)
			blender_mesh.vertices[vi].co[0] = mesh.verts[vi].co[0] * 1.0;
			blender_mesh.vertices[vi].co[1] = mesh.verts[vi].co[1] * 1.0;
			blender_mesh.vertices[vi].co[2] = mesh.verts[vi].co[2] * 1.0;
		
		# TODO mesh.shader
		
		for fi in range(len(mesh.tris)):
			f = blender_mesh.faces[fi]
			f.vertices[0] = mesh.tris[fi].vert_index[0]
			f.vertices[1] = mesh.tris[fi].vert_index[2]
			f.vertices[2] = mesh.tris[fi].vert_index[1]
			# smooth the face, since md5 has smoothing for all faces
			f.use_smooth = True
		
		blender_uvm = blender_mesh.uv_textures.new()
		for fi in range(len(mesh.tris)):
			uvf = blender_uvm.data[fi]
			uvf.uv1 = mesh.verts[mesh.tris[fi].vert_index[0]].uvco
			uvf.uv2 = mesh.verts[mesh.tris[fi].vert_index[2]].uvco
			uvf.uv3 = mesh.verts[mesh.tris[fi].vert_index[1]].uvco
		
		blender_mesh.calc_normals()
		blender_mesh.update(calc_edges=True)
		blender_mesh.validate(verbose=1)
		ob = bpy.data.objects.new(mesh.name, blender_mesh)
		bpy.context.scene.objects.link(ob)
		bpy.context.scene.update()
		
		vertgroup_created = []
		vertgroups = {}
		for b in md5_bones:
			vertgroup_created.append(0)
		
		for vert in mesh.verts:
			weight_index = vert.blend_index
			for weight_counter in range(vert.blend_count):
				# get the current weight info
				w = mesh.weights[weight_index + weight_counter]
				# check if the vertex group was already created
				if vertgroup_created[w.bone_index] == 0:
					vertgroup_created[w.bone_index] = 1
					vertgroups[w.bone_index] = ob.vertex_groups.new(md5_bones[w.bone_index].name)
				vg = vertgroups[w.bone_index]
				vg.add([vert.vert_index], w.bias, 'REPLACE')
		
		ob.parent = armObj
		ob.parent_type = 'ARMATURE'
		bpy.context.scene.update()
		

from bpy.props import *

class IMPORT_OT_idtech4_md5(bpy.types.Operator):
	'''Import from MD5 file format (.md5mesh)'''
	bl_idname = "import_scene.idtech4_md5"
	bl_description = 'Import from MD5 file format (.md5mesh)'
	bl_label = "Import MD5MESH" +' v.'+ __version__
	bl_space_type = "PROPERTIES"
	bl_region_type = "WINDOW"
	bl_options = {'UNDO'}
	
	filepath = StringProperty(
			subtype='FILE_PATH',
			)
	
	def draw(self, context):
		layout0 = self.layout
		#layout0.enabled = False

		#col = layout0.column_flow(2,align=True)
		layout = layout0.box()
		col = layout.column()
		#col.prop(self, 'KnotType') waits for more knottypes
		#col.label(text="import Parameters")
		#col.prop(self, 'replace')
		#col.prop(self, 'new_scene')
		'''
		row = layout.row(align=True)
		row.prop(self, 'curves')
		row.prop(self, 'circleResolution')

		row = layout.row(align=True)
		row.prop(self, 'merge')
		if self.merge:
			row.prop(self, 'mergeLimit')
 
		row = layout.row(align=True)
		#row.label('na')
		row.prop(self, 'draw_one')
		row.prop(self, 'thic_on')

		col = layout.column()
		col.prop(self, 'codec')
 
		row = layout.row(align=True)
		row.prop(self, 'debug')
		if self.debug:
			row.prop(self, 'verbose')
		'''
	
	def execute(self, context):
		load_md5(self.filepath)
		return {'FINISHED'}
	
	def invoke(self, context, event):
		wm = context.window_manager
		wm.fileselect_add(self)
		return {'RUNNING_MODAL'}
		
	
def menu_func(self, context):
	self.layout.operator(IMPORT_OT_idtech4_md5.bl_idname, text="id Tech 4 (.md5mesh)")

def register():
	bpy.utils.register_module(__name__)

	bpy.types.INFO_MT_file_import.append(menu_func)
 
def unregister():
	bpy.utils.unregister_module(__name__)

	bpy.types.INFO_MT_file_import.remove(menu_func)

if __name__ == "__main__":
	register()
