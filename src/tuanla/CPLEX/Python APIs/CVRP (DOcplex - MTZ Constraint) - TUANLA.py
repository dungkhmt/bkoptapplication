from docplex.mp.model import Model
import numpy as np
import matplotlib.pyplot as plt

n = 10  # The number of cities
Q = 15  # The capacity of a truck
clientes = [i for i in range(1, n+1)]
nodes = [0] + clientes

rnd = np.random
rnd.seed(1)
q = {i:rnd.randint(1,10) for i in clientes}
rnd.seed(1)
cord_x = rnd.rand(len(nodes)) * 200
cord_y = rnd.rand(len(nodes)) * 100

arcs = {(i, j) for i in nodes for j in nodes if i != j}
distance = {(i, j):np.hypot(cord_x[i] - cord_x[j], cord_y[i] - cord_y[j]) for i in nodes for j in nodes if i != j}


md = Model('CRVP')
x = md.binary_var_dict(arcs, name='x')
u = md.continuous_var_dict(nodes, ub = Q, name ='u')
md.minimize(md.sum(distance[i, j] * x[i, j] for i, j in arcs))

# Constraints.
md.add_constraints(md.sum(x[i, j] for j in nodes if i != j) == 1 for i in clientes)
md.add_constraints(md.sum(x[i, j] for i in nodes if i != j) == 1 for j in clientes)
md.add_indicator_constraints(md.indicator_constraint(x[i, j], u[i] + q[j] == u[j]) for i, j in arcs if i != 0 and j != 0)
md.add_constraints(u[i] >= q[i] for i in clientes)

# Show model.
print(md.export_to_string())
md.parameters.timelimit = 120

# Solving.
solution = md.solve()

# Display the result.
md.get_solve_status()
solution.display()
plt.figure(figsize=(12,5))
plt.scatter(cord_x, cord_y, color="green")

for i in clientes:
    plt.annotate('$q_{%d}=%d$' % (i, q[i]), (cord_x[i] + 1, cord_y[i] - 0.5))

plt.plot(cord_x[0], cord_y[0], color ="red", marker ='s')
plt.annotate('Depot', (cord_x[0] - 2, cord_y[0] + 2))

arcs_active = [k for k in arcs if x[k].solution_value > 0.9]
#print(arcs_active)
for i, j in arcs_active:
    plt.plot([cord_x[i], cord_x[j]], [cord_y[i], cord_y[j]], color ='blue', alpha = 1)

plt.xlabel('cord_x')
plt.ylabel('cord_y')
plt.title("Graph VRP")
plt.show()