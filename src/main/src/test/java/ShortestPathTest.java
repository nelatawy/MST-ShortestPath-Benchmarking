package test.java;

import main.java.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ShortestPathTest {

    Graph<String> graph;

    @BeforeEach
    public void prepare(){
        graph = new Graph<>();
    }

    @Test
    public void testDijkstraSingleSource(){
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
    public void testDijkstraNegativeWeights(){
        graph.addEdge("A", "B", -1);
        graph.addEdge("A", "C", 5);
        graph.addEdge("B", "C", -2);
        graph.addEdge("B", "C", 10);

        Map<String, Integer> shortestPaths = graph.dijkstra("A");
        assertNull(shortestPaths, "Dijkstra should return null in case of negative weights");
    }
}
