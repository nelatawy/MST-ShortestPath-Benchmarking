package main.java.benchmarking.inputgeneration;

import main.java.graph.Edge;
import main.java.graph.Graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DAGGenerator implements GraphGenerator{
    @Override
    public Graph<Integer> generate(int nodeCount) {
        Graph<Integer> graph = new Graph<>();
        Random random = new Random(42);
        Set<Edge<Integer>> addedEdges = new HashSet<>();

        for(int i = 0; i < nodeCount - 1; i++){
            graph.addDirectedEdge(i, i + 1, random.nextInt(1000)); // make sure graph is connected
        }
        int edgeCnt = Math.min(4 * nodeCount + 1, (nodeCount *(nodeCount - 1) /2)); // the total edge count is 5 * number of nodes

        for (int i = 0; i < edgeCnt; i++){
            int from = random.nextInt(nodeCount - 1);
            int to = (from + 1) + random.nextInt(nodeCount - (from + 1)); // to enforce ordering that prevents cycles
            int weight = random.nextInt(1000);
            int tries = 0;
            while (addedEdges.contains(new Edge<>(from, to, weight))){
                // internally the hashCode relies on the `from`, `to` not the memory address so as the `equals` method.
                from = random.nextInt(nodeCount - 1);
                to = (from + 1) + random.nextInt(nodeCount - (from + 1));
                tries++;
                if (tries > nodeCount * nodeCount) // prevent infinite loops
                    return graph;
            }
            graph.addDirectedEdge(from, to, weight);
            addedEdges.add(new Edge<>(from, to, weight));
        }
        return graph;
    }
}
