package main.java.DS;

import java.util.HashMap;
import java.util.Map;



public class TreeDS<E> implements DisjointSet<E>{

    class TreeNode {
        E data;
        TreeNode parent;
        TreeNode(E data, TreeNode parent){
            this.data = data;
            this.parent = parent;
        }
    }

    Map<E, TreeNode> nodeMap;
    int setCount;

    public TreeDS(){
        nodeMap = new HashMap<>();
        setCount = 0;
    }
    @Override
    public E getSet(E ele) {
        TreeNode itr = nodeMap.get(ele);
        if (itr == null) return null;
        while (itr.parent != null){
            itr = itr.parent;
        }
        return itr.data;
    }

    @Override
    public void unionSets(E first, E second) {
        TreeNode firstNode = nodeMap.get(getSet(first));
        TreeNode secondNode = nodeMap.get(getSet(second));
        if(firstNode == null || secondNode == null)
            return;
        secondNode.parent = firstNode;
        setCount--;
    }

    @Override
    public void addSet(E rootEle) {
        TreeNode newNode = new TreeNode(rootEle, null);
        nodeMap.put(rootEle, newNode);
        setCount++;
    }

    public int getSetCount(){
        return this.setCount;
    }
}
