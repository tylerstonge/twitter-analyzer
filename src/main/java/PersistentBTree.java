import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class PersistentBTree {

    private int order;
    private Node root;
    private FileChannel file;

    private class Node {
        public long address;
        public long parent;
        public boolean isLeaf;
        public long[] children;
        public Entry[] entries;

        public Node(Node parent, int order, boolean isLeaf) {
            this.parent = parent;
            this.entries = new Entry[order - 1];
            this.children = new long[order];
        }

        public long setAddress(long address) {
            this.address = address;
        }
    }

    private class Entry {
        public long key;
        public long value;

        public Entry(long key, long value) {
            this.key = key;
            this.value = value;
        }
    }

    public PersistentBTree(int order) {
        // Get or create the file
        File f = new File("btree.dat");
        if (!f.exists()) {
            // No BTree data file exists
            f.createNewFile();
            file = new RandomAccessFile(f, "rw").getChannel();
            root = new Node(null, order, true);
            putNode(0, root);
        } else {
            // There is some BTree data
            file = new RandomAccessFile(f, "rw").getChannel();
            root = getNode(0);
        }
    }

    private static Node getNode(long address) {
        ByteBuffer length = ByteBuffer.allocate(4);
        try {
            file.read(length);
            ByteBuffer oldRoot = ByteBuffer.allocate(length.getInt());
            file.read(oldRoot);
            root = oldRoot;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * Node layout
    *
    * 8 bytes       parent
    * 1 byte        isLeaf
    * 8 * order     children pointers
    * 4 bytes       size of entries array in bytes
    * ???           entries array
    */
    private static void putNode(long address, Node node) {
        file.position(address);

        // Store parent
        ByteBuffer parent = ByteBuffer.allocate(8);
        parent.put(node.parent);
        file.write(parent);

        // Store isLeaf
        ByteBuffer isLeaf = ByteBuffer.allocate(1);
        node.isLeaf ? isLeaf.put((byte) 0x1) : isLeaf.put((byte) 0x0);
        file.write(isLeaf);

        // Store children pointers
        ByteBuffer children = ByteBuffer.allocate(8 * order); // 8-bytes per long
        for (int i = 0; i < node.children.length; i++) {
            children.putLong(node.children[i]);
        }
        file.write(children);

        // Convert entries array to bytes
        byte[] entries = serialize(node.entries);

        // Store entries length
        ByteBuffer entriesLength = ByteBuffer.allocate(4);
        entriesLength.putInt(entries.length);
        file.write(entriesLength);

        // Store entries
        ByteBuffer entries = ByteBuffer.wrap(entries);
        file.write(entries);
    }



    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}
