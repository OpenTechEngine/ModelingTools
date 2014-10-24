"""
This script imports id Tech 4 MD5ANIM files to Blender.

Usage:
Run this script from "File->Import" menu and then load the desired MD5ANIM file.
"""

bl_info = {
	'name': 'Import id Tech 4 Animation Format (.md5anim)',
	'author': 'Ali Scissons, Bob Holcomb, Thomas "der_ton" Hutter',
	'version': (0, 1, 0),
	"blender": (2, 6, 0),
	"api": 40791,
	'location': 'File > Import > id Tech 4 Animation (.md5anim)',
	'description': 'Import animation files in the id Tech 4 format (.md5anim)',
	'category': 'Import-Export',
	}
	
__version__ = '.'.join([str(s) for s in bl_info['version']])

import os
import sys
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

class md5anim_bone:
	name = ""
	parent_index = 0
	flags = 0
	frameDataIndex = 0
	bindpos = []
	bindquat = []
	#bindmat = []
	posemat = None #armature-space pose matrix, needed to import animation
	restmat = None
	invrestmat = None
	anim_frame = 0
	
	def __init__(self):
		name = ""
		self.bindpos=[0.0]*3
		self.bindquat=[0.0]*4
		#self.bindmat=[None]*3  #is this how you initilize a 2d-array
		#for i in range(3): self.bindmat[i] = [0.0]*3
		self.parent_index = 0
		self.flags = 0
		self.frameDataIndex = 0
		self.restmat = None
		self.invrestmat = None
		self.posemat = None
		anim_frame = 0

