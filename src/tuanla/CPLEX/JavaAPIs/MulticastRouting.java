package tuanla.CPLEX.JavaAPIs;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;

public class MulticastRouting {

	private void MPmodel() {

		try {

			// Engine.
			IloCplex md = new IloCplex();

			// Variables.
			int n = 8;
			int[][] c = new int[][] { { 0, 3, 2, 4, 10000, 10000, 10000, 10000 },
									  { 3, 0, 6, 10000, 2, 10000, 10000, 10000 }, 
									  { 2, 6, 0, 3, 3, 4, 10000, 10000 },
									  { 4, 10000, 3, 0, 10000, 4, 10000, 4 }, 
									  { 10000, 2, 3, 10000, 0, 2, 3, 10000 },
									  { 10000, 10000, 4, 4, 2, 0, 3, 2 }, 
									  { 10000, 10000, 10000, 10000, 3, 3, 0, 6 },
									  { 10000, 10000, 10000, 4, 10000, 2, 6, 0 } };

			int[][] L = new int[][] { { 0, 6, 5, 7, 100, 100, 100, 100 }, 
									  { 6, 0, 6, 100, 2, 100, 100, 100 },
									  { 5, 6, 0, 4, 4, 7, 100, 100 }, 
									  { 7, 100, 4, 0, 100, 8, 100, 5 }, 
									  { 100, 2, 4, 100, 0, 6, 3, 100 },
									  { 100, 100, 7, 8, 6, 0, 4, 2 }, 
									  { 100, 100, 100, 100, 3, 4, 0, 6 },
									  { 100, 100, 100, 5, 100, 2, 6, 0 } };

			IloIntVar[][] x = new IloIntVar[n][n];
			IloIntVar[] y = new IloIntVar[n];
			y[0] = md.intVar(0, 0);

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i == j) {
						x[i][j] = md.intVar(0, 0);
					} else {
						x[i][j] = md.intVar(0, 1);
					}
				}

				if (i > 0) {
					y[i] = md.intVar(0, 12);
				}
			}

			// Objective function.
			IloLinearNumExpr objective = md.linearNumExpr();

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					objective.addTerm(c[i][j], x[i][j]);
				}
			}

			md.addMinimize(objective);

			// Constraints.
			// Constraint 1.
			IloLinearNumExpr[] flow_in = new IloLinearNumExpr[n];
			for (int i = 1; i < n; i++) {
				flow_in[i] = md.linearNumExpr();

				for (int j = 0; j < n; j++) {
					flow_in[i].addTerm(1, x[j][i]);
				}

				md.addEq(flow_in[i], 1);
			}

			// Constraint 2.
			for (int u = 1; u < n; u++) {
				for (int v = 0; v < n; v++) {
					if (u != v) {
						md.addLe(md.sum(md.prod(1, y[u]), md.prod(10000, x[v][u]), md.prod(-1, y[v])),
								L[u][v] + 10000);
						md.addGe(md.sum(md.prod(1, y[u]), md.prod(-10000, x[v][u]), md.prod(-1, y[v])),
								L[u][v] - 10000);
					}
				}
			}

			// Solves and prints solution.
			if (md.solve()) {
				System.out.println("\n\nOptimal solution:");
				System.out.println("\nTotal cost: " + md.getObjValue());
				System.out.print("\nLinks: ");

				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if (md.getValue(x[i][j]) == 1)
							System.out.print("(" + (i + 1) + ", " + (j + 1) + ")   ");
					}

				}

				System.out.print("\n\nTotal time: ");

				for (int i = 0; i < n; i++) {
					System.out.print("t[" + (i + 1) + "] = " + md.getValue(y[i]) + "   ");
				}
			} else {
				System.out.print("Negative infinity");
			}

			md.end(); // To free up resources. Very important, especially when you run a CPLEX project many times or run many CPLEX projects!
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		MulticastRouting mr = new MulticastRouting();
		mr.MPmodel();
	}

}
