package main.java.benchmarking.inputgeneration;

import main.java.graph.Graph;

import java.util.Random;

public class CompleteGenerator implements GraphGenerator{
    @Override
    public Graph<Integer> generate(int nodeCount) {
        Graph<Integer> graph = new Graph<>();
        Random random = new Random(42);
        for (int from = 0; from < nodeCount; from++){
            for (int to = from + 1; to < nodeCount; to++){
                int weight = random.nextInt(1000);
                graph.addEdge(from, to, weight);
            }
        }
        return graph;
    }
}
