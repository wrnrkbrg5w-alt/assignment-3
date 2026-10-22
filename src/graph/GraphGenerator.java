package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {
    public static Graph generate(int vertices, int edges, long seed, String type) {
        boolean undirected = type.equals("undirected");
        boolean dag = type.equals("dag");
        if (!undirected && !dag && !type.equals("cyclic")) {
            throw new IllegalArgumentException("Unknown graph type");
        }
        long maximum = (long) vertices * (vertices - 1) / (undirected || dag ? 2 : 1);
        int minimum = undirected ? vertices - 1 : (dag ? 0 : vertices);
        if (vertices < 1 || edges < minimum || edges > maximum || (!undirected && !dag && vertices < 2)) {
            throw new IllegalArgumentException("Invalid graph size");
        }
        Graph graph = new Graph(vertices, !undirected);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        if (undirected) {
            for (int v = 1; v < vertices; v++) add(graph, used, random.nextInt(v), v, random, true);
        } else if (!dag) {
            for (int v = 0; v < vertices; v++) add(graph, used, v, (v + 1) % vertices, random, false);
        }
        while (graph.edges().size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (from == to) continue;
            if ((undirected || dag) && from > to) {
                int temp = from;
                from = to;
                to = temp;
            }
            add(graph, used, from, to, random, undirected);
        }
        return graph;
    }

    private static void add(Graph graph, Set<Long> used, int from, int to, Random random, boolean undirected) {
        if (undirected && from > to) {
            int temp = from;
            from = to;
            to = temp;
        }
        long key = (long) from * graph.size() + to;
        if (used.add(key)) graph.addEdge(from, to, 1 + random.nextInt(100));
    }
}
