package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.Stats;
import java.util.ArrayList;
import java.util.List;

public class Kruskal {
    public static MSTResult run(Graph graph, Stats stats) {
        if (graph.isDirected()) throw new IllegalArgumentException("MST requires an undirected graph");
        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> {
            stats.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        DSU dsu = new DSU(graph.size(), stats);
        List<Edge> selected = new ArrayList<>();
        long weight = 0;
        int components = graph.size();
        for (Edge edge : sorted) {
            if (components == 1) break;
            stats.edgeChecks++;
            if (dsu.union(edge.from(), edge.to())) {
                selected.add(edge);
                weight += edge.weight();
                components--;
            }
        }
        return new MSTResult(selected, weight, components);
    }
}
