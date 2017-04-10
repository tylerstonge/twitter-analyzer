public class Tweet {
    private String author;
    private String text;
    private String sentiment;

    public Tweet(String author, String text, String sentiment) {
        this.author = author;
        this.text = text;
        this.sentiment = sentiment;
    }

    public Tweet(String author, String text) {
        this.author = author;
        this.text = text;
        this.sentiment = SentimentAnalyzer.getInstance().classify(text);
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
            matrix[0][i] = i;
        }
        
        // initialize first column to 0..m
        for (int i = 0; i < m; i++) {
            matrix[i][0] = i;
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
}
