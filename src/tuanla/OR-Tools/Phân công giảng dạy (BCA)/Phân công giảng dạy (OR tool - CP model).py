from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from ortools.sat.python import cp_model

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



def PhanCongGiangDay():
    """Minimal CP-SAT example to showcase calling the solver."""
    # Creates the model.

    model = cp_model.CpModel()

    x = [[0 for i in range(M)] for j in range(N)]
    result = [[] for i in range(3)]
    opt = [0 for i in range(3)]

    # Creates the variables.
    for i in range(N):
        for j in range(M):
            if i in c[j]:
                x[i][j] = model.NewIntVar(0, 1, 'x[%i][%i]' %(i,j))
            else:
                x[i][j] = model.NewIntVar(0, 0, 'x[%i][%i]' %(i,j))


    # Creates the constraints.
    for i in range(N):
        model.AddLinearConstraint(sum(x[i]), 1, 1)

    for k in range(M):
        model.AddLinearConstraint(sum(x[i][k]*t[i] for i in range(N)), 0, 15)  # Value = 15: Số lượng tiết tối đa được phân công cho 1 giáo viên

        for i in range(N):
            if len(S[i]) > 0:
                for j in S[i]:
                    model.AddLinearConstraint(x[i][k] + x[j][k], 0, 1)


    # Creates a solver and solves the model.
    solver = cp_model.CpSolver()
    status = solver.Solve(model)

    if status == cp_model.FEASIBLE:
        for j in range(M):
            for i in range(N):
                if solver.Value(x[i][j]) == 1:
                    result[j].append(i)
                    opt[j] += t[i]

        Display(result, opt)


if __name__ == '__main__':
    PhanCongGiangDay()