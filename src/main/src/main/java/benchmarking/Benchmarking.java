package main.java.benchmarking;

import main.java.benchmarking.inputgeneration.DAGGenerator;
import main.java.benchmarking.inputgeneration.DenseGenerator;
import main.java.benchmarking.inputgeneration.GraphGenerator;
import main.java.benchmarking.inputgeneration.GraphType;
import main.java.graph.Graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class Benchmarking {

    public static void MSTConstructionBenchmark(Graph<Integer> graph, GraphType type) throws IOException {
        int itrCnt = 5;
        List<Double> runningTimes = new ArrayList<>();
        Path file = Paths.get("mst-benchmarks.csv");
        if (!Files.exists(file)){
            Files.createFile(file);
            Files.writeString(file, "Algorithm, NodeCount, EdgeCount, Distribution, MeanTime, StdDev, MedianTime\n");
        }

        for(int i = 0; i < itrCnt; i++){
            Long start = System.nanoTime();
            graph.primMST();
            Long end = System.nanoTime();
            Double timeMillis = (double)(end - start)/1e6;
            runningTimes.add(timeMillis);
        }

        double mean = getMean(runningTimes);
        double standardDeviation = getStandardDeviation(mean, runningTimes);
        double median = getMedian(runningTimes);

        Files.writeString(file, "Prim, " +
                graph.getNodeCount() + ", " +
                graph.getEdgeCount() + ", " +
                type.name()          + ", " +
                mean                 + ", " +
                standardDeviation    + ", " +
                median               + "\n", StandardOpenOption.APPEND);


        runningTimes.clear();
        for(int i = 0; i < itrCnt; i++){
            Long start = System.nanoTime();
            graph.kruskalMST();
            Long end = System.nanoTime();
            Double timeMillis = (double)(end - start)/1e6;
            runningTimes.add(timeMillis);
        }

        mean = getMean(runningTimes);
        standardDeviation = getStandardDeviation(mean, runningTimes);
        median = getMedian(runningTimes);

        Files.writeString(file, "Kruskal, " +
                graph.getNodeCount() + ", " +
                graph.getEdgeCount() + ", " +
                type.name()          + ", " +
                mean                 + ", " +
                standardDeviation    + ", " +
                median               + "\n", StandardOpenOption.APPEND);
    }

    public static void generalShortestPathBenchmark(Graph<Integer> graph, GraphType type) throws IOException {
        int itrCnt = 5;
        List<Double> runningTimes = new ArrayList<>();
        Path file = Paths.get("general-sssp-benchmarks.csv");
        if (!Files.exists(file)){
            Files.createFile(file);
            Files.writeString(file, "Algorithm, NodeCount, EdgeCount, Distribution, MeanTime, StdDev, MedianTime\n");
        }

        int source = new Random(42).nextInt(graph.getNodeCount());
        for(int i = 0; i < itrCnt; i++){
            Long start = System.nanoTime();
            graph.dijkstra(source);
            // randomizing the source
            Long end = System.nanoTime();
            Double timeMillis = (double)(end - start)/1e6;
            runningTimes.add(timeMillis);
        }

        double mean = getMean(runningTimes);
        double standardDeviation = getStandardDeviation(mean, runningTimes);
        double median = getMedian(runningTimes);

        Files.writeString(file, "Dijkstra, " +
                graph.getNodeCount() + ", " +
                graph.getEdgeCount() + ", " +
                type.name()          + ", " +
                mean                 + ", " +
                standardDeviation    + ", " +
                median               + "\n", StandardOpenOption.APPEND);
    }

    public static void dagShortestPathBenchmarks(Graph<Integer> graph) throws IOException {
        int itrCnt = 5;
        List<Double> runningTimes = new ArrayList<>();
        Path file = Paths.get("dag-sssp-benchmarks.csv");
        if (!Files.exists(file)){
            Files.createFile(file);
            Files.writeString(file, "Algorithm, NodeCount, EdgeCount, MeanTime, StdDev, MedianTime, SpeedupMultiplier\n");
        }
        int source = new Random(42).nextInt(graph.getNodeCount());
        for(int i = 0; i < itrCnt; i++){
            Long start = System.nanoTime();
            graph.dagShortestPath(source);
            // randomizing the source
            Long end = System.nanoTime();
            Double timeMillis = (double)(end - start)/1e6;
            runningTimes.add(timeMillis);
        }

        double mean = getMean(runningTimes);
        double standardDeviation = getStandardDeviation(mean, runningTimes);
        double median = getMedian(runningTimes);

        // comparing to Dijkstra
        Long start = System.nanoTime();
        graph.dijkstra(source);
        Long end = System.nanoTime();
        double dijkstraTimeMillis = (double)(end - start)/1e6;

        Double speedup = dijkstraTimeMillis / mean;

        Files.writeString(file, "DAG-Topo, " +
                graph.getNodeCount() + ", " +
                graph.getEdgeCount() + ", " +
                mean                 + ", " +
                standardDeviation    + ", " +
                median               + ", " +
                speedup + "\n", StandardOpenOption.APPEND);


    }
    private static double getMean(List<Double> runningTimes){
        return runningTimes
                .stream()
                .mapToDouble(e->e)
                .average().orElse(0);
    }

    private static double getMedian(List<Double> runningTimes) {
        double median;
        Collections.sort(runningTimes);
        if ((runningTimes.size() & 1) == 0){
            median = (runningTimes.get((runningTimes.size() / 2) - 1) + runningTimes.get(runningTimes.size() / 2)) / 2;
        } else {
            median = runningTimes.get(runningTimes.size() / 2);
        }
        return median;
    }

    private static double getStandardDeviation(Double mean, List<Double> runningTimes){
        return Math.sqrt(
                runningTimes
                .stream()
                .mapToDouble(e->(e - mean)*(e - mean))
                .sum() / (runningTimes.size() - 1));
    }

    public static void main() throws IOException {
        GraphGenerator generator = new DAGGenerator();
        Graph<Integer> graph = generator.generate(5000);
        dagShortestPathBenchmarks(graph);
    }
}
