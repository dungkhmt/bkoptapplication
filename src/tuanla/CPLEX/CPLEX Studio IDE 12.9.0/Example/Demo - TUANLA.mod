/*********************************************
 * OPL 12.9.0.0 Model
 * Author: Asus
 * Creation Date: Nov 1, 2019 at 3:07:39 PM
 *********************************************/

dvar float+ x;  // x >= 0.
dvar float+ y;  // y >= 0.

dexpr float cost = 0.12*x + 0.15*y;  // cost: objective function.
minimize cost;

subject to {
	cons1: 60*x + 60*y >= 300;
	cons2: 12*x + 6*y >= 36;
	cons3: 10*x + 30*y >= 90;
}

// post processing: do something after solving.
execute {
	if (cplex.getCplexStatus() == 1) {
		writeln("Perfect!");	
	}
	else {
		writeln("Error!")	
	}
}