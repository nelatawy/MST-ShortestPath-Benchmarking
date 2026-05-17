package main.java.graph;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof Edge<?>)) return false;
        return from.equals(((Edge<?>) obj).from) && to.equals(((Edge<?>) obj).to);
    }

    @Override
    public int hashCode(){
        return Objects.hash(from, to);
    }
}