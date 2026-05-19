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
        int warmupItrCnt = 10;
        List<Double> runningTimes = new ArrayList<>();
        Path file = Paths.get("mst-benchmarks.csv");
        if (!Files.exists(file)){
            Files.createFile(file);
            Files.writeString(file, "Algorithm, NodeCount, EdgeCount, Distribution, MeanTime, StdDev, MedianTime\n");
        }

        for(int i = 0; i < warmupItrCnt; i++){
            graph.primMST();
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

        for(int i = 0; i < warmupItrCnt; i++){
            graph.kruskalMST();
        }
        for(int i = 0; i < itrCnt; i++){
            // Shuffle edges to random order before Kruskal benchmark to ensure fair comparison
            // (TimSort can exploit nearly-sorted data, making Kruskal artificially fast after Prim)
            graph.shuffleEdges();

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
        int warmUpItr = 25;
        int measurementItr = 5;
        int source = new Random(42).nextInt(graph.getNodeCount());


        for(int i = 0; i < warmUpItr; i++) {
            graph.dijkstra(source);
        }
        List<Double> runningTimes = new ArrayList<>();
        Path file = Paths.get("general-sssp-benchmarks.csv");
        if (!Files.exists(file)){
            Files.createFile(file);
            Files.writeString(file, "Algorithm, NodeCount, EdgeCount, Distribution, MeanTime, StdDev, MedianTime\n");
        }

        Map<Integer, Integer> shortestPaths = null;
        for(int i = 0; i < measurementItr; i++){
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

        Files.writeString(file, "Dijkstra, " +
                graph.getNodeCount() + ", " +
                graph.getEdgeCount() + ", " +
                type.name()          + ", " +
                mean                 + ", " +
                standardDeviation    + ", " +
                median               + "\n", StandardOpenOption.APPEND);
    }

    public static void dagShortestPathBenchmarks(Graph<Integer> graph) throws IOException {
        int warmUpItr = 25;
        int measurementItr = 10;
        int source = new Random(42).nextInt(graph.getNodeCount());

        for(int i = 0; i < warmUpItr; i++) {
            graph.dijkstra(source);
        }

        List<Double> runningTimes = new ArrayList<>();
        Path file = Paths.get("dag-sssp-benchmarks.csv");
        if (!Files.exists(file)){
            Files.createFile(file);
            Files.writeString(file, "Algorithm, NodeCount, EdgeCount, MeanTime, StdDev, MedianTime, SpeedupMultiplier\n");
        }

        Map<Integer, Integer> dijkstraShortestPaths = null;
//         comparing to Dijkstra
        for(int i = 0; i < measurementItr; i++){
            Long start = System.nanoTime();
            dijkstraShortestPaths = graph.dijkstra(source);
            // randomizing the source
            Long end = System.nanoTime();
            Double timeMillis = (double)(end - start)/1e6;
            runningTimes.add(timeMillis);
        }

        double dijkstraMean = getMean(runningTimes);

        for(int i = 0; i < warmUpItr; i++) {
            graph.dagShortestPath(source);
        }
        runningTimes.clear();
        Map<Integer, Integer> dagShortestPaths = null;

        for(int i = 0; i < measurementItr; i++){
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


        // validation
        for(int i = 0; i < graph.getNodeCount(); i ++){
            if(!Objects.equals(dagShortestPaths.get(i), dijkstraShortestPaths.get(i))){
                System.out.println("Shortest Path mismatch " + "node " + i + " dag : " + dagShortestPaths.get(i) + ", dijkstra : " + dijkstraShortestPaths.get(i));
            }
        }
        double speedup = dijkstraMean / mean;

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

    public static void populateMSTResults() throws IOException {
        List<Integer> sizes = new ArrayList<>(List.of(500,1000,2500, 5000, 10000));
        for (Integer size : sizes){
            GraphGenerator generator = new DAGGenerator();
            Graph<Integer> graph = generator.generate(size);
            MSTConstructionBenchmark(graph, GraphType.DAG);
        }
        for (Integer size : sizes){
            GraphGenerator generator = new SparseGenerator();
            Graph<Integer> graph = generator.generate(size);
            MSTConstructionBenchmark(graph, GraphType.SPARSE);
        }

        for (Integer size : sizes){
            GraphGenerator generator = new DenseGenerator();
            Graph<Integer> graph = generator.generate(size);
            MSTConstructionBenchmark(graph, GraphType.DENSE);
        }

        for (Integer size : sizes){
            GraphGenerator generator = new CompleteGenerator();
            Graph<Integer> graph = generator.generate(size);
            MSTConstructionBenchmark(graph, GraphType.COMPLETE);
        }
    }

    public static void populateDijkstraSSSPResults() throws IOException{
        List<Integer> sizes = new ArrayList<>(List.of(10000));
        for (Integer size : sizes){
            GraphGenerator generator = new DAGGenerator();
            Graph<Integer> graph = generator.generate(size);
            generalShortestPathBenchmark(graph, GraphType.DAG);
        }
//        for (Integer size : sizes){
//            GraphGenerator generator = new SparseGenerator();
//            Graph<Integer> graph = generator.generate(size);
//            generalShortestPathBenchmark(graph, GraphType.SPARSE);
//        }
//
//        for (Integer size : sizes){
//            GraphGenerator generator = new DenseGenerator();
//            Graph<Integer> graph = generator.generate(size);
//            generalShortestPathBenchmark(graph, GraphType.DENSE);
//        }
//
//        for (Integer size : sizes){
//            GraphGenerator generator = new CompleteGenerator();
//            Graph<Integer> graph = generator.generate(size);
//            generalShortestPathBenchmark(graph, GraphType.COMPLETE);
//        }
    }

    public static void populateDAGSSSPResults() throws IOException {
        List<Integer> sizes = new ArrayList<>(List.of(1000, 2500, 5000, 7500, 10000));
        GraphGenerator generator = new DAGGenerator();
        for (Integer size : sizes) {
            Graph<Integer> graph = generator.generate(size);
            dagShortestPathBenchmarks(graph);
        }
    }

    public static void main() throws IOException {
        populateDijkstraSSSPResults();
//        populateDAGSSSPResults();
    }
}
