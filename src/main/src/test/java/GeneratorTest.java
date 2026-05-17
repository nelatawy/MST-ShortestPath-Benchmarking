package test.java;

import main.java.benchmarking.inputgeneration.CompleteGenerator;
import main.java.benchmarking.inputgeneration.DAGGenerator;
import main.java.benchmarking.inputgeneration.DenseGenerator;
import main.java.benchmarking.inputgeneration.SparseGenerator;
import main.java.graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneratorTest {
    private Graph<Integer> graph;

    @Test
    public void DAGConnectedTest(){
        graph = new DAGGenerator().generate(100);
        List<Integer> result = graph.BFS(0);
        assertTrue( result != null && result.size() == 100, "Graph is disconnected");
    }

    @Test
    public void denseConnectedTest(){
        graph = new DenseGenerator().generate(100);
        List<Integer> result = graph.BFS(0);
        assertTrue( result != null && result.size() == 100, "Graph is disconnected");
    }

    @Test
    public void sparseConnectedTest(){
        graph = new SparseGenerator().generate(100);
        List<Integer> result = graph.BFS(0);
        assertTrue( result != null && result.size() == 100, "Graph is disconnected");
    }

    @Test
    public void completeConnectedTest(){
        graph = new CompleteGenerator().generate(100);
        List<Integer> result = graph.BFS(0);
        assertTrue( result != null && result.size() == 100, "Graph is disconnected");
    }

}