class md5anim:
	num_bones = 0
	md5anim_bones = []
	frameRate = 24
	numFrames = 0
	numAnimatedComponents = 0
	baseframe = []
	framedata = []

	def __init__(self):
		num_bones = 0
		md5anim_bones = []
		baseframe = []
		framedata = []
		
	def load_md5anim(self, md5_filename):
		# for some reason some empty bones are in the list still from previous imports
		del self.md5anim_bones[:]
		file=open(md5_filename,"r")
		lines=file.readlines()
		file.close()

		num_lines=len(lines)

		for line_counter in range(0,num_lines):
			current_line=lines[line_counter]
			words=current_line.split()

			if words and words[0]=="numJoints":
				self.num_bones=int(words[1])
				#print("num_bones: ", self.num_bones)
				
			elif words and words[0]=="numFrames":
				self.numFrames=int(words[1])
				#print("num_frames: ", self.numFrames)
				#fill framedata array with numframes empty arrays
				self.framedata = [[]]*self.numFrames
				
			elif words and words[0]=="frameRate":
				self.frameRate=int(words[1])
				#print("frameRate: ", self.frameRate)
				
			elif words and words[0]=="numAnimatedComponents":
				self.numAnimatedComponents=int(words[1])
				#print("numAnimatedComponents: ", self.numAnimatedComponents)
				
			elif words and words[0]=="hierarchy":
				for bone_counter in range(0,self.num_bones):
					# make a new bone
					self.md5anim_bones.append(md5anim_bone())
					# next line
					line_counter+=1
					current_line=lines[line_counter]
					words=current_line.split()
					#skip over blank lines
					while not words:
						line_counter+=1
						current_line=lines[line_counter]
						words=current_line.split()

					#self.md5anim_bones[bone_counter].bone_index=bone_counter
					#get rid of the quotes on either side
					temp_name=str(words[0])
					temp_name=temp_name[1:-1]
					self.md5anim_bones[bone_counter].name=temp_name
					#print("found bone: ", self.md5anim_bones[bone_counter].name)
					self.md5anim_bones[bone_counter].parent_index = int(words[1])
					#if self.md5anim_bones[bone_counter].parent_index>=0:
					#	self.md5anim_bones[bone_counter].parent = self.md5anim_bones[self.md5anim_bones[bone_counter].parent_index].name
					self.md5anim_bones[bone_counter].flags = int(words[2])
					self.md5anim_bones[bone_counter].frameDataIndex=int(words[3])


			elif words and words[0]=="baseframe":
				for bone_counter in range(0,self.num_bones):
					line_counter+=1
					current_line=lines[line_counter]
					words=current_line.split()
					#skip over blank lines
					while not words:
						line_counter+=1
						current_line=lines[line_counter]
						words=current_line.split()
					self.md5anim_bones[bone_counter].bindpos[0]=float(words[1])
					self.md5anim_bones[bone_counter].bindpos[1]=float(words[2])
					self.md5anim_bones[bone_counter].bindpos[2]=float(words[3])
					qx = float(words[6])
					qy = float(words[7])
					qz = float(words[8])
					qw = 1 - qx*qx - qy*qy - qz*qz
					if qw<0:
						qw=0
					else:
						qw = -sqrt(qw)
					self.md5anim_bones[bone_counter].bindquat = [qx,qy,qz,qw]

			elif words and words[0]=="frame":
				framenumber = int(words[1])
				self.framedata[framenumber]=[]
				line_counter+=1
				current_line=lines[line_counter]
				words=current_line.split()
				while words and not(words[0]=="frame" or words[0]=="}"):
					for i in range(0, len(words)):
						self.framedata[framenumber].append(float(words[i]))
					line_counter+=1
					current_line=lines[line_counter]
					words=current_line.split()

	def apply(self, arm_obj, actionname):
		#action = bpy.data.actions.new(actionname)
		first_anim = False
		if not arm_obj.animation_data:
			arm_obj.animation_data_create()
		if not arm_obj.animation_data.action:
			action = bpy.data.actions.new(actionname)
			arm_obj.animation_data.action = action
			first_anim = True
		thepose = arm_obj.pose
		sorted_bones = sorted(self.md5anim_bones, key = lambda md5anim_bone: md5anim_bone.parent_index)
		for b in self.md5anim_bones:
			b.invrestmat = arm_obj.data.bones[b.name].matrix_local.inverted()
			b.restmat = arm_obj.data.bones[b.name].matrix_local
		for currntframe in range(1, self.numFrames+1):
			#print("importing frame ", currntframe," of", self.numFrames)
			first_frame = bpy.context.scene.frame_end
			if not first_anim:
				bpy.context.scene.frame_current = currntframe + first_frame
			else:
				first_frame = 1
				bpy.context.scene.frame_current = currntframe
			for md5b in self.md5anim_bones:
				thebone = self.transform_bone(md5b, thepose, currntframe)
					
				rot_mode = thebone.rotation_mode
				if rot_mode == "QUATERNION":
					thebone.keyframe_insert("rotation_quaternion", frame=currntframe+first_frame)
				elif rot_mode == "AXIS_ANGLE":
					thebone.keyframe_insert("rotation_axis_angle", frame=currntframe+first_frame)
				else:
					thebone.keyframe_insert("rotation_euler", frame=currntframe+first_frame)
				thebone.keyframe_insert("location", frame=currntframe+first_frame)
		bpy.context.scene.update()
		bpy.context.scene.frame_current = first_frame
		print(actionname, first_frame, first_frame + currntframe)
	
	def transform_bone(self, bone, pose, current_frame):
		try:
			pose_bone = pose.bones[bone.name]
		except:
			print("could not find bone ", bone.name, " in armature")
			return
		
		if bone.anim_frame == current_frame:
			return pose_bone
		
		(qx,qy,qz,qw) = bone.bindquat
		lx,ly,lz = bone.bindpos
		frameDataIndex = bone.frameDataIndex
		if (bone.flags & 1):
			if bone.parent_index != -1 or not t_inplace:
				lx = self.framedata[current_frame-1][frameDataIndex]
			frameDataIndex+=1
		if (bone.flags & 2):
			if bone.parent_index != -1 or not t_inplace:
				ly = self.framedata[current_frame-1][frameDataIndex]
			frameDataIndex+=1		  
		if (bone.flags & 4):
			if bone.parent_index != -1 or not t_inplace:
				lz = self.framedata[current_frame-1][frameDataIndex]
			frameDataIndex+=1
		if (bone.flags & 8):
			qx = self.framedata[current_frame-1][frameDataIndex]
			frameDataIndex+=1
		if (bone.flags & 16):
			qy = self.framedata[current_frame-1][frameDataIndex]
			frameDataIndex+=1					 
		if (bone.flags & 32):
			qz = self.framedata[current_frame-1][frameDataIndex]
		qw = 1 - qx*qx - qy*qy - qz*qz
		if qw<0:
			qw=0
		else:
			qw = -sqrt(qw)
		lmat = quaternion2matrix([qx,qy,qz,qw])
		# TODO need global val scale (defaulted to 1.0)
		lmat[3][0] = lx*1.0
		lmat[3][1] = ly*1.0
		lmat[3][2] = lz*1.0
		lmat = Matrix(lmat)
		
		if bone.parent_index >= 0:
			# since this bone will depend on a parent's transformation matrix,
			# let's make sure it is transformed
			if self.md5anim_bones[bone.parent_index].anim_frame < current_frame:
				self.transform_bone(self.md5anim_bones[bone.parent_index], pose, current_frame)
			pose_bone.matrix = self.md5anim_bones[bone.parent_index].restmat * lmat
			bone.restmat = pose_bone.matrix
			bone.anim_frame = current_frame
			return pose_bone
		else:
			pose_bone.matrix = lmat
			bone.restmat = pose_bone.matrix
			bone.anim_frame = current_frame
			return pose_bone
			

