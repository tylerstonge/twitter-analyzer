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
        PersistentBTree b = new PersistentBTree(5);
        b.insert(1000L, 20L);
        b.printTree();
        // b.insert("Hello", "world");
        // b.insert("Apple", "Bby");
        // b.insert("Bark", "Dude");
        // b.printTree();

        // Cache c = new Cache();
        // List<Tweet> tweets = c.getTweetsFromUser("potus");
        // for (Tweet t : tweets) {
        //     System.out.println(t.getId() + "; " + t.getText());
        // }
    }
}
