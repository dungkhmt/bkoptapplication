from __future__ import print_function
from ortools.linear_solver import pywraplp

N = 8  # Number of vertex.

# Cost matrix, c[i][j] = c[j][i] = positive infinity ~ no direct link between vertex i and vertex j.
c = [[0, 3, 2, 4, 10000, 10000, 10000, 10000],
     [3, 0, 6, 10000, 2, 10000, 10000, 10000],
     [2, 6, 0, 3, 3, 4, 10000, 10000],
     [4, 10000, 3, 0, 10000, 4, 10000, 4],
     [10000, 2, 3, 10000, 0, 2, 3, 10000],
     [10000, 10000, 4, 4, 2, 0, 3, 2],
     [10000, 10000, 10000, 10000, 3, 3, 0, 6],
     [10000, 10000, 10000, 4, 10000, 2, 6, 0]]

# Time matrix, L[i][j] = L[j][i] = positive infinity ~ no direct link between vertex i and vertex j.
L = [[0, 6, 5, 7, 100, 100, 100, 100],
     [6, 0, 6, 100, 2, 100, 100, 100],
     [5, 6, 0, 4, 4, 7, 100, 100],
     [7, 100, 4, 0, 100, 8, 100, 5],
     [100, 2, 4, 100, 0, 6, 3, 100],
     [100, 100, 7, 8, 6, 0, 4, 2],
     [100, 100, 100, 100, 3, 4, 0, 6],
     [100, 100, 100, 5, 100, 2, 6, 0]]


def modelAndSolve():
    # Create the mip solver with the CBC backend.
    solver = pywraplp.Solver('Multicast Routing',
                             pywraplp.Solver.CBC_MIXED_INTEGER_PROGRAMMING)

    # Creates the variables.
    x = [[0 for i in range(N)] for j in range(N)]
    y = [0 for i in range(N)]
    y[0] = solver.IntVar(0.0, 0.0, 'y[0]')

    for i in range(N):
        for j in range(N):
            if i != j:
                x[i][j] = solver.IntVar(0.0, 1.0, 'x[%i][%i]' % (i, j))
            else:
                x[i][j] = solver.IntVar(0.0, 0.0, 'x[%i][%i]' % (i, j))

    # Constraint 3.
    for i in range(1, N):
        y[i] = solver.IntVar(0.0, 12.0, 'y[%i]' %(i))

    # Creates the constraints.
    # Constraint 1.
    for u in range(1, N):
        solver.Add(sum(x[i][u] for i in range(N)) == 1)

    #Constraint 2.
    for u in range(1, N):
        for v in range(N):
            if u != v:
                solver.Add(y[u] + 10000 * (x[v][u]-1) - y[v] <= L[u][v])
                solver.Add(y[u] - 10000 * (x[v][u]-1) - y[v] >= L[u][v])

    # Objective function.
    solver.Minimize(solver.Sum(x[u][v]*c[u][v] for u in range(N) for v in range(N)))

    # Solves.
    result_status = solver.Solve()
    # The problem has an optimal solution.
    assert result_status == pywraplp.Solver.OPTIMAL

    # The solution looks legit (when using solvers others than
    # GLOP_LINEAR_PROGRAMMING, verifying the solution is highly recommended!).
    assert solver.VerifySolution(1e-7, True)

    # Prints the result.
    print("Optimal solution:\n\nLinks: ", end="")

    for i in range(N):
        for j in range(N):
            if x[i][j].solution_value() == 1:
                print(f"({i+1}, {j+1})", end="   ")

    print("\n\nTotal time: ",end="")

    for i in range(N):
        print(f"L[{i+1}] = {y[i].solution_value()}", end="   ")

    print(f"\n\nTotal cost: {solver.Objective().Value()}")


if __name__ == '__main__':
    modelAndSolve()