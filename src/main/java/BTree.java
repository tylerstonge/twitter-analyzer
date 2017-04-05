public class BTree {

    private int order;
    private Node root;

    public class Node {
        private Object[] keys;
        private Object[] values;
        private Node[] children;
        private Node parent;

        /**
        * Constructor
        * @param parent The parent to this node.
        * @param order The number of children each node will have.
        */
        public Node(Node parent, int order) {
            // The number of keys is one less than the number of children
            this.keys = new Object[order - 1];
            // Each key has a corresponding value
            this.values = new Object[order - 1];
            // Number of children is equal to the order of the tree.
            this.children = new Node[order];
            // For backtracing purposes, keep track of the parent.
            this.parent = parent;
        }

        public boolean isFull() {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == null)
                    return false;
            }
            return true;
        }

        public void insert(Object newObject) {

        }
    }

    /**
    * Create an empty BTree of order n.
    * @param order number of children for each node
    */
    public BTree(int order) {
        this.order = order;
    }

    public void insert(Object o) {
        if (root == null) {
            root = new Node(null, order);
        }
    }
}
