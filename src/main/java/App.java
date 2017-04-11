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
        b.insert(5000L, 30L);
        b.insert(1000L, 30L);
        // b.insert(4000L, 30L);
        // b.insert(2000L, 30L);
        // b.insert(6000L, 30L);
        b.printTree();

        // Cache c = new Cache();
        // List<Tweet> tweets = c.getTweetsFromUser("potus");
        // for (Tweet t : tweets) {
        //     System.out.println(t.getId() + "; " + t.getText());
        // }
    }
}
