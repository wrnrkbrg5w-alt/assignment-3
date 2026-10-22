package shortestpath;

import graph.Edge;
import graph.Graph;
import graph.Stats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {
    private record Entry(int vertex, long distance) { }

    public record Result(long[] distances, int[] previous) {
        public List<Integer> pathTo(int target) {
            if (distances[target] == Long.MAX_VALUE) return List.of();
            List<Integer> path = new ArrayList<>();
            for (int v = target; v != -1; v = previous[v]) path.add(v);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph graph, int source, Stats stats) {
        graph.checkVertex(source);
        for (Edge edge : graph.edges()) {
            stats.edgeChecks++;
            if (edge.weight() < 0) throw new IllegalArgumentException("Dijkstra requires non-negative weights");
        }
        long[] distances = new long[graph.size()];
        int[] previous = new int[graph.size()];
        Arrays.fill(distances, Long.MAX_VALUE);
        Arrays.fill(previous, -1);
        PriorityQueue<Entry> queue = new PriorityQueue<>((a, b) -> {
            stats.comparisons++;
            return Long.compare(a.distance(), b.distance());
        });
        distances[source] = 0;
        queue.add(new Entry(source, 0));
        stats.heapPushes++;
        while (!queue.isEmpty()) {
            Entry current = queue.poll();
            stats.heapPops++;
            int vertex = current.vertex();
            if (current.distance() != distances[vertex]) continue;
            for (Edge edge : graph.neighbors(vertex)) {
                stats.edgeChecks++;
                long candidate = distances[vertex] + edge.weight();
                if (candidate < distances[edge.to()]) {
                    distances[edge.to()] = candidate;
                    previous[edge.to()] = vertex;
                    queue.add(new Entry(edge.to(), candidate));
                    stats.heapPushes++;
                }
            }
        }
        return new Result(distances, previous);
    }
}
