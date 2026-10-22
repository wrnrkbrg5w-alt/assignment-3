import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import graph.Graph;
import graph.Stats;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

public class Main {
    public static void main(String[] args) {
        int source = args.length == 0 ? 0 : Integer.parseInt(args[0]);
        Graph city = new Graph(6, false);
        city.addEdge(0, 1, 4);
        city.addEdge(0, 2, 2);
        city.addEdge(1, 2, 1);
        city.addEdge(1, 3, 5);
        city.addEdge(2, 3, 8);
        city.addEdge(2, 4, 10);
        city.addEdge(3, 4, 2);
        System.out.println("Districts 0-5; district 5 has no roads.");
        showMST("Kruskal", Kruskal.run(city, new Stats()));
        showMST("Prim", Prim.run(city, new Stats()));
        Dijkstra.Result routes = Dijkstra.run(city, source, new Stats());
        System.out.println("Shortest routes from district " + source + ":");
        for (int v = 0; v < city.size(); v++) {
            if (routes.distances()[v] == Long.MAX_VALUE) System.out.println(v + ": unreachable");
            else System.out.println(v + ": distance=" + routes.distances()[v] + ", path=" + routes.pathTo(v));
        }
        Graph services = new Graph(6, true);
        services.addEdge(0, 1, 1);
        services.addEdge(1, 2, 1);
        services.addEdge(2, 0, 1);
        services.addEdge(2, 3, 1);
        services.addEdge(3, 4, 1);
        services.addEdge(4, 3, 1);
        System.out.println("Service SCCs: " + Kosaraju.run(services, new Stats()));
        System.out.println("Service graph contains a cycle: " + TopologicalSort.run(services, new Stats()).hasCycle());
        Graph dependencies = new Graph(6, true);
        dependencies.addEdge(0, 2, 1);
        dependencies.addEdge(1, 2, 1);
        dependencies.addEdge(2, 3, 1);
        dependencies.addEdge(3, 4, 1);
        System.out.println("DAG topological order: " + TopologicalSort.run(dependencies, new Stats()).order());
    }

    private static void showMST(String name, MSTResult result) {
        System.out.println(name + ": weight=" + result.weight() + ", connected=" + result.connected()
                + ", components=" + result.components() + ", edges=" + result.edges());
        if (!result.connected()) System.out.println("No spanning tree exists; showing a minimum spanning forest.");
    }
}
