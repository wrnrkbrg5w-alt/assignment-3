package graph;

public class Stats {
    public long edgeChecks;
    public long comparisons;
    public long findCalls;
    public long unionCalls;
    public long parentSteps;
    public long heapPushes;
    public long heapPops;

    public String csv() {
        return edgeChecks + "," + comparisons + "," + findCalls + "," + unionCalls
                + "," + parentSteps + "," + heapPushes + "," + heapPops;
    }
}
