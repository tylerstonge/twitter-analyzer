import java.util.Arrays;
import java.util.Comparator;

public class BTree {

    private int order;
    private Node root;

    public class Node {
        Entry[] entries;
        Node[] children;
        Node parent;

        /**
        * Constructor
        * @param parent the parent to this node.
        * @param order the number of children each node will have.
        */
        public Node(Node parent, int order) {
            this.entries = new Entry[order - 1];
            this.children = new Node[order];
            this.parent = parent;
        }

        public Node(Node parent, Entry[] entries, Node[] children) {
            this.parent = parent;
            this.entries = entries;
            this.children = children;
        }

        public void sort() {
            Arrays.sort(entries, new EntryComparator());
            Arrays.sort(children, new ChildrenComparator());
        }

        public boolean isFull() {
            for (int i = 0; i < entries.length; i++) {
                if (entries[i] == null)
                    return false;
            }
            return true;
        }

        public boolean isEmpty() {
            for (int i = 0; i < entries.length; i++) {
                if (entries[i] != null)
                    return false;
            }
            return true;
        }

        public boolean isLeaf() {
            for (int i = 0; i < children.length; i++) {
                if (children[i] != null)
                    return false;
            }
            return true;
        }

        public boolean insertEntry(Entry e) {
            for (int i = 0; i < entries.length; i++) {
                if (entries[i] == null) {
                    entries[i] = e;
                    sort();
                    return true;
                }
            }
            return false;
        }

        public boolean insertChild(Node c) {
            for (int i = 0; i < children.length; i++) {
                if (children[i] == null) {
                    children[i] = c;
                    sort();
                    return true;
                }
            }
            return false;
        }

        public void removeChild(Node c) {
            for (int i = 0; i < children.length; i++) {
                if (children[i] == c) {
                    children[i] = null;
                    sort();
                    break;
                }
            }
        }
    }

    public class Entry {
        Comparable key;
        Object value;

        public Entry(Comparable key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
    * Create an empty BTree of order n.
    * @param order number of children for each node
    */
    public BTree(int order) {
        this.order = order;
        this.root = new Node(null, order);
    }

    public boolean insert(Comparable k, Object v) {
        if (root.isEmpty()) {
            // Initial case
            root.entries[0] = new Entry(k, v);
            return true;
        }
        return insert(root, k, v);
    }

    public void split(Node n) {
        int middle = (order - 1) / 2;

        // Insert children
        Node parent;
        if (n.parent == null) {
            // This is root
            parent = n;
        } else {
            parent = n.parent;
            parent.removeChild(n);
            parent.insertEntry(n.entries[middle]);
        }

        // Create left subtree
        Node left = new Node(parent, order);
        // Store values
        for (int i = 0; i < middle; i++) {
            Entry e = n.entries[i];
            if (e != null) {
                left.entries[i] = new Entry(e.key, e.value);
                n.entries[i] = null;
            }
        }

        // Create right subtree
        Node right = new Node(parent, order);
        for (int i = middle + 1; i < n.entries.length; i++) {
            Entry e = n.entries[i];
            if (e != null) {
                right.entries[i] = new Entry(e.key, e.value);
                n.entries[i] = null;
            }
        }

        left.sort();
        right.sort();

        parent.insertChild(left);
        parent.insertChild(right);
        parent.sort();
        if (parent.isFull()) split(parent);
    }

    public boolean insert(Node n, Comparable k, Object v) {
        if (n.isLeaf()) {
            if (n.insertEntry(new Entry(k, v))) {
                if (n.isFull()) split(n);
                return true;
            }
        } else {
            // Descend further
            for (int i = 0; i < n.entries.length; i++) {
                if (n.entries[i] != null && n.entries[i].key.compareTo(k) > 0) {
                    // This object is larger, current index of children must be what we want.
                    return insert(n.children[i], k, v);
                } else if (n.entries[i] != null && n.entries[i].key.compareTo(k) < 0) {
                    return insert(n.children[i + 1], k, v);
                } else {
                    return insert(n.children[i], k, v);
                }
            }
        }
        return false;
    }

    /**
    * Convenience function which calls printTree on root.
    */
    public void printTree() {
        printTree(root);
    }

    /**
    * Print the values of the node, and all the values of its subtrees.
    * @param n the node to start printing from
    */
    public void printTree(Node n) {
        // Print values of this node
        int x = 0;
        for (int i = 0; i < n.entries.length; i++) {;
            if (n.entries[i] != null)
                System.out.println((++x) + ") K: " + n.entries[i].key.toString() + "; V: " + n.entries[i].value.toString());
            else
                System.out.println((++x) + ") null");
        }
        // Print the values of its subtrees recursively
        for (int i = 0; i < n.children.length; i++) {
            if (n.children[i] != null) {
                System.out.println("MY PARENT IS: " + (n.parent == null ? "null" : n.parent.entries[0].key));
                printTree(n.children[i]);
            }
        }
    }

    private class EntryComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Entry e1 = (Entry) o1;
            Entry e2 = (Entry) o2;
            if (e2 != null && e1 != null) {
                return e1.key.compareTo(e2.key);
            } else if (e1 == null && e2 != null) {
                return 1;
            } else if (e2 == null && e1 != null) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private class ChildrenComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Node n1 = (Node) o1;
            Node n2 = (Node) o2;
            if (n1 != null && n2 != null && n1.entries[0] != null && n2.entries[0] != null) {
                return n1.entries[0].key.compareTo(n2.entries[0].key);
            } else if (n1 != null && n1.entries[0] != null && (n2 == null || n2.entries[0] == null)) {
                return -1;
            } else if (n2 != null && n2.entries[0] != null && (n1 == null || n1.entries[0] == null)) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
