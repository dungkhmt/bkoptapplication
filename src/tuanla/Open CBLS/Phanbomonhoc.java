package projectI;

import java.util.LinkedList;
import java.util.Vector;

import localsearch.model.*;

import localsearch.constraints.basic.Implicate;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.functions.*;
import localsearch.functions.basic.FuncMinus;
import localsearch.functions.basic.FuncPlus;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.functions.max_min.Max;
import localsearch.functions.max_min.Min;
import localsearch.search.*;

public class Phanbomonhoc {
			
	private static void display(ConstraintSystem S, VarIntLS[] x, ConditionalSum[] s_credit, int n, int m) {
		
		LinkedList<LinkedList<Integer>> result = new LinkedList<LinkedList<Integer>>();
		
		
		for (int i=0; i<m; i++) {
			result.add(new LinkedList<Integer>());
		}
		for (int i=0; i<n; i++) {
			result.get(x[i].getValue()).add(i);
		}
		
		System.out.println("\nS = " + S.violations());
		System.out.println("\nSolution:");	
		System.out.println("\n     Hoc ky          So tin chi          Mon");
		for(int i=0; i<m; i++) {
			System.out.print("        " + i + "                " + s_credit[i].getValue() + "              ");			
			for(int j=0; j<result.get(i).size() - 1; j++) {
				System.out.print(result.get(i).get(j) + ", ");
			}
			System.out.println(result.get(i).get(result.get(i).size() - 1));
		}
	}
	
	
	public static void main(String[] args) {
		
		int n=46;
		int m=8;
		
		int[] credit; // credit[i] is the number of credits of the course i
		int[] minCrd; // minC[i] is the minimum number of credits of period i
		int[] maxCrd; // maxC[i] is the maximum number of credits of period i
		int[] minCrs; // minCrs[i] is the minimum number of courses assigned to period i
		int[] maxCrs; // maxCrs[i] is the maximum number of courses assigned to period i
		int[] b;
		int[] e; //(b[i], e[i]) is an edge of prerequisite: course b[i] must be a prerequisite of course e[i]
		
		credit = new int[] {1,  3,  1,  2,  4, 
							4,  1,  5,  3,  4, 
							4,  5,  1,  3,  3, 
							4,  1,  1,  3,  3, 
							3,  3,  3,  3,  1, 
							4,  4,  3,  3,  3, 
							2,  4,  3,  3,  3, 
							3,  3,  3,  3,  3, 
							3,  3,  2,  3,  3,  3};
		minCrd=new int[m];
		maxCrd=new int[m];
		minCrs=new int[m];
		maxCrs=new int[m];
		
		
		for(int i=0; i<m; i++)
		{
			minCrd[i] = 10;
			maxCrd[i] = 24;
			minCrs[i] = 5;
			maxCrs[i] = 12;
		}
		
		b = new int[]{6,7,7,9,10,10,11,11,13,14,15,15,16,17,18,19,20,23,25,26,27,28,31,32,33,34,35,35,36,36,37,37,38,39,39,44,45,45};
		e = new int[]{0,1,5,4,4 ,5 ,7 ,10,8 ,8 ,9 ,10,6 ,2 ,13,13,14,15,13,20,21,23,25,25,29,27,27,27,29,29,35,35,29,35,35,37,31,31};
		
		
		LocalSearchManager mgr = new LocalSearchManager();
		ConstraintSystem S = new ConstraintSystem(mgr);
		VarIntLS[] x = new VarIntLS[n]; // x[i] is the period assigned to the course i
		for(int i=0; i<x.length; i++)
		{
			x[i] = new VarIntLS(mgr, 0, m-1);
		}
		
		
		ConditionalSum[] s_credit = new ConditionalSum[m];
		ConditionalSum[] s_courses = new ConditionalSum[m];
		int[] one = new int[n];
		
		
		for(int i=0; i<n; i++) one[i] = 1;
		for(int i=0; i < m; i++){
			// Constraint 1
			s_credit[i] = new ConditionalSum(x, credit, i);
			S.post(new LessOrEqual(s_credit[i], maxCrd[i]));
			S.post(new LessOrEqual(minCrd[i], s_credit[i]));
			
			// Constraint 2
			s_courses[i] = new ConditionalSum(x, one, i);
			S.post(new LessOrEqual(s_courses[i], maxCrs[i]));
			S.post(new LessOrEqual(minCrs[i], s_courses[i]));
		}
		for(int j=0; j<b.length; j++){
			//S.post(new LessOrEqual(x[b[j]],x[e[j]]));
			// Constraint 3
			IFunction f = new FuncMinus(x[e[j]], x[b[j]] );
			S.post(new LessOrEqual(1, f));
			//S.post(new Implicate(new LessOrEqual(x[b[j]], x[e[j]]), new LessOrEqual(f, 2)));
			//S.post(new Implicate(new LessOrEqual(x[b[j]], x[e[j]]), new LessOrEqual(1, f)));
			
		}
		
		
		Max max = new Max(s_credit);
		Min min = new Min(s_credit);
		FuncMinus mm = new FuncMinus(max,min);
		
		S.close();
		mgr.close();
		
		// Initialization value
		System.out.println("max = "+max.getValue());
		System.out.println("min = "+min.getValue());
		System.out.println("mm = "+mm.getValue());				
		System.out.println("\n\n     Hoc ky          So tin chi");
		for (int i=0; i<m; i++) {
			System.out.println("        " + i + "                " + s_credit[i].getValue());
		}			
		System.out.println("\n\n       Mon            Hoc ky");
		for(int i=0; i<10; i++) {
			System.out.println("        " + i + "                " + x[i].getValue());
		}
		for(int i=10; i<n; i++) {
			System.out.println("        " + i + "               " + x[i].getValue());
		}
		System.out.println();
		//if(true) return;
		
		TabuSearch ts = new TabuSearch();
		ts.search(S, 50, 100, 100000, 20);
		
		//localsearch.applications.Test T = new localsearch.applications.Test();
		//T.test(mm, 100000);
		
		
		
		//SA_search s = new SA_search();
		//s.search(S, 2000, 0.00001, 0.9);
		
		// Display the result
		display(S, x, s_credit, n, m);	
		System.out.print("\n\n");
				
		// Optimize
		ts.searchMaintainConstraintsMinimize(mm, S, 20, 100, 500, 100);
		
		// Display the result		
		display(S, x, s_credit, n, m);		
	}
}
