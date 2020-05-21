import concurrent.futures
from tqdm import tqdm
import requests
from z3 import *
import math
import json
import time
# M = 3
# N = 13
# t = [3, 3, 4, 3, 4, 3, 3, 3, 4, 3, 3, 4, 4]
# c = [[0, 2, 3, 4, 8, 10], [0, 1, 3, 5, 6, 7, 8], [1, 2, 3, 7, 9, 11, 12]]
# S = [[2, 4, 8], [4, 10], [], [7, 9], [], [11, 12], [8, 12], [], [], [], [], [], []]
maximum_time = 18
minimum_time = 12

class Teacher():
	def __init__(self,set_subject=[]):
		self.subject_teachable = set_subject
		
		
class School():
	def __init__(self,time_each_of_subject,list_of_teacher,constrain,maximum,minimum):
		self.time_each_of_subject = time_each_of_subject
		self.list_of_teacher = list_of_teacher
		self.number_subject,self.number_teacher = len(time_each_of_subject),len(list_of_teacher)
		self.constrain = constrain
		self.subject_assign = [BitVec('subject_'+str(i),self.number_teacher.bit_length()+1) for i in range(self.number_subject)]
		self.maximum_time,self.minimum_time = maximum,minimum
		self.s = Solver()
	
	def __str__(self):
		tmp_list = []
		self.previous_max_time = 0
		"Giao vien     Danh sach mon hoc duoc phan cong     So tiet\n"
		for i in range(self.number_teacher):
			tmp_dict = {}
			tmp_dict['Giao vien'] = i 
			tmp_dict['Danh sach mon hoc duoc phan cong'] = ""
			the_sum = 0
			for j in range(self.number_subject):
				if self.s.model()[self.subject_assign[j]].as_long()==i:
					tmp_dict['Danh sach mon hoc duoc phan cong'] += str(j)+","
					the_sum += self.time_each_of_subject[j]
			tmp_dict['So tiet'] = the_sum
			if self.previous_max_time < the_sum:
				self.previous_max_time = the_sum
			tmp_list.append(tmp_dict)
		return json.dumps(tmp_list)
	
	def rule_0(self):# each subject assign smaller than number teacher
		for i in range(self.number_subject):
			self.s.add(self.subject_assign[i]<self.number_teacher)
			self.s.add(self.subject_assign[i]>=0)
	
	def rule_1(self):# pair subject can't assign by same teacher
		for x,y in self.constrain:
			self.s.add(self.subject_assign[x]!=self.subject_assign[y])
			
	def rule_2(self):# time each teacher must satisfier
		for i in range(self.number_teacher):
			self.s.add(Sum([ If(self.subject_assign[j]==i,1,0)*self.time_each_of_subject[j] for j in range(self.number_subject) ]) <= self.maximum_time)
			self.s.add(Or(Sum([ If(self.subject_assign[j]==i,1,0)*self.time_each_of_subject[j] for j in range(self.number_subject) ]) >= self.minimum_time,And([ If(self.subject_assign[j]!=i,True,False) for j in range(self.number_subject) ]) ))
	
	def rule_2_reduce(self,id_teacher):# time each teacher must satisfier
		self.s.add(Sum([ If(self.subject_assign[j]==id_teacher,1,0)*self.time_each_of_subject[j] for j in range(self.number_subject) ]) <= self.maximum_time)
		self.s.add(Or(Sum([ If(self.subject_assign[j]==id_teacher,1,0)*self.time_each_of_subject[j] for j in range(self.number_subject) ]) >= self.minimum_time,And([ If(self.subject_assign[j]!=id_teacher,True,False) for j in range(self.number_subject) ]) ))
	
	def rule_3(self):# all subject must teach by the best teacher
		for subject_id in range(self.number_subject):
			for teacher_id in range(self.number_teacher):
				if subject_id not in self.list_of_teacher[teacher_id].subject_teachable:
					self.s.add(self.subject_assign[subject_id]!=teacher_id)
	
	def better_solution(self):# reduce maximum time highest teacher
		# self.next_solution()
		for i in range(self.number_teacher):
			self.s.add(Sum([ If(self.subject_assign[j]==i,1,0)*self.time_each_of_subject[j] for j in range(self.number_subject) ]) < self.previous_max_time)
	
	def next_solution(self):
		self.s.add(Or([self.subject_assign[i]!=self.s.model()[self.subject_assign[i]].as_long() for i in range(self.number_subject)]))
	
	def solve_1(self):
		self.rule_0()
		self.rule_1()
		self.rule_2()
		self.rule_3()
		if self.s.check()==sat:
			return True
		return False
		
	def solve_2(self):
		self.rule_0()
		self.rule_1()
		if self.s.check()==sat:
			print("[?]Passing checkpoint 1")
			self.rule_3()			
			if self.s.check()==sat:
				print("[?]Passing checkpoint 2")
				for i in tqdm(range(self.number_teacher)):
					# print("[?]Sat teacher id:",i)
					self.rule_2_reduce(i)
					if self.s.check()==unsat:
						return False
				if self.s.check()==sat:
					return True
		return False


