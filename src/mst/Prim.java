package mst;

import graph.Edge;
import graph.Graph;
import graph.Stats;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    public static MSTResult run(Graph graph, Stats stats) {
        if (graph.isDirected()) throw new IllegalArgumentException("MST requires an undirected graph");
        boolean[] visited = new boolean[graph.size()];
        List<Edge> selected = new ArrayList<>();
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            stats.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        long weight = 0;
        int components = 0;
        for (int start = 0; start < graph.size(); start++) {
            if (visited[start]) continue;
            components++;
            visited[start] = true;
            addEdges(graph, start, visited, queue, stats);
            while (!queue.isEmpty()) {
                Edge edge = queue.poll();
                stats.heapPops++;
                if (visited[edge.to()]) continue;
                visited[edge.to()] = true;
                selected.add(edge);
                weight += edge.weight();
                addEdges(graph, edge.to(), visited, queue, stats);
            }
        }
        return new MSTResult(selected, weight, components);
    }

    private static void addEdges(Graph graph, int vertex, boolean[] visited,
                                 PriorityQueue<Edge> queue, Stats stats) {
        for (Edge edge : graph.neighbors(vertex)) {
            stats.edgeChecks++;
            if (!visited[edge.to()]) {
                queue.add(edge);
                stats.heapPushes++;
            }
        }
    }
}
