package main.java;

import main.java.DS.Edge;

import java.util.*;



public class Graph<E> {
    /// we use an edge list for easier implementation of Edge-Centric Algorithms
    List<Edge<E>> edges;

    Map<E, List<Edge<E>>> neighbors;
    public Graph(){
        edges = new ArrayList<>();
        neighbors = new HashMap<>();
    }

    private record Pair<E>(Edge<E> fst, E snd) implements Comparable<Pair<E>> {
        @Override
        public int compareTo(Pair<E> other) {
            return Integer.compare(this.fst.weight, other.fst.weight);
        }
    }

    public void addEdge(E from, E to, int weight){
        Edge<E> edge = new Edge<>(from, to, weight);
        if (!neighbors.containsKey(from))
            neighbors.put(from, new ArrayList<>());

        if(!neighbors.containsKey(to))
            neighbors.put(to, new ArrayList<>());

        neighbors.get(from).add(edge);
        neighbors.get(to).add(edge);
        edges.add(edge);
    }

    public void addDirectedEdge(E from, E to, int weight){
        Edge<E> edge = new Edge<>(from, to, weight);
        if (!neighbors.containsKey(from))
            neighbors.put(from, new ArrayList<>());
        neighbors.get(from).add(edge);
    }

    public List<Edge<E>> primMST(){
        List<Edge<E>> minTree = new ArrayList<>();
        PriorityQueue<Pair<E>> pq = new PriorityQueue<>();

        Set<E> visited = new HashSet<>();

        if(edges.isEmpty())
            return null;

        E start = edges.getFirst().from;

        pq.add(new Pair<>(new Edge<>(null,null,0), start));
        while(!pq.isEmpty()){
            Pair<E> p = pq.poll();
            E node = p.snd;
            if (visited.contains(node)) continue; // ignore stale entries

            visited.add(node);
            if (p.fst.from != null){ // check to ignore initial mock edge
                minTree.add(p.fst);
            }
            for(Edge<E> edge : neighbors.get(node)){
                E neighbor = (edge.from == node)? edge.to : edge.from;
                // if condition because in bidirectional edges there is no guarantee
                pq.add(new Pair<>(edge, neighbor));
            }
        }
//        System.out.println(neighbors.size());
        if (visited.size() != neighbors.size())
            return null; //not all visited ... disconnected graph
        return minTree;
    }

}
