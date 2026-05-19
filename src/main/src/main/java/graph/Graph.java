package main.java.graph;

import main.java.DS.DisjointSet;
import main.java.DS.TreeDS;

import java.util.*;



public class Graph<E> {
    // we use an edge list for easier implementation of Edge-Centric Algorithms
    List<Edge<E>> edges;

    Map<E, List<Edge<E>>> neighbors;

    // mappings to use instead of many map lookups in DAG-SSSP
    Map<E, Integer> mapToIdx;
    Map<Integer, E> mapToObj;

    // keeping the InDegrees
    List<Integer> inDegrees;
    List<Integer> outDegrees;

    boolean edgesSorted;
    int nodeCount;

    public Graph(){
        edges = new ArrayList<>();
        neighbors = new HashMap<>();
        mapToIdx = new HashMap<>();
        mapToObj = new HashMap<>();

        inDegrees = new ArrayList<>();
        outDegrees = new ArrayList<>();

        edgesSorted = true; //trivially sorted
        nodeCount = 0;
    }

    /**
     * A simple helper wrapper that is used in priority queues and to order edges by priority.
     * @param fst the edge part of the record
     * @param snd the satellite data we want to order using the weight, or the data we want to bundle with the edge
     */
    private record EdgeRecord<E>(Edge<E> fst, E snd) implements Comparable<EdgeRecord<E>> {
        @Override
        public int compareTo(EdgeRecord<E> other) {
            return Integer.compare(this.fst.weight, other.fst.weight);
        }
    }