if __name__=="__main__":
	## testing
	teacher_0 = Teacher(set([0, 2, 3, 4, 8, 10]))
	teacher_1 = Teacher(set([0, 1, 3, 5, 6, 7, 8]))
	teacher_2 = Teacher(set([1, 2, 3, 7, 9, 11, 12]))
	list_of_teacher = [teacher_0,teacher_1,teacher_2]
	time_each_of_subject = [3, 3, 4, 3, 4, 3, 3, 3, 4, 3, 3, 4, 4]
	constrain_subject = [(0,2),(0,4),(0,8),(1,4),(1,10),(3,7),(3,9),(5,11),(5,12),(6,8),(6,12)]
	
	'''
	logic input
	num_subject num_teacher
	subject_id number_time number_teachable list_teachable
	.....
	number_constrain
	subject_0 subject_1
	....
	'''
	
	"""
	## too big
	executor = concurrent.futures.ThreadPoolExecutor(max_workers=6)
	data = requests.request("GET","https://raw.githubusercontent.com/dungkhmt/bkoptapplication/master/data/BCA/bca_input.txt")
	data = data.text.split('\n')
	
	number_subject,number_teacher = [int(i) for i in data[0].split(' ')]
	list_of_teacher = [Teacher([]) for _ in range(number_teacher)]
	time_each_of_subject = []
	constrain_subject = []
	
	print(number_subject,number_teacher)
	for i in range(1,number_subject+1):
		tmp = [int(i) for i in data[i][:-1].split(' ')]
		
		id_subject = tmp[0]
		time_each_of_subject.append(tmp[1])
		# print(tmp[0],tmp[1])
		for j in tmp[3:]:
			list_of_teacher[j].subject_teachable.append(id_subject)
			# print(j,list_of_teacher[j].subject_teachable,end=" ");
		# input()
		
	number_constrain = int(data[number_subject+1])
	print(number_constrain)
	for i in range(number_subject+2,number_subject+number_constrain+2):
		constrain_subject.append([int(j) for j in data[i].split(' ')])
	"""
	# print(list_of_teacher[0].subject_teachable)
	# print(constrain_subject)
	# print(constrain_subject[0][0])
	# input()
	
	
	print("[*]Start counting!")
	start = time.time()
	school = School(time_each_of_subject,list_of_teacher,constrain_subject,maximum_time,minimum_time)
	if school.solve_2():## solve_1 run too long
		print("[+]First solution\n[?]Runtime:",time.time()-start)
		while school.s.check() == sat:
			print(school)
			if 'n' in input("[?] Get better result(Y/n)").lower():
				break
			school.better_solution()
		print("[!] No more better found!")
	else:
		print("[-]No solution found!\n[?]Runtime:",time.time()-start)
