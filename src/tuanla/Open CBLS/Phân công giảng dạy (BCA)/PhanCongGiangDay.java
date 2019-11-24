package projectI;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import localsearch.constraints.basic.*;
import localsearch.functions.basic.*;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.functions.max_min.Max;
import localsearch.model.*;
import localsearch.search.TabuSearch;

public class PhanCongGiangDay {
	
	int teachers;
	int subjects;
	LocalSearchManager ls;
	ConstraintSystem S;
	ConditionalSum[] total;
	VarIntLS[] x;
	Random R;
	Set<Integer>[] S_T;
	
	
	public PhanCongGiangDay(int M, int N) {
		
		super();
		teachers = M;
		subjects = N;
		R = new Random();
		S_T = new Set[subjects]; // Subject - Teacher
		
		for (int i = 0; i < subjects; i++)
			S_T[i] = new HashSet<Integer>();
		
		int[][] C = {{0, 1}, {1, 2}, {0, 2}, {0, 1, 2}, {0}, {1}, {1}, {1, 2}, {0, 1}, {2}, {0}, {2}, {2}};
		int j;
		
		for (int i = 0; i < subjects; i++)			
			for (j = 0; j < C[i].length; j++)
				S_T[i].add(C[i][j]);				
	}


	public void stateModel_TabuSearch() {
		
		LocalSearchManager ls = new LocalSearchManager();
		S = new ConstraintSystem(ls);
		x = new VarIntLS[subjects];
		total = new ConditionalSum[teachers]; // The number of period assigned to a teacher
		int[] t = {3, 3, 4, 3, 4, 3, 3, 3, 4, 3, 3, 4, 4};
		int[][] s = {{2, 4, 8}, {4, 10}, {}, {7, 9}, {}, {11, 12}, {8, 12}, {}, {}, {}, {}, {}, {}};
		
		
		for (int i = 0; i < subjects; i++)	
			x[i] = new VarIntLS(ls, S_T[i]);
		// Constraint 1
		for (int i = 0; i < subjects; i++)
				for (int j = 0; j < s[i].length; j++)
					S.post(new NotEqual(x[i], x[s[i][j]]));				
		// Constraint 2
		for (int i = 0; i < teachers; i++)
			total[i] = new ConditionalSum(x, t, i);			
			
		
		Max max = new Max(total);
		FuncMinus m = new FuncMinus(max, 0);
		
		
		S.close();
		ls.close();	
		
		// Solve
		TabuSearch ts = new TabuSearch();
		ts.search(S, 30, 10, 1000000, 50); // Arguments is not optimal
		ts.searchMaintainConstraintsMinimize(m, S, 30, 10, 1000000, 50); // Arguments is not optimal
		
		// Display the result
		System.out.println();
		System.out.println("Giao vien     So tin chi     Danh sach mon hoc duoc phan cong");
		
		for (int i = 0; i < teachers; i++)
		{
			System.out.print("    " + i + "             " + total[i].getValue() + "         ");				
			
			boolean flag = true;
			for (int j = 0; j < subjects; j++) {
				if (x[j].getValue() == i) {
					if (flag) {
						System.out.print(j);
						flag = false;
					}
					else {
						System.out.print(", " + j);
					}
				}
			}
				
					
			
			System.out.println();
		}		
	}
	
	
	public static void main(String[] args) {
		
		PhanCongGiangDay pc= new PhanCongGiangDay(3, 13);
		pc.stateModel_TabuSearch();
	}
}
