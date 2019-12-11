package cuonglv.cplexapplications;

import java.io.File;
import java.util.Scanner;

import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

public class CVRPTW {

	private final int M = 999999;

	IloCplex cplex;
	IloIntVar[][][] x; // x[i][j][v] indicates whether vehicle v travels from location i to location j
	IloIntVar s[][]; // s[i][v] is the time at which vehicle v starts servicing at i
	IloIntExpr accDistance[];
	IloIntExpr accPayload[];

	int k, capacity[], workingTime[][];
	int n, distance[][], timeWindow[][], demand[], deliDur[];
	int moveTime[][];

	public void readData(String input) {
		try {
			File f = new File(input);
			Scanner scan = new Scanner(f);

			this.k = scan.nextInt();
			capacity = new int[k];
			workingTime = new int[k][2];
			
			for (int i = 0; i < k; i++) {
				capacity[i] = scan.nextInt();
				workingTime[i][0] = scan.nextInt();
				workingTime[i][1] = scan.nextInt();
			}

			n = scan.nextInt();
			demand = new int[n];
			deliDur = new int[n];
			timeWindow = new int[n][2];
			distance = new int[n][n];
			moveTime = new int[n][n];

			for (int i = 0; i < n; i++) {
				demand[i] = scan.nextInt();
				deliDur[i] = scan.nextInt();
			}

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					distance[i][j] = scan.nextInt();
				}
			}

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					moveTime[i][j] = scan.nextInt();
				}
			}

			for (int i = 0; i < n; i++) {
				timeWindow[i][0] = scan.nextInt();
				timeWindow[i][1] = scan.nextInt();
			}

			scan.close();
			System.out.println("Reading data successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stateModel() throws IloException {
		cplex = new IloCplex();
		x = new IloIntVar[n][n][k];
		s = new IloIntVar[n][k];
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				x[i][j] = cplex.boolVarArray(k);
			}
		}
		
		for (int i = 0; i < n; i++) {
			for (int v=0; v<this.k; v++) {
				s[i][v] = cplex.intVar(this.timeWindow[i][0], this.timeWindow[i][1]);
			}
		}

		// at most V vehicles depart from depot 0
		IloIntExpr[] temp1 = new IloIntExpr[n * k];
		int idx = 0;
		for (int j = 0; j < n; j++) {
			for (int v = 0; v < this.k; v++) {
				temp1[idx++] = cplex.prod(1, x[0][j][v]);
			}
		}
		cplex.addLe(cplex.sum(temp1), this.k);
		
		// exactly one vehicle enters to each location
		for (int j = 1; j < n; j++) {
			IloIntExpr[] temp = new IloIntExpr[(n - 1) * this.k];
			idx = 0;
			for (int i = 0; i < n; i++) {
				if (i != j) {
					for (int v = 0; v < this.k; v++) {
						temp[idx++] = cplex.prod(x[i][j][v], 1);
					}
				}

			}
			cplex.addEq(cplex.sum(temp), 1);
		}

		// if vehicle v enters to location i, v must leave i 
		for (int v = 0; v < this.k; v++) {
			for (int i = 0; i < this.n; i++) {
				IloIntExpr[] depart = new IloIntExpr[n - 1];
				IloIntExpr[] enter = new IloIntExpr[n - 1];
				idx = 0;
				for (int j = 0; j < this.n; j++) {
					if (i != j) {
						depart[idx] = cplex.prod(1, x[i][j][v]);
						enter[idx++] = cplex.prod(1, x[j][i][v]);
					}
				}
				cplex.addEq(cplex.sum(depart), cplex.sum(enter));
			}
		}

		// constraints for total payload per vehicle
		accDistance = new IloIntExpr[k];
		accPayload = new IloIntExpr[k];
		for (int v = 0; v < this.k; v++) {
			IloIntExpr tmp1[] = new IloIntExpr[n * n];
			IloIntExpr tmp2[] = new IloIntExpr[n * n];
			idx = 0;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					tmp1[idx] = cplex.prod(this.distance[i][j], x[i][j][v]);
					tmp2[idx++] = cplex.prod(this.demand[j], x[i][j][v]);
				}
			}
			accDistance[v] = cplex.sum(tmp1);
			accPayload[v] = cplex.sum(tmp2);
			cplex.addLe(accPayload[v], this.capacity[v]);
		}

		// time constraints
		for (int v = 0; v < this.k; v++) {
			for (int i = 0; i < n; i++) {
				for (int j = 1; j < n; j++) {
					// time windows constraint
					cplex.addLe(
							cplex.sum(cplex.prod(1, s[i][v]), cplex.prod(-1, s[j][v]), cplex.prod(this.M, x[i][j][v])),
							this.M - this.moveTime[i][j] - this.deliDur[i]);
				}
				// start working time constraint
				cplex.addGe(cplex.sum(cplex.prod(1, s[0][v]), cplex.prod(-this.M, x[0][i][v])),
						this.workingTime[v][0] + -this.M);
				
				// end working time constraint
				cplex.addLe(cplex.sum(cplex.prod(1, s[i][v]), cplex.prod(this.M, x[i][0][v])),
						this.workingTime[v][1] - this.moveTime[i][0] - this.deliDur[i] + this.M);
			}
		}

		IloIntExpr objectiveFunc = cplex.sum(accDistance);
		cplex.addMinimize(objectiveFunc);

	}

	public void solve() throws IloException {
		System.out.println("Start solving...");
		if (this.cplex.solve()) {
			cplex.output().println("Solution status = " + cplex.getStatus());
			this.printSolution();
		}
		else {
			System.out.println("NON-SOLUTION :(");
		}
		cplex.end();
	}

	public void printSolution() throws UnknownObjectException, IloException {
		double totalDis=0;
		for (int v = 0; v < this.k; v++) {
			int next = 0;
			double returnTime = 0, departTime = cplex.getValue(this.s[0][v]);
			System.out.print("Vehicle " + v + ": \n\tPath: 0(Leaving: " + departTime + ")");
			for (int i = 0; i < n; i++) {
				if (cplex.getValue(x[0][i][v])>0) {
					next = i;
					double reaching = cplex.getValue(s[0][v]) + this.deliDur[0] + this.moveTime[0][next];
					System.out.print(" --> " + next + "(Reaching: " + reaching
							+ ", waiting: " + Math.max(0, this.timeWindow[next][0] - reaching)
							+ ", leaving: " + (cplex.getValue(s[next][v]) + this.deliDur[next]) + ")");
				}
			}
			while (next != 0) {
				for (int j = 0; j < n; j++) {
					if (cplex.getValue(x[next][j][v])>0) {
						int pre = next;
						next = j;
						System.out.print(" --> " + j);
						double reaching = cplex.getValue(s[pre][v]) + this.deliDur[pre] + this.moveTime[pre][next];
						if (j != 0) {
							System.out.print("(Reaching:" + reaching
									+ ", waiting: " + Math.max(0, this.timeWindow[j][0] - reaching)
									+ ", leaving: " + (cplex.getValue(s[j][v]) + this.deliDur[j]) + ")");
						} else {
							returnTime = reaching;
							System.out.print("(Reaching: (" + reaching + ")");
						}
						break;
					}
				}
			}
			System.out.println("\n\tTotal distance: " + cplex.getValue(this.accDistance[v]));
			System.out.println("\tTotal payload: " + cplex.getValue(this.accPayload[v]));
			System.out.println("\tTotal trip time: " + (returnTime-departTime));
			totalDis+=cplex.getValue(this.accDistance[v]);
		}
		cplex.output().println("\nObjective value ~ Total trips distance = " + totalDis);
		cplex.end();
	}

	public static void main(String[] args) throws IloException {
		// TODO Auto-generated method stub
		CVRPTW test = new CVRPTW();
		test.readData("data/VehiclesRoutingProblems/CVRPTW.txt");
		test.stateModel();
		test.solve();

	}

}