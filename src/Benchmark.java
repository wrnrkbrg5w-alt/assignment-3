import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import graph.Graph;
import graph.GraphGenerator;
import graph.Stats;
import mst.Kruskal;
import mst.Prim;
import shortestpath.Dijkstra;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class Benchmark {
    private static volatile Object lastResult;
    private static final long SEED = 2503;
    private static final int RUNS = 5;
    private static final int WARMUP = 20;

    public static void main(String[] args) throws IOException {
        Path output = Path.of("results", "tables");
        Files.createDirectories(output);
        String header = "algorithm,graph,V,E,run,time_ns,edge_checks,comparisons,find_calls,union_calls,parent_steps,heap_pushes,heap_pops";
        try (BufferedWriter raw = Files.newBufferedWriter(output.resolve("raw.csv"));
             BufferedWriter average = Files.newBufferedWriter(output.resolve("averages.csv"))) {
            raw.write(header + "\n");
            average.write("algorithm,graph,V,E,average_ns,edge_checks,comparisons,find_calls,union_calls,parent_steps,heap_pushes,heap_pops\n");
            int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
            for (int[] size : sizes) {
                int v = size[0];
                int e = size[1];
                Graph undirected = GraphGenerator.generate(v, e, SEED, "undirected");
                Graph dag = GraphGenerator.generate(v, e, SEED, "dag");
                Graph cyclic = GraphGenerator.generate(v, e, SEED, "cyclic");
                if (Kruskal.run(undirected, new Stats()).weight() != Prim.run(undirected, new Stats()).weight()) {
                    throw new IllegalStateException("MST weights disagree");
                }
                measure("Kruskal", "undirected", undirected, raw, average);
                measure("Prim", "undirected", undirected, raw, average);
                measure("Kosaraju", "dag", dag, raw, average);
                measure("Kosaraju", "cyclic", cyclic, raw, average);
                measure("Topological", "dag", dag, raw, average);
                measure("Topological", "cyclic", cyclic, raw, average);
                measure("Dijkstra", "cyclic", cyclic, raw, average);
            }
        }
        String environment = "seed=" + SEED + "\nruns=" + RUNS + "\nwarmup_runs=" + WARMUP
                + "\njava=" + System.getProperty("java.version") + "\nvm=" + System.getProperty("java.vm.name")
                + "\nos=" + System.getProperty("os.name") + " " + System.getProperty("os.arch")
                + "\nprocessors=" + Runtime.getRuntime().availableProcessors() + "\n";
        Files.writeString(output.resolve("environment.txt"), environment);
        System.out.println("Saved raw runs, averages and environment to " + output);
    }

    private static void measure(String algorithm, String type, Graph graph, BufferedWriter raw,
                                BufferedWriter average) throws IOException {
        for (int i = 0; i < WARMUP; i++) lastResult = run(algorithm, graph, new Stats());
        long total = 0;
        String operations = null;
        for (int i = 1; i <= RUNS; i++) {
            Stats stats = new Stats();
            long start = System.nanoTime();
            Object result = run(algorithm, graph, stats);
            long elapsed = System.nanoTime() - start;
            lastResult = result;
            total += elapsed;
            if (operations != null && !operations.equals(stats.csv())) throw new IllegalStateException("Unstable operation counts");
            operations = stats.csv();
            raw.write(algorithm + "," + type + "," + graph.size() + "," + graph.edges().size()
                    + "," + i + "," + elapsed + "," + operations + "\n");
        }
        average.write(String.format(Locale.ROOT, "%s,%s,%d,%d,%.1f,%s%n", algorithm, type,
                graph.size(), graph.edges().size(), total / (double) RUNS, operations));
    }

    private static Object run(String algorithm, Graph graph, Stats stats) {
        return switch (algorithm) {
            case "Kruskal" -> Kruskal.run(graph, stats);
            case "Prim" -> Prim.run(graph, stats);
            case "Kosaraju" -> Kosaraju.run(graph, stats);
            case "Topological" -> TopologicalSort.run(graph, stats);
            case "Dijkstra" -> Dijkstra.run(graph, 0, stats);
            default -> throw new IllegalArgumentException("Unknown algorithm");
        };
    }
}
