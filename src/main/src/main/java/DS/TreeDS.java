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
        // path compression: make nodes point directly to root
        if (itr.parent != null) {
            itr.parent = (TreeNode) getSetNode(itr);
        }
        while (itr.parent != null){
            itr = itr.parent;
        }
        return itr.data;
    }
    
    private TreeNode getSetNode(TreeNode node) {
        if (node.parent == null) return node;
        node.parent = getSetNode(node.parent); // path compression
        return node.parent;
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

    @Override
    public int getSetCount(){
        return this.setCount;
    }
}
