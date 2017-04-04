public class BTree<K,V> {
    
    private int order;
    
    public class Node<K,V> {
        private K[] keys;
        private V[] values;
        private Node<K,V>[] children;
        private Node<K,V> parent;
        
        // /**
        // * Constructor
        // * @param parent The parent to this node.
        // * @param order The number of children each node will have.
        // */
        // public Node(Node<K,V> parent, int order) {
        //     // The number of keys is one less than the number of children
        //     this.keys = (K[]) new Object[order - 1];
        //     // Each key has a corresponding value
        //     this.values = (V[]) new Object[order - 1];
        //     // Number of children is equal to the order of the tree.
        //     this.children = (Node<K,V>) new Object[order];
        //     // For backtracing purposes, keep track of the parent.
        //     this.parent = parent;
        // }
        
        
    }
    
    /**
    * Create an empty BTree of order n.
    * @param order number of children for each node
    */
    public BTree(int order) {
        this.order = order;
    }
}