def load_md5anim(filepath):
	theanim = md5anim()
	theanim.load_md5anim(filepath)

	obj = None
	for armObj in bpy.context.scene.objects:
		data = armObj.data
		if type(data) is bpy.types.Armature:
			obj = armObj
			break

	if obj:
		#print("Applying animation to armature: ", obj.name)
		pth, actionname = os.path.split(filepath)
		theanim.apply(obj, actionname[:-8])
		scn = bpy.context.scene
		scn.frame_end = scn.frame_current + theanim.numFrames + 1
	else:
		print("Could not apply animation, no armature in scene")
	return
	
from bpy.props import *

class IMPORT_OT_idtech4_md5(bpy.types.Operator):
	'''Import from MD5 Animation file format (.md5anim)'''
	bl_idname = "import_scene.idtech4_md5anim"
	bl_description = 'Import from MD5 Animation file format (.md5anim)'
	bl_label = "Import MD5 Animation" +' v.'+ __version__
	bl_space_type = "PROPERTIES"
	bl_region_type = "WINDOW"
	bl_options = {'UNDO'}
	
	filepath = StringProperty(
			subtype='FILE_PATH',
			)
			
	in_place = BoolProperty(
			name = "Keep Animation in Place",
			description = "Prevents the origin bone translation changing from the base pose",
			default = True,
			)
	
	def draw(self, context):
		layout0 = self.layout
		#layout0.enabled = False

		#col = layout0.column_flow(2,align=True)
		layout = layout0.box()
		col = layout.column()
		col.label(text="Import Parameters")
		col.prop(self, 'in_place')
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
		global t_inplace
		t_inplace = self.in_place
		load_md5anim(self.filepath)
		return {'FINISHED'}
	
	def invoke(self, context, event):
		wm = context.window_manager
		wm.fileselect_add(self)
		return {'RUNNING_MODAL'}
		
	
def menu_func(self, context):
	self.layout.operator(IMPORT_OT_idtech4_md5.bl_idname, text="id Tech 4 Animation (.md5anim)")

def register():
	bpy.utils.register_module(__name__)

	bpy.types.INFO_MT_file_import.append(menu_func)
 
def unregister():
	bpy.utils.unregister_module(__name__)

	bpy.types.INFO_MT_file_import.remove(menu_func)

if __name__ == "__main__":
	register()
