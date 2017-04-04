public class Tweet {
    private String author;
    private String text;
    
    public Tweet(String author, String text) {
        this.author = author;
        this.text = text;
    }
    
    public String getText() {
        return this.text;
    }
}