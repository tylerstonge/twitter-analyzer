import java.util.List;

public class App {

    private static String[] defaultAccounts = new String[] {
        "potus",
        "RogerJStoneJr",
        "KremlinRussia_E",
        "MedvedevRussia",
        "GenFlynn",
        "EricTrump",
        "mike_pence",
        "HassanRouhani",
        "Marine2017_EN",
        "RichardBSpencer",
        "oreillyfactor"
    };

    public static void main(String[] args) {
        // TwitterReader reader = new TwitterReader();
        // for (String account : defaultAccounts) {
        //     List<Tweet> tweets = reader.getTweetsFromUser(account, 20);
        //     SentimentAnalyzer sa = SentimentAnalyzer.getInstance();
        //     System.out.println("----" + account + "----");
        //     for (Tweet t : tweets) {
        //         System.out.println(t.getText() + " - " + sa.classify(t.getText()));
        //     }
        // }
        BTree b = new BTree(4);
    }
}
