/*
 * Note:
 * 1. The dual value measures the increase in the objective function’s value per unit increase in the constraint’s bound.
 *    You can replace 5 with 6 in constraint 1 to see the change of the objective function.
 * 2. Slack variable: with slack variable y >= 0, the inequality Ax <= b can be converted to the equation Ax + y = b.
 */

package myself;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class Demo {

	private void LPExample() {
		
		try {
			
			// Engine.
			IloCplex md = new IloCplex();
			
			// Variables.
			IloNumVar x = md.numVar(0, Double.MAX_VALUE, "x");  // real variable: x >= 0.
			IloNumVar y = md.numVar(0, Double.MAX_VALUE, "y");  // real variable: y >= 0.
			
			// Objective function.
			IloLinearNumExpr objective = md.linearNumExpr();  // objective = 0,12x + 0,15y.
			objective.addTerm(0.12, x);
			objective.addTerm(0.15, y);
			
			md.addMinimize(objective);
			
			// Constraints.
			List<IloRange> constraints = new ArrayList<IloRange>();
			
			constraints.add(md.addGe(md.sum(md.prod(1, x), md.prod(1, y)), 5));  // x + y >= 5.
			constraints.add(md.addGe(md.sum(md.prod(2, x), md.prod(1, y)), 6));  // 2x + y >= 6.
			constraints.add(md.addGe(md.sum(md.prod(1, x), md.prod(3, y)), 9));  // x + 3y >= 9
			
			IloLinearNumExpr expr = md.linearNumExpr();  // 2x - y = 0.
			expr.addTerm(2, x);
			expr.addTerm(-1, y);
			constraints.add(md.addEq(expr, 0));
			
			expr = md.linearNumExpr();  // y <= x + 8;
			expr.addTerm(1, y);
			expr.addTerm(-1, x);
			constraints.add(md.addLe(expr, 8));
			
			md.setParam(IloCplex.IntParam.Simplex.Display, 0);
			
			// Solve and print solution.
			if (md.solve()) {
				System.out.println("Min value: " + md.getObjValue() + 
								 "\nx = " + md.getValue(x) + 
								 "\ny = " + md.getValue(y) + "\n");
				
				for (int i=0; i<constraints.size(); i++) {
					System.out.println("Dual value " + (i+1) + " = " + md.getDual(constraints.get(i)));
					System.out.println("Slack value " + (i+1) + " = " + md.getSlack(constraints.get(i)) + "\n");
				}
				
			} else {
				System.out.print("Negative infinity");
			}
			
			md.end();  // To free up resources. Very important, especially when you run a CPLEX project many times or run many CPLEX projects!
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		Demo test = new Demo();
		test.LPExample();
	}
}
