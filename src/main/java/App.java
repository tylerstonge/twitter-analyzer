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
        BTree b = new BTree(5);
        b.insert("Hello", "world");
        b.insert("Apple", "Bby");
        b.insert("Bark", "Dude");
        b.insert("Zork", "II");
        b.insert("Zark", "II");
        b.insert("Zfrk", "II");
        b.insert("Zzrk", "II");
        b.printTree();

        // Cache c = new Cache();
        // List<Tweet> tweets = c.getTweetsFromUser("potus");
        // for (Tweet t : tweets) {
        //     System.out.println(t.getId() + "; " + t.getText());
        // }
    }
}
