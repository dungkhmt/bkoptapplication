package cuonglv.cplexapplications;
import java.io.File;
import java.util.Scanner;

import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

public class NetworkBroadcast {
	
	private int[][] c;
	private int[][] l;
	private int n, m, L, s;
	private final int M=99999;
	
	private IloCplex model;
	private IloIntVar[][] x;
	private IloIntVar[] t;
	
	public void readData(String input) {
		try {
			File f = new File(input);
			Scanner scan = new Scanner(f);
			n = scan.nextInt();
			m = scan.nextInt();
			
			c = new int[n+1][n+1];
			l = new int[n+1][n+1];
			
			for (int i=0; i<n+1; i++) {
				for (int j=0; j<n+1; j++) {
					c[i][j] = this.M;
					l[i][j] = this.M;
				}
			}
			
			int u, v, t, w;
			for (int i=0; i<m; i++) {
				u = scan.nextInt();
				v = scan.nextInt();
				w = scan.nextInt();
				t = scan.nextInt();
				c[u][v] = w;
				c[v][u] = w;
				l[u][v] = t;
				l[v][u] = t;
			}
			
			s = scan.nextInt();
			L = scan.nextInt();
			
			scan.close();
			
			System.out.println("Reading data successfully.");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stateModel() throws IloException {
		this.model = new IloCplex();
		x = new IloIntVar[n+1][n+1];
		
		for (int i=0; i<=n; i++) {
			x[i] = model.boolVarArray(n+1);
		}
		this.t = model.intVarArray(n+1, 0, L);
		
		for (int i=2; i<=n; i++) {
			IloIntExpr[] temp = new IloIntExpr[n];
			for (int j=1; j<=n; j++) {
				temp[j-1] = model.prod(1, x[j][i]);
			}
			model.addEq(model.sum(temp), 1);
		}
		
		for (int i=1; i<=n; i++) {
			if (i==s) {
				model.addEq(model.prod(1, t[s]), 0);
			}
			else {
				model.addLe(model.prod(1, t[i]), L);
			}
		}
		
		for (int i=1; i<=n; i++) {
			for (int j=1; j<=n; j++) {
				if (l[j][i]<this.M && i!=this.s) {
					model.addLe(model.sum(model.prod(1, t[i]),
										  model.prod(this.M, x[j][i]),
										  model.prod(-1, t[j])), this.M + l[j][i]);
					
					model.addGe(model.sum(model.prod(1, t[i]),
										  model.prod(-this.M, x[j][i]),
										  model.prod(-1, t[j])), -this.M + l[j][i]);
				}
			}
		}
		IloIntExpr[] temp = new IloIntExpr[n];
		for (int i=1; i<=n; i++) {
			temp[i-1] = model.scalProd(x[i], c[i]);
		}
		model.addMinimize(model.sum(temp));
		
	}
	
	public void solve() throws IloException {
		System.out.println("Start solving...");
		boolean res = this.model.solve();
		if (res) {
			model.output().println("Solution status = " + model.getStatus());
			model.output().println("Total cost = " + model.getObjValue());
			for (int i=1; i<=n; i++) {
				for (int j=1; j<=n; j++) {
					if (model.getValue(x[i][j])==1) {
						System.out.println(i + " --> " + j +" - Time to reach to " + j + ": " + model.getValue(t[j]));
					}
				}
			}
		}
		model.end();
	}
	
	public static void main(String[] args) throws IloException {
		NetworkBroadcast test = new NetworkBroadcast();
		test.readData("data/NetworkBroadcast/NetworkBroadcast.txt");
		test.stateModel();
		test.solve();
	}	
}