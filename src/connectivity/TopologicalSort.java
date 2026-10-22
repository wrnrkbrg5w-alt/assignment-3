package connectivity;

import graph.Edge;
import graph.Graph;
import graph.Stats;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class TopologicalSort {
    public record Result(List<Integer> order, boolean hasCycle) { }

    public static Result run(Graph graph, Stats stats) {
        if (!graph.isDirected()) throw new IllegalArgumentException("A directed graph is required");
        int[] indegree = new int[graph.size()];
        for (Edge edge : graph.edges()) {
            stats.edgeChecks++;
            indegree[edge.to()]++;
        }
        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < graph.size(); i++) {
            if (indegree[i] == 0) queue.add(i);
        }
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            order.add(vertex);
            for (Edge edge : graph.neighbors(vertex)) {
                stats.edgeChecks++;
                if (--indegree[edge.to()] == 0) queue.add(edge.to());
            }
        }
        boolean hasCycle = order.size() != graph.size();
        return new Result(hasCycle ? List.of() : order, hasCycle);
    }
}
