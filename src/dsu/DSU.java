package dsu;

import graph.Stats;

public class DSU {
    private final int[] parent;
    private final int[] size;
    private final Stats stats;

    public DSU(int n, Stats stats) {
        parent = new int[n];
        size = new int[n];
        this.stats = stats;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int vertex) {
        stats.findCalls++;
        int root = vertex;
        while (parent[root] != root) {
            stats.parentSteps++;
            root = parent[root];
        }
        while (parent[vertex] != vertex) {
            int next = parent[vertex];
            parent[vertex] = root;
            vertex = next;
        }
        return root;
    }

    public boolean union(int a, int b) {
        stats.unionCalls++;
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) return false;
        if (size[rootA] < size[rootB]) {
            int temp = rootA;
            rootA = rootB;
            rootB = temp;
        }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        return true;
    }
}
