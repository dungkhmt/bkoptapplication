from z3 import *
import json

"""
scenario:
each_subject_require_amount_time and pre_order
each_semester: (number_subject < 3) & 5 <= time <= 7

var_attact to subject:
value => that subject study in that semester
logic:   0,1   2,3    4,5    6,7
       year_1 year_2 year_3 year_4
can't study_parallel if violation preorder-constrain
=> target minimum number_semeter
"""
class Subject():
	def __init__(self,amount_of_time,list_of_pre_subject=[]):
		self.list_of_pre_subject = list_of_pre_subject 
		self.time__ = amount_of_time

class Schedule():
	def __init__(self,list_of_subject,max_subject_each_semester,time_constrain):
		self.time_min,self.time_max = time_constrain
		self.max_subject_semester = max_subject_each_semester
		self.number_subject = len(list_of_subject)
		self.list_of_subject = list_of_subject
		self.arrangement_var = [BitVec('subject_'+str(i),self.number_subject.bit_length()+1) for i in range(self.number_subject)]# worst case each semester study 1 subject
		self.s = Solver()
	
	def __str__(self):
		self.total_time_semeter,self.max_number_semeter = 0,0
		schedule__ = {}
		for i in range(self.number_subject):
			semester = 'sem_'+str(self.s.model()[self.arrangement_var[i]].as_long())
			if semester in schedule__:
				schedule__[semester].append(i)
			else:
				schedule__[semester] = [i]
			self.total_time_semeter += self.s.model()[self.arrangement_var[i]].as_long()
			if self.max_number_semeter < self.s.model()[self.arrangement_var[i]].as_long():
				self.max_number_semeter = self.s.model()[self.arrangement_var[i]].as_long()
		return json.dumps(schedule__)
		
	def rule0(self):# solution must better than worst-case and all-subject must be study
		for i in range(self.number_subject):
			self.s.add(self.arrangement_var[i] < self.number_subject)
			# self.s.add(self.arrangement_var[i] < 5)
			self.s.add(self.arrangement_var[i] >= 0)
	
	def rule1(self):# all subject must satisfied the constrain
		for i in range(self.number_subject):
			for j in self.list_of_subject[i].list_of_pre_subject:
				self.s.add(self.arrangement_var[j]<self.arrangement_var[i])
	
	def rule2(self):# number_subject_each_semester are limited
		for id_sem in range(self.number_subject):
			self.s.add( Sum([ If(self.arrangement_var[id_subject]==id_sem,1,0) for id_subject in range(self.number_subject)]) <= self.max_subject_semester)
			
	def rule3(self):# time_constrain
		for id_sem in range(self.number_subject):
			self.s.add(Sum([ If(self.arrangement_var[id_subject]==id_sem,1,0)*self.list_of_subject[id_subject].time__  for id_subject in range(self.number_subject) ]) <= self.time_max)
			self.s.add(Or(Sum([ If(self.arrangement_var[id_subject]==id_sem,1,0)*self.list_of_subject[id_subject].time__  for id_subject in range(self.number_subject) ]) >= self.time_min,And([ If(self.arrangement_var[id_subject]!=id_sem,True,False)for id_subject in range(self.number_subject) ]) ))
	
	def better_solution(self):# reduce maximum time highest teacher
		# self.next_solution()
		self.s.add(Sum([self.arrangement_var[i] for i in range(self.number_subject)]) < self.max_number_semeter)
		for i in range(self.number_subject):
			self.s.add(self.arrangement_var[i] < self.max_number_semeter)
		
	def solve(self):
		self.rule0()
		self.rule1()
		self.rule2()
		self.rule3()
		if self.s.check()==sat:
			print("[+] Solution found")
			return True
		print("[-] No solution found")
		return False
		
	
if __name__=="__main__":
	## testing 
	list_of_subject=[Subject(2,[1]),Subject(1),Subject(2),Subject(1),
					 Subject(3),Subject(2,[4]),Subject(1,[1,4]),Subject(3,[4]),
					 Subject(2,[5]),Subject(3,[2]),Subject(1,[3,7]),Subject(3,[3,5])
					]
	time_constrain = (5,7)
	max_subject_each_semester = 3
	
	schedule = Schedule(list_of_subject,max_subject_each_semester,time_constrain)
	if schedule.solve():
		while schedule.s.check() == sat:
			print(schedule)
			if 'n' in input("[?] Get better result(Y/n)").lower():
				break
			schedule.better_solution()
		print("[!] No more better found!")
