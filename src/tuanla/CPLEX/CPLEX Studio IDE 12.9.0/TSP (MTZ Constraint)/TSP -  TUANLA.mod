/*********************************************
 * OPL 12.9.0.0 Model
 * Author: Asus
 * Creation Date: Oct 31, 2019 at 2:53:32 PM
 *********************************************/

// The number of cities
int n =...;
range cities = 1..n;

tuple location {
	float x;
	float y;
}

tuple edge {
	int i; // citi i.
	int j; // city j.
}

setof(edge) edges = {<i, j> | i, j in cities: i != j};
float c[edges];
location cityLocation[cities]; // store cordinates of cities.

// pre-processing: do something before solving problem.
execute {
	
	// Generate random data
	function calDistance(city1, city2) {
		return Opl.sqrt(Opl.pow(city1.x - city2.x, 2) + Opl.pow(city1.y - city2.y, 2));	
	}
	
	for (var i in cities) {
		cityLocation[i].x = Opl.rand(100); // return random integer value in range [0, 100].
		cityLocation[i].y = Opl.rand(100);
	}
	
	for (var e in edges) {
	c[e] = calDistance(cityLocation[e.i], cityLocation[e.j]);	
	}
}

// create model
// decision variable

dvar boolean x[edges]; // binary variable.
dvar float u[2..n] in 1..n-1;

// modeling

dexpr float TotalCost = sum(e in edges) c[e]*x[e];

minimize TotalCost;

subject to {
	
	forall (j in cities)
		flow_in: // the name of constraint.
		sum(i in cities: i != j) x[<i, j>] == 1; // cannot write x[i][j] because x is tuple.	
	
	forall (i in cities)
	  flow_out:
	  sum(j in cities: i != j) x[<i, j>] == 1;
	  
	forall (i in cities: i>1, j in cities: j>1 && j != i)
	  subtour_elimination:
	  u[i] - u[j] + n*x[<i, j>] <= n - 1;
}

/* Control flow.
main {
	var model = thisOplModel.modelDefinition;
	var data = thisOplModel.dataElements;
	for (var size=5; size<=50; size += 5) {
		var MyCplex = new IloCplex();
		var opl = new IloOplModel(model, MyCplex);
		data.n = size;
		opl.addDataSource(data);
		opl.generate();
		if (MyCplex.solve()) {
			writeln("Solution: ", MyCplex.getObjValue(),
			", size: ", size,
			", time: ", MyCplex.getCplexTime());		
		}
		opl.end();
		MyCplex.end();		
	}
}*/
