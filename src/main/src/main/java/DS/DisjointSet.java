package main.java.DS;

public interface DisjointSet<E> {

    /**
     * Get the value of the representative node of the given element's set.
     * @param ele the node whose set representative you want to find.
     * @return the representative
     */
    public E getSet(E ele);

    /**
     * Combine the sets having elements 'first' and 'second' into the set represented at first;
     * @param first the first element
     * @param second second element
     */
    public void unionSets(E first, E second);

    /**
     * Create a new set whose representative is 'rootEle'.
     * @param rootEle the new set's root
     */
    public void addSet(E rootEle);

}
