from __future__ import print_function
from ortools.linear_solver import pywraplp
import time


c = [] # cost matrix
N = 0 # the number of cities
K = 4 # the number of trucks


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
    x = [[0 for i in range(N)] for j in range(N)]

    for i in range(N):
         for j in range(N):
              if i != j:
                   x[i][j] = s.IntVar(0.0, 1.0, 'x[%i][%i]' % (i, j))
              else:
                   x[i][j] = s.IntVar(0.0, 0.0, 'x[%i][%i]' % (i, j))

    # B1: Creates the constraints.
    s.Add(K == sum(x[0][j] for j in range(N)))
    s.Add(K == sum(x[i][0] for i in range(N)))

    for i in range(1, N):
         s.Add(1 == sum(x[i][j] for j in range(N)))

    for j in range(1, N):
         s.Add(1 == sum(x[i][j] for i in range(N)))
    # Eliminate subtours which doesn't start at 0.
    for sub in Subtours:
        if sub[0] != 0:
            sub_length = len(sub) - 1
            k = [x[sub[i]][sub[j]] + x[sub[j]][sub[i]] for i in range(sub_length) for j in range(i + 1, sub_length + 1)]
            s.Add(sub_length >= sum(k))

    s.Minimize(s.Sum(x[i][j]*c[i][j] for i in range(N) for j in range(N)))

    # B2: Solve.
    rc = s.Solve()
    tours = extract_tours(SolVal(x))

    return rc, ObjVal(s), tours


def extract_tours(R):
    next, tours, considered = -1, [[0] for i in range(K)], [1] + [0] * (N - 1) # considered: [1] + [0]*(N-1) ~ [1, 0, 0, 0,...] (N elements) ~ mark considered vertexs

    # B1: Subcycles start at 0.
    for i in range(K):
        for j in range(N):
            if (R[0][j] == 1) and (considered[j] == 0):
                considered[j] = 1
                tours[i].append(j)
                next = j
                # find all vertex in subcycle.
                while next != 0:
                    for k in range(N):
                        if R[next][k] == 1:
                            if k != 0:
                                considered[k] = 1
                                tours[i].append(k)
                            next = k
                            break # Because sum(x[i]) == 1, i = 1, 2, ..., N - 1.
                break # Because sum(x[i]) == 1, i = 1, 2, ..., N - 1.

    # B2: The rest.
    if sum(considered) < N:
        node = considered.index(0)
        tours.append([])

        while sum(considered) < N:
            for i in range(N):
                if R[node][i] == 1:
                    next = i  # next: the next vertext in subcycle.
                    break
            # ~ next = [i for i in range(N) if R[node][i] == 1][0].
            if next not in tours[-1]:
                tours[-1].append(next)
                node = next
            else:
                node = considered.index(0)  # return the index of the first element has value = 0 from left to right.
                tours.append([node])

            considered[node] = 1


    return tours


def display(Value, tours):
    count = 0

    # B1
    print('Solution:', end='')

    for i in range(len(tours)):
        count += 1
        print(f"\nRoute {count}: ", end='')

        for j in range(len(tours[i])):
            print(f'{tours[i][j]}  -->  ', end='')

        print(0, end='')
    print(f'\n\n--> Total distance: {Value}\n')


def solve_model(c):
    subtours, tours = [], []

    while len(tours) != K:
        rc, Value, tours = solve_model_eliminate(c, subtours)
        if rc == 0:
            subtours.extend(tours)

    display(Value, tours)


if __name__ == '__main__':

    inputData("TSP - 15.txt")

    t = time.time()
    solve_model(c)
    t = time.time() - t
    print("Solving time:", t)