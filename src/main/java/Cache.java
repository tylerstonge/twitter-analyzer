import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;

public class Cache {

    private final long threshold = 86400L; // One day in seconds
    File folder;
    TwitterReader twitter;

    public Cache() {
        folder = new File(getClass().getClassLoader().getResource("cache").getFile());
        twitter = new TwitterReader();
    }

    public List<Tweet> getTweetsFromUser(String username) {
        // Each user has their own cache file
        File[] storedUsers = folder.listFiles();

        // If the user is already stored in cache, read that data
        for (int i = 0; i < storedUsers.length; i++) {
            if (storedUsers[i].getName().equals(username)) {
                return readCacheFile(storedUsers[i]);
            }
        }

        // The user is not in the cache already, so pull the data
        return createCacheFile(username);
    }
    
    public boolean cacheIsFresh(String username) {
        // Each user has their own cache file
        File[] storedUsers = folder.listFiles();

        // If the user is already stored in cache, read that data
        for (int i = 0; i < storedUsers.length; i++) {
            if (storedUsers[i].getName().equals(username)) {
                try {
                    RandomAccessFile f = new RandomAccessFile(storedUsers[i], "rw");
                    f.readInt();
                    long timestamp = f.readLong();
                    long currentTime = System.currentTimeMillis() / 1000L;
                    f.close();
                    if (currentTime - timestamp <= threshold)
                        return true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public List<Tweet> readCacheFile(File file) {
        List<Tweet> tweets = new ArrayList<Tweet>();
        try {
            RandomAccessFile f = new RandomAccessFile(file, "rw");
            String username = file.getName();
            int length = f.readInt();
            long timestamp = f.readLong();
            long currentTime = System.currentTimeMillis() / 1000L;
            long latestId = f.readLong();
            if (currentTime - timestamp > threshold) {
                // Update cache if it is older than threshold
                System.out.println("Refreshing cache for " + username);
                
                // Get new tweets
                tweets = twitter.newTweetsFromUser(username, 100, latestId);
                
                // Go to the end of the file
                f.seek(f.length());
                
                // Update cache
                long lastId = 0L;
                for (Tweet t : tweets) {
                    lastId = t.getId();
                    t.setLocation(f.getFilePointer());
                    f.writeLong(t.getId());
                    f.writeUTF(t.getSentiment());
                    f.writeUTF(t.getText());
                }
                
                // update length
                f.seek(0);
                f.writeInt(length + tweets.size());
                
                // update time
                f.writeLong(currentTime);
                
                // Update the latest tweet id
                f.writeLong(lastId);
                
                f.close();
            } else {
                // Cache is fresh, get data
                for (int i = 0; i < length; i++) {
                    long location = f.getFilePointer();
                    long id = f.readLong();
                    String sentiment = f.readUTF();
                    String text = f.readUTF();
                    tweets.add(new Tweet(id, username, text, sentiment, location));
                }
                f.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tweets;
    }

    public Tweet getTweetFromId(long id, long location) {
        File[] storedUsers = folder.listFiles();
        for (File user : storedUsers) {
            try {
                RandomAccessFile f = new RandomAccessFile(user, "rw");
                String username = user.getName();
                int length = f.readInt();
                f.seek(location);
                long tid = f.readLong();
                if (tid == id) {
                    String sentiment = f.readUTF();
                    String text = f.readUTF();
                    return new Tweet(id, username, text, sentiment, location);
                }
                f.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) { }
        }
        return new Tweet(0L, "dropkick", "uh oh!" , "neg", 100101L);
    }

    public List<Tweet> createCacheFile(String username) {
        List<Tweet> tweets = new ArrayList<Tweet>();
        try {
            // Create the file and get tweets
            RandomAccessFile f = new RandomAccessFile(folder.getPath() + "/" + username, "rw");
            tweets = twitter.getTweetsFromUser(username, 100);

            // Store the length of the list
            f.writeInt(tweets.size());

            // Store the current as unix timestamp
            f.writeLong(System.currentTimeMillis() / 1000L);
            
            // Store dummy data to update with LastID
            f.writeLong(0L);

            // Write the tweets to file for future access
            long lastId = 0L;
            for (Tweet t : tweets) {
                lastId = t.getId();
                t.setLocation(f.getFilePointer());
                f.writeLong(t.getId());
                f.writeUTF(t.getSentiment());
                f.writeUTF(t.getText());
            }
            
            // Update the latest tweet id
            f.seek(12);
            f.writeLong(lastId);
            
            f.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tweets;
    }

}
