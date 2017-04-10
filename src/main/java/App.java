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

    // TwitterReader reader = new TwitterReader();
    // for (String account : defaultAccounts) {
    //     List<Tweet> tweets = reader.getTweetsFromUser(account, 20);
    //     SentimentAnalyzer sa = SentimentAnalyzer.getInstance();
    //     System.out.println("----" + account + "----");
    //     for (Tweet t : tweets) {
    //         System.out.println(t.getText() + " - " + sa.classify(t.getText()));
    //     }
    // }

    public static void main(String[] args) {
        // BTree b = new BTree(5);
        // b.insert("Hello", "world");
        // b.insert("Apple", "Bby");
        // b.insert("Bark", "Dude");
        // b.insert("Zork", "II");
        // b.insert("Zark", "II");
        // b.insert("Zfrk", "II");
        // b.insert("Zzrk", "II");
        // b.insert("Aas", "hoole");
        // b.insert("AaBs", "hoole");
        // b.insert("AaAAs", "hoole");
        // b.insert("AaAAAs", "hoole");
        // b.printTree();
        
        Cache c = new Cache();
        List<Tweet> tweets = c.getTweetsFromUser("potus");
        for (Tweet t : tweets) {
            System.out.println(t.getText());
        }
    }
}
