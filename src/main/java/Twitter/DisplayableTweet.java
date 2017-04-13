import javafx.beans.property.SimpleStringProperty;

public class DisplayableTweet {
    SimpleStringProperty id = new SimpleStringProperty("");
    SimpleStringProperty author = new SimpleStringProperty("");
    SimpleStringProperty text = new SimpleStringProperty("");
    SimpleStringProperty sentiment = new SimpleStringProperty("");
    
    public DisplayableTweet(String id, String author, String text, String sentiment) {
        setId(id);
        setAuthor(author);
        setText(text);
        setSentiment(sentiment);
    }
    
    public void setId(String id) {
        this.id.set(id);
    }
    
    public String getId() {
        return id.get();
    }
    
    public void setAuthor(String author) {
        this.author.set(author);
    }
    
    public String getAuthor() {
        return author.get();
    }
    
    public void setText(String text) {
        this.text.set(text);
    }
    
    public String getText() {
        return text.get();
    }

    public void setSentiment(String sentiment) {
        this.sentiment.set(sentiment);
    }
    
    public String getSentiment() {
        return sentiment.get();
    }
    
    public double getSimilarity(DisplayableTweet t) {
        double ld = (double) Tweet.getLevenshteinDistance(getText(), t.getText());
        if (t.getSentiment().equals(getSentiment())) {
            ld = ld * 2.0;
        }
        return ld * Tweet.cosineSimilarity(t.getText(), getText());
    }
}