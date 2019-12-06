package cuonglv.chocoapplications;

import java.io.File;
import java.util.Scanner;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

public class MVRPWT {

	private int v, capacity[], maxDis[], workingTime[][];
	private int n, distance[][], timeWindow[][], weight[], deliDur[];
	private int t[][];

	private Model model;
	private BoolVar x[][][];
	private IntVar accDis[], accWei[], accTime[][];
	private IntVar obj;

	public void readData(String input) {
		try {
			File f = new File(input);
			Scanner scan = new Scanner(f);

			this.v = scan.nextInt();
			capacity = new int[v];
			maxDis = new int[v];
			workingTime = new int[v][2];

			for (int i = 0; i < v; i++) {
				capacity[i] = scan.nextInt();
				maxDis[i] = scan.nextInt();
				workingTime[i][0] = scan.nextInt();
				workingTime[i][1] = scan.nextInt();
			}

			n = scan.nextInt();
			weight = new int[n];
			deliDur = new int[n];
			timeWindow = new int[n][2];
			distance = new int[n][n];
			t = new int[n][n];

			for (int i = 0; i < n; i++) {
				weight[i] = scan.nextInt();
				deliDur[i] = scan.nextInt();
			}

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					distance[i][j] = scan.nextInt();
				}
			}

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					t[i][j] = scan.nextInt();
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

	public void stateModel() {
		model = new Model();
		x = new BoolVar[n][n][v];
		accTime = new IntVar[n][v];
		accWei = new IntVar[v];
		accDis = new IntVar[v];

		for (int i = 0; i < n; i++) {
			x[i] = model.boolVarMatrix(n, v);
		}
		
		BoolVar[] tmp1 = new BoolVar[n * v];
		BoolVar[] tmp2 = new BoolVar[n * v];
		int idx = 0;

		// at most this.v vehicles depart from the depot 0
		// at most this.v vehicles turn back to the depot
		for (int i = 0; i < n; i++) {
			for (int v = 0; v < this.v; v++) {
				tmp1[idx] = x[i][0][v];
				tmp2[idx++] = x[0][i][v];
			}
		}
		model.sum(tmp1, "<=", this.v).post();
		model.sum(tmp2, "<=", this.v).post();

		for (int i = 1; i < n; i++) {
			// tmp1 for creating the constraint to assures that exactly one vehicle enters to location i
			// tmp2 for creating the constraint to assures that exactly one vehicle departs from location i
			tmp1 = new BoolVar[v * (n - 1)];
			tmp2 = new BoolVar[v * (n - 1)];
			idx = 0;
			for (int j = 0; j < n; j++) {
				if (i != j) {
					for (int v = 0; v < this.v; v++) {
						tmp1[idx] = x[j][i][v];
						tmp2[idx++] = x[i][j][v];
					}
				}
			}
			model.sum(tmp1, "=", 1).post();
			model.sum(tmp2, "=", 1).post();
		}

		// if vehicle v enters to location i, v must also depart from i
		for (int v = 0; v < this.v; v++) {
			for (int i = 0; i < this.n; i++) {
				idx = 0;
				IntVar[] balance = new IntVar[2 * n - 2];
				for (int j = 0; j < this.n; j++) {
					if (i != j) {
						balance[idx++] = x[i][j][v];
						balance[idx++] = model.intMinusView(x[j][i][v]);
					}
				}
				model.sum(balance, "=", 0).post();
			}
		}

		// creates the accumulative variables of trips distance and payload
		// the accumulative variables have domains which assure that total trip distance and payload of vehicle v do not exceed its limitations
		for (int v = 0; v < this.v; v++) {
			this.accDis[v] = model.intVar(0, this.maxDis[v]);
			this.accWei[v] = model.intVar(0, this.capacity[v]);
			tmp1 = new BoolVar[n * n];
			int w[] = new int[n * n];
			int d[] = new int[n * n];
			idx = 0;
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					w[idx] = this.weight[j];
					d[idx] = this.distance[i][j];
					tmp1[idx++] = x[i][j][v];
				}
			}
			model.scalar(tmp1, w, "=", this.accWei[v]).post();
			model.scalar(tmp1, d, "=", this.accDis[v]).post();
		}
		
		// creates the accumulative time variables
		// the variables have domains which assure that vehicle v must enter the location i within the time window
		for (int i=0; i<n; i++) {
			for (int v=0; v<this.v; v++) {
				accTime[i][v] = model.intVar(this.timeWindow[i][0], this.timeWindow[i][1]);
			}
		}

		// if vehicle v travels from location i to j
		// accTime[j] = accTime[i] + deliveryDuration[i] + time to go from i to j
		for (int i = 0; i < n; i++) {
			for (int j = 1; j < n; j++) {
				if (i != j) {
					for (int v = 0; v < this.v; v++) {
						model.ifThen(x[i][j][v], model.arithm(this.accTime[j][v], "=",
								model.intOffsetView(this.accTime[i][v], this.t[i][j] + this.deliDur[i])));
					}
				}
			}
		}
		
		for (int v=0; v<this.v; v++) {
			for (int i=1; i<n; i++) {
				// start working time constraint
				//model.ifThen(x[0][i][v], model.arithm(this.accTime[0][v], ">=", this.workingTime[v][0]));
				
				// end working time constraint
				//model.ifThen(x[i][0][v], model.arithm(this.accTime[i][v], "<=", this.workingTime[v][1]-this.t[i][0]-this.deliDur[i]));
			}
		}
		
		this.obj = model.intVar(0, 3000);
		model.sum(this.accDis, "=", this.obj).post();
		model.setObjective(model.MINIMIZE, this.obj);

	}

	public void solve() {
		Solver solver = this.model.getSolver();
		if (solver.solve()) {
			this.printSolution();
		} else if (solver.hasEndedUnexpectedly()) {
			System.out.println("Could not find a solution nor prove that none exists :(");
		} else {
			System.out.println("NON-SOLUTION :(");
		}
	}

	public void printSolution() {
		System.out.println("Objective value: " + model.getObjective());
		for (int v = 0; v < this.v; v++) {
			int returnTime=0, departTime = this.accTime[0][v].getValue();
			System.out.print("Vehicle " + v + ": 0 (DepartTime: " + departTime);
			int next = 0;
			for (int j = 0; j < n; j++) {
				if (x[0][j][v].getValue() > 0) {
					next = j;
					System.out.print(") --> " + next + " (Reaching:" + this.accTime[next][v].getValue() + ", leaving: "
							+ (this.accTime[next][v].getValue() + this.deliDur[next]) + ")");
				}
			}
			while (next != 0) {
				for (int j = 0; j < n; j++) {
					if (x[next][j][v].getValue() > 0) {
						int pre = next;
						next = j;
						System.out.print(" --> " + j);
						if (j != 0) {
							System.out.print(" (Reaching:" + this.accTime[j][v].getValue() + ", leaving: "
									+ (this.accTime[j][v].getValue() + this.deliDur[j]) + ")");
						} else {
							returnTime = this.accTime[pre][v].getValue() + this.deliDur[pre] + this.t[pre][0];
							System.out.print("(ReturnTime: (" + returnTime + ")");
						}
						break;
					}
				}
			}
			System.out.println("\n\tTotal distance: " + this.accDis[v].getValue());
			System.out.println("\tTotal payload: " + this.accWei[v].getValue());
			System.out.println("\tTotal trip time: " + (returnTime-departTime));
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MVRPWT test = new MVRPWT();
		test.readData("data/VehiclesRoutingProblems/CMVRPWT-CP-Choco.txt");
		test.stateModel();
		test.solve();

	}

}
