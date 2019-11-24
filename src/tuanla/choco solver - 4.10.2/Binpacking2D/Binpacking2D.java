/*
 * Note: This program uses choco solver 4.10.2. You can find installer in folder softwares.
 */

package myself;

import java.io.File;
import java.util.Scanner;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class Binpacking2D {

	private int W, H;
	private int n;
	private int[] w;
	private int[] h;
	
	private void readData(String fn) {
		
		try {
			Scanner in = new Scanner(new File(fn));
			W = in.nextInt();
			H = in.nextInt();
			n = in.nextInt();
			w = new int[n];
			h = new int[n];
			
			for (int i=0; i<n; i++) {
				w[i] = in.nextInt();
				h[i] = in.nextInt();
			}
			
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void modelAndSolve() {
		
		// Model.
		Model md = new Model("Binpacking2D");
		
		// Variables.
		IntVar[] o = md.intVarArray("o", n, 0, 1);
		IntVar[] x = md.intVarArray("x", n, 0, W-1);
		IntVar[] y = md.intVarArray("y", n, 0, H-1);
		
		// Constraints.		
		// Prevent overlapping.
		for (int i=0; i<n-1; i++) {
			for (int j=i+1; j<n; j++) {
				
				md.ifThen(md.and(md.arithm(o[i], "=", 0), md.arithm(o[j], "=", 0)),
						  md.or(md.arithm(x[j], "-", x[i], ">=", w[i]),
								md.arithm(x[i], "-", x[j], ">=", w[j]),
								md.arithm(y[j], "-", y[i], ">=", h[i]),
								md.arithm(y[i], "-", y[j], ">=", h[j])));
				
				md.ifThen(md.and(md.arithm(o[i], "=", 0), md.arithm(o[j], "=", 1)),
						  md.or(md.arithm(x[j], "-", x[i], ">=", w[i]),
								md.arithm(x[i], "-", x[j], ">=", h[j]),
								md.arithm(y[j], "-", y[i], ">=", h[i]),
								md.arithm(y[i], "-", y[j], ">=", w[j])));
				
				md.ifThen(md.and(md.arithm(o[i], "=", 1), md.arithm(o[j], "=", 0)),
						  md.or(md.arithm(x[j], "-", x[i], ">=", h[i]),
								md.arithm(x[i], "-", x[j], ">=", w[j]),
								md.arithm(y[j], "-", y[i], ">=", w[i]),
								md.arithm(y[i], "-", y[j], ">=", h[j])));
				
				md.ifThen(md.and(md.arithm(o[i], "=", 1), md.arithm(o[j], "=", 1)),
						  md.or(md.arithm(x[j], "-", x[i], ">=", h[i]),
								md.arithm(x[i], "-", x[j], ">=", h[j]),
								md.arithm(y[j], "-", y[i], ">=", w[i]),
								md.arithm(y[i], "-", y[j], ">=", w[j])));
			}
		}
		
		// All item locates completely in container.
		for(int i=0; i<n; i++) {
			md.ifThen(md.arithm(o[i], "=", 0), md.and(md.arithm(x[i], "<=", W - w[i]),
													  md.arithm(y[i], "<=", H - h[i])));
		
			md.ifThen(md.arithm(o[i], "=", 1), md.and(md.arithm(x[i], "<=", W - h[i]),
					  								  md.arithm(y[i], "<=", H - w[i])));
		}
		
		// Solve and print solution.
		Solver solver = md.getSolver();
		
		if (solver.solve()) {
			
			System.out.println("Solution:");			
			System.out.println("     item          o          from, to");
			
			for (int i=0; i<n; i++) {
				
				if (i<9) {
					System.out.print("      " + (i+1) + "            " + o[i].getValue() + "          (" 
								   + x[i].getValue() + ", " + y[i].getValue() + "), ");
				} else {
					System.out.print("      " + (i+1) + "           " + o[i].getValue() + "          (" 
							   + x[i].getValue() + ", " + y[i].getValue() + "), ");
				}
				
				if (o[i].getValue() == 0) {
					System.out.println("(" + (x[i].getValue() + w[i]) + ", " + (y[i].getValue() + h[i]) + ")");
				} else {
					System.out.println("(" + (x[i].getValue() + h[i]) + ", " + (y[i].getValue() + w[i]) + ")");
				}
			}
		}
		else {
			System.out.print("No solution");
		}		
	}
	
	public static void main(String[] args) {
		
		Binpacking2D bp = new Binpacking2D();
		bp.readData("BinPacking2D-W19-H19-I21.txt");
		bp.modelAndSolve();
	}
}
