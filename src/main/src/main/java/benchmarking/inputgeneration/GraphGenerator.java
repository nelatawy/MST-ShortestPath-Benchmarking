package main.java.benchmarking.inputgeneration;

import main.java.graph.Graph;

public interface GraphGenerator {
    /**
     * This method generates a connected graph of `nodeCount` nodes based on the concrete implementation of the generator.
     * @param nodeCount number of nodes
     * @return a Graph of Integers with `nodeCount` nodes
     */
    Graph<Integer> generate(int nodeCount);
}
