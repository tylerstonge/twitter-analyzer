import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class PersistentBTree {

    private int order;
    private Node root;
    private File file;
    private long eof;
    private static int fileoffset = 8;

    /**
    * Node layout
    *
    * 8 bytes               parent
    * 1 byte                isLeaf
    * 8 * (2 * order)       children pointers
    * 8 * (2 * order - 1)   keys array
    */
    private class Node {
        public long offset;
        public long parent;
        public boolean isLeaf;
        public long[] children;
        public long[] keys;

        public Node(long child) {
            this.parent = 0;
            this.isLeaf = false;
            this.keys = new long[2*order - 1];
            Arrays.fill(this.keys, Long.MAX_VALUE);
            this.children = new long[2*order];
            this.children[0] = child;
        }

        public Node(long offset, long parent, boolean isLeaf, long[] children, long[] keys) {
            this.offset = offset;
            this.parent = parent;
            this.isLeaf = isLeaf;
            this.children = children;
            this.keys = keys;
        }

        public Node(long parent, int order, boolean isLeaf) {
            this.parent = parent;
            this.keys = new long[2*order - 1];
            Arrays.fill(this.keys, Long.MAX_VALUE);
            this.children = new long[2*order];
            this.isLeaf = isLeaf;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public boolean isFull() {
            for (int i = 0; i < keys.length; i++)
                if (keys[i] == Long.MAX_VALUE) { return false; }
            return true;
        }
    }

    public PersistentBTree(int order) {
        this.order = order;
        this.eof = fileoffset;
        // Get or create the file
        try {
            file = new File("btree");
            if (!file.exists()) {
                // No BTree data file exists
                file.createNewFile();
                this.root = new Node(0, order, true);
                // Keep track of EOF as first long in file
                writeNode(root);
                updateEOF(this.eof);
            } else {
                root = readNode(this.fileoffset);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insert(long key, long value) {
        try {
            Node r = readNode(root.offset);
            if (r.isFull()) {
                Node s = new Node(r.offset);
                this.eof = writeNode(eof, s);
                s.setOffset(this.eof);
                updateEOF(this.eof);
                splitChild(s, 1, r);
                insertNonFull(s, key, value);
            } else {
                insertNonFull(r, key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printTree() {
        printTree(root);
    }

    public void printTree(Node n) {
        try {
            n = readNode(n.offset);
            System.out.println("NODE -- " + n.isLeaf);
            for (int i = 0; i < n.keys.length; i++) {
                System.out.println("K: " + (n.keys[i] == Long.MAX_VALUE ? "null" : n.keys[i]));
            }
            if (!n.isLeaf) {
                for (int i = 0; i < n.children.length; i++) {
                    if (n.children[i] > 0L)
                        printTree(readNode(n.children[i]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertNonFull(Node x, long k, long v) throws IOException {
        int i = x.keys.length - 1;
        if (x.isLeaf) {
            // Shift keys bigger than k to the right
            while (i > 0 && k < x.keys[i - 1]) {
                x.keys[i] = x.keys[i - 1];
                i--;
            }
            x.keys[i] = k;
            writeNode(x);
        } else {
            while (i >= 0 && k < x.keys[i]) {
                i--;
            }
            Node c = readNode(x.children[i]);
            if (c.isFull()) {
                splitChild(x, i, c);
                if (k > x.keys[i])
                    i++;
            }
            insertNonFull(c, k, v);
        }
    }

    public void splitChild(Node x, int i, Node y) throws IOException {
        // Allocate the new node to eof, and move where the current eof is
        Node z = new Node(x.offset);
        z.offset = writeNode(z);

        z.isLeaf = y.isLeaf;
        x.isLeaf = false;

        // t is the middle key
        int t = order - 1;

        for (int j = 0; j < t; j++) {
            // Copy key and values
            z.keys[j] = y.keys[j+t];
            y.keys[j+t] = 0;
        }

        if (!y.isLeaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
                y.children[j + t] = 0;
            }
        }

        for (int j = x.keys.length; j >= i + 1; j--) {
            x.children[j] = x.children[j - 1];
        }
        x.children[i] = z.offset;

        for (int j = x.keys.length - 1; j > i; j--) {
            x.keys[j] = x.keys[j - 1];
        }

        x.keys[i] = y.keys[t - 1];

        writeNode(y);
        writeNode(z);
        writeNode(x);
    }


    private Node readNode(long offset) throws IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(offset);

        // get parent location
        long parent = channel.readLong();

        // get isLeaf
        boolean isLeaf = channel.readUnsignedByte() == (byte) 0x1 ? true : false;

        // get children
        long[] children = new long[2*order];
        for (int i = 0; i < order; i++) {
            children[i] = channel.readLong();
        }

        long keys[] = new long[2*order - 1];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = channel.readLong();
        }
        channel.close();

        return new Node(offset, parent, isLeaf, children, keys);
    }

    private long writeNode(Node node) throws IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        if (node.offset == 0) {
            node.offset = eof;
        }
        channel.seek(node.offset);

        // Store parent
        channel.writeLong(node.parent);

        // Store isLeaf
        if (node.isLeaf) { channel.writeByte((byte) 0x1); }
        else { channel.writeByte((byte) 0x0); }

        // Store children pointers
        for (int i = 0; i < node.children.length; i++) {
            channel.writeLong(node.children[i]);
        }

        // Store keys
        for (int i = 0; i < node.keys.length; i++) {
            channel.writeLong(node.keys[i]);
        }
        
        if (node.offset == eof) {
            this.eof = channel.getFilePointer();
            updateEOF(this.eof);
        }
        channel.close();
        
        return node.offset;
    }

    private long writeNode(long offset, Node node) throws IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(node.offset);

        // Store parent
        channel.writeLong(node.parent);

        // Store isLeaf
        if (node.isLeaf) { channel.writeByte((byte) 0x1); }
        else { channel.writeByte((byte) 0x0); }

        // Store children pointers
        for (int i = 0; i < node.children.length; i++) {
            channel.writeLong(node.children[i]);
        }

        // Store keys
        for (int i = 0; i < node.keys.length; i++) {
            channel.writeLong(node.keys[i]);
        }

        long position = channel.getFilePointer();
        channel.close();

        return position;
    }

    private void updateEOF(long eof) throws FileNotFoundException, IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(0);
        channel.writeLong(eof);
        channel.close();
    }
}
