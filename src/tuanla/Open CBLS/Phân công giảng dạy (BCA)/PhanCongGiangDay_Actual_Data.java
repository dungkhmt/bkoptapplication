package projectI;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import localsearch.constraints.basic.*;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.model.ConstraintSystem;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;

public class PhanCongGiangDay_Actual_Data {
	
	int teachers, subjects;
	int[] tc;
	LocalSearchManager ls;
	ConstraintSystem S;
	ConditionalSum[] total;
	VarIntLS[] x;
	Random R;
	Set<Integer>[] S_T;
	ArrayList<ArrayList<Integer>> s;
	
	
	public PhanCongGiangDay_Actual_Data() {
		
		super();
		
		// Read input data
		try {
			
			Scanner scanner = new Scanner(new File("bca_input.txt"));
			subjects = scanner.nextInt();
			teachers = scanner.nextInt();
			tc = new int[subjects];
			int class_index, feasible_teachers, conflicts, i, j;
			s = new ArrayList<ArrayList<Integer>>(subjects);
			S_T = new Set[subjects];
			
			
			for (i = 0; i < subjects; i++) {
				S_T[i] = new HashSet<Integer>();	
			}						
			for (i = 0; i < subjects; i++)
			{
				class_index = scanner.nextInt();
				tc[class_index] = scanner.nextInt();
				feasible_teachers = scanner.nextInt();
				
				for (j = 0; j < feasible_teachers; j++)
					S_T[i].add(scanner.nextInt());			
			}
			
			conflicts = scanner.nextInt();
			
			for (i = 0; i < subjects; i++) {
				s.add(new ArrayList<Integer>());
			}						
			for (i = 0; i < conflicts; i++)
			{
				int tmp = scanner.nextInt();
				s.get(tmp).add(scanner.nextInt());
			}
			
			scanner.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		R = new Random();		
	}


	public void stateModel_TabuSearch() {
		
		LocalSearchManager ls = new LocalSearchManager();
		S = new ConstraintSystem(ls);
		x = new VarIntLS[subjects];
		total = new ConditionalSum[teachers]; // The number of credits assigned to a teacher
		
		
		for (int i = 0; i < subjects; i++) {
			x[i] = new VarIntLS(ls, S_T[i]);
		}				
		// Constraint 1
		for (int i = 0; i < subjects; i++) {
			for (int j = 0; j < s.get(i).size(); j++) {
				S.post(new NotEqual(x[i], x[s.get(i).get(j)]));	
			}					
		}								
		// Constraint 2
		for (int i = 0; i < teachers; i++) {
			total[i] = new ConditionalSum(x, tc, i);
			S.post(new LessOrEqual(total[i], 18)); // f = 18: Maximum number of credits assigned to a teacher
		}
								
		S.close();
		ls.close();	
		
		// Solve
		TabuSearch ts = new TabuSearch();
		ts.search(S, 100, 1800, 10000, 20); // Arguments is not optimal
		
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
		
		PhanCongGiangDay_Actual_Data pc = new PhanCongGiangDay_Actual_Data();
		pc.stateModel_TabuSearch();
	}
}
