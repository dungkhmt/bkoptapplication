from __future__ import print_function
from ortools.linear_solver import pywraplp


M = 3
N = 13
t = [3, 3, 4, 3, 4, 3, 3, 3, 4, 3, 3, 4, 4]
c = [[0, 2, 3, 4, 8, 10], [0, 1, 3, 5, 6, 7, 8], [1, 2, 3, 7, 9, 11, 12]]
S = [[2, 4, 8], [4, 10], [], [7, 9], [], [11, 12], [8, 12], [], [], [], [], [], []]


def Display(result, opt):
    print('Giao vien     So tiet     Danh sach mon hoc duoc phan cong')

    for i in range(M):
        print('   ', i, '         ', opt[i], '       ', end='')

        for j in range(len(result[i]) - 1):
            print('%d, ' % result[i][j], end='')

        print(result[i][len(result[i]) - 1])


def main():
    # Create the mip solver with the CBC backend.
    solver = pywraplp.Solver('simple_mip_program',
                             pywraplp.Solver.CBC_MIXED_INTEGER_PROGRAMMING)

    # Creates the variables.
    result = [[] for i in range(M)]
    opt = [0 for i in range(M)]
    x = [[0 for i in range(M)] for j in range(N)]
    f = solver.IntVar(10.0, 20.0, 'Value')

    for i in range(N):
        for j in range(M):
            if i in c[j]:
                x[i][j] = solver.IntVar(0.0, 1.0, 'x[%i][%i]' % (i, j))
            else:
                x[i][j] = solver.IntVar(0.0, 0.0, 'x[%i][%i]' % (i, j))

    # Creates the constraints.
    for i in range(N):
        solver.Add(sum(x[i]) <= 1)
        solver.Add(-sum(x[i]) <= -1)

    for k in range(M):
        solver.Add(sum(x[i][k]*t[i] for i in range(N)) - f <= 0)

        for i in range(N):
            if len(S[i]) > 0:
                for j in S[i]:
                    solver.Add(x[i][k] + x[j][k] <= 1)

    solver.Minimize(f)

    result_status = solver.Solve()
    # The problem has an optimal solution.
    assert result_status == pywraplp.Solver.OPTIMAL

    # The solution looks legit (when using solvers others than
    # GLOP_LINEAR_PROGRAMMING, verifying the solution is highly recommended!).
    assert solver.VerifySolution(1e-7, True)

    for j in range(M):
        for i in range(N):
            if x[i][j].solution_value() == 1:
                result[j].append(i)
                opt[j] += t[i]

    Display(result, opt)


if __name__ == '__main__':
    main()