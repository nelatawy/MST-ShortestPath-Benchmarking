package main.java.graph;

import main.java.DS.DisjointSet;
import main.java.DS.TreeDS;

import java.util.*;



public class Graph<E> {
    // we use an edge list for easier implementation of Edge-Centric Algorithms
    List<Edge<E>> edges;

    Map<E, List<Edge<E>>> neighbors;

    boolean edgesSorted;
    int nodeCount;

    public Graph(){
        edges = new ArrayList<>();
        neighbors = new HashMap<>();
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
     * Does exactly what you think it does.
     * @param from src
     * @param to destination
     * @param weight weight, set to 0 for unweighted behavior
     */
    public void addEdge(E from, E to, int weight){
        Edge<E> edge = new Edge<>(from, to, weight);
        if (!neighbors.containsKey(from)){
            neighbors.put(from, new ArrayList<>());
            nodeCount++;
        }

        if(!neighbors.containsKey(to)){
            neighbors.put(to, new ArrayList<>());
            nodeCount++;
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
            neighbors.put(from, new ArrayList<>());
            nodeCount++;
        }

        if(!neighbors.containsKey(to)){
            neighbors.put(to, new ArrayList<>());
            nodeCount++;
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
        PriorityQueue<EdgeRecord<E>> pq = new PriorityQueue<>();

        Set<E> visited = new HashSet<>();

        if(edges.isEmpty())
            return null;

        E start = edges.getFirst().from;

        pq.add(new EdgeRecord<>(new Edge<>(null,null,0), start));
        while(!pq.isEmpty()){
            EdgeRecord<E> p = pq.poll();
            E node = p.snd;
            if (visited.contains(node)) continue; // ignore stale entries

            visited.add(node);
            if (p.fst.from != null){ // check to ignore initial mock edge
                minTree.add(p.fst);
            }
            for(Edge<E> edge : neighbors.get(node)){
                E neighbor = (edge.from.equals(node))? edge.to : edge.from;
                // if condition because in bidirectional edges there is no guarantee
                pq.add(new EdgeRecord<>(edge, neighbor));
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
        if (!edgesSorted){
            // to be faster on multiple queries
            Collections.sort(edges);
            edgesSorted = true;
        }

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
                if(visited.contains(neighbor)) continue; //extra optimization to ignore pre-visited nodes
                pq.add(new WeightRecord<>(record.cost + edge.weight, neighbor));
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
        Map<E, Integer> visited = new HashMap<>();
        // we save the status of it (0 : pushed to the stack, 1: visited once (processing children), 2: finished processing children)
        Stack<E> stack = new Stack<>();

        // first we get the topological sorting of nodes
        List<E> topoSorted = new ArrayList<>();
        Map<E, Integer> shortestPaths = new HashMap<>();
        stack.push(source);
        while(!stack.empty()){
            E top = stack.pop();
            Integer status = visited.get(top);
            if(status == null) { // first encounter
                stack.push(top); // to be processed afterward
                visited.put(top, 1); // processing

                for(Edge<E> edge : neighbors.get(top)){
                    E neighbor = edge.to;
                    Integer neighborStatus = visited.get(neighbor);
                    if (neighborStatus != null &&  neighborStatus.equals(2))  continue;// to not push unnecessary elements
                    stack.push(neighbor);
                    // we mark visited here to avoid unnecessary pushes that slow things down
                }
            }
            else if (status.equals(1)) { // this is the second visit you can process self
                visited.put(top, 2);
                topoSorted.add(top);
            }
        }
        Collections.reverse(topoSorted);

        // then we process nodes in topological order
        // by definition, all nodes in a path leading to a specified node are of lower index than that of the node itself
        // so on processing a node we already have the shortest path to it, relaxation is correct like in Dijkstra's algorithm
        shortestPaths.put(source, 0);
        for(E node : topoSorted){
            int baseCost = shortestPaths.get(node); // can't be null by definition
            for(Edge<E> edge : neighbors.get(node)){
                Integer oldCost = shortestPaths.get(edge.to); // can be null
                int newCost = baseCost + edge.weight;
                if(oldCost == null || oldCost.compareTo(newCost) > 0)
                    shortestPaths.put(edge.to, newCost);
            }
        }
        return shortestPaths;
    }

    public int getNodeCount(){return this.nodeCount;}

    public int getEdgeCount(){return this.edges.size();}

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
