/*
 * Note: this programme use choco solver 4.10.2. You can find installer in folder softwares.
 */

package myself;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class BACP {

	private void modelAndSolve() {
		
		// Model.
		Model md = new Model("BACP");
		
		// Variables.
		int n=46, m=8;		
		int[] credit = new int[] {  1,  3,  1,  2,  4, 
									4,  1,  5,  3,  4, 
									4,  5,  1,  3,  3, 
									4,  1,  1,  3,  3, 
									3,  3,  3,  3,  1, 
									4,  4,  3,  3,  3, 
									2,  4,  3,  3,  3, 
									3,  3,  3,  3,  3, 
									3,  3,  2,  3,  3,  3  };
		
		int[] b = new int[]{6,7,7,9,10,10,11,11,13,14,15,15,16,17,18,19,20,23,25,26,27,28,31,32,33,34,35,35,36,36,37,37,38,39,39,44,45,45};
		int[] e = new int[]{0,1,5,4,4 ,5 ,7 ,10,8 ,8 ,9 ,10,6 ,2 ,13,13,14,15,13,20,21,23,25,25,29,27,27,27,29,29,35,35,29,35,35,37,31,31};
		
		IntVar[] x = new IntVar[n];
		IntVar[] s_credit = new IntVar[m];  // Total credit of term i^th.
		IntVar[] s_course = new IntVar[m];  // Total course of term i^th.
		IntVar max = md.intVar("max", 10, 20);  // Stores max value of s_credit.
		
		for (int i=0; i<n; i++) {
			x[i] = md.intVar("x[" + i + "]", 0, m-1);
		}
		
		for (int i=0; i<m; i++) {
			s_credit[i] = md.intVar("s_credit[" + i + "]", 10, 24);
			s_course[i] = md.intVar("s_course[" + i + "]", 5, 12);
		}
		
		// Constraints.	
		for (int i=0; i<b.length; i++) {
			md.arithm(x[b[i]], "<", x[e[i]]).post();
		}
		
		md.binPacking(x, credit, s_credit, 0).post();  // 10 <= s_credit[i] <= 24.
		
		for (int i=0; i<m; i++) {
			md.count(i, x, s_course[i]).post();  // 5 <= s_course[i] <= 12.
		}
		
		md.max(max, s_credit).post();
		md.setObjective(Model.MINIMIZE, max);
		
		
		// Solve and print solution.
		Solver solver = md.getSolver();		
		int count = 1;
		
		while (solver.solve()) {
			System.out.println("Solution " + count + ":");
			
			for (int i=0; i<n; i++) {
				System.out.print(x[i] + "   ");
			}
			
			System.out.println();
			
			for (int i=0; i<m; i++) {
				System.out.print(s_credit[i] + "   ");
			}
			
			System.out.println();
			
			for (int i=0; i<m; i++) {
				System.out.print(s_course[i] + "   ");
			}
			
			System.out.print("\n\n\n");
			
			count++;
		}
		
		System.out.print("The last solution is optimal solution.");
	}
	
	public static void main(String[] args) {
	    BACP test = new BACP();
		test.modelAndSolve();    	
	}	
}
