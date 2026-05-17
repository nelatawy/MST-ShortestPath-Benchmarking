package test.java;

import main.java.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ShortestPathTest {

    private Graph<String> graph;

    @BeforeEach
    public void setUp() {
        graph = new Graph<>();
    }

    @Test
    public void testDijkstraSingleSource() {
        graph.addEdge("A", "B", 1);
        graph.addEdge("A", "C", 5);
        graph.addEdge("B", "C", 2);
        graph.addEdge("B", "C", 10);

        Map<String, Integer> shortestPaths = graph.dijkstra("A");
        assertEquals(1, shortestPaths.get("B"));
        assertEquals(0, shortestPaths.get("A"));
        assertEquals(3, shortestPaths.get("C"));
    }

    @Test
    public void testDijkstraNegativeWeights() {
        graph.addEdge("A", "B", -1);
        graph.addEdge("A", "C", 5);
        graph.addEdge("B", "C", -2);
        graph.addEdge("B", "C", 10);

        Map<String, Integer> shortestPaths = graph.dijkstra("A");
        assertNull(shortestPaths, "Dijkstra should return null in case of negative weights");
    }


    @Test
    public void testDisconnectedGraph() {
        graph.addEdge("A", "B", 2);
        graph.addEdge("X", "Y", 3);

        Map<String, Integer> shortestPaths = graph.dijkstra("A");

        assertEquals(0, shortestPaths.get("A"));
        assertEquals(2, shortestPaths.get("B"));

        assertNull(shortestPaths.get("X"));
    }

    @Test
    public void testGraphWithCycles() {
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 2);
        graph.addEdge("C", "A", 4); // Cycle back to A
        graph.addEdge("C", "D", 1);

        Map<String, Integer> shortestPaths = graph.dijkstra("A");
        assertEquals(0, shortestPaths.get("A"));
        assertEquals(1, shortestPaths.get("B"));
        assertEquals(3, shortestPaths.get("C"));
        assertEquals(4, shortestPaths.get("D"));
    }

    @Test
    public void testSelfLoops() {
        graph.addEdge("A", "A", 5); // Self loop
        graph.addEdge("A", "B", 2);
        graph.addEdge("B", "B", 1); // Another self loop

        Map<String, Integer> shortestPaths = graph.dijkstra("A");
        assertEquals(0, shortestPaths.get("A"), "Distance to self should remain 0 despite self loop");
        assertEquals(2, shortestPaths.get("B"));
    }

    @Test
    public void testZeroWeightEdges() {
        graph.addEdge("A", "B", 0);
        graph.addEdge("B", "C", 0);
        graph.addEdge("A", "C", 4);

        Map<String, Integer> shortestPaths = graph.dijkstra("A");
        assertEquals(0, shortestPaths.get("A"));
        assertEquals(0, shortestPaths.get("B"));
        assertEquals(0, shortestPaths.get("C"));
    }

    @Test
    public void testNonExistentSource() {
        graph.addEdge("A", "B", 2);

        Map<String, Integer> shortestPaths = graph.dijkstra("Z");
        assertTrue(shortestPaths == null || !shortestPaths.containsKey("A"));
    }

    @Test
    public void testSingleNodeGraph() {
        graph.addEdge("A", "A", 0);

        Map<String, Integer> shortestPaths = graph.dijkstra("A");
        assertEquals(0, shortestPaths.get("A"));
    }
}
