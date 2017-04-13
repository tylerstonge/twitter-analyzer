import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;

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

    public List<Tweet> readCacheFile(File file) {
        List<Tweet> tweets = new ArrayList<Tweet>();
        try {
            RandomAccessFile f = new RandomAccessFile(file, "rw");
            String username = file.getName();
            int length = f.readInt();
            long timestamp = f.readLong();
            if ((System.currentTimeMillis() / 1000L) - timestamp > threshold) {
                // Update cache if it is older than threshold
                System.out.println("Refreshing cache for " + username);
                f.close();
                file.delete();
                tweets = createCacheFile(username);
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
                if (length > location) {
                    f.seek(location);
                    long tid = f.readLong();
                    if (tid == id) {
                        String sentiment = f.readUTF();
                        String text = f.readUTF();
                        return new Tweet(id, username, text, sentiment, location);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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

            // Write the tweets to file for future access
            for (Tweet t : tweets) {
                t.setLocation(f.getFilePointer());
                f.writeLong(t.getId());
                f.writeUTF(t.getSentiment());
                f.writeUTF(t.getText());
            }

            f.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tweets;
    }

}
