package connectivity;

import graph.Edge;
import graph.Graph;
import graph.Stats;
import java.util.ArrayList;
import java.util.List;

public class Kosaraju {
    public static List<List<Integer>> run(Graph graph, Stats stats) {
        if (!graph.isDirected()) throw new IllegalArgumentException("A directed graph is required");
        boolean[] visited = new boolean[graph.size()];
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < graph.size(); i++) {
            if (!visited[i]) finish(graph, i, visited, order, stats);
        }
        Graph reversed = new Graph(graph.size(), true);
        for (Edge edge : graph.edges()) {
            stats.edgeChecks++;
            reversed.addEdge(edge.to(), edge.from(), edge.weight());
        }
        visited = new boolean[graph.size()];
        List<List<Integer>> components = new ArrayList<>();
        for (int i = order.size() - 1; i >= 0; i--) {
            int vertex = order.get(i);
            if (!visited[vertex]) {
                List<Integer> component = new ArrayList<>();
                collect(reversed, vertex, visited, component, stats);
                components.add(component);
            }
        }
        return components;
    }

    private static void finish(Graph graph, int vertex, boolean[] visited,
                               List<Integer> order, Stats stats) {
        visited[vertex] = true;
        for (Edge edge : graph.neighbors(vertex)) {
            stats.edgeChecks++;
            if (!visited[edge.to()]) finish(graph, edge.to(), visited, order, stats);
        }
        order.add(vertex);
    }

    private static void collect(Graph graph, int vertex, boolean[] visited,
                                List<Integer> component, Stats stats) {
        visited[vertex] = true;
        component.add(vertex);
        for (Edge edge : graph.neighbors(vertex)) {
            stats.edgeChecks++;
            if (!visited[edge.to()]) collect(graph, edge.to(), visited, component, stats);
        }
    }
}
