package main.java.benchmarking;

import main.java.benchmarking.inputgeneration.*;
import main.java.graph.Graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


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
        Map<Integer, Integer> shortestPaths = null;
        for(int i = 0; i < itrCnt; i++){
            Long start = System.nanoTime();
            shortestPaths = graph.dijkstra(source);
            // randomizing the source
            Long end = System.nanoTime();
            Double timeMillis = (double)(end - start)/1e6;
            runningTimes.add(timeMillis);
        }

        double mean = getMean(runningTimes);
        double standardDeviation = getStandardDeviation(mean, runningTimes);
        double median = getMedian(runningTimes);

        for(int i = 0; i < graph.getNodeCount(); i ++){

            System.out.println("Shortest Path " + "node " + i + ": " + shortestPaths.get(i));

        }

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
        Map<Integer, Integer> dagShortestPaths = null;
        int source = new Random(42).nextInt(graph.getNodeCount());
        for(int i = 0; i < itrCnt; i++){
            Long start = System.nanoTime();
            dagShortestPaths = graph.dagShortestPath(source);
            // randomizing the source
            Long end = System.nanoTime();
            Double timeMillis = (double)(end - start)/1e6;
            runningTimes.add(timeMillis);
        }

        double mean = getMean(runningTimes);
        double standardDeviation = getStandardDeviation(mean, runningTimes);
        double median = getMedian(runningTimes);

//         comparing to Dijkstra
        Long start = System.nanoTime();
        Map<Integer, Integer> dijkstraShortestPaths = graph.dijkstra(source);
        Long end = System.nanoTime();
        double dijkstraTimeMillis = (double)(end - start)/1e6;
//        System.out.println(source);
        // validation
        for(int i = 0; i < graph.getNodeCount(); i ++){
            if(!Objects.equals(dagShortestPaths.get(i), dijkstraShortestPaths.get(i))){
                System.out.println("Shortest Path mismatch " + "node " + i + " dag : " + dagShortestPaths.get(i) + ", dijkstra : " + dijkstraShortestPaths.get(i));
            }
        }
        double speedup = dijkstraTimeMillis / mean;

        Files.writeString(file,
                "DAG-Topo"      + ", " +
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
        Graph<Integer> graph = generator.generate(10000);
//        Map<Integer, Integer> shortest = graph.dijkstra(0);
//        for(int i = 0; i < graph.getNodeCount(); i ++){
//            System.out.println("Shortest Path " + "node " + i + ": " + shortest.get(i));
//        }
        dagShortestPathBenchmarks(graph);
    }
}
