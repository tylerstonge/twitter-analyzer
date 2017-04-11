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

public class PersistentBTree {

    private int order;
    private Node root;
    private File file;
    private long eof;
    private static int blocksize;
    private static int maxentries;

    /**
    * Node layout
    *
    * 8 bytes           parent
    * 1 byte            isLeaf
    * 8 * order         children pointers
    * (order - 1) * 16   entries array
    */
    private class Node {
        public long offset;
        public long parent;
        public boolean isLeaf;
        public long[] children;
        public long[] entries;

        public Node(long child) {
            this.parent = 0;
            this.isLeaf = false;
            this.entries = new long[(order - 1) * 2]; // store keys alongside values 2*i are keys, 2*i+1 are values
            this.children = new long[order];
            this.children[0] = child;
        }

        public Node(long offset, long parent, boolean isLeaf, long[] children, long[] entries) {
            this.offset = offset;
            this.parent = parent;
            this.isLeaf = isLeaf;
            this.children = children;
            this.entries = entries;
        }

        public Node(long parent, int order, boolean isLeaf) {
            this.parent = parent;
            this.entries = new long[(order - 1) * 2];
            this.children = new long[order];
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public boolean isFull() {
            for (int i = 0; i < entries.length; i += 2)
                if (entries[i] == 0) { return false; }
            return true;
        }
    }

    public PersistentBTree(int order) {
        this.blocksize = 8 + 1 + (8 * order) + (order - 1) * 16;
        this.maxentries = (order - 1) * 2;
        // Get or create the file
        try {
            file = new File("btree");
            if (!file.exists()) {
                // No BTree data file exists
                file.createNewFile();
                this.root = new Node(0, order, true);
                // Keep track of EOF as first long in file
                this.eof = writeNode(8, root);
                updateEOF(this.eof);
            } else {
                root = readNode(0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insert(long key, long value) {
        try {
            Node r = root;
            if (r.isFull()) {
                System.out.println("Node full, gotta split");
                Node s = new Node(r.offset);
                this.eof = writeNode(eof, s);
                s.setOffset(this.eof);
                updateEOF(this.eof);
                splitChild(s, 1, r);
                insertNonFull(s, key, value);
            } else {
                System.out.println("Inserting " + key + " - " + value);
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
            for (int i = 0; i < n.entries.length; i+=2) {
                System.out.println("K: " + n.entries[i] + "; V: " + n.entries[i + 1]);
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
        int i = order - 1;
        if (x.isLeaf) {
            // Shift keys bigger than k to the right
            while (i >= 0 && k < x.entries[i*2]) {
                x.entries[i + 2] = x.entries[i];
                x.entries[i + 3] = x.entries[i + 1];
                i--;
            }
            x.entries[i] = k;
            x.entries[i + 1] = v;
            writeNode(x);
        } else {
            while (i >= 0 && k < x.entries[i*2]) {
                Node c = readNode(x.children[i]);
                if (c.isFull()) {
                    splitChild(x, i, c);
                    if (k > x.entries[i*2])
                        i++;
                }
                insertNonFull(c, k, v);
            }
        }
    }

    public void splitChild(Node x, int i, Node y) throws IOException {
        // Allocate the new node to eof, and move where the current eof is
        Node z = new Node(x.offset);
        this.eof = writeNode(this.eof, z);
        z.setOffset(eof);
        updateEOF(this.eof);

        z.isLeaf = y.isLeaf;

        // t is the middle key
        int t = order / 2;

        for (int j = 0; j < t; j+=2) {
            // Copy key and values
            z.entries[j] = y.entries[j+t];
            z.entries[j+1] = y.entries[j+t+1];
            y.entries[j+t] = 0;
            y.entries[j+t+1] = 0;
        }

        if (!y.isLeaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
                y.children[j + t] = 0;
            }
        }

        for (int j = order; j > i; j--) {
            x.children[j + 1] = x.children[j];
        }
        x.children[i + 1] = z.offset;

        for (int j = order - 1; j > i; j--) {
            x.entries[j + 1] = x.entries[j];
            x.entries[j + 2] = x.entries[j + 1];
        }

        x.entries[i] = y.entries[t];
        x.entries[i + 1] = y.entries[t + 1];

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
        long[] children = new long[order];
        for (int i = 0; i < order; i++) {
            children[i] = channel.readLong();
        }

        long entries[] = new long[this.maxentries];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = channel.readLong();
        }
        channel.close();

        return new Node(offset, parent, isLeaf, children, entries);
    }

    private void writeNode(Node node) throws IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(node.offset);

        // Store parent
        channel.writeLong(node.parent);

        // Store isLeaf
        if (node.isLeaf) { channel.writeByte((byte) 0x1); }
        else { channel.writeByte((byte) 0x0); }

        // Store children pointers
        for (int i = 0; i < order; i++) {
            channel.writeLong(node.children[i]);
        }

        // Store entries
        for (int i = 0; i < node.entries.length; i++) {
            channel.writeLong(node.entries[i]);
        }

        channel.close();
    }

    private long writeNode(long offset, Node node) throws IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(node.offset);

        // Store parent
        channel.writeLong(node.parent);

        // Store isLeaf
        ByteBuffer isLeaf = ByteBuffer.allocate(1);
        if (node.isLeaf) { channel.writeByte((byte) 0x1); }
        else { channel.writeByte((byte) 0x0); }

        // Store children pointers
        for (int i = 0; i < order; i++) {
            channel.writeLong(node.children[i]);
        }

        // Store entries
        ByteBuffer entries = ByteBuffer.allocate(this.maxentries * 8);
        for (int i = 0; i < node.entries.length; i++) {
            entries.putLong(node.entries[i]);
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
