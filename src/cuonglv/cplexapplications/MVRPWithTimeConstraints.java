package cuonglv.cplexapplications;

import java.io.File;
import java.util.Scanner;

import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

public class MVRPWithTimeConstraints {

	private final int M = 999999;

	IloCplex cplex;
	IloIntVar[][][] x; // x[i][j][v] indicates whether vehicle v travels from location i to location j
	IloIntVar s[][]; // if vehicle v travels to location i, s[i][v] is the time at which vehicle v reach to i
	IloIntExpr accumulativeDistance[];
	IloIntExpr accumulativeLoading[];

	int v, capacity[], maxDis[], timeline[];
	int n, distance[][], timeWindow[][], demand[], timeForLoadingGoods[];
	int time[][];

	public void readData(String input) {
		try {
			File f = new File(input);
			Scanner scan = new Scanner(f);

			this.v = scan.nextInt();
			capacity = new int[v];
			maxDis = new int[v];
			timeline = new int[v];
			
			for (int i = 0; i < v; i++) {
				capacity[i] = scan.nextInt();
				maxDis[i] = scan.nextInt();
				timeline[i] = scan.nextInt();
			}

			n = scan.nextInt();
			demand = new int[n];
			timeForLoadingGoods = new int[n];
			timeWindow = new int[n][2];
			distance = new int[n][n];
			time = new int[n][n];

			for (int i = 0; i < n; i++) {
				demand[i] = scan.nextInt();
				timeForLoadingGoods[i] = scan.nextInt();
			}

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					distance[i][j] = scan.nextInt();
				}
			}

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					time[i][j] = scan.nextInt();
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
		x = new IloIntVar[n][n][v];
		s = new IloIntVar[n][v];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				x[i][j] = cplex.boolVarArray(v);
			}
		}

		for (int i = 0; i < n; i++) {
			s[i] = cplex.intVarArray(this.v, timeWindow[i][0],  timeWindow[i][1] - this.timeForLoadingGoods[i]);
		}

		// at most this.v vehicles depart from depot 0
		IloIntExpr[] temp1 = new IloIntExpr[n * v];
		int idx = 0;
		for (int j = 0; j < n; j++) {
			for (int v = 0; v < this.v; v++) {
				temp1[idx++] = cplex.prod(1, x[0][j][v]);
			}
		}
		cplex.addLe(cplex.sum(temp1), this.v);

		// at most this.v vehicles turn back to depot 0
		IloIntExpr[] temp2 = new IloIntExpr[n * v];
		idx = 0;
		for (int i = 0; i < n; i++) {
			for (int v = 0; v < this.v; v++) {
				temp2[idx++] = cplex.prod(1, x[i][0][v]);
			}
		}
		cplex.addLe(cplex.sum(temp2), this.v);

		// exactly one vehicle leaves from each location
		for (int i = 1; i < n; i++) {
			IloIntExpr[] temp = new IloIntExpr[(n - 1) * v];
			idx = 0;
			for (int j = 0; j < n; j++) {
				if (i != j) {
					for (int v = 0; v < this.v; v++) {
						temp[idx++] = cplex.prod(x[i][j][v], 1);
					}
				}
			}
			cplex.addEq(cplex.sum(temp), 1);
		}

		// exactly one vehicle enters to each location
		for (int j = 1; j < n; j++) {
			IloIntExpr[] temp = new IloIntExpr[(n - 1) * this.v];
			idx = 0;
			for (int i = 0; i < n; i++) {
				if (i != j) {
					for (int v = 0; v < this.v; v++) {
						temp[idx++] = cplex.prod(x[i][j][v], 1);
					}
				}

			}
			cplex.addEq(cplex.sum(temp), 1);
		}

		// if vehicle v enters to location i, v must leave i 
		for (int v = 0; v < this.v; v++) {
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

		// constraints for total distance and total payload per vehicle
		accumulativeDistance = new IloIntExpr[v];
		accumulativeLoading = new IloIntExpr[v];
		for (int v = 0; v < this.v; v++) {
			IloIntExpr tmp1[] = new IloIntExpr[n * n];
			IloIntExpr tmp2[] = new IloIntExpr[n * n];
			idx = 0;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					tmp1[idx] = cplex.prod(this.distance[i][j], x[i][j][v]);
					tmp2[idx++] = cplex.prod(this.demand[j], x[i][j][v]);
				}
			}
			accumulativeDistance[v] = cplex.sum(tmp1);
			accumulativeLoading[v] = cplex.sum(tmp2);
			cplex.addLe(accumulativeDistance[v], this.maxDis[v]);
			cplex.addLe(accumulativeLoading[v], this.capacity[v]);
		}

		// time constraints
		for (int v = 0; v < this.v; v++) {
			for (int i = 0; i < n; i++) {
				for (int j = 1; j < n; j++) {
					// s[i][v] + timeForLoadingGoods[i] + time[i][j] <= s[j][v] + M(1-x[i][j][v]
					cplex.addLe(
							cplex.sum(cplex.prod(1, s[i][v]), cplex.prod(-1, s[j][v]), cplex.prod(this.M, x[i][j][v])),
							this.M - this.time[i][j] - this.timeForLoadingGoods[i]);

					// s[i][v] + timeForLoadingGoods[i] + time[i][j] >= s[j][v] - M(1-x[i][j][v]
					cplex.addGe(
							cplex.sum(cplex.prod(1, s[i][v]), cplex.prod(-1, s[j][v]), cplex.prod(-this.M, x[i][j][v])),
							-this.M - this.time[i][j] - this.timeForLoadingGoods[i]);

					// s[j][v] + timeForLoadingGoods[j] + M(x[i][j][v] -1) <= timeWindow[j][1]
					cplex.addLe(cplex.sum(cplex.prod(1, s[j][v]), cplex.prod(this.M, x[i][j][v])),
							this.M + this.timeWindow[j][1] - this.timeForLoadingGoods[j]);
					
					// s[j][v] - M(x[i][j][v] -1) >= timeWindow[j][0]
					cplex.addGe(cplex.sum(cplex.prod(1, s[j][v]), cplex.prod(-this.M, x[i][j][v])),
							-this.M + this.timeWindow[j][0]);

				}
				// s[i][v] + this.time[i][0] + this.timeForLoadingGoods[i] + M(x[i][0][v] - 1) <= this.timeline[v]
				cplex.addLe(cplex.sum(cplex.prod(1, s[i][v]), cplex.prod(this.M, x[i][0][v])),
						this.timeline[v] - this.time[i][0] - this.timeForLoadingGoods[i] + this.M);
			}
		}

		IloIntExpr objectiveFunc = cplex.sum(accumulativeDistance);
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
		for (int v = 0; v < this.v; v++) {
			int next = 0;
			double returnTime = 0, departTime = cplex.getValue(this.s[0][v]);
			System.out.print("Vehicle " + v + ": \n\tPath: 0(Leaving: " + departTime + ")");
			for (int i = 0; i < n; i++) {
				if (cplex.getValue(x[0][i][v])>0) {
					next = i;
					System.out.print(" --> " + next + "(Reaching: " + cplex.getValue(s[next][v]) + ", leaving: "
							+ (cplex.getValue(s[next][v]) + this.timeForLoadingGoods[next]) + ")");
				}
			}
			while (next != 0) {
				for (int j = 0; j < n; j++) {
					if (cplex.getValue(x[next][j][v])>0) {
						int pre = next;
						next = j;
						System.out.print(" --> " + j);
						if (j != 0) {
							System.out.print("(Reaching:" + cplex.getValue(s[j][v]) + ", leaving: "
									+ (cplex.getValue(s[j][v]) + this.timeForLoadingGoods[j]) + ")");
						} else {
							returnTime = cplex.getValue(s[pre][v]) + this.timeForLoadingGoods[pre] + this.time[pre][0];
							System.out.print("(Reaching: (" + returnTime + ")");
						}
						break;
					}
				}
			}
			System.out.println("\n\tTotal distance: " + cplex.getValue(this.accumulativeDistance[v]));
			System.out.println("\tTotal payload: " + cplex.getValue(this.accumulativeLoading[v]));
			System.out.println("\tTotal trip time: " + (returnTime-departTime));
			totalDis+=cplex.getValue(this.accumulativeDistance[v]);
		}
		cplex.output().println("\nObjective value ~ Total trips distance = " + totalDis);
		cplex.end();
	}

	public static void main(String[] args) throws IloException {
		// TODO Auto-generated method stub
		MVRPWithTimeConstraints test = new MVRPWithTimeConstraints();
		test.readData("data/VehiclesRoutingProblems/MVRPWithTimeWindows-MIP.txt");
		test.stateModel();
		test.solve();

	}

}
