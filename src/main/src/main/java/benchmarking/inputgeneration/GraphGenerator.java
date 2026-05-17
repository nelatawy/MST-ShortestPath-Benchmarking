package main.java.benchmarking.inputgeneration;

import main.java.graph.Graph;

public interface GraphGenerator {
    Graph<Integer> generate(int nodeCount);
}
