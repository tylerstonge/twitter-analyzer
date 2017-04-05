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
        this.sentiment = sentiment;
    }

    public String getText() {
        return this.text;
    }

    public String getSentiment() {
        return this.sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}
