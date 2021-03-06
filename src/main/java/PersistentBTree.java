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
import java.util.List;
import java.util.ArrayList;
import javafx.collections.ObservableList;

public class PersistentBTree {

    private static final int FILEOFFSET = 16;
    private int order;
    private Node root;
    private File file;
    private long eof;

    private class Node {
        public long offset;
        public boolean isLeaf;
        public long[] children;
        public long[] keys;
        public long[] values;

        public Node() {
            this.isLeaf = true;
            this.keys = new long[2*order - 1];
            this.values = new long[2*order - 1];
            Arrays.fill(this.keys, Long.MAX_VALUE);
            this.children = new long[2*order];
        }

        public Node(long child) {
            this.isLeaf = false;
            this.keys = new long[2*order - 1];
            this.values = new long[2*order - 1];
            Arrays.fill(this.keys, Long.MAX_VALUE);
            this.children = new long[2*order];
            this.children[0] = child;
        }

        public Node(long offset, boolean isLeaf, long[] children, long[] keys, long[] values) {
            this.offset = offset;
            this.isLeaf = isLeaf;
            this.children = children;
            this.keys = keys;
            this.values = values;
        }

        public Node(int order, boolean isLeaf) {
            this.keys = new long[2*order - 1];
            this.values = new long[2*order - 1];
            Arrays.fill(this.keys, Long.MAX_VALUE);
            this.children = new long[2*order];
            this.isLeaf = isLeaf;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public boolean isFull() {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == Long.MAX_VALUE)
                    return false;
            }
            return true;
        }
    }

    public PersistentBTree(int order) {
        this.order = order;
        this.eof = this.FILEOFFSET;
        // Get or create the file
        try {
            file = new File("btree");
            if (!file.exists()) {
                // No BTree data file exists
                file.createNewFile();
                this.root = new Node(order, true);

                // Keep track of EOF as first long in file
                root = writeNode(root);
                updateEOF(this.eof);
                updateRootOffset(root.offset);
            } else {
                root = readNode(getRootOffset());
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
                // New root
                Node s = new Node(r.offset);
                s = writeNode(s);
                root = s;
                updateRootOffset(s.offset);

                // Split root
                splitChild(s, 0, r);
                insertNonFull(s, key, value);
            } else {
                insertNonFull(r, key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void populateTweets(ObservableList<DisplayableTweet> t, Cache c) {
        populateTweets(this.root, t, c);
    }

    public void populateTweets(Node n, ObservableList<DisplayableTweet> t, Cache c) {
        try {
            n = readNode(n.offset);
            for (int i = 0; i < n.keys.length; i++) {
                if (n.keys[i] != Long.MAX_VALUE) {
                    Tweet tt = c.getTweetFromId(n.keys[i], n.values[i]);
                    DisplayableTweet dt = new DisplayableTweet(""+tt.getId(), tt.getAuthor(), tt.getText(), tt.getSentiment());
                    if (dt != null)
                        t.add(dt);
                }
            }
            
            if (!n.isLeaf) {
                for (int i = 0; i < n.children.length; i++) {
                    if (n.children[i] > 0) {
                        populateTweets(readNode(n.children[i]), t, c);
                    }
                }
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
            System.out.println("NODE -- isLeaf: " + n.isLeaf + " Offset: " + n.offset);
            for (int i = 0; i < n.keys.length; i++) {
                System.out.println("K: " + (n.keys[i] == Long.MAX_VALUE ? "null" : n.keys[i]));
                System.out.println("V: " +  n.values[i]);
            }
            if (!n.isLeaf) {
                for (int i = 0; i < n.children.length; i++) {
                    if (n.children[i] > 0) {
                        printTree(readNode(n.children[i]));
                    }
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
                x.values[i] = x.values[i - 1];
                i--;
            }
            
            if (i < x.keys.length - 1 && x.keys[i + 1] == k) {
                x.values[i] = v;
                writeNode(x);
            } else {
                x.keys[i] = k;
                x.values[i] = v;
                writeNode(x);
            }
        } else {
            // Find which child this key belongs in
            while (i >= 0 && k < x.keys[i]) {
                i--;
            }
            i++;
            Node c = readNode(x.children[i]);
            // If this child is full, split and try to insert again
            if (c.isFull()) {
                splitChild(x, i, c);
                if (k > x.keys[i])
                    c = readNode(x.children[i + 1]);
            }
            insertNonFull(c, k, v);
        }
    }

    public void splitChild(Node x, int i, Node y) throws IOException {
        // Allocate the new node to eof, and move where the current eof is
        Node z = new Node();
        z = writeNode(z);

        z.isLeaf = y.isLeaf;

        // t is the middle key
        int t = order - 1;

        for (int j = 0; j < t; j++) {
            // Copy key and values
            z.keys[j] = y.keys[j + t + 1];
            z.values[j] = y.values[j + t + 1];
            y.keys[j + t + 1] = Long.MAX_VALUE;
            y.values[j + t + 1] = 0L;
        }

        if (!y.isLeaf) {
            for (int j = 0; j < t + 1; j++) {
                z.children[j] = y.children[j + t];
                y.children[j + t] = 0;
            }
        }

        for (int j = x.keys.length - 1; j >= i + 1; j--) {
            x.children[j + 1] = x.children[j];
        }
        x.children[i + 1] = z.offset;

        for (int j = x.keys.length - 2; j >= i; j--) {
            x.keys[j + 1] = x.keys[j];
            x.values[j + 1] = x.values[j];
        }

        x.keys[i] = y.keys[t];
        y.keys[t] = Long.MAX_VALUE;
        x.values[i] = y.values[t];
        y.values[t] = 0L;

        writeNode(y);
        writeNode(z);
        writeNode(x);
    }


    private Node readNode(long offset) throws IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(offset);

        // get isLeaf
        boolean isLeaf = channel.readByte() == (byte) 0x1 ? true : false;

        // get children
        long[] children = new long[2*order];
        for (int i = 0; i < children.length; i++) {
            children[i] = channel.readLong();
        }

        // Keys and values are same length, read at same time
        long[] keys = new long[2*order - 1];
        long[] values = new long[2*order - 1];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = channel.readLong();
            values[i] = channel.readLong();
        }
        
        channel.close();

        return new Node(offset, isLeaf, children, keys, values);
    }

    private Node writeNode(Node node) throws IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        if (node.offset == 0) {
            node.offset = this.eof;
        }
        channel.seek(node.offset);

        // Store isLeaf
        if (node.isLeaf) { channel.writeByte((byte) 0x1); }
        else { channel.writeByte((byte) 0x0); }

        // Store children pointers
        for (int i = 0; i < node.children.length; i++) {
            channel.writeLong(node.children[i]);
        }

        // Store keys and values
        for (int i = 0; i < node.keys.length; i++) {
            channel.writeLong(node.keys[i]);
            channel.writeLong(node.values[i]);
        }

        if (node.offset == eof) {
            this.eof = channel.getFilePointer();
            updateEOF(this.eof);
        }
        channel.close();

        return node;
    }

    private void updateEOF(long eof) throws FileNotFoundException, IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(0);
        channel.writeLong(eof);
        channel.close();
    }

    private void updateRootOffset(long offset) throws FileNotFoundException, IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(8);
        channel.writeLong(offset);
        channel.close();
    }

    private long getRootOffset() throws FileNotFoundException, IOException {
        RandomAccessFile channel = new RandomAccessFile(file, "rw");
        channel.seek(8);
        long position = channel.readLong();
        channel.close();
        return position;
    }
}
