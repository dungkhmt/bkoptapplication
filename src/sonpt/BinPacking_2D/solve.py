import matplotlib.pyplot as plt
import concurrent.futures
import numpy as np
from tqdm import tqdm
from z3 import *
import requests
import json
import time
import random
list_color = ['k','y','m','c','r','g','b']
list_shape = ['.','o','v','^','<','>','1','2','3','4','s','p','*','h','H','+','x','D','d','|','_']

class BinPacking2D():# simple shape box are retanger
	def __init__(self,info_problem,list_box):
		self.x,self.y,self.number_box = info_problem
		self.list_box = list_box
		self.max_size_box = 0
		self.COUNTER = random.randint(0,20)
		for x,y in self.list_box:
			self.max_size_box = max(x,y,self.max_size_box)
		## coordinate and rotate
		self.coordinate = [[BitVec('x_'+str(i),(self.x+self.max_size_box).bit_length()+1),BitVec('y_'+str(i),(self.y+self.max_size_box).bit_length()+1),BitVec('R_'+str(i),1)] for i in range(self.number_box)]
		self.s = Solver()
	
	def __str__(self):
		tmp_dict = []
		for i in range(self.number_box):
			x,y,r = self.s.model()[self.coordinate[i][0]].as_long(),self.s.model()[self.coordinate[i][1]].as_long(),self.s.model()[self.coordinate[i][2]].as_long()
			tmp_dict.append({'id':i,'type box':self.list_box[i],'coordinate':(x,y),'rotate':r})
			str_color = list_color[self.COUNTER%len(list_color)]+list_shape[self.COUNTER%len(list_shape)]
			self.COUNTER+=1
			for a in range(self.list_box[i][0]):
				for b in range(self.list_box[i][1]):
					plt.plot(x+a, y+b, str_color) if r == 0 else plt.plot(x+b, y+a, str_color)
			
			
		return json.dumps(tmp_dict)
	
	def rule_0(self):
		# If box[i].r == 0
		# -1 < box[i].location_x; box[i].location_x + box[i].x < size_x
		# -1 < box[i].location_y; box[i].location_y + box[i].y < size_y
		# If box[i].r == 1
		# -1 < box[i].location_x; box[i].location_x + box[i].y < size_x
		# -1 < box[i].location_y; box[i].location_y + box[i].x < size_y
		# box[i].r == 0 or box[i].r == 1
		for i in range(self.number_box):
			self.s.add(Or(self.coordinate[i][2]==1,self.coordinate[i][2]==0))
			self.s.add(And(self.coordinate[i][0]<self.x,self.coordinate[i][1]<self.y))# remove int overflow
			self.s.add(And(self.coordinate[i][0]>-1,self.coordinate[i][1]>-1)) 
			self.s.add(
				If(self.coordinate[i][2]==0,
					And(
						self.coordinate[i][0]+self.list_box[i][0]<=self.x,
						self.coordinate[i][1]+self.list_box[i][1]<=self.y
					),
					And(
						self.coordinate[i][0]+self.list_box[i][1]<self.x,
						self.coordinate[i][1]+self.list_box[i][0]<self.y
					)
				)
			) 			
		
	def rule_1(self):# remove collide x y and roated 
		for i in range(self.number_box-1):
			for j in range(i+1,self.number_box):
				self.s.add( 
					If(self.coordinate[i][2]==0,
						If(self.coordinate[j][2]==0,
							Or(
								self.coordinate[i][0]+self.list_box[i][0] <= self.coordinate[j][0],
								self.coordinate[j][0]+self.list_box[j][0] <= self.coordinate[i][0],	
								self.coordinate[i][1]+self.list_box[i][1] <= self.coordinate[j][1],
								self.coordinate[j][1]+self.list_box[j][1] <= self.coordinate[i][1],
							),
							Or(
								self.coordinate[i][0]+self.list_box[i][0] <= self.coordinate[j][0],
								self.coordinate[j][0]+self.list_box[j][1] <= self.coordinate[i][0],	
								self.coordinate[i][1]+self.list_box[i][1] <= self.coordinate[j][1],
								self.coordinate[j][1]+self.list_box[j][0] <= self.coordinate[i][1],
							)
						),
						If(self.coordinate[j][2]==0,
							Or(
								self.coordinate[i][0]+self.list_box[i][1] <= self.coordinate[j][0],
								self.coordinate[j][0]+self.list_box[j][0] <= self.coordinate[i][0],	
								self.coordinate[i][1]+self.list_box[i][0] <= self.coordinate[j][1],
								self.coordinate[j][1]+self.list_box[j][1] <= self.coordinate[i][1],
							),
							Or(
								self.coordinate[i][0]+self.list_box[i][1] <= self.coordinate[j][0],
								self.coordinate[j][0]+self.list_box[j][1] <= self.coordinate[i][0],	
								self.coordinate[i][1]+self.list_box[i][0] <= self.coordinate[j][1],
								self.coordinate[j][1]+self.list_box[j][0] <= self.coordinate[i][1],
							)
						)
					)
				)
	
	def next_solution(self):
		self.s.add(
					Or(
						[  self.coordinate[i][j]!=self.s.model()[self.coordinate[i][j]].as_long() for j in range(3)  for i in range(self.number_box)]
					)
				 )
			
	def solve(self):
		self.rule_0()
		self.rule_1()
		if self.s.check()==sat:
			return True
		return False
	
def do_stuff(future):
	res = future.result()
		
if __name__=="__main__":
	# executor = concurrent.futures.ThreadPoolExecutor(max_workers=10)
	resp = requests.request("GET","https://raw.githubusercontent.com/dungkhmt/bkoptapplication/master/data/BinPacking2D/Binpacking2D-W19-H19-I21.txt")
	data = resp.text.split('\n')#[:-17]
	info_problem = [int(i) for i in data[0].split(' ')]
	# info_problem[2] -= 17
	list_box = []
	for i in range(1,info_problem[2]+1):
		list_box.append( [int(j) for j in data[i].split(' ')] )
	print(info_problem,list_box)
	
	
	# info_problem = [5,5,3]
	# list_box = [[1,1],[2,2],[2,4]]
	print("[*]Start counting!")
	start = time.time()
	bin_packing_2d = BinPacking2D(info_problem,list_box)
	counting = 1
	if bin_packing_2d.solve():## solve_1 run too long
		print("[+]First solution\n[?]Runtime:",time.time()-start)
		while bin_packing_2d.s.check() == sat:
			print("Solution -",counting,":")
			print(bin_packing_2d)	
			plt.show()
			# f = executor.submit(plt.show)
			# f.add_done_callback(do_stuff)
			if 'n' in input("[?] Get next result(Y/n)").lower():
				break
			bin_packing_2d.next_solution()
			counting+=1
		print("[-] Stop get result!")
	else:
		print("[-]No solution found!\n[?]Runtime:",time.time()-start)
	
