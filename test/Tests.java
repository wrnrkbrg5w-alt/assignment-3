import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import graph.Stats;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Tests {
    private static int checks;

    public static void main(String[] args) {
        DSU dsu = new DSU(5, new Stats());
        check(dsu.union(0, 1), "First union");
        check(dsu.union(1, 2), "Second union");
        check(!dsu.union(0, 2), "Repeated union");
        check(dsu.find(0) == dsu.find(2) && dsu.find(0) != dsu.find(3), "DSU membership");
        Graph small = new Graph(4, false);
        small.addEdge(0, 1, 1);
        small.addEdge(1, 2, 2);
        small.addEdge(0, 2, 8);
        small.addEdge(2, 3, 3);
        checkMST(small, 6, 1);
        Graph equal = new Graph(4, false);
        equal.addEdge(0, 1, 1);
        equal.addEdge(1, 2, 1);
        equal.addEdge(2, 3, 1);
        equal.addEdge(3, 0, 1);
        equal.addEdge(0, 2, 1);
        checkMST(equal, 3, 1);
        Graph disconnected = new Graph(5, false);
        disconnected.addEdge(0, 1, 2);
        disconnected.addEdge(2, 3, 4);
        checkMST(disconnected, 6, 3);
        Graph single = new Graph(1, false);
        checkMST(single, 0, 1);
        Graph negativeMST = new Graph(3, false);
        negativeMST.addEdge(0, 1, -3);
        negativeMST.addEdge(1, 2, 1);
        negativeMST.addEdge(0, 2, 4);
        checkMST(negativeMST, -2, 1);
        Graph directed = new Graph(6, true);
        directed.addEdge(0, 1, 4);
        directed.addEdge(0, 2, 1);
        directed.addEdge(2, 1, 2);
        directed.addEdge(1, 3, 1);
        directed.addEdge(2, 3, 8);
        directed.addEdge(3, 4, 0);
        Dijkstra.Result paths = Dijkstra.run(directed, 0, new Stats());
        check(Arrays.equals(paths.distances(), new long[]{0, 3, 1, 4, 4, Long.MAX_VALUE}), "Known shortest distances");
        check(paths.pathTo(4).equals(List.of(0, 2, 1, 3, 4)), "Known path");
        check(paths.pathTo(5).isEmpty(), "Unreachable path");
        verifyPaths(directed, 0, paths);
        validateDAG(directed);
        verifySCC(directed);
        Graph cycle = new Graph(6, true);
        cycle.addEdge(0, 1, 1);
        cycle.addEdge(1, 2, 1);
        cycle.addEdge(2, 0, 1);
        cycle.addEdge(2, 3, 1);
        cycle.addEdge(3, 4, 1);
        cycle.addEdge(4, 3, 1);
        check(TopologicalSort.run(cycle, new Stats()).hasCycle(), "Cycle detection");
        check(TopologicalSort.run(cycle, new Stats()).order().isEmpty(), "No partial order on a cycle");
        check(Kosaraju.run(cycle, new Stats()).size() == 3, "Three SCCs");
        verifySCC(cycle);
        Graph self = new Graph(1, true);
        validateDAG(self);
        verifySCC(self);
        check(Dijkstra.run(self, 0, new Stats()).pathTo(0).equals(List.of(0)), "Single vertex path");
        self.addEdge(0, 0, 0);
        check(TopologicalSort.run(self, new Stats()).hasCycle(), "Self-loop cycle");
        verifySCC(self);
        Graph largeDistance = new Graph(3, true);
        largeDistance.addEdge(0, 1, Integer.MAX_VALUE);
        largeDistance.addEdge(1, 2, Integer.MAX_VALUE);
        check(Dijkstra.run(largeDistance, 0, new Stats()).distances()[2] == 4294967294L, "Long distances");
        Graph negative = new Graph(3, true);
        negative.addEdge(1, 2, -1);
        expectError(() -> Dijkstra.run(negative, 0, new Stats()));
        expectError(() -> Dijkstra.run(directed, 6, new Stats()));
        expectError(() -> Kruskal.run(directed, new Stats()));
        expectError(() -> Prim.run(directed, new Stats()));
        expectError(() -> TopologicalSort.run(small, new Stats()));
        expectError(() -> Kosaraju.run(small, new Stats()));
        Graph parallel = new Graph(3, false);
        parallel.addEdge(0, 1, 8);
        parallel.addEdge(0, 1, 2);
        parallel.addEdge(1, 2, 3);
        parallel.addEdge(0, 0, 0);
        checkMST(parallel, 5, 1);
        verifyPaths(parallel, 0, Dijkstra.run(parallel, 0, new Stats()));
        randomizedTests();
        for (int[] size : new int[][]{{10, 30}, {100, 500}, {1000, 5000}}) {
            Graph g = GraphGenerator.generate(size[0], size[1], 2503, "undirected");
            check(g.edges().size() == size[1], "Exact edge count");
            check(g.edges().equals(GraphGenerator.generate(size[0], size[1], 2503, "undirected").edges()), "Reproducible generation");
            checkMST(g, Kruskal.run(g, new Stats()).weight(), 1);
            validateDAG(GraphGenerator.generate(size[0], size[1], 2503, "dag"));
            Graph cyclic = GraphGenerator.generate(size[0], size[1], 2503, "cyclic");
            check(TopologicalSort.run(cyclic, new Stats()).hasCycle(), "Generated cycle");
            check(Kosaraju.run(cyclic, new Stats()).size() == 1, "Ring makes one SCC");
        }
        System.out.println("All " + checks + " checks passed.");
    }

    private static void randomizedTests() {
        Random random = new Random(73);
        for (int trial = 0; trial < 30; trial++) {
            Graph graph = new Graph(8, true);
            for (int u = 0; u < 8; u++) {
                for (int v = 0; v < 8; v++) {
                    if (u != v && random.nextInt(4) == 0) graph.addEdge(u, v, random.nextInt(11));
                }
            }
            verifySCC(graph);
            for (int source = 0; source < 8; source++) {
                Dijkstra.Result result = Dijkstra.run(graph, source, new Stats());
                check(Arrays.equals(result.distances(), bellmanFord(graph, source)), "Dijkstra vs Bellman-Ford");
                verifyPaths(graph, source, result);
            }
            Graph tiny = new Graph(5, false);
            for (int u = 0; u < 5; u++) {
                for (int v = u + 1; v < 5; v++) tiny.addEdge(u, v, random.nextInt(7));
            }
            checkMST(tiny, bruteMST(tiny), 1);
        }
    }

    private static long bruteMST(Graph graph) {
        long best = Long.MAX_VALUE;
        List<Edge> edges = graph.edges();
        for (int mask = 0; mask < (1 << edges.size()); mask++) {
            if (Integer.bitCount(mask) != graph.size() - 1) continue;
            DSU dsu = new DSU(graph.size(), new Stats());
            long weight = 0;
            boolean valid = true;
            for (int i = 0; i < edges.size(); i++) {
                if ((mask & (1 << i)) != 0) {
                    Edge edge = edges.get(i);
                    valid &= dsu.union(edge.from(), edge.to());
                    weight += edge.weight();
                }
            }
            if (valid) best = Math.min(best, weight);
        }
        return best;
    }

    private static long[] bellmanFord(Graph graph, int source) {
        long[] distances = new long[graph.size()];
        Arrays.fill(distances, Long.MAX_VALUE);
        distances[source] = 0;
        for (int i = 1; i < graph.size(); i++) {
            for (int u = 0; u < graph.size(); u++) {
                for (Edge edge : graph.neighbors(u)) {
                    if (distances[u] != Long.MAX_VALUE) distances[edge.to()] = Math.min(distances[edge.to()], distances[u] + edge.weight());
                }
            }
        }
        return distances;
    }

    private static void verifyPaths(Graph graph, int source, Dijkstra.Result result) {
        for (int target = 0; target < graph.size(); target++) {
            List<Integer> path = result.pathTo(target);
            if (result.distances()[target] == Long.MAX_VALUE) {
                check(path.isEmpty(), "Unreachable vertex");
                continue;
            }
            check(path.get(0) == source && path.get(path.size() - 1) == target, "Path endpoints");
            long weight = 0;
            for (int i = 1; i < path.size(); i++) {
                int minimum = Integer.MAX_VALUE;
                boolean found = false;
                for (Edge edge : graph.neighbors(path.get(i - 1))) {
                    if (edge.to() == path.get(i)) {
                        minimum = Math.min(minimum, edge.weight());
                        found = true;
                    }
                }
                check(found, "Path uses real edges");
                weight += minimum;
            }
            check(weight == result.distances()[target], "Path weight");
        }
    }

    private static void checkMST(Graph graph, long expected, int components) {
        for (MSTResult result : List.of(Kruskal.run(graph, new Stats()), Prim.run(graph, new Stats()))) {
            check(result.weight() == expected, "MST/forest weight");
            check(result.components() == components, "Component count");
            check(result.connected() == (components == 1), "Connected flag");
            check(result.edges().size() == graph.size() - components, "Forest edge count");
            DSU dsu = new DSU(graph.size(), new Stats());
            for (Edge edge : result.edges()) check(dsu.union(edge.from(), edge.to()), "Forest is acyclic");
        }
    }

    private static void validateDAG(Graph graph) {
        TopologicalSort.Result result = TopologicalSort.run(graph, new Stats());
        check(!result.hasCycle() && result.order().size() == graph.size(), "DAG ordering exists");
        int[] positions = new int[graph.size()];
        Arrays.fill(positions, -1);
        for (int i = 0; i < result.order().size(); i++) {
            int vertex = result.order().get(i);
            check(positions[vertex] == -1, "Unique ordered vertex");
            positions[vertex] = i;
        }
        for (Edge edge : graph.edges()) check(positions[edge.from()] < positions[edge.to()], "Topological edge direction");
    }

    private static void verifySCC(Graph graph) {
        List<List<Integer>> components = Kosaraju.run(graph, new Stats());
        int[] membership = new int[graph.size()];
        Arrays.fill(membership, -1);
        for (int i = 0; i < components.size(); i++) {
            for (int v : components.get(i)) {
                check(membership[v] == -1, "Vertex appears in only one SCC");
                membership[v] = i;
            }
        }
        boolean[][] reachable = new boolean[graph.size()][graph.size()];
        for (int source = 0; source < graph.size(); source++) {
            check(membership[source] != -1, "Every vertex has an SCC");
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(source);
            reachable[source][source] = true;
            while (!queue.isEmpty()) {
                int u = queue.remove();
                for (Edge edge : graph.neighbors(u)) {
                    if (!reachable[source][edge.to()]) {
                        reachable[source][edge.to()] = true;
                        queue.add(edge.to());
                    }
                }
            }
        }
        for (int u = 0; u < graph.size(); u++) {
            for (int v = 0; v < graph.size(); v++) {
                check((membership[u] == membership[v]) == (reachable[u][v] && reachable[v][u]), "SCC matches mutual reachability");
            }
        }
    }

    private static void expectError(Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException exception) { rejected = true; }
        check(rejected, "Invalid input rejected");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
