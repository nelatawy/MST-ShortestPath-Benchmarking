package test.java;

import main.java.graph.Edge;
import main.java.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MSTTest {
    Graph<String> graph;

    @BeforeEach
    public void prepare(){
        graph = new Graph<>();
    }

    @Test
    public void testPrimMST(){

        graph.addEdge("A", "B", 1);
        graph.addEdge("A", "C", 3);
        graph.addEdge("B", "C", 2);
        graph.addEdge("B", "D", 4);
        graph.addEdge("C", "D", 5);

        List<Edge<String>> mstEdges = graph.primMST();

        assertEquals(3, mstEdges.size(), "MST should have exactly V - 1 edges");


        int totalWeight = mstEdges.stream().mapToInt(e -> e.weight).sum();
        assertEquals(7, totalWeight, "Total MST weight should be 7");

        boolean hasEdgeAB = mstEdges.stream().anyMatch(e ->
                (e.from.equals("A") && e.to.equals("B") && e.weight == 1) ||
                        (e.from.equals("B") && e.to.equals("A") && e.weight == 1));

        boolean hasEdgeBC = mstEdges.stream().anyMatch(e ->
                (e.from.equals("B") && e.to.equals("C") && e.weight == 2) ||
                        (e.from.equals("C") && e.to.equals("B") && e.weight == 2));

        boolean hasEdgeBD = mstEdges.stream().anyMatch(e ->
                (e.from.equals("B") && e.to.equals("D") && e.weight == 4) ||
                        (e.from.equals("D") && e.to.equals("B") && e.weight == 4));

        assertTrue(hasEdgeAB, "MST missing edge A-B (weight 1)");
        assertTrue(hasEdgeBC, "MST missing edge B-C (weight 2)");
        assertTrue(hasEdgeBD, "MST missing edge B-D (weight 4)");
    }

    @Test
    public void testPrimMSTDisconnected(){

        graph.addEdge("A", "B", 1);
        graph.addEdge("C", "D", 5);
        graph.addEdge("E", "F", 3);

        List<Edge<String>> mstEdges = graph.primMST();

        assertNull(mstEdges, "MST Should be null since graph is disconnected");
    }

    @Test
    public void testKruskalMST(){

        graph.addEdge("A", "B", 1);
        graph.addEdge("A", "C", 3);
        graph.addEdge("B", "C", 2);
        graph.addEdge("B", "D", 4);
        graph.addEdge("C", "D", 5);

        List<Edge<String>> mstEdges = graph.kruskalMST();

        assertEquals(3, mstEdges.size(), "MST should have exactly V - 1 edges");


        int totalWeight = mstEdges.stream().mapToInt(e -> e.weight).sum();
        assertEquals(7, totalWeight, "Total MST weight should be 7");

        boolean hasEdgeAB = mstEdges.stream().anyMatch(e ->
                (e.from.equals("A") && e.to.equals("B") && e.weight == 1) ||
                        (e.from.equals("B") && e.to.equals("A") && e.weight == 1));

        boolean hasEdgeBC = mstEdges.stream().anyMatch(e ->
                (e.from.equals("B") && e.to.equals("C") && e.weight == 2) ||
                        (e.from.equals("C") && e.to.equals("B") && e.weight == 2));

        boolean hasEdgeBD = mstEdges.stream().anyMatch(e ->
                (e.from.equals("B") && e.to.equals("D") && e.weight == 4) ||
                        (e.from.equals("D") && e.to.equals("B") && e.weight == 4));

        assertTrue(hasEdgeAB, "MST missing edge A-B (weight 1)");
        assertTrue(hasEdgeBC, "MST missing edge B-C (weight 2)");
        assertTrue(hasEdgeBD, "MST missing edge B-D (weight 4)");
    }

    @Test
    public void testKruskalMSTDisconnected(){

        graph.addEdge("A", "B", 1);
        graph.addEdge("C", "D", 5);
        graph.addEdge("E", "F", 3);

        List<Edge<String>> mstEdges = graph.kruskalMST();

        assertNull(mstEdges, "MST Should be null since graph is disconnected");
    }
}
