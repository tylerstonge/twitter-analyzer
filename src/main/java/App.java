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
        PersistentBTree b = new PersistentBTree(4);
        b.insert(1000L);
        b.insert(2000L);
        b.insert(3000L);
        b.insert(4000L);
        b.insert(5000L);
        b.insert(6000L);
        b.insert(7000L);
        b.insert(8000L);
        b.insert(9000L);
        b.insert(10000L);
        b.insert(11000L);
        b.insert(12000L);
        b.printTree();

        // Cache c = new Cache();
        // List<Tweet> tweets = c.getTweetsFromUser("potus");
        // for (Tweet t : tweets) {
        //     System.out.println(t.getId() + "; " + t.getText());
        // }
    }
}
