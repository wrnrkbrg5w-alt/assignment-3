package mst;

import graph.Edge;
import java.util.List;

public record MSTResult(List<Edge> edges, long weight, int components) {
    public boolean connected() { return components == 1; }
}
