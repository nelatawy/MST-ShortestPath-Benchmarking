package test.java;

import main.java.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DAGShortestPathTest {
    Graph<String> graph;

    @BeforeEach
    public void setUp() {
        graph = new Graph<>();
    }

    @Test
    public void shortestPathSingleSource(){
        graph.addDirectedEdge("A", "B", 1);
        graph.addDirectedEdge("A", "C", 5);
        graph.addDirectedEdge("B", "C", 2);
        graph.addDirectedEdge("B", "C", 10);

        Map<String, Integer> shortestPaths = graph.dagShortestPath("A");
        assertEquals(1, shortestPaths.get("B"));
        assertEquals(0, shortestPaths.get("A"));
        assertEquals(3, shortestPaths.get("C"));
    }
    @Test
    public void testNegativeEdgeWeights() {
        graph.addDirectedEdge("A", "B", 3);
        graph.addDirectedEdge("A", "C", 2);
        graph.addDirectedEdge("B", "D", -4);
        graph.addDirectedEdge("C", "D", 1);

        Map<String, Integer> shortestPaths = graph.dagShortestPath("A");
        assertEquals(0, shortestPaths.get("A"));
        assertEquals(3, shortestPaths.get("B"));
        assertEquals(2, shortestPaths.get("C"));
        assertEquals(-1, shortestPaths.get("D")); // Path A -> B -> D is 3 + (-4) = -1
    }

    @Test
    public void testDisconnectedGraphAndUnreachableNodes() {
        graph.addDirectedEdge("A", "B", 2);
        graph.addDirectedEdge("C", "D", 4); // Separate component

        Map<String, Integer> shortestPaths = graph.dagShortestPath("A");
        assertEquals(0, shortestPaths.get("A"));
        assertEquals(2, shortestPaths.get("B"));

        assertNull(shortestPaths.get("C"));
        assertNull(shortestPaths.get("D"));
    }

    @Test
    public void testMultiplePathsToSameDestination() {

        graph.addDirectedEdge("A", "B", 1);
        graph.addDirectedEdge("B", "D", 7);

        graph.addDirectedEdge("A", "C", 4);
        graph.addDirectedEdge("C", "D", 2);

        graph.addDirectedEdge("A", "D", 10);

        Map<String, Integer> shortestPaths = graph.dagShortestPath("A");
        assertEquals(0, shortestPaths.get("A"));
        assertEquals(6, shortestPaths.get("D")); // Path A -> C -> D is 4 + 2 = 6
    }

    @Test
    public void testLongLinearChain() {

        graph.addDirectedEdge("A", "B", 5);
        graph.addDirectedEdge("B", "C", 10);
        graph.addDirectedEdge("C", "D", 15);

        Map<String, Integer> shortestPaths = graph.dagShortestPath("A");
        assertEquals(0, shortestPaths.get("A"));
        assertEquals(5, shortestPaths.get("B"));
        assertEquals(15, shortestPaths.get("C"));
        assertEquals(30, shortestPaths.get("D"));
    }

}
