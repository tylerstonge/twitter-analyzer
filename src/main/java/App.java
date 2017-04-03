import java.util.List;

public class App {
    public static void main(String[] args) {
        TwitterReader reader = new TwitterReader();
        List<Tweet> tweets = reader.getTweetsFromUser("potus", 20);
        SentimentAnalyzer sa = SentimentAnalyzer.getInstance();
        for (Tweet t : tweets) {
            System.out.println(t.getText() + " - " + sa.classify(t.getText()));
        }
    }
}