    /**
     * A simple helper wrapper that is used in priority queues and to order nodes by a value.
     * @param node the satellite data we want to order using the value, or the data we want to bundle with the value
     */
    private record WeightRecord<E>(Integer cost, E node) implements Comparable<WeightRecord<E>> {
        @Override
        public int compareTo(WeightRecord<E> other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    /**
     * Helper method that handles node addition, mapping, neighbor array allocation, etc.
     * @param node val of the node we want to add.
     */
    private void addNode(E node){
        neighbors.put(node, new ArrayList<>());
        mapToIdx.put(node, nodeCount);
        mapToObj.put(nodeCount, node);
        inDegrees.add(0);
        outDegrees.add(0);
        nodeCount++;
    }
    /**
     * Does exactly what you think it does.
     * @param from src
     * @param to destination
     * @param weight weight, set to 0 for unweighted behavior
     */
    public void addEdge(E from, E to, int weight){
        Edge<E> edge = new Edge<>(from, to, weight);
        if (!neighbors.containsKey(from)){
            addNode(from);
        }

        if(!neighbors.containsKey(to)){
            addNode(to);
        }

        neighbors.get(from).add(edge);
        neighbors.get(to).add(edge);
        edges.add(edge);

        edgesSorted = false;
    }

    /**
     * Also does exactly what you think it does.
     * @param from src
     * @param to destination
     * @param weight weight, set to 0 for unweighted behavior
     */
    public void addDirectedEdge(E from, E to, int weight){
        Edge<E> edge = new Edge<>(from, to, weight);
        if (!neighbors.containsKey(from)){
            addNode(from);
        }

        if(!neighbors.containsKey(to)){
            addNode(to);
        }

        neighbors.get(from).add(edge);
        edges.add(edge);
        edgesSorted = false;
    }

    /**
     * Uses the popular Prim's Algorithm to get the Minimum Spanning Tree of the graph, using a greedy, Dijkstra-like approach.
     * @return the list of edges constructing the MST, or null if graph is disconnected.
     */
    public List<Edge<E>> primMST(){
        List<Edge<E>> minTree = new ArrayList<>();
        Set<E> visited = new HashSet<>();
        
        if(edges.isEmpty())
            return null;
        Map<E, Integer> minCost = new HashMap<>();
        Map<E, Edge<E>> minEdge = new HashMap<>();
        PriorityQueue<WeightRecord<E>> pq = new PriorityQueue<>();

        E start = edges.getFirst().from;

        for(E node : neighbors.keySet()) {
            minCost.put(node, Integer.MAX_VALUE);
        }

        minCost.put(start, 0);
        pq.add(new WeightRecord<>(0, start));

        while(!pq.isEmpty()){
            WeightRecord<E> record = pq.poll();
            E node = record.node;
            
            if(visited.contains(node)) continue;
            visited.add(node);

            if(minEdge.containsKey(node)) { // safety check for initial mock edge
                minTree.add(minEdge.get(node));
            }
            
            // Check all edges from this node
            for(Edge<E> edge : neighbors.get(node)){
                E neighbor = (edge.from.equals(node))? edge.to : edge.from;
                if(!visited.contains(neighbor) && edge.weight < minCost.get(neighbor)){
                    //only add to the priority queue if the new edge is better
                    minCost.put(neighbor, edge.weight);
                    minEdge.put(neighbor, edge);
                    pq.add(new WeightRecord<>(edge.weight, neighbor));
                }
            }
        }
        
        if (visited.size() != neighbors.size())
            return null; //not all visited ... disconnected graph
        return minTree;
    }

    /**
     * Uses Kruskal's Algorithm to get the Minimum Spanning Tree of the graph, using a sort-then-operate
     * strategy that sorts edges by weight then taken the edges that combine two disconnected components together.
     * @return the list of edges constructing the MST, or null if graph is disconnected.
     */
    public List<Edge<E>> kruskalMST(){
        List<Edge<E>> mstEdges = new ArrayList<>();
//        if (!edgesSorted){
            // to be faster on multiple queries
            Collections.sort(edges);
            edgesSorted = true;
//        }

        // construct the Disjoint Set
        DisjointSet<E> set = new TreeDS<>();
        for(Map.Entry<E, List<Edge<E>>> entry : neighbors.entrySet()){
            set.addSet(entry.getKey());
        }
        for(Edge<E> edge : edges){
            if(set.getSet(edge.from) != set.getSet(edge.to)){
                // two different sets that we can combine
                set.unionSets(edge.from, edge.to);
                mstEdges.add(edge);
            }
        }
        if (set.getSetCount() != 1)
            return null; //disconnected graph

        return mstEdges;
    }

    /**
     * Uses Dijkstra's Algorithm  to find the shortest paths from a given source;
     * @param source the origin of the search
     * @return a Map of every node value with the length of its shortest path, or null in case of negative weights;
     * a map entry value will be null if it's unreachable from source.
     */
    public Map<E, Integer> dijkstra(E source){
        if (edges.isEmpty())
            return null;

        if (!neighbors.containsKey(source))
            return null;

        Map<E, Integer> shortestPaths = new HashMap<>();
        for(Edge<E> edge : edges){
            if (edge.weight < 0)
                return null; // we can't allow for -ve edges + can lead to infinite cycles or uncontrollable growth of pq

        }
        PriorityQueue<WeightRecord<E>> pq = new PriorityQueue<>();
        Map<E, Integer> minWeight = new HashMap<>();

        for(E node : neighbors.keySet()) {
            minWeight.put(node, Integer.MAX_VALUE);
        }
        minWeight.put(source, 0);

        Set<E> visited = new HashSet<>();
        pq.add(new WeightRecord<>(0, source));
        while (!pq.isEmpty()){
            WeightRecord<E> record = pq.poll();
            if (visited.contains(record.node))
                continue;
            visited.add(record.node);
            shortestPaths.put(record.node, record.cost);
            for(Edge<E> edge : neighbors.get(record.node)){
                E neighbor = (edge.from.equals(record.node))? edge.to : edge.from;
                if(!visited.contains(neighbor) && (edge.weight + record.cost < minWeight.get(neighbor))){
                    minWeight.put(neighbor, edge.weight + record.cost);
                    pq.add(new WeightRecord<>(edge.weight + record.cost, neighbor));
                } //extra optimization to ignore pre-visited nodes
                // only add to PQ if not already visited and better than already discovered
            }

        }
        return shortestPaths;
    }

    /**
     * A shortest-path method that only operates on DAGs in O(V + E) relies on algorithmic invariance,
     * processes the nodes in topological order and relaxes nodes to obtain the shortest path
     * @param source the origin
     * @return a Map of every node value with the length of its shortest path, a map entry value will be null if it's unreachable from source.
     */
    public Map<E, Integer> dagShortestPath(E source){

        // first we get the topological sorting of nodes
        List<E> topoSorted = new ArrayList<>();
        Map<E, Integer> shortestPaths = new HashMap<>();
        // ----- Khan's Topological Sorting ----

        int[] inDegrees = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            inDegrees[i] = this.inDegrees.get(i);
        }
        // to avoid reallocation penalty and / or messing with global data
        // using c-style array to avoid unnecessary pointer hops or cache misses
        Queue<Integer> queue = new LinkedList<>();

        for(int i = 0; i < nodeCount; i++){
            if (inDegrees[i] == 0)
                queue.add(i);
        }
        while(!queue.isEmpty()){
            int top = queue.poll();
            topoSorted.add(mapToObj.get(top));
            for(Edge<E> edge : neighbors.get(mapToObj.get(top))){
                int neighborIdx = mapToIdx.get(edge.to);
                int newDeg = inDegrees[neighborIdx] - 1;
                inDegrees[neighborIdx] = newDeg;
                if (newDeg == 0) {
                    queue.add(neighborIdx);
                }
            }
        }
        // then we process nodes in topological order
        // by definition, all nodes in a path leading to a specified node are of lower index than that of the node itself
        // so on processing a node we already have the shortest path to it, relaxation is correct like in Dijkstra's algorithm
        shortestPaths.put(source, 0);
        for(E node : topoSorted){
            Integer baseCost = shortestPaths.get(node);
            if (baseCost == null) // it's unreachable from source
                continue;
            for(Edge<E> edge : neighbors.get(node)){
                Integer oldCost = shortestPaths.get(edge.to); // can be null
                int newCost = baseCost + edge.weight;
                if(oldCost == null || oldCost > newCost)
                    shortestPaths.put(edge.to, newCost);
            }
        }
        return shortestPaths;
    }

    public int getNodeCount(){return this.nodeCount;}

    public int getEdgeCount(){return this.edges.size();}

    /**
     * Shuffles the edge list to randomize order and resets the sorted flag.
     * Use this before benchmarking Kruskal to ensure fair comparison with unsorted edges.
     */
    public void shuffleEdges(){
        Collections.shuffle(edges);
        edgesSorted = false;
    }

    public List<E> BFS(E source){
        List<E> result = new ArrayList<>();
        Queue<E> queue = new LinkedList<>();
        queue.add(source);
        if (!neighbors.containsKey(source))
            return null;
        Set<E> visited = new HashSet<>();
        visited.add(source);
        while (!queue.isEmpty()){
            E front = queue.poll();
            result.add(front);
            for(Edge<E> edge : neighbors.get(front)){
                E neighbor = ((edge.from.equals(front))? edge.to : edge.from);
                if(visited.contains(neighbor)) continue;
                queue.add(neighbor);
                visited.add(neighbor);
            }
        }
        return result;
    }

}
