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
    private FileChannel file;
    private long eof;

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
        // Get or create the file
        try {
            File f = new File("btree.dat");
            if (!f.exists()) {
                // No BTree data file exists
                f.createNewFile();
                file = new RandomAccessFile(f, "rw").getChannel();
                root = new Node(0, order, true);
                // Keep track of EOF as first long in file
                eof = writeNode(8, root);
                ByteBuffer rawEOF = ByteBuffer.allocate(8);
                rawEOF.putLong(eof);
                file.write(rawEOF, 0);
            } else {
                // There is some BTree data
                file = new RandomAccessFile(f, "rw").getChannel();
                root = readNode(0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insert(long key, long value) {
        Node r = root;
        if (r.isFull()) {
            Node s = new Node(r.offset);
            updateEOF(writeNode(eof, s));
            splitChild(s, 1, r);
        }
    }
    
    public void splitChild(Node x, int i, Node y) {
        Node z = new Node();
    }

    
    private Node readNode(long offset) throws IOException {
        file.position(offset);
        
        // get parent location
        ByteBuffer rParent = ByteBuffer.allocate(8);
        file.read(rParent);
        long parent = rParent.getLong();
        
        // get isLeaf
        ByteBuffer rIsLeaf = ByteBuffer.allocate(1);
        file.read(rIsLeaf);
        boolean isLeaf = rIsLeaf.array()[0] == (byte) 0x1 ? true : false;
        
        // get children
        ByteBuffer rChildren = ByteBuffer.allocate(8 * order);
        file.read(rChildren);
        long[] children = new long[order];
        for (int i = 0; i < order; i++) {
            children[i] = rChildren.getLong();
        }
        
        ByteBuffer rEntries = ByteBuffer.allocate((order - 1) * 16);
        file.read(rEntries);
        long[] entries = new long[(order - 1) * 2];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = rEntries.getLong();
        }
        
        return new Node(offset, parent, isLeaf, children, entries);
    }

    /**
    * Node layout
    *
    * 8 bytes           parent
    * 1 byte            isLeaf
    * 8 * order         children pointers
    * (order - 1) * 16   entries array
    */
    private void writeNode(long offset, Node node) throws IOException {
        file.position(offset);

        // Store parent
        ByteBuffer parent = ByteBuffer.allocate(8);
        parent.putLong(node.parent);
        file.write(parent);

        // Store isLeaf
        ByteBuffer isLeaf = ByteBuffer.allocate(1);
        if (node.isLeaf) { isLeaf.put((byte) 0x1); } 
        else { isLeaf.put((byte) 0x0); }
        file.write(isLeaf);

        // Store children pointers
        ByteBuffer children = ByteBuffer.allocate(8 * order); // 8-bytes per long
        for (int i = 0; i < order; i++) {
            children.putLong(node.children[i]);
        }
        file.write(children);
        
        // Store entries
        ByteBuffer entries = ByteBuffer.allocate((order - 1) * 16);
        for (int i = 0; i < node.entries.length; i++) {
            entries.putLong(node.entries[i]);
        }
        file.write(entries);
        
        return file.position();
    }
    
    private void updateEOF(long eof) {
        ByteBuffer rawEOF = ByteBuffer.allocate(8);
        rawEOF.putLong(eof);
        file.write(eof, 0);
    }
}
