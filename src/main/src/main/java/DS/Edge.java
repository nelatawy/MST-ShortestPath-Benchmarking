package main.java.DS;

public class Edge<E> implements Comparable<Edge<E>>{
    public E from;
    public E to;
    public int weight;

    public Edge(E from, E to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge<E> other) {
        return Integer.compare(this.weight, other.weight);
    }
}