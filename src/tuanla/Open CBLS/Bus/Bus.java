package myself;

import localsearch.constraints.alldifferent.AllDifferent;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.constraints.basic.LessThan;
import localsearch.functions.basic.FuncPlus;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.model.ConstraintSystem;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;


public class Bus {

	public static void main(String[] args) {
		
		LocalSearchManager ls = new LocalSearchManager();
		ConstraintSystem c = new ConstraintSystem(ls);
		int N = 3, k = 2;
		VarIntLS[] x = new VarIntLS[2*N+1]; // x[i] = j: i la diem thu j trong lo trinh
		VarIntLS[] p = new VarIntLS[2*N+1]; // p[i]: so khach tren xe sau khi roi diem i
		VarIntLS[] result = new VarIntLS[2*N+1]; //result la route nhung thuoc kieu VarIntLS
		ConditionalSum[] route = new ConditionalSum[2*N+1]; // route[i] = j: j la diem thu i trong lo trinh
		ConditionalSum[] t = new ConditionalSum[2*N+1];		
		
		int[] r = new int[] {0, 1, 2, 3, 4, 5, 6};		
		int[] w = new int[] {0, 1, 1, 1, -1, -1, -1};		
		int[][] d = new int[][] {
									{0, 8, 5, 1, 10, 5, 9},
									{9, 0, 5, 6, 6, 2, 8},
									{2, 2, 0, 3, 8, 7, 2},
									{5, 3, 4, 0, 3, 2, 7},
									{9, 6, 8, 7, 0, 9, 10},
									{3, 8, 10, 6, 5, 0, 2},
									{3, 4, 4, 5, 2, 2, 0}
															};
		
		x[0] = new VarIntLS(ls, 0, 0);
		p[0] = new VarIntLS(ls, 0, 0);
		t[0] = new ConditionalSum(x, w, 0);	
		route[0] = new ConditionalSum(x, r, 0);
		result[0] = new VarIntLS(ls, 0, 0);
		
		// B1: 
		for (int i=1; i<2*N+1; i++) {
			x[i] = new VarIntLS(ls, 1, 2*N);
			p[i] = new VarIntLS(ls, 0, k);
			result[i] = new VarIntLS(ls, 1, 2*N);
		}		
		
		c.post(new AllDifferent(x));
		
		for (int i=1; i<N+1; i++) {
			c.post(new LessThan(x[i], x[i+N]));
		}
		
		for (int i=1; i<2*N+1; i++) {
			
			t[i] = new ConditionalSum(x, w, i);			
			route[i] = new ConditionalSum(x, r, i);
			
			c.post(new IsEqual(route[i], result[i]));
			c.post(new IsEqual(new FuncPlus(p[i], 0), new FuncPlus(new FuncPlus(p[i-1], 0), t[i])));
		}
					
		TotalCost distance = new TotalCost(d, result);		
		c.post(new LessOrEqual(distance, 25));
		
		c.close();
		ls.close();
		
		// B2: Solve. 
		TabuSearch ts = new TabuSearch();			
		ts.search(c, 50, 10, 100000, 100);
		ts.searchMaintainConstraintsMinimize(distance, c, 200, 10, 10000, 20);			
		
		// B3: Display the result.		
		for (int i=0; i<2*N+1; i++) {
			System.out.print(result[i].getValue() + " ");
		}
		
		System.out.print("\nTotal cost: " + distance.getValue());
		
//		System.out.println();		
//		
//		for (int i=0; i<2*N+1; i++) {
//			System.out.print(p[i].getValue() + " ");
//		}
//		
//		System.out.println();		
//		
//		for (int i=0; i<2*N+1; i++) {
//			System.out.print(t[i].getValue() + " ");
//		}
	}

}
