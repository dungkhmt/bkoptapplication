from __future__ import print_function
from ortools.linear_solver import pywraplp
import time


c = []  # cost matrix
N = 0  # the number of cities


def newSolver(name,integer=False):
    return pywraplp.Solver(name, pywraplp.Solver.CBC_MIXED_INTEGER_PROGRAMMING if integer else pywraplp.Solver.GLOP_LINEAR_PROGRAMMING)


def inputData(filename):
    global c, N

    f = open(filename,"r+")

    s = f.read()
    c = s.split("\n")
    N = len(c)
    for i in range(N):
        c[i] = c[i].split()
        c[i] = list(map(int, c[i]))

    f.close()


def SolVal(x):
    if type(x) is not list:
        return 0 if x is None \
        else x if isinstance(x,(int,float)) \
        else x.SolutionValue() if x.Integer() is False \
        else int(x.SolutionValue())
    elif type(x) is list:
        return [SolVal(e) for e in x]


def ObjVal(x):
    return x.Objective().Value()


def solve_model_eliminate(c, Subtours = []):
    s, N = newSolver('TSP', True), len(c)
    x = [[s.IntVar(0, 0 if c[i][j] is None else 1, '') for j in range(N)] for i in range(N)]

    for i in range(N):
        s.Add(1 == sum(x[i][j] for j in range(N)))
        s.Add(1 == sum(x[j][i] for j in range(N)))
        s.Add(0 == x[i][i])

    for sub in Subtours:
        sub_length = len(sub) - 1
        k = [x[sub[i]][sub[j]] + x[sub[j]][sub[i]] for i in range(sub_length) for j in range(i + 1, sub_length + 1)]
        s.Add(sub_length >= sum(k))

    s.Minimize(s.Sum(x[i][j]*c[i][j] for i in range(N) for j in range(N)))

    rc = s.Solve()
    tours = extract_tours(SolVal(x))

    return rc, ObjVal(s), tours


def extract_tours(R):
    node, tours, considered = 0, [[0]], [1] + [0] * (N - 1) # considered: [1] + [0]*(N-1) ~ [1, 0, 0, 0,...] (N elements) ~ mark considered vertexs

    while sum(considered) < N:
        for i in range(N):
            if R[node][i] == 1:
                next = i # next: the next vertext in subcycle
                break
        # ~ next = [i for i in range(N) if R[node][i] == 1][0]
        if next not in tours[-1]:
            tours[-1].append(next)
            node = next
        else:
            node = considered.index(0) # return the index of the first element has value = 0 from left to right
            tours.append([node])

        considered[node] = 1

    return tours


def solve_model(c):
    subtours, tours = [], []

    while len(tours) != 1:
        rc, Value, tours = solve_model_eliminate(c, subtours)
        if rc == 0:
            subtours.extend(tours)

    return rc, Value, tours[0]


def display(result):
    print("Best path: 0", end='')

    for i in range(1, len(result[2])):
        print("  --> ", result[2][i],  end='')

    print("  -->  0")
    print("Min cost: ", result[1])


if __name__ == '__main__':
    inputData("TSP - 15.txt")

    t = time.time()
    result = solve_model(c)
    t = time.time() - t
    print("Solving time:", t)

    display(result)
