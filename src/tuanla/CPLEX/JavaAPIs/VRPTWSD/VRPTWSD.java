/*
 * This program solves VRPTWSD using the MIP model. Please read the file "Vehnicle_Routing_Problem.pdf", section 3 for more details about the model for this problem.
 */


package myself;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class VRPTWSD {
	
	private int n;  // Number of customers.
	private int m;  // Number of vehicles.
	private int[][] distance;  // Distance matrix.
	private int[][] time;  // Time matrix.
	private int[] e;
	private int[] l;
	private int[] a;
	private int[] q; 
	private int[] s = new int[] {};
	
	public void readData(File input) {
		try {
			Scanner sc = new Scanner(input);
			n = sc.nextInt();
			m = sc.nextInt();
			distance = new int[n][n];
			time = new int[n][n];
			e = new int[n];
			l = new int[n];
			a = new int[m];
			q = new int[n];
			s = new int[n];
			
			for (int i=0; i<n; i++) {
				int j=0;
				
				for (; j<n; j++) {
					distance[i][j] = sc.nextInt();
				}
				
				for (; j<2*n; j++) {
					time[i][j-n] = sc.nextInt();
				}
			}
			
			for (int i=0; i<n; i++)	{
				e[i] = sc.nextInt();
				l[i] = sc.nextInt();
			}
			
			for (int i=0; i<m; i++) {
				a[i] = sc.nextInt();
			}
			
			for (int i=0; i<n; i++) {
				q[i] = sc.nextInt();
			}
			
			for (int i=0; i<n; i++) {
				s[i] = sc.nextInt();
			}
			
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void modelAndSolve() {
		try {			
			// Engine.
			IloCplex md = new IloCplex();
			
			// Variables.
			int count = 0;
			IloIntVar[][][] x = new IloIntVar[n][n][m];
			IloNumVar[][] y = new IloNumVar[n][m];
			IloNumVar[][] b = new IloNumVar[n][m];

			for (int i=0; i<n; i++) {
				for (int j=0; j<n; j++) {
					for (int k=0; k<m; k++) {
						if (i == j) {
							x[i][j][k] = md.intVar(0, 0);
						} else {
							x[i][j][k] = md.intVar(0, 1);
						}
					}					
				}
			}
			
			for (int i=1; i<n; i++) {
				for (int k=0; k<m; k++) {
					y[i][k] = md.numVar(0.0, 1.0);
					b[i][k] = md.numVar(0.0, 23.0);
				}
			}
			
			
			// Objective function.
			IloLinearNumExpr objective = md.linearNumExpr();

			for (int i=0; i < n; i++) {
				for (int j=0; j < n; j++) {
					for (int k=0; k<m; k++) {
						objective.addTerm(distance[i][j], x[i][j][k]);
					}				
				}
			}

			md.addMinimize(objective);

			
			// Constraints.
			// Constraint 1.
			IloLinearNumExpr[] cons1 = new IloLinearNumExpr[m];
			
			for (int k=0; k<m; k++) {
				cons1[k]= md.linearNumExpr();
				
				for (int j=1; j<n; j++) {
					cons1[k].addTerm(1, x[0][j][k]);					
				}
				
				md.addEq(cons1[k], 1);
			}

			// Constraint 2.
			IloLinearNumExpr[] cons2 = new IloLinearNumExpr[n*m];
			
			for (int k=0; k<m; k++) {				
				for (int p=0; p<n; p++) {
					cons2[count]= md.linearNumExpr();
					
					for (int i=0; i<n; i++) {
						cons2[k].addTerm(1, x[i][p][k]);
						cons2[k].addTerm(-1, x[p][i][k]);
					}
					
					md.addEq(cons2[k], 0);
					count++;
				}	
			}				
			
			// Constraint3.
			IloLinearNumExpr[] cons3 = new IloLinearNumExpr[n];
			
			for (int i=1; i<n; i++) {
				cons3[i] = md.linearNumExpr();
				
				for (int k=0; k<m; k++) {
					cons3[i].addTerm(1, y[i][k]);
				}
				
				md.addEq(cons3[i], 1);
			}			
			
			// Constraint4.
			IloLinearNumExpr[] cons4 = new IloLinearNumExpr[m];
			
			for (int k=0; k<m; k++) {
				cons4[k] = md.linearNumExpr();
				
				for (int i=1; i<n; i++) {
					cons4[k].addTerm(q[i], y[i][k]);
				}
				
				md.addLe(cons4[k], a[k]);
			}						
						
			// Constraint5.
			IloLinearNumExpr[] cons5 = new IloLinearNumExpr[(n-1)*m];
			count = 0;
			
			for (int i=1; i<n; i++) {
				for (int k=0; k<m; k++) {
					cons5[count] = md.linearNumExpr();
					
					for (int j=0; j<n; j++) {
						cons5[count].addTerm(1, x[j][i][k]);
					}
					
					md.addGe(cons5[count], y[i][k]);
					count++;
				}
			}			
			
			// Constraint6.						
			for (int i=1; i<n; i++)	{
				for (int j=1; j<n; j++) {
					for (int k=0; k<m; k++) {					
						md.addLe(md.sum(md.prod(1, b[i][k]),
									    md.prod(32000, x[i][j][k]),
									    md.prod(-1, b[j][k])), 32000 - s[i] - time[i][j]);
					}
				}
			}									
			
			// Constraint7.
			for (int k=0; k<m; k++) {
				for (int i=1; i<n; i++) {
					md.addGe(md.prod(1, b[i][k]), e[i]);
					md.addLe(md.prod(1, b[i][k]), l[i]);
				}
			}			
						
						
			// Solve and print solution.
			if (md.solve()) {			
				System.out.println("\n\nOptimal solution:");
				System.out.printf("Total distance: %.1f", md.getObjValue());
				
				for (int k=0; k<m; k++) {
					int curr_vertex = 0;
					
					System.out.print("\n\nRoute for vehicle " + k + ":\n0");
					
					while(true) {						
						for(int j=0; j<n; j++) {
							if ((int)round(md.getValue(x[curr_vertex][j][k])) == 1) {								
								curr_vertex = j;
								break;							
														
							}
						}							
						
						if (curr_vertex == 0) break;
						else {							
							System.out.printf("  -->  %d Time(%.1f, %.1f)", curr_vertex,
																		    md.getValue(b[curr_vertex][k]),
																		    md.getValue(b[curr_vertex][k]) + s[curr_vertex]);
						}
					}
				}
				
			} else {
				System.out.print("Negative infinity");
			}
			
			md.end();
			
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public static double round(double value) {
	    return new BigDecimal(((Double)value).toString()).setScale(0,RoundingMode.HALF_UP).doubleValue();
	}
	
	public static void main(String[] args) {
		
		VRPTWSD model = new VRPTWSD();
		
		model.readData(new File("VRPTWSD data.txt"));
		model.modelAndSolve();			               
	}
}
