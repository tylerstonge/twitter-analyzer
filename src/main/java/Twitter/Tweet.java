import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

public class Tweet {
    private String author;
    private String text;
    private String sentiment;
    private long id;
    private long location;

    public Tweet(long id, String author, String text, String sentiment, long location) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.sentiment = sentiment;
        this.location = location;
    }

    public Tweet(long id, String author, String text, long location) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.sentiment = SentimentAnalyzer.getInstance().classify(text);
        this.location = location;
    }

    public void setLocation(long location) {
        this.location = location;
    }

    public long getId() {
        return this.id;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getText() {
        return this.text;
    }

    public String getSentiment() {
        return this.sentiment;
    }

    public long getLocation() {
        return this.location;
    }

    /**
    * Calculates the Levenshtein Distance, or edit distance, between two strings
    */
    public static int getLevenshteinDistance(String s, String t) {
        int n = s.length();
        int m = t.length();
        if (n == 0 || m == 0) { return n == 0 ? m : n; }
        int[][] matrix = new int[n + 1][m + 1];

        // initialize first row to 0..n
        for (int i = 0; i < n; i++) {
            matrix[i][0] = i;
        }

        // initialize first column to 0..m
        for (int i = 0; i < m; i++) {
            matrix[0][i] = i;
        }

        // iterate through matrix
        for (int i = 1; i <= n; i++) {
            char si = s.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                char tj = t.charAt(j - 1);
                int cost = si == tj ? 0 : 1;
                matrix[i][j] = Math.min(Math.min(matrix[i - 1][j] + 1, matrix[i][j - 1] + 1), matrix[i - 1][j - 1] + cost);
            }
        }
        return matrix[n][m];
    }
    
    public static Map<String, Integer> getFrequencies(String[] terms) {
        Map<String, Integer> freq = new HashMap<String, Integer>();
        for (String t : terms) {
            Integer n = freq.get(t);
            n = (n == null) ? 1 : n++;
            freq.put(t, n);
        }
        return freq;
    }
    
    public static double cosineSimilarity(String text1, String text2) {
        double dotProduct = 0;
        double magnitudeA = 0;
        double magnitudeB = 0;
        //Get vectors
        Map<String, Integer> a = getFrequencies(text1.split("\\W+"));
        Map<String, Integer> b = getFrequencies(text2.split("\\W+"));

        //Get unique words from both sequences
        HashSet<String> intersection = new HashSet<String>(a.keySet());
        intersection.retainAll(b.keySet());

        //Calculate dot product
        for (String item : intersection) {
            dotProduct += a.get(item) * b.get(item);
        }

        //Calculate magnitude a
        for (String k : a.keySet()) {
            magnitudeA += Math.pow(a.get(k), 2);
        }

        //Calculate magnitude b
        for (String k : b.keySet()) {
            magnitudeB += Math.pow(b.get(k), 2);
        }

        //return cosine similarity
        return dotProduct / Math.sqrt(magnitudeA * magnitudeB);
    }
}
